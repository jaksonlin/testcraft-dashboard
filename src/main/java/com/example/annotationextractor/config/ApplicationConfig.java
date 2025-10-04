package com.example.annotationextractor.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration
 * Controls which components are enabled based on properties
 */
@Configuration
public class ApplicationConfig {
    
    // This configuration allows the app to start even if database is not available
    // Database integration will be optional initially
}
