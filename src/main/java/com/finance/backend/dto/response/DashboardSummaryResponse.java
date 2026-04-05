package com.finance.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private List<CategoryTotal> categoryTotals;
    private List<TransactionResponse> recentTransactions;
    private Map<String, MonthlyTrend> monthlyTrends;

    @Data
    @Builder
    public static class CategoryTotal {
        private String category;
        private String type;
        private BigDecimal total;
    }

    @Data
    @Builder
    public static class MonthlyTrend {
        private String month;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;
    }
}
