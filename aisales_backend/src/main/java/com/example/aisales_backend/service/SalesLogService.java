package com.example.aisales_backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aisales_backend.dto.SalesLogRequest;
import com.example.aisales_backend.dto.SalesLogResponse;
import com.example.aisales_backend.entity.Call;
import com.example.aisales_backend.entity.Contact;
import com.example.aisales_backend.entity.Order;
import com.example.aisales_backend.entity.SalesLog;
import com.example.aisales_backend.entity.User;
import com.example.aisales_backend.repository.CallRepository;
import com.example.aisales_backend.repository.ContactRepository;
import com.example.aisales_backend.repository.OrderRepository;
import com.example.aisales_backend.repository.SalesLogRepository;
import com.example.aisales_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesLogService {

    private final SalesLogRepository salesLogRepository;
    private final CallRepository callRepository;
    private final OrderRepository orderRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    @Transactional
    public SalesLogResponse createSalesLog(SalesLogRequest request) {
        log.info("Creating sales log for customer: {}, amount: {}", request.getCustomerName(), request.getSaleAmount());

        // Validate and fetch related entities
        Call call = null;
        if (request.getCallId() != null) {
            call = callRepository.findById(request.getCallId())
                    .orElseThrow(() -> new RuntimeException("Call not found with ID: " + request.getCallId()));
        }

        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + request.getOrderId()));
        }

        Contact contact = null;
        if (request.getContactId() != null) {
            contact = contactRepository.findById(request.getContactId())
                    .orElseThrow(() -> new RuntimeException("Contact not found with ID: " + request.getContactId()));
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        // Create sales log entity
        SalesLog salesLog = SalesLog.builder()
                .customerName(request.getCustomerName())
                .companyDetails(request.getCompanyDetails())
                .saleAmount(request.getSaleAmount())
                .closedDate(request.getClosedDate())
                .call(call)
                .order(order)
                .contact(contact)
                .user(user)
                .notes(request.getNotes())
                .build();

        // Save to database
        SalesLog savedSalesLog = salesLogRepository.save(salesLog);
        log.info("Sales log created successfully with ID: {}", savedSalesLog.getId());

        return mapToResponse(savedSalesLog);
    }

    public SalesLogResponse getSalesLogById(Long id) {
        SalesLog salesLog = salesLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales log not found with ID: " + id));
        return mapToResponse(salesLog);
    }

    public List<SalesLogResponse> getSalesLogsByUserId(Long userId) {
        List<SalesLog> salesLogs = salesLogRepository.findByUserIdOrderByClosedDateDesc(userId);
        return salesLogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SalesLogResponse> getSalesLogsByContactId(Long contactId) {
        List<SalesLog> salesLogs = salesLogRepository.findByContactIdOrderByClosedDateDesc(contactId);
        return salesLogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SalesLogResponse> getSalesLogsByOrderId(Long orderId) {
        List<SalesLog> salesLogs = salesLogRepository.findByOrderIdOrderByClosedDateDesc(orderId);
        return salesLogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SalesLogResponse> getSalesLogsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<SalesLog> salesLogs = salesLogRepository.findByClosedDateBetweenOrderByClosedDateDesc(startDate, endDate);
        return salesLogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SalesLogResponse> getSalesLogsByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        List<SalesLog> salesLogs = salesLogRepository.findByUserIdAndClosedDateBetweenOrderByClosedDateDesc(userId, startDate, endDate);
        return salesLogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SalesLogResponse getSalesLogByCallId(Long callId) {
        SalesLog salesLog = salesLogRepository.findByCallId(callId);
        if (salesLog == null) {
            throw new RuntimeException("No sales log found for call ID: " + callId);
        }
        return mapToResponse(salesLog);
    }

    public java.math.BigDecimal getTotalRevenueByUserId(Long userId) {
        return salesLogRepository.getTotalRevenueByUserId(userId);
    }

    public java.math.BigDecimal getTotalRevenueByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return salesLogRepository.getTotalRevenueByUserIdAndDateRange(userId, startDate, endDate);
    }

    public Long getSalesCountByUserId(Long userId) {
        return salesLogRepository.getSalesCountByUserId(userId);
    }

    public Long getSalesCountByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return salesLogRepository.getSalesCountByUserIdAndDateRange(userId, startDate, endDate);
    }

    private SalesLogResponse mapToResponse(SalesLog salesLog) {
        return SalesLogResponse.builder()
                .id(salesLog.getId())
                .customerName(salesLog.getCustomerName())
                .companyDetails(salesLog.getCompanyDetails())
                .saleAmount(salesLog.getSaleAmount())
                .closedDate(salesLog.getClosedDate())
                .callId(salesLog.getCall() != null ? salesLog.getCall().getId() : null)
                .callTitle(salesLog.getCall() != null ? salesLog.getCall().getCallTitle() : null)
                .orderId(salesLog.getOrder() != null ? salesLog.getOrder().getId() : null)
                .orderNumber(salesLog.getOrder() != null ? salesLog.getOrder().getOrderNumber() : null)
                .contactId(salesLog.getContact() != null ? salesLog.getContact().getId() : null)
                .contactName(salesLog.getContact() != null ? 
                    salesLog.getContact().getFirstName() + " " + salesLog.getContact().getLastName() : null)
                .userId(salesLog.getUser().getId())
                .userName(salesLog.getUser().getFirstName() + " " + salesLog.getUser().getLastName())
                .notes(salesLog.getNotes())
                .createdAt(salesLog.getCreatedAt())
                .updatedAt(salesLog.getUpdatedAt())
                .build();
    }
}
