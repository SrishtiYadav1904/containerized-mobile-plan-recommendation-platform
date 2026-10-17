package com.mpr.platform.controller;

import com.mpr.platform.model.Plan;
import com.mpr.platform.model.PlanStatus;
import com.mpr.platform.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/plans")
public class PlanWebController {

    private final PlanService planService;

    public PlanWebController(PlanService planService) {
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
    public String listPlans(@RequestParam(value = "q", required = false) String query,
                            @RequestParam(value = "operator", required = false) String operator,
                            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                            @RequestParam(value = "validity", required = false) Integer validity,
                            Model model,
                            Authentication authentication) {
        String role = extractPrimaryRole(authentication);
        List<Plan> plans = planService.searchAndFilterPlans(query, operator, maxPrice, validity, role);

        model.addAttribute("plans", plans);
        model.addAttribute("currentRole", role);
        model.addAttribute("username", authentication != null ? authentication.getName() : "Guest");
        model.addAttribute("query", query);
        model.addAttribute("selectedOperator", operator);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedValidity", validity);
        return "plans/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication) {
        if (!model.containsAttribute("plan")) {
            model.addAttribute("plan", new Plan());
        }
        model.addAttribute("currentRole", extractPrimaryRole(authentication));
        return "plans/create";
    }

    @PostMapping
    public String createPlan(@Valid @ModelAttribute("plan") Plan plan,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication,
                             Principal principal) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentRole", extractPrimaryRole(authentication));
            return "plans/create";
        }

        String username = principal != null ? principal.getName() : "analyst";
        Plan savedPlan = planService.createPlan(plan, username);

        redirectAttributes.addFlashAttribute("successMessage",
                "Plan '" + savedPlan.getName() + "' created successfully in DRAFT status.");
        return "redirect:/plans";
    }

    @GetMapping("/{id}")
    public String viewPlanDetail(@PathVariable Long id, Model model, Authentication authentication) {
        Plan plan = planService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found with id " + id));

        String role = extractPrimaryRole(authentication);
        if (!planService.canUserViewPlan(plan, role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access Denied: Customers can only view PUBLISHED plans. This plan is currently " + plan.getStatus() + ".");
        }

        model.addAttribute("plan", plan);
        model.addAttribute("currentRole", role);
        model.addAttribute("username", authentication != null ? authentication.getName() : "Guest");
        return "plans/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        Plan plan = planService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found with id " + id));

        model.addAttribute("plan", plan);
        model.addAttribute("currentRole", extractPrimaryRole(authentication));
        return "plans/edit";
    }

    @PostMapping("/{id}/edit")
    public String processEditPlan(@PathVariable Long id,
                                  @Valid @ModelAttribute("plan") Plan plan,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  Authentication authentication,
                                  Principal principal) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentRole", extractPrimaryRole(authentication));
            return "plans/edit";
        }

        String role = extractPrimaryRole(authentication);
        String username = principal != null ? principal.getName() : "analyst";

        try {
            Plan updated = planService.updatePlan(id, plan, username, role);
            redirectAttributes.addFlashAttribute("successMessage", "Plan '" + updated.getName() + "' updated successfully.");
            return "redirect:/plans/" + id;
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("currentRole", role);
            return "plans/edit";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam("targetStatus") PlanStatus targetStatus,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication,
                               Principal principal) {
        String role = extractPrimaryRole(authentication);
        String username = principal != null ? principal.getName() : "analyst";

        try {
            Plan transitioned = planService.transitionStatus(id, targetStatus, role, username);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Status successfully transitioned to " + transitioned.getStatus() + " for plan: " + transitioned.getName());
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/plans/" + id;
    }
}
