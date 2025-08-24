package com.example.annotationextractor;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.URIish;

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
 */
public class GitRepositoryManager {
    
    private final String repositoryHubPath;
    private final String username;
    private final String password;
    
    /**
     * Constructor for GitRepositoryManager
     * 
     * @param repositoryHubPath Path to the repository hub directory
     * @param username Git username (can be null for public repos)
     * @param password Git password/token (can be null for public repos)
     */
    public GitRepositoryManager(String repositoryHubPath, String username, String password) {
        this.repositoryHubPath = repositoryHubPath;
        this.username = username;
        this.password = password;
    }
    
    /**
     * Constructor for public repositories (no authentication)
     * 
     * @param repositoryHubPath Path to the repository hub directory
     */
    public GitRepositoryManager(String repositoryHubPath) {
        this(repositoryHubPath, null, null);
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
            
            Git git = null;
            try {
                if (username != null && password != null) {
                    // Clone with authentication
                    git = Git.cloneRepository()
                        .setURI(gitUrl)
                        .setDirectory(repoPath.toFile())
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                        .call();
                } else {
                    // Clone without authentication
                    git = Git.cloneRepository()
                        .setURI(gitUrl)
                        .setDirectory(repoPath.toFile())
                        .call();
                }
                
                System.out.println("Successfully cloned repository: " + repoName);
                return true;
                
            } finally {
                if (git != null) {
                    git.close();
                }
            }
            
        } catch (GitAPIException e) {
            System.err.println("Failed to clone repository " + repoName + ": " + e.getMessage());
            return false;
        }
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
