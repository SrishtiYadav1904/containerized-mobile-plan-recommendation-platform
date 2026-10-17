package com.mpr.platform.model;

import jakarta.persistence.*;

/**
 * Stores ranked recommendation score outputs computed for a RecommendationRequest.
 */
@Entity
@Table(name = "recommendation_results")
public class RecommendationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private Double score;

    @Column(name = "plan_rank", nullable = false)
    private Integer rank;

    @Column(name = "score_breakdown", length = 255)
    private String scoreBreakdown;

    public RecommendationResult() {
    }

    public RecommendationResult(Long requestId, Plan plan, Double score, Integer rank, String scoreBreakdown) {
        this.requestId = requestId;
        this.plan = plan;
        this.score = score;
        this.rank = rank;
        this.scoreBreakdown = scoreBreakdown;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getScoreBreakdown() {
        return scoreBreakdown;
    }

    public void setScoreBreakdown(String scoreBreakdown) {
        this.scoreBreakdown = scoreBreakdown;
    }
}
