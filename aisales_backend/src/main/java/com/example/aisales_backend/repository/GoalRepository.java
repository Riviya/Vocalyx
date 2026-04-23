package com.example.aisales_backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.aisales_backend.entity.Goal;
import com.example.aisales_backend.entity.User;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUser(User user);

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.user = :user")
    Long countByUser(@Param("user") User user);

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.user = :user AND g.status = 'Completed'")
    Long countCompletedByUser(@Param("user") User user);

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.user = :user AND g.status != 'Completed'")
    Long countPendingByUser(@Param("user") User user);

    @Query("SELECT AVG((g.currentProgress / g.targetRevenue) * 100) FROM Goal g WHERE g.user = :user")
    Double averageProgressByUser(@Param("user") User user);

    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.id != :excludeId AND " +
            "((g.startDate <= :endDate AND g.endDate >= :startDate))")
    List<Goal> findOverlappingGoals(@Param("user") User user,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    @Param("excludeId") Long excludeId);

    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.startDate <= CURRENT_DATE AND g.endDate >= CURRENT_DATE")
    List<Goal> findActiveGoalsByUser(@Param("user") User user);
}

