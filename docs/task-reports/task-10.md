# Task Report — Task 10: Continuous Testing in Jenkins

## Task report

- **Task:** Task 10 — Continuous Testing in Jenkins
- **Status:** Completed
- **Date:** 2026-10-18
- **Goal:** Integrate the Task 9 Selenium end-to-end UI test suite into the Jenkins CI/CD pipeline (`Jenkinsfile`), separate test execution into distinct Unit/Integration and Selenium E2E stages, publish JUnit test reports in Jenkins, archive failure screenshots and DOM HTML dumps upon test failure, enforce the Quality Gate so that test failures prevent WAR packaging and Tomcat server deployment, safely demonstrate the defect workflow (controlled failure on a separate branch, diagnostics capture, fix and rebuild), and verify pipeline execution on Jenkins.
- **What was implemented:**
  1. **Pipeline Separation of Test Stages (`Jenkinsfile`):**
     - Refactored `Jenkinsfile` to introduce two separate, isolated test stages:
       - **Stage 3 (`Unit & Integration Tests`):** Executes all 30 Spring Boot unit and integration tests via `mvnw.cmd -B clean test -Dtest="!MobilePlanSeleniumTests"`.
       - **Stage 4 (`Selenium End-to-End Tests`):** Executes all 5 automated UI user journeys with headless Google Chrome via `mvnw.cmd -B test -Dtest=MobilePlanSeleniumTests -Dtest.base.url=http://localhost:${params.APP_PORT}/mpr-platform -Dheadless=true`.
     - Configured the Quality Gate: if any test fails in Stage 3 or Stage 4, Maven exits with a non-zero code, immediately halting the pipeline. Downstream Stage 5 (`Package`) and Stage 6 (`Deploy to Server`) are skipped entirely, protecting the Tomcat server from broken code.
  2. **Browser & Driver Validation Stage:**
     - Enhanced Stage 2 (`Environment & Port Validation`) with automated Google Chrome binary verification at `C:\Program Files\Google\Chrome\Application\chrome.exe`.
  3. **Diagnostic Test Archival & Reporting (`post { always { ... } }`):**
     - Configured declarative `post` actions to publish JUnit test XML reports (`target/surefire-reports/*.xml`) on both passing and failing builds.
     - Configured `archiveArtifacts` to archive UI failure diagnostics: `target/screenshots/*.png`, `target/screenshots/*.html`, and `target/surefire-reports/*.txt`.
     - Restricted WAR and JAR artifact archival to `post { success { ... } }` so invalid packages are never preserved as releasable artifacts.
  4. **Dynamic Branch Parameterization:**
     - Parameterized `BRANCH_NAME` (default: `develop`) in `Jenkinsfile` and updated Jenkins job XML `<name>*/${BRANCH_NAME}</name>` to support building feature branches, defect demonstration branches, and `develop`.
  5. **Controlled Defect Workflow Demonstration:**
     - Created branch `defect/10-quality-gate-demonstration`.
     - Introduced an intentional assertion defect in `MobilePlanSeleniumTests.java` (asserting `SUPERUSER` instead of `CUSTOMER` in Journey 1).
     - Executed **Jenkins Build #12**:
       - Stage 3 (`Unit & Integration Tests`) PASSED (30/30).
       - Stage 4 (`Selenium End-to-End Tests`) FAILED at Journey 1 (`AssertionFailedError: expected: <SUPERUSER> but was: <CUSTOMER>`).
       - JUnit callback in `BaseSeleniumTest` captured full PNG screenshot: `MobilePlanSeleniumTests_testAuthenticationAndRoleBasedAccess_20261018_114049.png` and HTML page source dump: `MobilePlanSeleniumTests_testAuthenticationAndRoleBasedAccess_20261018_114049.html`.
       - Quality Gate tripped: Stage 5 (`Package`) and Stage 6 (`Deploy to Server`) were skipped.
       - Jenkins published JUnit report (1 failed, 34 passed) and archived the failure screenshot and DOM dump. Build status: `FAILURE`.
     - Switched to `feature/10-continuous-testing-jenkins` with clean code.
     - Executed **Jenkins Build #13**:
       - All 35 tests passed (30 unit + 5 selenium).
       - Stage 5 (`Package`) built `target/containerized-mobile-plan-recommendation-platform.war`.
       - Stage 6 (`Deploy to Server`) deployed WAR to Tomcat 10.1 (`E:\Downloads\apache-tomcat-10.1.61\webapps\mpr-platform.war`).
       - Health check probe returned `{"status":"UP"}`. Build status: `SUCCESS`.
     - Opened GitHub [PR #7](https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/7), merged into `develop`, synced `main`, and deleted the remote defect branch.
     - Executed **Jenkins Build #14** on `develop`: All 35 tests passed, packaging and deployment succeeded.
- **Files created or changed:**
  - `Jenkinsfile`: Refactored with separate test stages, browser validation, failure screenshot archival, and Quality Gate enforcement.
  - `scripts/trigger-jenkins-build.ps1`: Automated PowerShell script to authenticate with CSRF crumb, trigger parameterized builds, and stream live console logs.
  - `src/test/java/com/mpr/platform/ui/MobilePlanSeleniumTests.java`: Tested defect assertion on temporary branch; clean baseline preserved on `develop`.
  - `docs/task-reports/task-10.md`: Official Task 10 milestone report.
  - `TASK-TRACKER.md`: Updated Task 10 status to Completed.
  - `PROJECT-PROGRESS.md`: Added Task 10 comprehensive learning guide, viva Q&As, and updated progress metrics.
- **Commands executed and actual outcomes:**
  ```powershell
  # 1. Feature branch creation
  git checkout -b feature/10-continuous-testing-jenkins develop

  # 2. Update Jenkinsfile and commit
  git add Jenkinsfile scripts/trigger-jenkins-build.ps1
  git commit -m "feat(jenkins): integrate selenium e2e quality gate and test failure diagnostics into pipeline"
  git push -u origin feature/10-continuous-testing-jenkins

  # 3. Create defect branch and introduce deliberate assertion error
  git checkout -b defect/10-quality-gate-demonstration
  git commit -am "test(defect): introduce deliberate role badge assertion defect for quality gate demonstration"
  git push -u origin defect/10-quality-gate-demonstration

  # 4. Trigger Jenkins Build #12 (Defect Demonstration)
  powershell -ExecutionPolicy Bypass -File scripts/trigger-jenkins-build.ps1 -BranchName "defect/10-quality-gate-demonstration" -AppPort "8084"
  # Outcome: Build #12 FAILED at Stage 4; Packaging & Deployment skipped; Screenshot archived.

  # 5. Switch back to feature branch and trigger Jenkins Build #13 (Fixed / Clean Build)
  git checkout feature/10-continuous-testing-jenkins
  powershell -ExecutionPolicy Bypass -File scripts/trigger-jenkins-build.ps1 -BranchName "feature/10-continuous-testing-jenkins" -AppPort "8084"
  # Outcome: Build #13 SUCCESS (35/35 tests passed, WAR packaged and deployed, health check UP).

  # 6. GitHub PR creation, merge, and branch sync
  gh pr create --base develop --head feature/10-continuous-testing-jenkins --title "feat(ci): integrate selenium e2e quality gate and failure diagnostics into jenkins pipeline" ...
  gh pr merge 7 --merge --delete-branch
  git checkout develop ; git pull origin develop
  git checkout main ; git merge develop ; git push origin main ; git checkout develop
  git push origin --delete defect/10-quality-gate-demonstration

  # 7. Trigger Jenkins Build #14 on develop
  powershell -ExecutionPolicy Bypass -File scripts/trigger-jenkins-build.ps1 -BranchName "develop" -AppPort "8084"
  # Outcome: Build #14 SUCCESS.
  ```
- **Tests and results:**
  - **Jenkins Build #12 (Defect Branch):**
    - Unit & Integration Tests: 30 passed, 0 failed.
    - Selenium Tests: 4 passed, 1 failed (`testAuthenticationAndRoleBasedAccess`).
    - Failure Diagnostic: Screenshot `MobilePlanSeleniumTests_testAuthenticationAndRoleBasedAccess_20261018_114049.png` and HTML dump captured and archived in Jenkins.
    - Quality Gate: Stages `Package` and `Deploy to Server` skipped.
    - Final Status: `FAILURE`.
  - **Jenkins Build #13 (Feature Branch):**
    - Unit & Integration Tests: 30 passed, 0 failed.
    - Selenium Tests: 5 passed, 0 failed.
    - Quality Gate: Passed. Stage `Package` built 58 MB WAR. Stage `Deploy to Server` deployed to Tomcat on port 8084.
    - Health Check: `{"status":"UP"}`.
    - Final Status: `SUCCESS`.
  - **Jenkins Build #14 (`develop` Branch):**
    - Unit & Integration Tests: 30 passed, 0 failed.
    - Selenium Tests: 5 passed, 0 failed.
    - Total: 35/35 tests passed, 0 failures.
    - Final Status: `SUCCESS`.
- **Evidence captured (paths/URLs/screenshots/logs):**
  - Jenkins Job: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/`
  - Jenkins Build #12 (Defect Run): `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/12/`
  - Jenkins Build #12 Archived Failure Screenshot: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/12/artifact/target/screenshots/MobilePlanSeleniumTests_testAuthenticationAndRoleBasedAccess_20261018_114049.png`
  - Jenkins Build #12 Archived Failure DOM Source: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/12/artifact/target/screenshots/MobilePlanSeleniumTests_testAuthenticationAndRoleBasedAccess_20261018_114049.html`
  - Jenkins Build #13 (Passing Run): `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/13/`
  - Jenkins Build #14 (`develop` Integration Run): `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/14/`
  - Live Deployed Application: `http://localhost:8084/mpr-platform/`
  - Live Health Check: `http://localhost:8084/mpr-platform/actuator/health`
  - GitHub Pull Request #7: `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/7`
- **Git branch / commit / tag / PR (if applicable):**
  - Branches: `feature/10-continuous-testing-jenkins` (merged), `defect/10-quality-gate-demonstration` (demonstrated & deleted), `develop`, `main`.
  - Commits: `eb6011c`, `4cdab47`, `382f6e6`, `6c73fe0` (Merge PR #7).
  - Pull Request: [PR #7](https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/7).
- **Manual steps still required:** None. All CI/CD continuous testing steps, defect workflow demonstration, and live server deployments executed and verified.
- **Known issues or risks:**
  - Chrome / ChromeDriver CDP Warning: Selenium logged `WARNING: Unable to find CDP implementation matching 155`. This is a non-fatal warning as standard W3C WebDriver endpoints are used for all actions.
- **How to verify the result:**
  1. Open Jenkins job at `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/`.
  2. Inspect Build #12: verify it failed at Stage 4 (`Selenium End-to-End Tests`), skipped `Package` and `Deploy to Server`, and has archived `.png` and `.html` failure diagnostics.
  3. Inspect Build #13 and Build #14: verify all stages passed, 35/35 tests passed, WAR archived, and deployment health check returned HTTP 200 `UP`.
  4. Access live application at `http://localhost:8084/mpr-platform/` and actuator health at `http://localhost:8084/mpr-platform/actuator/health`.
- **Next task / handoff notes:**
  - Proceed to **Task 11 — Docker Image and Container Lifecycle**.
  - Target: Dockerfile authoring, multi-stage build, container run on host port `8085` (avoiding forbidden ports 8072, 8081, 8082), container lifecycle validation, and image push to Docker Hub.
