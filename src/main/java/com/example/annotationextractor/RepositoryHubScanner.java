package com.example.annotationextractor;

import com.example.annotationextractor.database.DatabaseSchemaManager;
import com.example.annotationextractor.database.DataPersistenceService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main orchestrator for repository hub scanning
 * Manages the entire process from repository management to scanning and database storage
 */
public class RepositoryHubScanner {
    
    private final GitRepositoryManager gitManager;
    private final DataPersistenceService dataService;
    private final String repositoryListPath;
    
    public RepositoryHubScanner(String repositoryHubPath, String repositoryListPath, String username, String password) {
        this.gitManager = new GitRepositoryManager(repositoryHubPath, username, password);
        this.dataService = new DataPersistenceService();
        this.repositoryListPath = repositoryListPath;
    }
    
    public RepositoryHubScanner(String repositoryHubPath, String repositoryListPath) {
        this(repositoryHubPath, repositoryListPath, null, null);
    }
    
    public boolean executeFullScan() {
        try {
            System.out.println("Starting Repository Hub Scan");
            System.out.println("============================");
            System.out.println("Repository Hub: " + gitManager.getRepositoryHubPath());
            System.out.println("Repository List: " + repositoryListPath);
            System.out.println("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            System.out.println();
            
            // Initialize database schema
            if (!DatabaseSchemaManager.schemaExists()) {
                DatabaseSchemaManager.initializeSchema();
            }
            
            // Initialize repository hub directory
            gitManager.initializeRepositoryHub();
            
            // Read repository list
            List<String> repositoryUrls = RepositoryListProcessor.readRepositoryUrls(repositoryListPath);
            System.out.println("Found " + repositoryUrls.size() + " repositories to process");
            
            // Clone/update repositories
            int successfulRepos = 0;
            for (String gitUrl : repositoryUrls) {
                if (gitManager.cloneOrUpdateRepository(gitUrl)) {
                    successfulRepos++;
                }
            }
            
            // Scan repositories
            TestCollectionSummary scanSummary = scanAllRepositories();
            if (scanSummary != null) {
                storeScanResults(scanSummary);
                System.out.println("Repository Hub Scan Completed Successfully!");
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("Repository Hub Scan failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private TestCollectionSummary scanAllRepositories() {
        try {
            return RepositoryScanner.scanRepositories(gitManager.getRepositoryHubPath());
        } catch (Exception e) {
            System.err.println("Error during repository scanning: " + e.getMessage());
            return null;
        }
    }
    
    private void storeScanResults(TestCollectionSummary summary) {
        // Implementation for storing results in database
        System.out.println("Storing scan results...");
    }
    
    public GitRepositoryManager getGitManager() {
        return gitManager;
    }
}
