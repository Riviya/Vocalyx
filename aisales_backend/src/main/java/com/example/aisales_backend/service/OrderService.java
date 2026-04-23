package com.example.aisales_backend.service;

import com.example.aisales_backend.entity.Order;
import com.example.aisales_backend.entity.Contact;
import com.example.aisales_backend.repository.OrderRepository;
import com.example.aisales_backend.repository.ContactRepository;
import com.example.aisales_backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ContactRepository contactRepository;

    public Order createOrder(Long contactId, Long companyId, Double orderValue) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id " + contactId));

        Order order = Order.builder()
                .orderNumber("ORD-" + System.currentTimeMillis())
                .orderDate(LocalDateTime.now())
                .orderValue(orderValue)
                .contact(contact)
                .build();

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByContact(Long contactId) {
        return orderRepository.findByContactId(contactId);
    }

    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public Optional<Order> getMostRecentOrderByContact(Long contactId) {
        return orderRepository.findMostRecentOrderByContactId(contactId);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
    }
}
