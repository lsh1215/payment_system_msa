package com.example.demo.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // Common Errors (4000~4099)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력값입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON_002", "잘못된 타입입니다."),
    MISSING_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_003", "필수 입력값이 누락되었습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_004", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_005", "허용되지 않은 HTTP 메서드입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON_006", "접근이 거부되었습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_007", "서버 내부 오류가 발생했습니다."),
    
    // Account Errors (4100~4199)
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT_001", "계좌를 찾을 수 없습니다."),
    ACCOUNT_INACTIVE(HttpStatus.BAD_REQUEST, "ACCOUNT_002", "비활성화된 계좌입니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "ACCOUNT_003", "잔고가 부족합니다."),
    ACCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "ACCOUNT_004", "이미 존재하는 계좌입니다."),
    
    // Transaction Errors (4200~4299)
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "TRANSACTION_001", "거래를 찾을 수 없습니다."),
    INVALID_TRANSACTION_AMOUNT(HttpStatus.BAD_REQUEST, "TRANSACTION_002", "유효하지 않은 거래 금액입니다."),
    TRANSACTION_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "TRANSACTION_003", "이미 처리된 거래입니다."),
    
    // Settlement Errors (4300~4399)
    SETTLEMENT_ALREADY_RUNNING(HttpStatus.CONFLICT, "SETTLEMENT_001", "정산이 이미 진행 중입니다."),
    SETTLEMENT_PROCEDURE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SETTLEMENT_002", "정산 프로시저 실행 중 오류가 발생했습니다."),
    SETTLEMENT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_003", "정산 이력을 찾을 수 없습니다."),
    
    // Batch Errors (4400~4499)
    BATCH_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "BATCH_001", "배치 작업을 찾을 수 없습니다."),
    BATCH_JOB_EXECUTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "BATCH_002", "배치 작업 실행 중 오류가 발생했습니다.");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    
    public int getStatus() {
        return httpStatus.value();
    }
}
