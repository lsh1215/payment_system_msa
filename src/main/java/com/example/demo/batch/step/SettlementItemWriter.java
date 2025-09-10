package com.example.demo.batch.step;

import com.example.demo.settlementhistory.repository.SettlementProcedureRepository;
import com.example.demo.transaction.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementItemWriter implements ItemWriter<Transaction> {
    
    private final SettlementProcedureRepository settlementProcedureRepository;
    
    @Override
    public void write(Chunk<? extends Transaction> chunk) throws Exception {
        log.info("Starting settlement process for {} transactions", chunk.size());
        
        try {
            // Stored Procedure 호출하여 정산 처리
            var result = settlementProcedureRepository.executeSettlementProcedure();
            
            log.info("Settlement completed - Processed: {}, Errors: {}", 
                    result.getProcessedCount(), result.getErrorCount());
            
        } catch (Exception e) {
            log.error("Error occurred during settlement process", e);
            throw e;
        }
    }
}
