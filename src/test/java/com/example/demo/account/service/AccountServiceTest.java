package com.example.demo.account.service;

import com.example.demo.account.entity.Account;
import com.example.demo.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id("TEST_ACCOUNT_001")
                .accountType(Account.AccountType.BASIC)
                .balance(new BigDecimal("1000.00"))
                .status(Account.AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateAccount() {
        // Given
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        Account createdAccount = accountService.createAccount(testAccount);

        // Then
        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getId()).isEqualTo("TEST_ACCOUNT_001");
        assertThat(createdAccount.getAccountType()).isEqualTo(Account.AccountType.BASIC);
        assertThat(createdAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void testGetAccountById() {
        // Given
        when(accountRepository.findById("TEST_ACCOUNT_001")).thenReturn(Optional.of(testAccount));

        // When
        Optional<Account> foundAccount = accountService.getAccountById("TEST_ACCOUNT_001");

        // Then
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getId()).isEqualTo("TEST_ACCOUNT_001");
    }

    @Test
    void testGetAllAccounts() {
        // Given
        when(accountRepository.findAll()).thenReturn(List.of(testAccount));

        // When
        List<Account> accounts = accountService.getAllAccounts();

        // Then
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getId()).isEqualTo("TEST_ACCOUNT_001");
    }

    @Test
    void testUpdateAccount() {
        // Given
        testAccount.setBalance(new BigDecimal("1500.00"));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        Account updatedAccount = accountService.updateAccount(testAccount);

        // Then
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    void testDeleteAccount() {
        // Given
        // When
        accountService.deleteAccount("TEST_ACCOUNT_001");

        // Then - void 메서드이므로 예외가 발생하지 않으면 성공
        // 추가 검증이 필요하다면 verify를 사용
    }
}
