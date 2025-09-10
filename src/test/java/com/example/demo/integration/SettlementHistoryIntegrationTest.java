package com.example.demo.integration;

import com.example.demo.account.entity.Account;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.settlementhistory.entity.SettlementHistory;
import com.example.demo.settlementhistory.repository.SettlementHistoryRepository;
import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
@ActiveProfiles("test")
class SettlementHistoryIntegrationTest extends AbstractStoredProcedureTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("settlement_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private SettlementHistoryRepository settlementHistoryRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Given: 테스트 데이터 정리
        transactionRepository.deleteAll();
        settlementHistoryRepository.deleteAll();
        accountRepository.deleteAll();
        
        // Stored Procedure 생성 (Main 프로젝트의 procedures.sql 사용)
        createSettlementProcedure();
        
        // MockMvc 설정
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testGetSettlementHistory() throws Exception {
        // Given: 정산 이력 데이터 생성
        SettlementHistory settlementHistory = SettlementHistory.builder()
                .settlementDate(LocalDate.now())
                .processedCount(5)
                .errorCount(0)
                .status(SettlementHistory.SettlementStatus.SUCCESS)
                .build();
        settlementHistoryRepository.save(settlementHistory);

        // When & Then: GET /api/v1/settlements 호출
        mockMvc.perform(get("/api/v1/settlements"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].processedCount").value(5))
                .andExpect(jsonPath("$[0].errorCount").value(0))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    void testRunSettlementManually() throws Exception {
        // Given: 테스트 데이터 생성
        Account account = Account.builder()
                .id("TEST_ACCOUNT_API")
                .accountType(Account.AccountType.BASIC)
                .balance(new BigDecimal("1000.00"))
                .status(Account.AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .accountId("TEST_ACCOUNT_API")
                .amount(new BigDecimal("100.00"))
                .type(Transaction.TransactionType.DEPOSIT)
                .isProcessed(false)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        // When & Then: POST /api/v1/settlements/run 호출
        mockMvc.perform(post("/api/v1/settlements/run"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Settlement job started successfully"));

        // 정산 이력이 생성되었는지 확인
        List<SettlementHistory> settlementHistory = settlementHistoryRepository.findAll();
        assertThat(settlementHistory).isNotEmpty();
    }

    @Test
    void testGetSettlementHistoryWithEmptyData() throws Exception {
        // Given: 정산 이력이 없는 상태

        // When & Then: GET /api/v1/settlements 호출
        mockMvc.perform(get("/api/v1/settlements"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    private void createSettlementProcedure() {
        try {
            // 1. 기존 Stored Procedure 삭제
            try {
                jdbcTemplate.execute("DROP PROCEDURE IF EXISTS SP_SETTLE_ACCOUNTS");
            } catch (Exception e) {
                // 무시
            }
            
            // 2. Main 프로젝트의 procedures.sql 파일을 읽어서 실행
            ClassPathResource resource = new ClassPathResource("procedures.sql");
            byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            String sql = new String(bytes, "UTF-8");
            
            // DELIMITER 구문을 제거하고 실행
            String[] statements = sql.split("DELIMITER \\$\\$");
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--") && !trimmed.equals("DELIMITER ;")) {
                    // DELIMITER $$ 제거하고 실행
                    trimmed = trimmed.replaceAll("\\$\\$", "").trim();
                    if (!trimmed.isEmpty()) {
                        try {
                            jdbcTemplate.execute(trimmed);
                        } catch (Exception e) {
                            // 이미 존재하는 경우 무시
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create stored procedure", e);
        }
    }
}
