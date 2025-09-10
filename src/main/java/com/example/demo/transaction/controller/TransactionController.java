package com.example.demo.transaction.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.service.TransactionService;
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

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Transaction>>> getAllTransactions(Pageable pageable) {
        Page<Transaction> transactions = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<Page<Transaction>>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<Transaction> transactions = transactionService.findTransactionsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<Page<Transaction>>> getTransactionsByAccountId(
            @PathVariable String accountId,
            Pageable pageable) {
        Page<Transaction> transactions = transactionService.findTransactionsByAccountId(accountId, pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/unprocessed")
    public ResponseEntity<ApiResponse<Page<Transaction>>> getUnprocessedTransactions(Pageable pageable) {
        Page<Transaction> transactions = transactionService.findUnprocessedTransactions(pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Transaction>> createTransaction(
            @Valid @RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdTransaction));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<Transaction>>> createTransactions(
            @Valid @RequestBody List<Transaction> transactions) {
        List<Transaction> createdTransactions = transactionService.createTransactions(transactions);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdTransactions));
    }
}
