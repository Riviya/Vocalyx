package com.example.aisales_backend.dto;

import com.example.aisales_backend.entity.Contact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactResponse {

    private Long id;
    private Contact.Salutation salutation;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String email;
    private String phoneNumber;
    private Contact.Department department;
    private Contact.ContactStatus status;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
