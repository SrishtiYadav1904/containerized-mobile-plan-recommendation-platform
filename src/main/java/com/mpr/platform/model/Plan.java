package com.mpr.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Mobile Plan Tariff entity representing catalog records.
 */
@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Operator name is required (e.g. Jio, Airtel, Vi, BSNL)")
    @Size(max = 50, message = "Operator name cannot exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String operator;

    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "1.0", message = "Price must be at least 1.00")
    @Column(nullable = false)
    private Double price;

    @NotNull(message = "Validity period is required")
    @Min(value = 1, message = "Validity must be at least 1 day")
    @Column(name = "validity_days", nullable = false)
    private Integer validityDays;

    @NotNull(message = "Daily data is required (use 0 for voice-only plans)")
    @DecimalMin(value = "0.0", message = "Data quota cannot be negative")
    @Column(name = "data_gb_per_day", nullable = false)
    private Double dataGbPerDay;

    @NotNull(message = "Voice minutes is required (enter -1 or 99999 for unlimited)")
    @Column(name = "voice_minutes", nullable = false)
    private Integer voiceMinutes;

    @NotNull(message = "SMS count is required")
    @Min(value = 0, message = "SMS count cannot be negative")
    @Column(name = "sms_count", nullable = false)
    private Integer smsCount;

    @Size(max = 500, message = "Benefits description cannot exceed 500 characters")
    @Column(length = 500)
    private String benefits;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanStatus status = PlanStatus.DRAFT;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Plan() {
        this.status = PlanStatus.DRAFT;
    }

    public Plan(String operator, String name, Double price, Integer validityDays,
                Double dataGbPerDay, Integer voiceMinutes, Integer smsCount, String benefits) {
        this.operator = operator;
        this.name = name;
        this.price = price;
        this.validityDays = validityDays;
        this.dataGbPerDay = dataGbPerDay;
        this.voiceMinutes = voiceMinutes;
        this.smsCount = smsCount;
        this.benefits = benefits;
        this.status = PlanStatus.DRAFT;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PlanStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }

    public Double getDataGbPerDay() {
        return dataGbPerDay;
    }

    public void setDataGbPerDay(Double dataGbPerDay) {
        this.dataGbPerDay = dataGbPerDay;
    }

    public Integer getVoiceMinutes() {
        return voiceMinutes;
    }

    public void setVoiceMinutes(Integer voiceMinutes) {
        this.voiceMinutes = voiceMinutes;
    }

    public Integer getSmsCount() {
        return smsCount;
    }

    public void setSmsCount(Integer smsCount) {
        this.smsCount = smsCount;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPublished() {
        return this.status == PlanStatus.PUBLISHED;
    }

    public String getVoiceDescription() {
        if (voiceMinutes == null || voiceMinutes < 0 || voiceMinutes >= 9999) {
            return "Unlimited Voice";
        }
        return voiceMinutes + " mins";
    }
}
