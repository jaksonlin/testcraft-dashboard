package com.example.annotationextractor.casemodel;

import com.github.javaparser.ast.expr.AnnotationExpr;

/**
 * Interface for extracting test case IDs from annotations.
 * 
 * This allows the system to support multiple annotation types for test case linking,
 * not just @UnittestCaseInfo. Any annotation that can provide test case IDs can
 * implement this interface.
 * 
 * Design Philosophy:
 * - Annotation-agnostic: Support any annotation format
 * - Plugin-based: Easy to add new annotation types
 * - Future-proof: Not locked into one annotation design
 */
public interface TestCaseIdExtractor {
    
    /**
     * Check if this extractor can handle the given annotation.
     * 
     * @param annotation The annotation to check
     * @return true if this extractor supports the annotation
     */
    boolean supports(AnnotationExpr annotation);
    
    /**
     * Extract test case IDs from the annotation.
     * 
     * @param annotation The annotation to extract from
     * @return Array of test case IDs (e.g., ["TC-1234", "TC-5678"])
     */
    String[] extractTestCaseIds(AnnotationExpr annotation);
    
    /**
     * Get the priority of this extractor.
     * When multiple extractors support the same annotation,
     * the one with higher priority is used.
     * 
     * @return Priority value (higher = more priority)
     */
    default int getPriority() {
        return 0;
    }
}

