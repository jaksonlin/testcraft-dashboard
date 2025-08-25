package com.example.annotationextractor.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Utility class for testing database connections from the command line
 */
public class DatabaseConnectionTester {
    
    public static void main(String[] args) {
        System.out.println("Database Connection Tester");
        System.out.println("==========================");
        
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            printUsage();
            return;
        }
        
        // Parse database connection parameters
        String host = "localhost";
        String port = "5432";
        String database = "test_analytics";
        String username = "postgres";
        String password = "postgres";
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--host") && i + 1 < args.length) {
                host = args[++i];
            } else if (args[i].equals("--port") && i + 1 < args.length) {
                port = args[++i];
            } else if (args[i].equals("--db") && i + 1 < args.length) {
                database = args[++i];
            } else if (args[i].equals("--user") && i + 1 < args.length) {
                username = args[++i];
            } else if (args[i].equals("--pass") && i + 1 < args.length) {
                password = args[++i];
            }
        }
        
        System.out.println("Testing connection to:");
        System.out.println("  Host: " + host);
        System.out.println("  Port: " + port);
        System.out.println("  Database: " + database);
        System.out.println("  Username: " + username);
        System.out.println();
        
        try {
            // Test the connection
            DatabaseConfig.initializeFromCli(host, port, database, username, password);
            
            // Try to get a connection and metadata
            try (Connection conn = DatabaseConfig.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                
                System.out.println("✅ Connection successful!");
                System.out.println("Database: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
                System.out.println("Driver: " + metaData.getDriverName() + " " + metaData.getDriverVersion());
                System.out.println("URL: " + metaData.getURL());
                
                // Test if we can create a simple query
                try (var stmt = conn.createStatement()) {
                    var rs = stmt.executeQuery("SELECT version()");
                    if (rs.next()) {
                        System.out.println("PostgreSQL Version: " + rs.getString(1));
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("❌ Error getting connection: " + e.getMessage());
                System.exit(1);
            }
            
            // Test connection pool health
            System.out.println("\nConnection Pool Status:");
            System.out.println(DatabaseConfig.getPoolStats());
            
            System.out.println("\n✅ All tests passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            DatabaseConfig.close();
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage: java DatabaseConnectionTester [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --host <host>     Database host (default: localhost)");
        System.out.println("  --port <port>     Database port (default: 5432)");
        System.out.println("  --db <name>       Database name (default: test_analytics)");
        System.out.println("  --user <user>     Database username (default: postgres)");
        System.out.println("  --pass <pass>     Database password (default: postgres)");
        System.out.println("  --help, -h        Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java DatabaseConnectionTester");
        System.out.println("  java DatabaseConnectionTester --host mydb.example.com --port 5433");
        System.out.println("  java DatabaseConnectionTester --host localhost --db production_db --user myuser --pass mypass");
        System.out.println();
        System.out.println("Note: Parameters not specified will use defaults or values from database.properties");
    }
}
