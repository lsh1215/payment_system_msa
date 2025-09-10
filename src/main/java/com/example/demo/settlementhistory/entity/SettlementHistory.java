package com.example.demo.settlementhistory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;
    
    @Column(name = "processed_count", nullable = false)
    private Integer processedCount;
    
    @Column(name = "error_count", nullable = false)
    private Integer errorCount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SettlementStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public enum SettlementStatus {
        SUCCESS, FAIL
    }
}
