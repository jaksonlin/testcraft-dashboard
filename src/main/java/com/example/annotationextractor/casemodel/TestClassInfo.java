package com.example.annotationextractor.casemodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model to hold information about a test class including all its test methods
 */
public class TestClassInfo {
    private String className;
    private String packageName;
    private String filePath;
    private List<TestMethodInfo> testMethods;
    private int totalTestMethods;
    private int annotatedTestMethods;

    public void setTotalTestMethods(int totalTestMethods) {
        this.totalTestMethods = totalTestMethods;
    }

    public void setAnnotatedTestMethods(int annotatedTestMethods) {
        this.annotatedTestMethods = annotatedTestMethods;
    }

    public TestClassInfo() {
        this.className = "";
        this.packageName = "";
        this.filePath = "";
        this.testMethods = new ArrayList<>();
        this.totalTestMethods = 0;
        this.annotatedTestMethods = 0;
    }

    public TestClassInfo(String className, String packageName, String filePath) {
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
        this.testMethods = new ArrayList<>();
        this.totalTestMethods = 0;
        this.annotatedTestMethods = 0;
    }

    public void addTestMethod(TestMethodInfo testMethod) {
        this.testMethods.add(testMethod);
        this.totalTestMethods++;
        if (testMethod.getAnnotationData() != null && testMethod.getAnnotationData().getTitle() != null && !testMethod.getAnnotationData().getTitle().isEmpty()) {
            this.annotatedTestMethods++;
        }
    }

    // Getters and Setters
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

    public List<TestMethodInfo> getTestMethods() {
        return testMethods;
    }

    public void setTestMethods(List<TestMethodInfo> testMethods) {
        this.testMethods = testMethods;
        this.totalTestMethods = testMethods.size();
        this.annotatedTestMethods = 0;
        for (TestMethodInfo method : testMethods) {
            if (method.getAnnotationData() != null && 
                !method.getAnnotationData().getTitle().isEmpty()) {
                this.annotatedTestMethods++;
            }
        }
    }

    public int getTotalTestMethods() {
        return totalTestMethods;
    }

    public int getAnnotatedTestMethods() {
        return annotatedTestMethods;
    }

    @Override
    public String toString() {
        return "TestClassInfo{" +
                "className='" + className + '\'' +
                ", packageName='" + packageName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", totalTestMethods=" + totalTestMethods +
                ", annotatedTestMethods=" + annotatedTestMethods +
                ", testMethods=" + testMethods +
                '}';
    }
}
