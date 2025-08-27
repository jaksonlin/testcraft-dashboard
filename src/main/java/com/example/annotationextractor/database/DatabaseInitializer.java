package com.example.annotationextractor.database;

/**
 * Simple database initializer that can be run to set up the database schema
 */
public class DatabaseInitializer {
    
    public static void main(String[] args) {
        System.out.println("Database Schema Initializer");
        System.out.println("============================");
        
        try {
            // Check if schema already exists
            if (DatabaseSchemaManager.schemaExists()) {
                System.out.println("✅ Database schema already exists");
                System.out.println("Checking if team tables need to be added...");
                
                // Initialize schema (this will add missing tables/columns)
                DatabaseSchemaManager.initializeSchema();
                System.out.println("✅ Database schema updated successfully");
            } else {
                System.out.println("🔄 Creating new database schema...");
                DatabaseSchemaManager.initializeSchema();
                System.out.println("✅ Database schema created successfully");
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
