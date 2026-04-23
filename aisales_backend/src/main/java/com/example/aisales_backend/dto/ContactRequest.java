package com.example.aisales_backend.dto;

import com.example.aisales_backend.entity.Contact;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactRequest {

    @NotNull(message = "Salutation is required")
    private Contact.Salutation salutation;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;

    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @NotNull(message = "Department is required")
    private Contact.Department department;

    @Builder.Default
    private Contact.ContactStatus status = Contact.ContactStatus.ACTIVE;

    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String companyName;
}
