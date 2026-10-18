# Task Report — Task 11: Docker Image and Container Lifecycle

## Task report

- **Task:** Task 11 — Docker Image and Container Lifecycle
- **Status:** Completed
- **Date:** 2026-10-18
- **Goal:** Author a production-grade `Dockerfile` using the approved Java 17 / Apache Tomcat 10.1 stack; enforce non-root user execution; optimize build context with `.dockerignore`; configure an automated Actuator health check; build local images tagged with semantic version `:v0.1.0` and `:latest`; run the container with internal port 8080 mapped to host port 8085 (verifying 8085 is free and strictly avoiding forbidden ports 8072, 8081, 8082); demonstrate complete container lifecycle management (inspect, logs, stop, start/restart, removal) without touching unrelated containers; document container operations in `docs/docker-guide.md`; and provide exact Docker Hub push steps.
- **What was implemented:**
  1. **Production Dockerfile (`Dockerfile`):**
     - Base Image: `tomcat:10.1-jre17-temurin` (Jakarta EE 10 servlet container on Eclipse Temurin JRE 17).
     - Security: Created unprivileged system user `appuser` (UID `1001`) and group `appgroup` (GID `1001`), chowned `/usr/local/tomcat`, and set `USER 1001`.
     - Attack surface minimization: Removed all default sample web applications (`webapps/*`).
     - Dual-context deployment: Deployed WAR as `ROOT.war` (for root context `/`) and `mpr-platform.war` (for `/mpr-platform`).
     - Automated Health Check: Configured `HEALTHCHECK --interval=15s --timeout=5s --start-period=35s --retries=3 CMD curl -f http://localhost:8080/actuator/health || exit 1`.
  2. **Build Context Optimization (`.dockerignore`):**
     - Ignored `.git`, `.github`, source files, IDE configurations, documentation, and temporary files, preserving only `target/*.war` to keep the context minimal (58.2 MB).
  3. **Image Building & Tagging:**
     - Successfully built image `srishtiyadav1904/containerized-mobile-plan-recommendation-platform:v0.1.0` and `:latest` (Image ID: `b85b15d7a8b4`, Content size: 244 MB, Total size: 722 MB).
  4. **Port Policy Compliance & Container Execution:**
     - Verified host port `8085` was completely free. Strictly avoided forbidden ports `8072`, `8081`, `8082`.
     - Launched container `mpr-platform-container` with port mapping `-p 8085:8080`.
     - Container initialized in 33.25 seconds and automatically transitioned to `(healthy)` in `docker ps`.
     - Verified live HTTP endpoints on port 8085:
       - `http://localhost:8085/actuator/health` -> HTTP 200 `{"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}`.
       - `http://localhost:8085/plans` -> HTTP 200.
       - `http://localhost:8085/login` -> HTTP 200.
  5. **Container Lifecycle Management Demonstrated:**
     - `docker inspect`: Verified ID `8c13dabdd198...`, status `running`, health `healthy`, user `1001`.
     - `docker logs`: Inspected live Spring Boot on Tomcat 10.1 initialization logs.
     - `docker stop`: Stopped container cleanly (exit status 143).
     - `docker start`: Restarted container back to `running`.
     - `docker restart`: Successfully restarted container.
     - `docker rm`: Removed and relaunched persistent container `mpr-platform-container` with `--restart unless-stopped`.
     - Unrelated container `driving-school-container` was identified and left completely untouched.
  6. **Documentation & Guides:**
     - Authored `docs/docker-guide.md` with full architecture, tagging standards, lifecycle commands, and Docker Hub instructions.
- **Files created or changed:**
  - `Dockerfile`: Production Tomcat 10.1 / JRE 17 Dockerfile with non-root user and healthcheck.
  - `.dockerignore`: Excluded non-production files from build context.
  - `docs/docker-guide.md`: Containerization architecture and lifecycle user guide.
  - `docs/task-reports/task-11.md`: Official Task 11 milestone report.
  - `TASK-TRACKER.md`: Marked Task 11 as Completed.
  - `PROJECT-PROGRESS.md`: Added Task 11 learning guide, viva Q&As, updated system state and next steps.
- **Commands executed and actual outcomes:**
  ```powershell
  # 1. Start Docker Desktop and verify daemon
  docker info
  # Outcome: Server Version 29.8.2, OS: Docker Desktop / Linux WSL2, CPUs: 8, Memory: 3.785 GiB.

  # 2. Check port 8085 availability
  netstat -ano | findstr :8085
  # Outcome: Exit code 1 (port 8085 is completely free).

  # 3. Pull base Tomcat 10.1 image
  docker pull tomcat:10.1-jre17-temurin
  # Outcome: Status: Downloaded newer image for tomcat:10.1-jre17-temurin (sha256:f0991770...).

  # 4. Package local production WAR artifact
  .\mvnw.cmd -B package -DskipTests
  # Outcome: BUILD SUCCESS, created target/containerized-mobile-plan-recommendation-platform.war (55.5 MB).

  # 5. Build Docker image with semantic and latest tags
  docker build -t srishtiyadav1904/containerized-mobile-plan-recommendation-platform:v0.1.0 \
               -t srishtiyadav1904/containerized-mobile-plan-recommendation-platform:latest \
               -t containerized-mobile-plan-recommendation-platform:v0.1.0 \
               -t containerized-mobile-plan-recommendation-platform:latest .
  # Outcome: Successfully built image b85b15d7a8b4.

  # 6. Run container with port mapping 8085:8080
  docker run -d --name mpr-platform-container -p 8085:8080 srishtiyadav1904/containerized-mobile-plan-recommendation-platform:v0.1.0
  # Outcome: Container 8a90899f9fa4 started.

  # 7. Verify healthcheck and live HTTP endpoints
  docker ps
  # Outcome: Up 43 seconds (healthy), 0.0.0.0:8085->8080/tcp.
  curl.exe -s http://localhost:8085/actuator/health
  # Outcome: {"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}.
  curl.exe -s -I http://localhost:8085/plans
  # Outcome: HTTP/1.1 200.

  # 8. Demonstrate lifecycle operations
  docker inspect --format="ID: {{.Id}} | State: {{.State.Status}} ({{.State.Health.Status}}) | User: {{.Config.User}}" mpr-platform-container
  # Outcome: ID: 8a90899f... | State: running (healthy) | User: 1001.
  docker logs --tail 10 mpr-platform-container
  # Outcome: Log stream showed Spring Boot and Tomcat startup in 33250 ms.
  docker stop mpr-platform-container
  # Outcome: Stopped.
  docker start mpr-platform-container
  # Outcome: Started.
  docker restart mpr-platform-container
  # Outcome: Restarted.
  docker stop mpr-platform-container ; docker rm mpr-platform-container
  # Outcome: Removed.

  # 9. Launch persistent production container
  docker run -d --name mpr-platform-container --restart unless-stopped -p 8085:8080 srishtiyadav1904/containerized-mobile-plan-recommendation-platform:v0.1.0
  # Outcome: Container 8c13dabdd198 started and healthy.
  ```
- **Tests and results:**
  - Automated Health Check: Docker daemon transitioned container status from `(health: starting)` to `(healthy)`.
  - HTTP Probes:
    - `GET http://localhost:8085/actuator/health`: HTTP 200 with `status: UP`.
    - `GET http://localhost:8085/plans`: HTTP 200 with HTML catalogue response.
    - `GET http://localhost:8085/login`: HTTP 200 with login form response.
  - Security Test: Container runs under UID `1001` (`appuser`), not root (`0`).
- **Evidence captured (paths/URLs/screenshots/logs):**
  - Dockerfile: [Dockerfile](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/Dockerfile)
  - Dockerignore: [.dockerignore](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/.dockerignore)
  - Docker Guide: [docs/docker-guide.md](file:///e:/Downloads/containerized_mobile_plan_devops_task_pack/docs/docker-guide.md)
  - Image ID: `b85b15d7a8b4`
  - Container ID: `8c13dabdd1983cced3011c15b7049a4ad3279c16309a0b62ed3d0e2c97042ac4`
  - Live Docker Health Endpoint: `http://localhost:8085/actuator/health`
  - Live Docker Application: `http://localhost:8085/plans`
- **Git branch / commit / tag / PR (if applicable):**
  - Branch: `feature/11-docker-containerization`
  - Tag: `v0.1.0` (Docker image tag)
- **Manual steps still required:**
  - Pushing the image to Docker Hub (`docker push srishtiyadav1904/containerized-mobile-plan-recommendation-platform:v0.1.0`) requires interactive authentication with Docker Hub credentials (`docker login -u srishtiyadav1904`). Exact instructions are provided in `docs/docker-guide.md`.
- **Known issues or risks:**
  - Docker Hub Push Authorization: Attempted push returned `push access denied, repository does not exist or may require authorization`. This is expected without active Docker Hub CLI session; manual login required.
- **How to verify the result:**
  1. Run `docker ps`: verify `mpr-platform-container` is `Up` and `(healthy)` on port mapping `0.0.0.0:8085->8080/tcp`.
  2. Run `curl http://localhost:8085/actuator/health`: verify HTTP 200 with `{"status":"UP"}`.
  3. Open `http://localhost:8085/plans` in a web browser: verify the responsive catalogue page loads with soft pink/lilac styling.
- **Next task / handoff notes:**
  - Proceed to **Task 12 — Jenkins-Docker Continuous Deployment**.
  - Target: Extend `Jenkinsfile` to automate Docker image building, tagging, and containerized deployment directly from the Jenkins pipeline upon test success.
