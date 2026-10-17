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
    @DisplayName("getPlansForUserRole returns only PUBLISHED plans for Customers")
    void testCustomerViewsOnlyPublishedPlans() {
        Plan publishedPlan = new Plan("Airtel", "Airtel Super 28D", 349.0, 28, 2.0, -1, 100, "Prime Video");
        publishedPlan.setStatus(PlanStatus.PUBLISHED);

        when(planRepository.findByStatus(PlanStatus.PUBLISHED)).thenReturn(List.of(publishedPlan));

        List<Plan> customerPlans = planService.getPlansForUserRole("CUSTOMER");

        assertEquals(1, customerPlans.size());
        assertEquals(PlanStatus.PUBLISHED, customerPlans.get(0).getStatus());
        verify(planRepository, times(1)).findByStatus(PlanStatus.PUBLISHED);
        verify(planRepository, never()).findAll();
    }

    @Test
    @DisplayName("getPlansForUserRole returns all plans for Analysts")
    void testAnalystViewsAllPlans() {
        Plan draftPlan = new Plan("Jio", "Jio Draft Plan", 199.0, 28, 1.0, 300, 100, "None");
        draftPlan.setStatus(PlanStatus.DRAFT);

        Plan publishedPlan = new Plan("Airtel", "Airtel Super", 349.0, 28, 2.0, -1, 100, "Prime Video");
        publishedPlan.setStatus(PlanStatus.PUBLISHED);

        when(planRepository.findAll()).thenReturn(List.of(draftPlan, publishedPlan));

        List<Plan> analystPlans = planService.getPlansForUserRole("ANALYST");

        assertEquals(2, analystPlans.size());
        verify(planRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("canUserViewPlan permits customer only if PUBLISHED")
    void testCanUserViewPlanPermissions() {
        Plan draftPlan = new Plan("Vi", "Vi Draft", 249.0, 28, 1.5, -1, 100, "");
        draftPlan.setStatus(PlanStatus.DRAFT);

        Plan publishedPlan = new Plan("Vi", "Vi Published", 299.0, 28, 1.5, -1, 100, "");
        publishedPlan.setStatus(PlanStatus.PUBLISHED);

        assertFalse(planService.canUserViewPlan(draftPlan, "CUSTOMER"));
        assertTrue(planService.canUserViewPlan(draftPlan, "ANALYST"));
        assertTrue(planService.canUserViewPlan(draftPlan, "ADMINISTRATOR"));

        assertTrue(planService.canUserViewPlan(publishedPlan, "CUSTOMER"));
        assertTrue(planService.canUserViewPlan(publishedPlan, "ANALYST"));
    }
}
