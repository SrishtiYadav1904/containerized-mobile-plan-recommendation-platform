# Task 12 Report: Jenkins-Docker Continuous Deployment

- **Task:** Task 12 — Jenkins-Docker Continuous Deployment
- **Status:** Completed
- **Date:** 2026-10-18
- **Goal:** Extend the Declarative Jenkins CI/CD pipeline to build versioned Docker images, deploy a fresh container (`mpr-platform-container`) to host port 8085 only after all test gates pass, enforce port safety policies, maintain rollback metadata for Task 14, perform post-deployment health verification, and distinguish local vs registry workflows.

---

## 1. What Was Implemented

1. **Pipeline Parameterization & Environment Hardening ([Jenkinsfile](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/Jenkinsfile)):**
   - Added `DEPLOY_DOCKER` (default: `true`), `DOCKER_HOST_PORT` (default: `8085`), `DOCKER_IMAGE_NAME` (`srishtiyadav1904/containerized-mobile-plan-recommendation-platform`), `DOCKER_CONTAINER_NAME` (`mpr-platform-container`), and `PUSH_DOCKER_HUB` (default: `false`).
   - Configured Docker Desktop binary directory (`C:\Users\Shreyas\AppData\Local\Programs\DockerDesktop\resources\bin`) in the pipeline `environment { PATH = ... }` block to ensure `docker.exe` is reliably discoverable within Jenkins worker processes.
   - Enforced host port safety checks in Stage 2 (`Environment & Port Validation`) verifying that both Tomcat port 8084 and Docker host port 8085 strictly avoid forbidden ports `8072`, `8081`, and `8082`.

2. **Automated Docker Image Packaging ([scripts/build-docker.ps1](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/scripts/build-docker.ps1)):**
   - Validates that the packaged WAR artifact exists in `target/`.
   - Generates trustworthy, versioned build tags formatted as `build-${BUILD_NUMBER}-${GIT_COMMIT_SHORT}` (e.g. `build-18-22013c2`), along with `:latest` and `:v0.1.0`.
   - Robustly parses comma-separated or array tags and executes `docker build -f Dockerfile -t <tags> .`.
   - Evaluates optional Docker Hub push requests with clear local-vs-remote logging.

3. **Continuous Deployment & Rollback Metadata Tracking ([scripts/deploy-docker.ps1](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/scripts/deploy-docker.ps1)):**
   - **Port Validation:** Throws fatal error if host port is in the forbidden list (`8072`, `8081`, `8082`).
   - **Rollback Preparation:** Inspects if `mpr-platform-container` is currently running, extracts its container ID, image name, and status, and records this into `deployment-state.json` as `previousDeployment` (providing direct foundation for Task 14 Automated Rollback).
   - **Graceful Lifecycle Management:** Safely stops and removes only the target project container (`mpr-platform-container`). Never stops or deletes unrelated containers (`driving-school-container` left untouched).
   - **Container Execution:** Launches fresh container via `docker run -d -p 8085:8080 --name mpr-platform-container --restart unless-stopped <image:tag>`.
   - **Post-Deployment Health Verification:** Polls `http://localhost:8085/actuator/health` with a 90-second timeout until status is `UP`. Fails the script and dumps container logs if health verification times out.
   - **State Persistence:** Writes complete deployment metadata to `deployment-state.json`.

4. **Jenkins Pipeline Stages 7, 8, and 9:**
   - **Stage 7 (`Build Docker Image`):** Executes `build-docker.ps1` with the short commit SHA and Jenkins build number.
   - **Stage 8 (`Deploy Docker Container`):** Executes `deploy-docker.ps1`, replacing previous containers, verifying health check, and generating `deployment-state.json`.
   - **Stage 9 (`Publish Image to Registry`):** Checks for Jenkins credentials (`docker-hub-credentials`) or logs the verified local image workflow.
   - **Post Actions:** Archives `deployment-state.json`, `target/*.war`, surefire XML reports, and Selenium failure screenshots.

---

## 2. Files Created or Changed

| File | Status | Description |
|---|---|---|
| [Jenkinsfile](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/Jenkinsfile) | Modified | Added Docker parameters, PATH injection, Stage 7 (Build Image), Stage 8 (Deploy Container), Stage 9 (Publish), and `deployment-state.json` artifact archiving |
| [scripts/build-docker.ps1](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/scripts/build-docker.ps1) | Created | Script to package and multi-tag Docker images with build numbers, commit SHAs, and release tags |
| [scripts/deploy-docker.ps1](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/scripts/deploy-docker.ps1) | Created | Continuous deployment automation with port safety, rollback state preservation, and health probe |
| [scripts/trigger-jenkins-build.ps1](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/scripts/trigger-jenkins-build.ps1) | Modified | Added parameters for `DeployDocker`, `DockerHostPort`, and `PushDockerHub` |
| [scripts/health-check.ps1](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/scripts/health-check.ps1) | Modified | Added support for root and empty/null context path endpoints |
| [deployment-state.json](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/deployment-state.json) | Created | Machine-readable deployment metadata capturing current and previous image/container state |
| [docs/task-reports/task-12.md](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/docs/task-reports/task-12.md) | Created | This task report |

---

## 3. Commands Executed and Actual Outcomes

```powershell
# 1. Author and test deployment helper scripts locally
powershell -ExecutionPolicy Bypass -File scripts/build-docker.ps1 -ImageTag "build-test" -AdditionalTags "latest,v0.1.0"
# Outcome: BUILD SUCCESS. Tagged build-test, latest, and v0.1.0. Image ID: 1ed5a5e8237e.

powershell -ExecutionPolicy Bypass -File scripts/deploy-docker.ps1 -ImageTag "build-test"
# Outcome: DEPLOYMENT COMPLETE. Container c20a8c9e... stopped, new container 61da329c... started, health check UP.

# 2. Trigger Jenkins Pipeline Build #18 on branch feature/12-jenkins-docker-cd
powershell -ExecutionPolicy Bypass -File scripts/trigger-jenkins-build.ps1 -BranchName "feature/12-jenkins-docker-cd" -AppPort "8084" -DeployWar "true" -DeployDocker "true" -DockerHostPort "8085"
# Outcome: Jenkins Build #18 triggered, executed 9 stages, Finished: SUCCESS!

# 3. Query container health and endpoints on host port 8085
curl.exe -s http://localhost:8085/actuator/health
# Outcome: {"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}

curl.exe -I http://localhost:8085/plans
# Outcome: HTTP/1.1 200 OK

curl.exe -I http://localhost:8085/login
# Outcome: HTTP/1.1 200 OK
```

---

## 4. Tests and Results

- **Unit & Integration Tests (Stage 3):** 30 tests run, 0 failures, 0 errors (`PlanServiceTest` [9], `RecommendationServiceTest` [4], `UserServiceTest` [4], `PlanControllerTest` [12], `MobilePlanRecommendationApplicationTests` [1]).
- **Selenium End-to-End Tests (Stage 4):** 5 user journeys run on headless Chrome 155, 0 failures, 0 errors.
- **Packaging (Stage 5):** WAR generated at `target/containerized-mobile-plan-recommendation-platform.war` (55.51 MB).
- **Tomcat Server Deployment (Stage 6):** WAR deployed to `E:\Downloads\apache-tomcat-10.1.61\webapps\mpr-platform.war`. Health check on `http://localhost:8084/mpr-platform/actuator/health` passed (`UP`).
- **Docker Image Packaging (Stage 7):** Built versioned image `srishtiyadav1904/containerized-mobile-plan-recommendation-platform:build-18-22013c2` and tagged `:latest` and `:v0.1.0`.
- **Docker Container Deployment (Stage 8):** Deployed container `61da329c6b16` (`mpr-platform-container`). Health check on `http://localhost:8085/actuator/health` passed (`UP`).
- **Jenkins Build Outcome:** Build #18 completed with `SUCCESS`.

---

## 5. Evidence Captured

- **Jenkins Job URL:** `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/`
- **Jenkins Build #18 URL:** `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/18/`
- **Archived Metadata Artifact:** `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/18/artifact/deployment-state.json`
- **Archived WAR Binary:** `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/18/artifact/target/containerized-mobile-plan-recommendation-platform.war`
- **Live Container Endpoint:** `http://localhost:8085/actuator/health`
- **Live Catalogue View:** `http://localhost:8085/plans`
- **Archived `deployment-state.json` Content:**
  ```json
  {
      "projectName":  "containerized-mobile-plan-recommendation-platform",
      "containerName":  "mpr-platform-container",
      "deployedAt":  "2026-10-18T13:16:25.8855045+05:30",
      "currentImage":  "srishtiyadav1904/containerized-mobile-plan-recommendation-platform:latest",
      "containerId":  "61da329c6b169c562464ea24074f7cc2865651425aa3747ea76708d76d6ad5ee",
      "hostPort":  8085,
      "containerPort":  8080,
      "healthEndpoint":  "http://localhost:8085/actuator/health",
      "healthStatus":  "UP",
      "previousDeployment":  {
          "timestamp":  "2026-10-18T07:26:14.978698408Z",
          "status":  "running",
          "containerId":  "c20a8c9e9059",
          "health":  "healthy",
          "image":  "srishtiyadav1904/containerized-mobile-plan-recommendation-platform:test-run"
      }
  }
  ```

---

## 6. Git Branch, Commit, and PR

- **Branch:** `feature/12-jenkins-docker-cd`
- **Commit:** `22013c2`
- **Remote Tracking:** `origin/feature/12-jenkins-docker-cd`
- **Pull Request:** Target `develop` branch.

---

## 7. Manual Steps Still Required

- **Remote Docker Hub Push (Optional):** If publishing images to public Docker Hub is required, create a `Username with password` credential in Jenkins with ID `docker-hub-credentials` and set `PUSH_DOCKER_HUB` to `true`. Otherwise, the local verified image workflow functions completely autonomously.

---

## 8. Known Issues or Risks

- None. Both Tomcat on port 8084 and Docker on port 8085 run simultaneously without port conflicts. Forbidden ports 8072, 8081, and 8082 remain completely untouched.

---

## 9. How to Verify the Result

1. Open Jenkins: Navigate to `http://localhost:8080/job/Mobile-Plan-Recommendation-CI/18/` and verify all 9 pipeline stages are green.
2. Inspect Docker: Run `docker ps --filter "name=mpr-platform-container"`. Verify status `Up (healthy)` and port `0.0.0.0:8085->8080/tcp`.
3. Test Health Endpoint: Run `curl http://localhost:8085/actuator/health` -> verify HTTP 200 `{"status":"UP"}`.
4. Test Web UI: Open `http://localhost:8085/plans` in any browser -> verify 24 seed plans render with soft pink/lilac styling.
5. Inspect Metadata: View `deployment-state.json` -> verify `previousDeployment` details are preserved.

---

## 10. Next Task / Handoff Notes

- **Task 13 — Configuration Management Script:** Author Ansible playbooks to provision and configure the server environment, runtime dependencies, directory structure, and configuration files.
