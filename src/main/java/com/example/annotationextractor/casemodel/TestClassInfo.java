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
    private List<String> importedTypes;
    private int totalTestMethods;
    private int annotatedTestMethods;
    private Integer classLineNumber;
    private String testClassContent;
    private String helperClassesLineNumbers;
    private int classLoc;
    private List<String> referencedTypes;

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
        this.classLineNumber = null;
        this.testClassContent = null;
        this.helperClassesLineNumbers = null;
        this.classLoc = 0;
        this.importedTypes = new ArrayList<>();
        this.referencedTypes = new ArrayList<>();
    }

    public TestClassInfo(String className, String packageName, String filePath) {
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
        this.testMethods = new ArrayList<>();
        this.totalTestMethods = 0;
        this.annotatedTestMethods = 0;
        this.classLineNumber = null;
        this.testClassContent = null;
        this.helperClassesLineNumbers = null;
        this.classLoc = 0;
        this.importedTypes = new ArrayList<>();
        this.referencedTypes = new ArrayList<>();
    }

    public void addTestMethod(TestMethodInfo testMethod) {
        this.testMethods.add(testMethod);
        this.totalTestMethods++;
        if (testMethod.getAnnotationData() != null && testMethod.getAnnotationData().getTitle() != null && !testMethod.getAnnotationData().getTitle().isEmpty()) {
            this.annotatedTestMethods++;
        }
    }

    public void addImportedType(String importedType) {
        this.importedTypes.add(importedType);
    }

    public void addReferencedType(String referencedType) {
        this.referencedTypes.add(referencedType);
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

    public Integer getClassLineNumber() {
        return classLineNumber;
    }

    public void setClassLineNumber(Integer classLineNumber) {
        this.classLineNumber = classLineNumber;
    }

    public String getTestClassContent() {
        return testClassContent;
    }

    public void setTestClassContent(String testClassContent) {
        this.testClassContent = testClassContent;
    }

    public String getHelperClassesLineNumbers() {
        return helperClassesLineNumbers;
    }

    public void setHelperClassesLineNumbers(String helperClassesLineNumbers) {
        this.helperClassesLineNumbers = helperClassesLineNumbers;
    }

    public int getClassLoc() {
        return classLoc;
    }

    public void setClassLoc(int classLoc) {
        this.classLoc = classLoc;
    }

    public List<String> getImportedTypes() {
        return importedTypes;
    }

    public void setImportedTypes(List<String> importedTypes) {
        this.importedTypes = importedTypes;
    }

    public List<String> getReferencedTypes() {
        return referencedTypes;
    }

    public void setReferencedTypes(List<String> referencedTypes) {
        this.referencedTypes = referencedTypes;
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
