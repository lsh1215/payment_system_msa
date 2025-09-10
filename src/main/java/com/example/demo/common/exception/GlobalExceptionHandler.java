package com.example.demo.common.exception;

import com.example.demo.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("Business Exception: {}", e.getMessage(), e);
        
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<Void> response = ApiResponse.error(
                errorCode.getStatus(),
                e.getMessage(),
                errorCode.getCode()
        );
        
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
    
    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation Exception: {}", e.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .message("입력값 검증에 실패했습니다.")
                .error(ApiResponse.ErrorDetail.builder()
                        .code("VALIDATION_ERROR")
                        .detail("필드 검증 오류")
                        .fieldErrors(fieldErrors)
                        .build())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Bind 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
        log.error("Bind Exception: {}", e.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .message("요청 파라미터 바인딩에 실패했습니다.")
                .error(ApiResponse.ErrorDetail.builder()
                        .code("BIND_ERROR")
                        .detail("파라미터 바인딩 오류")
                        .fieldErrors(fieldErrors)
                        .build())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("Type Mismatch Exception: {}", e.getMessage());
        
        ErrorCode errorCode = ErrorCode.INVALID_TYPE_VALUE;
        ApiResponse<Void> response = ApiResponse.error(
                errorCode.getStatus(),
                String.format("'%s' 파라미터의 타입이 올바르지 않습니다.", e.getName()),
                errorCode.getCode()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 제약 조건 위반 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Constraint Violation Exception: {}", e.getMessage());
        
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        ApiResponse<Void> response = ApiResponse.error(
                errorCode.getStatus(),
                "입력값 제약 조건을 위반했습니다.",
                errorCode.getCode()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 데이터 무결성 위반 예외 처리
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Data Integrity Violation Exception: {}", e.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.CONFLICT.value(),
                "데이터 무결성 제약 조건을 위반했습니다.",
                "DATA_INTEGRITY_ERROR"
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * HTTP 메시지 읽기 오류 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("Http Message Not Readable Exception: {}", e.getMessage());
        
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        ApiResponse<Void> response = ApiResponse.error(
                errorCode.getStatus(),
                "요청 메시지를 읽을 수 없습니다. JSON 형식을 확인해주세요.",
                errorCode.getCode()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 허용되지 않은 HTTP 메서드 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("Method Not Supported Exception: {}", e.getMessage());
        
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        ApiResponse<Void> response = ApiResponse.error(
                errorCode.getStatus(),
                String.format("'%s' 메서드는 허용되지 않습니다.", e.getMethod()),
                errorCode.getCode()
        );
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }
    
    /**
     * 핸들러를 찾을 수 없는 예외 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("No Handler Found Exception: {}", e.getMessage());
        
        ErrorCode errorCode = ErrorCode.NOT_FOUND;
        ApiResponse<Void> response = ApiResponse.error(
                errorCode.getStatus(),
                String.format("'%s %s' 요청을 처리할 수 없습니다.", e.getHttpMethod(), e.getRequestURL()),
                errorCode.getCode()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected Exception: {}", e.getMessage(), e);
        
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ApiResponse<Void> response = ApiResponse.error(
                errorCode.getStatus(),
                "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.",
                errorCode.getCode()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
