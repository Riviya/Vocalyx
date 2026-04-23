package com.example.aisales_backend.dto.history;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallDto {
    private Long id;
    private String callTitle;
    private LocalDateTime callDateTime;
    private String callDirection;
    private String summary;
    private Integer sentimentScore;
    private String sentimentType;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Long orderId;
}