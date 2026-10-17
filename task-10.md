# Task 10 — Continuous Testing in Jenkins

## Purpose

Use this file as the Codex instruction for Task 10. Complete only this task's scope and preserve verified work from earlier tasks.

## Project context and non-negotiable constraints

- Project: **Containerized Mobile Plan Recommendation Platform**
- GitHub owner: `SrishtiYadav1904`
- Tasks 1–3 are already completed. Treat the supplied Task 1–3 document as the source of truth; do not redo those deliverables.
- Stack from the approved document: Java 17, Spring Boot 3, Maven, Thymeleaf, HTML/CSS/Bootstrap, Spring Security, Spring Data JPA, MySQL 8 for local/runtime data, H2 for automated tests, JUnit 5, Selenium WebDriver, Tomcat 10.1, Jenkins, Docker, and Ansible.
- UI direction: polished, friendly, modern and responsive; **light theme with soft pink/lilac accents**, clean typography, good spacing, accessible contrast. Do not use a dark theme, cyber/security aesthetic, neon colors, or a generic technical dashboard look.
- Core roles: `CUSTOMER`, `ANALYST`, `ADMINISTRATOR`.
- Plan statuses and valid transitions: `DRAFT → IN_REVIEW` (Analyst submits), `IN_REVIEW → PUBLISHED` (Administrator publishes), `PUBLISHED → ARCHIVED` (Administrator archives). Reject invalid transitions.
- Customers can see only published plans. Analysts can create/update plans and submit them for review. Administrators can approve/publish/archive and manage users.
- MVP functions: registration/login; role-based access; create, view, update, search/filter plans; status workflow; usage-and-budget-based ranked recommendations; dashboard with counts by status/operator and average price; validation and seed data.
- Plan fields: operator, name, price, validity days, daily data GB, voice minutes, SMS count and benefits. Recommendation inputs: monthly data, monthly call minutes, budget and benefit preferences. Use a transparent configurable rule-based score, not machine learning.
- Data model in the approved document: users, plans, recommendation_requests, recommendation_results.
- API operations in the approved document include `/api/auth/register`, `/api/auth/login`, `/api/plans`, `/api/plans/{id}`, `/api/plans/{id}/status`, `/api/recommendations`, `/api/dashboard/summary`, and `/actuator/health`. Keep APIs consistent if the implementation uses server-rendered pages too.
- Port policy: ports **8072, 8081 and 8082 are already in use and must not be used**. Use **8084** for the Tomcat-hosted application and **8085** for the Docker-published application port (host port), with container app port 8080. Jenkins is assumed to be available on its existing port 8080; do not reinstall or change it unless a real environment check proves that is necessary. Check for port conflicts before starting services. Do not bind the app to 8072, 8081 or 8082.
- Repository should be named `containerized-mobile-plan-recommendation-platform` under `SrishtiYadav1904`. Do not assume a repository already exists: inspect the current working directory and Git remote first. If remote creation requires authentication or GitHub UI access, give the user exact manual steps rather than pretending it succeeded.
- Never commit credentials, tokens, passwords, private keys, or local environment files. Use environment variables/Jenkins credentials.
- Work incrementally. Inspect existing files before editing; preserve completed work; do not overwrite unrelated projects or configuration. Run relevant tests after changes and report actual results only. Do not claim a PR, remote push, Jenkins run, Docker Hub push, or deployment happened unless verified.
- Use PowerShell-friendly commands where practical because the developer machine is Windows 11. Explain commands before asking the user to run them if they can affect services or data.
- At the end of each task, create/update the task report in `docs/task-reports/` using the report template below. Record completed work, files changed, commands actually run, test results, evidence locations, issues/blockers and next steps. Distinguish **completed**, **not completed**, and **requires manual action**.

## Codex prompt

Integrate the Selenium suite from Task 9 into the Jenkins pipeline. Add separate stages for unit/integration tests and Selenium end-to-end tests; publish JUnit reports and preserve failure screenshots as archived artefacts. Configure the pipeline so a failed test prevents packaging/deployment from proceeding to the deployment stage. Ensure Chrome/headless dependencies are available on the Jenkins agent, or document a reliable agent setup; do not silently skip Selenium tests. Demonstrate the required defect workflow safely: introduce one small, deliberate defect on a separate branch or controlled temporary commit, show that an automated test fails, then revert/fix the defect and rerun the pipeline successfully. Never leave the main/develop branch broken. Capture actual failed and passing build identifiers/logs if Jenkins access is available. If the Jenkins UI or agent is inaccessible, prepare the configuration and exact execution steps, and clearly mark evidence that needs manual capture. Update the task report.

## Required report file

Create or update `docs/task-reports/task-10.md` with the following structure and fill it with real outcomes:

## Task report

- **Task:** 
- **Status:** Not started / In progress / Completed / Blocked
- **Date:** 
- **Goal:** 
- **What was implemented:** 
- **Files created or changed:** 
- **Commands executed and actual outcomes:** 
- **Tests and results:** 
- **Evidence captured (paths/URLs/screenshots/logs):** 
- **Git branch / commit / tag / PR (if applicable):** 
- **Manual steps still required:** 
- **Known issues or risks:** 
- **How to verify the result:** 
- **Next task / handoff notes:** 

