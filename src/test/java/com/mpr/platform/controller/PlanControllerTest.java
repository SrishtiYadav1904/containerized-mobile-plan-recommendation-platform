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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @WithMockUser(username = "analyst", roles = {"ANALYST"})
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
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @DisplayName("GET /plans/new as CUSTOMER returns HTTP 403 Forbidden")
    void testGetCreateFormAsCustomerReturnsForbidden() throws Exception {
        mockMvc.perform(get("/plans/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "analyst", roles = {"ANALYST"})
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
    @DisplayName("GET /api/plans returns HTTP 200 and JSON array")
    void testGetApiPlans() throws Exception {
        mockMvc.perform(get("/api/plans")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }
}
