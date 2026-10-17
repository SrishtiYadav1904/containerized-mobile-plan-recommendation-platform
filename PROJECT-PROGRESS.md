# Project Progress & DevOps Learning Guide

**Project Name:** Containerized Mobile Plan Recommendation Platform  
**GitHub Repository:** [https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform](https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform)  
**Student Name:** Srishti D Yadav  
**Roll Number:** 23102C0014  
**Guide Name:** Dr. Snehal Andhale  
**Academic Class:** DevOps CMPN C  
**Institution:** Vidyalankar Institute of Technology  
**Document Status:** Actively Maintained Project Progress Log & Viva Preparation Manual  

---

## Table of Contents
1. [Section A: Project Overview](#section-a-project-overview)
2. [Section B: Architecture and System Flow](#section-b-architecture-and-system-flow)
3. [Section C: Task-by-Task Progress (Tasks 1 to 15)](#section-c-task-by-task-progress-tasks-1-to-15)
4. [Section D: Current System State](#section-d-current-system-state)
5. [Section E: DevOps Concepts Learned (Glossary)](#section-e-devops-concepts-learned-glossary)
6. [Section F: End-to-End Demonstration Guide](#section-f-end-to-end-demonstration-guide)
7. [Section G: Next Steps](#section-g-next-steps)

---

## Section A: Project Overview

### 1. Problem Statement
In the telecom marketplace, consumers face hundreds of prepaid and postpaid tariffs with conflicting validity windows, daily data limits, voice quotas, and bundled perks (OTT, SMS, international roaming). Manually comparing plans across dispersed carrier portals is slow, error-prone, and frustrating. On the management side, telecom analysts frequently maintain plans in ad-hoc spreadsheets without peer review, leading to outdated prices and broken offers reaching customers.

On the operational side, traditional software delivery involves manual builds, file-copy server deployments, and untested releases. This leads to configuration drift, runtime failures in production, and zero rollback mechanisms.

### 2. The Platform Solution
The **Containerized Mobile Plan Recommendation Platform** is a secure, role-governed web application designed to solve both problems:
1. **Application Layer:** Provides a structured catalog review workflow where telecom analysts draft plans, administrators approve and publish them, and consumers receive personalized, ranked plan recommendations tailored to their usage and budget.
2. **DevOps Layer:** Enforces an automated, immutable deployment pipeline using Git branching, Maven build packaging, Jenkins CI/CD, Selenium headless testing, Docker containerization, and Ansible configuration management with automatic health checking and rollback.

### 3. Target Users & Access Control
- **`CUSTOMER`:** End-users. Can browse and filter only approved (`PUBLISHED`) mobile plans, input personal calling/data needs and budget constraints, and view ranked recommendations.
- **`ANALYST`:** Telecom catalog operators. Can create new plan drafts, modify draft attributes, submit plans for administrative review (`IN_REVIEW`), and view catalog summary statistics.
- **`ADMINISTRATOR`:** System owners. Can review submitted plans, publish (`PUBLISHED`) or retire (`ARCHIVED`) plans, manage user access, and inspect platform analytics.

### 4. 15-Task MVP Scope
The project scope is strictly frozen into a 15-task agile backlog:
1. Foundation & Database Schema
2. User Registration & Login (Form Authentication)
3. Role-Based Access Control (`CUSTOMER`, `ANALYST`, `ADMINISTRATOR`)
4. Plan Record Creation (Draft state)
5. Plan Catalogue List View
6. Plan Detail View
7. Plan Update & Audit Trail
8. Plan Search by Name & Operator
9. Plan Filtering by Price & Validity
10. Submission Workflow (`DRAFT → IN_REVIEW`)
11. Governance Workflow (`IN_REVIEW → PUBLISHED → ARCHIVED`)
12. Customer Usage & Budget Input Form
13. Recommendation Engine Scoring & Ranking
14. Catalogue Metrics Dashboard
15. Input Validation, Error Handling & Seed Data

### 5. Technology Stack & Technical Rationale

| Layer / Tool | Technology Selected | Rationale for Selection |
|---|---|---|
| **Language** | Java 17 LTS | Modern Long-Term Support release offering strong memory performance, pattern matching, and enterprise longevity. |
| **Framework** | Spring Boot 3.2.5 | Convention-over-configuration architecture with built-in production metrics (`Actuator`), high productivity, and robust JPA/Security starters. |
| **View Engine** | Thymeleaf + HTML5 / CSS3 | Fast server-rendered templating without requiring heavy JavaScript Single Page Application (SPA) builds. Ensures seamless mobile responsiveness. |
| **UI Aesthetics** | Custom Light Theme | Friendly, accessible light interface with soft pink (`#E86A92`) and lilac (`#8E7CC3`) accents; clean contrast and modern card layouts. |
| **Build Tool** | Apache Maven 3.9+ | Standardized dependency management, reproducible build phases (`compile`, `test`, `package`), and seamless Jenkins integration. |
| **Packaging** | Web Application Archive (WAR) | Allows flexible deployment: runnable as a standalone JAR/WAR locally, deployable into standalone Apache Tomcat 10.1, and containerizable in Docker. |
| **Database** | MySQL 8.0 & In-Memory H2 | In-memory H2 provides zero-setup, lightning-fast automated unit test runs; MySQL 8 provides ACID-compliant persistence for production. |
| **Continuous Integration** | Jenkins (Pipeline as Code) | Automates checkout, testing, packaging, and artefact archiving via declarative `Jenkinsfile`. |
| **Automated Testing** | JUnit 5 & Selenium WebDriver | JUnit 5 ensures backend logic correctness; Selenium simulates real customer browser interactions in headless Chrome. |
| **Containerization** | Docker | Packages the application and runtime environment into an immutable container image (`tomcat:10.1-jdk17`), eliminating "works on my machine" issues. |
| **Configuration Management** | Ansible | Declarative playbooks ensure target server environment idempotency, verify health probes, and execute automated rollback on failure. |

---

## Section B: Architecture and System Flow

### 1. Application Layer Flow
1. **Presentation (Browser):** The user interacts with responsive Thymeleaf templates rendered by Spring Boot.
2. **Security & Routing:** Spring Security intercepts incoming HTTP requests, authenticates user sessions, and verifies role authority before forwarding requests.
3. **Controller Layer:** Dispatches requests, binds form inputs, invokes business services, and returns view models or REST JSON payloads.
4. **Service & Algorithm Layer:** Contains core business logic, status transition validation (`DRAFT → IN_REVIEW → PUBLISHED → ARCHIVED`), and the transparent rule-based recommendation scorer.
5. **Data Access (JPA / Hibernate):** Spring Data JPA maps Java domain models to relational database tables, executing queries against MySQL 8 (or H2 during test runs).

### 2. DevOps Deployment Lifecycle Flow
$$\text{Developer Git Push} \longrightarrow \text{GitHub Webhook/Poll} \longrightarrow \text{Jenkins CI Pipeline} \longrightarrow \text{Maven Clean Test \& Package} \longrightarrow \text{Selenium Headless Tests} \longrightarrow \text{Docker Container Image Build} \longrightarrow \text{Ansible Provisioning \& Health Probe} \longrightarrow \text{Production Release (or Rollback)}$$

```mermaid
flowchart TD
    subgraph Developer_Station ["Developer Workstation (Windows 11)"]
        DEV["Developer Commit"] -->|git push| GH_REPO[("GitHub Repository\nSrishtiYadav1904")]
    end

    subgraph CI_Pipeline ["Continuous Integration & Testing (Jenkins)"]
        GH_REPO -->|Trigger Poll / Webhook| JNK["Jenkins CI Pipeline (Jenkinsfile)"]
        JNK --> MVN_TEST["Maven Build & Unit Tests (JUnit 5 + H2)"]
        MVN_TEST --> MVN_PKG["Package WAR Artifact"]
        MVN_PKG --> SEL_TEST["Automated UI Tests (Selenium Headless)"]
    end

    subgraph CD_Deployment ["Continuous Deployment & Infrastructure"]
        SEL_TEST -->|Build Image| DOCKER_BUILD["Docker Engine\n(Image: mpr-platform:latest)"]
        DOCKER_BUILD --> ANSIBLE_PROV["Ansible Playbook Provisioning"]
        ANSIBLE_PROV --> APP_RUN["Running Container / Tomcat 10.1\nHost Port 8085 / 8084"]
        APP_RUN --> HEALTH_CHECK{"Actuator Health Check\n/actuator/health"}
        HEALTH_CHECK -->|200 UP| PROD_OK["Deployment Healthy"]
        HEALTH_CHECK -->|Failure| ROLLBACK["Automated Rollback to Previous Image"]
    end

    classDef implemented fill:#D4EDDA,stroke:#28A745,color:#155724;
    classDef planned fill:#FFF3CD,stroke:#FFEEBA,color:#856404;

    class DEV,GH_REPO implemented;
    class JNK,MVN_TEST,MVN_PKG,SEL_TEST,DOCKER_BUILD,ANSIBLE_PROV,APP_RUN,HEALTH_CHECK,PROD_OK,ROLLBACK planned;
```

---

## Section C: Task-by-Task Progress (Tasks 1 to 15)

---

### Task 1 — Problem Definition and Scope
- **Task Objective:** Define the core problem, identify primary user classes, evaluate stakeholder pain points, establish project boundaries, and freeze the 15-task MVP scope.
- **What Was Implemented:** Authored formal system scope document. Categorized target users (`CUSTOMER`, `ANALYST`, `ADMINISTRATOR`), mapped the 4-state lifecycle workflow (`DRAFT`, `IN_REVIEW`, `PUBLISHED`, `ARCHIVED`), and defined measurable project success criteria.
- **Why It Matters:** Prevents scope creep and provides the architectural boundary before code or infrastructure is touched.
- **How It Works:** Compares real-world consumer confusion during mobile plan selection against telecom operational inefficiencies, establishing the exact features needed to solve both.
- **Tools Used:** Requirements engineering, Microsoft Word documentation (`devopsproj1to3_23102C0014.docx`).
- **Important Files:**
  - `devopsproj1to3_23102C0014.docx`: Original academic submission document.
  - `docs/tasks-1-3-summary.md`: Consolidated repository reference guide.
- **Commands Executed:** Baseline document synthesis.
- **Testing and Verification:** Approved by project guide Dr. Snehal Andhale.
- **Git Activity:** Baseline completed prior to Git repository initialization.
- **Evidence:** Document `devopsproj1to3_23102C0014.docx` in project root.
- **Problems Encountered:** Ambiguity in recommendation scoring; resolved by choosing a transparent rule-based algorithm over opaque machine learning.
- **Remaining Work:** None for Task 1.
- **Completion Status:** **Completed** (Approved baseline).
- **Viva Preparation:**
  - *Q1: Why was a rule-based algorithm selected instead of Machine Learning for plan recommendations?*  
    *Answer:* Mobile plan catalogues change frequently, and recommendations must be 100% explainable, deterministic, and instant based on strict user constraints (e.g., budget limits, exact validity days) rather than probabilistic statistical models.
  - *Q2: What are the three core user roles and their boundaries?*  
    *Answer:* Customer (browse published plans and receive recommendations), Analyst (create and edit plan drafts and submit for review), and Administrator (governance, publish/archive plans, user management).
  - *Q3: What are the four valid plan states?*  
    *Answer:* `DRAFT`, `IN_REVIEW`, `PUBLISHED`, and `ARCHIVED`.

---

### Task 2 — Agile Planning and DevOps Workflow
- **Task Objective:** Establish agile Scrum/Kanban delivery framework, draft user stories with acceptance criteria, build a product backlog with Fibonacci estimation, and define the Definition of Done.
- **What Was Implemented:** Created 12 user stories (US1–US12), mapped 4 sprints (Sprint 0 setup + 3 feature/DevOps sprints), designed Kanban board workflow with WIP limits (2), and formulated the Definition of Ready (DoR) and Definition of Done (DoD).
- **Why It Matters:** Ensures structured software engineering practices, traceability between code commits and requirements, and reliable release verification.
- **How It Works:** Work progresses from Backlog $\rightarrow$ To Do $\rightarrow$ In Progress $\rightarrow$ In Review (Pull Request) $\rightarrow$ Testing $\rightarrow$ Done (DoD verified).
- **Tools Used:** Scrum & Kanban methodology, Markdown trackers.
- **Important Files:**
  - `docs/tasks-1-3-summary.md` (Sections 2.1–2.7)
  - `TASK-TRACKER.md`: Ongoing task tracking matrix.
- **Commands Executed:** N/A (Methodological planning).
- **Testing and Verification:** User stories mapped directly to functional test cases.
- **Git Activity:** Baseline completed prior to Git initialization.
- **Evidence:** Tasks 1–3 documentation.
- **Problems Encountered:** Heavy third sprint; addressed by designating plan filtering as a "Should" requirement that can be rescheduled if needed.
- **Remaining Work:** None for Task 2.
- **Completion Status:** **Completed** (Approved baseline).
- **Viva Preparation:**
  - *Q1: What is the Definition of Done (DoD) in this DevOps project?*  
    *Answer:* An item is Done only when code is committed on a feature branch, passes local unit tests, passes Jenkins CI build and Selenium UI tests, has an approved PR merged into `develop`, satisfies all acceptance criteria, and contains zero hardcoded credentials.
  - *Q2: Why are WIP (Work In Progress) limits used on the Kanban board?*  
    *Answer:* WIP limits prevent multitasking bottlenecks, reduce context switching, and ensure developers finish reviewing and testing existing tasks before starting new ones.
  - *Q3: What is the Fibonacci estimation scale used in this project?*  
    *Answer:* 1, 2, 3, 5, and 8 story points, reflecting relative complexity, risk, and effort.

---

### Task 3 — Requirements, Architecture and Technology Setup
- **Task Objective:** Specify the Software Requirements Specification (SRS), logical architecture, database schema, REST API catalogue, technology choices, and port allocation policy.
- **What Was Implemented:** Produced complete SRS with 10 Functional Requirements (FR1–FR10) and 7 Non-Functional Requirements (NFR1–NFR7). Designed entity tables (`users`, `plans`, `recommendation_requests`, `recommendation_results`). Defined 9 primary REST endpoints. Formulated host port collision safeguards.
- **Why It Matters:** Provides the complete technical contract for development and avoids deployment collisions on host machines.
- **How It Works:** Establishes clear component boundaries between frontend templates, Spring controllers, JPA services, and persistence layers.
- **Tools Used:** Spring Boot architecture, MySQL Workbench, Markdown documentation.
- **Important Files:**
  - `docs/tasks-1-3-summary.md` (Sections 3.1–3.8)
- **Commands Executed:** Verification of tool prerequisites (`java -version`, `mvn -version`, `git --version`).
- **Testing and Verification:** Schema normalized to Third Normal Form (3NF). Port policy established.
- **Git Activity:** Baseline completed prior to Git initialization.
- **Evidence:** Tasks 1–3 documentation.
- **Problems Encountered:** Host machine had active services on ports 8072, 8081, and 8082; resolved by assigning application Tomcat port to 8084 and Docker published host port to 8085 (internal port 8080).
- **Remaining Work:** None for Task 3.
- **Completion Status:** **Completed** (Approved baseline).
- **Viva Preparation:**
  - *Q1: Why are ports 8072, 8081, and 8082 strictly avoided?*  
    *Answer:* Those ports are reserved by other pre-existing applications on the development system. To prevent binding conflicts and service crashes, Tomcat uses port 8084 and Docker uses host port 8085.
  - *Q2: What is the difference between H2 and MySQL in this project?*  
    *Answer:* H2 is an ultra-fast in-memory database used for automated unit tests without external dependencies; MySQL 8 is a persistent ACID database used for local execution and production runtime.
  - *Q3: What is the purpose of Spring Boot Actuator?*  
    *Answer:* It provides production-ready monitoring endpoints, specifically `/actuator/health`, which CI/CD pipelines and container orchestrators query to confirm the application is healthy.

---

### Task 4 — Git and GitHub Repository Initialization
- **Task Objective:** Initialize the version control repository for `containerized-mobile-plan-recommendation-platform` under GitHub owner `SrishtiYadav1904`, configure GitFlow branching rules, establish `.gitignore` and issue/PR templates, build the Spring Boot 3 / Java 17 skeleton with WAR packaging, verify automated tests, and publish to GitHub.
- **What Was Implemented:**
  1. Initialized Git repository with `main` and `develop` branches.
  2. Created Maven `pom.xml` with Spring Boot 3.2.5, Java 17 target, WAR packaging, and starters for Web, Thymeleaf, Security, JPA, Validation, Actuator, MySQL, and H2.
  3. Created Spring Boot entry point (`MobilePlanRecommendationApplication.java`), `SecurityConfig.java`, `HomeController.java`, responsive Thymeleaf landing page (`index.html`) with soft pink/lilac CSS styling (`styles.css`), and `application.properties`.
  4. Added comprehensive `.gitignore` filtering build artifacts (`target/`, `*.war`), IDE configurations, Windows OS files, and secrets (`.env`, `*.key`).
  5. Configured `.github/ISSUE_TEMPLATE/bug_report.md`, `feature_request.md`, and `.github/pull_request_template.md`.
  6. Authored `CONTRIBUTING.md`, project `README.md`, preserved task pack guide in `TASK-PACK-README.md`, and created `docs/tasks-1-3-summary.md`.
  7. Formally updated `TASK-TRACKER.md` and created `docs/task-reports/task-04.md`.
  8. Successfully authenticated via GitHub CLI (`gh auth login`) and published both `main` and `develop` branches to GitHub.
- **Why It Matters:** Establishes the foundational source control repository, automated build system, and team collaboration standards needed for all subsequent development and CI/CD pipelines.
- **How It Works:** Git tracks version history and manages branch isolation; Maven compiles Java source code and executes automated tests; GitHub hosts the centralized remote repository; GitHub CLI manages OAuth authentication.
- **Tools Used:** Git 2.47.0, GitHub, GitHub CLI (`gh`), Apache Maven 3.9.9, JDK 21 (running Java 17 compiler release), Spring Boot 3.2.5, JUnit 5.
- **Important Files:**
  - `pom.xml`: Root Maven build configuration.
  - `src/main/java/com/mpr/platform/MobilePlanRecommendationApplication.java`: Application main class.
  - `src/main/resources/application.properties`: Application runtime configuration.
  - `src/main/resources/templates/index.html`: Soft pink/lilac themed home page.
  - `src/main/resources/static/css/styles.css`: Custom responsive stylesheet.
  - `src/test/java/com/mpr/platform/MobilePlanRecommendationApplicationTests.java`: Context loads test.
  - `.gitignore`: Security and build artifact ignore rules.
  - `CONTRIBUTING.md`: Branching rules and Definition of Done.
  - `README.md`: Public platform documentation.
  - `docs/task-reports/task-04.md`: Detailed Task 4 outcome report.
- **Commands Executed:**
  ```powershell
  # 1. Initialize repository
  git init -b main
  # 2. Compile and run automated tests
  mvn clean test          # Outcome: BUILD SUCCESS (1 test run, 0 failures, 0 errors)
  # 3. Add remote origin
  git remote add origin https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform.git
  # 4. Authenticate via GitHub CLI
  gh auth login -h github.com -p https -w    # Logged in as SrishtiYadav1904
  gh auth setup-git                          # Configured git credential helper
  # 5. Push branches to GitHub
  git push -u origin main                    # Outcome: [new branch] main -> main
  git push -u origin develop                 # Outcome: [new branch] develop -> develop
  # 6. Verify remote branches via GitHub API
  gh api repos/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/branches
  ```
- **Testing and Verification:**
  - `mvn clean test` executed with `BUILD SUCCESS` in 26.8s.
  - Embedded H2 database initialized connection pool `HikariPool-1`.
  - Spring Boot Actuator endpoints `/actuator/health` and `/actuator/info` exposed.
  - Verified remote repository on GitHub: Public repository exists, both `main` and `develop` branches present, pointing to verified commit.
- **Git Activity:**
  - Repository URL: `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform`
  - Active Branches: `main` (default release), `develop` (integration)
  - Key Commits:
    - `234e156`: `feat(init): initialize repository structure, Maven Spring Boot skeleton, and DevOps config (Task 4)`
    - `ad3134e`: `docs(report): record final commit hash and verification in task-04 report`
    - `f8a9b47`: `docs(report): update task-04 verification and manual publication steps`
    - `826890a`: `docs(report): finalize task-04 verification with confirmed GitHub publication`
- **Evidence:**
  - Task Report: [docs/task-reports/task-04.md](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/docs/task-reports/task-04.md)
  - GitHub Remote API Output: Both branches tracking `origin` cleanly.
  - Test Log: `C:\Users\Shreyas\.gemini\antigravity-ide\brain\5ab87586-0b4f-44d5-a383-377a38d54682\.system_generated\tasks\task-55.log`
- **Problems Encountered:**
  - *Problem 1:* Attempting to push triggered `HTTP 403: Permission denied to Jiraiyasaanin`.  
    *Root Cause:* Windows Git Credential Manager had cached credentials for a different GitHub user (`Jiraiyasaanin`).  
    *Resolution:* Ran `gh auth login -h github.com -p https -w` to authenticate as `SrishtiYadav1904`, followed by `gh auth setup-git` to bind Git credential helpers directly to the authorized account.
  - *Problem 2:* Password authentication failure in terminal.  
    *Root Cause:* GitHub deprecated standard account passwords for Git operations in 2021.  
    *Resolution:* Replaced password input with OAuth token via GitHub CLI browser authentication.
- **Remaining Work:** None. Task 4 is fully verified and published.
- **Completion Status:** **Completed** (Verified locally and on GitHub).
- **Viva Preparation:**
  - *Q1: Why is .gitignore crucial in a DevOps project?*  
    *Answer:* `.gitignore` prevents build artifacts (`target/`, `*.class`, `*.war`), IDE settings, and sensitive environment secrets (`.env`, private keys) from being tracked in version control, ensuring repository hygiene, security, and reproducibility.
  - *Q2: Explain the GitFlow branching strategy configured in this project.*  
    *Answer:* `main` stores stable, deployable production releases; `develop` serves as the integration branch for tested code; `feature/<task-id>-<short-name>` branches isolate new feature work and merge back into `develop` via pull requests.
  - *Q3: Why is WAR packaging configured in pom.xml instead of JAR?*  
    *Answer:* WAR packaging enables the application to be deployed directly into standalone Apache Tomcat 10.1 servers (required in Task 8) while retaining the ability to run as an embedded Spring Boot application locally.

---

### Task 5 — Feature Development with Branching
- **Task Objective:** Implement the first application feature on a dedicated branch (`feature/5-create-view-plans`): create and view mobile plan records for Analysts, enforce `DRAFT` status, restrict customer visibility to `PUBLISHED` plans, write unit and integration tests, and raise a pull request targeting `develop`.
- **What Was Implemented:**
  1. Branched `feature/5-create-view-plans` from `develop`.
  2. Implemented `PlanStatus.java` enum (`DRAFT`, `IN_REVIEW`, `PUBLISHED`, `ARCHIVED`).
  3. Implemented `Plan.java` JPA entity with the 8 approved attributes (Operator, Name, Price, Validity, Data GB/day, Voice minutes, SMS count, Benefits) plus audit attributes and validation annotations.
  4. Created `PlanRepository.java` with Spring Data JPA query methods.
  5. Created `PlanService.java` enforcing initial `DRAFT` status, input validation, role-based visibility filtering (Customers view only `PUBLISHED`, Analysts/Admins view all), and sample data initialization.
  6. Configured role-based security in `SecurityConfig.java` for `CUSTOMER`, `ANALYST`, and `ADMINISTRATOR` roles with BCrypt encoding.
  7. Built `PlanWebController.java` (`GET /plans`, `GET /plans/new`, `POST /plans`, `GET /plans/{id}`) and `PlanRestController.java` (`GET /api/plans`, `POST /api/plans`).
  8. Created responsive, accessible light-theme Thymeleaf views: `plans/list.html`, `plans/create.html`, and `plans/detail.html` styled with soft pink (`#E86A92`) and lilac (`#8E7CC3`) accents.
  9. Authored 11 comprehensive unit and integration tests (`PlanServiceTest` and `PlanControllerTest`), achieving 100% test pass rate across 12 total test cases.
- **Why It Matters:** Demonstrates professional Git branching, entity modeling, status constraints, and service layer validation before merging into the main codebase.
- **How It Works:** Analysts submit plan creation requests via web form. Spring controller validates inputs, persists the entity with status `DRAFT`, and redirects to the catalog list. Customers querying the catalog receive only `PUBLISHED` plans.
- **Tools Used:** Git, Java 17, Spring Boot 3 (Web, Data JPA, Security, Validation), Thymeleaf, JUnit 5, Mockito, MockMvc.
- **Important Files:**
  - `src/main/java/com/mpr/platform/model/Plan.java`
  - `src/main/java/com/mpr/platform/model/PlanStatus.java`
  - `src/main/java/com/mpr/platform/repository/PlanRepository.java`
  - `src/main/java/com/mpr/platform/service/PlanService.java`
  - `src/main/java/com/mpr/platform/controller/PlanWebController.java`
  - `src/main/java/com/mpr/platform/controller/PlanRestController.java`
  - `src/main/resources/templates/plans/list.html`
  - `src/main/resources/templates/plans/create.html`
  - `src/main/resources/templates/plans/detail.html`
  - `src/test/java/com/mpr/platform/service/PlanServiceTest.java`
  - `src/test/java/com/mpr/platform/controller/PlanControllerTest.java`
  - `docs/task-reports/task-05.md`
- **Commands Executed:**
  ```powershell
  git checkout -b feature/5-create-view-plans develop
  mvn clean test          # Outcome: BUILD SUCCESS (12 tests run, 0 failures, 0 errors in 26.6s)
  ```
- **Testing and Verification:**
  - `PlanServiceTest`: 5 unit tests verifying DRAFT enforcement, price validation, customer PUBLISHED filtering, and detail permission checks.
  - `PlanControllerTest`: 6 MockMvc integration tests verifying web routing, security role gates, form creation, and REST endpoints.
  - Total: 12 tests run, 0 failures, 0 errors.
- **Git Activity:**
  - Feature Branch: `feature/5-create-view-plans`
  - Target Branch: `develop`
- **Evidence:**
  - Task Report: [docs/task-reports/task-05.md](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/docs/task-reports/task-05.md)
  - Maven Surefire Reports: `target/surefire-reports/`
- **Problems Encountered:** None. JPA entities, role filters, and Thymeleaf views compiled and passed cleanly.
- **Remaining Work:** Commit changes to `feature/5-create-view-plans`, push to GitHub, and create Pull Request to `develop`.
- **Completion Status:** **Completed** (Implementation and test suite verified).
- **Viva Preparation:**
  - *Q1: Why should feature development always happen on a separate branch?*  
    *Answer:* Branching isolates unfinished or experimental code from the stable integration branch (`develop`), enabling multiple developers to work concurrently without breaking the build.
  - *Q2: How does the application enforce that Customers only see PUBLISHED plans?*  
    *Answer:* The service layer filters catalog queries: when the authenticated user has the `CUSTOMER` role, it appends a `WHERE status = 'PUBLISHED'` predicate to the query.
  - *Q3: Why is input validation performed at both the Bean Validation level and the Service level?*  
    *Answer:* Bean Validation (`@Valid`, `@NotBlank`, `@DecimalMin`) catches input errors at the HTTP request layer and returns user-friendly form error messages, while Service-level validation acts as a defensive guard ensuring business rules cannot be bypassed by any controller or background job.

---

### Task 6 — MVP Completion and Git Collaboration
- **Task Objective:** Complete the full approved MVP (user authentication with BCrypt hashing, role-based access control for `CUSTOMER`, `ANALYST`, and `ADMINISTRATOR`, plan CRUD with search and filtering, role-governed status transition workflow, transparent multi-attribute recommendation engine, executive summary dashboard, and 24 realistic seed plans), demonstrate a controlled Git merge conflict and its clean resolution in a safe documentation file, expand automated test coverage, merge the feature branch into `develop` via Pull Request, sync `main`, and tag the release-ready baseline `v0.1.0`.
- **What Was Implemented:**
  1. **Domain Models & Repositories:**
     - `User.java`: Entity representing registered users with fields `username`, `password` (BCrypt encoded), `email`, and `role` (`CUSTOMER`, `ANALYST`, `ADMINISTRATOR`).
     - `RecommendationRequest.java`: Customer input entity storing `monthlyDataGb`, `monthlyVoiceMinutes`, `budget`, `preferredOperator`, and `benefitPreferences`.
     - `RecommendationResult.java`: Persisted recommendation scoring results storing overall `score`, component breakdown scores (`dataScore`, `voiceScore`, `priceScore`, `perksScore`), and human-readable explanation.
     - Spring Data JPA repositories: `UserRepository`, `RecommendationRequestRepository`, `RecommendationResultRepository`.
  2. **Security & Authentication Service:**
     - `UserService.java`: Handles user registration with username duplication checks, BCrypt password hashing, and implements Spring Security's `UserDetailsService` mapping domain roles to `ROLE_CUSTOMER`, `ROLE_ANALYST`, `ROLE_ADMINISTRATOR`. Pre-provisions 3 demo seed accounts (`customer`, `analyst`, `admin`).
     - `SecurityConfig.java`: Integrated `DaoAuthenticationProvider` with `UserService`, added custom `/login` and `/register` web routes, configured role guards for analyst actions (`/plans/new`, `/plans/*/edit`), administrator actions (`/plans/*/status`, `/dashboard`, `/api/dashboard/summary`), and permitted public access to catalog and recommendation views.
     - `AuthController.java` & `AuthRestController.java`: Web controllers and REST endpoints (`/api/auth/register`, `/api/auth/login`).
  3. **Plan Status Workflow & CRUD:**
     - `PlanService.java`: Enforces strict lifecycle state transitions:
       - `DRAFT → IN_REVIEW`: Analyst or Administrator submits draft.
       - `IN_REVIEW → PUBLISHED`: Administrator approves and publishes.
       - `PUBLISHED → ARCHIVED`: Administrator retires the plan.
       - Any invalid state jump (e.g., `DRAFT → PUBLISHED` or non-sequential changes) is strictly rejected with `IllegalStateException`.
     - Implemented dynamic plan search by keyword (matching plan name or operator) and multi-attribute filters (maximum price, minimum validity).
     - Loaded 24 realistic seed plans across 4 leading Indian operators (Jio, Airtel, Vi, BSNL) across varied validity windows (28, 56, 84, 365 days).
  4. **Transparent Multi-Attribute Recommendation Engine:**
     - `RecommendationService.java`: Configurable, rule-based multi-criteria scoring algorithm:
       $$\text{Score} = (0.35 \times \text{DataScore}) + (0.25 \times \text{VoiceScore}) + (0.25 \times \text{PriceScore}) + (0.15 \times \text{PerksScore})$$
     - Strict hard filtering: Plans priced strictly higher than the customer's budget are eliminated. Only plans in `PUBLISHED` status are evaluated.
     - Stores and renders itemized score breakdowns and explanatory justification for each suggested plan.
     - Created interactive web views: `recommendations/form.html` (input questionnaire) and `recommendations/results.html` (ranked results table with score pills).
  5. **Executive Summary Dashboard:**
     - `DashboardWebController.java` (`/dashboard`) and `PlanRestController.java` (`/api/dashboard/summary`).
     - Real-time aggregated platform metrics: Total plan count, breakdown by status (`DRAFT`, `IN_REVIEW`, `PUBLISHED`, `ARCHIVED`), breakdown by operator (`Jio`, `Airtel`, `Vi`, `BSNL`), and average plan price.
  6. **Controlled Git Merge Conflict & Resolution:**
     - Isolated temporary branch `conflict-simulation` branched from `develop`.
     - Made concurrent, conflicting edits to lines 7–17 of `docs/git-collaboration-demo.md`.
     - Executed `git merge conflict-simulation` on `feature/6-mvp-completion`, triggering `CONFLICT (add/add): Merge conflict in docs/git-collaboration-demo.md`.
     - Verified conflict markers (`<<<<<<< HEAD`, `=======`, `>>>>>>> conflict-simulation`), resolved them cleanly by merging the lead developer features and reviewer verification criteria, staged, committed resolution commit `6966f1a`, and deleted the temporary simulation branch.
  7. **Release Baseline Tagging:**
     - Merged Pull Request #2 into `develop` (`e51885b`).
     - Merged `develop` into `main` and tagged annotated release `v0.1.0`.
- **Why It Matters:** Fulfills all functional capabilities required for the platform's Minimum Viable Product (MVP), validates system integrity through 30 automated tests, demonstrates mastery of team Git workflows and conflict resolution, and marks an immutable release milestone.
- **How It Works:**
  - When an Analyst logs in, they can create plan drafts and submit them for review.
  - When an Administrator logs in, they review submitted plans, publish them to the live catalog, and inspect dashboard metrics.
  - When a Customer visits the site, they input their monthly data needs, call minutes, and maximum budget. The recommendation engine evaluates all published plans within budget, computes weighted scores, and returns a ranked list with score breakdowns.
- **Tools Used:** Git, Java 17, Spring Boot 3, Spring Security 6 (BCrypt), Spring Data JPA, Thymeleaf, Bootstrap 5, JUnit 5, MockMvc, GitHub CLI (`gh`).
- **Important Files:**
  - `src/main/java/com/mpr/platform/model/User.java`: User domain entity.
  - `src/main/java/com/mpr/platform/model/RecommendationRequest.java`: Recommendation input entity.
  - `src/main/java/com/mpr/platform/model/RecommendationResult.java`: Recommendation score result entity.
  - `src/main/java/com/mpr/platform/service/UserService.java`: User registration and Spring Security UserDetailsService.
  - `src/main/java/com/mpr/platform/service/PlanService.java`: Plan lifecycle transitions, search, and dashboard aggregation.
  - `src/main/java/com/mpr/platform/service/RecommendationService.java`: 4-factor scoring engine.
  - `src/main/java/com/mpr/platform/config/SecurityConfig.java`: Role guards and route permissions.
  - `src/main/resources/templates/dashboard/summary.html`: Metric charts and summary cards.
  - `src/main/resources/templates/recommendations/form.html` & `results.html`: Customer recommendation flow.
  - `docs/git-collaboration-demo.md`: Safe documentation conflict evidence artifact.
  - `docs/task-reports/task-06.md`: Official Task 6 milestone report.
- **Commands Executed:**
  ```powershell
  git checkout -b feature/6-mvp-completion develop
  mvn test                                                   # Result: 30 tests run, 0 failures, 0 errors
  git add src/ pom.xml
  git commit -m "feat(mvp): complete MVP auth, role workflows, recommendations, and dashboard (Task 6)"
  # Simulated controlled merge conflict
  git add docs/git-collaboration-demo.md ; git commit -m "docs: add initial collaboration documentation note"
  git checkout -b conflict-simulation develop
  # Added conflicting content
  git add docs/git-collaboration-demo.md ; git commit -m "docs: add peer reviewer collaboration note"
  git checkout feature/6-mvp-completion
  git merge conflict-simulation                             # Outcome: CONFLICT (add/add)
  # Cleanly resolved conflict markers
  git add docs/git-collaboration-demo.md ; git commit -m "docs: resolve merge conflict in git-collaboration-demo.md cleanly"
  git branch -d conflict-simulation
  git push -u origin feature/6-mvp-completion
  gh pr create --base develop --head feature/6-mvp-completion
  gh pr merge 2 --merge
  git checkout develop ; git pull origin develop
  git checkout main ; git merge develop ; git push origin main
  git tag -a v0.1.0 -m "Release v0.1.0: Complete Mobile Plan Recommendation Platform MVP"
  git push origin v0.1.0
  ```
- **Testing and Verification:**
  - `PlanServiceTest` (9 tests): Status transitions (`DRAFT → IN_REVIEW`, `IN_REVIEW → PUBLISHED`, `PUBLISHED → ARCHIVED`), analyst rejection on publishing, invalid transition rejection, search filtering, and dashboard metric counts.
  - `RecommendationServiceTest` (4 tests): Budget exclusion, weight respect, non-published exclusion, and zero-match boundary test.
  - `UserServiceTest` (4 tests): User registration, duplicate username handling, seed account initialization, and user lookup.
  - `PlanControllerTest` (12 tests): Routing, security role permissions, form validation, and REST API serialization.
  - `MobilePlanRecommendationApplicationTests` (1 test): Spring Boot context loading.
  - **Total:** 30 tests run, 0 failures, 0 errors, 0 skipped.
- **Git Activity:**
  - Feature Branch: `feature/6-mvp-completion`
  - Pull Request: [PR #2](https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/2) (Merged into `develop`)
  - Integration Merge Commit: `e51885b`
  - Release Tag: [`v0.1.0`](https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/releases/tag/v0.1.0) on `main`
- **Evidence:**
  - Task Report: [docs/task-reports/task-06.md](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/docs/task-reports/task-06.md)
  - GitHub PR #2: `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/2`
  - GitHub Tag v0.1.0: `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/releases/tag/v0.1.0`
  - Conflict Demonstration Artifact: [docs/git-collaboration-demo.md](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/docs/git-collaboration-demo.md)
- **Problems Encountered:**
  - *Problem:* Testing role-protected REST endpoints (`/api/dashboard/summary`) in `PlanControllerTest` initially failed with HTTP 403 Forbidden.  
    *Root Cause:* MockMvc requests without security context default to anonymous, and the dashboard summary endpoint is restricted to `ROLE_ADMINISTRATOR`.  
    *Resolution:* Annotated the dashboard slice tests with `@WithMockUser(roles = "ADMINISTRATOR")` to provide the required authority context during testing.
- **Remaining Work:** None. Task 6 MVP is 100% complete, tested, merged, and tagged.
- **Completion Status:** **Completed** (Verified locally and published on GitHub).
- **Viva Preparation:**
  - *Q1: How does BCrypt password hashing enhance security in Spring Security?*  
    *Answer:* BCrypt incorporates a random salt to protect against rainbow table attacks and uses an adaptable work factor (cost parameter) that slows down computation to thwart brute-force password cracking. Passwords are never stored in plain text.
  - *Q2: Explain the plan status transition state machine and how invalid jumps are guarded.*  
    *Answer:* The platform strictly enforces `DRAFT → IN_REVIEW` (Analyst/Admin), `IN_REVIEW → PUBLISHED` (Admin), and `PUBLISHED → ARCHIVED` (Admin). `PlanService.transitionStatus()` programmatically checks the entity's current status and user role before applying mutations, throwing an `IllegalStateException` on unauthorized moves (e.g. Analyst publishing) or invalid skips (e.g. `DRAFT → PUBLISHED`).
  - *Q3: Explain the mathematical formula used by the recommendation engine.*  
    *Answer:* It calculates a weighted linear composite score: $\text{Score} = (0.35 \times \text{DataScore}) + (0.25 \times \text{VoiceScore}) + (0.25 \times \text{PriceScore}) + (0.15 \times \text{PerksScore})$, where Data and Voice scores evaluate quota fulfillment, Price score awards higher points for greater savings below customer budget, and Perks score rewards matching OTT/benefit preferences. Plans priced above budget are strictly filtered out.
  - *Q4: What causes a Git merge conflict, and how did you demonstrate resolving it?*  
    *Answer:* A merge conflict happens when Git cannot automatically reconcile differing modifications made to the same lines of code in two branches being merged. We demonstrated this safely in `docs/git-collaboration-demo.md` by branching `conflict-simulation`, creating conflicting lines, merging it into `feature/6-mvp-completion`, observing Git's conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`), manually editing the file to incorporate both lead and peer review notes, staging the resolved file with `git add`, and recording the merge commit.
  - *Q5: What is the purpose of an annotated Git tag like v0.1.0?*  
    *Answer:* An annotated tag (`git tag -a`) is stored as a full Git object containing the tagger name, email, date, and a release message, pointing to an exact commit SHA. It acts as an immutable milestone indicating that the codebase has satisfied all acceptance tests and represents a release baseline ready for CI/CD automation.

---

### Task 7 — Jenkins Installation and Continuous Integration Job
- **Task Objective:** Discover and reuse the existing Jenkins server on port 8080 without reinstalling, preserve existing jobs and port safety, configure a continuous integration pipeline job for `containerized-mobile-plan-recommendation-platform`, implement automated Git checkout from `develop`, clean test execution, WAR packaging, JUnit test report publication, artifact archiving, polling trigger schedule (`H/15 * * * *`), and verify execution with actual build logs and archived artifact evidence.
- **What Was Implemented:**
  1. **Jenkins Controller & Environment Discovery:**
     - Verified Jenkins 2.568.1 running as an active Windows Service (`C:\Program Files\Jenkins\jenkins.exe`, PID 7308) bound to port `8080`.
     - Confirmed `JENKINS_HOME` at `C:\ProgramData\Jenkins\.jenkins`.
     - Confirmed installed toolchains: Java 21 LTS (`jdk-21.0.12`), Apache Maven 3.9.9 (`C:\Users\Shreyas\apache-maven-3.9.9`), and Git (`2.47.0.windows.1`).
     - Strictly respected port collision policies: Kept Jenkins on `8080`; preserved existing coursework job `Driving-School-CI` untouched; strictly avoided forbidden ports `8072`, `8081`, and `8082`.
  2. **Deterministic Build Setup (Maven Wrapper):**
     - Generated and committed cross-platform Maven wrapper scripts (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`) locking Maven version 3.9.9 to guarantee identical build behavior across developer workstations and Jenkins CI agents.
  3. **CI Architecture Documentation:**
     - Authored `docs/ci-configuration.md` specifying CI pipeline architecture, trigger mechanisms (GitHub Webhook & SCM Polling `H/15 * * * *`), credentials policy, and port reservation compliance.
  4. **Jenkins Pipeline Job Configuration (`Mobile-Plan-Recommendation-CI`):**
     - Created and configured the Pipeline job via Jenkins REST API using session cookies and CSRF crumb authentication.
     - Configured Git SCM pointing to `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform.git` tracking the `develop` branch.
     - Added automated build trigger with polling schedule: `H/15 * * * *`.
     - Configured resilient batch execution: Checks for `mvnw.cmd` and invokes `mvnw.cmd -B clean test`, with automatic fallback to global Maven binary.
     - Configured packaging stage: `mvnw.cmd -B package -DskipTests` to assemble the WAR file.
     - Configured post-build actions: Discovers and aggregates JUnit XML reports from `target/surefire-reports/*.xml`, and archives the deployable WAR artifact `target/*.war`.
  5. **Verified Build Execution:**
     - Executed Build #2 on the Jenkins controller.
     - Build status: **SUCCESS** (Total duration: 1m 43s).
     - Test results: **30 tests run, 30 passed, 0 failures, 0 skipped**.
     - Archived artifact: `target/containerized-mobile-plan-recommendation-platform.war` (58,208,477 bytes, ~58 MB).
- **Why It Matters:** Continuous Integration provides rapid feedback on code changes, detects regressions automatically, guarantees all 30 tests pass before merging, and packages verified, version-controlled artifacts ready for downstream deployment.
- **How It Works:**
  - Jenkins polls GitHub every 15 minutes.
  - When changes are detected on `develop`, Jenkins clones the repository into its workspace.
  - Jenkins triggers the Maven wrapper to compile code and execute JUnit 5 tests against the in-memory H2 database.
  - Upon test success, Maven packages the Spring Boot application into an executable and container/Tomcat-deployable WAR file.
  - The JUnit plugin parses test XML files and visualizes test trends in the Jenkins dashboard; the artifact archiver stores the resulting `.war` file in the build archive directory.
- **Tools Used:** Jenkins 2.568.1 (Pipeline, Git, JUnit, Archiver plugins), Apache Maven 3.9.9, Maven Wrapper, Java 21, Git, PowerShell, GitHub CLI (`gh`).
- **Important Files:**
  - `docs/ci-configuration.md`: Comprehensive CI architecture and configuration guide.
  - `docs/jenkins-job-config.xml`: XML definition of the Jenkins Pipeline job.
  - `mvnw` & `mvnw.cmd`: Maven wrapper executables.
  - `.mvn/wrapper/maven-wrapper.properties`: Maven wrapper version configuration.
  - `docs/task-reports/task-07.md`: Official Task 7 milestone report.
- **Commands Executed:**
  ```powershell
  # Check Jenkins service and port
  Get-Service | Where-Object { $_.Name -match "jenkins" }
  netstat -ano | findstr :8080
  # Generate Maven wrapper
  mvn wrapper:wrapper
  # Feature branch workflow
  git checkout -b feature/7-jenkins-ci-job develop
  git add .mvn/ mvnw mvnw.cmd docs/ci-configuration.md
  git commit -m "feat(ci): add maven wrapper and CI configuration guide (Task 7)"
  git push -u origin feature/7-jenkins-ci-job
  gh pr create --base develop --head feature/7-jenkins-ci-job ...
  gh pr merge 3 --merge
  git checkout develop ; git pull origin develop
  git checkout main ; git merge develop ; git push origin main
  # Trigger and monitor Jenkins build
  curl.exe -b cookies.txt -X POST "http://localhost:8080/job/Mobile-Plan-Recommendation-CI/build" -H "Jenkins-Crumb: <crumb>"
  Invoke-RestMethod -Uri "http://localhost:8080/job/Mobile-Plan-Recommendation-CI/2/api/json"
  Invoke-RestMethod -Uri "http://localhost:8080/job/Mobile-Plan-Recommendation-CI/2/testReport/api/json"
  ```
- **Testing and Verification:**
  - Jenkins Build #2 executed with exit code 0.
  - Jenkins JUnit Test Results:
    - Total: 30 tests
    - Passed: 30 tests
    - Failed: 0 tests
    - Skipped: 0 tests
  - Archived Artifact verified on disk:
    `C:\ProgramData\Jenkins\.jenkins\jobs\Mobile-Plan-Recommendation-CI\builds\2\archive\target\containerized-mobile-plan-recommendation-platform.war` (58.2 MB).
- **Git Activity:**
  - Feature Branch: `feature/7-jenkins-ci-job`
  - Pull Request: [PR #3](https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/3) (Merged into `develop`)
  - Integration Commit: `209427d` on `develop` and `main`
- **Evidence:**
  - Task Report: [docs/task-reports/task-07.md](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/docs/task-reports/task-07.md)
  - Jenkins Job: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/`
  - Jenkins Build #2: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/2/`
  - GitHub PR #3: `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/3`
  - CI Configuration Guide: [docs/ci-configuration.md](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/docs/ci-configuration.md)
- **Problems Encountered:**
  - *Problem:* Build #1 initially failed with `'mvn' is not recognized as an internal or external command`.  
    *Root Cause:* Jenkins runs under the Windows `NT AUTHORITY\SYSTEM` service account, which does not inherit user-level environment variables where Maven was added to PATH.  
    *Resolution:* Updated the pipeline script to prioritize `mvnw.cmd` (Maven wrapper located in the repository root) with an explicit fallback to `"C:\Users\Shreyas\apache-maven-3.9.9\bin\mvn.cmd"`. Build #2 passed seamlessly.
- **Remaining Work:** None. Task 7 is fully verified with actual Jenkins execution evidence.
- **Completion Status:** **Completed** (Verified on Jenkins controller).
- **Viva Preparation:**
  - *Q1: What is the difference between SCM Polling and Webhook triggers in Jenkins?*  
    *Answer:* SCM Polling is a pull-based mechanism where Jenkins periodically queries the remote Git repository (e.g., every 15 minutes via `H/15 * * * *`) to check for new commit hashes. A Webhook is a push-based mechanism where GitHub instantly notifies Jenkins via an HTTP POST request immediately upon a commit push, eliminating polling latency.
  - *Q2: Why is the Maven Wrapper (mvnw) preferred over a globally installed Maven binary in CI/CD?*  
    *Answer:* The Maven wrapper bundles the exact Maven version specification in `.mvn/wrapper/maven-wrapper.properties` and downloads it automatically if missing. This guarantees that all developers and CI server agents execute builds using identical Maven binaries, preventing "works on my machine" version incompatibilities.
  - *Q3: How does Jenkins capture and display JUnit test results?*  
    *Answer:* Maven Surefire plugin writes XML test execution reports to `target/surefire-reports/TEST-*.xml`. The Jenkins JUnit plugin (`junit 'target/surefire-reports/*.xml'`) parses these files, records pass/fail counts, extracts failure stack traces, and renders historical trend graphs on the job overview page.
  - *Q4: What is the purpose of the buildDiscarder option in Jenkins pipelines?*  
    *Answer:* `buildDiscarder(logRotator(numToKeepStr: '10'))` automatically purges old build logs and archived artifacts beyond the last 10 builds, preventing the Jenkins host disk from running out of storage space.
  - *Q5: Why must CI pipelines enforce port reservation policies?*  
    *Answer:* CI build and test runners must avoid binding to ports assigned to production services or concurrent applications. In our platform, Jenkins runs on port `8080`, unit tests use in-memory ephemeral ports/H2 database, Tomcat is reserved for `8084`, and Docker host is reserved for `8085`, strictly avoiding ports `8072`, `8081`, and `8082` which are actively occupied by other system processes.

---

### Task 8 — Pipeline as Code and Server Deployment
- **Task Objective:** Author a declarative `Jenkinsfile` with stages for Checkout, Build, Test, Package, Archive, and Deploy; deploy the WAR to standalone Apache Tomcat 10.1 on port 8084; verify Actuator health check.
- **What Was Implemented:** *Not started yet.*
- **Why It Matters:** Storing pipeline configuration in version control ("Pipeline as Code") ensures pipeline steps are versioned, reviewable, and reproducible across teams.
- **Completion Status:** **Not started**.
- **Viva Preparation:**
  - *Q1: What is "Pipeline as Code"?*  
    *Answer:* Defining the entire build, test, and deployment process in a version-controlled script (like a `Jenkinsfile`) rather than configuring jobs manually in a web UI.
  - *Q2: What is the difference between scripted and declarative Jenkins pipelines?*  
    *Answer:* Declarative syntax uses a structured, readable hierarchy with predefined blocks (`pipeline`, `stages`, `steps`, `post`), making it less error-prone than free-form Groovy-scripted pipelines.

---

### Task 9 — Selenium Test Design and Local Execution
- **Task Objective:** Design automated UI end-to-end test scripts using Selenium WebDriver covering critical user journeys (analyst creating a plan, customer viewing recommendations).
- **What Was Implemented:** *Not started yet.*
- **Why It Matters:** Unit tests check isolated methods, but end-to-end UI tests verify that HTML forms, browser sessions, JavaScript, and HTTP routes function harmoniously from a real user's perspective.
- **Completion Status:** **Not started**.
- **Viva Preparation:**
  - *Q1: What is Selenium WebDriver?*  
    *Answer:* A browser automation framework that directly controls a browser instance (such as Chrome or Firefox) to simulate user actions like clicking, typing, and submitting forms.
  - *Q2: Why are headless browsers used in CI pipelines?*  
    *Answer:* Headless browsers run in memory without rendering a physical GUI window, allowing fast, automated browser testing on servers without graphical display servers.

---

### Task 10 — Continuous Testing in Jenkins
- **Task Objective:** Integrate Selenium UI tests into the Jenkins pipeline with automated test reporting, demonstrate build failure when a UI test fails, fix the issue, and demonstrate a successful rebuild.
- **What Was Implemented:** *Not started yet.*
- **Why It Matters:** Proves the "quality gate" concept: failing tests must halt the pipeline before any deployment occurs.
- **Completion Status:** **Not started**.
- **Viva Preparation:**
  - *Q1: What is a "Quality Gate" in CI/CD?*  
    *Answer:* A mandatory check (such as unit test pass rate, code coverage, or UI test pass) that must succeed before an artifact is allowed to proceed to the next stage or to deployment.

---

### Task 11 — Docker Image and Container Lifecycle
- **Task Objective:** Create a root-level `Dockerfile` using `tomcat:10.1-jdk17`, package the WAR inside the container, configure container port 8080 mapped to host port 8085, and demonstrate image building and container management.
- **What Was Implemented:** *Not started yet.*
- **Why It Matters:** Containers bundle application binaries, runtime dependencies, and system libraries together, guaranteeing identical behavior across development, testing, and production servers.
- **Completion Status:** **Not started**.
- **Viva Preparation:**
  - *Q1: What is the difference between a Docker image and a Docker container?*  
    *Answer:* An image is a read-only template with instructions for building a container; a container is a running, isolated instance of that image.
  - *Q2: What does the port mapping -p 8085:8080 mean?*  
    *Answer:* It forwards traffic hitting port 8085 on the host operating system to port 8080 inside the isolated container where the web server is listening.

---

### Task 12 — Jenkins-Docker Continuous Deployment
- **Task Objective:** Extend the Jenkins pipeline to automatically build versioned Docker images upon successful tests, tag images, and deploy containers continuously.
- **What Was Implemented:** *Not started yet.*
- **Why It Matters:** Closes the gap between code integration and containerized deployment, ensuring validated code is packaged into deployable containers automatically.
- **Completion Status:** **Not started**.
- **Viva Preparation:**
  - *Q1: What is Continuous Deployment (CD)?*  
    *Answer:* The automated practice where every change that passes all stages of the CI pipeline is released automatically to production without human intervention.

---

### Task 13 — Configuration Management Script
- **Task Objective:** Author an Ansible playbook to automate server provisioning, package installation, runtime configuration, and directory setup.
- **What Was Implemented:** *Not started yet.*
- **Why It Matters:** Eliminates manual server configuration and ensures infrastructure setup is documented, version-controlled, and automated ("Infrastructure as Code").
- **Completion Status:** **Not started**.
- **Viva Preparation:**
  - *Q1: What is Ansible?*  
    *Answer:* An open-source, agentless configuration management and automation tool that uses declarative YAML playbooks to configure servers over SSH or local connections.

---

### Task 14 — Automated Provisioning and Reliability Validation
- **Task Objective:** Validate Ansible playbook idempotency, implement automated health-check verification via `/actuator/health`, and demonstrate automated rollback when a deployed version fails health verification.
- **What Was Implemented:** *Not started yet.*
- **Why It Matters:** Demonstrates production-grade reliability: idempotent provisioning ensures rerun safety, and automated rollback guarantees zero prolonged downtime if bad code slips through.
- **Completion Status:** **Not started**.
- **Viva Preparation:**
  - *Q1: What does "Idempotency" mean in configuration management?*  
    *Answer:* An operation is idempotent if executing it multiple times yields the exact same system state without duplicating side effects (e.g., ensuring a package is installed only if not already present).
  - *Q2: How does automated rollback work in containerized deployments?*  
    *Answer:* If the deployment health probe fails to receive a healthy HTTP 200 response from `/actuator/health` within a timeout, the orchestrator stops the faulty container and restarts the previous verified container image version.

---

### Task 15 — Documentation and Final Review
- **Task Objective:** Consolidate comprehensive project evidence, white paper, demonstration walkthrough, and final academic review.
- **What Was Implemented:** *Ongoing maintenance throughout all tasks.*
- **Why It Matters:** Synthesizes the end-to-end engineering journey for evaluators and establishes clear operational documentation.
- **Completion Status:** **In progress** (Actively documented via `PROJECT-PROGRESS.md`).

---

## Section D: Current System State

| Dimension | Current Verified State | Evidence / Location |
|---|---|---|
| **Active Local Branch** | `develop` (clean working tree, merged PR #3, synced with `main`) | `git status` |
| **Active Remote Branches** | `main`, `develop`, `feature/5-create-view-plans`, `feature/6-mvp-completion`, `feature/7-jenkins-ci-job` on GitHub | `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform` |
| **Latest Release Tag** | `v0.1.0` (Annotated release tag pointing to commit `e51885b` on `main`) | `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/releases/tag/v0.1.0` |
| **Latest Commit SHA** | `209427d` (`Merge pull request #3 from feature/7-jenkins-ci-job`) | `git log -n 1 --oneline` |
| **Working Application Features** | Full MVP complete: User registration & BCrypt authentication, Role-Based Access Control (`CUSTOMER`, `ANALYST`, `ADMINISTRATOR`), Plan CRUD with keyword search and price/validity filters, role-controlled status workflow (`DRAFT → IN_REVIEW → PUBLISHED → ARCHIVED`), customer usage/budget recommendation form with 4-factor scoring engine (35% data, 25% voice, 25% price, 15% perks), executive summary dashboard (`/dashboard` and `/api/dashboard/summary`), 24 realistic seed plans across 4 operators (Jio, Airtel, Vi, BSNL), Actuator health endpoint (`/actuator/health`), in-memory H2 database, light theme UI with soft pink/lilac accents. | `src/main/` |
| **Database Configuration** | Default: In-Memory H2 (`jdbc:h2:mem:mpr_db`). MySQL 8 configured via environment variable overrides (`DB_URL`, `DB_USER`, `DB_PASS`). Automatic initial plan and user seeding active. | `src/main/resources/application.properties` |
| **Test Suite Results** | 30 tests run, 0 failures, 0 errors, 0 skipped (`PlanServiceTest` [9], `RecommendationServiceTest` [4], `UserServiceTest` [4], `PlanControllerTest` [12], `MobilePlanRecommendationApplicationTests` [1]). All 30 tests published and verified in Jenkins JUnit reporting. | Jenkins Test Report (`http://localhost:8080/job/Mobile-Plan-Recommendation-CI/2/testReport/`) |
| **Jenkins Status** | **Completed & Active:** Pipeline job `Mobile-Plan-Recommendation-CI` configured on Jenkins 2.568.1 (port 8080). Build #2 executed with status `SUCCESS` (30/30 tests passed, WAR archived). | `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/` |
| **Archived Artifacts** | `target/containerized-mobile-plan-recommendation-platform.war` (58.2 MB) | Jenkins Build #2 Archive on disk |
| **Docker Status** | Not started (Planned for Tasks 11 & 12). | N/A |
| **Ansible Status** | Not started (Planned for Tasks 13 & 14). | N/A |
| **Verified Working Ports** | Jenkins Controller: `8080`. Planned Tomcat: `8084`. Planned Docker host: `8085`. Reserved ports `8072`, `8081`, `8082` strictly avoided. | Port Policy in `README.md` |
| **Known Blockers** | None. Tasks 1–7 complete, tested, and published. | `TASK-TRACKER.md` |

---

## Section E: DevOps Concepts Learned (Glossary)

- **Version Control System (VCS):** Software (like Git) that records changes to files over time, enabling developers to recall specific versions, compare differences, and collaborate without overwriting each other's work.
- **GitFlow:** A branching model where `main` contains production releases, `develop` serves as the integration branch, and dedicated `feature/` branches isolate task development.
- **Pull Request (PR):** A mechanism in Git hosting platforms (like GitHub) that notifies team members that a feature branch is ready for review, discussion, automated testing, and merging into a target branch.
- **Merge Conflict Resolution:** The process of reconciling concurrent differing changes made to identical lines in two branches. Git marks conflicts using markers (`<<<<<<<`, `=======`, `>>>>>>>`), and the developer combines or chooses the correct edits, stages the resolved file, and commits the merge.
- **Continuous Integration (CI):** An automated pipeline practice where code commits trigger automated compilation, static analysis, and test suites, ensuring regressions are detected immediately.
- **Continuous Delivery (CD):** An extension of CI where code changes are automatically tested and packaged, producing an artifact ready to be deployed to testing or production environments.
- **Continuous Deployment:** Full automation where every verified code change passing all test gates is deployed directly into production without human approval.
- **Build Lifecycle (Maven):** A sequence of phases including `validate`, `compile`, `test`, `package`, `verify`, `install`, and `deploy`. Running `mvn clean test` cleans previous build outputs, compiles source code, and executes test suites.
- **Maven Wrapper (mvnw):** A cross-platform script and properties bundle that downloads and invokes a project-locked Maven version without requiring a manual Maven installation on the host system.
- **SCM Polling:** A periodic pull mechanism where Jenkins queries Git remote branches (e.g. `H/15 * * * *`) to discover new commits and trigger builds when inbound webhooks cannot penetrate local firewalls.
- **JUnit Plugin:** A Jenkins plugin that aggregates and renders XML test reports generated during Maven test runs, tracking pass rates and failure trends across builds.
- **Web Application Archive (WAR):** A compressed package containing web application resources (servlets, JSPs/HTML, Java classes, libraries) designed for deployment on Java web application servers like Apache Tomcat.
- **Spring Boot Actuator:** A Spring sub-framework providing production metrics, health checks (`/actuator/health`), thread dumps, and application info via standard HTTP endpoints.
- **Headless Browser:** A web browser that executes without a visible user interface, operated programmatically (e.g., via Selenium WebDriver) for automated integration testing in CI environments.
- **Docker Image vs Container:** A Docker image is an immutable blueprint containing the code, runtime, system libraries, and settings. A Docker container is an isolated, executable runtime instance of that image.
- **Port Mapping (`-p <host>:<container>`):** Directs incoming network traffic from a specific port on the host operating system to a listening port inside a Docker container.
- **Infrastructure as Code (IaC):** The practice of provisioning and managing computing infrastructure using machine-readable configuration files (e.g., Ansible YAML playbooks) rather than physical hardware configuration or manual interactive tools.
- **Idempotency:** A property of configuration management operations whereby applying the same operation repeatedly yields the identical result without unintended state changes.
- **Automated Rollback:** A resilience mechanism that automatically reverts the running deployment to the previously verified, healthy container image or artifact if post-deployment health probes fail.

---

## Section F: End-to-End Demonstration Guide

When demonstrating the project in its current state (Tasks 1–7 Completed — Full MVP & Working Jenkins CI) to an evaluator:

### Step 1: Explain the Project Scope, Architecture & State Machine
- **Action:** Open `README.md` and `PROJECT-PROGRESS.md`.
- **Explain:** Describe the real-world problem (confusing mobile plan tariffs and unreviewed catalogue updates), the 3 roles (`CUSTOMER`, `ANALYST`, `ADMINISTRATOR`), the 4 plan states (`DRAFT → IN_REVIEW → PUBLISHED → ARCHIVED`), and the rule-based recommendation formula (35% data, 25% calls, 25% budget savings, 15% perks).

### Step 2: Show GitHub Source Control, PRs, and Release Tag
- **Action:** Navigate to [https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform](https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform).
- **Explain:**
  - Show the branches (`main`, `develop`, `feature/5-create-view-plans`, `feature/6-mvp-completion`, `feature/7-jenkins-ci-job`).
  - Show the Pull Requests: PR #1, PR #2, and PR #3, all cleanly merged.
  - Show Release Tag `v0.1.0` marking the production-ready baseline.
  - Show `docs/git-collaboration-demo.md` demonstrating real-world merge conflict resolution.
  - Show `docs/ci-configuration.md` explaining the CI pipeline architecture.

### Step 3: Demonstrate Jenkins CI Pipeline & Test Reports
- **Action:** Open browser to `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/`.
- **Explain:**
  - Point out that Jenkins is running locally on port `8080` without port conflict.
  - Show Build #2 status: **SUCCESS** with green pipeline stage view (Checkout $\rightarrow$ Compile and Test $\rightarrow$ Package).
  - Open **Test Result** link: show 30/30 tests passing with zero failures.
  - Open **Build Artifacts**: show the archived 58 MB `containerized-mobile-plan-recommendation-platform.war` artifact.

### Step 4: Demonstrate Running Application & Features
- **Action:** Run:
  ```powershell
  mvn spring-boot:run
  ```
- **Open Browser:** Navigate to `http://localhost:8080`.
- **Demonstrate:**
  1. **Catalogue View:** Browse 24 realistic Indian operator plans (Jio, Airtel, Vi, BSNL) filtered with soft pink/lilac styling.
  2. **Recommendation Form:** Visit `/recommendations`, enter customer monthly data (e.g. 50 GB), voice minutes (1000 mins), and budget (₹500). Inspect the ranked recommendation results and score breakdown pills.
  3. **Role-Based Login:** Visit `/login`, log in as `admin` (password: `admin123`).
  4. **Executive Dashboard:** Access `/dashboard` to view real-time plan status distribution and average price metrics across operators.
  5. **Health Probe:** Visit `http://localhost:8080/actuator/health` to show `{"status":"UP"}`.

---

## Section G: Next Steps

### Immediate Next Task: Task 8 — Pipeline as Code and Server Deployment

- **Goal:** Author a root-level declarative `Jenkinsfile` incorporating stages for Checkout, Build, Unit/Integration Test, Package, Archive, and Deployment to standalone Apache Tomcat 10.1 on port `8084`, parameterized with `DEPLOY_ENV` or `APP_PORT`, and verified with `/actuator/health`.
- **Prerequisites:**
  - Task 7 CI Job completed and verified on Jenkins (Satisfied ✅).
  - Tomcat 10.1 configured for port 8084.
  - Port collision policies respected: Jenkins on 8080, Tomcat on 8084, Docker host on 8085; avoid 8072, 8081, 8082.
- **Actions Required:**
  1. Create `feature/8-pipeline-server-deploy` branch from `develop`.
  2. Create root-level `Jenkinsfile` with declarative pipeline stages.
  3. Configure safe deployment to Tomcat 10.1 on port `8084`.
  4. Implement post-deployment health check verifying HTTP 200 from `/actuator/health`.
  5. Verify pipeline execution, record build logs, update `docs/task-reports/task-08.md`, `TASK-TRACKER.md`, and `PROJECT-PROGRESS.md`.
