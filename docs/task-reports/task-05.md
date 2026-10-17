# Task 5 — Feature Development with Branching

## Task report

- **Task:** Task 5 — Feature Development with Branching
- **Status:** Completed
- **Date:** 2026-10-18
- **Goal:** Implement the foundational mobile plan catalogue workflow on a dedicated feature branch (`feature/5-create-view-plans`) branched from `develop`. Deliver the Plan JPA entity with 8 approved attributes, status constraints (`DRAFT`), repository, service, validation, Web/REST controllers, and responsive Thymeleaf light-theme views with soft pink/lilac accents. Enforce that Analysts/Admins can create plans while Customers see only `PUBLISHED` plans. Verify all automated unit and integration tests and raise a Pull Request to `develop`.

- **What was implemented:**
  1. **Branching Workflow:**
     - Created and switched to feature branch `feature/5-create-view-plans` branched from `develop`.
  2. **Domain Model & Persistence:**
     - Created `PlanStatus.java` enum defining `DRAFT`, `IN_REVIEW`, `PUBLISHED`, `ARCHIVED`.
     - Created `Plan.java` JPA entity mapping to table `plans` with the 8 mandatory fields: `operator`, `name`, `price`, `validityDays`, `dataGbPerDay`, `voiceMinutes`, `smsCount`, `benefits`, plus `status`, `createdBy`, `updatedBy`, `createdAt`, `updatedAt`. Added validation constraints (`@NotBlank`, `@DecimalMin`, `@Min`, `@Size`).
     - Created `PlanRepository.java` extending `JpaRepository` with custom finder queries (`findByStatus`, `findByOperatorIgnoreCase`, `findByPriceLessThanEqual`).
  3. **Business Logic & Service Layer:**
     - Created `PlanService.java`:
       - `createPlan(Plan plan, String createdBy)`: Enforces that new plans strictly receive initial status `DRAFT`. Sets audit attributes (`createdBy`, `updatedBy`, timestamps). Validates positive price and validity.
       - `getPlansForUserRole(String role)`: Core role gate. Returns only `PUBLISHED` plans for `CUSTOMER` role; returns all plans (`DRAFT`, `IN_REVIEW`, `PUBLISHED`, `ARCHIVED`) for `ANALYST` and `ADMINISTRATOR`.
       - `canUserViewPlan(Plan plan, String role)`: Access check for individual plan detail viewing.
       - `seedInitialPlansIfEmpty()`: Automatic seed data on startup with realistic tariffs across carriers (Jio, Airtel, Vi, BSNL) and lifecycle statuses.
  4. **Security & Role-Based Authorization:**
     - Updated `SecurityConfig.java`:
       - Configured test users for `CUSTOMER`, `ANALYST`, and `ADMINISTRATOR` with BCrypt password encoding.
       - Role protection: `/plans/new` and `POST /plans` strictly restricted to `hasAnyRole('ANALYST', 'ADMINISTRATOR')`.
       - Public/Customer access allowed for browsing `/plans` and viewing published plan details.
       - CSRF configured for HTML forms; frame options disabled for embedded consoles.
  5. **Web Controllers & REST APIs:**
     - Created `PlanWebController.java`:
       - `GET /plans`: Catalogue list view tailored to authenticated user role.
       - `GET /plans/new`: Form view for creating new plan drafts.
       - `POST /plans`: Validated submission handler with flash alerts and redirect.
       - `GET /plans/{id}`: Detailed plan view with role-based forbidden checks.
     - Created `PlanRestController.java`:
       - `GET /api/plans`, `GET /api/plans/{id}`, and `POST /api/plans`.
  6. **UI & Responsive Aesthetics:**
     - Updated `src/main/resources/static/css/styles.css` with soft pink (`#E86A92`) and lilac (`#8E7CC3`) light-theme tokens, plan cards, status badges, and styled form controls.
     - Created `src/main/resources/templates/plans/list.html`: Responsive grid of mobile tariff cards with badges, operator labels, and role-aware action buttons.
     - Created `src/main/resources/templates/plans/create.html`: Structured form for Analysts to input plan attributes with error validation markers.
     - Created `src/main/resources/templates/plans/detail.html`: Clean specifications table and audit info.
     - Updated `src/main/resources/templates/index.html` with links to browse and create plans.
  7. **Comprehensive Automated Tests:**
     - `PlanServiceTest.java`: 5 unit tests verifying DRAFT status enforcement, price validation, customer PUBLISHED filtering, and detail permission checks.
     - `PlanControllerTest.java`: 6 MockMvc integration tests verifying web routing, security role gates, form creation, and REST endpoints.

- **Files created or changed:**
  - `src/main/java/com/mpr/platform/model/PlanStatus.java`
  - `src/main/java/com/mpr/platform/model/Plan.java`
  - `src/main/java/com/mpr/platform/repository/PlanRepository.java`
  - `src/main/java/com/mpr/platform/service/PlanService.java`
  - `src/main/java/com/mpr/platform/config/SecurityConfig.java`
  - `src/main/java/com/mpr/platform/controller/PlanWebController.java`
  - `src/main/java/com/mpr/platform/controller/PlanRestController.java`
  - `src/main/resources/static/css/styles.css`
  - `src/main/resources/templates/plans/list.html`
  - `src/main/resources/templates/plans/create.html`
  - `src/main/resources/templates/plans/detail.html`
  - `src/main/resources/templates/index.html`
  - `src/test/java/com/mpr/platform/service/PlanServiceTest.java`
  - `src/test/java/com/mpr/platform/controller/PlanControllerTest.java`
  - `docs/task-reports/task-05.md`
  - `TASK-TRACKER.md`
  - `PROJECT-PROGRESS.md`

- **Commands executed and actual outcomes:**
  1. `git checkout -b feature/5-create-view-plans develop`
     - Outcome: Created and checked out dedicated feature branch `feature/5-create-view-plans`.
  2. `mvn clean test`
     - Outcome: `BUILD SUCCESS` (12 tests run, 0 failures, 0 errors, 0 skipped in 26.6s).
  3. `git status`
     - Outcome: All modified and new files tracked cleanly.

- **Tests and results:**
  - **Unit Tests (`PlanServiceTest`):**
    - `testCreatePlanEnforcesDraftStatus`: PASSED.
    - `testCreatePlanRejectsInvalidPrice`: PASSED.
    - `testCustomerViewsOnlyPublishedPlans`: PASSED.
    - `testAnalystViewsAllPlans`: PASSED.
    - `testCanUserViewPlanPermissions`: PASSED.
  - **Integration Tests (`PlanControllerTest`):**
    - `testGetPlansListPublic`: PASSED (HTTP 200, view `plans/list`).
    - `testGetCreateFormAsAnalyst`: PASSED (HTTP 200, view `plans/create`).
    - `testGetCreateFormUnauthenticatedRedirectsToLogin`: PASSED (HTTP 302 redirect).
    - `testGetCreateFormAsCustomerReturnsForbidden`: PASSED (HTTP 403 Forbidden).
    - `testCreatePlanAsAnalyst`: PASSED (HTTP 302 redirect to `/plans`).
    - `testGetApiPlans`: PASSED (HTTP 200 JSON array).
  - **Context Test (`MobilePlanRecommendationApplicationTests`):** PASSED.
  - Total: 12 tests run, 0 failures, 0 errors.

- **Evidence captured (paths/URLs/screenshots/logs):**
  - Surefire test reports: `target/surefire-reports/`
  - Test task log: `C:\Users\Shreyas\.gemini\antigravity-ide\brain\5ab87586-0b4f-44d5-a383-377a38d54682\.system_generated\tasks\task-236.log`
  - Feature branch: `feature/5-create-view-plans`

- **Git branch / commit / tag / PR (if applicable):**
  - Feature Branch: `feature/5-create-view-plans` (Commit `a9f5f12`)
  - Base Integration Branch: `develop`
  - Pull Request: [PR #1](https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/1) (Merged)
  - Merge Commit on `develop`: `07437a2` (`Merge pull request #1 from feature/5-create-view-plans`)

- **Manual steps still required:**
  - **None.** Feature branch created, pushed, PR raised and verified, automated tests passed, and PR successfully merged into `develop`.

- **Known issues or risks:**
  - None. In-memory H2 database ensures isolated testing. Password hashing via BCrypt is active.
  - Port safety respected: Application runs on port 8080 locally; reserved ports 8072, 8081, 8082 untouched.

- **How to verify the result:**
  1. Run `mvn test` to re-execute the 12 automated unit and integration tests.
  2. Run `mvn spring-boot:run` and navigate to `http://localhost:8080/plans` as a guest/customer to see only `PUBLISHED` plans.
  3. Sign in as `analyst` (password `password`) and verify that all plans (including `DRAFT` and `IN_REVIEW`) are visible, and the "+ Create Plan" button is active.
  4. Submit a new plan draft and confirm it appears with `DRAFT` status badge.
  5. Sign out, sign in as `customer` (password `password`), and verify that the draft plan is hidden.

- **Next task / handoff notes:**
  - Proceed to **Task 6 — MVP Completion and Git Collaboration**:
    - Implement full user registration/login persistence in database.
    - Implement plan status transition workflow (`DRAFT → IN_REVIEW → PUBLISHED → ARCHIVED`).
    - Implement rule-based recommendation engine and summary dashboard.
    - Demonstrate Git collaboration with merge conflict resolution and create release tag `v1.0.0` on `main`.
