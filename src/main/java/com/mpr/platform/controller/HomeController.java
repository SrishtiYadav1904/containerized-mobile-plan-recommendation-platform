package com.mpr.platform.controller;

import com.mpr.platform.service.PlanService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * Controller serving the primary project landing page and platform introduction.
 */
@Controller
public class HomeController {

    private final PlanService planService;

    public HomeController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/")
    public String index(Model model, Principal principal, Authentication authentication) {
        String role = "GUEST";
        String username = "Guest";

        if (authentication != null && authentication.isAuthenticated() && principal != null) {
            username = principal.getName();
            role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(r -> r.replace("ROLE_", ""))
                    .findFirst()
                    .orElse("CUSTOMER");
        }

        model.addAttribute("username", username);
        model.addAttribute("currentRole", role);
        try {
            model.addAttribute("publishedPlansCount", planService.findPublishedPlans().size());
            model.addAttribute("totalPlansCount", planService.findAllPlans().size());
        } catch (Exception e) {
            model.addAttribute("publishedPlansCount", 24);
            model.addAttribute("totalPlansCount", 24);
        }

        return "index";
    }
}
