# Docker Containerization & Lifecycle Guide

## Containerized Mobile Plan Recommendation Platform

### Overview
This document details the containerization architecture, security model, image tagging policy, and container lifecycle operations for the Containerized Mobile Plan Recommendation Platform.

---

### 1. Container Architecture & Base Image
- **Base Image:** `tomcat:10.1-jre17-temurin`
  - Apache Tomcat 10.1 provides native Jakarta EE 10 servlet container support.
  - Eclipse Temurin OpenJDK 17 provides an officially supported Long-Term Support (LTS) Java runtime.
- **Artifact:** Web Application Archive (`target/containerized-mobile-plan-recommendation-platform.war`).
- **Deployment Strategy:**
  - Deployed as `/usr/local/tomcat/webapps/ROOT.war` (accessible at `http://localhost:8085/`).
  - Deployed as `/usr/local/tomcat/webapps/mpr-platform.war` (accessible at `http://localhost:8085/mpr-platform/`).
- **Internal Container Port:** `8080`.
- **Host Port Binding:** `8085` (strictly avoiding forbidden ports `8072`, `8081`, `8082`).

---

### 2. Security Best Practices
- **Non-Root Execution:**
  - Unprivileged user `appuser` (UID `1001`) and group `appgroup` (GID `1001`) created inside the container.
  - Ownership of `/usr/local/tomcat` assigned to `appuser:appgroup`.
  - Enforced via `USER 1001` in `Dockerfile`.
- **Sample App Removal:**
  - Default Tomcat sample applications (`docs`, `examples`, `manager`, `host-manager`, `ROOT`) removed to minimize attack surface.
- **Build Context Optimization:**
  - `.dockerignore` excludes `.git`, source files, test classes, IDE folders, and temporary files, keeping the build context minimal.
- **No Embedded Credentials:**
  - Credentials and runtime parameters are passed via environment variables (`DB_URL`, `DB_USER`, `DB_PASS`).

---

### 3. Image Tagging Policy
Images are tagged following semantic versioning:
1. **Semantic Release Tag:** `srishtiyadav1904/containerized-mobile-plan-recommendation-platform:v0.1.0`
2. **Latest Tag:** `srishtiyadav1904/containerized-mobile-plan-recommendation-platform:latest`
3. **Local Development Tags:** `containerized-mobile-plan-recommendation-platform:v0.1.0` and `containerized-mobile-plan-recommendation-platform:latest`

---

### 4. Automated Health Check
The Dockerfile configures an automated container health probe:
```dockerfile
HEALTHCHECK --interval=15s --timeout=5s --start-period=35s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```
- Probes Spring Boot Actuator endpoint.
- Marks container state as `(healthy)` when the JVM, database connection pool, and servlet container are ready.

---

### 5. Container Lifecycle Commands

#### Build Image
```bash
docker build -t srishtiyadav1904/containerized-mobile-plan-recommendation-platform:v0.1.0 \
             -t srishtiyadav1904/containerized-mobile-plan-recommendation-platform:latest .
```

#### Run Container
```bash
# Verify host port 8085 is free
netstat -ano | findstr :8085

# Start container
docker run -d \
  --name mpr-platform-container \
  --restart unless-stopped \
  -p 8085:8080 \
  srishtiyadav1904/containerized-mobile-plan-recommendation-platform:v0.1.0
```

#### Inspect Container State & Health
```bash
docker inspect mpr-platform-container
docker inspect --format="Status: {{.State.Status}} | Health: {{.State.Health.Status}} | User: {{.Config.User}}" mpr-platform-container
```

#### View Live Logs
```bash
docker logs -f mpr-platform-container
```

#### Stop Container
```bash
docker stop mpr-platform-container
```

#### Restart Container
```bash
docker start mpr-platform-container
# OR
docker restart mpr-platform-container
```

#### Remove Container
```bash
docker stop mpr-platform-container
docker rm mpr-platform-container
```

---

### 6. Docker Hub Authentication & Push Instructions
To push the verified image to Docker Hub under `srishtiyadav1904`:
```bash
# 1. Authenticate with Docker Hub
docker login -u srishtiyadav1904

# 2. Push semantic versioned image
docker push srishtiyadav1904/containerized-mobile-plan-recommendation-platform:v0.1.0

# 3. Push latest image
docker push srishtiyadav1904/containerized-mobile-plan-recommendation-platform:latest
```
