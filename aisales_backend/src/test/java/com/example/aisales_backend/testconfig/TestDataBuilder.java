package com.example.aisales_backend.testconfig;

import com.example.aisales_backend.dto.*;
import com.example.aisales_backend.entity.Call;
import com.example.aisales_backend.entity.Contact;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestDataBuilder {

    public static RegisterRequest validRegistrationRequest() {
        return RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("password123")
                .build();
    }

    public static LoginRequest validLoginRequest() {
        return LoginRequest.builder()
                .email("john.doe@test.com")
                .password("password123")
                .build();
    }

    public static ContactRequest validContactRequest() {
        return ContactRequest.builder()
                .salutation(Contact.Salutation.MR)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@company.com")
                .phoneNumber("+1234567890")
                .department(Contact.Department.SALES)
                .status(Contact.ContactStatus.ACTIVE)
                .companyName("Tech Corp")
                .build();
    }

    public static GoalRequest validGoalRequest() {
        return new GoalRequest(
                "Q1 Sales Target",
                "Achieve 100K in sales",
                100000.0,
                0.0,
                LocalDate.now(),
                LocalDate.now().plusMonths(3),
                "Acme Corp",
                "High"
        );
    }
}