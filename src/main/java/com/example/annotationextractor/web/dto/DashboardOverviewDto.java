package com.example.annotationextractor.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for dashboard overview data
 */
public class DashboardOverviewDto {
    private int totalRepositories;
    private int totalTeams;
    private int totalTestClasses;
    private int totalTestMethods;
    private int totalAnnotatedMethods;
    private double overallCoverageRate;
    private LocalDateTime lastScanDate;
    private List<TeamSummaryDto> topTeams;
    private List<RepositorySummaryDto> topRepositories;

    // Constructors
    public DashboardOverviewDto() {}

    // Getters and Setters
    public int getTotalRepositories() {
        return totalRepositories;
    }

    public void setTotalRepositories(int totalRepositories) {
        this.totalRepositories = totalRepositories;
    }

    public int getTotalTeams() {
        return totalTeams;
    }

    public void setTotalTeams(int totalTeams) {
        this.totalTeams = totalTeams;
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

    public double getOverallCoverageRate() {
        return overallCoverageRate;
    }

    public void setOverallCoverageRate(double overallCoverageRate) {
        this.overallCoverageRate = overallCoverageRate;
    }

    public LocalDateTime getLastScanDate() {
        return lastScanDate;
    }

    public void setLastScanDate(LocalDateTime lastScanDate) {
        this.lastScanDate = lastScanDate;
    }

    public List<TeamSummaryDto> getTopTeams() {
        return topTeams;
    }

    public void setTopTeams(List<TeamSummaryDto> topTeams) {
        this.topTeams = topTeams;
    }

    public List<RepositorySummaryDto> getTopRepositories() {
        return topRepositories;
    }

    public void setTopRepositories(List<RepositorySummaryDto> topRepositories) {
        this.topRepositories = topRepositories;
    }
}
