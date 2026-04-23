package com.example.aisales_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyResponse {

    private Long id;
    private String companyName;
    private String industry;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
