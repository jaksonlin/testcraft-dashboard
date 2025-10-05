package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.domain.model.DailyMetric;
import com.example.annotationextractor.domain.model.ScanSession;
import com.example.annotationextractor.service.DashboardDataService;
import com.example.annotationextractor.web.dto.DashboardOverviewDto;
import com.example.annotationextractor.web.dto.TeamMetricsDto;
import com.example.annotationextractor.web.dto.RepositoryMetricsDto;
import com.example.annotationextractor.web.dto.RepositoryDetailDto;
import com.example.annotationextractor.web.dto.TestMethodDetailDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for dashboard data
 * Provides endpoints for the main dashboard views
 */
@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "http://localhost:3000") // React dev server
public class DashboardController {

    private final DashboardDataService dashboardDataService;

    public DashboardController(DashboardDataService dashboardDataService) {
        this.dashboardDataService = dashboardDataService;
    }

    /**
     * Get overview metrics for the main dashboard
     */
    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewDto> getOverview() {
        DashboardOverviewDto overview = dashboardDataService.getDashboardOverview();
        return ResponseEntity.ok(overview);
    }

    /**
     * Get team-based metrics
     */
    @GetMapping("/teams")
    public ResponseEntity<List<TeamMetricsDto>> getTeamMetrics() {
        List<TeamMetricsDto> teams = dashboardDataService.getTeamMetrics();
        return ResponseEntity.ok(teams);
    }

    /**
     * Get repository metrics for a specific team
     */
    @GetMapping("/teams/{teamId}/repositories")
    public ResponseEntity<List<RepositoryMetricsDto>> getRepositoryMetrics(
            @PathVariable Long teamId) {
        List<RepositoryMetricsDto> repositories = dashboardDataService.getRepositoryMetrics(teamId);
        return ResponseEntity.ok(repositories);
    }

    /**
     * Get coverage trends for the specified period
     */
    @GetMapping("/trends/coverage")
    public ResponseEntity<List<DailyMetric>> getCoverageTrends(
            @RequestParam(defaultValue = "30") int days) {
        List<DailyMetric> trends = dashboardDataService.getCoverageTrends(days);
        return ResponseEntity.ok(trends);
    }

    /**
     * Get recent scan sessions
     */
    @GetMapping("/scan-sessions/recent")
    public ResponseEntity<List<ScanSession>> getRecentScanSessions(
            @RequestParam(defaultValue = "10") int limit) {
        List<ScanSession> sessions = dashboardDataService.getRecentScanSessions(limit);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get repository details matching Excel Repository Details sheet format
     */
    @GetMapping("/repositories/details")
    public ResponseEntity<List<RepositoryDetailDto>> getRepositoryDetails() {
        List<RepositoryDetailDto> repositories = dashboardDataService.getRepositoryDetails();
        return ResponseEntity.ok(repositories);
    }

    /**
     * Get test method details matching Excel Test Method Details sheet format
     */
    @GetMapping("/test-methods/details")
    public ResponseEntity<List<TestMethodDetailDto>> getTestMethodDetails(
            @RequestParam(required = false) Long teamId,
            @RequestParam(defaultValue = "100") Integer limit) {
        List<TestMethodDetailDto> testMethods = dashboardDataService.getTestMethodDetails(teamId, limit);
        return ResponseEntity.ok(testMethods);
    }

    /**
     * Get health status of the dashboard
     */
    @GetMapping("/health")
    public ResponseEntity<Object> getHealth() {
        return ResponseEntity.ok("Dashboard is healthy");
    }
}
