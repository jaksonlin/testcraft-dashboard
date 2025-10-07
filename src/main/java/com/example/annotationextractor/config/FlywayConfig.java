package com.example.annotationextractor.config;

import com.example.annotationextractor.database.DatabaseConfig;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * Flyway configuration that works with the project's custom DatabaseConfig.
 * 
 * This configuration runs Flyway migrations using the existing DataSource
 * from DatabaseConfig, bypassing Spring Boot's auto-configuration which
 * doesn't work with database.properties.
 */
@Configuration
@ConditionalOnProperty(name = "testcraft.database.enabled", havingValue = "true", matchIfMissing = true)
public class FlywayConfig {
    
    /**
     * Create and configure Flyway bean that runs migrations automatically
     */
    @Bean(initMethod = "migrate")
    @DependsOn("dataSource")
    public Flyway flyway(DataSource dataSource) {
        System.out.println("🔄 Configuring Flyway for database migrations...");
        
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)  // Allow migration on existing database
            .baselineVersion("0")     // Start from version 0
            .load();
        
        System.out.println("✅ Flyway configured successfully");
        return flyway;
    }
}

