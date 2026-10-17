package com.mpr.platform.service;

import com.mpr.platform.model.Plan;
import com.mpr.platform.model.PlanStatus;
import com.mpr.platform.repository.PlanRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    /**
     * Create a new plan record. Status is strictly set to DRAFT as per workflow rules.
     */
    public Plan createPlan(Plan plan, String createdBy) {
        if (plan == null) {
            throw new IllegalArgumentException("Plan cannot be null");
        }
        if (plan.getPrice() == null || plan.getPrice() <= 0) {
            throw new IllegalArgumentException("Plan price must be greater than zero");
        }
        if (plan.getValidityDays() == null || plan.getValidityDays() <= 0) {
            throw new IllegalArgumentException("Validity days must be greater than zero");
        }

        plan.setStatus(PlanStatus.DRAFT);
        plan.setCreatedBy(createdBy != null ? createdBy : "analyst");
        plan.setUpdatedBy(createdBy != null ? createdBy : "analyst");
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        return planRepository.save(plan);
    }

    /**
     * Update an existing plan record.
     */
    public Plan updatePlan(Long id, Plan updatedPlan, String editorName, String editorRole) {
        Plan plan = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found with id: " + id));

        // Customers cannot edit plans
        if (editorRole == null || editorRole.equalsIgnoreCase("CUSTOMER") || editorRole.equalsIgnoreCase("ROLE_CUSTOMER")) {
            throw new IllegalStateException("Access Denied: Customers are not permitted to modify plan records");
        }

        plan.setOperator(updatedPlan.getOperator());
        plan.setName(updatedPlan.getName());
        plan.setPrice(updatedPlan.getPrice());
        plan.setValidityDays(updatedPlan.getValidityDays());
        plan.setDataGbPerDay(updatedPlan.getDataGbPerDay());
        plan.setVoiceMinutes(updatedPlan.getVoiceMinutes());
        plan.setSmsCount(updatedPlan.getSmsCount());
        plan.setBenefits(updatedPlan.getBenefits());
        plan.setUpdatedBy(editorName != null ? editorName : "analyst");
        plan.setUpdatedAt(LocalDateTime.now());

        return planRepository.save(plan);
    }

    /**
     * Strictly enforced lifecycle state transitions:
     * DRAFT -> IN_REVIEW (Analyst submits)
     * IN_REVIEW -> PUBLISHED (Administrator publishes)
     * PUBLISHED -> ARCHIVED (Administrator archives)
     * Any other transition is rejected.
     */
    public Plan transitionStatus(Long id, PlanStatus targetStatus, String actorRole, String actorName) {
        Plan plan = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found with id: " + id));

        PlanStatus currentStatus = plan.getStatus();
        String role = actorRole != null ? actorRole.toUpperCase().replace("ROLE_", "") : "ANALYST";

        boolean valid = false;

        if (currentStatus == PlanStatus.DRAFT && targetStatus == PlanStatus.IN_REVIEW) {
            if ("ANALYST".equals(role) || "ADMINISTRATOR".equals(role)) {
                valid = true;
            } else {
                throw new IllegalStateException("Only Analysts or Administrators can submit plans for review");
            }
        } else if (currentStatus == PlanStatus.IN_REVIEW && targetStatus == PlanStatus.PUBLISHED) {
            if ("ADMINISTRATOR".equals(role)) {
                valid = true;
            } else {
                throw new IllegalStateException("Only Administrators can approve and publish plans");
            }
        } else if (currentStatus == PlanStatus.PUBLISHED && targetStatus == PlanStatus.ARCHIVED) {
            if ("ADMINISTRATOR".equals(role)) {
                valid = true;
            } else {
                throw new IllegalStateException("Only Administrators can archive plans");
            }
        }

        if (!valid) {
            throw new IllegalStateException(String.format(
                    "Invalid plan status transition from %s to %s by role %s",
                    currentStatus, targetStatus, role));
        }

        plan.setStatus(targetStatus);
        plan.setUpdatedBy(actorName != null ? actorName : role.toLowerCase());
        plan.setUpdatedAt(LocalDateTime.now());

        return planRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public Optional<Plan> findById(Long id) {
        return planRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Plan> findAllPlans() {
        return planRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Plan> findPublishedPlans() {
        return planRepository.findByStatus(PlanStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public List<Plan> getPlansForUserRole(String role) {
        if (role == null || role.equalsIgnoreCase("CUSTOMER") || role.equalsIgnoreCase("ROLE_CUSTOMER")) {
            return findPublishedPlans();
        }
        return findAllPlans();
    }

    public boolean canUserViewPlan(Plan plan, String role) {
        if (plan == null) {
            return false;
        }
        if (plan.getStatus() == PlanStatus.PUBLISHED) {
            return true;
        }
        return role != null && (
            role.equalsIgnoreCase("ANALYST") || role.equalsIgnoreCase("ROLE_ANALYST") ||
            role.equalsIgnoreCase("ADMINISTRATOR") || role.equalsIgnoreCase("ROLE_ADMINISTRATOR")
        );
    }

    /**
     * Search and filter plans by keyword, operator, maximum price, and validity days.
     */
    @Transactional(readOnly = true)
    public List<Plan> searchAndFilterPlans(String query, String operator, Double maxPrice, Integer validity, String role) {
        List<Plan> basePlans = getPlansForUserRole(role);

        return basePlans.stream()
                .filter(p -> {
                    if (query != null && !query.trim().isEmpty()) {
                        String q = query.trim().toLowerCase();
                        boolean matchName = p.getName().toLowerCase().contains(q);
                        boolean matchOp = p.getOperator().toLowerCase().contains(q);
                        boolean matchBenefits = p.getBenefits() != null && p.getBenefits().toLowerCase().contains(q);
                        if (!matchName && !matchOp && !matchBenefits) return false;
                    }
                    if (operator != null && !operator.trim().isEmpty() && !operator.equalsIgnoreCase("ALL")) {
                        if (!p.getOperator().equalsIgnoreCase(operator.trim())) return false;
                    }
                    if (maxPrice != null && maxPrice > 0) {
                        if (p.getPrice() > maxPrice) return false;
                    }
                    if (validity != null && validity > 0) {
                        if (!Objects.equals(p.getValidityDays(), validity)) return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Plan::getPrice))
                .collect(Collectors.toList());
    }

    // Analytics & Metrics for Dashboard

    @Transactional(readOnly = true)
    public Map<PlanStatus, Long> getPlanCountsByStatus() {
        Map<PlanStatus, Long> counts = new EnumMap<>(PlanStatus.class);
        for (PlanStatus status : PlanStatus.values()) {
            counts.put(status, planRepository.countByStatus(status));
        }
        return counts;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getPlanCountsByOperator() {
        return planRepository.findAll().stream()
                .collect(Collectors.groupingBy(Plan::getOperator, Collectors.counting()));
    }

    @Transactional(readOnly = true)
    public double getAveragePlanPrice() {
        return planRepository.findAll().stream()
                .mapToDouble(Plan::getPrice)
                .average()
                .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getAveragePriceByOperator() {
        return planRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Plan::getOperator,
                        Collectors.averagingDouble(Plan::getPrice)
                ));
    }

    /**
     * Seeds 24 realistic sample plans across 4 operators (Jio, Airtel, Vi, BSNL)
     * covering different price tiers, data allowances, validity periods, and statuses.
     */
    @PostConstruct
    public void seedInitialPlansIfEmpty() {
        if (planRepository.count() < 10) {
            planRepository.deleteAll();

            List<Plan> seedPlans = List.of(
                // Jio Plans (7 plans)
                createSeed("Jio", "Jio True 5G Starter 28D", 239.0, 28, 1.5, -1, 100, "Unlimited 5G Data, JioTV, JioCinema", PlanStatus.PUBLISHED, "admin"),
                createSeed("Jio", "Jio True 5G Popular 28D", 299.0, 28, 2.0, -1, 100, "Unlimited 5G, JioCinema Premium, JioCloud 6GB", PlanStatus.PUBLISHED, "admin"),
                createSeed("Jio", "Jio Entertainment 28D", 398.0, 28, 2.0, -1, 100, "Disney+ Hotstar 3 Months, JioTV", PlanStatus.PUBLISHED, "admin"),
                createSeed("Jio", "Jio Hero 56D", 533.0, 56, 2.0, -1, 100, "Unlimited 5G Data, Free JioTunes, JioCinema", PlanStatus.PUBLISHED, "admin"),
                createSeed("Jio", "Jio Quarterly Power 84D", 719.0, 84, 2.0, -1, 100, "Unlimited 5G, JioCinema, JioTV, JioCloud", PlanStatus.PUBLISHED, "admin"),
                createSeed("Jio", "Jio Long Term Annual Tariff", 2999.0, 365, 2.5, -1, 100, "Prime 5G, Disney+ Hotstar 1 Year, SonyLIV", PlanStatus.DRAFT, "analyst"),
                createSeed("Jio", "Jio Voice Basic 28D", 155.0, 28, 0.07, -1, 300, "National Voice, 2GB Monthly Total Data", PlanStatus.IN_REVIEW, "analyst"),

                // Airtel Plans (6 plans)
                createSeed("Airtel", "Airtel Smart Recharge 28D", 265.0, 28, 1.0, -1, 100, "Unlimited 5G, Free Hellotunes, Wynk Music", PlanStatus.PUBLISHED, "admin"),
                createSeed("Airtel", "Airtel Super Hero 28D", 349.0, 28, 2.0, -1, 100, "Unlimited 5G, Apollo 24/7 Circle, Wynk Music", PlanStatus.PUBLISHED, "admin"),
                createSeed("Airtel", "Airtel OTT Pro 28D", 499.0, 28, 3.0, -1, 100, "Disney+ Hotstar Mobile, Xstream Play, Wynk", PlanStatus.PUBLISHED, "admin"),
                createSeed("Airtel", "Airtel Mid-Term 56D", 549.0, 56, 2.0, -1, 100, "Unlimited 5G, Wynk Free, Spam Call Protection", PlanStatus.PUBLISHED, "admin"),
                createSeed("Airtel", "Airtel Mega 84D", 839.0, 84, 2.0, -1, 100, "Disney+ Hotstar 3 Months, Xstream Play 84D", PlanStatus.PUBLISHED, "admin"),
                createSeed("Airtel", "Airtel Annual Infinity 365D", 3359.0, 365, 2.5, -1, 100, "Disney+ Hotstar 1 Year, Apollo 24/7, Wynk", PlanStatus.ARCHIVED, "admin"),

                // Vodafone Idea (Vi) Plans (6 plans)
                createSeed("Vodafone Idea", "Vi Unlimited 28D", 299.0, 28, 1.5, -1, 100, "Binge All Night (12am-6am), Weekend Data Rollover", PlanStatus.PUBLISHED, "admin"),
                createSeed("Vodafone Idea", "Vi Hero Unlimited 28D", 359.0, 28, 3.0, -1, 100, "Data Delight 2GB extra, Binge All Night", PlanStatus.PUBLISHED, "admin"),
                createSeed("Vodafone Idea", "Vi Mid-Term Saver 56D", 479.0, 56, 1.5, -1, 100, "Weekend Data Rollover, Free Vi Movies & TV", PlanStatus.PUBLISHED, "admin"),
                createSeed("Vodafone Idea", "Vi Quarterly 84D", 719.0, 84, 1.5, -1, 100, "Binge All Night, Data Delight, Vi Movies & TV", PlanStatus.PUBLISHED, "admin"),
                createSeed("Vodafone Idea", "Vi Annual Entertainment", 2899.0, 365, 1.5, -1, 100, "Binge All Night, Free Disney+ Hotstar 1 Year", PlanStatus.IN_REVIEW, "analyst"),
                createSeed("Vodafone Idea", "Vi Mini Data Saver 21D", 199.0, 21, 1.0, -1, 100, "Basic calling with weekend rollover", PlanStatus.DRAFT, "analyst"),

                // BSNL Plans (5 plans)
                createSeed("BSNL", "BSNL Budget Voice Saver 30D", 147.0, 30, 0.33, -1, 100, "Free BSNL Tunes, Nationwide Roaming, 10GB lump sum", PlanStatus.PUBLISHED, "admin"),
                createSeed("BSNL", "BSNL Bharat Fiber Voice Saver 30D", 199.0, 30, 2.0, -1, 100, "Free BSNL Tunes, National Voice Calling", PlanStatus.PUBLISHED, "admin"),
                createSeed("BSNL", "BSNL Bi-Monthly Power 70D", 397.0, 70, 2.0, -1, 100, "Unlimited calls for first 30 days, BSNL Tunes", PlanStatus.PUBLISHED, "admin"),
                createSeed("BSNL", "BSNL Long Validity 150D", 399.0, 150, 1.0, -1, 100, "Nationwide roaming, 1GB data per day for 80 days", PlanStatus.DRAFT, "analyst"),
                createSeed("BSNL", "BSNL Annual Super Saver 365D", 1999.0, 365, 2.0, -1, 100, "600GB lump sum data, Unlimited voice calling", PlanStatus.PUBLISHED, "admin")
            );

            planRepository.saveAll(seedPlans);
        }
    }

    private Plan createSeed(String operator, String name, Double price, Integer validity,
                            Double data, Integer voice, Integer sms, String benefits,
                            PlanStatus status, String createdBy) {
        Plan p = new Plan(operator, name, price, validity, data, voice, sms, benefits);
        p.setStatus(status);
        p.setCreatedBy(createdBy);
        p.setUpdatedBy(createdBy);
        p.setCreatedAt(LocalDateTime.now().minusDays(5));
        p.setUpdatedAt(LocalDateTime.now().minusDays(2));
        return p;
    }
}
