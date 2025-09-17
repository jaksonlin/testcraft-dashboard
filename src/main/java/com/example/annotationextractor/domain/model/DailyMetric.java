package com.example.annotationextractor.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain model representing daily aggregated metrics.
 */
public final class DailyMetric {

    private final Long id;
    private final LocalDate metricDate;
    private final int totalRepositories;
    private final int totalTestClasses;
    private final int totalTestMethods;
    private final int totalAnnotatedMethods;
    private final double overallCoverageRate;
    private final int newTestMethods;
    private final int newAnnotatedMethods;

    public DailyMetric(Long id,
                       LocalDate metricDate,
                       int totalRepositories,
                       int totalTestClasses,
                       int totalTestMethods,
                       int totalAnnotatedMethods,
                       double overallCoverageRate,
                       int newTestMethods,
                       int newAnnotatedMethods) {
        this.id = id;
        this.metricDate = metricDate;
        this.totalRepositories = totalRepositories;
        this.totalTestClasses = totalTestClasses;
        this.totalTestMethods = totalTestMethods;
        this.totalAnnotatedMethods = totalAnnotatedMethods;
        this.overallCoverageRate = overallCoverageRate;
        this.newTestMethods = newTestMethods;
        this.newAnnotatedMethods = newAnnotatedMethods;
    }

    public Long getId() { return id; }
    public LocalDate getMetricDate() { return metricDate; }
    public int getTotalRepositories() { return totalRepositories; }
    public int getTotalTestClasses() { return totalTestClasses; }
    public int getTotalTestMethods() { return totalTestMethods; }
    public int getTotalAnnotatedMethods() { return totalAnnotatedMethods; }
    public double getOverallCoverageRate() { return overallCoverageRate; }
    public int getNewTestMethods() { return newTestMethods; }
    public int getNewAnnotatedMethods() { return newAnnotatedMethods; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyMetric that = (DailyMetric) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


