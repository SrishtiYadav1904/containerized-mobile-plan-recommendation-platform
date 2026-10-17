package com.mpr.platform.service;

import com.mpr.platform.model.Plan;
import com.mpr.platform.model.PlanStatus;
import com.mpr.platform.repository.PlanRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

        // Enforce initial status as DRAFT
        plan.setStatus(PlanStatus.DRAFT);
        plan.setCreatedBy(createdBy != null ? createdBy : "analyst");
        plan.setUpdatedBy(createdBy != null ? createdBy : "analyst");
        plan.setCreatedAt(LocalDateTime.now());
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

    /**
     * Retrieve plans based on user role.
     * Customers can ONLY view PUBLISHED plans.
     * Analysts and Administrators can view all plans (DRAFT, IN_REVIEW, PUBLISHED, ARCHIVED).
     */
    @Transactional(readOnly = true)
    public List<Plan> getPlansForUserRole(String role) {
        if (role == null || role.equalsIgnoreCase("CUSTOMER") || role.equalsIgnoreCase("ROLE_CUSTOMER")) {
            return findPublishedPlans();
        }
        return findAllPlans();
    }

    /**
     * Determine if a user with the given role is permitted to inspect a specific plan record.
     */
    public boolean canUserViewPlan(Plan plan, String role) {
        if (plan == null) {
            return false;
        }
        if (plan.getStatus() == PlanStatus.PUBLISHED) {
            return true;
        }
        // Non-published plans (DRAFT, IN_REVIEW, ARCHIVED) visible only to Analyst and Admin
        return role != null && (
            role.equalsIgnoreCase("ANALYST") || role.equalsIgnoreCase("ROLE_ANALYST") ||
            role.equalsIgnoreCase("ADMINISTRATOR") || role.equalsIgnoreCase("ROLE_ADMINISTRATOR")
        );
    }

    /**
     * Seed sample initial data if database is empty for testing and immediate evaluation.
     */
    @PostConstruct
    public void seedInitialPlansIfEmpty() {
        if (planRepository.count() == 0) {
            // Seed sample plans across statuses and operators
            Plan p1 = new Plan("Jio", "Jio True 5G Unlimited 28D", 299.0, 28, 1.5, -1, 100, "Unlimited 5G Data, JioCinema, JioTV");
            p1.setStatus(PlanStatus.PUBLISHED);
            p1.setCreatedBy("admin");
            p1.setCreatedAt(LocalDateTime.now().minusDays(10));
            p1.setUpdatedAt(LocalDateTime.now().minusDays(10));
            planRepository.save(p1);

            Plan p2 = new Plan("Airtel", "Airtel Super Hero 28D", 349.0, 28, 2.0, -1, 100, "Unlimited 5G, Wynk Music, Apollo 24/7 Circle");
            p2.setStatus(PlanStatus.PUBLISHED);
            p2.setCreatedBy("admin");
            p2.setCreatedAt(LocalDateTime.now().minusDays(8));
            p2.setUpdatedAt(LocalDateTime.now().minusDays(8));
            planRepository.save(p2);

            Plan p3 = new Plan("Vodafone Idea", "Vi Hero Unlimited 56D", 479.0, 56, 1.5, -1, 100, "Binge All Night (12am-6am), Weekend Data Rollover");
            p3.setStatus(PlanStatus.PUBLISHED);
            p3.setCreatedBy("admin");
            p3.setCreatedAt(LocalDateTime.now().minusDays(5));
            p3.setUpdatedAt(LocalDateTime.now().minusDays(5));
            planRepository.save(p3);

            Plan p4 = new Plan("Jio", "Jio Long Term Annual Tariff", 2999.0, 365, 2.5, -1, 100, "Prime 5G, Disney+ Hotstar Mobile 1 Year");
            p4.setStatus(PlanStatus.DRAFT);
            p4.setCreatedBy("analyst");
            p4.setCreatedAt(LocalDateTime.now().minusDays(2));
            p4.setUpdatedAt(LocalDateTime.now().minusDays(2));
            planRepository.save(p4);

            Plan p5 = new Plan("BSNL", "BSNL Bharat Fiber Voice Saver", 199.0, 30, 2.0, -1, 100, "Free BSNL Tunes, Nationwide Roaming");
            p5.setStatus(PlanStatus.IN_REVIEW);
            p5.setCreatedBy("analyst");
            p5.setCreatedAt(LocalDateTime.now().minusDays(1));
            p5.setUpdatedAt(LocalDateTime.now().minusDays(1));
            planRepository.save(p5);
        }
    }
}
