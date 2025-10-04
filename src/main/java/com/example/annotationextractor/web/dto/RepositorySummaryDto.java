package com.example.annotationextractor.web.dto;

import java.time.LocalDateTime;

/**
 * DTO for repository summary data
 */
public class RepositorySummaryDto {
    private Long id;
    private String repositoryName;
    private String gitUrl;
    private String teamName;
    private int testClassCount;
    private int testMethodCount;
    private int annotatedMethodCount;
    private double coverageRate;
    private LocalDateTime lastScanDate;

    // Constructors
    public RepositorySummaryDto() {}

    public RepositorySummaryDto(Long id, String repositoryName, String gitUrl) {
        this.id = id;
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

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
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

    public LocalDateTime getLastScanDate() {
        return lastScanDate;
    }

    public void setLastScanDate(LocalDateTime lastScanDate) {
        this.lastScanDate = lastScanDate;
    }
}
