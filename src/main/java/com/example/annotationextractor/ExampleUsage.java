package com.example.annotationextractor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.Arrays;

/**
 * Example class demonstrating how to use the UnittestCaseInfoExtractor
 */
public class ExampleUsage {

    public static void main(String[] args) {
        // Example 1: Parse Java source code and extract annotation values
        extractFromSourceCode();
        
        // Example 2: Process annotation directly (if you already have AnnotationExpr)
        processAnnotationDirectly();
    }

    /**
     * Example: Extract annotation values from Java source code
     */
    private static void extractFromSourceCode() {
        System.out.println("=== Example 1: Extract from Source Code ===");
        
        // Sample Java code with UnittestCaseInfo annotation
        String javaCode = 
            "public class UserServiceTest {\n" +
            "    @UnittestCaseInfo(\n" +
            "        author = \"Alice Johnson\",\n" +
            "        title = \"Test User Registration\",\n" +
            "        targetClass = \"UserService\",\n" +
            "        targetMethod = \"registerUser\",\n" +
            "        testPoints = {\"validation\", \"persistence\", \"email\"},\n" +
            "        description = \"Test user registration with valid data\",\n" +
            "        tags = {\"registration\", \"user\", \"positive\"},\n" +
            "        status = UnittestCaseStatus.READY,\n" +
            "        relatedRequirements = {\"REQ-USER-001\"},\n" +
            "        lastUpdateTime = \"2024-01-20\",\n" +
            "        lastUpdateAuthor = \"Bob Smith\"\n" +
            "    )\n" +
            "    public void testUserRegistration() {\n" +
            "        // Test implementation would go here\n" +
            "    }\n" +
            "}";

        try {
            // Parse the Java code
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(javaCode).getResult().get();
            
            // Find the method with annotation
            MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();
            
            // Get the annotation
            AnnotationExpr annotation = method.getAnnotationByName("UnittestCaseInfo").get();
            
            // Extract values using our extractor
            UnittestCaseInfoData extractedData = UnittestCaseInfoExtractor.extractAnnotationValues(annotation);
            
            // Display extracted information
            System.out.println("Extracted Test Case Information:");
            System.out.println("  Author: " + extractedData.getAuthor());
            System.out.println("  Title: " + extractedData.getTitle());
            System.out.println("  Target Class: " + extractedData.getTargetClass());
            System.out.println("  Target Method: " + extractedData.getTargetMethod());
            System.out.println("  Test Points: " + Arrays.toString(extractedData.getTestPoints()));
            System.out.println("  Description: " + extractedData.getDescription());
            System.out.println("  Tags: " + Arrays.toString(extractedData.getTags()));
            System.out.println("  Status: " + extractedData.getStatus());
            System.out.println("  Related Requirements: " + Arrays.toString(extractedData.getRelatedRequirements()));
            System.out.println("  Last Update: " + extractedData.getLastUpdateTime());
            System.out.println("  Last Author: " + extractedData.getLastUpdateAuthor());
            
        } catch (Exception e) {
            System.err.println("Error processing source code: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Example: Process annotation directly (useful when you already have AnnotationExpr)
     */
    private static void processAnnotationDirectly() {
        System.out.println("\n=== Example 2: Process Annotation Directly ===");
        
        // This would be your AnnotationExpr from JavaParser
        // For demonstration, we'll create a simple one by parsing minimal code
        String minimalCode = 
            "public class Dummy {\n" +
            "    @UnittestCaseInfo(\n" +
            "        author = \"Demo User\",\n" +
            "        title = \"Demo Test Case\"\n" +
            "    )\n" +
            "    public void demo() {}\n" +
            "}";
        
        try {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(minimalCode).getResult().get();
            MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();
            AnnotationExpr annotation = method.getAnnotationByName("UnittestCaseInfo").get();
            
            // Extract values
            UnittestCaseInfoData data = UnittestCaseInfoExtractor.extractAnnotationValues(annotation);
            
            System.out.println("Direct Annotation Processing:");
            System.out.println("  Author: " + data.getAuthor());
            System.out.println("  Title: " + data.getTitle());
            System.out.println("  Status (default): " + data.getStatus());
            System.out.println("  Test Points (default): " + Arrays.toString(data.getTestPoints()));
            
        } catch (Exception e) {
            System.err.println("Error processing annotation directly: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Example: How to use the extracted data for reporting or analysis
     */
    public static void analyzeTestCases(UnittestCaseInfoData[] testCases) {
        System.out.println("\n=== Test Case Analysis ===");
        
        int totalTests = testCases.length;
        int readyTests = 0;
        int inProgressTests = 0;
        int todoTests = 0;
        
        for (UnittestCaseInfoData testCase : testCases) {
            String status = testCase.getStatus();
            if (status.contains("READY")) {
                readyTests++;
            } else if (status.contains("IN_PROGRESS")) {
                inProgressTests++;
            } else if (status.contains("TODO")) {
                todoTests++;
            }
        }
        
        System.out.println("Total Test Cases: " + totalTests);
        System.out.println("Ready: " + readyTests);
        System.out.println("In Progress: " + inProgressTests);
        System.out.println("TODO: " + todoTests);
        System.out.println("Completion Rate: " + String.format("%.1f%%", (double)(readyTests + inProgressTests) / totalTests * 100));
    }
}
