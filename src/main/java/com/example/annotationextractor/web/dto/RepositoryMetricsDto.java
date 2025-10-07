package com.example.annotationextractor.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for repository metrics data
 */
public class RepositoryMetricsDto {
    private Long id;
    private Long repositoryId; // Frontend expects this field name
    private String repositoryName;
    private String repositoryPath;
    private String gitUrl;
    private String gitBranch;
    private String teamName;
    private String technologyStack;
    private int testClassCount;
    private int testMethodCount;
    private int annotatedMethodCount;
    private double coverageRate;
    private LocalDateTime firstScanDate;
    private LocalDateTime lastScanDate;
    private List<TestClassSummaryDto> testClasses;

    // Constructors
    public RepositoryMetricsDto() {}

    public RepositoryMetricsDto(Long id, String repositoryName, String gitUrl) {
        this.id = id;
        this.repositoryId = id; // Set repositoryId to same value as id
        this.repositoryName = repositoryName;
        this.gitUrl = gitUrl;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }

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

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTechnologyStack() {
        return technologyStack;
    }

    public void setTechnologyStack(String technologyStack) {
        this.technologyStack = technologyStack;
    }

    public int getTestClassCount() {
        return testClassCount;
    }

    public void setTestClassCount(int testClassCount) {
        this.testClassCount = testClassCount;
    }

    public int getTestMethodCount() {
        return testMethodCount;
    }

    public void setTestMethodCount(int testMethodCount) {
        this.testMethodCount = testMethodCount;
    }

    public int getAnnotatedMethodCount() {
        return annotatedMethodCount;
    }

    public void setAnnotatedMethodCount(int annotatedMethodCount) {
        this.annotatedMethodCount = annotatedMethodCount;
    }

    public double getCoverageRate() {
        return coverageRate;
    }

    public void setCoverageRate(double coverageRate) {
        this.coverageRate = coverageRate;
    }

    public LocalDateTime getFirstScanDate() {
        return firstScanDate;
    }

    public void setFirstScanDate(LocalDateTime firstScanDate) {
        this.firstScanDate = firstScanDate;
    }

    public LocalDateTime getLastScanDate() {
        return lastScanDate;
    }

    public void setLastScanDate(LocalDateTime lastScanDate) {
        this.lastScanDate = lastScanDate;
    }

    public List<TestClassSummaryDto> getTestClasses() {
        return testClasses;
    }

    public void setTestClasses(List<TestClassSummaryDto> testClasses) {
        this.testClasses = testClasses;
    }
}
