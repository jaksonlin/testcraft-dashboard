package com.example.annotationextractor.casemodel;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import java.util.ArrayList;
import java.util.List;

/**
 * Extractor for @UnittestCaseInfo annotation.
 * 
 * Extracts test case IDs from:
 * 1. testCaseIds field (preferred)
 * 2. tags field (backward compatibility)
 */
public class UnittestCaseInfoTestCaseIdExtractor implements TestCaseIdExtractor {
    
    private static final String ANNOTATION_NAME = "UnittestCaseInfo";
    private static final String TEST_CASE_IDS_FIELD = "testCaseIds";
    private static final String TAGS_FIELD = "tags";
    
    @Override
    public boolean supports(AnnotationExpr annotation) {
        if (annotation == null) {
            return false;
        }
        String name = annotation.getNameAsString();
        return ANNOTATION_NAME.equals(name);
    }
    
    @Override
    public String[] extractTestCaseIds(AnnotationExpr annotation) {
        if (!supports(annotation)) {
            return new String[0];
        }
        
        // Try to extract from testCaseIds field first
        if (annotation instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr normalAnnotation = (NormalAnnotationExpr) annotation;
            
            // Priority 1: testCaseIds field
            String[] fromTestCaseIds = extractFromField(normalAnnotation, TEST_CASE_IDS_FIELD);
            if (fromTestCaseIds.length > 0) {
                return fromTestCaseIds;
            }
            
            // Priority 2: tags field (backward compatibility)
            String[] fromTags = extractFromField(normalAnnotation, TAGS_FIELD);
            return filterTestCaseIds(fromTags);
        }
        
        return new String[0];
    }
    
    @Override
    public int getPriority() {
        return 100; // High priority for our main annotation
    }
    
    /**
     * Extract string array from a specific field
     */
    private String[] extractFromField(NormalAnnotationExpr annotation, String fieldName) {
        for (MemberValuePair pair : annotation.getPairs()) {
            if (fieldName.equals(pair.getNameAsString())) {
                return extractStringArrayValue(pair.getValue());
            }
        }
        return new String[0];
    }
    
    /**
     * Extract string array value from expression
     */
    private String[] extractStringArrayValue(Expression expression) {
        if (expression == null) {
            return new String[0];
        }
        
        if (expression instanceof ArrayInitializerExpr) {
            ArrayInitializerExpr arrayExpr = (ArrayInitializerExpr) expression;
            List<String> values = new ArrayList<>();
            
            for (Expression element : arrayExpr.getValues()) {
                values.add(extractStringValue(element));
            }
            
            return values.toArray(new String[0]);
        } else if (expression instanceof StringLiteralExpr) {
            // Single string value in array context
            return new String[]{((StringLiteralExpr) expression).getValue()};
        }
        
        return new String[0];
    }
    
    /**
     * Extract string value from expression
     */
    private String extractStringValue(Expression expression) {
        if (expression == null) {
            return "";
        }
        
        if (expression instanceof StringLiteralExpr) {
            return ((StringLiteralExpr) expression).getValue();
        }
        
        return expression.toString().replace("\"", "");
    }
    
    /**
     * Filter tags to only include test case IDs
     * Pattern: XX-123 to XXXX-123 (2-4 uppercase letters, hyphen, digits)
     */
    private String[] filterTestCaseIds(String[] tags) {
        if (tags == null || tags.length == 0) {
            return new String[0];
        }
        
        List<String> testCaseIds = new ArrayList<>();
        for (String tag : tags) {
            if (isTestCaseId(tag)) {
                testCaseIds.add(tag);
            }
        }
        
        return testCaseIds.toArray(new String[0]);
    }
    
    /**
     * Check if a string looks like a test case ID
     */
    private boolean isTestCaseId(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.matches("^[A-Z]{2,4}-\\d+$");
    }
}

