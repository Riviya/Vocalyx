package com.example.aisales_backend.dto;

public class DashboardSummary {
    private Long totalGoals;
    private Long achievedGoals;
    private Long pendingGoals;
    private Integer averageProgress;

    public DashboardSummary() {}

    public DashboardSummary(Long totalGoals, Long achievedGoals, Long pendingGoals, Integer averageProgress) {
        this.totalGoals = totalGoals;
        this.achievedGoals = achievedGoals;
        this.pendingGoals = pendingGoals;
        this.averageProgress = averageProgress;
    }

    public Long getTotalGoals() { return totalGoals; }
    public void setTotalGoals(Long totalGoals) { this.totalGoals = totalGoals; }
    public Long getAchievedGoals() { return achievedGoals; }
    public void setAchievedGoals(Long achievedGoals) { this.achievedGoals = achievedGoals; }
    public Long getPendingGoals() { return pendingGoals; }
    public void setPendingGoals(Long pendingGoals) { this.pendingGoals = pendingGoals; }
    public Integer getAverageProgress() { return averageProgress; }
    public void setAverageProgress(Integer averageProgress) { this.averageProgress = averageProgress; }
}


