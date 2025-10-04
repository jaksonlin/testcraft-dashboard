package com.example.annotationextractor.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for detailed test method information matching Excel Test Method Details sheet
 */
public class TestMethodDetailDto {
    
    private Long id;
    private String repository;
    private String testClass;
    private String testMethod;
    private Integer line;
    private String title;
    private String author;
    private String status;
    private String targetClass;
    private String targetMethod;
    private String description;
    private String testPoints;
    private List<String> tags;
    private List<String> requirements;
    private List<String> testCaseIds;
    private List<String> defects;
    private LocalDateTime lastModified;
    private String lastUpdateAuthor;
    private String teamName;
    private String teamCode;
    private String gitUrl;
    
    // Constructors
    public TestMethodDetailDto() {}
    
    public TestMethodDetailDto(Long id, String repository, String testClass, String testMethod) {
        this.id = id;
        this.repository = repository;
        this.testClass = testClass;
        this.testMethod = testMethod;
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
    
    public String getTestClass() {
        return testClass;
    }
    
    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }
    
    public String getTestMethod() {
        return testMethod;
    }
    
    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }
    
    public Integer getLine() {
        return line;
    }
    
    public void setLine(Integer line) {
        this.line = line;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTargetClass() {
        return targetClass;
    }
    
    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }
    
    public String getTargetMethod() {
        return targetMethod;
    }
    
    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getTestPoints() {
        return testPoints;
    }
    
    public void setTestPoints(String testPoints) {
        this.testPoints = testPoints;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public List<String> getRequirements() {
        return requirements;
    }
    
    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }
    
    public List<String> getTestCaseIds() {
        return testCaseIds;
    }
    
    public void setTestCaseIds(List<String> testCaseIds) {
        this.testCaseIds = testCaseIds;
    }
    
    public List<String> getDefects() {
        return defects;
    }
    
    public void setDefects(List<String> defects) {
        this.defects = defects;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
    
    public String getLastUpdateAuthor() {
        return lastUpdateAuthor;
    }
    
    public void setLastUpdateAuthor(String lastUpdateAuthor) {
        this.lastUpdateAuthor = lastUpdateAuthor;
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
    
    public String getGitUrl() {
        return gitUrl;
    }
    
    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }
}
