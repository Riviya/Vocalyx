package com.example.aisales_backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse {

    private String transcript;
    private String summary;

    private Integer sentimentPercentage;

    private String sentimentLabel;
}
