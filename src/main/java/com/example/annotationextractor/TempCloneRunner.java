package com.example.annotationextractor;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.database.DatabaseSchemaManager;
import com.example.annotationextractor.database.DataPersistenceService;
import com.example.annotationextractor.reporting.ExcelReportGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for temporary cloning operations
 * Clones repositories one by one, scans them, and deletes them to save disk space
 */
public class TempCloneRunner {
    
    public static void main(String[] args) {
        System.out.println("Temporary Clone Runner");
        System.out.println("======================");
        
        if (args.length < 2) {
            printUsage();
            return;
        }
        
        String repositoryHubPath = args[0];
        String repositoryListPath = args[1];
        String username = null;
        String password = null;
        String sshKeyPath = null;
        
        // Database connection parameters
        String dbHost = null;
        String dbPort = null;
        String dbName = null;
        String dbUser = null;
        String dbPass = null;
        
        // Parse arguments
        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("--db-host") && i + 1 < args.length) {
                dbHost = args[++i];
            } else if (args[i].equals("--db-port") && i + 1 < args.length) {
                dbPort = args[++i];
            } else if (args[i].equals("--db-name") && i + 1 < args.length) {
                dbName = args[++i];
            } else if (args[i].equals("--db-user") && i + 1 < args.length) {
                dbUser = args[++i];
            } else if (args[i].equals("--db-pass") && i + 1 < args.length) {
                dbPass = args[++i];
            } else if (sshKeyPath == null && !args[i].startsWith("--")) {
                // First non-flag argument is username, second is password, third is ssh key
                if (username == null) {
                    username = args[i];
                } else if (password == null) {
                    password = args[i];
                } else if (sshKeyPath == null) {
                    sshKeyPath = args[i];
                }
            }
        }
        
        try {
            // Initialize database connection if CLI parameters are provided
            if (dbHost != null || dbPort != null || dbName != null || dbUser != null || dbPass != null) {
                String finalDbHost = dbHost != null ? dbHost : "localhost";
                String finalDbPort = dbPort != null ? dbPort : "5432";
                String finalDbName = dbName != null ? dbName : "test_analytics";
                String finalDbUser = dbUser != null ? dbUser : "postgres";
                String finalDbPass = dbPass != null ? dbPass : "postgres";
                
                System.out.println("Initializing database connection with CLI parameters...");
                DatabaseConfig.initializeFromCli(
                    finalDbHost, finalDbPort, finalDbName, finalDbUser, finalDbPass
                );
            } else {
                System.out.println("Initializing database connection from properties file...");
                DatabaseConfig.initialize();
            }
            
            // Create scanner with temporary clone mode enabled
            RepositoryHubScanner scanner;
            if (sshKeyPath != null) {
                scanner = new RepositoryHubScanner(repositoryHubPath, repositoryListPath, username, password, sshKeyPath);
                System.out.println("Using SSH authentication with key: " + sshKeyPath);
            } else if (username != null && password != null) {
                scanner = new RepositoryHubScanner(repositoryHubPath, repositoryListPath, username, password);
                System.out.println("Using HTTPS authentication for user: " + username);
            } else {
                scanner = new RepositoryHubScanner(repositoryHubPath, repositoryListPath);
                System.out.println("No authentication - cloning public repositories only");
            }
            
            // Enable temporary clone mode
            scanner.setTempCloneMode(true);
            System.out.println("Temporary clone mode enabled - repositories will be deleted after scanning to save disk space");
            
            // Execute the scan
            boolean success = scanner.executeFullScan();
            
            if (success) {
                System.exit(0);
            } else {
                System.exit(1);
            }
            
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage: java TempCloneRunner <repository_hub_path> <repository_list_file> [username] [password] [ssh_key_path] [database_options]");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  repository_hub_path    Directory where repositories will be temporarily cloned");
        System.out.println("  repository_list_file  Text file containing git repository URLs (one per line)");
        System.out.println("  username             Git username for private repositories (optional)");
        System.out.println("  password             Git password/token for private repositories (optional)");
        System.out.println("  ssh_key_path         Path to SSH private key for SSH authentication (optional)");
        System.out.println();
        System.out.println("Database Options (override database.properties):");
        System.out.println("  --db-host <host>     Database host (default: localhost)");
        System.out.println("  --db-port <port>     Database port (default: 5432)");
        System.out.println("  --db-name <name>     Database name (default: test_analytics)");
        System.out.println("  --db-user <user>     Database username (default: postgres)");
        System.out.println("  --db-pass <pass>     Database password (default: postgres)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java TempCloneRunner ./temp-repos ./repo-list.txt");
        System.out.println("  java TempCloneRunner ./temp-repos ./repo-list.txt myuser mytoken");
        System.out.println("  java TempCloneRunner ./temp-repos ./repo-list.txt --db-host localhost --db-name test_db");
        System.out.println();
        System.out.println("Note: This utility clones repositories one by one, scans each, and deletes it immediately");
        System.out.println("      to save disk space. Useful for large repositories or limited disk space scenarios.");
    }
}
