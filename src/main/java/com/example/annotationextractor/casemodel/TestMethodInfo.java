package com.example.annotationextractor.casemodel;

import java.util.Arrays;

/**
 * Data model to hold information about a test method including its annotations
 */
public class TestMethodInfo {
    private String methodName;
    private String className;
    private String packageName;
    private String filePath;
    private UnittestCaseInfoData annotationData;
    private int lineNumber;
    private String[] testCaseIds;  // Test case IDs extracted from ANY annotation

    public TestMethodInfo() {
        this.methodName = "";
        this.className = "";
        this.packageName = "";
        this.filePath = "";
        this.annotationData = new UnittestCaseInfoData();
        this.lineNumber = 0;
        this.testCaseIds = new String[0];
    }

    public TestMethodInfo(String methodName, String className, String packageName, 
                         String filePath, UnittestCaseInfoData annotationData, int lineNumber) {
        this.methodName = methodName;
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
        this.annotationData = annotationData;
        this.lineNumber = lineNumber;
    }

    // Getters and Setters
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public UnittestCaseInfoData getAnnotationData() {
        return annotationData;
    }

    public void setAnnotationData(UnittestCaseInfoData annotationData) {
        this.annotationData = annotationData;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String[] getTestCaseIds() {
        return testCaseIds;
    }

    public void setTestCaseIds(String[] testCaseIds) {
        this.testCaseIds = testCaseIds;
    }

    /**
     * Check if this test method is linked to any test cases
     * 
     * @return true if test case IDs are present
     */
    public boolean hasTestCaseIds() {
        return testCaseIds != null && testCaseIds.length > 0;
    }

    @Override
    public String toString() {
        return "TestMethodInfo{" +
                "methodName='" + methodName + '\'' +
                ", className='" + className + '\'' +
                ", packageName='" + packageName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", annotationData=" + annotationData +
                ", lineNumber=" + lineNumber +
                ", testCaseIds=" + Arrays.toString(testCaseIds) +
                '}';
    }
}
