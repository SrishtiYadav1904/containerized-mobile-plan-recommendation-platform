# Containerized Mobile Plan Recommendation Platform

[![Java](https://img.shields.io/badge/Java-17_LTS-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Build](https://img.shields.io/badge/Build-Maven_3.9+-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Database](https://img.shields.io/badge/Database-MySQL_8_%2F_H2-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-Jenkins_%7C_Docker_%7C_Ansible-D24939?logo=jenkins&logoColor=white)](https://www.jenkins.io/)

A full-lifecycle DevOps platform engineered to streamline mobile plan comparisons, catalog management, and usage-based recommendations. The platform is built using **Java 17**, **Spring Boot 3**, **Thymeleaf**, **MySQL 8**, packaged as a **WAR** for **Tomcat 10.1**, and automated through **Jenkins**, **Docker**, and **Ansible**.

---

## 📌 Project Overview & Metadata

- **Student Name:** Srishti D Yadav
- **Roll Number:** 23102C0014
- **Guide Name:** Dr. Snehal Andhale
- **Course / Class:** DevOps CMPN C
- **Institution:** Vidyalankar Institute of Technology
- **GitHub Repository:** [`SrishtiYadav1904/containerized-mobile-plan-recommendation-platform`](https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform)

---

## 🚀 Key Features & MVP Scope

1. **User Authentication & Role-Based Access Control:**
   - Pre-configured roles: `CUSTOMER`, `ANALYST`, and `ADMINISTRATOR`.
   - Passwords hashed via BCrypt; role-gated URL routing and API security.
2. **Catalog Management & Review Workflow:**
   - 8 core plan fields: Operator, Name, Price, Validity (days), Daily Data (GB), Voice Minutes, SMS Count, Benefits.
   - Enforced lifecycle state transitions:  
     $$\text{DRAFT} \xrightarrow[\text{Analyst}]{\text{submit}} \text{IN\_REVIEW} \xrightarrow[\text{Admin}]{\text{publish}} \text{PUBLISHED} \xrightarrow[\text{Admin}]{\text{archive}} \text{ARCHIVED}$$
   - Customer visibility is strictly constrained to `PUBLISHED` plans.
3. **Usage & Budget Recommendation Engine:**
   - Transparent, rule-based multi-criteria scoring matching customer monthly data, calling minutes, budget limits, and service preferences without black-box ML.
4. **Catalogue Analytics Dashboard:**
   - Aggregated metrics showing distribution across operators, current plan statuses, and average pricing.
5. **Modern Accessible UI Aesthetic:**
   - Light theme with soft pink (`#E86A92`) and lilac (`#8E7CC3`) accents, clear typographic hierarchy, and responsive layouts.

---

## 🏗️ Architecture & Technology Stack

```
+-------------------------------------------------------------+
|                     Client Layer (Browser)                  |
|          Thymeleaf Server-Rendered Views & Bootstrap        |
+-------------------------------------------------------------+
                              │ HTTP / REST
                              ▼
+-------------------------------------------------------------+
|           Spring Boot 3 Application Layer (Java 17)         |
|  - Spring MVC Controllers & REST APIs                       |
|  - Spring Security (Form Login, Sessions & Role Checks)     |
|  - Recommendation Engine (Rule-based Scorer)                |
|  - Spring Data JPA Services & Repositories                  |
|  - Spring Boot Actuator (/actuator/health)                  |
+-------------------------------------------------------------+
                              │ JDBC
                              ▼
+-------------------------------------------------------------+
|                       Persistence Layer                     |
|  - Automated Tests: In-Memory H2 Database                   |
|  - Runtime / Production: MySQL 8.0 (Database: mpr_db)       |
+-------------------------------------------------------------+
```

| Component | Selected Technology | Notes |
|---|---|---|
| **Language** | Java 17 LTS | Configured with `<java.version>17</java.version>` |
| **Framework** | Spring Boot 3.2.5 | Web, Data JPA, Security, Validation, Actuator |
| **View Engine** | Thymeleaf + HTML5 / CSS3 | Soft pink and lilac light aesthetic |
| **Build Tool** | Apache Maven 3.9+ | Standard wrapper & lifecycle |
| **Packaging** | WAR | Compatible with Tomcat 10.1 & standalone execution |
| **Databases** | MySQL 8.0 & H2 | H2 for fast unit testing; MySQL for runtime |
| **Testing** | JUnit 5 & Selenium WebDriver | Headless CI testing |
| **CI / CD** | Jenkins Pipeline (`Jenkinsfile`) | Declarative pipeline stages |
| **Containerization** | Docker | Base: `tomcat:10.1-jdk17` |
| **Configuration** | Ansible | Playbooks for automated provisioning & rollback |

---

## 🔌 Port Policy & Host Environment Allocation

To prevent port collision with other active services on the developer environment:
- ❌ **Reserved Ports (Do Not Bind):** `8072`, `8081`, `8082`
- ✅ **Tomcat 10.1 Host Port:** `8084`
- ✅ **Docker Published Host Port:** `8085` $\rightarrow$ Container Port `8080`
- ✅ **Jenkins CI Host Port:** `8080`
- ✅ **MySQL Host Port:** `3306`

---

## 🛠️ Prerequisites & Local Setup

### System Prerequisites
- **JDK 17** (or JDK 21 in Java 17 compatibility mode)
- **Apache Maven 3.9+**
- **Git 2.40+**
- *(Optional for runtime database)* **MySQL 8.0 Server**

### Build & Run Locally

1. **Clone the repository:**
   ```bash
   git clone https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform.git
   cd containerized-mobile-plan-recommendation-platform
   ```

2. **Compile and execute automated tests:**
   ```bash
   mvn clean test
   ```

3. **Run using Spring Boot with embedded H2 (default):**
   ```bash
   mvn spring-boot:run
   ```

4. **Verify Health Endpoint:**
   Open [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) in your browser. Expected response:
   ```json
   {"status":"UP"}
   ```

5. **Package for Tomcat 10.1:**
   ```bash
   mvn clean package
   ```
   The deployable WAR file is generated at `target/containerized-mobile-plan-recommendation-platform.war`.

---

## 🌿 Branching Strategy & Git Workflow

We adhere to a structured GitFlow branching policy:
- **`main`**: Production releases only. Protected branch.
- **`develop`**: Integration branch for completed features.
- **`feature/<task-id>-<short-name>`**: Dedicated feature branches (e.g., `feature/5-create-view-plans`).
- **`fix/<short-name>`**: Bug fixes and patches.

Pull requests require passing test suites and adherence to the [Definition of Done](CONTRIBUTING.md). For detailed contribution rules, see [CONTRIBUTING.md](CONTRIBUTING.md).

---

## 📋 DevOps Delivery Roadmap

| Task | Scope & Objective | Deliverable |
|:---:|---|---|
| **Task 4** | Git & GitHub Repository Initialization | Repo layout, `pom.xml`, `.gitignore`, templates, branch rules |
| **Task 5** | Feature Development with Branching | Plan catalogue creation/view, feature branch, PR |
| **Task 6** | MVP Completion & Git Collaboration | Full role workflows, merge conflict resolution, v1.0.0 tag |
| **Task 7** | Jenkins Installation & CI Job | Automated build on commit, test reporting, artifact archiving |
| **Task 8** | Pipeline as Code & Server Deployment | Declarative `Jenkinsfile`, Tomcat deployment (Port `8084`) |
| **Task 9** | Selenium Test Design & Local Execution | Automated browser test suite covering critical customer/analyst journeys |
| **Task 10** | Continuous Testing in Jenkins | Headless Selenium runs inside CI pipeline |
| **Task 11** | Docker Image & Container Lifecycle | Multi-stage `Dockerfile`, container port `8085:8080` |
| **Task 12** | Jenkins-Docker Continuous Deployment | Automated image build, registry tag, and container deployment |
| **Task 13** | Configuration Management Script | Ansible playbook for environment provisioning |
| **Task 14** | Automated Provisioning & Reliability | Ansible idempotency, health-check validation, rollback execution |
| **Task 15** | Final Review, Documentation & Landing Page | Comprehensive audit, interactive 9-section landing page, 40 tests passed |

---

## 🌐 Project Landing Page & Routing

The platform features a responsive, light-themed landing page (`/`) styled in soft pink (`#E86A92`) and lilac (`#8E7CC3`) accents, presenting:
- **Interactive Sticky Navigation:** Anchor jumps across all sections with dynamic user session badge.
- **Hero Section:** Plan discovery value proposition, real-time catalog statistics, and live plan preview mockup.
- **5-Step User Journey:** Registration $\rightarrow$ Plan Discovery $\rightarrow$ Smart Recommendations $\rightarrow$ Analyst Authoring $\rightarrow$ Administrative Governance.
- **6 Platform Capabilities:** Multi-Carrier Catalogue, 4-Factor Rule Scorer, RBAC Security, Plan Lifecycle Governance, Analyst Authoring Suite, Executive Analytics.
- **10-Stage DevOps Pipeline:** Visual pipeline flow from Developer Push to Self-Healing Rollback with Fail-Fast Quality Gate indicators.
- **System Architecture:** Layered Application Runtime (Spring Boot, Thymeleaf, Spring Security, JPA) vs DevOps Delivery Infrastructure.
- **Reliability & Safety Matrix:** 100% test pass rate (40/40 tests), port isolation (8084/8085), non-root execution (UID 1001), Ansible idempotency, and automated rollback.
- **Comprehensive Tech Stack Grid:** Backend, Frontend, Testing, and DevOps tools.
- **Academic Credentials Footer:** Student Srishti D Yadav (23102C0014), Guide Dr. Snehal Andhale, Vidyalankar Institute of Technology.

### Primary Application Routes

| Path | Access Level | Description |
|---|---|---|
| `/` | Public (All) | Interactive Project Landing Page |
| `/plans` | Public / Customer / All | Mobile Plan Catalogue with search and filters |
| `/plans/{id}` | Public / All | Detailed plan specifications and benefits |
| `/recommendations` | Customer / All | 4-Factor Smart Recommendation Engine form & results |
| `/plans/new` | `ANALYST`, `ADMINISTRATOR` | New plan draft authoring form |
| `/dashboard` | `ANALYST`, `ADMINISTRATOR` | Executive metrics & status distribution dashboard |
| `/login` | Public | Role-based authentication form |
| `/actuator/health` | Public / Monitoring | Spring Boot Actuator health probe (`UP`) |

---

## 📄 Documentation & Reports

- Task progress reports: Located in `docs/task-reports/` (e.g., [`docs/task-reports/task-15.md`](docs/task-reports/task-15.md))
- Complete Project Progress & Viva Guide: [`PROJECT-PROGRESS.md`](PROJECT-PROGRESS.md)
- Architecture and Tasks 1–3 Baseline: [`docs/tasks-1-3-summary.md`](docs/tasks-1-3-summary.md)
- Task Tracker: [`TASK-TRACKER.md`](TASK-TRACKER.md)
