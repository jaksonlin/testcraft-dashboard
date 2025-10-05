package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.service.RepositoryDataService;
import com.example.annotationextractor.service.TeamDataService;
import com.example.annotationextractor.web.dto.TeamMetricsDto;
import com.example.annotationextractor.web.dto.RepositoryMetricsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for team management
 * Provides endpoints for team-specific operations
 */
@RestController
@RequestMapping("/teams")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // React dev server and Vite dev server
public class TeamController {

    private final TeamDataService teamDataService;
    private final RepositoryDataService repositoryDataService;
    public TeamController(TeamDataService teamDataService, RepositoryDataService repositoryDataService) {
        this.teamDataService = teamDataService;
        this.repositoryDataService = repositoryDataService;
    }

    /**
     * Get all teams with their metrics
     */
    @GetMapping
    public ResponseEntity<List<TeamMetricsDto>> getAllTeams() {
        List<TeamMetricsDto> teams = teamDataService.getTeamMetrics();
        return ResponseEntity.ok(teams);
    }

    /**
     * Get team details by ID
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamMetricsDto> getTeamById(@PathVariable Long teamId) {
        List<TeamMetricsDto> allTeams = teamDataService.getTeamMetrics();
        TeamMetricsDto team = allTeams.stream()
            .filter(t -> t.getId().equals(teamId))
            .findFirst()
            .orElse(null);
        
        if (team != null) {
            return ResponseEntity.ok(team);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get repositories for a specific team
     */
    @GetMapping("/{teamId}/repositories")
    public ResponseEntity<List<RepositoryMetricsDto>> getTeamRepositories(@PathVariable Long teamId) {
        List<RepositoryMetricsDto> repositories = repositoryDataService.getRepositoryMetricsByTeamId(teamId);
        return ResponseEntity.ok(repositories);
    }

    /**
     * Get team performance comparison
     */
    @GetMapping("/comparison")
    public ResponseEntity<List<TeamMetricsDto>> getTeamComparison() {
        List<TeamMetricsDto> teams = teamDataService.getTeamMetrics();
        // Sort by average coverage rate for comparison
        teams.sort((t1, t2) -> Double.compare(t2.getAverageCoverageRate(), t1.getAverageCoverageRate()));
        return ResponseEntity.ok(teams);
    }
}
