package com.example.demo.transaction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_errors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionError {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;
    
    @Column(name = "error_code", length = 50, nullable = false)
    private String errorCode;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @CreationTimestamp
    @Column(name = "logged_at")
    private LocalDateTime loggedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    private Transaction transaction;
}
