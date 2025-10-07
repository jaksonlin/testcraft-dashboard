package com.example.annotationextractor.casemodel;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Extractor for @TestCaseId annotation - the lightweight future design.
 * 
 * Supports:
 * - @TestCaseId("TC-1234")
 * - @TestCaseId({"TC-1234", "TC-5678"})
 * - @TestCaseId(value = {"TC-1234"})
 */
public class TestCaseIdAnnotationExtractor implements TestCaseIdExtractor {
    
    private static final String ANNOTATION_NAME = "TestCaseId";
    
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
        
        // @TestCaseId("TC-1234") or @TestCaseId({"TC-1234", "TC-5678"})
        if (annotation instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr singleMember = (SingleMemberAnnotationExpr) annotation;
            return extractArrayValue(singleMember.getMemberValue());
        }
        
        // @TestCaseId(value = {"TC-1234"})
        if (annotation instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr normalAnnotation = (NormalAnnotationExpr) annotation;
            for (MemberValuePair pair : normalAnnotation.getPairs()) {
                if ("value".equals(pair.getNameAsString())) {
                    return extractArrayValue(pair.getValue());
                }
            }
        }
        
        return new String[0];
    }
    
    @Override
    public int getPriority() {
        return 90; // High priority (slightly less than UnittestCaseInfo)
    }
    
    private String[] extractArrayValue(Expression expression) {
        if (expression == null) {
            return new String[0];
        }
        
        // Array: {"TC-1234", "TC-5678"}
        if (expression instanceof ArrayInitializerExpr) {
            ArrayInitializerExpr arrayExpr = (ArrayInitializerExpr) expression;
            List<String> values = new ArrayList<>();
            
            for (Expression element : arrayExpr.getValues()) {
                String value = extractStringValue(element);
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
            
            return values.toArray(new String[0]);
        }
        
        // Single value: "TC-1234"
        String singleValue = extractStringValue(expression);
        if (!singleValue.isEmpty()) {
            return new String[]{singleValue};
        }
        
        return new String[0];
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
}

