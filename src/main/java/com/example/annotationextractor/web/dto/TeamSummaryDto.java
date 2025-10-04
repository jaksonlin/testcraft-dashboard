package com.example.annotationextractor.web.dto;

/**
 * DTO for team summary data
 */
public class TeamSummaryDto {
    private Long id;
    private String teamName;
    private String teamCode;
    private int repositoryCount;
    private double averageCoverageRate;
    private int totalTestMethods;
    private int totalAnnotatedMethods;

    // Constructors
    public TeamSummaryDto() {}

    public TeamSummaryDto(Long id, String teamName, String teamCode) {
        this.id = id;
        this.teamName = teamName;
        this.teamCode = teamCode;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getRepositoryCount() {
        return repositoryCount;
    }

    public void setRepositoryCount(int repositoryCount) {
        this.repositoryCount = repositoryCount;
    }

    public double getAverageCoverageRate() {
        return averageCoverageRate;
    }

    public void setAverageCoverageRate(double averageCoverageRate) {
        this.averageCoverageRate = averageCoverageRate;
    }

    public int getTotalTestMethods() {
        return totalTestMethods;
    }

    public void setTotalTestMethods(int totalTestMethods) {
        this.totalTestMethods = totalTestMethods;
    }

    public int getTotalAnnotatedMethods() {
        return totalAnnotatedMethods;
    }

    public void setTotalAnnotatedMethods(int totalAnnotatedMethods) {
        this.totalAnnotatedMethods = totalAnnotatedMethods;
    }
}
