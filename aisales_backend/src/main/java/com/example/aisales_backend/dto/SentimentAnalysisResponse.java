package com.example.aisales_backend.dto;

import com.example.aisales_backend.entity.Call;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SentimentAnalysisResponse {

    private Long callId;
    private String transcript;
    private Double sentimentPercentage;
    private String sentimentLabel;
    private String status;
    private String summary;
}
