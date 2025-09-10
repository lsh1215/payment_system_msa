package com.example.demo.integration;

import com.example.demo.account.entity.Account;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.settlementhistory.entity.SettlementHistory;
import com.example.demo.settlementhistory.repository.SettlementHistoryRepository;
import com.example.demo.settlementhistory.repository.SettlementProcedureRepository;
import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class SettlementProcedureIntegrationTest extends AbstractStoredProcedureTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("procedure_test_db")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private SettlementHistoryRepository settlementHistoryRepository;
    
    @Autowired
    private SettlementProcedureRepository settlementProcedureRepository;
    
    // JdbcTemplate은 AbstractStoredProcedureTest에서 제공
    
    @BeforeEach
    @Transactional
    void setUp() {
        // 테스트 데이터 정리 (JPA가 스키마를 먼저 생성하도록)
        transactionRepository.deleteAll();
        settlementHistoryRepository.deleteAll();
        accountRepository.deleteAll();
        
        // TestContainer에 운영환경과 동일한 SP 초기화
        initializeStoredProcedure();
        
        // 테스트 계좌 생성
        Account basicAccount = Account.builder()
                .id("BASIC001")
                .balance(new BigDecimal("10000.00"))
                .status(Account.AccountStatus.ACTIVE)
                .accountType(Account.AccountType.BASIC)
                .build();
        
        Account premiumAccount = Account.builder()
                .id("PREMIUM001")
                .balance(new BigDecimal("50000.00"))
                .status(Account.AccountStatus.ACTIVE)
                .accountType(Account.AccountType.PREMIUM)
                .build();
        
        accountRepository.saveAll(List.of(basicAccount, premiumAccount));
        accountRepository.flush(); // 강제로 DB에 저장하여 외래키 제약 조건 해결
        
        // 테스트 거래 생성
        Transaction depositTransaction = Transaction.builder()
                .accountId("BASIC001")
                .amount(new BigDecimal("1000.00"))
                .type(Transaction.TransactionType.DEPOSIT)
                .createdAt(LocalDateTime.now())
                .isProcessed(false)
                .build();
        
        Transaction withdrawalTransaction = Transaction.builder()
                .accountId("PREMIUM001")
                .amount(new BigDecimal("2000.00"))
                .type(Transaction.TransactionType.WITHDRAWAL)
                .createdAt(LocalDateTime.now())
                .isProcessed(false)
                .build();
        
        transactionRepository.saveAll(List.of(depositTransaction, withdrawalTransaction));
    }
    
    @Test
    void testSettlementProcedureExecution() {
        // Given
        List<Transaction> unprocessedTransactions = transactionRepository.findByIsProcessed(false);
        assertThat(unprocessedTransactions).hasSize(2);
        
        // When - Stored Procedure 실행
        var result = settlementProcedureRepository.executeSettlementProcedure();
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProcessedCount()).isGreaterThan(0);
        
        // 정산 이력 확인
        List<SettlementHistory> settlementHistory = settlementHistoryRepository.findAll();
        assertThat(settlementHistory).isNotEmpty();
        
        // 처리된 거래 확인
        List<Transaction> processedTransactions = transactionRepository.findByIsProcessed(true);
        assertThat(processedTransactions).isNotEmpty();
        
        // SP 실행 후 잔고 변경 확인
        Account basicAccount = accountRepository.findById("BASIC001").orElseThrow();
        Account premiumAccount = accountRepository.findById("PREMIUM001").orElseThrow();
        
        // BASIC001: 초기 10,000 + DEPOSIT 1,000 = 11,000
        assertThat(basicAccount.getBalance()).isEqualByComparingTo(new BigDecimal("11000.00"));
        // PREMIUM001: 초기 50,000 - WITHDRAWAL 2,000 = 48,000  
        assertThat(premiumAccount.getBalance()).isEqualByComparingTo(new BigDecimal("48000.00"));
    }
    
    @Test
    void testInsufficientFundsError() {
        // Given - 잔고 부족 상황 생성
        Transaction largeWithdrawal = Transaction.builder()
                .accountId("BASIC001")
                .amount(new BigDecimal("50000.00")) // 잔고(10000)보다 큰 금액
                .type(Transaction.TransactionType.WITHDRAWAL)
                .createdAt(LocalDateTime.now())
                .isProcessed(false)
                .build();
        
        transactionRepository.save(largeWithdrawal);
        
        // When - Stored Procedure 실행
        var result = settlementProcedureRepository.executeSettlementProcedure();
        
        // Then
        assertThat(result).isNotNull();
        // 오류가 발생했으므로 errorCount가 0보다 클 것
        assertThat(result.getErrorCount()).isGreaterThan(0);
        
        // 정산 이력에서 FAIL 상태 확인
        List<SettlementHistory> settlementHistory = settlementHistoryRepository.findAll();
        assertThat(settlementHistory).isNotEmpty();
        assertThat(settlementHistory.get(0).getStatus()).isEqualTo(SettlementHistory.SettlementStatus.FAIL);
    }
    
}
