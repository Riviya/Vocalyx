package com.example.aisales_backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.aisales_backend.entity.SalesLog;

@Repository
public interface SalesLogRepository extends JpaRepository<SalesLog, Long> {

    // Find sales logs by user
    List<SalesLog> findByUserIdOrderByClosedDateDesc(Long userId);

    // Find sales logs by contact
    List<SalesLog> findByContactIdOrderByClosedDateDesc(Long contactId);

    // Find sales logs by call
    SalesLog findByCallId(Long callId);

    // Find sales logs by order
    List<SalesLog> findByOrderIdOrderByClosedDateDesc(Long orderId);

    // Find sales logs within date range
    List<SalesLog> findByClosedDateBetweenOrderByClosedDateDesc(LocalDate startDate, LocalDate endDate);

    // Find sales logs by user within date range
    List<SalesLog> findByUserIdAndClosedDateBetweenOrderByClosedDateDesc(Long userId, LocalDate startDate, LocalDate endDate);

    // Get total revenue by user
    @Query("SELECT COALESCE(SUM(s.saleAmount), 0) FROM SalesLog s WHERE s.user.id = :userId")
    java.math.BigDecimal getTotalRevenueByUserId(@Param("userId") Long userId);

    // Get total revenue by user within date range
    @Query("SELECT COALESCE(SUM(s.saleAmount), 0) FROM SalesLog s WHERE s.user.id = :userId AND s.closedDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalRevenueByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get sales count by user
    @Query("SELECT COUNT(s) FROM SalesLog s WHERE s.user.id = :userId")
    Long getSalesCountByUserId(@Param("userId") Long userId);

    // Get sales count by user within date range
    @Query("SELECT COUNT(s) FROM SalesLog s WHERE s.user.id = :userId AND s.closedDate BETWEEN :startDate AND :endDate")
    Long getSalesCountByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Fetch sales logs with eager loading of related entities
    @Query("SELECT s FROM SalesLog s " +
           "LEFT JOIN FETCH s.call " +
           "LEFT JOIN FETCH s.order " +
           "LEFT JOIN FETCH s.contact " +
           "LEFT JOIN FETCH s.user " +
           "WHERE s.user.id = :userId " +
           "ORDER BY s.closedDate DESC")
    List<SalesLog> findByUserIdWithEagerLoading(@Param("userId") Long userId);
}
