# Task 9 Report — Selenium Test Automation and End-to-End User Journey Verification

## Task report

- **Task:** Task 9 — Selenium Test Design and Local Execution
- **Status:** Completed (Fully Verified with Headless Chrome 155 Execution & 5 Passing End-to-End User Journeys)
- **Date:** 2026-10-18
- **Goal:** Design and execute robust automated browser tests using Selenium WebDriver (Java, JUnit 5, Maven Surefire) following the Page Object Model (POM) pattern against the deployed Containerized Mobile Plan Recommendation Platform on Apache Tomcat 10.1 (port 8084); cover 5 critical business journeys (Authentication & RBAC, Catalogue search and filtering, Analyst plan creation with DRAFT status badge, Customer usage/budget recommendation calculation, and Administrator status approval lifecycle `DRAFT -> IN_REVIEW -> PUBLISHED`); enforce explicit waits, headless Chrome compatibility, configurable base URL, and automated diagnostic failure artifacts (PNG screenshot and HTML page source dump); and document the strategy in `docs/selenium-test-plan.md`.

- **What was implemented:**
  1. **Selenium Test Plan (`docs/selenium-test-plan.md`):**
     - Detailed test plan specifying objectives, scope, browser/environment matrix, base URL configuration (`APP_BASE_URL` or `-Dtest.base.url`, defaulting to `http://localhost:8084/mpr-platform`), Page Object Model design architecture, explicit wait strategy, failure artifact diagnostics policy, and CI/Jenkins headless readiness.
  2. **Dependencies in `pom.xml`:**
     - Added `selenium-java` (version `4.20.0`) and `webdrivermanager` (version `5.8.0`) under `<scope>test</scope>` in `pom.xml`.
  3. **Base Test Harness (`src/test/java/com/mpr/platform/ui/BaseSeleniumTest.java`):**
     - Headless Chrome configuration (`--headless=new`, `--no-sandbox`, `--disable-dev-shm-usage`, `--window-size=1920,1080`).
     - Automated ChromeDriver setup via `WebDriverManager.chromedriver().setup()`.
     - Explicit wait helpers (`waitForVisible`, `waitForClickable`, `waitForUrlContains`, `safeClick`, `safeType`).
     - Automated failure diagnostics using JUnit 5 `AfterTestExecutionCallback`: on test failure, automatically captures full-page PNG screenshot and serializes current DOM HTML to `target/screenshots/{TestClass}_{TestMethod}_{Timestamp}.png/.html`.
  4. **Page Object Models (`src/test/java/com/mpr/platform/ui/pages/`):**
     - `LoginPage.java`: Encapsulates sign-in inputs, JavaScript submission, and redirection verification.
     - `PlansPage.java`: Encapsulates catalogue card interactions, role badge inspection, keyword search, carrier filter, budget filter, `openPlanDetails`, and logout form submission.
     - `PlanCreatePage.java`: Encapsulates tariff draft creation inputs, carrier dropdown selection, robust JavaScript form submission, and alert-success redirection wait.
     - `RecommendationsPage.java`: Encapsulates customer monthly usage preferences input (data, voice, budget, perks), calculation submission, and 4-factor scoring card validation.
     - `PlanDetailPage.java`: Encapsulates plan specifications view, status badge polling, `submitForReview` action, and `approveAndPublish` action.
     - `DashboardPage.java`: Encapsulates executive analytics KPI card count verification.
  5. **Automated User Journey Suite (`src/test/java/com/mpr/platform/ui/MobilePlanSeleniumTests.java`):**
     - **Journey 1 (`@Order(1)`): Authentication & Role-Based Access Control (RBAC)**
       - Logs in as Customer (`customer@example.com`), verifies catalogue landing with `CUSTOMER` badge, verifies absence of "+ Create Plan" and "Dashboard" links.
       - Attempts direct navigation to restricted `/dashboard`; verifies access restriction (403 Forbidden / Access Denied).
       - Logs out, signs in as Administrator (`admin@example.com`), navigates to `/dashboard`, verifies 4+ metric KPI cards are loaded.
     - **Journey 2 (`@Order(2)`): Plan Catalogue Browsing, Multi-Criteria Search & Filtering**
       - Validates seeded catalogue volume (>= 10 plans).
       - Executes keyword search for `5G`, validates query parameter `?q=5G`, confirms matching plans rendered.
       - Filters by carrier `Airtel`, validates query parameter `?operator=Airtel`, confirms all returned cards belong to Airtel.
       - Filters by max price `₹300`, validates query parameter `?maxPrice=300`, confirms price constraints.
     - **Journey 3 (`@Order(3)`): Analyst Plan Draft Creation & Form Validation**
       - Logs in as Analyst (`analyst@example.com`), navigates to `/plans/new`.
       - Submits new tariff draft ("Jio Selenium E2E ...", ₹349, 30 days, 2.0 GB/day, unlimited voice, 100 SMS).
       - Confirms redirect to catalogue, appearance of success alert, and rendered card with `DRAFT` status badge.
     - **Journey 4 (`@Order(4)`): Customer Usage & Budget Recommendation Calculation**
       - Navigates to `/recommendations`, submits customer profile (45 GB/mo, 500 mins, ₹500 budget, "5G, Disney+ Hotstar").
       - Confirms recommendation results table rendered with 4-factor scoring breakdown and top plan card.
     - **Journey 5 (`@Order(5)`): Administrator Status Approval Lifecycle & Public Visibility**
       - Logs in as Administrator (`admin@example.com`), creates a plan draft.
       - Opens plan details, transitions status `DRAFT -> IN_REVIEW` via "Submit for Review".
       - Approves plan `IN_REVIEW -> PUBLISHED` via "Approve & Publish".
       - Logs out, checks catalogue as unauthenticated Guest, verifies newly published tariff is publicly visible.

- **Files created or changed:**
  - `pom.xml` (added `selenium-java` and `webdrivermanager` test dependencies)
  - `docs/selenium-test-plan.md` (comprehensive automated test design document)
  - `src/test/java/com/mpr/platform/ui/BaseSeleniumTest.java` (test harness and failure screenshot extension)
  - `src/test/java/com/mpr/platform/ui/pages/LoginPage.java` (Page Object for `/login`)
  - `src/test/java/com/mpr/platform/ui/pages/PlansPage.java` (Page Object for `/plans`)
  - `src/test/java/com/mpr/platform/ui/pages/PlanCreatePage.java` (Page Object for `/plans/new`)
  - `src/test/java/com/mpr/platform/ui/pages/PlanDetailPage.java` (Page Object for `/plans/{id}`)
  - `src/test/java/com/mpr/platform/ui/pages/RecommendationsPage.java` (Page Object for `/recommendations`)
  - `src/test/java/com/mpr/platform/ui/pages/DashboardPage.java` (Page Object for `/dashboard`)
  - `src/test/java/com/mpr/platform/ui/MobilePlanSeleniumTests.java` (5 End-to-End Selenium User Journeys)
  - `docs/task-reports/task-09.md` (this report)

- **Commands executed and actual outcomes:**
  1. `git checkout -b feature/9-selenium-test-automation develop` -> Created feature branch from `develop`.
  2. `.\mvnw.cmd test-compile` -> Verified POM dependencies and compiled test classes cleanly.
  3. `.\mvnw.cmd test -Dtest=MobilePlanSeleniumTests` -> Executed automated UI suite against live Tomcat 10.1 (`http://localhost:8084/mpr-platform`).
     - **Result:** `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 28.99 s` -> `BUILD SUCCESS`.
  4. `.\mvnw.cmd test` -> Executed full test suite (30 unit/slice tests + 5 Selenium journeys).
     - **Result:** `Tests run: 35, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 54.296 s` -> `BUILD SUCCESS`.

- **Issues encountered and resolutions:**
  1. **JUnit 5 Lifecycle for Failure Screenshot Diagnostics:**
     - *Issue:* Initially used `TestWatcher.testFailed()` to capture screenshots, but in JUnit 5 `@AfterEach` runs before `TestWatcher`, causing `driver.quit()` to terminate the session before the screenshot could be taken.
     - *Resolution:* Implemented `AfterTestExecutionCallback`, which executes immediately after the test method and prior to `@AfterEach`, reliably writing PNG screenshots and HTML DOM dumps to `target/screenshots/`.
  2. **Headless Chrome Form Submissions & Viewport Offsets:**
     - *Issue:* In Chrome headless mode, standard `element.click()` occasionally failed to trigger HTML form submissions on `/plans/new` and `/login` due to off-screen button positioning and form validation state.
     - *Resolution:* Added `setInputValue` helper with explicit event dispatching (`input`, `change`) and utilized JavaScript `arguments[0].click()` with explicit wait polling for redirect alerts and URL transitions.
  3. **Case Sensitivity in CSS Transformed Operators:**
     - *Issue:* Journey 2 asserted `Airtel` against carrier tag text, which was rendered with CSS `text-transform: uppercase`, returning `AIRTEL`.
     - *Resolution:* Updated assertion to `assertTrue(op.trim().equalsIgnoreCase("Airtel"))`.

- **Next step:** Update `TASK-TRACKER.md` and `PROJECT-PROGRESS.md`, commit and push changes, open PR #6 to merge into `develop`, fast-forward `main`, and prepare for Task 10 (Docker containerization & multi-stage Dockerfile).
