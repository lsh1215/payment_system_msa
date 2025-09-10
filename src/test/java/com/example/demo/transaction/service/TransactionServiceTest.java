package com.example.demo.transaction.service;

import com.example.demo.transaction.entity.Transaction;
import com.example.demo.transaction.repository.TransactionRepository;
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
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = Transaction.builder()
                .id(1L)
                .accountId("TEST_ACCOUNT_001")
                .amount(new BigDecimal("100.00"))
                .type(Transaction.TransactionType.DEPOSIT)
                .isProcessed(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateTransaction() {
        // Given
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        Transaction createdTransaction = transactionService.createTransaction(testTransaction);

        // Then
        assertThat(createdTransaction).isNotNull();
        assertThat(createdTransaction.getAccountId()).isEqualTo("TEST_ACCOUNT_001");
        assertThat(createdTransaction.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(createdTransaction.getType()).isEqualTo(Transaction.TransactionType.DEPOSIT);
    }

    @Test
    void testGetTransactionById() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // When
        Optional<Transaction> foundTransaction = transactionService.getTransactionById(1L);

        // Then
        assertThat(foundTransaction).isPresent();
        assertThat(foundTransaction.get().getId()).isEqualTo(1L);
    }

    @Test
    void testGetTransactionsByAccount() {
        // Given
        when(transactionRepository.findByAccountIdAndIsProcessed("TEST_ACCOUNT_001", false)).thenReturn(List.of(testTransaction));

        // When
        List<Transaction> transactions = transactionService.getTransactionsByAccount("TEST_ACCOUNT_001");

        // Then
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getAccountId()).isEqualTo("TEST_ACCOUNT_001");
    }

    @Test
    void testGetUnprocessedTransactions() {
        // Given
        when(transactionRepository.findByIsProcessed(false)).thenReturn(List.of(testTransaction));

        // When
        List<Transaction> unprocessedTransactions = transactionService.getUnprocessedTransactions();

        // Then
        assertThat(unprocessedTransactions).hasSize(1);
        assertThat(unprocessedTransactions.get(0).getIsProcessed()).isFalse();
    }
}
