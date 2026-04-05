package com.finance.backend.service;

import com.finance.backend.dto.request.TransactionRequest;
import com.finance.backend.dto.response.TransactionResponse;
import com.finance.backend.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TransactionService {
    TransactionResponse createTransaction(TransactionRequest request);
    TransactionResponse getTransactionById(Long id);
    Page<TransactionResponse> getAllTransactions(TransactionType type, String category,
                                                  LocalDate startDate, LocalDate endDate,
                                                  Pageable pageable);
    TransactionResponse updateTransaction(Long id, TransactionRequest request);
    void deleteTransaction(Long id);
}
