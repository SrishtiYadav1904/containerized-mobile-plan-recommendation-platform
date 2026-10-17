# Task 7 Report — Jenkins Installation and Continuous Integration Job

## Task report

- **Task:** Task 7 — Jenkins Installation and Continuous Integration Job
- **Status:** Completed
- **Date:** 2026-10-18
- **Goal:** Verify the local Jenkins environment on port 8080 without reinstalling, preserve existing jobs, configure a continuous integration pipeline job for `containerized-mobile-plan-recommendation-platform`, implement automated checkout, clean test execution, packaging, JUnit test report publication, artifact archiving, polling trigger schedule (`H/15 * * * *`), and verify execution with actual build logs and archived artifact evidence.

- **What was implemented:**
  1. **Environment Verification:**
     - Verified Jenkins 2.568.1 running as a Windows service on `http://localhost:8080` (PID 7308, `C:\Program Files\Jenkins\jenkins.exe`, `JENKINS_HOME` at `C:\ProgramData\Jenkins\.jenkins`).
     - Verified Java 21 LTS (`jdk-21.0.12`), Apache Maven 3.9.9 (`C:\Users\Shreyas\apache-maven-3.9.9`), and Git (`git version 2.47.0.windows.1`).
     - Verified port safety: Preserved port 8080 for Jenkins; strictly avoided reserved/conflicting ports 8072, 8081, and 8082. Preserved existing coursework job `Driving-School-CI` completely untouched.
  2. **Build Determinism & Maven Wrapper:**
     - Added cross-platform Maven wrapper (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`) locking Maven version 3.9.9 for reproducible CI runs.
  3. **CI Architecture Documentation:**
     - Authored `docs/ci-configuration.md` specifying CI pipeline architecture, SCM trigger strategies (GitHub Webhook & SCM Polling `H/15 * * * *`), credentials policy, port reservation compliance, and pipeline definitions.
  4. **Jenkins CI Job Creation & Configuration:**
     - Configured Pipeline job `Mobile-Plan-Recommendation-CI` via Jenkins REST API using session authentication and CSRF crumb token.
     - SCM: Git checkout of `develop` branch from `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform.git`.
     - Build steps: Resilient batch execution invoking `mvnw.cmd -B clean test` (with fallback to global Maven path), followed by `mvnw.cmd -B package -DskipTests`.
     - Post-build actions: JUnit test report publication from `target/surefire-reports/*.xml`, and WAR artifact archiving (`target/*.war`).
  5. **Build Execution & Evidence:**
     - Triggered Build #2.
     - Pipeline executed all stages: Checkout $\rightarrow$ Compile and Test $\rightarrow$ Package $\rightarrow$ JUnit Recording $\rightarrow$ Artifact Archiving.
     - Result: `SUCCESS` (Duration: 1m 43s).
     - Test Result: 30 tests run, 30 passed, 0 failures, 0 skipped.
     - Archived Artifact: `target/containerized-mobile-plan-recommendation-platform.war` (58,208,477 bytes).

- **Files created or changed:**
  - `mvnw`
  - `mvnw.cmd`
  - `.mvn/wrapper/maven-wrapper.properties`
  - `docs/ci-configuration.md`
  - `docs/jenkins-job-config.xml`
  - `docs/task-reports/task-07.md`

- **Commands executed and actual outcomes:**
  1. `Get-Service | Where-Object { $_.Name -match "jenkins" }` -> Jenkins service `Running`.
  2. `netstat -ano | findstr :8080` -> Confirmed port 8080 listening by PID 7308.
  3. `mvn wrapper:wrapper` -> Generated `mvnw`, `mvnw.cmd`, and `.mvn/wrapper/maven-wrapper.properties`.
  4. `git checkout -b feature/7-jenkins-ci-job develop` -> Switched to feature branch.
  5. `git add .mvn/ mvnw mvnw.cmd docs/ci-configuration.md ; git commit -m "feat(ci): add maven wrapper and CI configuration guide (Task 7)"` -> Commit `82118bc`.
  6. `git push -u origin feature/7-jenkins-ci-job` -> Pushed to GitHub.
  7. `gh pr create --base develop --head feature/7-jenkins-ci-job ...` -> Created PR #3 (`https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/3`).
  8. `gh pr merge 3 --merge` -> Merged PR #3 into `develop` (`209427d`).
  9. `git checkout develop ; git pull origin develop` -> Local `develop` fast-forwarded to `209427d`.
  10. `git checkout main ; git merge develop ; git push origin main ; git checkout develop` -> Synced `main` with `develop`.
  11. `Invoke-WebRequest ... /createItem?name=Mobile-Plan-Recommendation-CI` -> Created Jenkins Pipeline job.
  12. `curl.exe -b $cookieFile -X POST "http://localhost:8080/job/Mobile-Plan-Recommendation-CI/build"` -> Triggered CI build.
  13. `Invoke-RestMethod ... /job/Mobile-Plan-Recommendation-CI/2/api/json` -> Build 2 status: `SUCCESS`.
  14. `Invoke-RestMethod ... /job/Mobile-Plan-Recommendation-CI/2/testReport/api/json` -> `totalCount: 30`, `passCount: 30`, `failCount: 0`.

- **Tests and results:**
  - Build #2 automated test report:
    - `PlanServiceTest`: 9/9 passed.
    - `RecommendationServiceTest`: 4/4 passed.
    - `UserServiceTest`: 4/4 passed.
    - `PlanControllerTest`: 12/12 passed.
    - `MobilePlanRecommendationApplicationTests`: 1/1 passed.
  - **Total:** 30 tests executed, 30 passed, 0 failures, 0 skipped.

- **Evidence captured (paths/URLs/screenshots/logs):**
  - Jenkins Job URL: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/`
  - Jenkins Build #2 URL: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/2/`
  - Jenkins Test Report URL: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/2/testReport/`
  - Archived WAR Artifact on disk: `C:\ProgramData\Jenkins\.jenkins\jobs\Mobile-Plan-Recommendation-CI\builds\2\archive\target\containerized-mobile-plan-recommendation-platform.war` (Size: 58,208,477 bytes)
  - GitHub Pull Request #3: `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/3`
  - CI Architecture Guide: `docs/ci-configuration.md`

- **Git branch / commit / tag / PR (if applicable):**
  - Feature branch: `feature/7-jenkins-ci-job`
  - PR #3: `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/3` (Merged)
  - Integration commit: `209427d` on `develop` and `main`

- **Manual steps still required:**
  - Optional for local Jenkins behind NAT: If real-time GitHub webhook push notifications are desired instead of SCM polling (`H/15 * * * *`), an ngrok/cloudflared tunnel forwarding to `http://localhost:8080/github-webhook/` can be configured in GitHub repository Settings $\rightarrow$ Webhooks. SCM polling is already active as an automatic fallback.

- **Known issues or risks:**
  - None. Jenkins build executed cleanly; port collision restrictions were strictly adhered to.

- **How to verify the result:**
  1. Open browser and navigate to `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/`.
  2. Inspect Build #2 to view the green status badge, stage view, and test result trend.
  3. Click **Test Result** to verify 30 passed tests across all packages.
  4. Inspect the **Build Artifacts** section to download the archived `containerized-mobile-plan-recommendation-platform.war`.

- **Next task / handoff notes:**
  - Proceed to Task 8: Pipeline as Code and Server Deployment (`Jenkinsfile` declarative pipeline with parameterized settings, Tomcat 10.1 deployment on port 8084, and `/actuator/health` verification).
