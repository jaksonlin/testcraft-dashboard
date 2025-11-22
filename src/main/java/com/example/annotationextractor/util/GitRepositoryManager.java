package com.example.annotationextractor.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
    private final String targetBranch;
    
    /**
     * Constructor for GitRepositoryManager with SSH key support
     * 
     * @param repositoryHubPath Path to the repository hub directory
     * @param username Git username (can be null for public repos)
     * @param password Git password/token (can be null for public repos)
     * @param sshKeyPath Path to SSH private key (can be null to use default SSH config)
     */
    public GitRepositoryManager(String repositoryHubPath, String username, String password, String sshKeyPath) {
        this(repositoryHubPath, username, password, sshKeyPath, null);
    }

    public GitRepositoryManager(String repositoryHubPath, String username, String password, String sshKeyPath, String targetBranch) {
        this.repositoryHubPath = repositoryHubPath;
        this.username = username;
        this.password = password;
        this.sshKeyPath = sshKeyPath;
        this.timeoutSeconds = 300; // 5 minutes default timeout
        this.targetBranch = normalizeBranch(targetBranch);
        
        // Pre-populate known hosts to avoid interactive prompts
        populateKnownHosts();
    }
    
    /**
     * Constructor for GitRepositoryManager
     * 
     * @param repositoryHubPath Path to the repository hub directory
     * @param username Git username (can be null for public repos)
     * @param password Git password/token (can be null for public repos)
     */
    public GitRepositoryManager(String repositoryHubPath, String username, String password) {
        this(repositoryHubPath, username, password, null, null);
    }
    
    /**
     * Pre-populate known hosts for common Git servers to avoid interactive prompts
     */
    private void populateKnownHosts() {
        try {
            // Common Git server host keys (these are public and safe to add)
            String[] knownHosts = {
                "github.com ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCj7ndNxQowgcQnjshcLrqPEiiphnt+VTTvDP6mHBL9j1aNUkY4Ue1gvwnGLVlOhGeYrnZaMgRK6+PKCUXaDbC7qtbW8gIkhL7aGCsOr/C56SJMy/BCZfxd1nWzAOxSDPgVsmerOBYfNqltV9/hWCqBywINIR+5dIg6JTJ72pcEpEjcYgXkE2YEFXV1JHnsKgbLWNlhScqb2UmyRkQyytRLtL+38TGxkxCflmO+5Z8CSSNY7GidjMIZ7Q4zMjL2ZoL0H6XoVvF3S1mKgf3HwV467B2Xfpz0kSkmIWSkqw==\n",
                "github.com ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIOMqqnkVzrm0SdG6UOoqKLsabgH5C9okWi0dh2l9GKJl\n",
                "github.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBEmKSENjQEezOmxkZMy7opKgwFB9nkt5YRrYMjNuG5N87uRgg6CLrbo5wAdT/y6v0mKV0U2w0WZ2YB/++Tpockg=\n"
            };
            
            Path knownHostsFile = Paths.get(System.getProperty("user.home"), ".ssh", "known_hosts");
            Files.createDirectories(knownHostsFile.getParent());
            
            // Check if hosts are already in known_hosts
            List<String> existingHosts = Files.exists(knownHostsFile) ? 
                Files.readAllLines(knownHostsFile) : new ArrayList<>();
            
            for (String hostEntry : knownHosts) {
                String hostname = hostEntry.split(" ")[0];
                boolean hostExists = existingHosts.stream()
                    .anyMatch(line -> line.startsWith(hostname + " "));
                
                if (!hostExists) {
                    Files.write(knownHostsFile, hostEntry.getBytes(), 
                        java.nio.file.StandardOpenOption.CREATE, 
                        java.nio.file.StandardOpenOption.APPEND);
                    System.out.println("Added " + hostname + " to known_hosts");
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not populate known_hosts: " + e.getMessage());
        }
    }
    
    /**
     * Constructor for public repositories (no authentication)
     * 
     * @param repositoryHubPath Path to the repository hub directory
     */
    public GitRepositoryManager(String repositoryHubPath) {
        this(repositoryHubPath, null, null, null, null);
    }
    
    /**
     * Initialize the repository hub directory
     * Creates the directory if it doesn't exist
     * 
     * @throws IOException if directory creation fails
     */
    public Path initializeRepositoryHub() throws IOException {
        Path hubPath = Paths.get(repositoryHubPath);
        if (!Files.exists(hubPath)) {
            Files.createDirectories(hubPath);
            System.out.println("Created repository hub directory: " + repositoryHubPath);
        } else {
            System.out.println("Repository hub directory already exists: " + repositoryHubPath);
        }
        return hubPath;
    }
    
    /**
     * Clone a repository if it doesn't exist, or pull latest changes if it does
     * 
     * @param gitUrl Git repository URL
     * @return true if successful, false otherwise
     */
    public Path cloneOrUpdateRepository(String gitUrl) {
        try {
            String repoName = extractRepositoryName(gitUrl);
            Path repoPath = Paths.get(repositoryHubPath, repoName);
            
            // If it's already a git repo, just pull
            if (Files.exists(repoPath.resolve(".git"))) {
                // Repository exists, pull latest changes
                if (pullRepository(repoPath, repoName)) {
                    return repoPath;
                }
            } else {
                // Directory exists but not a git repo → clean and re-clone (previous incomplete/failed clone)
                if (Files.exists(repoPath)) {
                    try {
                        boolean hasContent = Files.list(repoPath).findAny().isPresent();
                        if (hasContent) {
                            System.out.println("Existing non-git directory detected, cleaning before clone: " + repoPath);
                            cleanDirectory(repoPath);
                        }
                    } catch (IOException ignore) { }
                }
                // Clone fresh
                if (cloneRepository(gitUrl, repoPath, repoName)) {
                    return repoPath;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error processing repository " + gitUrl + ": " + e.getMessage());
            return null;
        }
        System.err.println("Failed to clone or update repository " + gitUrl);
        return null;
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
            
            // Ensure parent directories exist for nested paths like xk/amc/arm
            Path parentDir = repoPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            // Build Git clone command
            List<String> command = buildGitCloneCommand(gitUrl, repoPath);
            if (useTargetBranch()) {
                System.out.println("Requested branch: " + targetBranch);
            }
            
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
                pb.environment().put("GIT_SSH_COMMAND", "ssh -i " + sshKeyPath + " -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null");
            } else if (isSshUrl(gitUrl)) {
                // Even without SSH key, disable host key checking for automated operations
                pb.environment().put("GIT_SSH_COMMAND", "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null");
            }
            
            System.out.println("Executing: " + String.join(" ", command));
            System.out.println("Clone target directory: " + repoPath);
            
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

        if (useTargetBranch()) {
            command.add("--branch");
            command.add(targetBranch);
            command.add("--single-branch");
        }
        
        // Add the repository URL and target directory (full path to support nested directories)
        command.add(gitUrl);
        command.add(repoPath.toString());
        
        return command;
    }
    
    /**
     * Pull latest changes from an existing repository using system Git command
     */
    private boolean pullRepository(Path repoPath, String repoName) {
        try {
            System.out.println("Pulling latest changes for repository: " + repoName);
            System.out.println("Repository path: " + repoPath);
            if (useTargetBranch()) {
                System.out.println("Ensuring branch: " + targetBranch);
                if (!ensureBranchCheckedOut(repoPath)) {
                    System.err.println("Failed to align repository " + repoName + " to branch " + targetBranch);
                    return false;
                }
            }

            List<String> command = useTargetBranch()
                    ? List.of("git", "pull", "origin", targetBranch)
                    : List.of("git", "pull");

            return runGitCommand(repoPath, command);
        } catch (Exception e) {
            System.err.println("Failed to pull repository " + repoName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean ensureBranchCheckedOut(Path repoPath) {
        if (!useTargetBranch()) {
            return true;
        }
        if (!runGitCommand(repoPath, List.of("git", "fetch", "origin", targetBranch))) {
            return false;
        }
        if (runGitCommand(repoPath, List.of("git", "checkout", targetBranch))) {
            return true;
        }
        return runGitCommand(repoPath, List.of("git", "checkout", "-B", targetBranch, "origin/" + targetBranch));
    }

    private boolean runGitCommand(Path repoPath, List<String> command) {
        try {
            System.out.println("Executing git command in " + repoPath + ": " + String.join(" ", command));
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(repoPath.toFile());
            applySshEnvironment(pb);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("git> " + line);
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    System.err.println("Error reading git command output: " + e.getMessage());
                }
            });
            outputThread.start();

            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                System.err.println("Git command timed out after " + timeoutSeconds + " seconds: " + command);
                process.destroyForcibly();
                outputThread.interrupt();
                return false;
            }

            outputThread.join(1000);
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                System.err.println("Git command failed (" + exitCode + "): " + String.join(" ", command));
                System.err.println(output.toString());
                return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("Failed to execute git command " + command + ": " + e.getMessage());
            return false;
        }
    }

    private void applySshEnvironment(ProcessBuilder pb) {
        if (sshKeyPath != null) {
            pb.environment().put("GIT_SSH_COMMAND", "ssh -i " + sshKeyPath + " -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null");
        } else {
            pb.environment().put("GIT_SSH_COMMAND", "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null");
        }
    }

    private boolean useTargetBranch() {
        return targetBranch != null && !targetBranch.isBlank();
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    private String normalizeBranch(String branch) {
        if (branch == null) {
            return null;
        }
        String trimmed = branch.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    /**
     * Clean a directory by removing all contents
     * Handles Git repositories and read-only files properly
     */
    private void cleanDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        
        System.out.println("Cleaning directory: " + dir);
        
        try {
            // First, try to remove .git directory specifically (this is often the problem)
            Path gitDir = dir.resolve(".git");
            if (Files.exists(gitDir)) {
                System.out.println("Removing .git directory...");
                removeGitDirectory(gitDir);
            }
            
            // Now remove the rest of the directory contents
            Files.walk(dir)
                .sorted((a, b) -> b.compareTo(a)) // Sort in reverse order to delete files before directories
                .forEach(path -> {
                    if (!path.equals(dir)) { // Don't try to delete the root directory itself
                        try {
                            if (Files.isDirectory(path)) {
                                Files.deleteIfExists(path);
                            } else {
                                // For files, try to make them writable first
                                try {
                                    path.toFile().setWritable(true);
                                } catch (Exception e) {
                                    // Ignore if we can't change permissions
                                }
                                Files.deleteIfExists(path);
                            }
                        } catch (IOException e) {
                            System.err.println("Warning: Could not delete " + path + ": " + e.getMessage());
                        }
                    }
                });
                
        } catch (Exception e) {
            System.err.println("Error during directory cleanup: " + e.getMessage());
            // Fallback: try to use system commands for stubborn directories
            try {
                forceRemoveDirectory(dir);
            } catch (Exception fallbackError) {
                System.err.println("Fallback cleanup also failed: " + fallbackError.getMessage());
                throw new IOException("Failed to clean directory: " + dir, e);
            }
        }
    }
    
    /**
     * Remove Git directory contents with proper handling of read-only files
     */
    private void removeGitDirectory(Path gitDir) throws IOException {
        if (!Files.exists(gitDir)) {
            return;
        }
        
        try {
            // Remove Git index and other files first
            Path indexFile = gitDir.resolve("index");
            if (Files.exists(indexFile)) {
                try {
                    indexFile.toFile().setWritable(true);
                    Files.deleteIfExists(indexFile);
                } catch (Exception e) {
                    System.err.println("Warning: Could not delete Git index: " + e.getMessage());
                }
            }
            
            // Remove objects directory (often contains read-only files)
            Path objectsDir = gitDir.resolve("objects");
            if (Files.exists(objectsDir)) {
                removeDirectoryRecursively(objectsDir);
            }
            
            // Remove refs directory
            Path refsDir = gitDir.resolve("refs");
            if (Files.exists(refsDir)) {
                removeDirectoryRecursively(refsDir);
            }
            
            // Remove other Git files
            String[] gitFiles = {"HEAD", "config", "description", "hooks", "info", "logs"};
            for (String gitFile : gitFiles) {
                Path filePath = gitDir.resolve(gitFile);
                if (Files.exists(filePath)) {
                    try {
                        if (Files.isDirectory(filePath)) {
                            removeDirectoryRecursively(filePath);
                        } else {
                            filePath.toFile().setWritable(true);
                            Files.deleteIfExists(filePath);
                        }
                    } catch (Exception e) {
                        System.err.println("Warning: Could not delete Git file " + gitFile + ": " + e.getMessage());
                    }
                }
            }
            
            // Finally remove the .git directory itself
            Files.deleteIfExists(gitDir);
            
        } catch (Exception e) {
            System.err.println("Error removing Git directory: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Remove directory recursively with proper permission handling
     */
    private void removeDirectoryRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        
        Files.walk(dir)
            .sorted((a, b) -> b.compareTo(a))
            .forEach(path -> {
                try {
                    if (Files.isDirectory(path)) {
                        Files.deleteIfExists(path);
                    } else {
                        // Make file writable before deletion
                        try {
                            path.toFile().setWritable(true);
                        } catch (Exception e) {
                            // Ignore permission errors
                        }
                        Files.deleteIfExists(path);
                    }
                } catch (IOException e) {
                    System.err.println("Warning: Could not delete " + path + ": " + e.getMessage());
                }
            });
    }
    
    /**
     * Force remove directory using system commands (fallback method)
     */
    private void forceRemoveDirectory(Path dir) throws IOException {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                // Windows: use rmdir /s /q
                pb = new ProcessBuilder("cmd", "/c", "rmdir", "/s", "/q", dir.toString());
            } else {
                // Unix/Linux: use rm -rf
                pb = new ProcessBuilder("rm", "-rf", dir.toString());
            }
            
            Process process = pb.start();
            
            // Wait for completion with timeout (30 seconds)
            boolean completed = process.waitFor(30, TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                throw new IOException("System command timed out after 30 seconds");
            }
            
            int exitCode = process.exitValue();
            
            if (exitCode != 0) {
                throw new IOException("System command failed with exit code: " + exitCode);
            }
            
            System.out.println("Directory removed using system command: " + dir);
            
        } catch (Exception e) {
            throw new IOException("Failed to force remove directory using system command: " + e.getMessage(), e);
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
    
    /**
     * Get available disk space for the repository hub directory
     * 
     * @return Available space in bytes, or -1 if calculation fails
     */
    public long getAvailableDiskSpace() {
        try {
            Path hubPath = Paths.get(repositoryHubPath);
            if (!Files.exists(hubPath)) {
                return -1;
            }
            
            return hubPath.toFile().getFreeSpace();
        } catch (Exception e) {
            System.err.println("Error calculating available disk space: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Get disk space information for the repository hub directory
     * 
     * @return Formatted string with disk space information
     */
    public String getDiskSpaceInfo() {
        try {
            Path hubPath = Paths.get(repositoryHubPath);
            if (!Files.exists(hubPath)) {
                return "Repository hub directory does not exist";
            }
            
            long totalSpace = hubPath.toFile().getTotalSpace();
            long freeSpace = hubPath.toFile().getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            
            StringBuilder info = new StringBuilder();
            info.append("Disk Space Information:\n");
            info.append("Repository Hub: ").append(repositoryHubPath).append("\n");
            info.append("Total Space: ").append(formatFileSize(totalSpace)).append("\n");
            info.append("Used Space: ").append(formatFileSize(usedSpace)).append("\n");
            info.append("Free Space: ").append(formatFileSize(freeSpace)).append("\n");
            
            if (totalSpace > 0) {
                double usagePercent = (double) usedSpace / totalSpace * 100;
                info.append("Usage: ").append(String.format("%.1f%%", usagePercent)).append("\n");
            }
            
            return info.toString();
            
        } catch (Exception e) {
            return "Error getting disk space information: " + e.getMessage();
        }
    }

     /**
     * Extract git URL from a repository directory
     */
    public static String extractGitUrlFromRepository(Path repoPath) {
        try {
            Path gitConfigPath = repoPath.resolve(".git").resolve("config");
            if (Files.exists(gitConfigPath)) {
                List<String> lines = Files.readAllLines(gitConfigPath);
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("url = ")) {
                        return line.substring(6).trim();
                    }
                }
            }
        } catch (IOException e) {
            // Ignore errors reading git config
        }
        return null;
    }

    /**
     * Find all git repositories in a directory
     */
    public static List<Path> findGitRepositories(Path rootPath) throws IOException {
        List<Path> repositories = new ArrayList<>();
        
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // Check if this directory contains a .git folder
                if (Files.exists(dir.resolve(".git"))) {
                    repositories.add(dir);
                    return FileVisitResult.SKIP_SUBTREE; // Don't go deeper into this repo
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Log error but continue
                System.err.println("Failed to visit file: " + file + " - " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });
        
        return repositories;
    }
    
}
