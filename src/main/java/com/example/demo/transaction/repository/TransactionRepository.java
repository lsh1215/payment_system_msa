package com.example.demo.transaction.repository;

import com.example.demo.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByIsProcessed(Boolean isProcessed);
    
    List<Transaction> findByAccountIdAndIsProcessed(String accountId, Boolean isProcessed);
    
    List<Transaction> findByTypeAndIsProcessed(Transaction.TransactionType type, Boolean isProcessed);
    
    @Query("SELECT t FROM Transaction t WHERE t.isProcessed = :isProcessed ORDER BY t.createdAt ASC")
    List<Transaction> findUnprocessedTransactionsOrderByCreatedAt(@Param("isProcessed") Boolean isProcessed);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.isProcessed = :isProcessed")
    Long countByIsProcessed(@Param("isProcessed") Boolean isProcessed);
    
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    Page<Transaction> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate, 
                                           Pageable pageable);
}
