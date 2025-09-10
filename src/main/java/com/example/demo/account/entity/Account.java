package com.example.demo.account.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @Column(name = "id", length = 50)
    private String id;
    
    @Column(name = "balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal balance;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AccountStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", length = 20, nullable = false)
    private AccountType accountType;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum AccountStatus {
        ACTIVE, INACTIVE
    }
    
    public enum AccountType {
        BASIC, PREMIUM
    }
}
