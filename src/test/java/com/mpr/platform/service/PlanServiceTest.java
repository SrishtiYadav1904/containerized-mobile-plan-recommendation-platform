package com.mpr.platform.service;

import com.mpr.platform.model.Plan;
import com.mpr.platform.model.PlanStatus;
import com.mpr.platform.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanService planService;

    private Plan samplePlan;

    @BeforeEach
    void setUp() {
        samplePlan = new Plan("Jio", "5G Value 28D", 299.0, 28, 1.5, -1, 100, "Disney+ Hotstar");
        samplePlan.setId(1L);
    }

    @Test
    @DisplayName("createPlan must enforce DRAFT status and set audit timestamps")
    void testCreatePlanEnforcesDraftStatus() {
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plan created = planService.createPlan(samplePlan, "test_analyst");

        assertNotNull(created);
        assertEquals(PlanStatus.DRAFT, created.getStatus(), "Newly created plan status must be DRAFT");
        assertEquals("test_analyst", created.getCreatedBy());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        verify(planRepository, times(1)).save(samplePlan);
    }

    @Test
    @DisplayName("createPlan must reject zero or negative price")
    void testCreatePlanRejectsInvalidPrice() {
        samplePlan.setPrice(0.0);
        assertThrows(IllegalArgumentException.class, () -> planService.createPlan(samplePlan, "analyst"));

        samplePlan.setPrice(-100.0);
        assertThrows(IllegalArgumentException.class, () -> planService.createPlan(samplePlan, "analyst"));
    }

    @Test
    @DisplayName("Status transition: DRAFT to IN_REVIEW permitted for Analyst")
    void testTransitionDraftToInReview() {
        samplePlan.setStatus(PlanStatus.DRAFT);
        when(planRepository.findById(1L)).thenReturn(Optional.of(samplePlan));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plan transitioned = planService.transitionStatus(1L, PlanStatus.IN_REVIEW, "ANALYST", "analyst");
        assertEquals(PlanStatus.IN_REVIEW, transitioned.getStatus());
    }

    @Test
    @DisplayName("Status transition: IN_REVIEW to PUBLISHED permitted for Administrator")
    void testTransitionInReviewToPublishedAdmin() {
        samplePlan.setStatus(PlanStatus.IN_REVIEW);
        when(planRepository.findById(1L)).thenReturn(Optional.of(samplePlan));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plan transitioned = planService.transitionStatus(1L, PlanStatus.PUBLISHED, "ADMINISTRATOR", "admin");
        assertEquals(PlanStatus.PUBLISHED, transitioned.getStatus());
    }

    @Test
    @DisplayName("Status transition: IN_REVIEW to PUBLISHED rejected for Analyst")
    void testTransitionInReviewToPublishedRejectedForAnalyst() {
        samplePlan.setStatus(PlanStatus.IN_REVIEW);
        when(planRepository.findById(1L)).thenReturn(Optional.of(samplePlan));

        assertThrows(IllegalStateException.class, () ->
                planService.transitionStatus(1L, PlanStatus.PUBLISHED, "ANALYST", "analyst")
        );
    }

    @Test
    @DisplayName("Invalid status transition: DRAFT directly to PUBLISHED must throw IllegalStateException")
    void testInvalidTransitionDraftDirectlyToPublished() {
        samplePlan.setStatus(PlanStatus.DRAFT);
        when(planRepository.findById(1L)).thenReturn(Optional.of(samplePlan));

        assertThrows(IllegalStateException.class, () ->
                planService.transitionStatus(1L, PlanStatus.PUBLISHED, "ADMINISTRATOR", "admin")
        );
    }

    @Test
    @DisplayName("Status transition: PUBLISHED to ARCHIVED permitted for Administrator")
    void testTransitionPublishedToArchived() {
        samplePlan.setStatus(PlanStatus.PUBLISHED);
        when(planRepository.findById(1L)).thenReturn(Optional.of(samplePlan));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Plan transitioned = planService.transitionStatus(1L, PlanStatus.ARCHIVED, "ADMINISTRATOR", "admin");
        assertEquals(PlanStatus.ARCHIVED, transitioned.getStatus());
    }

    @Test
    @DisplayName("searchAndFilterPlans correctly filters by operator, price, and validity")
    void testSearchAndFilterPlans() {
        Plan p1 = new Plan("Jio", "Jio 28D Super", 299.0, 28, 1.5, -1, 100, "5G");
        p1.setStatus(PlanStatus.PUBLISHED);
        Plan p2 = new Plan("Airtel", "Airtel 56D Hero", 479.0, 56, 1.5, -1, 100, "Wynk");
        p2.setStatus(PlanStatus.PUBLISHED);

        when(planRepository.findByStatus(PlanStatus.PUBLISHED)).thenReturn(List.of(p1, p2));

        // Filter by operator Jio
        List<Plan> jioPlans = planService.searchAndFilterPlans(null, "Jio", null, null, "CUSTOMER");
        assertEquals(1, jioPlans.size());
        assertEquals("Jio", jioPlans.get(0).getOperator());

        // Filter by max price 300
        List<Plan> budgetPlans = planService.searchAndFilterPlans(null, null, 300.0, null, "CUSTOMER");
        assertEquals(1, budgetPlans.size());
        assertEquals(299.0, budgetPlans.get(0).getPrice());
    }

    @Test
    @DisplayName("getPlansForUserRole returns only PUBLISHED plans for Customers")
    void testCustomerViewsOnlyPublishedPlans() {
        Plan publishedPlan = new Plan("Airtel", "Airtel Super 28D", 349.0, 28, 2.0, -1, 100, "Prime Video");
        publishedPlan.setStatus(PlanStatus.PUBLISHED);

        when(planRepository.findByStatus(PlanStatus.PUBLISHED)).thenReturn(List.of(publishedPlan));

        List<Plan> customerPlans = planService.getPlansForUserRole("CUSTOMER");

        assertEquals(1, customerPlans.size());
        assertEquals(PlanStatus.PUBLISHED, customerPlans.get(0).getStatus());
        verify(planRepository, times(1)).findByStatus(PlanStatus.PUBLISHED);
    }
}
