package com.example.annotationextractor.service;

import com.example.annotationextractor.application.DailyMetricQueryService;
import com.example.annotationextractor.domain.model.DailyMetric;
import com.example.annotationextractor.web.dto.DailyMetricDto;
import com.example.annotationextractor.web.dto.TeamMetricsDto;
import com.example.annotationextractor.web.dto.AnalyticsOverviewDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for analytics data operations
 */
@Service
public class AnalyticsDataService {

    private final DailyMetricQueryService dailyMetricQueryService;
    private final TeamDataService teamDataService;

    public AnalyticsDataService(DailyMetricQueryService dailyMetricQueryService, TeamDataService teamDataService) {
        this.dailyMetricQueryService = dailyMetricQueryService;
        this.teamDataService = teamDataService;
    }

    /**
     * Get daily metrics for the specified number of days
     */
    public List<DailyMetricDto> getDailyMetrics(int days) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);
            
            List<DailyMetric> metrics = dailyMetricQueryService.range(startDate, endDate);
            
            return metrics.stream()
                .map(this::convertToDailyMetricDto)
                .toList();
        } catch (Exception e) {
            System.err.println("Error fetching daily metrics: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get coverage trend data for the specified period
     */
    public List<CoverageTrendPoint> getCoverageTrend(int days) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);
            
            List<DailyMetric> metrics = dailyMetricQueryService.range(startDate, endDate);
            
            return metrics.stream()
                .map(metric -> new CoverageTrendPoint(
                    metric.getMetricDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    metric.getOverallCoverageRate()
                ))
                .toList();
        } catch (Exception e) {
            System.err.println("Error fetching coverage trend: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get team comparison data
     */
    public List<TeamMetricsDto> getTeamComparison() {
        try {
            List<TeamMetricsDto> teams = teamDataService.getTeamMetrics();
            // Sort by average coverage rate for comparison
            teams.sort((t1, t2) -> Double.compare(t2.getAverageCoverageRate(), t1.getAverageCoverageRate()));
            return teams;
        } catch (Exception e) {
            System.err.println("Error fetching team comparison: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get growth metrics for the specified period
     */
    public List<GrowthMetricPoint> getGrowthMetrics(int days) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);
            
            List<DailyMetric> metrics = dailyMetricQueryService.range(startDate, endDate);
            
            return metrics.stream()
                .map(metric -> new GrowthMetricPoint(
                    metric.getMetricDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    metric.getTotalRepositories(),
                    metric.getTotalTestMethods(),
                    metric.getTotalAnnotatedMethods()
                ))
                .toList();
        } catch (Exception e) {
            System.err.println("Error fetching growth metrics: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get analytics overview
     */
    public AnalyticsOverviewDto getAnalyticsOverview() {
        try {
            List<DailyMetric> recentMetrics = dailyMetricQueryService.recent(30);
            
            int totalDaysTracked = recentMetrics.size();
            double averageCoverageRate = recentMetrics.stream()
                .mapToDouble(DailyMetric::getOverallCoverageRate)
                .average()
                .orElse(0.0);
            
            String coverageTrend = calculateCoverageTrend(recentMetrics);
            
            AnalyticsOverviewDto.GrowthSummary totalGrowth = calculateTotalGrowth(recentMetrics);
            AnalyticsOverviewDto.RecentActivity recentActivity = calculateRecentActivity(recentMetrics);
            
            return new AnalyticsOverviewDto(
                totalDaysTracked,
                averageCoverageRate,
                coverageTrend,
                totalGrowth,
                recentActivity
            );
        } catch (Exception e) {
            System.err.println("Error fetching analytics overview: " + e.getMessage());
            return new AnalyticsOverviewDto(0, 0.0, "stable", 
                new AnalyticsOverviewDto.GrowthSummary(0, 0, 0),
                new AnalyticsOverviewDto.RecentActivity(0, 0));
        }
    }

    /**
     * Convert DailyMetric domain model to DTO
     */
    private DailyMetricDto convertToDailyMetricDto(DailyMetric metric) {
        return new DailyMetricDto(
            metric.getId(),
            metric.getMetricDate(),
            metric.getTotalRepositories(),
            metric.getTotalTestClasses(),
            metric.getTotalTestMethods(),
            metric.getTotalAnnotatedMethods(),
            metric.getOverallCoverageRate(),
            metric.getNewTestMethods(),
            metric.getNewAnnotatedMethods()
        );
    }

    /**
     * Calculate coverage trend from recent metrics
     */
    private String calculateCoverageTrend(List<DailyMetric> metrics) {
        if (metrics.size() < 2) return "stable";
        
        double firstCoverage = metrics.get(0).getOverallCoverageRate();
        double lastCoverage = metrics.get(metrics.size() - 1).getOverallCoverageRate();
        double diff = lastCoverage - firstCoverage;
        
        if (diff > 1.0) return "up";
        if (diff < -1.0) return "down";
        return "stable";
    }

    /**
     * Calculate total growth from metrics
     */
    private AnalyticsOverviewDto.GrowthSummary calculateTotalGrowth(List<DailyMetric> metrics) {
        if (metrics.isEmpty()) {
            return new AnalyticsOverviewDto.GrowthSummary(0, 0, 0);
        }
        
        DailyMetric latest = metrics.get(metrics.size() - 1);
        return new AnalyticsOverviewDto.GrowthSummary(
            latest.getTotalRepositories(),
            latest.getTotalTestMethods(),
            latest.getTotalAnnotatedMethods()
        );
    }

    /**
     * Calculate recent activity from metrics
     */
    private AnalyticsOverviewDto.RecentActivity calculateRecentActivity(List<DailyMetric> metrics) {
        int lastWeek = metrics.stream()
            .filter(metric -> metric.getMetricDate().isAfter(LocalDate.now().minusDays(7)))
            .mapToInt(DailyMetric::getNewTestMethods)
            .sum();
            
        int lastMonth = metrics.stream()
            .filter(metric -> metric.getMetricDate().isAfter(LocalDate.now().minusDays(30)))
            .mapToInt(DailyMetric::getNewTestMethods)
            .sum();
            
        return new AnalyticsOverviewDto.RecentActivity(lastWeek, lastMonth);
    }

    /**
     * Data transfer object for coverage trend points
     */
    public static class CoverageTrendPoint {
        private String date;
        private double coverage;

        public CoverageTrendPoint() {}

        public CoverageTrendPoint(String date, double coverage) {
            this.date = date;
            this.coverage = coverage;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public double getCoverage() { return coverage; }
        public void setCoverage(double coverage) { this.coverage = coverage; }
    }

    /**
     * Data transfer object for growth metric points
     */
    public static class GrowthMetricPoint {
        private String date;
        private int repositories;
        private int testMethods;
        private int annotatedMethods;

        public GrowthMetricPoint() {}

        public GrowthMetricPoint(String date, int repositories, int testMethods, int annotatedMethods) {
            this.date = date;
            this.repositories = repositories;
            this.testMethods = testMethods;
            this.annotatedMethods = annotatedMethods;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public int getRepositories() { return repositories; }
        public void setRepositories(int repositories) { this.repositories = repositories; }
        public int getTestMethods() { return testMethods; }
        public void setTestMethods(int testMethods) { this.testMethods = testMethods; }
        public int getAnnotatedMethods() { return annotatedMethods; }
        public void setAnnotatedMethods(int annotatedMethods) { this.annotatedMethods = annotatedMethods; }
    }
}
