package com.example.annotationextractor.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for scan configuration
 */
public class ScanConfigDto {
    
    @JsonProperty("tempCloneMode")
    private Boolean tempCloneMode;
    
    @JsonProperty("repositoryHubPath")
    private String repositoryHubPath;
    
    @JsonProperty("repositoryListFile")
    private String repositoryListFile;
    
    @JsonProperty("maxRepositoriesPerScan")
    private Integer maxRepositoriesPerScan;
    
    @JsonProperty("schedulerEnabled")
    private Boolean schedulerEnabled;
    
    @JsonProperty("dailyScanCron")
    private String dailyScanCron;
    
    @JsonProperty("repositoryConfigContent")
    private String repositoryConfigContent;

    @JsonProperty("organization")
    private String organization;

    @JsonProperty("scanBranch")
    private String scanBranch;
    
    // Default constructor
    public ScanConfigDto() {}
    
    // Constructor with all fields
    public ScanConfigDto(Boolean tempCloneMode, String repositoryHubPath, 
                        String repositoryListFile, Integer maxRepositoriesPerScan,
                        Boolean schedulerEnabled, String dailyScanCron, String repositoryConfigContent,
                        String organization, String scanBranch) {
        this.tempCloneMode = tempCloneMode;
        this.repositoryHubPath = repositoryHubPath;
        this.repositoryListFile = repositoryListFile;
        this.maxRepositoriesPerScan = maxRepositoriesPerScan;
        this.schedulerEnabled = schedulerEnabled;
        this.dailyScanCron = dailyScanCron;
        this.repositoryConfigContent = repositoryConfigContent;
        this.organization = organization;
        this.scanBranch = scanBranch;
    }
    
    // Getters and Setters
    public Boolean getTempCloneMode() {
        return tempCloneMode;
    }
    
    public void setTempCloneMode(Boolean tempCloneMode) {
        this.tempCloneMode = tempCloneMode;
    }
    
    public String getRepositoryHubPath() {
        return repositoryHubPath;
    }
    
    public void setRepositoryHubPath(String repositoryHubPath) {
        this.repositoryHubPath = repositoryHubPath;
    }
    
    public String getRepositoryListFile() {
        return repositoryListFile;
    }
    
    public void setRepositoryListFile(String repositoryListFile) {
        this.repositoryListFile = repositoryListFile;
    }
    
    public Integer getMaxRepositoriesPerScan() {
        return maxRepositoriesPerScan;
    }
    
    public void setMaxRepositoriesPerScan(Integer maxRepositoriesPerScan) {
        this.maxRepositoriesPerScan = maxRepositoriesPerScan;
    }
    
    public Boolean getSchedulerEnabled() {
        return schedulerEnabled;
    }
    
    public void setSchedulerEnabled(Boolean schedulerEnabled) {
        this.schedulerEnabled = schedulerEnabled;
    }
    
    public String getDailyScanCron() {
        return dailyScanCron;
    }
    
    public void setDailyScanCron(String dailyScanCron) {
        this.dailyScanCron = dailyScanCron;
    }
    
    public String getRepositoryConfigContent() {
        return repositoryConfigContent;
    }
    
    public void setRepositoryConfigContent(String repositoryConfigContent) {
        this.repositoryConfigContent = repositoryConfigContent;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getScanBranch() {
        return scanBranch;
    }

    public void setScanBranch(String scanBranch) {
        this.scanBranch = scanBranch;
    }
    
    @Override
    public String toString() {
        return "ScanConfigDto{" +
                "tempCloneMode=" + tempCloneMode +
                ", repositoryHubPath='" + repositoryHubPath + '\'' +
                ", repositoryListFile='" + repositoryListFile + '\'' +
                ", maxRepositoriesPerScan=" + maxRepositoriesPerScan +
                ", schedulerEnabled=" + schedulerEnabled +
                ", dailyScanCron='" + dailyScanCron + '\'' +
                ", repositoryConfigContent='" + repositoryConfigContent + '\'' +
                ", organization='" + organization + '\'' +
                ", scanBranch='" + scanBranch + '\'' +
                '}';
    }
}
