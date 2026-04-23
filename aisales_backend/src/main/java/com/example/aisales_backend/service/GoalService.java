package com.example.aisales_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.aisales_backend.dto.DashboardSummary;
import com.example.aisales_backend.dto.GoalRequest;
import com.example.aisales_backend.dto.GoalResponse;
import com.example.aisales_backend.entity.Goal;
import com.example.aisales_backend.entity.User;
import com.example.aisales_backend.repository.GoalRepository;
import com.example.aisales_backend.repository.UserRepository;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String determineStatus(Double currentProgress, Double targetRevenue) {
        double cp = currentProgress == null ? 0 : currentProgress;
        double tv = targetRevenue == null ? 0 : targetRevenue;
        if (cp == 0) return "Not Started";
        if (tv > 0 && cp >= tv) return "Completed";
        return "In Progress";
    }

    private GoalResponse convertToResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getDescription(),
                goal.getTargetRevenue(),
                goal.getCurrentProgress(),
                goal.getStartDate(),
                goal.getEndDate(),
                goal.getStatus(),
                goal.getCompany(),
                goal.getPriority()
        );
    }

    public List<GoalResponse> getAllGoals() {
        User user = getCurrentUser();
        return goalRepository.findByUser(user).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public GoalResponse getGoalById(Long id) {
        User user = getCurrentUser();
        Optional<Goal> goal = goalRepository.findById(id);
        if (goal.isPresent() && goal.get().getUser().getId().equals(user.getId())) {
            return convertToResponse(goal.get());
        } else {
            throw new RuntimeException("Goal not found or access denied");
        }
    }

    public GoalResponse createGoal(GoalRequest goalRequest) {
        User user = getCurrentUser();

        Goal goal = new Goal();
        goal.setName(goalRequest.getName());
        goal.setDescription(goalRequest.getDescription());
        goal.setTargetRevenue(goalRequest.getTargetRevenue());
        goal.setCurrentProgress(goalRequest.getCurrentProgress() == null ? 0.0 : goalRequest.getCurrentProgress());
        goal.setStartDate(goalRequest.getStartDate());
        goal.setEndDate(goalRequest.getEndDate());
        goal.setCompany(goalRequest.getCompany());
        goal.setPriority(goalRequest.getPriority());
        goal.setStatus(determineStatus(goal.getCurrentProgress(), goal.getTargetRevenue()));
        goal.setUser(user);

        Goal savedGoal = goalRepository.save(goal);
        return convertToResponse(savedGoal);
    }

    public GoalResponse updateGoal(Long id, GoalRequest goalRequest) {
        User user = getCurrentUser();
        Optional<Goal> goalOptional = goalRepository.findById(id);

        if (goalOptional.isPresent() && goalOptional.get().getUser().getId().equals(user.getId())) {
            Goal goal = goalOptional.get();
            goal.setName(goalRequest.getName());
            goal.setDescription(goalRequest.getDescription());
            goal.setTargetRevenue(goalRequest.getTargetRevenue());
            goal.setCurrentProgress(goalRequest.getCurrentProgress() == null ? goal.getCurrentProgress() : goalRequest.getCurrentProgress());
            goal.setStartDate(goalRequest.getStartDate());
            goal.setEndDate(goalRequest.getEndDate());
            goal.setCompany(goalRequest.getCompany());
            goal.setPriority(goalRequest.getPriority());
            goal.setStatus(determineStatus(goal.getCurrentProgress(), goal.getTargetRevenue()));
            Goal updatedGoal = goalRepository.save(goal);
            return convertToResponse(updatedGoal);
        } else {
            throw new RuntimeException("Goal not found or access denied");
        }
    }

    public void deleteGoal(Long id) {
        User user = getCurrentUser();
        Optional<Goal> goal = goalRepository.findById(id);
        if (goal.isPresent() && goal.get().getUser().getId().equals(user.getId())) {
            goalRepository.deleteById(id);
        } else {
            throw new RuntimeException("Goal not found or access denied");
        }
    }

    public DashboardSummary getDashboardSummary() {
        User user = getCurrentUser();
        Long totalGoals = goalRepository.countByUser(user);
        Long achievedGoals = goalRepository.countCompletedByUser(user);
        Long pendingGoals = goalRepository.countPendingByUser(user);
        Double avgProgress = goalRepository.averageProgressByUser(user);
        Integer averageProgress = avgProgress != null ? (int) Math.round(avgProgress) : 0;
        return new DashboardSummary(totalGoals, achievedGoals, pendingGoals, averageProgress);
    }
}


