package com.example.aisales_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesLogResponse {

    private Long id;
    private String customerName;
    private String companyDetails;
    private BigDecimal saleAmount;
    private LocalDate closedDate;
    private Long callId;
    private String callTitle;
    private Long orderId;
    private String orderNumber;
    private Long contactId;
    private String contactName;
    private Long userId;
    private String userName;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
