package com.finance.backend.controller;

import com.finance.backend.dto.response.ApiResponse;
import com.finance.backend.dto.response.DashboardSummaryResponse;
import com.finance.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Returns full dashboard summary:
     * Accessible by all authenticated users (VIEWER, ANALYST, ADMIN).
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().year}") int year) {
        DashboardSummaryResponse summary = dashboardService.getSummary(year);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
