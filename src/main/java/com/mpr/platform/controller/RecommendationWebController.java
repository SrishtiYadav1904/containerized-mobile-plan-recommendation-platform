package com.mpr.platform.controller;

import com.mpr.platform.model.RecommendationRequest;
import com.mpr.platform.model.RecommendationResult;
import com.mpr.platform.model.User;
import com.mpr.platform.service.RecommendationService;
import com.mpr.platform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/recommendations")
public class RecommendationWebController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    public RecommendationWebController(RecommendationService recommendationService, UserService userService) {
        this.recommendationService = recommendationService;
        this.userService = userService;
    }

    @GetMapping
    public String showRecommendationForm(Model model, Authentication authentication) {
        if (!model.containsAttribute("request")) {
            RecommendationRequest req = new RecommendationRequest();
            req.setMonthlyDataGb(30.0);
            req.setMonthlyMinutes(500);
            req.setBudget(500.0);
            req.setPreferences("5G, Unlimited, OTT");
            model.addAttribute("request", req);
        }
        model.addAttribute("username", authentication != null ? authentication.getName() : "Guest");
        return "recommendations/form";
    }

    @PostMapping
    public String processRecommendation(@Valid @ModelAttribute("request") RecommendationRequest request,
                                        BindingResult bindingResult,
                                        Model model,
                                        Authentication authentication) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("username", authentication != null ? authentication.getName() : "Guest");
            return "recommendations/form";
        }

        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            userService.findByEmail(authentication.getName()).ifPresent(u -> request.setUserId(u.getId()));
        }

        List<RecommendationResult> results = recommendationService.generateRecommendations(request);

        model.addAttribute("request", request);
        model.addAttribute("results", results);
        model.addAttribute("username", authentication != null ? authentication.getName() : "Guest");
        return "recommendations/results";
    }
}
