package com.example.annotationextractor.runner;

import com.example.annotationextractor.database.DatabaseSchemaManager;
import com.example.annotationextractor.reporting.ExcelReportGenerator;
import com.example.annotationextractor.util.GitRepositoryManager;
import com.example.annotationextractor.casemodel.RepositoryTestInfo;
import com.example.annotationextractor.casemodel.TestCollectionSummary;
import com.example.annotationextractor.database.DataPersistenceService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Main orchestrator for repository hub scanning
 * Manages the entire process from repository management to scanning and database storage
 */
public class RepositoryHubScanner {
    
    private final GitRepositoryManager gitManager;
    private final DataPersistenceService dataService;
    private final String repositoryListPath;
    private boolean tempCloneMode = false;
    
    public RepositoryHubScanner(String repositoryHubPath, String repositoryListPath, String username, String password, String sshKeyPath) {
        this.gitManager = new GitRepositoryManager(repositoryHubPath, username, password, sshKeyPath);
        this.dataService = new DataPersistenceService();
        this.repositoryListPath = repositoryListPath;
    }
    
    public RepositoryHubScanner(String repositoryHubPath, String repositoryListPath, String username, String password) {
        this(repositoryHubPath, repositoryListPath, username, password, null);
    }
    
    public RepositoryHubScanner(String repositoryHubPath, String repositoryListPath) {
        this(repositoryHubPath, repositoryListPath, null, null, null);
    }

    
    
    /**
     * Set temporary clone mode
     * When enabled, repositories will be deleted after scanning to save disk space
     */
    public void setTempCloneMode(boolean tempCloneMode) {
        this.tempCloneMode = tempCloneMode;
    }
    
    public boolean executeFullScan() {
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Starting Repository Hub Scan");
            System.out.println("============================");
            System.out.println("Repository Hub: " + gitManager.getRepositoryHubPath());
            System.out.println("Repository List: " + repositoryListPath);
            System.out.println("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            System.out.println("Temporary Clone Mode: " + (tempCloneMode ? "ENABLED" : "DISABLED"));
            System.out.println();
            
            // Initialize database schema
            if (!DatabaseSchemaManager.schemaExists()) {
                DatabaseSchemaManager.initializeSchema();
            }
            
            // Initialize repository hub directory
            gitManager.initializeRepositoryHub();
            
            // Read repository list
            List<RepositoryHubRunnerConfig> repositoryUrls = RepositoryListProcessor.readRepositoryHubRunnerConfigs(repositoryListPath);
            updateRepositoryHubRunnerConfigs(repositoryUrls);
            System.out.println("Found " + repositoryUrls.size() + " repositories to process");
            
            // Process repositories one by one in temporary clone mode
            if (tempCloneMode) {
                return processRepositoriesTemporarily(repositoryUrls, startTime);
            } else {
                // Original behavior: clone all repositories first, then scan
                return processRepositoriesNormally(repositoryUrls, startTime);
            }
            
        } catch (Exception e) {
            System.err.println("Repository Hub Scan failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void updateRepositoryHubRunnerConfigs(List<RepositoryHubRunnerConfig> repositoryUrls) throws SQLException {
        for (RepositoryHubRunnerConfig config : repositoryUrls) {
            DataPersistenceService.assignRepositoryToTeam(config.getRepositoryUrl(), config.getTeamName(), config.getTeamCode());
        }
    }
    
    /**
     * Process repositories in temporary clone mode: clone, scan, delete
     */
    private boolean processRepositoriesTemporarily(List<RepositoryHubRunnerConfig> repositoryHubRunnerConfigs, long startTime) {
        int successfulRepos = 0;
        int totalRepos = repositoryHubRunnerConfigs.size();
        
        // Create an aggregated summary for all repositories
        TestCollectionSummary aggregatedSummary = new TestCollectionSummary();
        aggregatedSummary.setScanDirectory(gitManager.getRepositoryHubPath());
        aggregatedSummary.setScanTimestamp(System.currentTimeMillis());
        
        
        for (int i = 0; i < repositoryHubRunnerConfigs.size(); i++) {
            RepositoryHubRunnerConfig config = repositoryHubRunnerConfigs.get(i);
            String gitUrl = config.getRepositoryUrl();
            System.out.println("\n" + "=".repeat(80));
            System.out.println("Processing repository " + (i + 1) + " of " + totalRepos + ": " + gitUrl);
            System.out.println("=".repeat(80));
            
            try {
                // Clone the repository
                if (!gitManager.cloneOrUpdateRepository(gitUrl)) {
                    System.err.println("❌ Failed to clone repository: " + gitUrl);
                    continue;
                } 
                successfulRepos++;
                System.out.println("✅ Successfully cloned repository: " + gitUrl);
                
                // Scan this single repository
                TestCollectionSummary scanSummary = scanSingleRepository(gitUrl);
                if (scanSummary != null) {
                    // Set git URL for all repositories in the scan summary
                    for (RepositoryTestInfo repo : scanSummary.getRepositories()) {
                        repo.setGitUrl(gitUrl);
                    }
                    // Aggregate the results
                    aggregateScanResults(aggregatedSummary, scanSummary);
                    System.out.println("🔍 Repository scanned successfully");
                } else {
                    System.out.println("❌ Failed to scan repository: " + gitUrl);
                }
                 
            } catch (Exception e) {
                System.err.println("❌ Error processing repository " + gitUrl + ": " + e.getMessage());
            } finally {
                gitManager.deleteRepository(gitUrl);
            }

        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Temporary clone mode completed!");
        System.out.println("=".repeat(80));
        System.out.println("Successfully processed " + successfulRepos + " out of " + totalRepos + " repositories");
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("⏱️ Scan completed in " + duration + " milliseconds");
        // Store the aggregated results
        if (successfulRepos > 0) {
            try {
                storeScanResults(aggregatedSummary, duration);
            } catch (SQLException e) {
                System.err.println("❌ Error persisting to database: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Generate final report AFTER data persistence
        generateReport();
        System.out.println("\n" + "=".repeat(80));
        
        return successfulRepos > 0;
    }

    private void generateReport() {
        // Generate final report AFTER data persistence
        try {
            String reportPath = "reports/weekly_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            Path reportDir = Paths.get(reportPath).getParent();
            if (!Files.exists(reportDir)) {
                Files.createDirectories(reportDir);
            }
            ExcelReportGenerator.generateWeeklyReport(reportPath);
            System.out.println("📊 Report generated successfully: " + reportPath);
        } catch (Exception e) {
            System.err.println("❌ Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    
    /**
     * Aggregate scan results from individual repository into the main summary
     */
    private void aggregateScanResults(TestCollectionSummary mainSummary, TestCollectionSummary repoSummary) {
        if (repoSummary == null) return;
        
        // Add repository info - the addRepository method automatically updates totals
        if (repoSummary.getRepositories() != null) {
            for (RepositoryTestInfo repo : repoSummary.getRepositories()) {
                mainSummary.addRepository(repo);
            }
        }
    }
    
    /**
     * Process repositories in normal mode: clone all, then scan all
     */
    private boolean processRepositoriesNormally(List<RepositoryHubRunnerConfig> repositoryUrls, long startTime) {
        // Clone/update repositories
        int successfulRepos = 0;
        for (RepositoryHubRunnerConfig config : repositoryUrls) {
            String gitUrl = config.getRepositoryUrl();
            if (gitManager.cloneOrUpdateRepository(gitUrl)) {
                successfulRepos++;
            } else {
                System.out.println("Failed to clone/update repository: " + gitUrl);
            }
        }
        System.out.println("Successfully cloned/updated " + successfulRepos + " repositories");
        

        // Scan repositories and persist data to database
        try {
            // Scan repositories
            TestCollectionSummary scanSummary = scanAllRepositories();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Scan completed in " + duration + " milliseconds");
    
            if (scanSummary != null) {
                storeScanResults(scanSummary, duration);
                System.out.println("Repository Hub Scan Completed Successfully!");
            } else {
                System.out.println("Repository Hub Scan Failed!");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error persisting to database: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Generate report
        generateReport();
        return true;
    }
    
    /**
     * Scan a single repository
     */
    private TestCollectionSummary scanSingleRepository(String gitUrl) {
        try {
            Path repoPath = gitManager.getRepositoryPath(gitUrl);
            if (repoPath != null) {
                return RepositoryScanner.scanRepositories(repoPath.toString());
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error scanning single repository " + gitUrl + ": " + e.getMessage());
            return null;
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
    
    private void storeScanResults(TestCollectionSummary summary, long duration) throws SQLException {
        System.out.println("\nPersisting data to database...");
        long scanSessionId = DataPersistenceService.persistScanSession(summary, duration);
        System.out.println("Data persisted successfully. Scan Session ID: " + scanSessionId);
    }
    
    public GitRepositoryManager getGitManager() {
        return gitManager;
    }

    /**
     * Format file size in human-readable format
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
