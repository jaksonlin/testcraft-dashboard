package com.example.annotationextractor.web.dto;

import java.time.LocalDateTime;

/**
 * DTO for repository details matching Excel Repository Details sheet
 */
public class RepositoryDetailDto {
    
    private Long id;
    private String repository;
    private String path;
    private String gitUrl;
    private Integer testClasses;
    private Integer testMethods;
    private Integer annotatedMethods;
    private Double coverageRate;
    private LocalDateTime lastScan;
    private String teamName;
    private String teamCode;
    
    // Constructors
    public RepositoryDetailDto() {}
    
    public RepositoryDetailDto(Long id, String repository, String gitUrl) {
        this.id = id;
        this.repository = repository;
        this.gitUrl = gitUrl;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRepository() {
        return repository;
    }
    
    public void setRepository(String repository) {
        this.repository = repository;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getGitUrl() {
        return gitUrl;
    }
    
    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }
    
    public Integer getTestClasses() {
        return testClasses;
    }
    
    public void setTestClasses(Integer testClasses) {
        this.testClasses = testClasses;
    }
    
    public Integer getTestMethodCount() {
        return testMethods;
    }
    
    public void setTestMethodCount(Integer testMethods) {
        this.testMethods = testMethods;
    }
    
    public Integer getAnnotatedMethods() {
        return annotatedMethods;
    }
    
    public void setAnnotatedMethods(Integer annotatedMethods) {
        this.annotatedMethods = annotatedMethods;
    }
    
    public Double getCoverageRate() {
        return coverageRate;
    }
    
    public void setCoverageRate(Double coverageRate) {
        this.coverageRate = coverageRate;
    }
    
    public LocalDateTime getLastScan() {
        return lastScan;
    }
    
    public void setLastScan(LocalDateTime lastScan) {
        this.lastScan = lastScan;
    }
    
    public String getTeamName() {
        return teamName;
    }
    
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    public String getTeamCode() {
        return teamCode;
    }
    
    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }
}
