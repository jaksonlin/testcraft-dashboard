package com.example.annotationextractor.casemodel;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parser class to extract test method information from Java test classes
 */
public class TestClassParser {
    
    // Registry for extracting test case IDs from any annotation
    private static final TestCaseIdExtractorRegistry testCaseIdRegistry = new TestCaseIdExtractorRegistry();
    
    /**
     * Parse a Java test class file and extract all test method information
     * 
     * @param filePath Path to the Java test class file
     * @return TestClassInfo object containing all extracted test method information
     * @throws IOException if file cannot be read
     */
    public static TestClassInfo parseTestClass(Path filePath) throws IOException {
        File file = filePath.toFile();
        if (!file.exists() || !file.canRead()) {
            throw new IOException("Cannot read file: " + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(fis).getResult().orElse(null);
            if (cu == null) {
                throw new IOException("Failed to parse Java file: " + filePath);
            }
            
            TestClassVisitor visitor = new TestClassVisitor(filePath);
            cu.accept(visitor, null);
            
            // Check if we found multiple public classes (invalid Java)
            if (visitor.hasMultiplePublicClasses()) {
                System.err.println("⚠️  WARNING: File contains multiple public classes: " + filePath);
                System.err.println("   Public classes found: " + visitor.getPublicClassNames());
                System.err.println("   This is invalid Java and will be skipped.");
                return new TestClassInfo(); // Return empty result
            }
            
            return visitor.getTestClassInfo();
        }
    }
    
    /**
     * Check if a method is a test method based on annotations
     */
    private static boolean isTestMethod(MethodDeclaration method) {
        for (AnnotationExpr annotation : method.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            if (annotationName.equals("Test") || 
                annotationName.equals("org.junit.Test") ||
                annotationName.equals("org.junit.jupiter.api.Test") ||
                annotationName.equals("junit.framework.TestCase")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if a class is a test class
     */
    private static boolean isTestClass(ClassOrInterfaceDeclaration classDecl) {
        // Only process PUBLIC classes (JUnit only executes public classes)
        if (!classDecl.isPublic()) {
            return false;
        }
        
        // Check if class name ends with "Test" or "Tests"
        String className = classDecl.getNameAsString();
        if (className.endsWith("Test") || className.endsWith("Tests")) {
            return true;
        }
        
        // Check if class has test annotations
        for (AnnotationExpr annotation : classDecl.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            if (annotationName.equals("Test") || 
                annotationName.equals("org.junit.Test") ||
                annotationName.equals("org.junit.jupiter.api.Test")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Visitor class to traverse the AST and extract test method information
     */
    private static class TestClassVisitor extends VoidVisitorAdapter<Void> {
        private final Path filePath;
        private TestClassInfo testClassInfo;
        private String packageName = "";
        private List<String> publicClassNames = new ArrayList<>();
        private boolean hasMultiplePublicClasses = false;
        
        public TestClassVisitor(Path filePath) {
            this.filePath = filePath;
            this.testClassInfo = new TestClassInfo();
        }
        
        @Override
        public void visit(CompilationUnit cu, Void arg) {
            // Extract package name
            Optional<com.github.javaparser.ast.PackageDeclaration> packageDecl = cu.getPackageDeclaration();
            if (packageDecl.isPresent()) {
                this.packageName = packageDecl.get().getNameAsString();
            }
            
            super.visit(cu, arg);
        }
        
        @Override
        public void visit(ClassOrInterfaceDeclaration classDecl, Void arg) {
            // Debug: Log all classes found
            String className = classDecl.getNameAsString();
            boolean isPublic = classDecl.isPublic();
            System.out.println("🔍 Found class: " + className + " (public: " + isPublic + ")");
            
            // Track public classes for validation
            if (isPublic) {
                publicClassNames.add(className);
                if (publicClassNames.size() > 1) {
                    hasMultiplePublicClasses = true;
                }
            }
            
            // Only process test classes
            if (isTestClass(classDecl)) {
                System.out.println("✅ Processing test class: " + className);
                testClassInfo.setClassName(classDecl.getNameAsString());
                testClassInfo.setPackageName(packageName);
                testClassInfo.setFilePath(filePath.toString());
                
                super.visit(classDecl, arg);
            } else {
                System.out.println("❌ Skipping non-test class: " + className);
            }
        }
        
        @Override
        public void visit(MethodDeclaration methodDecl, Void arg) {
            // Only process test methods
            if (isTestMethod(methodDecl)) {
                TestMethodInfo testMethodInfo = new TestMethodInfo();
                testMethodInfo.setMethodName(methodDecl.getNameAsString());
                testMethodInfo.setClassName(testClassInfo.getClassName());
                testMethodInfo.setPackageName(testClassInfo.getPackageName());
                testMethodInfo.setFilePath(testClassInfo.getFilePath());
                testMethodInfo.setLineNumber(methodDecl.getBegin().get().line);
                
                List<AnnotationExpr> annotations = methodDecl.getAnnotations();
                
                // Extract test case IDs from ALL annotations using the plugin-based registry
                String[] testCaseIds = testCaseIdRegistry.extractTestCaseIds(annotations);
                testMethodInfo.setTestCaseIds(testCaseIds);
                
                // Also extract UnittestCaseInfo annotation data for backward compatibility
                for (AnnotationExpr annotation : annotations) {
                    if (annotation.getNameAsString().equals("UnittestCaseInfo")) {
                        UnittestCaseInfoData annotationData = UnittestCaseInfoExtractor.extractAnnotationValues(annotation);
                        testMethodInfo.setAnnotationData(annotationData);
                        break;
                    }
                }
                
                testClassInfo.addTestMethod(testMethodInfo);
            }
        }
        
        public TestClassInfo getTestClassInfo() {
            return testClassInfo;
        }
        
        public boolean hasMultiplePublicClasses() {
            return hasMultiplePublicClasses;
        }
        
        public String getPublicClassNames() {
            return String.join(", ", publicClassNames);
        }
    }
}
