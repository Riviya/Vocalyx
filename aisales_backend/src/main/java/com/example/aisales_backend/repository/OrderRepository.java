package com.example.aisales_backend.repository;

import com.example.aisales_backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by contact ID
    List<Order> findByContactId(Long contactId);
    
    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Find the most recent order for a contact
    @Query("SELECT o FROM Order o WHERE o.contact.id = :contactId ORDER BY o.orderDate DESC")
    Optional<Order> findMostRecentOrderByContactId(@Param("contactId") Long contactId);
    
    // Find orders by contact ID (simplified without company)
    @Query("SELECT o FROM Order o WHERE o.contact.id = :contactId ORDER BY o.orderDate DESC")
    List<Order> findByContactIdOrderByDateDesc(@Param("contactId") Long contactId);
}


