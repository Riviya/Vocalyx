package com.example.aisales_backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aisales_backend.dto.SalesLogResponse;
import com.example.aisales_backend.entity.Goal;
import com.example.aisales_backend.entity.SalesLog;
import com.example.aisales_backend.entity.User;
import com.example.aisales_backend.repository.GoalRepository;
import com.example.aisales_backend.repository.SalesLogRepository;
import com.example.aisales_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalSalesIntegrationService {

    private final GoalRepository goalRepository;
    private final SalesLogRepository salesLogRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void updateGoalProgressFromSales() {
        User user = getCurrentUser();
        List<Goal> activeGoals = goalRepository.findActiveGoalsByUser(user);
        
        for (Goal goal : activeGoals) {
            // Get sales for this goal's date range
            List<SalesLog> salesInPeriod = salesLogRepository.findByUserIdAndClosedDateBetweenOrderByClosedDateDesc(
                user.getId(), 
                goal.getStartDate(), 
                goal.getEndDate()
            );
            
            // Calculate total sales amount for this period
            BigDecimal totalSalesAmount = salesInPeriod.stream()
                .map(SalesLog::getSaleAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Update goal progress
            goal.setCurrentProgress(totalSalesAmount.doubleValue());
            
            // Update status based on progress
            if (totalSalesAmount.doubleValue() >= goal.getTargetRevenue()) {
                goal.setStatus("Completed");
            } else if (totalSalesAmount.doubleValue() > 0) {
                goal.setStatus("In Progress");
            } else {
                goal.setStatus("Not Started");
            }
            
            goalRepository.save(goal);
            log.info("Updated goal {} progress to {} from sales", goal.getName(), totalSalesAmount);
        }
    }

    @Transactional(readOnly = true)
    public List<SalesLogResponse> getSalesForGoal(Long goalId) {
        User user = getCurrentUser();
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new RuntimeException("Goal not found"));
        
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        List<SalesLog> sales = salesLogRepository.findByUserIdAndClosedDateBetweenOrderByClosedDateDesc(
            user.getId(), 
            goal.getStartDate(), 
            goal.getEndDate()
        );
        
        return sales.stream()
            .map(this::mapToSalesLogResponse)
            .collect(Collectors.toList());
    }

    public BigDecimal getTotalSalesAmountForGoal(Long goalId) {
        User user = getCurrentUser();
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new RuntimeException("Goal not found"));
        
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        List<SalesLog> sales = salesLogRepository.findByUserIdAndClosedDateBetweenOrderByClosedDateDesc(
            user.getId(), 
            goal.getStartDate(), 
            goal.getEndDate()
        );
        
        return sales.stream()
            .map(SalesLog::getSaleAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<SalesLogResponse> getRecentSalesForUser() {
        User user = getCurrentUser();
        List<SalesLog> sales = salesLogRepository.findByUserIdWithEagerLoading(user.getId());
        
        return sales.stream()
            .map(this::mapToSalesLogResponse)
            .collect(Collectors.toList());
    }

    public BigDecimal getTotalRevenueForUser() {
        User user = getCurrentUser();
        return salesLogRepository.getTotalRevenueByUserId(user.getId());
    }

    public Long getTotalSalesCountForUser() {
        User user = getCurrentUser();
        return salesLogRepository.getSalesCountByUserId(user.getId());
    }

    private SalesLogResponse mapToSalesLogResponse(SalesLog salesLog) {
        return SalesLogResponse.builder()
                .id(salesLog.getId())
                .customerName(salesLog.getCustomerName())
                .companyDetails(salesLog.getCompanyDetails())
                .saleAmount(salesLog.getSaleAmount())
                .closedDate(salesLog.getClosedDate())
                .callId(salesLog.getCall() != null ? salesLog.getCall().getId() : null)
                .callTitle(salesLog.getCall() != null ? salesLog.getCall().getCallTitle() : null)
                .orderId(salesLog.getOrder() != null ? salesLog.getOrder().getId() : null)
                .orderNumber(salesLog.getOrder() != null ? salesLog.getOrder().getOrderNumber() : null)
                .contactId(salesLog.getContact() != null ? salesLog.getContact().getId() : null)
                .contactName(salesLog.getContact() != null ? 
                    salesLog.getContact().getFirstName() + " " + salesLog.getContact().getLastName() : null)
                .userId(salesLog.getUser().getId())
                .userName(salesLog.getUser().getFirstName() + " " + salesLog.getUser().getLastName())
                .notes(salesLog.getNotes())
                .createdAt(salesLog.getCreatedAt())
                .updatedAt(salesLog.getUpdatedAt())
                .build();
    }
}
