# Selenium Automated Test Plan — Containerized Mobile Plan Recommendation Platform

## 1. Overview and Objectives

This test plan defines the end-to-end (E2E) automated browser testing strategy for the **Containerized Mobile Plan Recommendation Platform** using **Selenium WebDriver**, **JUnit 5**, and **Maven**.

### Objectives:
- Validate critical user journeys from the perspective of real browser clients.
- Verify security boundaries: Authentication, authorization, and Role-Based Access Control (RBAC).
- Confirm core business capabilities: Tariff catalogue search/filtering, plan lifecycle authoring, and transparent 4-factor recommendation calculation.
- Guarantee CI/CD pipeline compatibility with headless Chrome execution and automatic screenshot failure diagnostics.

---

## 2. Test Environment Matrix & Tooling

| Component | Specification |
|---|---|
| **Automation Framework** | Selenium WebDriver (`selenium-java:4.20.0`) |
| **Driver Management** | WebDriverManager (`webdrivermanager:5.8.0`) + Selenium Manager |
| **Test Runner** | JUnit 5 (`junit-jupiter`) via Maven Surefire |
| **Browser Support** | Google Chrome (Headless mode for CI, Headed for local debugging) |
| **Target Endpoints** | Configurable via `-Dtest.base.url` (Default: `http://localhost:8084/mpr-platform` or embedded free port) |
| **Port Collision Policy** | Avoid ports `8072`, `8081`, `8082`. Use port `8084` or embedded random port. |
| **Failure Diagnostics** | Automatic screenshot capture and page source dump in `target/screenshots/` |

---

## 3. Architecture & Design Principles

1. **Page Object Model (POM):**
   - Web pages are represented as clean Java classes encapsulating DOM selectors and user actions.
   - Decouples test assertions from UI layout and CSS selectors.
2. **Explicit Synchronization (No Brittle Thread.sleep):**
   - Uses `WebDriverWait` with `ExpectedConditions` (e.g., `visibilityOfElementLocated`, `elementToBeClickable`, `urlContains`).
   - Resilient against network delays and asynchronous DOM rendering.
3. **Failure Diagnostics via TestWatcher:**
   - On test failure, JUnit 5 extension captures an immediate browser screenshot (`.png`) and DOM HTML (`.html`) to `target/screenshots/`.
4. **Deterministic Test Data:**
   - Tests execute against known seeded data (`customer@example.com`, `analyst@example.com`, `admin@example.com`) without external network dependencies.

---

## 4. Test Journey Catalogue

### Journey 1: Authentication & Role-Based Access Control (RBAC)
- **User Personas:** `Customer`, `Administrator`
- **Preconditions:** System running with default seed accounts.
- **Workflow Steps:**
  1. Navigate to `/login`.
  2. Input Customer credentials (`customer@example.com` / `password`).
  3. Submit login -> Assert redirect to `/plans` with `CUSTOMER` role badge.
  4. Attempt navigation to restricted administrative routes (`/plans/new` and `/dashboard`).
  5. Assert access denied (HTTP 403 / redirect).
  6. Log out.
  7. Input Administrator credentials (`admin@example.com` / `password`).
  8. Submit login -> Navigate to `/dashboard`.
  9. Assert Executive Dashboard loads with metric KPI cards.

### Journey 2: Catalogue Browsing, Keyword Search & Filtering
- **User Persona:** `Guest / Customer`
- **Preconditions:** Catalogue loaded with published plans.
- **Workflow Steps:**
  1. Navigate to `/plans`.
  2. Assert catalogue title and plan cards are visible.
  3. Enter `"5G"` in search input (`#searchQ`) and click Filter.
  4. Assert all returned plan cards contain `"5G"`.
  5. Select operator `"Airtel"` from carrier dropdown (`#operatorSelect`) and click Filter.
  6. Assert all returned plans belong to Airtel.
  7. Enter max price `"300"` in `#maxPriceInput` and click Filter.
  8. Assert all returned plans have price $\le$ ₹300.00.

### Journey 3: Analyst Creates New Tariff Draft with Validation
- **User Persona:** `Analyst`
- **Preconditions:** Logged in as `analyst@example.com`.
- **Workflow Steps:**
  1. Navigate to `/plans/new`.
  2. Assert Create Plan Draft form is displayed.
  3. Fill plan details:
     - Operator: `Jio`
     - Plan Name: `Jio Automated Selenium 5G 30D`
     - Price: `349.00`
     - Validity: `30`
     - Data: `2.0` GB/day
     - Voice: `-1` (Unlimited)
     - SMS: `100`
     - Perks: `Unlimited 5G Data, JioHotstar, Cloud Backup`
  4. Submit form -> Assert redirect to `/plans`.
  5. Assert newly created plan appears in catalogue with **`DRAFT`** status badge.

### Journey 4: Customer Usage & Budget Recommendation Engine
- **User Persona:** `Customer`
- **Preconditions:** Published plans available in database.
- **Workflow Steps:**
  1. Navigate to `/recommendations`.
  2. Fill recommendation form:
     - Monthly Data: `50` GB
     - Monthly Minutes: `1000` mins
     - Maximum Budget: `500` INR
     - Perks Preferences: `5G, Hotstar`
  3. Click "Compute Best Matching Plans".
  4. Assert page lands on `/recommendations` results view.
  5. Assert heading `"Top Recommended Tariffs"` is displayed.
  6. Assert recommended plan cards display computed score badges and operator details.

### Journey 5: Administrator Plan Status Lifecycle Approval
- **User Persona:** `Administrator`
- **Preconditions:** Plan in `DRAFT` status exists.
- **Workflow Steps:**
  1. Log in as `admin@example.com`.
  2. Navigate to detail view of the newly created DRAFT plan (`/plans/{id}`).
  3. Verify current status badge is `DRAFT`.
  4. Click "Submit for Review (IN_REVIEW)" button -> Assert status transitions to `IN_REVIEW`.
  5. Click "Approve & Publish (PUBLISHED)" button -> Assert status transitions to `PUBLISHED`.
  6. Verify plan is now visible to Customers on the public catalogue.

---

## 5. Execution Commands

```powershell
# Run Selenium tests against default local Tomcat deployment (port 8084)
mvn test -Dtest=MobilePlanSeleniumTests

# Run Selenium tests with explicit custom base URL
mvn test -Dtest=MobilePlanSeleniumTests -Dtest.base.url=http://localhost:8084/mpr-platform

# Run in headed mode for visual local inspection
mvn test -Dtest=MobilePlanSeleniumTests -Dheadless=false
```

---

## 6. Failure Artifacts & Diagnostic Policy

When any Selenium test fails:
1. `BaseSeleniumTest` automatically captures a full-page screenshot (`.png`) named `<TestClass>_<TestMethod>_<Timestamp>.png` under `target/screenshots/`.
2. The current page HTML source (`.html`) is exported alongside the screenshot to preserve DOM state for debugging.
3. The Jenkins CI pipeline archives `target/screenshots/*.png` in post-build actions.
