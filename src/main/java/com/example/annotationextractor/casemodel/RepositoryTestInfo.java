package com.example.annotationextractor.casemodel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Data model to hold information about all test classes found in a single repository
 */
public class RepositoryTestInfo {
    private String repositoryName;
    private Path repositoryPath;
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public void setRepositoryPath(Path repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    private String gitUrl;
    private final String teamName;
    private final String teamCode;
    public String getTeamName() {
        return teamName;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getTeamCode() {
        return teamCode;
    }

    private List<TestClassInfo> testClasses;
    
    
    private int totalTestClasses;
    private int totalTestMethods;
    private int totalAnnotatedTestMethods;

    public void setTotalTestClasses(int totalTestClasses) {
        this.totalTestClasses = totalTestClasses;
    }

    public void setTotalTestMethods(int totalTestMethods) {
        this.totalTestMethods = totalTestMethods;
    }

    public void setTotalAnnotatedTestMethods(int totalAnnotatedTestMethods) {
        this.totalAnnotatedTestMethods = totalAnnotatedTestMethods;
    }


    public RepositoryTestInfo(String gitUrl, String teamName, String teamCode) {
        this.gitUrl = gitUrl;
        this.teamName = teamName;
        this.teamCode = teamCode;
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
        if (repositoryName == null) {
            return "";
        }
        return repositoryName;
    }


    public String getRepositoryPathString() {
        if (repositoryPath == null) {
            return "";
        }
        return repositoryPath.toString();
    }

    public Path getRepositoryPath() {
        return repositoryPath;
    }


    public String getGitUrl() {
        return gitUrl;
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
                ", gitUrl='" + gitUrl + '\'' +
                ", totalTestClasses=" + totalTestClasses +
                ", totalTestMethods=" + totalTestMethods +
                ", totalAnnotatedTestMethods=" + totalAnnotatedTestMethods +
                ", testClasses=" + testClasses +
                '}';
    }
}
