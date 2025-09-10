package com.example.demo.transaction.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Transaction API", description = "거래 관련 API")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "전체 거래 조회", description = "페이징 처리된 전체 거래 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Transaction>>> getAllTransactions(Pageable pageable) {
        Page<Transaction> transactions = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @Operation(summary = "기간별 거래 조회", description = "특정 기간 동안의 거래를 페이징하여 조회합니다.")
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<Page<Transaction>>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<Transaction> transactions = transactionService.findTransactionsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @Operation(summary = "계좌별 거래 조회", description = "특정 계좌의 거래를 페이징하여 조회합니다.")
    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<Page<Transaction>>> getTransactionsByAccountId(
            @PathVariable String accountId,
            Pageable pageable) {
        Page<Transaction> transactions = transactionService.findTransactionsByAccountId(accountId, pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @Operation(summary = "미처리 거래 조회", description = "아직 처리되지 않은 거래를 페이징하여 조회합니다.")
    @GetMapping("/unprocessed")
    public ResponseEntity<ApiResponse<Page<Transaction>>> getUnprocessedTransactions(Pageable pageable) {
        Page<Transaction> transactions = transactionService.findUnprocessedTransactions(pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @Operation(summary = "거래 생성", description = "새로운 거래를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Transaction>> createTransaction(
            @Valid @RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdTransaction));
    }

    @Operation(summary = "거래 일괄 생성", description = "여러 거래를 한 번에 생성합니다.")
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<Transaction>>> createTransactions(
            @Valid @RequestBody List<Transaction> transactions) {
        List<Transaction> createdTransactions = transactionService.createTransactions(transactions);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdTransactions));
    }
}
