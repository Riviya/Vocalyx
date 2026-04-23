package com.example.aisales_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "calls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------------- Core Call Details ----------------
    @NotBlank(message = "Call title is required")
    @Size(min = 2, max = 200, message = "Call title must be between 2 and 200 characters")
    @Column(name = "call_title", nullable = false)
    private String callTitle;

    @Column(name = "call_date_time", nullable = false)
    private LocalDateTime callDateTime;

    @NotBlank(message = "Recording file path is required")
    @Column(name = "recording_file_path", nullable = false)
    private String recordingFilePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "call_direction", nullable = false)
    private CallDirection callDirection;

    // ---------------- Webhook Analysis Data ----------------
    @Size(max = 1000, message = "Summary must not exceed 1000 characters")
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;   // Webhook-provided summary

    @Column(name = "transcript", columnDefinition = "TEXT")
    private String transcript;  // Full call transcript

    @Column(name = "sentiment_percentage")
    private Integer sentimentPercentage;  // Percentage value e.g. 98

    @Column(name = "sentiment_label", columnDefinition = "TEXT")
    private String sentimentLabel;

    // Store raw webhook response for debugging / audit
    @Column(name = "webhook_response", columnDefinition = "LONGTEXT")
    private String webhookResponse;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, PROCESSING, COMPLETED

    // ---------------- File Info ----------------
    @Column(name = "file_size")
    private Long fileSize;

    @Size(max = 50, message = "File type must not exceed 50 characters")
    @Column(name = "file_type")
    private String fileType;

    // ---------------- Timestamps ----------------
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---------------- Company Information ----------------
    @Column(name = "company_name")
    private String companyName;  // Company name from contact or user input

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //  Link to Order (because one client can have multiple orders)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // ---------------- Lifecycle Hooks ----------------
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ---------------- Enums ----------------
    public enum CallDirection {
        OUTGOING, INCOMING
    }
}
