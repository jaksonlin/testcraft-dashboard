package com.example.annotationextractor;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.transport.URIish;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages Git repositories in a repository hub directory
 * Handles cloning, pulling, and updating repositories
 * Supports both SSH and HTTPS authentication methods
 */
public class GitRepositoryManager {
    
    private final String repositoryHubPath;
    private final String username;
    private final String password;
    private final String sshKeyPath;
    
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
     * Clone a new repository
     */
    private boolean cloneRepository(String gitUrl, Path repoPath, String repoName) {
        try {
            System.out.println("Cloning repository: " + repoName + " from " + gitUrl);
            
            // Check if repository directory already exists and clean it if needed
            if (Files.exists(repoPath)) {
                System.out.println("Repository directory already exists, cleaning: " + repoPath);
                cleanDirectory(repoPath);
            }
            
            Git git = null;
            try {
                if (isSshUrl(gitUrl)) {
                    // Clone with SSH authentication
                    System.out.println("Cloning with SSH authentication");
                    git = cloneWithSsh(gitUrl, repoPath);
                } else if (username != null && password != null) {
                    // Clone with HTTPS authentication
                    System.out.println("Cloning with HTTPS authentication for user: " + username);
                    git = Git.cloneRepository()
                        .setURI(gitUrl)
                        .setDirectory(repoPath.toFile())
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                        .setTimeout(300) // 5 minutes timeout
                        .call();
                } else {
                    // Clone without authentication (public repos)
                    System.out.println("Cloning public repository without authentication");
                    git = Git.cloneRepository()
                        .setURI(gitUrl)
                        .setDirectory(repoPath.toFile())
                        .setTimeout(300) // 5 minutes timeout
                        .call();
                }
                
                if (git != null) {
                    System.out.println("Successfully cloned repository: " + repoName);
                    return true;
                } else {
                    System.err.println("Failed to clone repository: " + repoName + " - git object is null");
                    return false;
                }
                
            } finally {
                if (git != null) {
                    git.close();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Failed to clone repository " + repoName + ": " + e.getMessage());
            
            // Provide more specific error messages for common issues
            if (e.getMessage().contains("remote hung up unexpectedly")) {
                System.err.println("  This usually indicates a network issue, authentication problem, or repository access issue.");
                System.err.println("  - Check your internet connection");
                System.err.println("  - Verify the repository URL is correct");
                System.err.println("  - Ensure you have access to the repository");
                if (isSshUrl(gitUrl)) {
                    System.err.println("  - Verify your SSH key is properly configured and added to your GitHub account");
                    System.err.println("  - Check that ssh-agent is running and your key is loaded");
                } else if (username != null) {
                    System.err.println("  - Verify your username and password/token are correct");
                }
            } else if (e.getMessage().contains("timeout")) {
                System.err.println("  Network timeout occurred. This might be due to:");
                System.err.println("  - Slow internet connection");
                System.err.println("  - Large repository size");
                System.err.println("  - Network firewall restrictions");
            } else if (e.getMessage().contains("Authentication failed")) {
                System.err.println("  Authentication failed. Please check:");
                if (isSshUrl(gitUrl)) {
                    System.err.println("  - Your SSH key is properly configured");
                    System.err.println("  - The SSH key is added to your GitHub account");
                    System.err.println("  - ssh-agent is running and your key is loaded");
                } else {
                    System.err.println("  - Username and password/token are correct");
                    System.err.println("  - You have access to the repository");
                    System.err.println("  - The repository is not private or you have proper access");
                }
            }
            
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
     * Clone repository using SSH authentication
     */
    private Git cloneWithSsh(String gitUrl, Path repoPath) throws GitAPIException, IOException {
        // Configure SSH session factory
        JschConfigSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                // No additional configuration needed
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch(fs);
                
                // If a specific SSH key path is provided, use it
                if (sshKeyPath != null && Files.exists(Paths.get(sshKeyPath))) {
                    defaultJSch.addIdentity(sshKeyPath);
                    System.out.println("Using SSH key: " + sshKeyPath);
                } else {
                    // Otherwise, use the default SSH configuration (including ssh-agent)
                    System.out.println("Using default SSH configuration (ssh-agent, ~/.ssh/id_rsa, etc.)");
                }
                
                return defaultJSch;
            }
        };

        return Git.cloneRepository()
            .setURI(gitUrl)
            .setDirectory(repoPath.toFile())
            .setTransportConfigCallback(transport -> {
                if (transport instanceof SshTransport) {
                    ((SshTransport) transport).setSshSessionFactory(sshSessionFactory);
                }
            })
            .setTimeout(300) // 5 minutes timeout
            .call();
    }

    /**
     * Check if the given URL is an SSH URL
     */
    private boolean isSshUrl(String gitUrl) {
        return gitUrl.startsWith("git@") || gitUrl.startsWith("ssh://");
    }

    /**
     * Pull latest changes from an existing repository
     */
    private boolean pullRepository(Path repoPath, String repoName) {
        try {
            System.out.println("Pulling latest changes for repository: " + repoName);
            
            try (Git git = Git.open(repoPath.toFile())) {
                PullResult result = git.pull().call();
                
                if (result.isSuccessful()) {
                    System.out.println("Successfully pulled latest changes for repository: " + repoName);
                    return true;
                } else {
                    System.err.println("Failed to pull changes for repository " + repoName + ": " + result.toString());
                    return false;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Failed to pull repository " + repoName + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract repository name from git URL
     */
    private String extractRepositoryName(String gitUrl) {
        try {
            URIish uri = new URIish(gitUrl);
            String path = uri.getPath();
            if (path.endsWith(".git")) {
                path = path.substring(0, path.length() - 4);
            }
            String[] pathParts = path.split("/");
            return pathParts[pathParts.length - 1];
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
}
