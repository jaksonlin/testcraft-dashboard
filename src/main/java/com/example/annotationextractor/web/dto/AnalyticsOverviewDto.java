package com.example.annotationextractor.web.dto;

/**
 * Data Transfer Object for analytics overview
 */
public class AnalyticsOverviewDto {
    private int totalDaysTracked;
    private double averageCoverageRate;
    private String coverageTrend; // "up", "down", "stable"
    private GrowthSummary totalGrowth;
    private RecentActivity recentActivity;

    public AnalyticsOverviewDto() {}

    public AnalyticsOverviewDto(int totalDaysTracked, double averageCoverageRate, String coverageTrend,
                              GrowthSummary totalGrowth, RecentActivity recentActivity) {
        this.totalDaysTracked = totalDaysTracked;
        this.averageCoverageRate = averageCoverageRate;
        this.coverageTrend = coverageTrend;
        this.totalGrowth = totalGrowth;
        this.recentActivity = recentActivity;
    }

    // Getters and setters
    public int getTotalDaysTracked() { return totalDaysTracked; }
    public void setTotalDaysTracked(int totalDaysTracked) { this.totalDaysTracked = totalDaysTracked; }

    public double getAverageCoverageRate() { return averageCoverageRate; }
    public void setAverageCoverageRate(double averageCoverageRate) { this.averageCoverageRate = averageCoverageRate; }

    public String getCoverageTrend() { return coverageTrend; }
    public void setCoverageTrend(String coverageTrend) { this.coverageTrend = coverageTrend; }

    public GrowthSummary getTotalGrowth() { return totalGrowth; }
    public void setTotalGrowth(GrowthSummary totalGrowth) { this.totalGrowth = totalGrowth; }

    public RecentActivity getRecentActivity() { return recentActivity; }
    public void setRecentActivity(RecentActivity recentActivity) { this.recentActivity = recentActivity; }

    /**
     * Growth summary data
     */
    public static class GrowthSummary {
        private int repositories;
        private int testMethods;
        private int annotatedMethods;

        public GrowthSummary() {}

        public GrowthSummary(int repositories, int testMethods, int annotatedMethods) {
            this.repositories = repositories;
            this.testMethods = testMethods;
            this.annotatedMethods = annotatedMethods;
        }

        public int getRepositories() { return repositories; }
        public void setRepositories(int repositories) { this.repositories = repositories; }

        public int getTestMethods() { return testMethods; }
        public void setTestMethods(int testMethods) { this.testMethods = testMethods; }

        public int getAnnotatedMethods() { return annotatedMethods; }
        public void setAnnotatedMethods(int annotatedMethods) { this.annotatedMethods = annotatedMethods; }
    }

    /**
     * Recent activity data
     */
    public static class RecentActivity {
        private int lastWeek;
        private int lastMonth;

        public RecentActivity() {}

        public RecentActivity(int lastWeek, int lastMonth) {
            this.lastWeek = lastWeek;
            this.lastMonth = lastMonth;
        }

        public int getLastWeek() { return lastWeek; }
        public void setLastWeek(int lastWeek) { this.lastWeek = lastWeek; }

        public int getLastMonth() { return lastMonth; }
        public void setLastMonth(int lastMonth) { this.lastMonth = lastMonth; }
    }
}
