package com.example.demo.settlementhistory.service;

import com.example.demo.settlementhistory.entity.SettlementHistory;
import com.example.demo.settlementhistory.repository.SettlementHistoryRepository;
import com.example.demo.settlementhistory.repository.SettlementProcedureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementHistoryService {
    
    private final SettlementHistoryRepository settlementHistoryRepository;
    private final SettlementProcedureRepository settlementProcedureRepository;
    
    public List<SettlementHistory> getAllSettlementHistory() {
        return settlementHistoryRepository.findAll();
    }
    
    public Page<SettlementHistory> getSettlementHistory(Pageable pageable) {
        return settlementHistoryRepository.findAllOrderBySettlementDateDesc(pageable);
    }
    
    public List<SettlementHistory> getSettlementHistoryByDateRange(LocalDate startDate, LocalDate endDate) {
        return settlementHistoryRepository.findBySettlementDateBetween(startDate, endDate);
    }
    
    public Optional<SettlementHistory> getSettlementHistoryByDate(LocalDate date) {
        return settlementHistoryRepository.findBySettlementDateOptional(date);
    }
    
    public List<SettlementHistory> getSettlementHistoryByStatus(SettlementHistory.SettlementStatus status) {
        return settlementHistoryRepository.findByStatus(status);
    }
    
    @Transactional
    public SettlementHistory executeSettlement() {
        log.info("Starting settlement process");
        
        try {
            var result = settlementProcedureRepository.executeSettlementProcedure();
            
            SettlementHistory settlementHistory = SettlementHistory.builder()
                    .settlementDate(LocalDate.now())
                    .processedCount(result.getProcessedCount().intValue())
                    .errorCount(result.getErrorCount().intValue())
                    .status(result.getErrorCount() == 0 ? SettlementHistory.SettlementStatus.SUCCESS : SettlementHistory.SettlementStatus.FAIL)
                    .build();
            
            return settlementHistoryRepository.save(settlementHistory);
            
        } catch (Exception e) {
            log.error("Error occurred during settlement process", e);
            
            SettlementHistory errorHistory = SettlementHistory.builder()
                    .settlementDate(LocalDate.now())
                    .processedCount(0)
                    .errorCount(1)
                    .status(SettlementHistory.SettlementStatus.FAIL)
                    .build();
            
            return settlementHistoryRepository.save(errorHistory);
        }
    }
}
