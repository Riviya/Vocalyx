package com.example.aisales_backend.dto;

import com.example.aisales_backend.entity.Call;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CallResponse {

    private Long id;
    private String callTitle;
    private LocalDateTime callDateTime;
    private String recordingFilePath;
    private Call.CallDirection callDirection;

    private String summary;
    private String transcript;
    private String sentimentLabel;
    private Integer sentimentPercentage;


    private Long fileSize;
    private String fileType;

    private String companyName;
    private Long contactId;
    private String contactName;
    private Long userId;
    private String userName;

    private Long orderId;
    private String orderNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
