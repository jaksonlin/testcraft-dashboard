package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.domain.model.DailyMetric;
import com.example.annotationextractor.domain.model.ScanSession;
import com.example.annotationextractor.service.DashboardDataService;
import com.example.annotationextractor.service.RepositoryDataService;
import com.example.annotationextractor.service.TeamDataService;
import com.example.annotationextractor.web.dto.DashboardOverviewDto;
import com.example.annotationextractor.web.dto.TeamMetricsDto;
import com.example.annotationextractor.web.dto.RepositoryMetricsDto;
import com.example.annotationextractor.web.dto.RepositoryDetailDto;
import com.example.annotationextractor.web.dto.TestMethodDetailDto;
import com.example.annotationextractor.web.dto.PagedResponse;
import com.example.annotationextractor.web.dto.GroupedTestMethodResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * REST Controller for dashboard data
 * Provides endpoints for the main dashboard views
 */
@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // React dev server and Vite dev server
public class DashboardController {

    private final DashboardDataService dashboardDataService;
    private TeamDataService teamDataService;
    private RepositoryDataService repositoryDataService;

    public DashboardController(DashboardDataService dashboardDataService) {
        this.dashboardDataService = dashboardDataService;
    }

    @Autowired(required = false)
    public void setTeamDataService(TeamDataService teamDataService) {
        this.teamDataService = teamDataService;
    }

    @Autowired(required = false)
    public void setRepositoryDataService(RepositoryDataService repositoryDataService) {
        this.repositoryDataService = repositoryDataService;
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
        if (teamDataService != null) {
            List<TeamMetricsDto> teams = teamDataService.getTeamMetrics();
            return ResponseEntity.ok(teams);
        } else {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get repository metrics for a specific team
     */
    @GetMapping("/teams/{teamId}/repositories")
    public ResponseEntity<List<RepositoryMetricsDto>> getRepositoryMetrics(
            @PathVariable Long teamId) {
        if (repositoryDataService != null) {
            List<RepositoryMetricsDto> repositories = repositoryDataService.getRepositoryMetricsByTeamId(teamId);
            return ResponseEntity.ok(repositories);
        } else {
            return ResponseEntity.ok(List.of());
        }
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
        if (repositoryDataService != null) {
            List<RepositoryDetailDto> repositories = repositoryDataService.getRepositoryDetails();
            return ResponseEntity.ok(repositories);
        } else {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get all test method details with pagination for better performance
     */
    @GetMapping("/test-methods/paginated")
    public ResponseEntity<PagedResponse<TestMethodDetailDto>> getTestMethodDetailsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) String repositoryName,
            @RequestParam(required = false) Boolean annotated) {
        if (repositoryDataService != null) {
            PagedResponse<TestMethodDetailDto> result = repositoryDataService.getTestMethodDetailsPaginated(
                page, size, teamName, repositoryName, annotated);
            return ResponseEntity.ok(result);
        } else {
            PagedResponse<TestMethodDetailDto> emptyResponse = new PagedResponse<>(List.of(), page, size, 0);
            return ResponseEntity.ok(emptyResponse);
        }
    }

    /**
     * Get all test method details grouped by team and class for hierarchical display
     * This endpoint provides pre-grouped data to avoid performance issues on the frontend
     */
    @GetMapping("/test-methods/grouped")
    public ResponseEntity<GroupedTestMethodResponse> getAllTestMethodDetailsGrouped(
            @RequestParam(defaultValue = "100") Integer limit) {
        if (repositoryDataService != null) {
            GroupedTestMethodResponse groupedData = repositoryDataService.getAllTestMethodDetailsGrouped(limit);
            return ResponseEntity.ok(groupedData);
        } else {
            GroupedTestMethodResponse emptyResponse = new GroupedTestMethodResponse(List.of(), null);
            return ResponseEntity.ok(emptyResponse);
        }
    }

    /**
     * Get health status of the dashboard
     */
    @GetMapping("/health")
    public ResponseEntity<Object> getHealth() {
        return ResponseEntity.ok("Dashboard is healthy");
    }
}
