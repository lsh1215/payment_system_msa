package com.example.demo.settlementhistory.repository;

import com.example.demo.settlementhistory.entity.SettlementHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementHistoryRepository extends JpaRepository<SettlementHistory, Long> {
    
    List<SettlementHistory> findBySettlementDate(LocalDate settlementDate);
    
    List<SettlementHistory> findByStatus(SettlementHistory.SettlementStatus status);
    
    @Query("SELECT s FROM SettlementHistory s WHERE s.settlementDate BETWEEN :startDate AND :endDate ORDER BY s.settlementDate DESC")
    List<SettlementHistory> findBySettlementDateBetween(@Param("startDate") LocalDate startDate, 
                                                       @Param("endDate") LocalDate endDate);
    
    @Query("SELECT s FROM SettlementHistory s ORDER BY s.settlementDate DESC")
    Page<SettlementHistory> findAllOrderBySettlementDateDesc(Pageable pageable);
    
    @Query("SELECT s FROM SettlementHistory s WHERE s.settlementDate = :date")
    Optional<SettlementHistory> findBySettlementDateOptional(@Param("date") LocalDate date);
}
