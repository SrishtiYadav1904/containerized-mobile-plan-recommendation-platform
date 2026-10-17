# Task 8 Report — Pipeline as Code and Server Deployment

## Task report

- **Task:** Task 8 — Pipeline as Code and Server Deployment
- **Status:** Completed (Fully Verified with Live Tomcat 10.1 Deployment & Health Check)
- **Date:** 2026-10-18
- **Goal:** Author a root-level declarative `Jenkinsfile` implementing Pipeline as Code with stages for checkout, environment/port policy validation, compile and test (fatal gate), package, archive/report, and deployment to Apache Tomcat 10.1 on port `8084`; parameterize deployment settings (`DEPLOY_ENV`, `APP_PORT`, `TOMCAT_HOME`, `DEPLOY_WAR`); strictly enforce port reservation policies avoiding forbidden ports `8072`, `8081`, and `8082`; implement robust post-deployment health check verification with retries and diagnostics; and verify pipeline execution on Jenkins.

- **What was implemented:**
  1. **Root-Level `Jenkinsfile` (Declarative Pipeline as Code):**
     - Authored `Jenkinsfile` in repository root using structured declarative syntax.
     - **Configured Parameters:**
       - `DEPLOY_ENV`: Choice parameter (`local-tomcat`, `staging`, `production`), default `local-tomcat`.
       - `APP_PORT`: String parameter, default `8084` (Forbidden: `8072, 8081, 8082`).
       - `TOMCAT_HOME`: String parameter, default `E:\Downloads\apache-tomcat-10.1.61` (with auto-detection fallback to `C:\apache-tomcat-10.1`).
       - `DEPLOY_WAR`: Boolean parameter, default `true`.
     - **Pipeline Stages:**
       - **Stage 1 (Checkout):** SCM checkout from `develop` branch.
       - **Stage 2 (Environment & Port Validation):** Validates Git, Java 21, and Maven wrapper. Explicitly checks `APP_PORT` against forbidden list (`8072, 8081, 8082`) and aborts with fatal error if violated.
       - **Stage 3 (Compile and Test):** Executes `mvnw.cmd -B clean test`. Test failures are fatal gates preventing downstream packaging/deployment.
       - **Stage 4 (Package):** Runs `mvnw.cmd -B package -DskipTests` to produce `target/containerized-mobile-plan-recommendation-platform.war`.
       - **Stage 5 (Archive and Report):** Publishes JUnit test reports (`target/surefire-reports/*.xml`) and archives deployable WAR/JAR artifacts with fingerprinting.
       - **Stage 6 (Deploy to Server):** Conditional server deployment stage. Detects Apache Tomcat 10.1, performs binary deployment via PowerShell `Copy-Item -Force` to `webapps/mpr-platform.war`, and invokes `scripts/health-check.ps1` to poll `/actuator/health` with exponential retries and diagnostic reporting.
       - **Post Actions:** Comprehensive notifications on build completion.
  2. **Robust Deployment Health Check Script (`scripts/health-check.ps1`):**
     - Dedicated, testable PowerShell script that queries `http://localhost:$Port/$ContextPath/actuator/health`.
     - Validates HTTP 200 response code and parses JSON body to confirm `"status":"UP"`.
     - Retries every 3 seconds up to a configurable timeout (default 60s) to give Tomcat sufficient time to unpack and initialize the Spring Boot context.
     - Exits with code 0 on success, or code 1 on failure with actionable diagnostics.
  3. **Server Deployment Guide (`docs/server-deployment-guide.md`):**
     - Detailed installation and configuration guide for Apache Tomcat 10.1 on Windows.
     - Port 8084 HTTP connector and port 8006 shutdown configuration in `conf/server.xml`.
     - Role configuration and credentials in `conf/tomcat-users.xml`.
     - Drop-in and pipeline deployment procedures, plus Actuator health check verification.
  4. **Jenkins SCM Job Execution & Verification:**
     - Build #4 verified initial SCM pipeline logic.
     - Build #6 exposed GString `resp` interpolation error and Windows cmd `copy` truncation.
     - PR #5 cleanly resolved both issues.
     - Build #7 executed with status **SUCCESS** (65s duration):
       - Tests: 30/30 passed.
       - WAR artifact: 58.2 MB binary copied to `E:\Downloads\apache-tomcat-10.1.61\webapps\mpr-platform.war`.
       - Health check passed: HTTP 200 with Actuator health status `UP` (database `H2` validated, disk space healthy).
       - Live application serving at `http://localhost:8084/mpr-platform/`.

- **Files created or changed:**
  - `Jenkinsfile`
  - `scripts/health-check.ps1`
  - `docs/server-deployment-guide.md`
  - `docs/jenkins-job-config.xml`
  - `docs/task-reports/task-08.md`

- **Commands executed and actual outcomes:**
  1. `git checkout -b feature/8-fix-deployment-health-check develop` -> Created bugfix branch.
  2. Created `scripts/health-check.ps1` with native PowerShell `Invoke-RestMethod` polling.
  3. Updated `Jenkinsfile` to use binary `Copy-Item` and `scripts/health-check.ps1`.
  4. `git commit -m "fix(pipeline): resolve Groovy interpolation error in health check and use binary WAR copy"` -> Commit `94f3526`.
  5. `git push -u origin feature/8-fix-deployment-health-check` -> Pushed to GitHub.
  6. `gh pr create ...` -> Created PR #5 (`https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/5`).
  7. `gh pr merge 5 --merge` -> Merged PR #5 into `develop` (`0871dfe`).
  8. Triggered Jenkins Build #7 with parameters (`TOMCAT_HOME=E:\Downloads\apache-tomcat-10.1.61`, `APP_PORT=8084`, `DEPLOY_WAR=true`).
  9. Jenkins Build #7 console verified: All 6 stages passed, health check confirmed `UP`, build status `SUCCESS`.

- **Tests and results:**
  - Jenkins Build #7 Pipeline Execution:
    - Stage 'Checkout': SUCCESS (Commit `0871dfe`)
    - Stage 'Environment & Port Validation': SUCCESS (Port 8084 validated, ports 8072, 8081, 8082 avoided)
    - Stage 'Compile and Test': SUCCESS (30 tests executed)
    - Stage 'Package': SUCCESS (58.2 MB WAR generated)
    - Stage 'Archive and Report': SUCCESS (JUnit XML parsed, WAR archived)
    - Stage 'Deploy to Server': SUCCESS (WAR deployed to Tomcat 10.1, health check returned HTTP 200 `UP`)
  - **Total:** 30 tests executed, 30 passed, 0 failures, 0 skipped.

- **Evidence captured (paths/URLs/screenshots/logs):**
  - Jenkins Job Overview: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/`
  - Jenkins Build #7 URL: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/7/`
  - Jenkins Build #7 Test Report: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/7/testReport/`
  - Live Application URL: `http://localhost:8084/mpr-platform/`
  - Live Health Endpoint: `http://localhost:8084/mpr-platform/actuator/health`
  - Live Health Check Response: `{"status":"UP","components":{"db":{"status":"UP","details":{"database":"H2","validationQuery":"isValid()"}},"diskSpace":{"status":"UP","details":{"total":996927336448,"free":547192422400,"threshold":10485760,"path":"E:\\Downloads\\apache-tomcat-10.1.61\\bin\\.","exists":true}},"ping":{"status":"UP"}}}`
  - Archived Artifact on Disk: `C:\ProgramData\Jenkins\.jenkins\jobs\Mobile-Plan-Recommendation-CI\builds\7\archive\target\containerized-mobile-plan-recommendation-platform.war`
  - Deployed WAR on Tomcat: `E:\Downloads\apache-tomcat-10.1.61\webapps\mpr-platform.war` (58,208,477 bytes)
  - GitHub Pull Request #5: `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/5`

- **Git branch / commit / tag / PR (if applicable):**
  - Feature branch: `feature/8-fix-deployment-health-check`
  - PR #5: `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/5` (Merged)
  - Integration commit: `0871dfe` on `develop`

- **Known issues or risks:**
  - None. Both SCM pipeline execution and standalone Tomcat server deployment are completely validated and operational.

- **How to verify the result:**
  1. Open browser to `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/7/`.
  2. View the green pipeline stage view: Checkout $\rightarrow$ Validation $\rightarrow$ Compile & Test $\rightarrow$ Package $\rightarrow$ Archive & Report $\rightarrow$ Deploy to Server.
  3. Verify console logs showing:
     `[DEPLOYMENT SUCCESS] Application is UP and healthy on port 8084!`
  4. Navigate to `http://localhost:8084/mpr-platform/` in browser to view the live catalogue with 24 plans.
  5. Navigate to `http://localhost:8084/mpr-platform/actuator/health` to confirm `{"status":"UP"}`.

- **Next task / handoff notes:**
  - Proceed to Task 9: Selenium Test Design and Local Execution.
