package com.example.annotationextractor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import java.io.IOException;

/**
 * Example usage class demonstrating both annotation extraction and repository scanning
 */
public class ExampleUsage {
    
    public static void main(String[] args) {
        System.out.println("Annotation Extractor Example Usage");
        System.out.println("=================================");
        
        // Example 1: Extract annotation from a single annotation expression
        demonstrateAnnotationExtraction();
        
        System.out.println("\n" + new String(new char[50]).replace('\0', '='));
        
        // Example 2: Scan repositories (if directory is provided)
        if (args.length > 0) {
            demonstrateRepositoryScanning(args[0]);
        } else {
            System.out.println("To demonstrate repository scanning, provide a directory path:");
            System.out.println("java ExampleUsage <directory_path>");
        }
    }
    
    /**
     * Demonstrate the original annotation extraction functionality
     */
    private static void demonstrateAnnotationExtraction() {
        System.out.println("Example 1: Annotation Extraction");
        System.out.println("--------------------------------");
        
        try {
            // Create a sample annotation expression
            JavaParser parser = new JavaParser();
            String annotationCode = "@UnittestCaseInfo(title=\"Sample Test\", author=\"John Doe\", status=\"READY\")";
            AnnotationExpr annotationExpr = parser.parseAnnotation(annotationCode).getResult().orElse(null);
            
            if (annotationExpr != null) {
                // Extract the annotation values
                UnittestCaseInfoData extractedData = UnittestCaseInfoExtractor.extractAnnotationValues(annotationExpr);
                
                System.out.println("Extracted annotation data:");
                System.out.println("  Title: " + extractedData.getTitle());
                System.out.println("  Author: " + extractedData.getAuthor());
                System.out.println("  Status: " + extractedData.getStatus());
                System.out.println("  Target Class: " + extractedData.getTargetClass());
                System.out.println("  Description: " + extractedData.getDescription());
            } else {
                System.out.println("Failed to parse annotation expression");
            }
            
        } catch (Exception e) {
            System.err.println("Error in annotation extraction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Demonstrate the new repository scanning functionality
     */
    private static void demonstrateRepositoryScanning(String directoryPath) {
        System.out.println("Example 2: Repository Scanning");
        System.out.println("--------------------------------");
        
        try {
            System.out.println("Scanning directory: " + directoryPath);
            
            // Scan the directory for repositories and collect test information
            TestCollectionSummary summary = RepositoryScanner.scanRepositories(directoryPath);
            
            // Display summary
            System.out.println("\nScan Results:");
            System.out.println("  Total Repositories: " + summary.getTotalRepositories());
            System.out.println("  Total Test Classes: " + summary.getTotalTestClasses());
            System.out.println("  Total Test Methods: " + summary.getTotalTestMethods());
            System.out.println("  Total Annotated Methods: " + summary.getTotalAnnotatedTestMethods());
            
            if (summary.getTotalRepositories() > 0) {
                System.out.println("\nRepository Details:");
                for (RepositoryTestInfo repo : summary.getRepositories()) {
                    System.out.println("  " + repo.getRepositoryName() + 
                                     " - " + repo.getTotalTestClasses() + " test classes, " +
                                     repo.getTotalAnnotatedTestMethods() + " annotated methods");
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error scanning repositories: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
