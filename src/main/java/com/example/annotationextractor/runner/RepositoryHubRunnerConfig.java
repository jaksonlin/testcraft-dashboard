package com.example.annotationextractor.runner;

public class RepositoryHubRunnerConfig {

    private final String repositoryUrl;
    private final String teamName;
    private final String teamCode;
    
    public RepositoryHubRunnerConfig(String repositoryUrl, String teamName, String teamCode) {
        this.repositoryUrl = repositoryUrl;
        this.teamName = teamName;
        this.teamCode = teamCode;
    }
    
    public String getRepositoryUrl() {
        return repositoryUrl;
    }
    
    public String getTeamName() {
        return teamName;
    }
    
    public String getTeamCode() {
        return teamCode;
    }
}
