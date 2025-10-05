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
    
    // Default constructor
    public ScanConfigDto() {}
    
    // Constructor with all fields
    public ScanConfigDto(Boolean tempCloneMode, String repositoryHubPath, 
                        String repositoryListFile, Integer maxRepositoriesPerScan,
                        Boolean schedulerEnabled, String dailyScanCron) {
        this.tempCloneMode = tempCloneMode;
        this.repositoryHubPath = repositoryHubPath;
        this.repositoryListFile = repositoryListFile;
        this.maxRepositoriesPerScan = maxRepositoriesPerScan;
        this.schedulerEnabled = schedulerEnabled;
        this.dailyScanCron = dailyScanCron;
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
    
    @Override
    public String toString() {
        return "ScanConfigDto{" +
                "tempCloneMode=" + tempCloneMode +
                ", repositoryHubPath='" + repositoryHubPath + '\'' +
                ", repositoryListFile='" + repositoryListFile + '\'' +
                ", maxRepositoriesPerScan=" + maxRepositoriesPerScan +
                ", schedulerEnabled=" + schedulerEnabled +
                ", dailyScanCron='" + dailyScanCron + '\'' +
                '}';
    }
}
