-- Stored Procedure: SP_SETTLE_ACCOUNTS
DELIMITER $$

DROP PROCEDURE IF EXISTS SP_SETTLE_ACCOUNTS$$

CREATE PROCEDURE SP_SETTLE_ACCOUNTS(OUT processed_count INT, OUT error_count INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_transaction_id BIGINT;
    DECLARE v_account_id VARCHAR(50);
    DECLARE v_amount DECIMAL(15,2);
    DECLARE v_type VARCHAR(20);
    DECLARE v_current_balance DECIMAL(15,2);
    DECLARE v_account_type VARCHAR(20);
    DECLARE v_processed_count INT DEFAULT 0;
    DECLARE v_error_count INT DEFAULT 0;
    DECLARE v_settlement_date DATE DEFAULT CURDATE();
    
    -- 커서 선언
    DECLARE transaction_cursor CURSOR FOR
        SELECT id, account_id, amount, type
        FROM transactions
        WHERE is_processed = FALSE
        ORDER BY created_at ASC;
    
    -- 예외 처리 핸들러
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    -- 트랜잭션 시작
    START TRANSACTION;
    
    -- 커서 열기
    OPEN transaction_cursor;
    
    -- 커서 루프
    read_loop: LOOP
        FETCH transaction_cursor INTO v_transaction_id, v_account_id, v_amount, v_type;
        
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 계좌 정보 조회
        SELECT balance, account_type INTO v_current_balance, v_account_type
        FROM accounts
        WHERE id = v_account_id AND status = 'ACTIVE';
        
        -- 계좌가 존재하지 않거나 비활성화된 경우
        IF v_current_balance IS NULL THEN
            INSERT INTO transaction_errors (transaction_id, error_code, error_message)
            VALUES (v_transaction_id, 'ACCOUNT_NOT_FOUND', 'Account not found or inactive');
            SET v_error_count = v_error_count + 1;
            ITERATE read_loop;
        END IF;
        
        -- 거래 처리
        IF v_type = 'DEPOSIT' THEN
            -- 입금 처리
            IF v_account_type = 'BASIC' THEN
                -- BASIC 계좌는 0.5% 보너스 추가
                SET v_amount = v_amount * 1.005;
            END IF;
            
            UPDATE accounts 
            SET balance = balance + v_amount, updated_at = CURRENT_TIMESTAMP
            WHERE id = v_account_id;
            
        ELSEIF v_type = 'WITHDRAWAL' THEN
            -- 출금 처리
            DECLARE v_fee DECIMAL(15,2) DEFAULT 0;
            
            -- PREMIUM 계좌는 1% 수수료
            IF v_account_type = 'PREMIUM' THEN
                SET v_fee = v_amount * 0.01;
                SET v_amount = v_amount + v_fee;
            END IF;
            
            -- 잔고 확인
            IF v_current_balance < v_amount THEN
                INSERT INTO transaction_errors (transaction_id, error_code, error_message)
                VALUES (v_transaction_id, 'INSUFFICIENT_FUNDS', 
                        CONCAT('Insufficient funds. Required: ', v_amount, ', Available: ', v_current_balance));
                SET v_error_count = v_error_count + 1;
                ITERATE read_loop;
            END IF;
            
            UPDATE accounts 
            SET balance = balance - v_amount, updated_at = CURRENT_TIMESTAMP
            WHERE id = v_account_id;
        END IF;
        
        -- 거래 처리 완료 표시
        UPDATE transactions 
        SET is_processed = TRUE 
        WHERE id = v_transaction_id;
        
        SET v_processed_count = v_processed_count + 1;
        
    END LOOP;
    
    -- 커서 닫기
    CLOSE transaction_cursor;
    
    -- 정산 이력 기록
    INSERT INTO settlement_history (settlement_date, processed_count, error_count, status)
    VALUES (v_settlement_date, v_processed_count, v_error_count, 
            CASE WHEN v_error_count = 0 THEN 'SUCCESS' ELSE 'FAIL' END);
    
    -- 트랜잭션 커밋
    COMMIT;
    
    -- 결과 반환
    SET processed_count = v_processed_count;
    SET error_count = v_error_count;
    
END$$

DELIMITER ;
