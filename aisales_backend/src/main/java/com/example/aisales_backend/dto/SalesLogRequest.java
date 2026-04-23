package com.example.aisales_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesLogRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String companyDetails;

    @NotNull(message = "Sale amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Sale amount must be greater than 0")
    private BigDecimal saleAmount;

    @NotNull(message = "Closed date is required")
    private LocalDate closedDate;

    private Long callId;

    private Long orderId;

    private Long contactId;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String notes;
}
