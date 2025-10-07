package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.service.AnalyticsDataService;
import com.example.annotationextractor.web.dto.DailyMetricDto;
import com.example.annotationextractor.web.dto.TeamMetricsDto;
import com.example.annotationextractor.web.dto.AnalyticsOverviewDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for analytics data
 * Provides endpoints for advanced analytics and reporting
 */
@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // React dev server and Vite dev server
public class AnalyticsController {

    private final AnalyticsDataService analyticsDataService;

    public AnalyticsController(AnalyticsDataService analyticsDataService) {
        this.analyticsDataService = analyticsDataService;
    }

    /**
     * Get daily metrics for the specified number of days
     */
    @GetMapping("/daily-metrics")
    public ResponseEntity<List<DailyMetricDto>> getDailyMetrics(
            @RequestParam(defaultValue = "30") int days) {
        List<DailyMetricDto> metrics = analyticsDataService.getDailyMetrics(days);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get coverage trend data for the specified period
     */
    @GetMapping("/coverage-trend")
    public ResponseEntity<List<AnalyticsDataService.CoverageTrendPoint>> getCoverageTrend(
            @RequestParam(defaultValue = "30") int days) {
        List<AnalyticsDataService.CoverageTrendPoint> trend = analyticsDataService.getCoverageTrend(days);
        return ResponseEntity.ok(trend);
    }

    /**
     * Get team comparison data
     */
    @GetMapping("/team-comparison")
    public ResponseEntity<List<TeamMetricsDto>> getTeamComparison() {
        List<TeamMetricsDto> teams = analyticsDataService.getTeamComparison();
        return ResponseEntity.ok(teams);
    }

    /**
     * Get growth metrics for the specified period
     */
    @GetMapping("/growth-metrics")
    public ResponseEntity<List<AnalyticsDataService.GrowthMetricPoint>> getGrowthMetrics(
            @RequestParam(defaultValue = "30") int days) {
        List<AnalyticsDataService.GrowthMetricPoint> metrics = analyticsDataService.getGrowthMetrics(days);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get analytics overview
     */
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsOverviewDto> getOverview() {
        AnalyticsOverviewDto overview = analyticsDataService.getAnalyticsOverview();
        return ResponseEntity.ok(overview);
    }

}
