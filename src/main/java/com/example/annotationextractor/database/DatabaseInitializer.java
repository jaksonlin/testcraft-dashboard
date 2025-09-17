package com.example.annotationextractor.database;

import org.flywaydb.core.Flyway;

/**
 * Simple database initializer that uses Flyway to set up the database schema
 */
public class DatabaseInitializer {
    
    public static void main(String[] args) {
        System.out.println("Database Schema Initializer (Flyway)");
        System.out.println("====================================");
        
        try {
            // Configure and run Flyway migrations
            Flyway flyway = Flyway.configure()
                .dataSource(DatabaseConfig.getDataSource())
                .load();
            
            System.out.println("🔄 Running database migrations...");
            var migrateResult = flyway.migrate();
            
            if (migrateResult.migrationsExecuted == 0) {
                System.out.println("✅ Database is up to date - no migrations needed");
            } else {
                System.out.println("✅ Executed " + migrateResult.migrationsExecuted + " migration(s) successfully");
            }
            
            System.out.println("\nDatabase is ready for use!");
            System.out.println("You can now:");
            System.out.println("1. Run repository scans");
            System.out.println("2. Use team management features");
            System.out.println("3. Generate reports");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
