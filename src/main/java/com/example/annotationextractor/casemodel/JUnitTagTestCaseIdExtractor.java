package com.example.annotationextractor.casemodel;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;

/**
 * Extractor for JUnit 5 @Tag annotation.
 * 
 * Example:
 * @Tag("TC-1234")
 * @Tag("ID-5678")
 * 
 * This allows teams to use standard JUnit @Tag for test case linking
 * instead of custom annotations.
 */
public class JUnitTagTestCaseIdExtractor implements TestCaseIdExtractor {
    
    private static final String ANNOTATION_NAME = "Tag";
    
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
        
        // @Tag("TC-1234")
        if (annotation instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr singleMember = (SingleMemberAnnotationExpr) annotation;
            String value = extractStringValue(singleMember.getMemberValue());
            
            if (isTestCaseId(value)) {
                return new String[]{value};
            }
        }
        
        return new String[0];
    }
    
    @Override
    public int getPriority() {
        return 50; // Medium priority
    }
    
    private String extractStringValue(Expression expression) {
        if (expression == null) {
            return "";
        }
        
        if (expression instanceof StringLiteralExpr) {
            return ((StringLiteralExpr) expression).getValue();
        }
        
        return expression.toString().replace("\"", "");
    }
    
    private boolean isTestCaseId(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.matches("^[A-Z]{2,4}-\\d+$");
    }
}

