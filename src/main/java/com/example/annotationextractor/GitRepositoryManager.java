package com.example.annotationextractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manages Git repositories in a repository hub directory
 * Handles cloning, pulling, and updating repositories using system Git commands
 * More reliable than JGit for complex SSH configurations and custom ports
 */
public class GitRepositoryManager {
    
    private final String repositoryHubPath;
    private final String username;
    private final String password;
    private final String sshKeyPath;
    private final int timeoutSeconds;
    
    /**
     * Constructor for GitRepositoryManager with SSH key support
     * 
     * @param repositoryHubPath Path to the repository hub directory
     * @param username Git username (can be null for public repos)
     * @param password Git password/token (can be null for public repos)
     * @param sshKeyPath Path to SSH private key (can be null to use default SSH config)
     */
    public GitRepositoryManager(String repositoryHubPath, String username, String password, String sshKeyPath) {
        this.repositoryHubPath = repositoryHubPath;
        this.username = username;
        this.password = password;
        this.sshKeyPath = sshKeyPath;
        this.timeoutSeconds = 300; // 5 minutes default timeout
    }
    
    /**
     * Constructor for GitRepositoryManager
     * 
     * @param repositoryHubPath Path to the repository hub directory
     * @param username Git username (can be null for public repos)
     * @param password Git password/token (can be null for public repos)
     */
    public GitRepositoryManager(String repositoryHubPath, String username, String password) {
        this(repositoryHubPath, username, password, null);
    }
    
    /**
     * Constructor for public repositories (no authentication)
     * 
     * @param repositoryHubPath Path to the repository hub directory
     */
    public GitRepositoryManager(String repositoryHubPath) {
        this(repositoryHubPath, null, null, null);
    }
    
    /**
     * Initialize the repository hub directory
     * Creates the directory if it doesn't exist
     * 
     * @throws IOException if directory creation fails
     */
    public void initializeRepositoryHub() throws IOException {
        Path hubPath = Paths.get(repositoryHubPath);
        if (!Files.exists(hubPath)) {
            Files.createDirectories(hubPath);
            System.out.println("Created repository hub directory: " + repositoryHubPath);
        } else {
            System.out.println("Repository hub directory already exists: " + repositoryHubPath);
        }
    }
    
    /**
     * Clone a repository if it doesn't exist, or pull latest changes if it does
     * 
     * @param gitUrl Git repository URL
     * @return true if successful, false otherwise
     */
    public boolean cloneOrUpdateRepository(String gitUrl) {
        try {
            String repoName = extractRepositoryName(gitUrl);
            Path repoPath = Paths.get(repositoryHubPath, repoName);
            
            if (Files.exists(repoPath.resolve(".git"))) {
                // Repository exists, pull latest changes
                return pullRepository(repoPath, repoName);
            } else {
                // Repository doesn't exist, clone it
                return cloneRepository(gitUrl, repoPath, repoName);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing repository " + gitUrl + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Clone a new repository using system Git command
     */
    private boolean cloneRepository(String gitUrl, Path repoPath, String repoName) {
        try {
            System.out.println("Cloning repository: " + repoName + " from " + gitUrl);
            
            // Check if repository directory already exists and clean it if needed
            if (Files.exists(repoPath)) {
                System.out.println("Repository directory already exists, cleaning: " + repoPath);
                cleanDirectory(repoPath);
            }
            
            // Build Git clone command
            List<String> command = buildGitCloneCommand(gitUrl, repoPath);
            
            // Execute the command
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(Paths.get(repositoryHubPath).toFile());
            
            // Set environment variables for authentication if needed
            if (username != null && password != null && !isSshUrl(gitUrl)) {
                pb.environment().put("GIT_ASKPASS", "echo");
                pb.environment().put("GIT_USERNAME", username);
                pb.environment().put("GIT_PASSWORD", password);
            }
            
            // Set SSH key if provided
            if (sshKeyPath != null && isSshUrl(gitUrl)) {
                pb.environment().put("GIT_SSH_COMMAND", "ssh -i " + sshKeyPath + " -o StrictHostKeyChecking=no");
            }
            
            System.out.println("Executing: " + String.join(" ", command));
            
            Process process = pb.start();
            
            // Wait for completion with timeout
            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                System.err.println("Git clone operation timed out after " + timeoutSeconds + " seconds");
                return false;
            }
            
            int exitCode = process.exitValue();
            
            if (exitCode == 0) {
                System.out.println("Successfully cloned repository: " + repoName);
                return true;
            } else {
                // Capture error output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("Git error: " + line);
                    }
                }
                System.err.println("Failed to clone repository " + repoName + " (exit code: " + exitCode + ")");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Failed to clone repository " + repoName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Build Git clone command based on URL type and authentication
     */
    private List<String> buildGitCloneCommand(String gitUrl, Path repoPath) {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.add("clone");
        
        // Add authentication options for HTTPS URLs
        if (!isSshUrl(gitUrl) && username != null && password != null) {
            // For HTTPS URLs, we'll use credential helper or environment variables
            command.add("--config");
            command.add("credential.helper=store");
        }
        
        // Add timeout
        command.add("--config");
        command.add("http.timeout=" + timeoutSeconds);
        
        // Add the repository URL and target directory
        command.add(gitUrl);
        command.add(repoPath.getFileName().toString());
        
        return command;
    }
    
    /**
     * Pull latest changes from an existing repository using system Git command
     */
    private boolean pullRepository(Path repoPath, String repoName) {
        try {
            System.out.println("Pulling latest changes for repository: " + repoName);
            
            List<String> command = List.of("git", "pull");
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(repoPath.toFile());
            
            // Set SSH key if provided
            if (sshKeyPath != null) {
                pb.environment().put("GIT_SSH_COMMAND", "ssh -i " + sshKeyPath + " -o StrictHostKeyChecking=no");
            }
            
            System.out.println("Executing: " + String.join(" ", command));
            
            Process process = pb.start();
            
            // Wait for completion with timeout
            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                System.err.println("Git pull operation timed out after " + timeoutSeconds + " seconds");
                return false;
            }
            
            int exitCode = process.exitValue();
            
            if (exitCode == 0) {
                System.out.println("Successfully pulled latest changes for repository: " + repoName);
                return true;
            } else {
                // Capture error output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("Git error: " + line);
                    }
                }
                System.err.println("Failed to pull changes for repository " + repoName + " (exit code: " + exitCode + ")");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Failed to pull repository " + repoName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Clean a directory by removing all contents
     */
    private void cleanDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                .sorted((a, b) -> b.compareTo(a)) // Sort in reverse order to delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Warning: Could not delete " + path + ": " + e.getMessage());
                    }
                });
        }
    }
    
    /**
     * Check if the given URL is an SSH URL
     */
    private boolean isSshUrl(String gitUrl) {
        return gitUrl.startsWith("git@") || gitUrl.startsWith("ssh://");
    }
    
    /**
     * Extract repository name from git URL
     */
    private String extractRepositoryName(String gitUrl) {
        try {
            // Handle SSH URLs
            if (gitUrl.startsWith("git@")) {
                String[] parts = gitUrl.split(":");
                if (parts.length == 2) {
                    String lastPart = parts[1];
                    if (lastPart.endsWith(".git")) {
                        lastPart = lastPart.substring(0, lastPart.length() - 4);
                    }
                    return lastPart;
                }
            }
            
            // Handle SSH URLs with ssh:// protocol
            if (gitUrl.startsWith("ssh://")) {
                String cleanUrl = gitUrl.replace("ssh://", "");
                String[] parts = cleanUrl.split("/");
                String lastPart = parts[parts.length - 1];
                if (lastPart.endsWith(".git")) {
                    lastPart = lastPart.substring(0, lastPart.length() - 4);
                }
                return lastPart;
            }
            
            // Handle HTTPS URLs
            String[] parts = gitUrl.split("/");
            String lastPart = parts[parts.length - 1];
            if (lastPart.endsWith(".git")) {
                lastPart = lastPart.substring(0, lastPart.length() - 4);
            }
            return lastPart;
            
        } catch (Exception e) {
            // Fallback: extract from URL string
            String[] parts = gitUrl.split("/");
            String lastPart = parts[parts.length - 1];
            if (lastPart.endsWith(".git")) {
                lastPart = lastPart.substring(0, lastPart.length() - 4);
            }
            return lastPart;
        }
    }
    
    /**
     * Get the path to a specific repository
     * 
     * @param gitUrl Git repository URL
     * @return Path to the repository directory, or null if not found
     */
    public Path getRepositoryPath(String gitUrl) {
        String repoName = extractRepositoryName(gitUrl);
        Path repoPath = Paths.get(repositoryHubPath, repoName);
        
        if (Files.exists(repoPath.resolve(".git"))) {
            return repoPath;
        }
        return null;
    }
    
    /**
     * Get all repository paths in the hub
     * 
     * @return List of repository paths
     */
    public List<Path> getAllRepositoryPaths() {
        List<Path> repositories = new ArrayList<>();
        Path hubPath = Paths.get(repositoryHubPath);
        
        if (!Files.exists(hubPath)) {
            return repositories;
        }
        
        try {
            Files.list(hubPath)
                .filter(path -> Files.isDirectory(path) && Files.exists(path.resolve(".git")))
                .forEach(repositories::add);
        } catch (IOException e) {
            System.err.println("Error listing repositories: " + e.getMessage());
        }
        
        return repositories;
    }
    
    /**
     * Delete a repository to free up disk space
     * 
     * @param gitUrl Git repository URL
     * @return true if successful, false otherwise
     */
    public boolean deleteRepository(String gitUrl) {
        try {
            String repoName = extractRepositoryName(gitUrl);
            Path repoPath = Paths.get(repositoryHubPath, repoName);
            
            if (!Files.exists(repoPath)) {
                System.out.println("Repository directory does not exist: " + repoPath);
                return true; // Consider it "deleted" if it doesn't exist
            }
            
            if (!Files.exists(repoPath.resolve(".git"))) {
                System.out.println("Not a git repository: " + repoPath);
                return false;
            }
            
            System.out.println("Deleting repository: " + repoName + " from " + repoPath);
            
            // Clean the directory completely
            cleanDirectory(repoPath);
            
            System.out.println("Successfully deleted repository: " + repoName);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to delete repository " + gitUrl + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get the total disk usage of all repositories in the hub
     * 
     * @return Total size in bytes, or -1 if calculation fails
     */
    public long getTotalDiskUsage() {
        try {
            List<Path> repositories = getAllRepositoryPaths();
            long totalSize = 0;
            
            for (Path repoPath : repositories) {
                totalSize += calculateDirectorySize(repoPath);
            }
            
            return totalSize;
        } catch (Exception e) {
            System.err.println("Error calculating disk usage: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Calculate the size of a directory recursively
     * 
     * @param dir Directory to calculate size for
     * @return Size in bytes
     */
    private long calculateDirectorySize(Path dir) {
        try {
            return Files.walk(dir)
                .filter(path -> Files.isRegularFile(path))
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
        } catch (IOException e) {
            return 0;
        }
    }
    
    /**
     * Get disk usage information for all repositories
     * 
     * @return Formatted string with disk usage information
     */
    public String getDiskUsageInfo() {
        long totalSize = getTotalDiskUsage();
        if (totalSize < 0) {
            return "Unable to calculate disk usage";
        }
        
        List<Path> repositories = getAllRepositoryPaths();
        StringBuilder info = new StringBuilder();
        info.append("Disk Usage Information:\n");
        info.append("Total repositories: ").append(repositories.size()).append("\n");
        info.append("Total size: ").append(formatFileSize(totalSize)).append("\n\n");
        
        for (Path repoPath : repositories) {
            try {
                long repoSize = calculateDirectorySize(repoPath);
                info.append(repoPath.getFileName()).append(": ").append(formatFileSize(repoSize)).append("\n");
            } catch (Exception e) {
                info.append(repoPath.getFileName()).append(": Error calculating size\n");
            }
        }
        
        return info.toString();
    }
    
    /**
     * Format file size in human-readable format
     * 
     * @param bytes Size in bytes
     * @return Formatted size string
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Check if a repository exists in the hub
     * 
     * @param gitUrl Git repository URL
     * @return true if repository exists, false otherwise
     */
    public boolean repositoryExists(String gitUrl) {
        return getRepositoryPath(gitUrl) != null;
    }
    
    /**
     * Get the repository hub path
     */
    public String getRepositoryHubPath() {
        return repositoryHubPath;
    }
    
    /**
     * Print SSH key format guidance
     */
    public static void printSshKeyGuidance() {
        System.out.println("\nSSH Key Format Support:");
        System.out.println("=======================");
        System.out.println("The tool now uses system Git commands for better compatibility.");
        System.out.println();
        System.out.println("SSH Key Support:");
        System.out.println("  - All SSH key formats supported (OpenSSH, PEM, PuTTY)");
        System.out.println("  - Uses your system's SSH configuration");
        System.out.println("  - Supports custom SSH ports and configurations");
        System.out.println();
        System.out.println("Benefits of System Git:");
        System.out.println("  - Better SSH compatibility");
        System.out.println("  - Supports all Git features");
        System.out.println("  - Uses your existing SSH setup");
        System.out.println("  - More reliable than JGit/JSch");
        System.out.println();
        System.out.println("Note: Make sure Git is installed and accessible in your PATH.");
    }
}
