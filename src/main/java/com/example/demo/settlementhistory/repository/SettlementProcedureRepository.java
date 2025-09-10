package com.example.demo.settlementhistory.repository;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.stereotype.Repository;

@Repository
public class SettlementProcedureRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Data
    public static class SettlementResult {
        private Long processedCount;
        private Long errorCount;
    }
    
    public SettlementResult executeSettlementProcedure() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("SP_SETTLE_ACCOUNTS");
        query.registerStoredProcedureParameter("processed_count", Integer.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("error_count", Integer.class, ParameterMode.OUT);
        query.execute();
        
        SettlementResult result = new SettlementResult();
        result.setProcessedCount(((Integer) query.getOutputParameterValue("processed_count")).longValue());
        result.setErrorCount(((Integer) query.getOutputParameterValue("error_count")).longValue());
        
        return result;
    }
}
