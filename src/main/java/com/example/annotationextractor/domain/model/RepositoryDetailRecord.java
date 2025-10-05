package com.example.annotationextractor.domain.model;

import java.time.LocalDateTime;

/**
 * Domain model for repository details matching Excel Repository Details sheet format
 */
public class RepositoryDetailRecord {
    
    private final Long id;
    private final String repositoryName;
    private final String repositoryPath;
    private final String gitUrl;
    private final Integer testClasses;
    private final Integer testMethods;
    private final Integer annotatedMethods;
    private final Double coverageRate;
    private final LocalDateTime lastScan;
    private final String teamName;
    private final String teamCode;
    
    public RepositoryDetailRecord(Long id, String repositoryName, String repositoryPath, String gitUrl,
                                Integer testClasses, Integer testMethods, Integer annotatedMethods, 
                                Double coverageRate, LocalDateTime lastScan, String teamName, String teamCode) {
        this.id = id;
        this.repositoryName = repositoryName;
        this.repositoryPath = repositoryPath;
        this.gitUrl = gitUrl;
        this.testClasses = testClasses;
        this.testMethods = testMethods;
        this.annotatedMethods = annotatedMethods;
        this.coverageRate = coverageRate;
        this.lastScan = lastScan;
        this.teamName = teamName;
        this.teamCode = teamCode;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getRepositoryName() { return repositoryName; }
    public String getRepositoryPath() { return repositoryPath; }
    public String getGitUrl() { return gitUrl; }
    public Integer getTestClasses() { return testClasses; }
    public Integer getTestMethodCount() { return testMethods; }
    public Integer getAnnotatedMethods() { return annotatedMethods; }
    public Double getCoverageRate() { return coverageRate; }
    public LocalDateTime getLastScan() { return lastScan; }
    public String getTeamName() { return teamName; }
    public String getTeamCode() { return teamCode; }
}
