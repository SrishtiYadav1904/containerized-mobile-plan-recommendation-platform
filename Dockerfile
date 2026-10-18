# =========================================================================
# Production Dockerfile for Containerized Mobile Plan Recommendation Platform
# Runtime Environment: Apache Tomcat 10.1 (Jakarta EE 10) on Eclipse Temurin JRE 17
# Base Image: tomcat:10.1-jre17-temurin
# Security: Runs as unprivileged non-root user (appuser: 1001)
# Port: Exposes internal port 8080 (mapped to host port 8085)
# =========================================================================

FROM tomcat:10.1-jre17-temurin

LABEL maintainer="Srishti Yadav <srishtiyadav1904>" \
      project="containerized-mobile-plan-recommendation-platform" \
      version="0.1.0" \
      description="Production Tomcat 10.1 container hosting Containerized Mobile Plan Recommendation Platform"

# Clean out default sample web applications for security and minimalism
RUN rm -rf /usr/local/tomcat/webapps/*

# Create unprivileged non-root runtime user and group
RUN groupadd -r appgroup -g 1001 && \
    useradd -u 1001 -r -g appgroup -m -d /home/appuser -s /sbin/nologin appuser

# Copy the verified production WAR artifact into Tomcat's webapps directory
# Deployed as ROOT.war for root context access (http://localhost:8080/)
# and mpr-platform.war for named context access (http://localhost:8080/mpr-platform/)
COPY --chown=appuser:appgroup target/containerized-mobile-plan-recommendation-platform.war /usr/local/tomcat/webapps/ROOT.war
COPY --chown=appuser:appgroup target/containerized-mobile-plan-recommendation-platform.war /usr/local/tomcat/webapps/mpr-platform.war

# Set directory ownership so non-root user can write to work, temp, and logs
RUN chown -R appuser:appgroup /usr/local/tomcat

# Enforce non-root execution
USER 1001

# Expose default Tomcat HTTP port
EXPOSE 8080

# Configure automated container health check probing Spring Boot Actuator
HEALTHCHECK --interval=15s --timeout=5s --start-period=35s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Start Apache Tomcat in foreground mode
CMD ["catalina.sh", "run"]
