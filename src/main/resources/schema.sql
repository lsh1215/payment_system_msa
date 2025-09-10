-- Create database schema for settlement system

-- 계좌 테이블
CREATE TABLE IF NOT EXISTS accounts (
    id VARCHAR(50) PRIMARY KEY,
    balance DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 거래 테이블
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_processed BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_account_id (account_id),
    INDEX idx_is_processed (is_processed),
    INDEX idx_created_at (created_at)
);

-- 정산 이력 테이블
CREATE TABLE IF NOT EXISTS settlement_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    settlement_date DATE NOT NULL,
    processed_count INT NOT NULL,
    error_count INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_settlement_date (settlement_date)
);

-- 거래 오류 테이블
CREATE TABLE IF NOT EXISTS transaction_errors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    error_code VARCHAR(50) NOT NULL,
    error_message VARCHAR(500),
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transaction_id (transaction_id)
);
