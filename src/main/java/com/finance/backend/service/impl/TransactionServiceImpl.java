package com.finance.backend.service.impl;

import com.finance.backend.dto.request.TransactionRequest;
import com.finance.backend.dto.response.TransactionResponse;
import com.finance.backend.entity.Transaction;
import com.finance.backend.entity.User;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.exception.ResourceNotFoundException;
import com.finance.backend.repository.TransactionRepository;
import com.finance.backend.service.TransactionService;
import com.finance.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User currentUser = userService.getCurrentUser();

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .deleted(false)
                .build();

        return toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        return toResponse(findActiveById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(TransactionType type, String category,
                                                         LocalDate startDate, LocalDate endDate,
                                                         Pageable pageable) {
        return transactionRepository
                .findAllWithFilters(type, category, startDate, endDate, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction transaction = findActiveById(id);

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory().trim());
        transaction.setDate(request.getDate());
        transaction.setNotes(request.getNotes());

        return toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = findActiveById(id);
        transaction.setDeleted(true);  // Soft delete
        transactionRepository.save(transaction);
    }

    // --- Helpers ---

    private Transaction findActiveById(Long id) {
        return transactionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    }

    public TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .category(t.getCategory())
                .date(t.getDate())
                .notes(t.getNotes())
                .createdByEmail(t.getCreatedBy().getEmail())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
