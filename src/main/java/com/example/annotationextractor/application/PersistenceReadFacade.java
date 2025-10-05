package com.example.annotationextractor.application;

import com.example.annotationextractor.adapters.persistence.jdbc.*;
import com.example.annotationextractor.domain.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Simple facade composing read-only use-cases. Pure Java, manually wired.
 */
public class PersistenceReadFacade {

    private final RepositoryQueryService repositoryQueryService;
    private final TeamQueryService teamQueryService;
    private final ScanSessionQueryService scanSessionQueryService;
    private final TestArtifactQueryService testArtifactQueryService;
    private final DailyMetricQueryService dailyMetricQueryService;

    public PersistenceReadFacade() {
        // Manual wiring with JDBC adapters (no Spring required yet)
        this.repositoryQueryService = new RepositoryQueryService(new JdbcRepositoryRecordAdapter());
        this.teamQueryService = new TeamQueryService(new JdbcTeamAdapter());
        this.scanSessionQueryService = new ScanSessionQueryService(new JdbcScanSessionAdapter());
        this.testArtifactQueryService = new TestArtifactQueryService(new JdbcTestClassAdapter(), new JdbcTestMethodAdapter());
        this.dailyMetricQueryService = new DailyMetricQueryService(new JdbcDailyMetricAdapter());
    }

    // Repositories
    public Optional<RepositoryRecord> getRepositoryByGitUrl(String gitUrl) {
        return repositoryQueryService.getByGitUrl(gitUrl);
    }
    public List<RepositoryRecord> listRepositoriesByTeam(Long teamId) { return repositoryQueryService.listByTeamId(teamId); }
    public List<RepositoryRecord> listAllRepositories() { return repositoryQueryService.listAll(); }

    // Teams
    public List<Team> listTeams() { return teamQueryService.listAll(); }

    // Scan sessions
    public List<ScanSession> recentScanSessions(int limit) { return scanSessionQueryService.recent(limit); }

    // Test artifacts
    public List<TestClass> listClassesByRepository(Long repositoryId) { return testArtifactQueryService.listClassesByRepository(repositoryId); }
    public List<TestMethod> listAnnotatedMethodsByRepository(Long repositoryId) { return testArtifactQueryService.listAnnotatedMethodsByRepository(repositoryId); }

    // Daily metrics
    public List<DailyMetric> recentDailyMetrics(int limit) { return dailyMetricQueryService.recent(limit); }
    public List<DailyMetric> dailyMetricsRange(LocalDate start, LocalDate end) { return dailyMetricQueryService.range(start, end); }
    
    // Dashboard-specific detailed queries
    public List<RepositoryDetailRecord> listRepositoryDetails() { 
        return repositoryQueryService.listRepositoryDetails(); 
    }
    public List<TestMethodDetailRecord> listTestMethodDetails(Long teamId, Integer limit) { 
        return testArtifactQueryService.listTestMethodDetails(teamId, limit); 
    }
}


