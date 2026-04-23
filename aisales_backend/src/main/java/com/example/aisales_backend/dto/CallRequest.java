package com.example.aisales_backend.dto;

import com.example.aisales_backend.entity.Call;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CallRequest {

    @NotBlank(message = "Call title is required")
    @Size(min = 2, max = 200)
    private String callTitle;

    @NotNull(message = "Call date and time is required")
    private LocalDateTime callDateTime;

    @NotBlank(message = "Recording file path is required")
    private String recordingFilePath;

    @NotNull(message = "Call direction is required")
    private Call.CallDirection callDirection;

    private String summary;
    
    private String companyName;  
    
    @NotNull(message = "Contact ID is required")
    private Long contactId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private Long orderId;

    private Long fileSize;
    private String fileType;
}
