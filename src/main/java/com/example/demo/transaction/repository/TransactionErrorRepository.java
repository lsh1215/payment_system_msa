package com.example.demo.transaction.repository;

import com.example.demo.transaction.entity.TransactionError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionErrorRepository extends JpaRepository<TransactionError, Long> {
    
    List<TransactionError> findByTransactionId(Long transactionId);
    
    List<TransactionError> findByErrorCode(String errorCode);
    
    List<TransactionError> findByLoggedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
