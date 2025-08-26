package com.example.annotationextractor.database;

/**
 * Command-line runner for database migrations
 */
public class DatabaseMigrationRunner {
    
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: DatabaseMigrationRunner <host> <port> <database> <username> [password]");
            System.out.println("Example: DatabaseMigrationRunner localhost 5432 test_analytics postgres");
            System.exit(1);
        }
        
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String database = args[2];
        String username = args[3];
        String password = args.length > 4 ? args[4] : "";
        
        try {
            System.out.println("Database Migration Tool");
            System.out.println("======================");
            System.out.println("Host: " + host);
            System.out.println("Port: " + port);
            System.out.println("Database: " + database);
            System.out.println("Username: " + username);
            System.out.println();
            
            // Initialize database connection
            DatabaseConfig.initialize(host, port, database, username, password);
            
            // Check if migration is needed
            if (DatabaseMigrationService.isMigrationNeeded()) {
                System.out.println("Duplicate repositories detected. Starting migration...");
                System.out.println();
                
                // Run the migration
                DatabaseMigrationService.migrateToFixDuplicateRepositories();
                
                System.out.println();
                System.out.println("Migration completed successfully!");
                System.out.println("Duplicate repositories have been cleaned up.");
                
            } else {
                System.out.println("No duplicate repositories found. Migration not needed.");
            }
            
        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            DatabaseConfig.close();
        }
    }
}
