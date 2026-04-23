package com.example.aisales_backend.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aisales_backend.dto.SalesLogResponse;
import com.example.aisales_backend.service.GoalSalesIntegrationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/goal-sales")
@RequiredArgsConstructor
@Slf4j
public class GoalSalesController {

    private final GoalSalesIntegrationService goalSalesIntegrationService;

    @PostMapping("/update-progress")
    public ResponseEntity<String> updateGoalProgressFromSales() {
        try {
            goalSalesIntegrationService.updateGoalProgressFromSales();
            return ResponseEntity.ok("Goal progress updated successfully from sales data");
        } catch (Exception e) {
            log.error("Failed to update goal progress from sales", e);
            return ResponseEntity.badRequest().body("Failed to update goal progress: " + e.getMessage());
        }
    }

    @GetMapping("/goal/{goalId}/sales")
    public ResponseEntity<List<SalesLogResponse>> getSalesForGoal(@PathVariable Long goalId) {
        try {
            List<SalesLogResponse> sales = goalSalesIntegrationService.getSalesForGoal(goalId);
            return ResponseEntity.ok(sales);
        } catch (Exception e) {
            log.error("Failed to get sales for goal {}", goalId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/goal/{goalId}/total-amount")
    public ResponseEntity<BigDecimal> getTotalSalesAmountForGoal(@PathVariable Long goalId) {
        try {
            BigDecimal totalAmount = goalSalesIntegrationService.getTotalSalesAmountForGoal(goalId);
            return ResponseEntity.ok(totalAmount);
        } catch (Exception e) {
            log.error("Failed to get total sales amount for goal {}", goalId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/recent-sales")
    public ResponseEntity<List<SalesLogResponse>> getRecentSalesForUser() {
        try {
            List<SalesLogResponse> sales = goalSalesIntegrationService.getRecentSalesForUser();
            return ResponseEntity.ok(sales);
        } catch (Exception e) {
            log.error("Failed to get recent sales for user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/total-revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueForUser() {
        try {
            BigDecimal totalRevenue = goalSalesIntegrationService.getTotalRevenueForUser();
            return ResponseEntity.ok(totalRevenue);
        } catch (Exception e) {
            log.error("Failed to get total revenue for user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/total-sales-count")
    public ResponseEntity<Long> getTotalSalesCountForUser() {
        try {
            Long salesCount = goalSalesIntegrationService.getTotalSalesCountForUser();
            return ResponseEntity.ok(salesCount);
        } catch (Exception e) {
            log.error("Failed to get total sales count for user", e);
            return ResponseEntity.badRequest().build();
        }
    }
}

