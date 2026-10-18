package com.mpr.platform.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration and unit tests validating the landing page controller and view rendering.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / returns HTTP 200, renders index template with public guest state")
    void testLandingPagePublicGuest() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("username", "Guest"))
                .andExpect(model().attribute("currentRole", "GUEST"))
                .andExpect(model().attributeExists("publishedPlansCount"))
                .andExpect(model().attributeExists("totalPlansCount"))
                .andExpect(content().string(containsString("Find a Mobile Plan")))
                .andExpect(content().string(containsString("The End-to-End DevOps Pipeline")))
                .andExpect(content().string(containsString("Verified Platform Capabilities")))
                .andExpect(content().string(containsString("Application Runtime Architecture")))
                .andExpect(content().string(containsString("Verified Reliability Practices")))
                .andExpect(content().string(containsString("The Verified Technology Stack")));
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    @DisplayName("GET / with CUSTOMER authentication reflects customer role in navigation")
    void testLandingPageAuthenticatedCustomer() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("username", "customer@example.com"))
                .andExpect(model().attribute("currentRole", "CUSTOMER"))
                .andExpect(content().string(containsString("customer@example.com")))
                .andExpect(content().string(containsString("CUSTOMER")));
    }

    @Test
    @WithMockUser(username = "analyst@example.com", roles = {"ANALYST"})
    @DisplayName("GET / with ANALYST authentication exposes dashboard action in navigation")
    void testLandingPageAuthenticatedAnalyst() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("username", "analyst@example.com"))
                .andExpect(model().attribute("currentRole", "ANALYST"))
                .andExpect(content().string(containsString("Dashboard")));
    }

    @Test
    @DisplayName("GET / contains valid navigation links to catalog, recommendations, and login")
    void testLandingPageLinks() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/plans\"")))
                .andExpect(content().string(containsString("href=\"/recommendations\"")))
                .andExpect(content().string(containsString("href=\"/login\"")));
    }

    @Test
    @DisplayName("GET / verifies all 10 DevOps pipeline stages and tools are accurately rendered")
    void testLandingPageDevOpsPipelineStages() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Stage 1")))
                .andExpect(content().string(containsString("Source Control & GitFlow")))
                .andExpect(content().string(containsString("Stage 2")))
                .andExpect(content().string(containsString("Pipeline as Code")))
                .andExpect(content().string(containsString("Stage 3")))
                .andExpect(content().string(containsString("Build & Unit Tests")))
                .andExpect(content().string(containsString("Stage 4")))
                .andExpect(content().string(containsString("UI Testing Quality Gate")))
                .andExpect(content().string(containsString("Stage 5")))
                .andExpect(content().string(containsString("Artifact Packaging")))
                .andExpect(content().string(containsString("Stage 6")))
                .andExpect(content().string(containsString("Standalone Server Deploy")))
                .andExpect(content().string(containsString("Stage 7")))
                .andExpect(content().string(containsString("Container Image Packaging")))
                .andExpect(content().string(containsString("Stage 8")))
                .andExpect(content().string(containsString("Container Deployment")))
                .andExpect(content().string(containsString("Stage 9")))
                .andExpect(content().string(containsString("Configuration Management")))
                .andExpect(content().string(containsString("Stage 10")))
                .andExpect(content().string(containsString("Reliability & Self-Healing")));
    }
}
