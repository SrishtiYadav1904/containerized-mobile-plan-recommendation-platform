# Task 8 Report — Pipeline as Code and Server Deployment

## Task report

- **Task:** Task 8 — Pipeline as Code and Server Deployment
- **Status:** Completed
- **Date:** 2026-10-18
- **Goal:** Author a root-level declarative `Jenkinsfile` implementing Pipeline as Code with stages for checkout, environment/port policy validation, compile and test (fatal gate), package, archive/report, and deployment to Apache Tomcat 10.1 on port `8084`; parameterize deployment settings (`DEPLOY_ENV`, `APP_PORT`, `TOMCAT_HOME`, `DEPLOY_WAR`); strictly enforce port reservation policies avoiding forbidden ports `8072`, `8081`, and `8082`; provide complete server deployment documentation and health check procedures; accurately report server deployment as pending since standalone Tomcat is not installed on the host; and verify pipeline execution on Jenkins.

- **What was implemented:**
  1. **Root-Level `Jenkinsfile` (Declarative Pipeline as Code):**
     - Authored `Jenkinsfile` in repository root using structured declarative syntax.
     - **Configured Parameters:**
       - `DEPLOY_ENV`: Choice parameter (`local-tomcat`, `staging`, `production`), default `local-tomcat`.
       - `APP_PORT`: String parameter, default `8084`.
       - `TOMCAT_HOME`: String parameter, default `C:\apache-tomcat-10.1`.
       - `DEPLOY_WAR`: Boolean parameter, default `true`.
     - **Pipeline Stages:**
       - **Stage 1 (Checkout):** SCM checkout from `develop` branch.
       - **Stage 2 (Environment & Port Validation):** Validates Git, Java 21, and Maven wrapper. Explicitly checks `APP_PORT` against forbidden list (`8072, 8081, 8082`) and aborts with fatal error if violated.
       - **Stage 3 (Compile and Test):** Executes `mvnw.cmd -B clean test`. Test failures are fatal and prevent downstream packaging/deployment.
       - **Stage 4 (Package):** Runs `mvnw.cmd -B package -DskipTests` to produce `target/containerized-mobile-plan-recommendation-platform.war`.
       - **Stage 5 (Archive and Report):** Publishes JUnit test reports (`target/surefire-reports/*.xml`) and archives deployable WAR/JAR artifacts with fingerprinting.
       - **Stage 6 (Deploy to Server):** Safe, conditional server deployment stage. Checks if Apache Tomcat exists at `TOMCAT_HOME`. If present, copies WAR to `webapps/mpr-platform.war` and probes `/actuator/health`. If absent (current machine state), logs clear diagnostic status marking deployment as **PENDING** without failing the pipeline build, per task instructions.
       - **Post Actions:** Comprehensive summary notifications on build completion.
  2. **Server Deployment Guide (`docs/server-deployment-guide.md`):**
     - Detailed installation and configuration guide for Apache Tomcat 10.1 on Windows.
     - Step-by-step instructions for port 8084 configuration in `conf/server.xml` and shutdown port 8006 to eliminate port collisions.
     - Manager credentials setup in `conf/tomcat-users.xml`.
     - Automated and manual drop-in deployment procedures.
     - Post-deployment health verification instructions using `http://localhost:8084/mpr-platform/actuator/health`.
  3. **Jenkins SCM Job Transition:**
     - Updated Jenkins job `Mobile-Plan-Recommendation-CI` definition from inline script to `CpsScmFlowDefinition` pointing to `Jenkinsfile` on `develop`.
  4. **Verified Pipeline Execution:**
     - Executed Build #4 on Jenkins.
     - All 6 stages completed successfully.
     - Result: `SUCCESS` (Duration: ~59s).
     - Test Result: 30/30 tests passed, 0 failures.
     - Artifact archived: `containerized-mobile-plan-recommendation-platform.war`.
     - Deployment stage: Evaluated Tomcat path, accurately reported `[DEPLOYMENT STATUS: PENDING - TOMCAT NOT DETECTED]`.

- **Files created or changed:**
  - `Jenkinsfile`
  - `docs/server-deployment-guide.md`
  - `docs/jenkins-job-config.xml`
  - `docs/task-reports/task-08.md`

- **Commands executed and actual outcomes:**
  1. `git checkout -b feature/8-pipeline-server-deploy develop` -> Created feature branch.
  2. `git add Jenkinsfile docs/server-deployment-guide.md ; git commit -m "feat(pipeline): author root Jenkinsfile and server deployment guide (Task 8)"` -> Commit `a492372`.
  3. `git push -u origin feature/8-pipeline-server-deploy` -> Pushed to GitHub.
  4. `gh pr create --base develop --head feature/8-pipeline-server-deploy ...` -> Created PR #4 (`https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/4`).
  5. `gh pr merge 4 --merge` -> Merged PR #4 into `develop` (`7e34fae`).
  6. `git checkout develop ; git pull origin develop ; git checkout main ; git merge develop ; git push origin main ; git checkout develop` -> Synced `develop` and `main`.
  7. `curl.exe ... /job/Mobile-Plan-Recommendation-CI/config.xml` -> Updated job definition to SCM `Jenkinsfile`.
  8. `curl.exe ... /job/Mobile-Plan-Recommendation-CI/build` -> Triggered Build #4.
  9. `curl.exe ... /job/Mobile-Plan-Recommendation-CI/4/consoleText` -> Verified build log: all stages passed, Tomcat reported as pending.
  10. `Invoke-RestMethod ... /job/Mobile-Plan-Recommendation-CI/4/testReport/api/json` -> `totalCount: 30`, `passCount: 30`, `failCount: 0`.

- **Tests and results:**
  - Jenkins Build #4 Pipeline Execution:
    - Stage 'Checkout': SUCCESS
    - Stage 'Environment & Port Validation': SUCCESS (Port 8084 validated, ports 8072, 8081, 8082 avoided)
    - Stage 'Compile and Test': SUCCESS (30 tests executed)
    - Stage 'Package': SUCCESS (WAR generated)
    - Stage 'Archive and Report': SUCCESS (JUnit XML parsed, WAR archived)
    - Stage 'Deploy to Server': SUCCESS (Tomcat evaluated; pending status reported)
  - **Total:** 30 tests executed, 30 passed, 0 failures, 0 skipped.

- **Evidence captured (paths/URLs/screenshots/logs):**
  - Jenkins Job Overview: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/`
  - Jenkins Build #4 URL: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/4/`
  - Jenkins Build #4 Test Report: `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/4/testReport/`
  - Archived Artifact on Disk: `C:\ProgramData\Jenkins\.jenkins\jobs\Mobile-Plan-Recommendation-CI\builds\4\archive\target\containerized-mobile-plan-recommendation-platform.war`
  - GitHub Pull Request #4: `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/4`
  - Server Deployment Guide: `docs/server-deployment-guide.md`
  - Root Pipeline as Code: `Jenkinsfile`

- **Git branch / commit / tag / PR (if applicable):**
  - Feature branch: `feature/8-pipeline-server-deploy`
  - PR #4: `https://github.com/SrishtiYadav1904/containerized-mobile-plan-recommendation-platform/pull/4` (Merged)
  - Integration commit: `7e34fae` on `develop` and `main`

- **Manual steps still required:**
  - Standalone Tomcat Deployment (Optional / Pending): When a local Tomcat instance is desired, follow `docs/server-deployment-guide.md` to install Tomcat 10.1 into `C:\apache-tomcat-10.1`, set HTTP port to `8084` in `conf/server.xml`, and run `startup.bat`. The pipeline will then deploy automatically to `webapps/mpr-platform.war` on the next build. Otherwise, proceed to Task 11 for Docker container deployment on port `8085`.

- **Known issues or risks:**
  - None. Port allocation rules were strictly enforced, avoiding ports 8072, 8081, and 8082.

- **How to verify the result:**
  1. Open browser to `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/4/`.
  2. View the declarative pipeline stage view showing green badges across all stages.
  3. Verify the console log shows port 8084 policy validation and successful test/package execution.
  4. Inspect the archived WAR artifact and test results (30/30 passed).
  5. Inspect the root `Jenkinsfile` and `docs/server-deployment-guide.md` in the repository.

- **Next task / handoff notes:**
  - Proceed to Task 9: Selenium Test Design and Local Execution (designing automated UI tests with Selenium WebDriver for analyst plan creation and customer recommendations).
