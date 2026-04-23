package com.example.aisales_backend.controller;

import com.example.aisales_backend.dto.ContactRequest;
import com.example.aisales_backend.dto.ContactResponse;
import com.example.aisales_backend.service.ContactService;
import com.example.aisales_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@Slf4j
public class ContactController {

    private final ContactService contactService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(Authentication authentication, @Valid @RequestBody ContactRequest request) {
        String userEmail = authentication.getName();
        var user = userService.getUserRepository().findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getCompany() == null) {
            throw new RuntimeException("User must have a company to create contacts");
        }
        
        log.info("Creating contact: {} {} for company: {}", request.getFirstName(), request.getLastName(), user.getCompany().getId());
        ContactResponse response = contactService.createContact(request, user.getCompany().getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ContactResponse>> getAllContacts(Authentication authentication) {
        String userEmail = authentication.getName();
        var user = userService.getUserRepository().findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getCompany() == null) {
            throw new RuntimeException("User must have a company to view contacts");
        }
        
        log.info("Fetching all contacts for company: {}", user.getCompany().getId());
        List<ContactResponse> responses = contactService.getAllContacts(user.getCompany().getId());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getContactById(Authentication authentication, @PathVariable Long id) {
        String userEmail = authentication.getName();
        var user = userService.getUserRepository().findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getCompany() == null) {
            throw new RuntimeException("User must have a company to view contacts");
        }
        
        log.info("Fetching contact by ID: {} for company: {}", id, user.getCompany().getId());
        ContactResponse response = contactService.getContactById(id, user.getCompany().getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(Authentication authentication, @PathVariable Long id, @Valid @RequestBody ContactRequest request) {
        String userEmail = authentication.getName();
        var user = userService.getUserRepository().findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getCompany() == null) {
            throw new RuntimeException("User must have a company to update contacts");
        }
        
        log.info("Updating contact with ID: {} for company: {}", id, user.getCompany().getId());
        ContactResponse response = contactService.updateContact(id, request, user.getCompany().getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(Authentication authentication, @PathVariable Long id) {
        String userEmail = authentication.getName();
        var user = userService.getUserRepository().findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getCompany() == null) {
            throw new RuntimeException("User must have a company to delete contacts");
        }
        
        log.info("Deleting contact with ID: {} for company: {}", id, user.getCompany().getId());
        contactService.deleteContact(id, user.getCompany().getId());
        return ResponseEntity.noContent().build();
    }
}
