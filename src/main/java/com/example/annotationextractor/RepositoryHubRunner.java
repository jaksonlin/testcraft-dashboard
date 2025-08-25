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
        
        // Check for help or SSH guidance options
        if (args.length > 0) {
            if (args[0].equals("--help") || args[0].equals("-h")) {
                printUsage();
                return;
            } else if (args[0].equals("--ssh-help")) {
                GitRepositoryManager.printSshKeyGuidance();
                return;
            }
        }
        
        if (args.length < 2) {
            printUsage();
            return;
        }
        
        String repositoryHubPath = args[0];
        String repositoryListPath = args[1];
        String username = args.length > 2 ? args[2] : null;
        String password = args.length > 3 ? args[3] : null;
        String sshKeyPath = args.length > 4 ? args[4] : null;
        
        try {
            // Validate paths
            if (!validatePaths(repositoryHubPath, repositoryListPath)) {
                return;
            }
            
            // Create and run the scanner
            RepositoryHubScanner scanner;
            if (sshKeyPath != null) {
                // Use SSH authentication
                scanner = new RepositoryHubScanner(repositoryHubPath, repositoryListPath, username, password, sshKeyPath);
                System.out.println("Using SSH authentication with key: " + sshKeyPath);
                System.out.println("Note: If you encounter SSH key issues, consider using OpenSSH format keys.");
            } else if (username != null && password != null) {
                // Use HTTPS authentication
                scanner = new RepositoryHubScanner(repositoryHubPath, repositoryListPath, username, password);
                System.out.println("Using HTTPS authentication for user: " + username);
            } else {
                // No authentication (public repos)
                scanner = new RepositoryHubScanner(repositoryHubPath, repositoryListPath);
                System.out.println("No authentication - cloning public repositories only");
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
        System.out.println("Usage: java RepositoryHubRunner <repository_hub_path> <repository_list_file> [username] [password] [ssh_key_path]");
        System.out.println("       java RepositoryHubRunner --ssh-help");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  repository_hub_path    Directory where repositories will be cloned/updated");
        System.out.println("  repository_list_file  Text file containing git repository URLs (one per line)");
        System.out.println("  username             Git username for private repositories (optional)");
        System.out.println("  password             Git password/token for private repositories (optional)");
        System.out.println("  ssh_key_path         Path to SSH private key for SSH authentication (optional)");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --help, -h            Show this help message");
        System.out.println("  --ssh-help            Show SSH key format guidance and conversion instructions");
        System.out.println();
        System.out.println("Authentication Methods:");
        System.out.println("  1. SSH (recommended if you have SSH keys configured):");
        System.out.println("     java RepositoryHubRunner ./repos ./repo-list.txt");
        System.out.println("     java RepositoryHubRunner ./repos ./repo-list.txt ~/.ssh/id_rsa");
        System.out.println("  2. HTTPS with username/password:");
        System.out.println("     java RepositoryHubRunner ./repos ./repo-list.txt myuser mytoken");
        System.out.println("  3. No authentication (public repos only):");
        System.out.println("     java RepositoryHubRunner ./repos ./repo-list.txt");
        System.out.println();
        System.out.println("SSH Key Support:");
        System.out.println("  - OpenSSH format: id_rsa, id_ed25519 (recommended)");
        System.out.println("  - PEM format: .pem, .key files");
        System.out.println("  - PuTTY format: .ppk files (limited support)");
        System.out.println("  - Use --ssh-help for detailed SSH key guidance");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  # SSH authentication (uses default SSH config)");
        System.out.println("  java RepositoryHubRunner ./repos ./repo-list.txt");
        System.out.println("  # SSH authentication with specific key");
        System.out.println("  java RepositoryHubRunner ./repos ./repo-list.txt ~/.ssh/github_key");
        System.out.println("  # HTTPS authentication");
        System.out.println("  java RepositoryHubRunner ./repo-list.txt myuser mytoken");
        System.out.println("  # Show SSH key help");
        System.out.println("  java RepositoryHubRunner --ssh-help");
        System.out.println();
        System.out.println("Repository List File Format:");
        System.out.println("  # Comments start with #");
        System.out.println("  https://github.com/example/repo1.git");
        System.out.println("  https://github.com/example/repo2");
        System.out.println("  git@github.com:example/repo3.git");
        System.out.println();
        System.out.println("Note: SSH URLs (git@github.com:...) will use SSH authentication.");
        System.out.println("      HTTPS URLs will use username/password if provided, or no auth for public repos.");
        System.out.println("      For SSH issues, run: java RepositoryHubRunner --ssh-help");
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
