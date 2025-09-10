package com.example.demo.transaction.service;

import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    
    public List<Transaction> getUnprocessedTransactions() {
        return transactionRepository.findByIsProcessed(false);
    }
    
    public List<Transaction> getTransactionsByAccount(String accountId) {
        return transactionRepository.findByAccountIdAndIsProcessed(accountId, false);
    }
    
    public Long countUnprocessedTransactions() {
        return transactionRepository.countByIsProcessed(false);
    }
    
    public Page<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return transactionRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }
    
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
    
    @Transactional
    public List<Transaction> createTransactions(List<Transaction> transactions) {
        return transactionRepository.saveAll(transactions);
    }
}
