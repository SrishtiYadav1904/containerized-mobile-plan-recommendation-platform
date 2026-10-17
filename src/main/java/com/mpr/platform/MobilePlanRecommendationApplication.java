package com.mpr.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Mobile Plan Recommendation Platform
 * Main entry point supporting standalone JAR execution and Tomcat 10.1 WAR deployment.
 */
@SpringBootApplication
public class MobilePlanRecommendationApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MobilePlanRecommendationApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(MobilePlanRecommendationApplication.class, args);
    }
}
