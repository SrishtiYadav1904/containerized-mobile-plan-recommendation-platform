package com.mpr.platform.controller;

import com.mpr.platform.model.Plan;
import com.mpr.platform.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

/**
 * REST API for mobile plan operations as specified in the platform architecture.
 */
@RestController
@RequestMapping("/api/plans")
public class PlanRestController {

    private final PlanService planService;

    public PlanRestController(PlanService planService) {
        this.planService = planService;
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

    @GetMapping
    public List<Plan> getAllPlans(Authentication authentication) {
        String role = extractPrimaryRole(authentication);
        return planService.getPlansForUserRole(role);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plan> getPlanById(@PathVariable Long id, Authentication authentication) {
        Plan plan = planService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found with id " + id));

        String role = extractPrimaryRole(authentication);
        if (!planService.canUserViewPlan(plan, role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access Denied: Customers can only view PUBLISHED plans.");
        }

        return ResponseEntity.ok(plan);
    }

    @PostMapping
    public ResponseEntity<Plan> createPlan(@Valid @RequestBody Plan plan, Principal principal) {
        String username = principal != null ? principal.getName() : "analyst";
        Plan created = planService.createPlan(plan, username);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
