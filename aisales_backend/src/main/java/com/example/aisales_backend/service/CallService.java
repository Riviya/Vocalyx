package com.example.aisales_backend.service;

import com.example.aisales_backend.dto.*;
import com.example.aisales_backend.entity.*;
import com.example.aisales_backend.exception.ResourceNotFoundException;
import com.example.aisales_backend.repository.CallRepository;
import com.example.aisales_backend.repository.ContactRepository;
import com.example.aisales_backend.repository.UserRepository;
import com.example.aisales_backend.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CallService {

    private final CallRepository callRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public CallResponse saveCall(CallRequest request) {
        // Fetch entities by IDs
        Contact contact = contactRepository.findById(request.getContactId())
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id " + request.getContactId()));
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + request.getUserId()));
        
        // Handle order tracking - find or create order for this contact
        Order order = null;
        if (request.getOrderId() != null) {
            // Use provided order ID
            order = orderRepository.findById(request.getOrderId())
                    .orElse(null);
        } else {
            // Find the most recent order for this contact, or create a new one
            order = orderRepository.findMostRecentOrderByContactId(contact.getId())
                    .orElse(null);
            
            // If no recent order exists, create a new one
            if (order == null) {
                order = Order.builder()
                        .orderNumber("ORD-" + System.currentTimeMillis()) // Generate unique order number
                        .orderDate(request.getCallDateTime())
                        .contact(contact)
                        .orderValue(0.0) // Default value, can be updated later
                        .build();
                order = orderRepository.save(order);
            }
        }

        // Use company name from request or fallback to contact's company name
        String companyName = request.getCompanyName() != null ? request.getCompanyName() : contact.getCompanyName();

        Call call = Call.builder()
                .callTitle(request.getCallTitle())
                .callDateTime(request.getCallDateTime())
                .recordingFilePath(request.getRecordingFilePath())
                .callDirection(request.getCallDirection())
                .fileSize(request.getFileSize())
                .fileType(request.getFileType())
                .companyName(companyName)
                .contact(contact)
                .user(user)
                .order(order)
                .build();

        Call saved = callRepository.save(call);
        return mapToResponse(saved);
    }

    public CallResponse getCallById(Long callId) {
        Call call = callRepository.findByIdWithRelationships(callId);
        if (call == null) {
            throw new ResourceNotFoundException("Call not found with id " + callId);
        }
        return mapToResponse(call);
    }

    public CallResponse updateWithWebhook(Long callId, WebhookResponse webhook) {
        log.info("Updating call {} with webhook data: {}", callId, webhook);
        
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found with id " + callId));

        log.info("Found call before update: transcript={}, summary={}, sentimentPercentage={}, sentimentLabel={}", 
                call.getTranscript(), call.getSummary(), call.getSentimentPercentage(), call.getSentimentLabel());

        // Update call with webhook response data
        call.setTranscript(webhook.getTranscript());
        call.setSummary(webhook.getSummary());
        call.setSentimentPercentage(webhook.getSentimentPercentage()); // Store percentage
        call.setSentimentLabel(webhook.getSentimentLabel());

        // Store raw webhook response for debugging/audit
        call.setWebhookResponse(webhook.toString());

        call.setStatus("COMPLETED");

        log.info("Call after update: transcript={}, summary={}, sentimentPercentage={}, sentimentLabel={}", 
                call.getTranscript(), call.getSummary(), call.getSentimentPercentage(), call.getSentimentLabel());

        Call updated = callRepository.save(call);
        log.info("Call saved to database successfully");
        
        return mapToResponse(updated);
    }

    public List<CallResponse> getCallsByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number " + orderNumber));
        
        return callRepository.findByOrderId(order.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<CallResponse> getCallsByContact(Long contactId) {
        return callRepository.findByContactId(contactId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CallResponse mapToResponse(Call call) {
        try {
            return CallResponse.builder()
                    .id(call.getId())
                    .callTitle(call.getCallTitle())
                    .callDateTime(call.getCallDateTime())
                    .recordingFilePath(call.getRecordingFilePath())
                    .callDirection(call.getCallDirection())
                    .summary(call.getSummary())
                    .transcript(call.getTranscript())
                    .sentimentPercentage(call.getSentimentPercentage())
                    .sentimentLabel(call.getSentimentLabel())
                    .fileSize(call.getFileSize())
                    .fileType(call.getFileType())
                    .companyName(call.getCompanyName())
                    .contactId(call.getContact() != null ? call.getContact().getId() : null)
                    .contactName(call.getContact() != null ? 
                        (call.getContact().getFirstName() + " " + call.getContact().getLastName()) : "Unknown Contact")
                    .userId(call.getUser() != null ? call.getUser().getId() : null)
                    .userName(call.getUser() != null ? call.getUser().getUsername() : "Unknown User")
                    .orderId(call.getOrder() != null ? call.getOrder().getId() : null)
                    .orderNumber(call.getOrder() != null ? call.getOrder().getOrderNumber() : null)
                    .createdAt(call.getCreatedAt())
                    .updatedAt(call.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error mapping call to response: ", e);
            // Return a minimal response if mapping fails
            return CallResponse.builder()
                    .id(call.getId())
                    .callTitle(call.getCallTitle())
                    .callDateTime(call.getCallDateTime())
                    .recordingFilePath(call.getRecordingFilePath())
                    .callDirection(call.getCallDirection())
                    .summary(call.getSummary())
                    .transcript(call.getTranscript())
                    .sentimentPercentage(call.getSentimentPercentage())
                    .sentimentLabel(call.getSentimentLabel())
                    .fileSize(call.getFileSize())
                    .fileType(call.getFileType())
                    .companyName(call.getCompanyName())
                    .contactId(null)
                    .contactName("Error loading contact")
                    .userId(null)
                    .userName("Error loading user")
                    .orderId(null)
                    .orderNumber(null)
                    .createdAt(call.getCreatedAt())
                    .updatedAt(call.getUpdatedAt())
                    .build();
        }
    }
}
