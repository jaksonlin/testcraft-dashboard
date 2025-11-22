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
    public Optional<ScanSession> getLatestCompletedScanSession() { return scanSessionQueryService.getLatestCompleted(); }

    // Test artifacts
    public List<TestClass> listClassesByScanSessionId(Long scanSessionId) { return testArtifactQueryService.listClassesByScanSessionId(scanSessionId); }
    public List<TestClass> listClassesByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId) { return testArtifactQueryService.listClassesByRepositoryIdAndScanSessionId(repositoryId, scanSessionId); }
    public Optional<TestClass> listClassByRepositoryIdAndScanSessionIdAndFilePath(Long repositoryId, Long scanSessionId, String filePath) { return testArtifactQueryService.listClassByRepositoryIdAndScanSessionIdAndFilePath(repositoryId, scanSessionId, filePath); }
    public long countClassesByScanSessionId(Long scanSessionId) { return testArtifactQueryService.countClassesByScanSessionId(scanSessionId); }
    public Optional<TestClass> getTestClassById(Long classId) { return testArtifactQueryService.getTestClassById(classId); }
    public List<TestMethod> listMethodsByTestClassId(Long testClassId) { return testArtifactQueryService.listMethodsByTestClassId(testClassId); } 
    public List<TestMethod> listMethodsByScanSessionId(Long scanSessionId) { return testArtifactQueryService.listMethodsByScanSessionId(scanSessionId); }
    public List<TestMethod> listAnnotatedMethodsByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId) { return testArtifactQueryService.listAnnotatedMethodsByRepositoryIdAndScanSessionId(repositoryId, scanSessionId); }
    public Optional<TestMethod> getTestMethodById(Long methodId) { return testArtifactQueryService.getTestMethodById(methodId); }
    
    // list by scan session
    public List<TestMethodDetailRecord> listTestMethodDetailsByScanSessionId(Long scanSessionId, Integer limit) { return testArtifactQueryService.listTestMethodDetailsByScanSessionId(scanSessionId, limit); }
    public long countTestMethodDetailsByScanSessionId(Long scanSessionId) { return testArtifactQueryService.countTestMethodDetailsByScanSessionId(scanSessionId); }

    // list with filters (DATABASE-level filtering, no client-side filtering)
    public List<TestMethodDetailRecord> listTestMethodDetailsWithFilters(
            Long scanSessionId, String teamName, String repositoryName, 
            String packageName, String className, Boolean annotated, String searchTerm, String codePattern, Integer offset, Integer limit) {
        return testArtifactQueryService.listTestMethodDetailsWithFilters(
            scanSessionId, teamName, repositoryName, packageName, className, annotated, searchTerm, codePattern, offset, limit);
    }
    public long countTestMethodDetailsWithFilters(
            Long scanSessionId, String teamName, String repositoryName, 
            String packageName, String className, Boolean annotated, String searchTerm, String codePattern) {
        return testArtifactQueryService.countTestMethodDetailsWithFilters(
            scanSessionId, teamName, repositoryName, packageName, className, annotated, searchTerm, codePattern);
    }

    // list by team and scan session
    public List<TestMethodDetailRecord> listTestMethodDetailsByTeamIdAndScanSessionId(Long teamId, Long scanSessionId, Integer limit) { 
        return testArtifactQueryService.listTestMethodDetailsByTeamIdAndScanSessionId(teamId, scanSessionId, limit); 
    }
    public long countTestMethodDetailsByTeamIdAndScanSessionId(Long teamId, Long scanSessionId) { return testArtifactQueryService.countTestMethodDetailsByTeamIdAndScanSessionId(teamId, scanSessionId); }

    // list by repository and scan session
    public List<TestMethodDetailRecord> listTestMethodDetailsByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId, Integer limit) { 
        return testArtifactQueryService.listTestMethodDetailsByRepositoryIdAndScanSessionId(repositoryId, scanSessionId, limit); 
    }
    public long countTestMethodDetailsByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId) { return testArtifactQueryService.countTestMethodDetailsByRepositoryIdAndScanSessionId(repositoryId, scanSessionId); }
    
    // list by class id
    public List<TestMethodDetailRecord> listTestMethodDetailsByClassId(Long classId, Integer limit) { return testArtifactQueryService.listTestMethodDetailsByClassId(classId, limit); }
    public long countTestMethodDetailsByClassId(Long classId) { return testArtifactQueryService.countTestMethodDetailsByClassId(classId); }

    // Daily metrics
    public List<DailyMetric> recentDailyMetrics(int limit) { return dailyMetricQueryService.recent(limit); }
    public List<DailyMetric> dailyMetricsRange(LocalDate start, LocalDate end) { return dailyMetricQueryService.range(start, end); }
    
    // Dashboard-specific detailed queries
    public List<RepositoryDetailRecord> listRepositoryDetails() { 
        return repositoryQueryService.listRepositoryDetails();  
    }
    
    // Hierarchical queries for progressive loading (database-level aggregation)
    public List<java.util.Map<String, Object>> getHierarchyByTeam(Long scanSessionId) {
        return testArtifactQueryService.getHierarchyByTeam(scanSessionId);
    }
    public List<java.util.Map<String, Object>> getHierarchyByPackage(Long scanSessionId, String teamName) {
        return testArtifactQueryService.getHierarchyByPackage(scanSessionId, teamName);
    }
    public List<java.util.Map<String, Object>> getHierarchyByClass(Long scanSessionId, String teamName, String packageName) {
        return testArtifactQueryService.getHierarchyByClass(scanSessionId, teamName, packageName);
    }
    
}


