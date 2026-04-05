package com.finance.backend.service.impl;

import com.finance.backend.dto.response.DashboardSummaryResponse;
import com.finance.backend.dto.response.DashboardSummaryResponse.CategoryTotal;
import com.finance.backend.dto.response.DashboardSummaryResponse.MonthlyTrend;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.repository.TransactionRepository;
import com.finance.backend.service.DashboardService;
import com.finance.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;
    private final TransactionServiceImpl transactionService;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(int year) {
        BigDecimal totalIncome   = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        List<CategoryTotal> categoryTotals = buildCategoryTotals();
        Map<String, MonthlyTrend> monthlyTrends = buildMonthlyTrends(year);

        var recentTransactions = transactionRepository
                .findRecentTransactions(PageRequest.of(0, 10))
                .stream()
                .map(transactionService::toResponse)
                .collect(Collectors.toList());

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .categoryTotals(categoryTotals)
                .recentTransactions(recentTransactions)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    private List<CategoryTotal> buildCategoryTotals() {
        return transactionRepository.getCategoryWiseTotals().stream()
                .map(row -> CategoryTotal.builder()
                        .category((String) row[0])
                        .type(row[1].toString())
                        .total((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }

    private Map<String, MonthlyTrend> buildMonthlyTrends(int year) {
        // Build a map: monthName -> MonthlyTrend
        Map<Integer, BigDecimal> incomeByMonth  = new HashMap<>();
        Map<Integer, BigDecimal> expenseByMonth = new HashMap<>();

        transactionRepository.getMonthlyTrends(year).forEach(row -> {
            int monthNum = ((Number) row[0]).intValue();
            TransactionType type = TransactionType.valueOf(row[1].toString());
            BigDecimal total = (BigDecimal) row[2];

            if (type == TransactionType.INCOME) {
                incomeByMonth.put(monthNum, total);
            } else {
                expenseByMonth.put(monthNum, total);
            }
        });

        Map<String, MonthlyTrend> result = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            BigDecimal income   = incomeByMonth.getOrDefault(m, BigDecimal.ZERO);
            BigDecimal expenses = expenseByMonth.getOrDefault(m, BigDecimal.ZERO);
            String monthName    = Month.of(m).name();

            result.put(monthName, MonthlyTrend.builder()
                    .month(monthName)
                    .income(income)
                    .expenses(expenses)
                    .net(income.subtract(expenses))
                    .build());
        }
        return result;
    }
}
