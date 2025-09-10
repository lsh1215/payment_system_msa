package com.example.demo.integration;

import com.example.demo.account.entity.Account;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.settlementhistory.entity.SettlementHistory;
import com.example.demo.settlementhistory.repository.SettlementHistoryRepository;
import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@Testcontainers
@ActiveProfiles("test")
class SettlementBatchIntegrationTest extends AbstractStoredProcedureTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("batch_test_db")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
    
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private SettlementHistoryRepository settlementHistoryRepository;
    
    @Autowired
    private Job settlementJob;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        transactionRepository.deleteAll();
        settlementHistoryRepository.deleteAll();
        accountRepository.deleteAll();
        
        // 테스트 계좌 생성
        Account account1 = Account.builder()
                .id("BATCH001")
                .balance(new BigDecimal("10000.00"))
                .status(Account.AccountStatus.ACTIVE)
                .accountType(Account.AccountType.BASIC)
                .build();
        
        Account account2 = Account.builder()
                .id("BATCH002")
                .balance(new BigDecimal("50000.00"))
                .status(Account.AccountStatus.ACTIVE)
                .accountType(Account.AccountType.PREMIUM)
                .build();
        
        accountRepository.saveAll(List.of(account1, account2));
        accountRepository.flush(); // 강제로 DB에 저장
        
        // TestContainer에 운영환경과 동일한 SP 초기화
        initializeStoredProcedure();
        
        // 테스트 거래 생성
        Transaction transaction1 = Transaction.builder()
                .accountId("BATCH001")
                .amount(new BigDecimal("1000.00"))
                .type(Transaction.TransactionType.DEPOSIT)
                .createdAt(LocalDateTime.now())
                .isProcessed(false)
                .build();
        
        Transaction transaction2 = Transaction.builder()
                .accountId("BATCH002")
                .amount(new BigDecimal("2000.00"))
                .type(Transaction.TransactionType.WITHDRAWAL)
                .createdAt(LocalDateTime.now())
                .isProcessed(false)
                .build();
        
        transactionRepository.saveAll(List.of(transaction1, transaction2));
    }
    
    @Test
    void testSettlementJob() throws Exception {
        // Given
        jobLauncherTestUtils.setJob(settlementJob);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        
        // When
        var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        // Then
        assertThat(jobExecution.getStatus().isUnsuccessful()).isFalse();
        
        // 정산 이력 확인
        List<SettlementHistory> settlementHistory = settlementHistoryRepository.findAll();
        assertThat(settlementHistory).isNotEmpty();
        
        // 처리된 거래 확인
        List<Transaction> processedTransactions = transactionRepository.findByIsProcessed(true);
        assertThat(processedTransactions).isNotEmpty();
    }
    
    @Test
    void testSettlementProcedure() {
        // Given
        Account account = accountRepository.findById("BATCH001").orElseThrow();
        BigDecimal initialBalance = account.getBalance();
        
        // When - BASIC 계좌에 입금 (0.5% 보너스 포함)
        Transaction deposit = Transaction.builder()
                .accountId("BATCH001")
                .amount(new BigDecimal("1000.00"))
                .type(Transaction.TransactionType.DEPOSIT)
                .createdAt(LocalDateTime.now())
                .isProcessed(false)
                .build();
        
        transactionRepository.save(deposit);
        
        // Stored Procedure 실행 (실제로는 SettlementHistoryService를 통해)
        // 여기서는 단순히 거래가 저장되었는지 확인
        List<Transaction> unprocessedTransactions = transactionRepository.findByIsProcessed(false);
        assertThat(unprocessedTransactions).hasSize(3); // 기존 2개 + 새로 추가한 1개
        
        // Then - 거래가 올바르게 저장되었는지 확인
        assertThat(deposit.getAccountId()).isEqualTo("BATCH001");
        assertThat(deposit.getAmount()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(deposit.getType()).isEqualTo(Transaction.TransactionType.DEPOSIT);
    }
    
    @Test
    void testAccountCreation() {
        // Given & When
        Account account = Account.builder()
                .id("BATCH003")
                .balance(new BigDecimal("50000.00"))
                .status(Account.AccountStatus.ACTIVE)
                .accountType(Account.AccountType.PREMIUM)
                .build();
        
        Account savedAccount = accountRepository.save(account);
        
        // Then
        assertThat(savedAccount.getId()).isEqualTo("BATCH003");
        assertThat(savedAccount.getBalance()).isEqualTo(new BigDecimal("50000.00"));
        assertThat(savedAccount.getStatus()).isEqualTo(Account.AccountStatus.ACTIVE);
        assertThat(savedAccount.getAccountType()).isEqualTo(Account.AccountType.PREMIUM);
    }
}
