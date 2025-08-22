package com.example.annotationextractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model to hold information about all test classes found in a single repository
 */
public class RepositoryTestInfo {
    private String repositoryName;
    private String repositoryPath;
    private List<TestClassInfo> testClasses;
    private int totalTestClasses;
    private int totalTestMethods;
    private int totalAnnotatedTestMethods;

    public RepositoryTestInfo() {
        this.repositoryName = "";
        this.repositoryPath = "";
        this.testClasses = new ArrayList<>();
        this.totalTestClasses = 0;
        this.totalTestMethods = 0;
        this.totalAnnotatedTestMethods = 0;
    }

    public RepositoryTestInfo(String repositoryName, String repositoryPath) {
        this.repositoryName = repositoryName;
        this.repositoryPath = repositoryPath;
        this.testClasses = new ArrayList<>();
        this.totalTestClasses = 0;
        this.totalTestMethods = 0;
        this.totalAnnotatedTestMethods = 0;
    }

    public void addTestClass(TestClassInfo testClass) {
        this.testClasses.add(testClass);
        this.totalTestClasses++;
        this.totalTestMethods += testClass.getTotalTestMethods();
        this.totalAnnotatedTestMethods += testClass.getAnnotatedTestMethods();
    }

    // Getters and Setters
    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public List<TestClassInfo> getTestClasses() {
        return testClasses;
    }

    public void setTestClasses(List<TestClassInfo> testClasses) {
        this.testClasses = testClasses;
        this.totalTestClasses = testClasses.size();
        this.totalTestMethods = 0;
        this.totalAnnotatedTestMethods = 0;
        for (TestClassInfo testClass : testClasses) {
            this.totalTestMethods += testClass.getTotalTestMethods();
            this.totalAnnotatedTestMethods += testClass.getAnnotatedTestMethods();
        }
    }

    public int getTotalTestClasses() {
        return totalTestClasses;
    }

    public int getTotalTestMethods() {
        return totalTestMethods;
    }

    public int getTotalAnnotatedTestMethods() {
        return totalAnnotatedTestMethods;
    }

    @Override
    public String toString() {
        return "RepositoryTestInfo{" +
                "repositoryName='" + repositoryName + '\'' +
                ", repositoryPath='" + repositoryPath + '\'' +
                ", totalTestClasses=" + totalTestClasses +
                ", totalTestMethods=" + totalTestMethods +
                ", totalAnnotatedTestMethods=" + totalAnnotatedTestMethods +
                ", testClasses=" + testClasses +
                '}';
    }
}
