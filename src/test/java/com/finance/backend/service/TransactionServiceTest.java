package com.finance.backend.service;

import com.finance.backend.dto.request.TransactionRequest;
import com.finance.backend.dto.response.TransactionResponse;
import com.finance.backend.entity.Transaction;
import com.finance.backend.entity.User;
import com.finance.backend.enums.Role;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.exception.ResourceNotFoundException;
import com.finance.backend.repository.TransactionRepository;
import com.finance.backend.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User adminUser;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L).email("admin@finance.com").fullName("Admin")
                .role(Role.ADMIN).active(true).build();

        sampleTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.of(2024, 3, 15))
                .notes("March salary")
                .createdBy(adminUser)
                .deleted(false)
                .build();
    }

    // --- createTransaction ---

    @Test
    @DisplayName("createTransaction: persists and returns new transaction")
    void createTransaction_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("500.00"));
        request.setType(TransactionType.INCOME);
        request.setCategory("Salary");
        request.setDate(LocalDate.of(2024, 3, 15));
        request.setNotes("March salary");

        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        TransactionResponse response = transactionService.createTransaction(request);

        assertThat(response.getAmount()).isEqualByComparingTo("500.00");
        assertThat(response.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(response.getCategory()).isEqualTo("Salary");
        verify(transactionRepository).save(any(Transaction.class));
    }

    // --- getTransactionById ---

    @Test
    @DisplayName("getTransactionById: returns transaction when found and not deleted")
    void getTransactionById_Found() {
        when(transactionRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(sampleTransaction));

        TransactionResponse response = transactionService.getTransactionById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCreatedByEmail()).isEqualTo("admin@finance.com");
    }

    @Test
    @DisplayName("getTransactionById: throws ResourceNotFoundException for missing/deleted transaction")
    void getTransactionById_NotFound() {
        when(transactionRepository.findByIdAndDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getAllTransactions ---

    @Test
    @DisplayName("getAllTransactions: returns paged results")
    void getAllTransactions_WithFilters() {
        Page<Transaction> page = new PageImpl<>(List.of(sampleTransaction));
        when(transactionRepository.findAllWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(page);

        Page<TransactionResponse> result = transactionService.getAllTransactions(
                TransactionType.INCOME, "Salary",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("Salary");
    }

    // --- updateTransaction ---

    @Test
    @DisplayName("updateTransaction: updates fields correctly")
    void updateTransaction_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("750.00"));
        request.setType(TransactionType.INCOME);
        request.setCategory("Bonus");
        request.setDate(LocalDate.of(2024, 4, 1));
        request.setNotes("Q1 bonus");

        when(transactionRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.updateTransaction(1L, request);

        assertThat(response.getAmount()).isEqualByComparingTo("750.00");
        assertThat(response.getCategory()).isEqualTo("Bonus");
    }

    // --- deleteTransaction (soft delete) ---

    @Test
    @DisplayName("deleteTransaction: marks transaction as deleted (soft delete)")
    void deleteTransaction_SoftDelete() {
        when(transactionRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        transactionService.deleteTransaction(1L);

        assertThat(sampleTransaction.isDeleted()).isTrue();
        verify(transactionRepository).save(sampleTransaction);
    }

    @Test
    @DisplayName("deleteTransaction: throws when transaction not found")
    void deleteTransaction_NotFound() {
        when(transactionRepository.findByIdAndDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
