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
    private List<TestHelperClassInfo> helperClasses;
    
    
    private int totalTestClasses;
    private int totalTestMethods;
    private int totalAnnotatedTestMethods;
    private int testCodeLines;
    private int testRelatedCodeLines;

    public void setTotalTestClasses(int totalTestClasses) {
        this.totalTestClasses = totalTestClasses;
    }

    public void setTotalTestMethods(int totalTestMethods) {
        this.totalTestMethods = totalTestMethods;
    }

    public void setTotalAnnotatedTestMethods(int totalAnnotatedTestMethods) {
        this.totalAnnotatedTestMethods = totalAnnotatedTestMethods;
    }

    public void setTestCodeLines(int testCodeLines) {
        this.testCodeLines = testCodeLines;
    }

    public void setTestRelatedCodeLines(int testRelatedCodeLines) {
        this.testRelatedCodeLines = testRelatedCodeLines;
    }

    public RepositoryTestInfo(String gitUrl, String teamName, String teamCode) {
        this.gitUrl = gitUrl;
        this.teamName = teamName;
        this.teamCode = teamCode;
        this.testClasses = new ArrayList<>();
        this.helperClasses = new ArrayList<>();
        this.totalTestClasses = 0;
        this.totalTestMethods = 0;
        this.totalAnnotatedTestMethods = 0;
        this.testCodeLines = 0;
        this.testRelatedCodeLines = 0;
    }

    public void addTestClass(TestClassInfo testClass) {
        this.testClasses.add(testClass);
        this.totalTestClasses++;
        this.totalTestMethods += testClass.getTotalTestMethods();
        this.totalAnnotatedTestMethods += testClass.getAnnotatedTestMethods();
        // Add test class content lines to test code lines
        if (testClass.getTestClassContent() != null) {
            String content = testClass.getTestClassContent();
            // Count lines by splitting on any line separator (handles Windows \r\n, Unix \n, Mac \r)
            long lineCount = content.lines().count();
            this.testCodeLines += (int) lineCount;
        }
    }

    public void addHelperClass(TestHelperClassInfo helperClass) {
        this.helperClasses.add(helperClass);
        // Add helper class LOC to test related code lines
        this.testRelatedCodeLines += helperClass.getLoc();
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

    public List<TestHelperClassInfo> getHelperClasses() {
        return helperClasses;
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

    public int getTestCodeLines() {
        return testCodeLines;
    }

    public int getTestRelatedCodeLines() {
        return testRelatedCodeLines;
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
