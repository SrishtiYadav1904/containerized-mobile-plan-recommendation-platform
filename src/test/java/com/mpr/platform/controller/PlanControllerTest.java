package com.mpr.platform.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /plans returns HTTP 200 and loads plans/list view for public/customer")
    void testGetPlansListPublic() throws Exception {
        mockMvc.perform(get("/plans"))
                .andExpect(status().isOk())
                .andExpect(view().name("plans/list"))
                .andExpect(model().attributeExists("plans"))
                .andExpect(model().attribute("currentRole", "CUSTOMER"));
    }

    @Test
    @DisplayName("GET /plans with search filter returns filtered list")
    void testSearchPlansByOperator() throws Exception {
        mockMvc.perform(get("/plans").param("operator", "Jio"))
                .andExpect(status().isOk())
                .andExpect(view().name("plans/list"))
                .andExpect(model().attributeExists("plans"));
    }

    @Test
    @WithMockUser(username = "analyst@example.com", roles = {"ANALYST"})
    @DisplayName("GET /plans/new as ANALYST returns HTTP 200 and loads create form")
    void testGetCreateFormAsAnalyst() throws Exception {
        mockMvc.perform(get("/plans/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("plans/create"))
                .andExpect(model().attributeExists("plan"));
    }

    @Test
    @DisplayName("GET /plans/new unauthenticated redirects to login")
    void testGetCreateFormUnauthenticatedRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/plans/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    @DisplayName("GET /plans/new as CUSTOMER returns HTTP 403 Forbidden")
    void testGetCreateFormAsCustomerReturnsForbidden() throws Exception {
        mockMvc.perform(get("/plans/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "analyst@example.com", roles = {"ANALYST"})
    @DisplayName("POST /plans as ANALYST with valid fields creates plan and redirects to /plans")
    void testCreatePlanAsAnalyst() throws Exception {
        mockMvc.perform(post("/plans")
                        .with(csrf())
                        .param("operator", "Jio")
                        .param("name", "Jio Freedom 28D")
                        .param("price", "299.0")
                        .param("validityDays", "28")
                        .param("dataGbPerDay", "1.5")
                        .param("voiceMinutes", "-1")
                        .param("smsCount", "100")
                        .param("benefits", "Unlimited 5G Data"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/plans"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("GET /recommendations displays the recommendation input form")
    void testGetRecommendationsForm() throws Exception {
        mockMvc.perform(get("/recommendations"))
                .andExpect(status().isOk())
                .andExpect(view().name("recommendations/form"))
                .andExpect(model().attributeExists("request"));
    }

    @Test
    @DisplayName("POST /recommendations computes ranked plans and renders results view")
    void testProcessRecommendations() throws Exception {
        mockMvc.perform(post("/recommendations")
                        .with(csrf())
                        .param("monthlyDataGb", "30.0")
                        .param("monthlyMinutes", "300")
                        .param("budget", "500.0")
                        .param("preferences", "5G, Disney+ Hotstar"))
                .andExpect(status().isOk())
                .andExpect(view().name("recommendations/results"))
                .andExpect(model().attributeExists("results"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMINISTRATOR"})
    @DisplayName("GET /dashboard as ADMINISTRATOR returns HTTP 200 and summary metrics")
    void testGetDashboardAsAdmin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/summary"))
                .andExpect(model().attributeExists("statusCounts", "operatorCounts", "avgPrice"));
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    @DisplayName("GET /dashboard as CUSTOMER returns HTTP 403 Forbidden")
    void testGetDashboardAsCustomerForbidden() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/plans returns HTTP 200 and JSON array")
    void testGetApiPlans() throws Exception {
        mockMvc.perform(get("/api/plans")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMINISTRATOR"})
    @DisplayName("GET /api/dashboard/summary as ADMINISTRATOR returns HTTP 200 and JSON metrics")
    void testGetApiDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCounts", notNullValue()))
                .andExpect(jsonPath("$.operatorCounts", notNullValue()));
    }
}
