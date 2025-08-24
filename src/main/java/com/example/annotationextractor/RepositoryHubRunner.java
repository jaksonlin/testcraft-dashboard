package com.example.annotationextractor;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Main command-line interface for Repository Hub Scanner
 * Usage: java RepositoryHubRunner <repository_hub_path> <repository_list_file> [username] [password]
 */
public class RepositoryHubRunner {
    
    public static void main(String[] args) {
        System.out.println("Repository Hub Scanner");
        System.out.println("======================");
        
        if (args.length < 2) {
            printUsage();
            return;
        }
        
        String repositoryHubPath = args[0];
        String repositoryListPath = args[1];
        String username = args.length > 2 ? args[2] : null;
        String password = args.length > 3 ? args[3] : null;
        
        try {
            // Validate paths
            if (!validatePaths(repositoryHubPath, repositoryListPath)) {
                return;
            }
            
            // Create and run the scanner
            RepositoryHubScanner scanner;
            if (username != null && password != null) {
                scanner = new RepositoryHubScanner(repositoryHubPath, repositoryListPath, username, password);
            } else {
                scanner = new RepositoryHubScanner(repositoryHubPath, repositoryListPath);
            }
            
            // Execute the full scan
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
        System.out.println("Usage: java RepositoryHubRunner <repository_hub_path> <repository_list_file> [username] [password]");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  repository_hub_path    Directory where repositories will be cloned/updated");
        System.out.println("  repository_list_file  Text file containing git repository URLs (one per line)");
        System.out.println("  username             Git username for private repositories (optional)");
        System.out.println("  password             Git password/token for private repositories (optional)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java RepositoryHubRunner ./repos ./repo-list.txt");
        System.out.println("  java RepositoryHubRunner ./repos ./repo-list.txt myuser mytoken");
        System.out.println();
        System.out.println("Repository List File Format:");
        System.out.println("  # Comments start with #");
        System.out.println("  https://github.com/example/repo1.git");
        System.out.println("  https://github.com/example/repo2");
        System.out.println("  git@github.com:example/repo3.git");
    }
    
    private static boolean validatePaths(String repositoryHubPath, String repositoryListPath) {
        // Check if repository list file exists
        if (!java.nio.file.Files.exists(Paths.get(repositoryListPath))) {
            System.err.println("Error: Repository list file not found: " + repositoryListPath);
            System.err.println("Use RepositoryListProcessor.createSampleRepositoryList() to create a sample file.");
            return false;
        }
        
        // Check if repository hub path is writable
        try {
            java.nio.file.Path hubPath = Paths.get(repositoryHubPath);
            if (java.nio.file.Files.exists(hubPath)) {
                if (!java.nio.file.Files.isDirectory(hubPath)) {
                    System.err.println("Error: Repository hub path exists but is not a directory: " + repositoryHubPath);
                    return false;
                }
                if (!java.nio.file.Files.isWritable(hubPath)) {
                    System.err.println("Error: Repository hub directory is not writable: " + repositoryHubPath);
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("Error: Cannot access repository hub path: " + repositoryHubPath);
            return false;
        }
        
        return true;
    }
}
