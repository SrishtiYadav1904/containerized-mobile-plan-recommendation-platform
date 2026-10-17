# Tasks 1–3 Architecture and Project Baseline Reference

**Project:** Containerized Mobile Plan Recommendation Platform  
**Student Name:** Srishti D Yadav  
**Roll Number:** 23102C0014  
**Guide Name:** Dr. Snehal Andhale  
**Course / Class:** DevOps CMPN C  
**Institution:** Vidyalankar Institute of Technology  

---

## 1. Problem Definition & Scope (Task 1 Summary)

### 1.1 Problem Statement
Customers face substantial friction when comparing mobile network plans across operators, trying to find plans matching their daily data, calling, SMS, validity, and budget constraints. Meanwhile, plan data is often scattered across unstructured spreadsheets or static pages without revision governance. On the operations side, manual builds and deployments cause environment inconsistency, skipped testing, and risky production releases.

This project delivers a containerized web platform that empowers analysts to manage plan catalogues through a structured review workflow, allows customers to get rule-based ranked recommendations, and automates testing, packaging, containerization, and configuration management via a full CI/CD pipeline (Git, Maven, Jenkins, Selenium, Docker, Ansible).

### 1.2 User Roles & Permissions
- **CUSTOMER:** Can register, log in, browse and search `PUBLISHED` plans only, and submit usage/budget queries to receive ranked recommendations.
- **ANALYST:** All Customer capabilities, plus create plan records (`DRAFT`), update existing plans, submit draft plans for review (`IN_REVIEW`), and view the catalog dashboard.
- **ADMINISTRATOR:** Full system governance. Can review, approve (`PUBLISHED`), or archive (`ARCHIVED`) plans, manage users, and inspect summary analytics.

### 1.3 Plan Status Workflow
Strict transition rules enforced:
- `DRAFT → IN_REVIEW`: Analyst submits plan.
- `IN_REVIEW → PUBLISHED`: Administrator verifies and publishes.
- `PUBLISHED → ARCHIVED`: Administrator retires the plan.
- *Any other transition is rejected.*

---

## 2. Agile Planning & DevOps Workflow (Task 2 Summary)

### 2.1 Working Approach & Sprints
- **Framework:** Scrum combined with Kanban board.
- **Sprint 0:** Planning, architecture, GitHub repository setup, baseline structure.
- **Sprint 1 (Tasks 1–5):** Foundation, authentication, role-based access, basic plan creation and view.
- **Sprint 2 (Tasks 6–10):** Full MVP catalog management, search/filters, CI in Jenkins, Selenium testing.
- **Sprint 3 (Tasks 11–15):** Recommendation engine, dashboard, Docker containerization, Ansible provisioning and rollback.

### 2.2 User Stories & Acceptance Criteria (Key Stories)
- **US1 (Auth):** User registration and login with BCrypt password hashing.
- **US2 (Roles):** Role-based access control protecting Analyst and Admin endpoints.
- **US3 (Create Plan):** Analysts create plans with 8 mandatory attributes (Operator, Name, Price, Validity, Data GB/day, Voice minutes, SMS count, Benefits). Initial status: `DRAFT`.
- **US4 (View Plans):** Plan catalog list and detail views. Customers see only `PUBLISHED` plans.
- **US5 (Update Plan):** Analysts update draft/review plans with update audit tracking (`updated_by`, `updated_at`).
- **US6 (Search & Filter):** Name/operator search and price/validity filtering.
- **US7 & US8 (Workflow):** Formal status transitions with role gates.
- **US9 & US10 (Recommendations):** Multi-attribute scoring based on customer usage and budget.
- **US11 (Dashboard):** Plan distribution counts by operator and status, plus average price.
- **US12 (DevOps Pipeline):** Continuous integration, containerization, automated testing, and rollback.

---

## 3. Requirements, Architecture & Technical Setup (Task 3 Summary)

### 3.1 Technology Stack Decisions
- **Programming Language:** Java 17 LTS
- **Backend Framework:** Spring Boot 3 (Web, Data JPA, Security, Validation, Actuator)
- **Template Engine & UI:** Thymeleaf, HTML5, CSS3, Bootstrap (Soft pink and lilac light aesthetic)
- **Build Automation:** Apache Maven 3.9+
- **Primary Database:** MySQL 8.0 (local runtime & production)
- **Testing Database:** H2 in-memory database (automated unit & integration tests)
- **Application Server:** Apache Tomcat 10.1 (WAR packaging)
- **Testing Frameworks:** JUnit 5, Mockito, Selenium WebDriver
- **CI/CD & Automation:** Jenkins (Pipeline as Code via Jenkinsfile), Docker, Ansible

### 3.2 Port Safety Allocation Policy
To avoid conflicts with existing background services on the host machine:
- **Reserved Ports (MUST NOT BE USED):** `8072`, `8081`, `8082`.
- **Tomcat Standalone Host Port:** `8084`
- **Docker Published Host Port:** `8085` (maps to container port `8080`)
- **Jenkins Server:** Assumed default `8080`
- **MySQL Database:** Standard `3306`

### 3.3 Database Schema (Initial Entity Model)
- **`users`:** `id` (PK), `name`, `email` (unique), `password_hash`, `role`
- **`plans`:** `id` (PK), `operator`, `name`, `price`, `validity_days`, `data_gb_per_day`, `voice_minutes`, `sms_count`, `benefits`, `status`, `created_by` (FK), `updated_by` (FK), `updated_at`
- **`recommendation_requests`:** `id` (PK), `user_id` (FK), `monthly_data_gb`, `monthly_minutes`, `budget`, `preferences`, `created_at`
- **`recommendation_results`:** `id` (PK), `request_id` (FK), `plan_id` (FK), `score`, `rank`

### 3.4 API Endpoints Specification
- `POST /api/auth/register` - Public registration
- `POST /api/auth/login` - Form/API login
- `GET /api/plans` - Search and list plans (filtered by role visibility)
- `GET /api/plans/{id}` - View individual plan detail
- `POST /api/plans` - Create new plan (Analyst, Admin)
- `PUT /api/plans/{id}` - Update plan details (Analyst, Admin)
- `PATCH /api/plans/{id}/status` - Status transition (`DRAFT → IN_REVIEW → PUBLISHED → ARCHIVED`)
- `POST /api/recommendations` - Compute and return ranked plan recommendations
- `GET /api/dashboard/summary` - Catalogue summary statistics
- `GET /actuator/health` - Health check endpoint for CI/CD readiness probes
