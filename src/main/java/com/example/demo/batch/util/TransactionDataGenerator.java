package com.example.demo.batch.util;

import com.example.demo.account.entity.Account;
import com.example.demo.transaction.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class TransactionDataGenerator {
    
    private final Random random = new Random();
    
    /**
     * 계좌 데이터 생성
     */
    public List<Account> generateAccounts(int count) {
        List<Account> accounts = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            Account.AccountType accountType = i % 3 == 0 ? Account.AccountType.PREMIUM : Account.AccountType.BASIC;
            BigDecimal balance = BigDecimal.valueOf(random.nextDouble() * 100000)
                    .setScale(2, RoundingMode.HALF_UP);
            
            Account account = Account.builder()
                    .id("ACC" + String.format("%06d", i))
                    .balance(balance)
                    .status(Account.AccountStatus.ACTIVE)
                    .accountType(accountType)
                    .build();
            
            accounts.add(account);
        }
        
        log.info("Generated {} accounts", count);
        return accounts;
    }
    
    /**
     * 거래 데이터 생성
     */
    public List<Transaction> generateTransactions(List<Account> accounts, int count) {
        List<Transaction> transactions = new ArrayList<>();
        LocalDateTime baseDate = LocalDateTime.now().minusDays(30);
        
        for (int i = 1; i <= count; i++) {
            Account account = accounts.get(random.nextInt(accounts.size()));
            Transaction.TransactionType type = random.nextBoolean() ? 
                    Transaction.TransactionType.DEPOSIT : Transaction.TransactionType.WITHDRAWAL;
            
            BigDecimal amount = BigDecimal.valueOf(random.nextDouble() * 10000 + 100)
                    .setScale(2, RoundingMode.HALF_UP);
            
            // 출금의 경우 계좌 잔고를 초과하지 않도록 제한
            if (type == Transaction.TransactionType.WITHDRAWAL && amount.compareTo(account.getBalance()) > 0) {
                amount = account.getBalance().multiply(BigDecimal.valueOf(0.1 + random.nextDouble() * 0.8))
                        .setScale(2, RoundingMode.HALF_UP);
            }
            
            LocalDateTime createdAt = baseDate.plusDays(random.nextInt(30))
                    .plusHours(random.nextInt(24))
                    .plusMinutes(random.nextInt(60));
            
            Transaction transaction = Transaction.builder()
                    .accountId(account.getId())
                    .amount(amount)
                    .type(type)
                    .createdAt(createdAt)
                    .isProcessed(false)
                    .build();
            
            transactions.add(transaction);
        }
        
        log.info("Generated {} transactions", count);
        return transactions;
    }
    
    /**
     * 대량 거래 데이터 생성 (10만 건 이상)
     */
    public List<Transaction> generateBulkTransactions(List<Account> accounts, int count) {
        List<Transaction> transactions = new ArrayList<>();
        LocalDateTime baseDate = LocalDateTime.now().minusDays(90);
        
        for (int i = 1; i <= count; i++) {
            Account account = accounts.get(random.nextInt(accounts.size()));
            Transaction.TransactionType type = random.nextBoolean() ? 
                    Transaction.TransactionType.DEPOSIT : Transaction.TransactionType.WITHDRAWAL;
            
            BigDecimal amount = BigDecimal.valueOf(random.nextDouble() * 50000 + 50)
                    .setScale(2, RoundingMode.HALF_UP);
            
            // 출금의 경우 계좌 잔고를 초과하지 않도록 제한
            if (type == Transaction.TransactionType.WITHDRAWAL && amount.compareTo(account.getBalance()) > 0) {
                amount = account.getBalance().multiply(BigDecimal.valueOf(0.1 + random.nextDouble() * 0.5))
                        .setScale(2, RoundingMode.HALF_UP);
            }
            
            LocalDateTime createdAt = baseDate.plusDays(random.nextInt(90))
                    .plusHours(random.nextInt(24))
                    .plusMinutes(random.nextInt(60))
                    .plusSeconds(random.nextInt(60));
            
            Transaction transaction = Transaction.builder()
                    .accountId(account.getId())
                    .amount(amount)
                    .type(type)
                    .createdAt(createdAt)
                    .isProcessed(false)
                    .build();
            
            transactions.add(transaction);
            
            // 메모리 관리를 위해 1000건씩 로그 출력
            if (i % 10000 == 0) {
                log.info("Generated {} transactions so far...", i);
            }
        }
        
        log.info("Generated {} bulk transactions", count);
        return transactions;
    }
}
