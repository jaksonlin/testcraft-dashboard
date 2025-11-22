package com.example.annotationextractor.runner;

import com.example.annotationextractor.application.PersistScanResultsUseCase;
import com.example.annotationextractor.casemodel.RepositoryTestInfo;
import com.example.annotationextractor.casemodel.TestCollectionSummary;
import com.example.annotationextractor.database.DataPersistenceService;
import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.domain.model.ScanRepositoryEntry;
import com.example.annotationextractor.reporting.ExcelReportGenerator;
import com.example.annotationextractor.util.GitRepositoryManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * Main orchestrator for repository hub scanning
 * Manages the entire process from repository management to scanning and database storage
 */
public class RepositoryHubScanner {
    
    private final RepositoryScanner repositoryScanner;

    public RepositoryHubScanner(GitRepositoryManager gitManager, List<ScanRepositoryEntry> repositoryEntries, int maxRepositoriesPerScan) throws IOException {
        this.repositoryScanner = new RepositoryScanner(gitManager, repositoryEntries, maxRepositoriesPerScan);
    }

    public boolean executeFullScan(boolean tempCloneMode) {
        try {
            System.out.println("Starting Repository Hub Scan");
            System.out.println("============================");
            System.out.println("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            System.out.println("Temporary Clone Mode: " + (tempCloneMode ? "ENABLED" : "DISABLED"));
            System.out.println();
            
            // Database schema is now managed by Flyway
            // Run migrations if needed
            try {
                org.flywaydb.core.Flyway flyway = org.flywaydb.core.Flyway.configure()
                    .dataSource(DatabaseConfig.getDataSource())
                    .load();
                flyway.migrate();
            } catch (Exception e) {
                System.err.println("Failed to run database migrations: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Database migration failed", e);
            }
            
            
            // Original behavior: clone all repositories first, then scan
            return processRepositoriesNormally(tempCloneMode);
            
            
        } catch (Exception e) {
            System.err.println("Repository Hub Scan failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    
    private String generateReport(Set<String> teamCodes) {
        // Generate final report AFTER data persistence
        try {
            String reportPath = "reports/weekly_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            Path reportDir = Paths.get(reportPath).getParent();
            if (!Files.exists(reportDir)) {
                Files.createDirectories(reportDir);
            }
            ExcelReportGenerator.generateWeeklyReport(reportPath, teamCodes);
            System.out.println("📊 Report generated successfully: " + reportPath);
            return reportPath;
        } catch (Exception e) {
            System.err.println("❌ Error generating report: " + e.getMessage());
            e.printStackTrace();
            return null;
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
    private boolean processRepositoriesNormally(boolean tempCloneMode) {
        

        // Scan repositories and persist data to database
        try {
            long startTime = System.currentTimeMillis();
            // Scan repositories
            TestCollectionSummary scanSummary = repositoryScanner.scanRepositories(tempCloneMode);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Scan completed in " + duration + " milliseconds");
    
            if (scanSummary != null) {
                long scanSessionId = storeScanResults(scanSummary, duration);
                System.out.println("Repository Hub Scan Completed Successfully!");
                
                // Generate report and store its path
                String reportPath = generateReport(scanSummary.getTeamCodes());
                if (reportPath != null) {
                    try {
                        // Convert to absolute path
                        Path absolutePath = Paths.get(reportPath).toAbsolutePath();
                        PersistScanResultsUseCase.updateReportFilePath(scanSessionId, absolutePath.toString());
                        System.out.println("Report path stored: " + absolutePath.toString());
                    } catch (SQLException e) {
                        System.err.println("Failed to store report path: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("Repository Hub Scan Failed!");
                return false;
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error scanning repositories: " + e.getMessage());
            e.printStackTrace();
            
        } catch (SQLException e) {
            System.err.println("Error persisting to database: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    

    private long storeScanResults(TestCollectionSummary summary, long duration) throws SQLException {
        System.out.println("\nPersisting data to database...");
        long scanSessionId = DataPersistenceService.persistScanSession(summary, duration);
        System.out.println("Data persisted successfully. Scan Session ID: " + scanSessionId);
        return scanSessionId;
    }
    

}
