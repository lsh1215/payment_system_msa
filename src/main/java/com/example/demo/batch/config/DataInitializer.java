package com.example.demo.batch.config;

import com.example.demo.account.entity.Account;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.batch.util.TransactionDataGenerator;
import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer implements CommandLineRunner {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionDataGenerator dataGenerator;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        
        // 기존 데이터 확인
        long accountCount = accountRepository.count();
        long transactionCount = transactionRepository.count();
        
        log.info("Current data - Accounts: {}, Transactions: {}", accountCount, transactionCount);
        
        // 계좌 데이터가 없으면 생성
        if (accountCount == 0) {
            log.info("Creating initial accounts...");
            List<Account> accounts = dataGenerator.generateAccounts(100);
            accountRepository.saveAll(accounts);
            log.info("Created {} accounts", accounts.size());
        }
        
        // 거래 데이터가 1000건 미만이면 추가 생성
        if (transactionCount < 1000) {
            log.info("Creating additional transactions...");
            List<Account> accounts = accountRepository.findAll();
            List<Transaction> transactions = dataGenerator.generateBulkTransactions(accounts, 100000);
            
            // 메모리 관리를 위해 배치로 저장
            int batchSize = 1000;
            for (int i = 0; i < transactions.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, transactions.size());
                List<Transaction> batch = transactions.subList(i, endIndex);
                transactionRepository.saveAll(batch);
                log.info("Saved transaction batch {}/{}", endIndex, transactions.size());
            }
            
            log.info("Created {} additional transactions", transactions.size());
        }
        
        log.info("Data initialization completed");
    }
}
