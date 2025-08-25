package com.example.annotationextractor;

import com.example.annotationextractor.database.DatabaseSchemaManager;
import com.example.annotationextractor.reporting.ExcelReportGenerator;
import com.example.annotationextractor.database.DataPersistenceService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
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
            long startTime = System.currentTimeMillis();
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
                } else {
                    System.out.println("Failed to clone/update repository: " + gitUrl);
                }
            }
            System.out.println("Successfully cloned/updated " + successfulRepos + " repositories");
            
            // Scan repositories
            TestCollectionSummary scanSummary = scanAllRepositories();
            if (scanSummary != null) {
                storeScanResults(scanSummary);
                System.out.println("Repository Hub Scan Completed Successfully!");
            } else {
                System.out.println("Repository Hub Scan Failed!");
                return false;
            }

            // Persist data to database
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Scan completed in " + duration + " milliseconds");

           if (!DatabaseSchemaManager.schemaExists()) {
                DatabaseSchemaManager.initializeSchema();
            }
            System.out.println("\nPersisting data to database...");
            try {
                long scanSessionId = DataPersistenceService.persistScanSession(scanSummary, duration);
                System.out.println("Data persisted successfully. Scan Session ID: " + scanSessionId);
            } catch (SQLException e) {
                System.err.println("Error persisting to database: " + e.getMessage());
                e.printStackTrace();
            }
            // Generate report
            try {
                String reportPath = "reports/weekly_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
                // create directory if it doesn't exist
                Path reportDir = Paths.get(reportPath).getParent();
                if (!Files.exists(reportDir)) {
                    Files.createDirectories(reportDir);
                }
                ExcelReportGenerator.generateWeeklyReport(reportPath);
                System.out.println("Report generated successfully: " + reportPath);
            } catch (Exception e) {
                System.err.println("Error generating report: " + e.getMessage());
                e.printStackTrace();
            }
            return true;
            
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
