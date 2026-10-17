package com.mpr.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Stores customer inputs submitted for personalized plan recommendation.
 */
@Entity
@Table(name = "recommendation_requests")
public class RecommendationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @NotNull(message = "Monthly data requirement is required")
    @DecimalMin(value = "0.0", message = "Monthly data cannot be negative")
    @Column(name = "monthly_data_gb", nullable = false)
    private Double monthlyDataGb;

    @NotNull(message = "Monthly calling minutes requirement is required")
    @Min(value = 0, message = "Monthly call minutes cannot be negative")
    @Column(name = "monthly_minutes", nullable = false)
    private Integer monthlyMinutes;

    @NotNull(message = "Budget limit is required")
    @DecimalMin(value = "1.0", message = "Budget must be at least ₹1.00")
    @Column(nullable = false)
    private Double budget;

    @Column(length = 255)
    private String preferences; // e.g. "OTT, 5G, Streaming"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public RecommendationRequest() {
    }

    public RecommendationRequest(Long userId, Double monthlyDataGb, Integer monthlyMinutes, Double budget, String preferences) {
        this.userId = userId;
        this.monthlyDataGb = monthlyDataGb;
        this.monthlyMinutes = monthlyMinutes;
        this.budget = budget;
        this.preferences = preferences;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getMonthlyDataGb() {
        return monthlyDataGb;
    }

    public void setMonthlyDataGb(Double monthlyDataGb) {
        this.monthlyDataGb = monthlyDataGb;
    }

    public Integer getMonthlyMinutes() {
        return monthlyMinutes;
    }

    public void setMonthlyMinutes(Integer monthlyMinutes) {
        this.monthlyMinutes = monthlyMinutes;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
