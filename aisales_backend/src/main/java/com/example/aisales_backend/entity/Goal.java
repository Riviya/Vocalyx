package com.example.aisales_backend.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Double targetRevenue;
    private Double currentProgress;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String company;
    private String priority; // e.g., High, Medium, Low

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Goal() {}

    public Goal(String name, String description, Double targetRevenue,
                Double currentProgress, LocalDate startDate, LocalDate endDate, 
                String status, User user,
                String company, String priority) {
        this.name = name;
        this.description = description;
        this.targetRevenue = targetRevenue;
        this.currentProgress = currentProgress;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.user = user;
        this.company = company;
        this.priority = priority;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getTargetRevenue() { return targetRevenue; }
    public void setTargetRevenue(Double targetRevenue) { this.targetRevenue = targetRevenue; }

    public Double getCurrentProgress() { return currentProgress; }
    public void setCurrentProgress(Double currentProgress) { this.currentProgress = currentProgress; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}


