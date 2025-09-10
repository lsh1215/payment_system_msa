package com.example.demo.settlementhistory.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ErrorCode;
import com.example.demo.settlementhistory.entity.SettlementHistory;
import com.example.demo.settlementhistory.service.SettlementHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "정산 관리", description = "정산 관리 API")
@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementHistoryController {
    
    private final SettlementHistoryService settlementHistoryService;
    private final JobLauncher jobLauncher;
    private final Job settlementJob;
    
    @Operation(summary = "정산 이력 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SettlementHistory>>> getSettlementHistory(Pageable pageable) {
        Page<SettlementHistory> history = settlementHistoryService.getSettlementHistory(pageable);
        return ResponseEntity.ok(ApiResponse.success(history, "정산 이력을 성공적으로 조회했습니다."));
    }
    
    @Operation(summary = "기간별 정산 이력 조회")
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<SettlementHistory>>> getSettlementHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<SettlementHistory> history = settlementHistoryService.getSettlementHistoryByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(history, "기간별 정산 이력을 성공적으로 조회했습니다."));
    }
    
    @Operation(summary = "일자별 정산 이력 조회")
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<SettlementHistory>> getSettlementHistoryByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        SettlementHistory history = settlementHistoryService.getSettlementHistoryByDate(date)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_HISTORY_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(history, "일자별 정산 이력을 성공적으로 조회했습니다."));
    }
    
    @Operation(summary = "상태별 정산 이력 조회")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<SettlementHistory>>> getSettlementHistoryByStatus(
            @PathVariable SettlementHistory.SettlementStatus status) {
        List<SettlementHistory> history = settlementHistoryService.getSettlementHistoryByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(history, "상태별 정산 이력을 성공적으로 조회했습니다."));
    }
    
    @Operation(summary = "정산 수동 실행")
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<SettlementHistory>> runSettlement() {
        try {
            SettlementHistory result = settlementHistoryService.executeSettlement();
            return ResponseEntity.ok(ApiResponse.success(result, "정산이 성공적으로 실행되었습니다."));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SETTLEMENT_PROCEDURE_ERROR, e.getMessage());
        }
    }
    
    @Operation(summary = "정산 배치 실행")
    @PostMapping("/run-batch")
    public ResponseEntity<ApiResponse<String>> runSettlementBatch() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(settlementJob, jobParameters);
            return ResponseEntity.ok(ApiResponse.success("배치 작업 ID: " + System.currentTimeMillis(), 
                    "정산 배치 작업이 성공적으로 시작되었습니다."));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BATCH_JOB_EXECUTION_ERROR, e.getMessage());
        }
    }
}
