package com.example.demo.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공통 API 응답 구조
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "공통 API 응답")
public class ApiResponse<T> {
    
    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    
    @Schema(description = "HTTP 상태 코드", example = "200")
    private int status;
    
    @Schema(description = "응답 메시지", example = "성공적으로 처리되었습니다.")
    private String message;
    
    @Schema(description = "응답 데이터")
    private T data;
    
    @Schema(description = "오류 상세 정보")
    private ErrorDetail error;
    
    @Schema(description = "응답 시간")
    private LocalDateTime timestamp;
    
    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "성공적으로 처리되었습니다.");
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> created(T data) {
        return created(data, "성공적으로 생성되었습니다.");
    }
    
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(201)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 오류 응답 생성
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return error(status, message, null);
    }
    
    public static <T> ApiResponse<T> error(int status, String message, String detail) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .error(ErrorDetail.builder()
                        .code(String.valueOf(status))
                        .detail(detail)
                        .build())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Schema(description = "오류 상세 정보")
    public static class ErrorDetail {
        
        @Schema(description = "오류 코드", example = "VALIDATION_ERROR")
        private String code;
        
        @Schema(description = "오류 상세 메시지", example = "필수 필드가 누락되었습니다.")
        private String detail;
        
        @Schema(description = "필드별 오류 정보")
        private Object fieldErrors;
    }
}
