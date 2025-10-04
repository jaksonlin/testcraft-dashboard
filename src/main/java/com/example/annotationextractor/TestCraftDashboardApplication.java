package com.example.annotationextractor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application for TestCraft Dashboard
 * 
 * Features:
 * - Continuous Git repository monitoring
 * - Test analytics dashboard
 * - REST API for frontend consumption
 * - Scheduled scanning service
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class TestCraftDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestCraftDashboardApplication.class, args);
    }
}
