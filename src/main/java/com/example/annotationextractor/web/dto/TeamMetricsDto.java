package com.example.annotationextractor.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for team metrics data
 */
public class TeamMetricsDto {
    private Long id;
    private String teamName;
    private String teamCode;
    private String department;
    private int repositoryCount;
    private int totalTestClasses;
    private int totalTestMethods;
    private int totalAnnotatedMethods;
    private double averageCoverageRate;
    private LocalDateTime lastScanDate;
    private List<RepositorySummaryDto> repositories;

    // Constructors
    public TeamMetricsDto() {}

    public TeamMetricsDto(Long id, String teamName, String teamCode) {
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getRepositoryCount() {
        return repositoryCount;
    }

    public void setRepositoryCount(int repositoryCount) {
        this.repositoryCount = repositoryCount;
    }

    public int getTotalTestClasses() {
        return totalTestClasses;
    }

    public void setTotalTestClasses(int totalTestClasses) {
        this.totalTestClasses = totalTestClasses;
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

    public double getAverageCoverageRate() {
        return averageCoverageRate;
    }

    public void setAverageCoverageRate(double averageCoverageRate) {
        this.averageCoverageRate = averageCoverageRate;
    }

    public LocalDateTime getLastScanDate() {
        return lastScanDate;
    }

    public void setLastScanDate(LocalDateTime lastScanDate) {
        this.lastScanDate = lastScanDate;
    }

    public List<RepositorySummaryDto> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<RepositorySummaryDto> repositories) {
        this.repositories = repositories;
    }
}
