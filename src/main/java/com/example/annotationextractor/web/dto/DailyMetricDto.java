package com.example.annotationextractor.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * Data Transfer Object for daily metrics
 */
public class DailyMetricDto {
    private Long id;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    private int totalRepositories;
    private int totalTestClasses;
    private int totalTestMethods;
    private int totalAnnotatedMethods;
    private double overallCoverageRate;
    private int newTestMethods;
    private int newAnnotatedMethods;

    public DailyMetricDto() {}

    public DailyMetricDto(Long id, LocalDate date, int totalRepositories, int totalTestClasses,
                         int totalTestMethods, int totalAnnotatedMethods, double overallCoverageRate,
                         int newTestMethods, int newAnnotatedMethods) {
        this.id = id;
        this.date = date;
        this.totalRepositories = totalRepositories;
        this.totalTestClasses = totalTestClasses;
        this.totalTestMethods = totalTestMethods;
        this.totalAnnotatedMethods = totalAnnotatedMethods;
        this.overallCoverageRate = overallCoverageRate;
        this.newTestMethods = newTestMethods;
        this.newAnnotatedMethods = newAnnotatedMethods;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getTotalRepositories() { return totalRepositories; }
    public void setTotalRepositories(int totalRepositories) { this.totalRepositories = totalRepositories; }

    public int getTotalTestClasses() { return totalTestClasses; }
    public void setTotalTestClasses(int totalTestClasses) { this.totalTestClasses = totalTestClasses; }

    public int getTotalTestMethods() { return totalTestMethods; }
    public void setTotalTestMethods(int totalTestMethods) { this.totalTestMethods = totalTestMethods; }

    public int getTotalAnnotatedMethods() { return totalAnnotatedMethods; }
    public void setTotalAnnotatedMethods(int totalAnnotatedMethods) { this.totalAnnotatedMethods = totalAnnotatedMethods; }

    public double getOverallCoverageRate() { return overallCoverageRate; }
    public void setOverallCoverageRate(double overallCoverageRate) { this.overallCoverageRate = overallCoverageRate; }

    public int getNewTestMethods() { return newTestMethods; }
    public void setNewTestMethods(int newTestMethods) { this.newTestMethods = newTestMethods; }

    public int getNewAnnotatedMethods() { return newAnnotatedMethods; }
    public void setNewAnnotatedMethods(int newAnnotatedMethods) { this.newAnnotatedMethods = newAnnotatedMethods; }
}
