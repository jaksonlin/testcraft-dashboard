package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.service.RepositoryDataService;
import com.example.annotationextractor.web.dto.RepositoryMetricsDto;
import com.example.annotationextractor.web.dto.RepositoryDetailDto;
import com.example.annotationextractor.web.dto.TestMethodDetailDto;
import com.example.annotationextractor.web.dto.TestClassSummaryDto;
import com.example.annotationextractor.web.dto.PagedResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
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
     * Get repositories with pagination and filtering
     */
    @GetMapping("/paginated")
    public ResponseEntity<PagedResponse<RepositoryMetricsDto>> getRepositoriesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String coverage,
            @RequestParam(required = false) String testMethods,
            @RequestParam(required = false) String lastScan,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        try {
            PagedResponse<RepositoryMetricsDto> response = repositoryDataService.getRepositoriesPaginated(
                page, size, search, team, coverage, testMethods, lastScan, sortBy, sortOrder);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching paginated repositories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
     * Get test classes for a specific repository (paginated)
     */
    @GetMapping("/{repositoryId}/classes/paginated")
    public ResponseEntity<PagedResponse<TestClassSummaryDto>> getRepositoryClassesPaginated(
            @PathVariable Long repositoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Boolean annotated) {
        try {
            PagedResponse<TestClassSummaryDto> response = repositoryDataService.getRepositoryClassesPaginated(
                repositoryId, page, size, className, annotated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching paginated repository classes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get test classes for a specific repository
     */
    @GetMapping("/{repositoryId}/classes")
    public ResponseEntity<List<TestClassSummaryDto>> getRepositoryClasses(@PathVariable Long repositoryId) {
        List<TestClassSummaryDto> classes = repositoryDataService.getRepositoryClasses(repositoryId);
        return ResponseEntity.ok(classes);
    }

    /**
     * Get test methods for a specific class in a repository
     */
    @GetMapping("/{repositoryId}/classes/{classId}/methods")
    public ResponseEntity<List<TestMethodDetailDto>> getClassMethods(
            @PathVariable Long repositoryId,
            @PathVariable Long classId,
            @RequestParam(defaultValue = "100") Integer limit) {
        List<TestMethodDetailDto> methods = repositoryDataService.getTestMethodsByClassId(classId, limit);
        return ResponseEntity.ok(methods);
    }

    /**
     * Get all test methods for a specific repository
     */
    @GetMapping("/{repositoryId}/test-methods")
    public ResponseEntity<List<TestMethodDetailDto>> getRepositoryTestMethods(
            @PathVariable Long repositoryId,
            @RequestParam(defaultValue = "100") Integer limit) {
        List<TestMethodDetailDto> methods = repositoryDataService.getTestMethodsByRepositoryId(repositoryId, limit);
        return ResponseEntity.ok(methods);
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
