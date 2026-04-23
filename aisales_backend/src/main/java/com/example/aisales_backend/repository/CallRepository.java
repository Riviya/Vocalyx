package com.example.aisales_backend.repository;

import com.example.aisales_backend.entity.Call;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CallRepository extends JpaRepository<Call, Long> {

    // Find calls by contact ID
    List<Call> findByContactId(Long contactId);

    // Find calls by order ID
    List<Call> findByOrderId(Long orderId);

    // Find calls by user ID
    List<Call> findByUserId(Long userId);

    List<Call> findBySentimentLabel(String sentimentLabel);

    // Find recent calls for a contact
    @Query("SELECT c FROM Call c WHERE c.contact.id = :contactId ORDER BY c.callDateTime DESC")
    List<Call> findRecentCallsByContactId(@Param("contactId") Long contactId);

    // Find call by ID with eager loading of relationships
    @Query("SELECT c FROM Call c LEFT JOIN FETCH c.contact LEFT JOIN FETCH c.user LEFT JOIN FETCH c.order WHERE c.id = :callId")
    Call findByIdWithRelationships(@Param("callId") Long callId);

    // Change from findTopByOrder_OrderIdOrderByCreatedAtDesc to findTopByOrder_IdOrderByCreatedAtDesc
    Optional<Call> findTopByOrder_IdOrderByCreatedAtDesc(Long orderId);

    @Query("SELECT c FROM Call c JOIN FETCH c.contact JOIN FETCH c.order ORDER BY c.callDateTime DESC")
    List<Call> findAllWithContactOrderByCallDateTimeDesc();

    @Query("SELECT c FROM Call c JOIN FETCH c.order WHERE c.id = :callId")
    Optional<Call> findByIdWithOrder(@Param("callId") Long callId);
}