package com.example.demo.account.controller;

import com.example.demo.account.entity.Account;
import com.example.demo.account.service.AccountService;
import com.example.demo.common.dto.ApiResponse;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "계좌 관리", description = "계좌 관리 API")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    
    private final AccountService accountService;
    
    @Operation(summary = "전체 계좌 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Account>>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.success(accounts, "계좌 목록을 성공적으로 조회했습니다."));
    }
    
    @Operation(summary = "계좌 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Account>> getAccountById(@PathVariable String id) {
        Account account = accountService.getAccountById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(account, "계좌 정보를 성공적으로 조회했습니다."));
    }
    
    @Operation(summary = "상태별 계좌 조회")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Account>>> getAccountsByStatus(@PathVariable Account.AccountStatus status) {
        List<Account> accounts = accountService.getAccountsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(accounts, "상태별 계좌 목록을 성공적으로 조회했습니다."));
    }
    
    @Operation(summary = "유형별 계좌 조회")
    @GetMapping("/type/{accountType}")
    public ResponseEntity<ApiResponse<List<Account>>> getAccountsByType(@PathVariable Account.AccountType accountType) {
        List<Account> accounts = accountService.getAccountsByType(accountType);
        return ResponseEntity.ok(ApiResponse.success(accounts, "유형별 계좌 목록을 성공적으로 조회했습니다."));
    }
    
    @Operation(summary = "계좌 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<Account>> createAccount(@Valid @RequestBody Account account) {
        Account createdAccount = accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(createdAccount, "계좌가 성공적으로 생성되었습니다."));
    }
    
    @Operation(summary = "계좌 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Account>> updateAccount(@PathVariable String id, @Valid @RequestBody Account account) {
        account.setId(id);
        Account updatedAccount = accountService.updateAccount(account);
        return ResponseEntity.ok(ApiResponse.success(updatedAccount, "계좌가 성공적으로 수정되었습니다."));
    }
    
    @Operation(summary = "계좌 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable String id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.success(null, "계좌가 성공적으로 삭제되었습니다."));
    }
}
