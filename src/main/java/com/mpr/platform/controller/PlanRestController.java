package com.mpr.platform.controller;

import com.mpr.platform.model.Plan;
import com.mpr.platform.model.PlanStatus;
import com.mpr.platform.model.RecommendationRequest;
import com.mpr.platform.model.RecommendationResult;
import com.mpr.platform.service.PlanService;
import com.mpr.platform.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PlanRestController {

    private final PlanService planService;
    private final RecommendationService recommendationService;

    public PlanRestController(PlanService planService, RecommendationService recommendationService) {
        this.planService = planService;
        this.recommendationService = recommendationService;
    }

    private String extractPrimaryRole(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "CUSTOMER";
        }
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String authName = ga.getAuthority();
            if (authName.startsWith("ROLE_")) {
                return authName.substring(5);
            }
            return authName;
        }
        return "CUSTOMER";
    }

    @GetMapping("/plans")
    public List<Plan> getAllPlans(@RequestParam(value = "q", required = false) String query,
                                  @RequestParam(value = "operator", required = false) String operator,
                                  @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                                  @RequestParam(value = "validity", required = false) Integer validity,
                                  Authentication authentication) {
        String role = extractPrimaryRole(authentication);
        return planService.searchAndFilterPlans(query, operator, maxPrice, validity, role);
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<Plan> getPlanById(@PathVariable Long id, Authentication authentication) {
        Plan plan = planService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found with id " + id));

        String role = extractPrimaryRole(authentication);
        if (!planService.canUserViewPlan(plan, role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Customers can only view PUBLISHED plans.");
        }
        return ResponseEntity.ok(plan);
    }

    @PostMapping("/plans")
    public ResponseEntity<Plan> createPlan(@Valid @RequestBody Plan plan, Principal principal) {
        String username = principal != null ? principal.getName() : "analyst";
        Plan created = planService.createPlan(plan, username);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<Plan> updatePlan(@PathVariable Long id,
                                          @Valid @RequestBody Plan plan,
                                          Authentication authentication,
                                          Principal principal) {
        String role = extractPrimaryRole(authentication);
        String username = principal != null ? principal.getName() : "analyst";
        try {
            Plan updated = planService.updatePlan(id, plan, username, role);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PatchMapping("/plans/{id}/status")
    public ResponseEntity<Plan> updateStatus(@PathVariable Long id,
                                            @RequestBody Map<String, String> payload,
                                            Authentication authentication,
                                            Principal principal) {
        String statusStr = payload.get("status");
        if (statusStr == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field 'status' is required");
        }
        PlanStatus targetStatus;
        try {
            targetStatus = PlanStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + statusStr);
        }

        String role = extractPrimaryRole(authentication);
        String username = principal != null ? principal.getName() : "analyst";

        try {
            Plan transitioned = planService.transitionStatus(id, targetStatus, role, username);
            return ResponseEntity.ok(transitioned);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping("/recommendations")
    public ResponseEntity<List<RecommendationResult>> getRecommendations(@Valid @RequestBody RecommendationRequest request) {
        List<RecommendationResult> results = recommendationService.generateRecommendations(request);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("statusCounts", planService.getPlanCountsByStatus());
        summary.put("operatorCounts", planService.getPlanCountsByOperator());
        summary.put("averagePrice", planService.getAveragePlanPrice());
        summary.put("averagePriceByOperator", planService.getAveragePriceByOperator());
        summary.put("totalRecommendationsComputed", recommendationService.getTotalRecommendationsComputed());
        summary.put("totalPlans", planService.findAllPlans().size());
        return ResponseEntity.ok(summary);
    }
}
