package com.example.annotationextractor.config;

import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.application.DailyMetricQueryService;
import com.example.annotationextractor.database.DatabaseConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Spring configuration for database-related beans
 * Integrates existing JDBC-based database layer with Spring Boot
 * This configuration is optional and won't prevent startup if database is unavailable
 */
@Configuration
@ConditionalOnProperty(name = "testcraft.database.enabled", havingValue = "true", matchIfMissing = false)
public class SpringDatabaseConfig {

    /**
     * Create PersistenceReadFacade as a Spring bean
     * This allows Spring to inject it into controllers
     */
    @Bean
    public PersistenceReadFacade persistenceReadFacade() {
        try {
            // Initialize the existing database configuration
            DatabaseConfig.initialize();
            return new PersistenceReadFacade();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PersistenceReadFacade", e);
        }
    }

    /**
     * Create DataSource bean for Spring Boot to use
     * This integrates with your existing HikariCP setup
     */
    @Bean
    public DataSource dataSource() {
        try {
            // Initialize the existing database configuration
            DatabaseConfig.initialize();
            // Return the existing HikariCP DataSource
            return DatabaseConfig.getDataSource();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DataSource", e);
        }
    }

    /**
     * Create DailyMetricQueryService as a Spring bean
     * This allows Spring to inject it into analytics services
     */
    @Bean
    public DailyMetricQueryService dailyMetricQueryService() {
        try {
            // Initialize the existing database configuration
            DatabaseConfig.initialize();
            return new DailyMetricQueryService(new com.example.annotationextractor.adapters.persistence.jdbc.JdbcDailyMetricAdapter());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DailyMetricQueryService", e);
        }
    }
}