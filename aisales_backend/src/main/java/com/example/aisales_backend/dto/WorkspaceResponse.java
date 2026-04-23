package com.example.aisales_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceResponse {
    private Long id;
    private String companyName;
    private String industry;
    private String address;
    private Long createdBy;
}


