package com.example.annotationextractor.casemodel;

import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Registry for all test case ID extractors.
 * 
 * This class manages multiple extractors and allows the system to support
 * any annotation format for test case linking.
 * 
 * Usage:
 * 1. Register extractors (built-in or custom)
 * 2. Call extractTestCaseIds() with any annotation
 * 3. Registry finds appropriate extractor and extracts IDs
 */
public class TestCaseIdExtractorRegistry {
    
    private final List<TestCaseIdExtractor> extractors;
    
    public TestCaseIdExtractorRegistry() {
        this.extractors = new ArrayList<>();
        registerDefaultExtractors();
    }
    
    /**
     * Register default extractors
     */
    private void registerDefaultExtractors() {
        // Register built-in extractors
        register(new UnittestCaseInfoTestCaseIdExtractor());  // Current heavy annotation
        register(new TestCaseIdAnnotationExtractor());         // Future lightweight annotation
        register(new JUnitTagTestCaseIdExtractor());           // Standard JUnit @Tag support
    }
    
    /**
     * Register a custom extractor
     * 
     * @param extractor The extractor to register
     */
    public void register(TestCaseIdExtractor extractor) {
        if (extractor != null) {
            extractors.add(extractor);
            // Sort by priority (highest first)
            extractors.sort(Comparator.comparingInt(TestCaseIdExtractor::getPriority).reversed());
        }
    }
    
    /**
     * Extract test case IDs from an annotation using the appropriate extractor.
     * 
     * @param annotation The annotation to extract from
     * @return Array of test case IDs, or empty array if no extractor supports it
     */
    public String[] extractTestCaseIds(AnnotationExpr annotation) {
        if (annotation == null) {
            return new String[0];
        }
        
        // Find the first extractor that supports this annotation
        for (TestCaseIdExtractor extractor : extractors) {
            if (extractor.supports(annotation)) {
                return extractor.extractTestCaseIds(annotation);
            }
        }
        
        return new String[0];
    }
    
    /**
     * Extract test case IDs from multiple annotations.
     * Useful when a test method has multiple annotations.
     * 
     * @param annotations List of annotations
     * @return Combined array of all test case IDs
     */
    public String[] extractTestCaseIds(List<AnnotationExpr> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return new String[0];
        }
        
        List<String> allIds = new ArrayList<>();
        
        for (AnnotationExpr annotation : annotations) {
            String[] ids = extractTestCaseIds(annotation);
            for (String id : ids) {
                if (!allIds.contains(id)) { // Avoid duplicates
                    allIds.add(id);
                }
            }
        }
        
        return allIds.toArray(new String[0]);
    }
    
    /**
     * Get all registered extractors
     * 
     * @return List of extractors
     */
    public List<TestCaseIdExtractor> getExtractors() {
        return new ArrayList<>(extractors);
    }
    
    /**
     * Get extractors that support a specific annotation
     * 
     * @param annotation The annotation to check
     * @return List of supporting extractors
     */
    public List<TestCaseIdExtractor> getSupportingExtractors(AnnotationExpr annotation) {
        if (annotation == null) {
            return new ArrayList<>();
        }
        
        return extractors.stream()
                .filter(extractor -> extractor.supports(annotation))
                .collect(Collectors.toList());
    }
}

