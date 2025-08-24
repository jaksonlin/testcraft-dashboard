package com.example.annotationextractor;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.database.DatabaseSchemaManager;
import com.example.annotationextractor.database.DataPersistenceService;
import com.example.annotationextractor.reporting.ExcelReportGenerator;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Main class to demonstrate the test collection system with database integration
 * This class scans a directory for Java git repositories and collects test information
 */
public class TestCollectionRunner {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java TestCollectionRunner <directory_path> [options]");
            System.out.println("Options:");
            System.out.println("  --init-db          Initialize database schema");
            System.out.println("  --generate-report  Generate Excel report after scan");
            System.out.println("  --report-path <path>  Specify report output path");
            System.out.println("Example: java TestCollectionRunner /path/to/repositories --init-db --generate-report");
            return;
        }
        
        String directoryPath = args[0];
        boolean initDb = false;
        boolean generateReport = false;
        String reportPath = "test_analytics_report.xlsx";
        
        // Parse command line options
        for (int i = 1; i < args.length; i++) {
            if ("--init-db".equals(args[i])) {
                initDb = true;
            } else if ("--generate-report".equals(args[i])) {
                generateReport = true;
            } else if ("--report-path".equals(args[i]) && i + 1 < args.length) {
                reportPath = args[++i];
            }
        }
        
        try {
            // Initialize database if requested
            if (initDb) {
                System.out.println("Initializing database...");
                DatabaseConfig.initialize();
                DatabaseSchemaManager.initializeSchema();
                System.out.println("Database initialized successfully!");
            }
            
            System.out.println("Starting test collection scan...");
            System.out.println("Scanning directory: " + directoryPath);
            
            // Record start time
            long startTime = System.currentTimeMillis();
            
            // Scan the directory for repositories and collect test information
            TestCollectionSummary summary = RepositoryScanner.scanRepositories(directoryPath);
            
            // Calculate scan duration
            long scanDuration = System.currentTimeMillis() - startTime;
            
            // Display the results
            displayResults(summary, scanDuration);
            
            // Persist to database if schema exists
            if (DatabaseSchemaManager.schemaExists()) {
                System.out.println("\nPersisting data to database...");
                try {
                    long scanSessionId = DataPersistenceService.persistScanSession(summary, scanDuration);
                    System.out.println("Data persisted successfully. Scan Session ID: " + scanSessionId);
                } catch (SQLException e) {
                    System.err.println("Error persisting to database: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("\nDatabase schema not found. Use --init-db to initialize.");
            }
            
            // Generate report if requested
            if (generateReport) {
                if (DatabaseSchemaManager.schemaExists()) {
                    System.out.println("\nGenerating Excel report...");
                    try {
                        ExcelReportGenerator.generateWeeklyReport(reportPath);
                        System.out.println("Report generated successfully: " + reportPath);
                    } catch (Exception e) {
                        System.err.println("Error generating report: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Cannot generate report: Database schema not initialized.");
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error scanning repositories: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close database connections
            DatabaseConfig.close();
        }
    }
    
    /**
     * Display the collected test information in a formatted way
     */
    private static void displayResults(TestCollectionSummary summary, long scanDuration) {
        System.out.println("\n" + repeatChar('=', 80));
        System.out.println("TEST COLLECTION SUMMARY");
        System.out.println(repeatChar('=', 80));
        System.out.println("Scan Directory: " + summary.getScanDirectory());
        System.out.println("Scan Timestamp: " + new java.util.Date(summary.getScanTimestamp()));
        System.out.println("Scan Duration: " + scanDuration + " ms");
        System.out.println("Total Repositories: " + summary.getTotalRepositories());
        System.out.println("Total Test Classes: " + summary.getTotalTestClasses());
        System.out.println("Total Test Methods: " + summary.getTotalTestMethods());
        System.out.println("Total Annotated Test Methods: " + summary.getTotalAnnotatedTestMethods());
        
        if (summary.getTotalTestMethods() > 0) {
            double overallCoverage = (double) summary.getTotalAnnotatedTestMethods() / summary.getTotalTestMethods() * 100;
            System.out.println("Overall Annotation Coverage: " + String.format("%.2f%%", overallCoverage));
        }
        
        if (summary.getTotalRepositories() == 0) {
            System.out.println("\nNo repositories with test classes found.");
            return;
        }
        
        System.out.println("\n" + repeatChar('-', 80));
        System.out.println("REPOSITORY DETAILS");
        System.out.println(repeatChar('-', 80));
        
        for (RepositoryTestInfo repo : summary.getRepositories()) {
            System.out.println("\nRepository: " + repo.getRepositoryName());
            System.out.println("Path: " + repo.getRepositoryPath());
            System.out.println("Test Classes: " + repo.getTotalTestClasses());
            System.out.println("Test Methods: " + repo.getTotalTestMethods());
            System.out.println("Annotated Methods: " + repo.getTotalAnnotatedTestMethods());
            
            if (repo.getTotalTestMethods() > 0) {
                double repoCoverage = (double) repo.getTotalAnnotatedTestMethods() / repo.getTotalTestMethods() * 100;
                System.out.println("Coverage Rate: " + String.format("%.2f%%", repoCoverage));
            }
            
            if (repo.getTotalTestClasses() > 0) {
                System.out.println("\n  Test Classes:");
                for (TestClassInfo testClass : repo.getTestClasses()) {
                    System.out.println("    " + testClass.getClassName() + 
                                     " (" + testClass.getPackageName() + ")");
                    System.out.println("      File: " + testClass.getFilePath());
                    System.out.println("      Methods: " + testClass.getTotalTestMethods() + 
                                     " (Annotated: " + testClass.getAnnotatedTestMethods() + ")");
                    
                    if (testClass.getTotalTestMethods() > 0) {
                        double classCoverage = (double) testClass.getAnnotatedTestMethods() / testClass.getTotalTestMethods() * 100;
                        System.out.println("      Coverage: " + String.format("%.2f%%", classCoverage));
                    }
                    
                    // Show details of annotated methods
                    for (TestMethodInfo method : testClass.getTestMethods()) {
                        if (method.getAnnotationData() != null && 
                            !method.getAnnotationData().getTitle().isEmpty()) {
                            System.out.println("        " + method.getMethodName() + 
                                             " - " + method.getAnnotationData().getTitle());
                        }
                    }
                }
            }
        }
        
        System.out.println("\n" + repeatChar('=', 80));
        System.out.println("SCAN COMPLETED SUCCESSFULLY");
        System.out.println(repeatChar('=', 80));
    }

    /**
     * Helper method to repeat a character for a given count (for Java < 11 compatibility)
     */
    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
