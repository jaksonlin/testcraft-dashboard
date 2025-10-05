package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.service.DashboardDataService;
import com.example.annotationextractor.service.RepositoryDataService;
import com.example.annotationextractor.web.dto.RepositoryMetricsDto;
import com.example.annotationextractor.web.dto.RepositoryDetailDto;
import com.example.annotationextractor.web.dto.TestMethodDetailDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for repository management
 * Provides endpoints for repository-specific operations
 */
@RestController
@RequestMapping("/repositories")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // React dev server and Vite dev server
public class RepositoryController {

    private final RepositoryDataService repositoryDataService;

    public RepositoryController(RepositoryDataService repositoryDataService) {
        this.repositoryDataService = repositoryDataService;
    }

    /**
     * Get all repositories with their metrics
     */
    @GetMapping
    public ResponseEntity<List<RepositoryMetricsDto>> getAllRepositories() {
        List<RepositoryMetricsDto> repositories = repositoryDataService.getAllRepositoryMetrics();
        return ResponseEntity.ok(repositories);
    }




    /**
     * Get repository details by ID
     */
    @GetMapping("/{repositoryId}")
    public ResponseEntity<RepositoryDetailDto> getRepositoryById(@PathVariable Long repositoryId) {
        List<RepositoryDetailDto> allRepositories = repositoryDataService.getRepositoryDetails();
        RepositoryDetailDto repository = allRepositories.stream()
            .filter(repo -> repo.getId().equals(repositoryId))
            .findFirst()
            .orElse(null);
        
        if (repository != null) {
            return ResponseEntity.ok(repository);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get test methods for a specific repository
     */
    @GetMapping("/{repositoryId}/test-methods")
    public ResponseEntity<List<TestMethodDetailDto>> getRepositoryTestMethods(
            @PathVariable Long repositoryId,
            @RequestParam(defaultValue = "100") Integer limit) {
        List<TestMethodDetailDto> testMethods = repositoryDataService.getTestMethodDetails(null, limit);
        // Filter by repository name if needed (since TestMethodDetailDto doesn't have repositoryId)
        List<TestMethodDetailDto> repositoryTestMethods = testMethods.stream()
            .filter(method -> method.getRepository() != null && method.getRepository().contains(String.valueOf(repositoryId)))
            .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(repositoryTestMethods);
    }

    /**
     * Get repositories by team
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<RepositoryMetricsDto>> getRepositoriesByTeam(@PathVariable Long teamId) {
        List<RepositoryMetricsDto> repositories = repositoryDataService.getRepositoryMetricsByTeamId(teamId);
        return ResponseEntity.ok(repositories);
    }

    /**
     * Search repositories by name or team
     */
    @GetMapping("/search")
    public ResponseEntity<List<RepositoryMetricsDto>> searchRepositories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String coverage) {
        
        List<RepositoryMetricsDto> allRepositories = repositoryDataService.getAllRepositoryMetrics();
        
        List<RepositoryMetricsDto> filteredRepositories = allRepositories.stream()
            .filter(repo -> {
                boolean matchesName = name == null || repo.getRepositoryName().toLowerCase().contains(name.toLowerCase());
                boolean matchesTeam = team == null || repo.getTeamName().equalsIgnoreCase(team);
                boolean matchesCoverage = coverage == null || matchesCoverageRange(repo.getCoverageRate(), coverage);
                return matchesName && matchesTeam && matchesCoverage;
            })
            .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(filteredRepositories);
    }

    /**
     * Helper method to check if coverage matches the specified range
     */
    private boolean matchesCoverageRange(double coverage, String range) {
        switch (range.toLowerCase()) {
            case "high":
                return coverage >= 80;
            case "medium":
                return coverage >= 50 && coverage < 80;
            case "low":
                return coverage < 50;
            default:
                return true;
        }
    }
}
