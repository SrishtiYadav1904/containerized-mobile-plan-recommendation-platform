package com.mpr.platform.ui;

import com.mpr.platform.ui.pages.*;
import org.openqa.selenium.WebDriver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end automated UI test suite executing critical business journeys
 * for the Containerized Mobile Plan Recommendation Platform using Selenium WebDriver.
 */
@TestMethodOrder(OrderAnnotation.class)
public class MobilePlanSeleniumTests extends BaseSeleniumTest {

    private static final String CUSTOMER_USER = "customer@example.com";
    private static final String ANALYST_USER = "analyst@example.com";
    private static final String ADMIN_USER = "admin@example.com";
    private static final String DEMO_PASSWORD = "password";

    @Test
    @Order(1)
    @DisplayName("Journey 1: Authentication & Role-Based Access Control (RBAC)")
    public void testAuthenticationAndRoleBasedAccess() {
        // 1. Customer Login
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(driver);
        assertTrue(loginPage.isPageLoaded(), "Login page should load successfully.");

        loginPage.login(CUSTOMER_USER, DEMO_PASSWORD);

        // 2. Verify redirect to catalogue and customer role badge
        PlansPage plansPage = new PlansPage(driver);
        assertTrue(plansPage.isPageLoaded(), "Customer should be redirected to catalogue after login.");
        assertEquals("CUSTOMER", plansPage.getCurrentRoleBadge(), "Role badge should display CUSTOMER.");
        assertFalse(plansPage.hasCreatePlanOption(), "Customer should not see '+ Create Plan' button.");
        assertFalse(plansPage.hasDashboardOption(), "Customer should not see 'Dashboard' navigation link.");

        // 3. Attempt direct restricted access as Customer
        navigateTo("/dashboard");
        assertFalse(driver.getPageSource().contains("Executive Operations Dashboard"),
                "Customer should not be able to view Executive Operations Dashboard content.");
        assertTrue(driver.getPageSource().contains("403") || driver.getPageSource().contains("Forbidden") || driver.getPageSource().contains("Access Denied") || !driver.getCurrentUrl().endsWith("/dashboard"),
                "Customer should encounter access restriction on /dashboard.");

        // 4. Return to catalogue, log out and log in as Administrator
        navigateTo("/plans");
        plansPage = new PlansPage(driver);
        assertTrue(plansPage.isPageLoaded(), "Catalogue should be loaded before logout.");
        plansPage.logout();
        navigateTo("/login");
        LoginPage adminLoginPage = new LoginPage(driver);
        assertTrue(adminLoginPage.isPageLoaded(), "Login page should be loaded for Admin login.");
        adminLoginPage.login(ADMIN_USER, DEMO_PASSWORD);

        // 5. Verify Administrator has access to Executive Dashboard
        navigateTo("/dashboard");
        DashboardPage dashboardPage = new DashboardPage(driver);
        assertTrue(dashboardPage.isDashboardLoaded(), "Administrator should successfully access /dashboard.");
        assertTrue(dashboardPage.getKpiCardsCount() >= 4, "Dashboard should display at least 4 metric KPI cards.");
    }

    @Test
    @Order(2)
    @DisplayName("Journey 2: Plan Catalogue Browsing, Keyword Search & Filtering")
    public void testCatalogueSearchAndFiltering() {
        navigateTo("/plans");
        PlansPage plansPage = new PlansPage(driver);
        assertTrue(plansPage.isPageLoaded(), "Catalogue page should load successfully.");

        int initialTotal = plansPage.getPlanCardsCount();
        assertTrue(initialTotal >= 10, "Catalogue should contain seeded plans (expected >= 10, found " + initialTotal + ").");

        // 1. Keyword search for '5G' (matches plan name or benefits)
        plansPage.search("5G");
        assertTrue(driver.getCurrentUrl().contains("q=5G"), "URL should contain search query parameter 'q=5G'.");
        List<String> searchResults = plansPage.getAllPlanNames();
        assertFalse(searchResults.isEmpty(), "Search for '5G' should return matching plans.");
        assertTrue(searchResults.stream().anyMatch(name -> name.toLowerCase().contains("5g")),
                "At least one plan matching 5G search should contain '5G' in the title.");
        for (String name : searchResults) {
            assertTrue(driver.getPageSource().contains(name), "Filtered plan '" + name + "' should be rendered on page.");
        }

        // 2. Filter by Operator 'Airtel'
        navigateTo("/plans");
        plansPage.filterByOperator("Airtel");
        assertTrue(driver.getCurrentUrl().contains("operator=Airtel"), "URL should contain operator parameter.");
        List<String> operatorResults = plansPage.getAllPlanOperators();
        assertFalse(operatorResults.isEmpty(), "Operator filter for Airtel should return plans.");
        for (String op : operatorResults) {
            assertTrue(op.trim().equalsIgnoreCase("Airtel"), "Filtered plan operator should be Airtel (found: " + op + ").");
        }

        // 3. Filter by Max Budget ₹300
        navigateTo("/plans");
        plansPage.filterByMaxPrice(300);
        assertTrue(driver.getCurrentUrl().contains("maxPrice=300"), "URL should contain maxPrice parameter.");
        assertTrue(plansPage.getPlanCardsCount() > 0, "Budget filter should return plans priced <= 300.");
    }

    @Test
    @Order(3)
    @DisplayName("Journey 3: Analyst Creates New Plan with Validation and DRAFT Status")
    public void testAnalystCreatesPlanDraft() {
        // 1. Log in as Analyst
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(ANALYST_USER, DEMO_PASSWORD);
        PlansPage plansPage = new PlansPage(driver);
        assertTrue(plansPage.isPageLoaded(), "Should land on catalogue after Analyst login.");

        // 2. Navigate to Plan Creation form
        navigateTo("/plans/new");
        PlanCreatePage createPage = new PlanCreatePage(driver);
        assertTrue(createPage.isPageLoaded(), "Create plan draft form should be loaded.");

        // 3. Submit valid new plan draft
        String uniquePlanName = "Jio Selenium E2E " + System.currentTimeMillis() % 10000;
        createPage.fillAndSubmitPlan(
                "Jio",
                uniquePlanName,
                349.00,
                30,
                2.0,
                -1,
                100,
                "Unlimited 5G Data, Cloud Sync, Selenium Automation Tested"
        );

        // 4. Verify redirected to catalogue and new plan is present
        assertTrue(plansPage.isPageLoaded(), "Should redirect to catalogue after plan creation.");
        assertTrue(driver.getPageSource().contains(uniquePlanName),
                "Newly created plan '" + uniquePlanName + "' should appear in catalogue.");
        assertTrue(driver.getPageSource().contains("DRAFT"),
                "Newly created plan should have DRAFT status badge.");
    }

    @Test
    @Order(4)
    @DisplayName("Journey 4: Customer Usage & Budget Recommendation Flow")
    public void testCustomerRecommendationEngineFlow() {
        navigateTo("/recommendations");
        RecommendationsPage recPage = new RecommendationsPage(driver);
        assertTrue(recPage.isFormLoaded(), "Recommendation finder form should be displayed.");

        // Submit monthly usage preferences
        recPage.submitRecommendationRequest(45.0, 500, 500.0, "5G, Disney+ Hotstar");

        // Verify recommendation results
        assertTrue(recPage.isResultsHeaderDisplayed(), "Top Recommended Tariffs results header should be displayed.");
        List<String> topPlans = recPage.getRecommendedPlanNames();
        assertFalse(topPlans.isEmpty(), "Recommendation engine should produce at least one ranked tariff.");
        System.out.println("[SELENIUM] Top recommended plan returned: " + topPlans.get(0));
    }

    @Test
    @Order(5)
    @DisplayName("Journey 5: Administrator Plan Status Approval Workflow")
    public void testAdministratorStatusApprovalWorkflow() {
        // 1. Log in as Administrator
        navigateTo("/login");
        new LoginPage(driver).login(ADMIN_USER, DEMO_PASSWORD);

        // 2. Create a DRAFT plan directly as admin to ensure a clean plan to transition
        navigateTo("/plans/new");
        String transitionPlanName = "Vi Lifecycle Workflow " + System.currentTimeMillis() % 10000;
        new PlanCreatePage(driver).fillAndSubmitPlan(
                "Vodafone Idea",
                transitionPlanName,
                299.00,
                28,
                1.5,
                -1,
                100,
                "Binge All Night, Lifecycle Workflow Test"
        );

        // 3. Find the created plan link in catalogue
        PlansPage plansPage = new PlansPage(driver);
        assertTrue(driver.getPageSource().contains(transitionPlanName));

        // Click into the detail page of the created plan
        plansPage.openPlanDetails(transitionPlanName);

        PlanDetailPage detailPage = new PlanDetailPage(driver);
        assertTrue(detailPage.isDetailLoaded(), "Plan detail page should load.");
        assertEquals("DRAFT", detailPage.getStatus(), "Initial status should be DRAFT.");

        // 4. Transition: DRAFT -> IN_REVIEW
        assertTrue(detailPage.canSubmitForReview(), "Submit for Review button should be available.");
        detailPage.submitForReview();

        // 5. Verify status updated to IN_REVIEW
        assertTrue(driver.getPageSource().contains("IN_REVIEW"), "Status should update to IN_REVIEW.");

        // 6. Transition: IN_REVIEW -> PUBLISHED
        assertTrue(detailPage.canApproveAndPublish(), "Approve & Publish button should be available for Admin.");
        detailPage.approveAndPublish();

        // 7. Verify status updated to PUBLISHED
        assertTrue(driver.getPageSource().contains("PUBLISHED"), "Status should update to PUBLISHED.");

        // 8. Verify public customer visibility: Return to catalogue, log out and check catalogue as Guest
        navigateTo("/plans");
        plansPage.logout();
        navigateTo("/plans");
        assertTrue(driver.getPageSource().contains(transitionPlanName),
                "Newly published plan should now be visible to unauthenticated Customers on the public catalogue.");
    }
}
