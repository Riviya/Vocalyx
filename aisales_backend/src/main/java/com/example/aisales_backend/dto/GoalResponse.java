package com.example.aisales_backend.dto;

import java.time.LocalDate;

public class GoalResponse {
    private Long id;
    private String name;
    private String description;
    private Double targetRevenue;
    private Double currentProgress;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer progressPercentage;
    private String company;
    private String priority;

    public GoalResponse() {}

    public GoalResponse(Long id, String name, String description, Double targetRevenue,
                        Double currentProgress, LocalDate startDate, LocalDate endDate, String status,
                        String company, String priority) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.targetRevenue = targetRevenue;
        this.currentProgress = currentProgress;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.company = company;
        this.priority = priority;
        this.progressPercentage = calculateProgressPercentage();
    }

    private Integer calculateProgressPercentage() {
        if (targetRevenue == null || targetRevenue == 0) return 0;
        return (int) Math.min(100, Math.round((currentProgress / targetRevenue) * 100));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getTargetRevenue() { return targetRevenue; }
    public void setTargetRevenue(Double targetRevenue) { this.targetRevenue = targetRevenue; this.progressPercentage = calculateProgressPercentage(); }
    public Double getCurrentProgress() { return currentProgress; }
    public void setCurrentProgress(Double currentProgress) { this.currentProgress = currentProgress; this.progressPercentage = calculateProgressPercentage(); }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}


