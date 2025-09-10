-- 테스트용 간단한 SP_SETTLE_ACCOUNTS
-- TestContainer 환경에서 안정적으로 실행되는 버전

DROP PROCEDURE IF EXISTS SP_SETTLE_ACCOUNTS;

CREATE PROCEDURE SP_SETTLE_ACCOUNTS(OUT processed_count INT, OUT error_count INT)
BEGIN
    DECLARE v_processed_count INT DEFAULT 0;
    DECLARE v_error_count INT DEFAULT 0;
    
    -- 간단한 거래 처리
    UPDATE transactions t
    JOIN accounts a ON t.account_id = a.id
    SET t.is_processed = TRUE,
        a.balance = CASE 
            WHEN t.type = 'DEPOSIT' THEN a.balance + t.amount
            WHEN t.type = 'WITHDRAWAL' AND a.balance >= t.amount THEN a.balance - t.amount
            ELSE a.balance
        END,
        a.updated_at = NOW()
    WHERE t.is_processed = FALSE 
      AND a.status = 'ACTIVE'
      AND (t.type = 'DEPOSIT' OR (t.type = 'WITHDRAWAL' AND a.balance >= t.amount));
    
    SET v_processed_count = ROW_COUNT();
    
    -- 잔고 부족한 출금 거래는 에러로 처리
    INSERT INTO transaction_errors (transaction_id, error_code, error_message, logged_at)
    SELECT t.id, 'INSUFFICIENT_FUNDS', 'Insufficient balance', NOW()
    FROM transactions t
    JOIN accounts a ON t.account_id = a.id
    WHERE t.is_processed = FALSE 
      AND t.type = 'WITHDRAWAL' 
      AND a.balance < t.amount;
    
    SET v_error_count = ROW_COUNT();
    
    -- 정산 이력 생성
    INSERT INTO settlement_history (settlement_date, processed_count, error_count, status, created_at)
    VALUES (CURDATE(), v_processed_count, v_error_count, 
            CASE WHEN v_error_count = 0 THEN 'SUCCESS' ELSE 'FAIL' END, NOW());
    
    -- 결과 반환
    SET processed_count = v_processed_count;
    SET error_count = v_error_count;
END;