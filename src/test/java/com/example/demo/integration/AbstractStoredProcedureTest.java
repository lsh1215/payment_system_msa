package com.example.demo.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.FileCopyUtils;

import jakarta.annotation.PostConstruct;

/**
 * TestContainers 환경에서 운영 DB와 동일한 Stored Procedure를 로딩하는 기본 클래스
 * 
 * 실무 패턴: 테스트 초기화 스크립트로 프로시저 생성
 * - 운영 환경과 동일한 프로시저를 테스트 컨테이너에 적용
 * - 외부 SQL 파일 활용으로 코드 중복 제거
 */
public abstract class AbstractStoredProcedureTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    private String storedProcedureSQL;

    @PostConstruct
    private void loadStoredProcedureSQL() {
        try {
            var resource = resourceLoader.getResource("classpath:test-procedures.sql");
            var bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            var fullSQL = new String(bytes, "UTF-8");
            
            this.storedProcedureSQL = extractProcedureSQL(fullSQL);
            
        } catch (Exception e) {
            throw new RuntimeException("SP 로딩 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * SQL 파일에서 CREATE PROCEDURE 추출 (DELIMITER 처리 포함)
     */
    private String extractProcedureSQL(String fullSQL) {
        var delimiterStart = fullSQL.indexOf("DELIMITER $$");
        
        if (delimiterStart != -1) {
            // DELIMITER $$ 방식
            var delimiterEnd = fullSQL.indexOf("DELIMITER ;");
            var procedureSection = fullSQL.substring(delimiterStart + "DELIMITER $$".length(), delimiterEnd);
            var createStart = procedureSection.indexOf("CREATE PROCEDURE");
            
            return procedureSection.substring(createStart).replaceAll("\\$\\$", "").trim();
        } else {
            // 일반 방식
            var createStart = fullSQL.indexOf("CREATE PROCEDURE");
            var fromCreate = fullSQL.substring(createStart);
            var lastEndIndex = fromCreate.lastIndexOf("END");
            
            return fromCreate.substring(0, lastEndIndex + 3).trim();
        }
    }
    

    /**
     * TestContainer 초기화: 운영 환경과 동일한 SP를 생성
     * 
     * 실무에서 사용하는 패턴 - 테이블 생성 후 SP 초기화
     */
    protected void initializeStoredProcedure() {
        try {
            jdbcTemplate.execute("DROP PROCEDURE IF EXISTS SP_SETTLE_ACCOUNTS");
            jdbcTemplate.execute(storedProcedureSQL);
        } catch (Exception e) {
            throw new RuntimeException("SP 초기화 실패: " + e.getMessage(), e);
        }
    }
}
