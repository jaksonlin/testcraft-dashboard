package com.example.annotationextractor.casemodel;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import java.util.ArrayList;
import java.util.List;

/**
 * Extractor class to process UnittestCaseInfo annotation and extract all values
 * into a structured data model for later inspection.
 */
public class UnittestCaseInfoExtractor {

    /**
     * Main method to extract all annotation values from AnnotationExpr
     * 
     * @param annotationExpr The AnnotationExpr to process
     * @return UnittestCaseInfoData object containing all extracted values
     */
    public static UnittestCaseInfoData extractAnnotationValues(AnnotationExpr annotationExpr) {
        if (annotationExpr == null) {
            throw new IllegalArgumentException("AnnotationExpr cannot be null");
        }

        UnittestCaseInfoData data = new UnittestCaseInfoData();

        // Handle different types of annotations
        if (annotationExpr instanceof SingleMemberAnnotationExpr) {
            // Single member annotation: @UnittestCaseInfo("value")
            SingleMemberAnnotationExpr singleMember = (SingleMemberAnnotationExpr) annotationExpr;
            // For single member, we assume it's the title
            data.setTitle(extractStringValue(singleMember.getMemberValue()));
        } else if (annotationExpr instanceof NormalAnnotationExpr) {
            // Normal annotation: @UnittestCaseInfo(key1="value1", key2="value2")
            NormalAnnotationExpr normalAnnotation = (NormalAnnotationExpr) annotationExpr;
            processNormalAnnotation(normalAnnotation, data);
        }

        return data;
    }

    /**
     * Process normal annotation with multiple key-value pairs
     */
    private static void processNormalAnnotation(NormalAnnotationExpr annotation, UnittestCaseInfoData data) {
        for (MemberValuePair pair : annotation.getPairs()) {
            String key = pair.getNameAsString();
            Expression value = pair.getValue();
            
            switch (key) {
                case "author":
                    data.setAuthor(extractStringValue(value));
                    break;
                case "title":
                    data.setTitle(extractStringValue(value));
                    break;
                case "targetClass":
                    data.setTargetClass(extractStringValue(value));
                    break;
                case "targetMethod":
                    data.setTargetMethod(extractStringValue(value));
                    break;
                case "testPoints":
                    data.setTestPoints(extractStringArrayValue(value));
                    break;
                case "description":
                    data.setDescription(extractStringValue(value));
                    break;
                case "tags":
                    data.setTags(extractStringArrayValue(value));
                    break;
                case "testCaseIds":
                    data.setTestCaseIds(extractStringArrayValue(value));
                    break;
                case "status":
                    data.setStatus(extractStatusValue(value));
                    break;
                case "relatedRequirements":
                    data.setRelatedRequirements(extractStringArrayValue(value));
                    break;
                case "relatedDefects":
                    data.setRelatedDefects(extractStringArrayValue(value));
                    break;
                case "relatedTestcases":
                    data.setRelatedTestcases(extractStringArrayValue(value));
                    break;
                case "lastUpdateTime":
                    data.setLastUpdateTime(extractStringValue(value));
                    break;
                case "lastUpdateAuthor":
                    data.setLastUpdateAuthor(extractStringValue(value));
                    break;
                case "methodSignature":
                    data.setMethodSignature(extractStringValue(value));
                    break;
                default:
                    // Unknown key, ignore
                    break;
            }
        }
    }

    /**
     * Extract string value from expression
     */
    private static String extractStringValue(Expression expression) {
        if (expression == null) {
            return "";
        }
        
        if (expression instanceof StringLiteralExpr) {
            return ((StringLiteralExpr) expression).getValue();
        } else if (expression instanceof NameExpr) {
            return ((NameExpr) expression).getNameAsString();
        } else if (expression instanceof FieldAccessExpr) {
            return ((FieldAccessExpr) expression).toString();
        }
        
        return expression.toString();
    }

    /**
     * Extract string array value from expression
     */
    private static String[] extractStringArrayValue(Expression expression) {
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
     * Extract status value from expression
     */
    private static String extractStatusValue(Expression expression) {
        if (expression == null) {
            return "TODO";
        }
        
        if (expression instanceof FieldAccessExpr) {
            FieldAccessExpr fieldExpr = (FieldAccessExpr) expression;
            return fieldExpr.getScope().toString() + "." + fieldExpr.getNameAsString();
        } else if (expression instanceof NameExpr) {
            return ((NameExpr) expression).getNameAsString();
        } else if (expression instanceof StringLiteralExpr) {
            return ((StringLiteralExpr) expression).getValue();
        }
        
        return expression.toString();
    }
}
