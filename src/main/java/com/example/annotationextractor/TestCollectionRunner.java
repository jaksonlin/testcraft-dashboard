package com.example.annotationextractor;

import java.io.IOException;

/**
 * Main class to demonstrate the test collection system
 * This class scans a directory for Java git repositories and collects test information
 */
public class TestCollectionRunner {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java TestCollectionRunner <directory_path>");
            System.out.println("Example: java TestCollectionRunner /path/to/repositories");
            return;
        }
        
        String directoryPath = args[0];
        
        try {
            System.out.println("Starting test collection scan...");
            System.out.println("Scanning directory: " + directoryPath);
            
            // Scan the directory for repositories and collect test information
            TestCollectionSummary summary = RepositoryScanner.scanRepositories(directoryPath);
            
            // Display the results
            displayResults(summary);
            
        } catch (IOException e) {
            System.err.println("Error scanning repositories: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Display the collected test information in a formatted way
     */
    private static void displayResults(TestCollectionSummary summary) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST COLLECTION SUMMARY");
        System.out.println("=".repeat(80));
        System.out.println("Scan Directory: " + summary.getScanDirectory());
        System.out.println("Scan Timestamp: " + new java.util.Date(summary.getScanTimestamp()));
        System.out.println("Total Repositories: " + summary.getTotalRepositories());
        System.out.println("Total Test Classes: " + summary.getTotalTestClasses());
        System.out.println("Total Test Methods: " + summary.getTotalTestMethods());
        System.out.println("Total Annotated Test Methods: " + summary.getTotalAnnotatedTestMethods());
        
        if (summary.getTotalRepositories() == 0) {
            System.out.println("\nNo repositories with test classes found.");
            return;
        }
        
        System.out.println("\n" + "-".repeat(80));
        System.out.println("REPOSITORY DETAILS");
        System.out.println("-".repeat(80));
        
        for (RepositoryTestInfo repo : summary.getRepositories()) {
            System.out.println("\nRepository: " + repo.getRepositoryName());
            System.out.println("Path: " + repo.getRepositoryPath());
            System.out.println("Test Classes: " + repo.getTotalTestClasses());
            System.out.println("Test Methods: " + repo.getTotalTestMethods());
            System.out.println("Annotated Methods: " + repo.getTotalAnnotatedTestMethods());
            
            if (repo.getTotalTestClasses() > 0) {
                System.out.println("\n  Test Classes:");
                for (TestClassInfo testClass : repo.getTestClasses()) {
                    System.out.println("    " + testClass.getClassName() + 
                                     " (" + testClass.getPackageName() + ")");
                    System.out.println("      File: " + testClass.getFilePath());
                    System.out.println("      Methods: " + testClass.getTotalTestMethods() + 
                                     " (Annotated: " + testClass.getAnnotatedTestMethods() + ")");
                    
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
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SCAN COMPLETED SUCCESSFULLY");
        System.out.println("=".repeat(80));
    }
}
