package com.example.demo.transaction.entity;

import com.example.demo.account.entity.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_id", length = 50, nullable = false)
    private String accountId;
    
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private TransactionType type;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "is_processed", nullable = false)
    private Boolean isProcessed;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;
    
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL
    }
}
