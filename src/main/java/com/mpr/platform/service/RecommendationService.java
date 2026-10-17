package com.mpr.platform.service;

import com.mpr.platform.model.Plan;
import com.mpr.platform.model.PlanStatus;
import com.mpr.platform.model.RecommendationRequest;
import com.mpr.platform.model.RecommendationResult;
import com.mpr.platform.repository.PlanRepository;
import com.mpr.platform.repository.RecommendationRequestRepository;
import com.mpr.platform.repository.RecommendationResultRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Transparent, rule-based recommendation scoring engine.
 * Computes multi-attribute ranked scores without opaque machine-learning models.
 */
@Service
@Transactional
public class RecommendationService {

    // Configurable scoring weights
    @Value("${recommendation.weight.data:0.35}")
    private double dataWeight;

    @Value("${recommendation.weight.voice:0.25}")
    private double voiceWeight;

    @Value("${recommendation.weight.price:0.25}")
    private double priceWeight;

    @Value("${recommendation.weight.benefits:0.15}")
    private double benefitsWeight;

    private final PlanRepository planRepository;
    private final RecommendationRequestRepository requestRepository;
    private final RecommendationResultRepository resultRepository;

    public RecommendationService(PlanRepository planRepository,
                                 RecommendationRequestRepository requestRepository,
                                 RecommendationResultRepository resultRepository) {
        this.planRepository = planRepository;
        this.requestRepository = requestRepository;
        this.resultRepository = resultRepository;
    }

    /**
     * Compute ranked recommendations for a customer's usage and budget inputs.
     */
    public List<RecommendationResult> generateRecommendations(RecommendationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Recommendation request cannot be null");
        }
        if (request.getBudget() == null || request.getBudget() <= 0) {
            throw new IllegalArgumentException("Budget must be greater than zero");
        }
        if (request.getMonthlyDataGb() == null || request.getMonthlyDataGb() < 0) {
            throw new IllegalArgumentException("Monthly data quota cannot be negative");
        }
        if (request.getMonthlyMinutes() == null || request.getMonthlyMinutes() < 0) {
            throw new IllegalArgumentException("Monthly call minutes cannot be negative");
        }

        // Save incoming request
        RecommendationRequest savedRequest = requestRepository.save(request);

        // Fetch candidate plans: ONLY PUBLISHED plans, and ONLY plans within budget
        List<Plan> eligiblePlans = planRepository.findByStatus(PlanStatus.PUBLISHED).stream()
                .filter(p -> p.getPrice() <= savedRequest.getBudget())
                .collect(Collectors.toList());

        if (eligiblePlans.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScoredPlan> scoredPlans = new ArrayList<>();
        for (Plan plan : eligiblePlans) {
            double dataScore = calculateDataScore(plan, savedRequest.getMonthlyDataGb());
            double voiceScore = calculateVoiceScore(plan, savedRequest.getMonthlyMinutes());
            double priceScore = calculatePriceScore(plan, savedRequest.getBudget());
            double benefitScore = calculateBenefitScore(plan, savedRequest.getPreferences());

            double totalScore = (dataScore * dataWeight) +
                                (voiceScore * voiceWeight) +
                                (priceScore * priceWeight) +
                                (benefitScore * benefitsWeight);

            double roundedScore = BigDecimal.valueOf(totalScore)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();

            String breakdown = String.format("Data: %.0f/100, Calls: %.0f/100, Savings: %.0f/100, Perks: %.0f/100",
                    dataScore, voiceScore, priceScore, benefitScore);

            scoredPlans.add(new ScoredPlan(plan, roundedScore, breakdown));
        }

        // Sort descending by score, then ascending by price
        scoredPlans.sort(Comparator
                .comparingDouble(ScoredPlan::getScore).reversed()
                .thenComparingDouble(sp -> sp.getPlan().getPrice()));

        // Assign 1-based ranks and persist results
        List<RecommendationResult> results = new ArrayList<>();
        int rank = 1;
        for (ScoredPlan sp : scoredPlans) {
            RecommendationResult result = new RecommendationResult(
                    savedRequest.getId(),
                    sp.getPlan(),
                    sp.getScore(),
                    rank++,
                    sp.getBreakdown()
            );
            results.add(result);
        }

        return resultRepository.saveAll(results);
    }

    private double calculateDataScore(Plan plan, double requestedMonthlyGb) {
        if (requestedMonthlyGb <= 0) return 100.0;

        // Estimate monthly data provided by the plan
        double effectiveDaysInMonth = Math.min(plan.getValidityDays(), 30);
        double monthlyProvidedGb = plan.getDataGbPerDay() * effectiveDaysInMonth;

        if (monthlyProvidedGb >= requestedMonthlyGb) {
            return 100.0;
        }
        return Math.min(100.0, (monthlyProvidedGb / requestedMonthlyGb) * 100.0);
    }

    private double calculateVoiceScore(Plan plan, int requestedMonthlyMinutes) {
        if (requestedMonthlyMinutes <= 0) return 100.0;
        if (plan.getVoiceMinutes() == -1 || plan.getVoiceMinutes() >= 9999) {
            return 100.0; // Unlimited voice calling
        }

        double effectiveDays = Math.min(plan.getValidityDays(), 30);
        double monthlyProvidedMins = ((double) plan.getVoiceMinutes() / plan.getValidityDays()) * effectiveDays;

        if (monthlyProvidedMins >= requestedMonthlyMinutes) {
            return 100.0;
        }
        return Math.min(100.0, (monthlyProvidedMins / requestedMonthlyMinutes) * 100.0);
    }

    private double calculatePriceScore(Plan plan, double budget) {
        if (budget <= 0) return 50.0;
        double savings = budget - plan.getPrice();
        double savingsRatio = savings / budget; // 0.0 to 1.0
        return 50.0 + (savingsRatio * 50.0);
    }

    private double calculateBenefitScore(Plan plan, String preferences) {
        if (plan.getBenefits() == null || plan.getBenefits().trim().isEmpty()) {
            return 30.0;
        }
        if (preferences == null || preferences.trim().isEmpty()) {
            return 80.0; // Baseline perk appreciation
        }

        String[] keywords = preferences.toLowerCase().split("[,\\s]+");
        String benefitsLower = plan.getBenefits().toLowerCase();

        int matches = 0;
        for (String kw : keywords) {
            if (!kw.trim().isEmpty() && benefitsLower.contains(kw.trim())) {
                matches++;
            }
        }

        if (matches >= 2) return 100.0;
        if (matches == 1) return 85.0;
        return 40.0;
    }

    public List<RecommendationResult> getResultsForRequest(Long requestId) {
        return resultRepository.findByRequestIdOrderByRankAsc(requestId);
    }

    public long getTotalRecommendationsComputed() {
        return requestRepository.count();
    }

    private static class ScoredPlan {
        private final Plan plan;
        private final double score;
        private final String breakdown;

        public ScoredPlan(Plan plan, double score, String breakdown) {
            this.plan = plan;
            this.score = score;
            this.breakdown = breakdown;
        }

        public Plan getPlan() {
            return plan;
        }

        public double getScore() {
            return score;
        }

        public String getBreakdown() {
            return breakdown;
        }
    }
}
