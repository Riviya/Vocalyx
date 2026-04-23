package com.example.aisales_backend.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.aisales_backend.dto.SalesLogRequest;
import com.example.aisales_backend.dto.SalesLogResponse;
import com.example.aisales_backend.service.SalesLogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/sales-log")
@RequiredArgsConstructor
@Slf4j
public class SalesLogController {

    private final SalesLogService salesLogService;

    @PostMapping
    public ResponseEntity<SalesLogResponse> createSalesLog(@Valid @RequestBody SalesLogRequest request) {
        log.info("Creating sales log for customer: {}", request.getCustomerName());
        SalesLogResponse response = salesLogService.createSalesLog(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesLogResponse> getSalesLogById(@PathVariable Long id) {
        SalesLogResponse response = salesLogService.getSalesLogById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SalesLogResponse>> getSalesLogsByUserId(@PathVariable Long userId) {
        List<SalesLogResponse> responses = salesLogService.getSalesLogsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/contact/{contactId}")
    public ResponseEntity<List<SalesLogResponse>> getSalesLogsByContactId(@PathVariable Long contactId) {
        List<SalesLogResponse> responses = salesLogService.getSalesLogsByContactId(contactId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<SalesLogResponse>> getSalesLogsByOrderId(@PathVariable Long orderId) {
        List<SalesLogResponse> responses = salesLogService.getSalesLogsByOrderId(orderId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/call/{callId}")
    public ResponseEntity<SalesLogResponse> getSalesLogByCallId(@PathVariable Long callId) {
        SalesLogResponse response = salesLogService.getSalesLogByCallId(callId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<SalesLogResponse>> getSalesLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<SalesLogResponse> responses = salesLogService.getSalesLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<SalesLogResponse>> getSalesLogsByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<SalesLogResponse> responses = salesLogService.getSalesLogsByUserIdAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueByUserId(@PathVariable Long userId) {
        BigDecimal totalRevenue = salesLogService.getTotalRevenueByUserId(userId);
        return ResponseEntity.ok(totalRevenue);
    }

    @GetMapping("/user/{userId}/revenue/date-range")
    public ResponseEntity<BigDecimal> getTotalRevenueByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal totalRevenue = salesLogService.getTotalRevenueByUserIdAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(totalRevenue);
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getSalesCountByUserId(@PathVariable Long userId) {
        Long salesCount = salesLogService.getSalesCountByUserId(userId);
        return ResponseEntity.ok(salesCount);
    }

    @GetMapping("/user/{userId}/count/date-range")
    public ResponseEntity<Long> getSalesCountByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long salesCount = salesLogService.getSalesCountByUserIdAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(salesCount);
    }
}
