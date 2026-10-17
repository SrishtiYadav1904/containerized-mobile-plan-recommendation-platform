package com.mpr.platform.service;

import com.mpr.platform.model.Plan;
import com.mpr.platform.model.PlanStatus;
import com.mpr.platform.model.RecommendationRequest;
import com.mpr.platform.model.RecommendationResult;
import com.mpr.platform.repository.PlanRepository;
import com.mpr.platform.repository.RecommendationRequestRepository;
import com.mpr.platform.repository.RecommendationResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private RecommendationRequestRepository requestRepository;

    @Mock
    private RecommendationResultRepository resultRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(recommendationService, "dataWeight", 0.35);
        ReflectionTestUtils.setField(recommendationService, "voiceWeight", 0.25);
        ReflectionTestUtils.setField(recommendationService, "priceWeight", 0.25);
        ReflectionTestUtils.setField(recommendationService, "benefitsWeight", 0.15);
    }

    @Test
    @DisplayName("generateRecommendations excludes plans priced above budget")
    void testExcludesPlansOverBudget() {
        Plan affordablePlan = new Plan("Jio", "Jio Affordable", 299.0, 28, 1.5, -1, 100, "5G");
        affordablePlan.setStatus(PlanStatus.PUBLISHED);

        Plan expensivePlan = new Plan("Jio", "Jio Expensive", 899.0, 84, 2.0, -1, 100, "5G");
        expensivePlan.setStatus(PlanStatus.PUBLISHED);

        when(planRepository.findByStatus(PlanStatus.PUBLISHED)).thenReturn(List.of(affordablePlan, expensivePlan));
        when(requestRepository.save(any(RecommendationRequest.class))).thenAnswer(i -> {
            RecommendationRequest r = i.getArgument(0);
            r.setId(10L);
            return r;
        });
        when(resultRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        RecommendationRequest req = new RecommendationRequest(1L, 30.0, 300, 500.0, "5G");
        List<RecommendationResult> results = recommendationService.generateRecommendations(req);

        assertEquals(1, results.size(), "Only plans at or below budget should be included");
        assertEquals("Jio Affordable", results.get(0).getPlan().getName());
        assertEquals(1, results.get(0).getRank());
        assertTrue(results.get(0).getScore() > 0);
    }

    @Test
    @DisplayName("generateRecommendations considers only PUBLISHED plans")
    void testConsidersOnlyPublishedPlans() {
        when(planRepository.findByStatus(PlanStatus.PUBLISHED)).thenReturn(List.of());
        when(requestRepository.save(any(RecommendationRequest.class))).thenAnswer(i -> {
            RecommendationRequest r = i.getArgument(0);
            r.setId(10L);
            return r;
        });

        RecommendationRequest req = new RecommendationRequest(1L, 20.0, 200, 300.0, "");
        List<RecommendationResult> results = recommendationService.generateRecommendations(req);

        assertTrue(results.isEmpty());
        verify(planRepository, times(1)).findByStatus(PlanStatus.PUBLISHED);
    }

    @Test
    @DisplayName("generateRecommendations ranks plans with better data and price higher")
    void testRanksBestFitFirst() {
        Plan p1 = new Plan("Vi", "Vi Low Data", 250.0, 28, 0.5, -1, 100, ""); // 14 GB/mo
        p1.setStatus(PlanStatus.PUBLISHED);

        Plan p2 = new Plan("Airtel", "Airtel High Data", 299.0, 28, 2.0, -1, 100, "5G, OTT"); // 56 GB/mo
        p2.setStatus(PlanStatus.PUBLISHED);

        when(planRepository.findByStatus(PlanStatus.PUBLISHED)).thenReturn(List.of(p1, p2));
        when(requestRepository.save(any(RecommendationRequest.class))).thenAnswer(i -> {
            RecommendationRequest r = i.getArgument(0);
            r.setId(10L);
            return r;
        });
        when(resultRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        RecommendationRequest req = new RecommendationRequest(1L, 45.0, 500, 350.0, "5G, OTT");
        List<RecommendationResult> results = recommendationService.generateRecommendations(req);

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getRank());
        assertEquals("Airtel High Data", results.get(0).getPlan().getName());
        assertTrue(results.get(0).getScore() > results.get(1).getScore());
    }

    @Test
    @DisplayName("generateRecommendations rejects null or invalid input")
    void testRejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> recommendationService.generateRecommendations(null));

        RecommendationRequest invalidBudget = new RecommendationRequest(1L, 20.0, 200, -50.0, "");
        assertThrows(IllegalArgumentException.class, () -> recommendationService.generateRecommendations(invalidBudget));
    }
}
