package com.mpr.platform.controller;

import com.mpr.platform.service.PlanService;
import com.mpr.platform.service.RecommendationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardWebController {

    private final PlanService planService;
    private final RecommendationService recommendationService;

    public DashboardWebController(PlanService planService, RecommendationService recommendationService) {
        this.planService = planService;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public String showDashboard(Model model, Authentication authentication) {
        model.addAttribute("statusCounts", planService.getPlanCountsByStatus());
        model.addAttribute("operatorCounts", planService.getPlanCountsByOperator());
        model.addAttribute("avgPrice", planService.getAveragePlanPrice());
        model.addAttribute("avgPriceByOperator", planService.getAveragePriceByOperator());
        model.addAttribute("totalRecommendations", recommendationService.getTotalRecommendationsComputed());
        model.addAttribute("totalPlans", planService.findAllPlans().size());
        model.addAttribute("username", authentication != null ? authentication.getName() : "Analyst");
        return "dashboard/summary";
    }
}
