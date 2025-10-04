package com.example.annotationextractor.service;

import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.*;
import com.example.annotationextractor.web.dto.*;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for transforming domain models to dashboard DTOs
 */
@Service
public class DashboardDataService {

    private final Optional<PersistenceReadFacade> persistenceReadFacade;

    public DashboardDataService(Optional<PersistenceReadFacade> persistenceReadFacade) {
        this.persistenceReadFacade = persistenceReadFacade;
    }

    /**
     * Get dashboard overview data
     */
    public DashboardOverviewDto getDashboardOverview() {
        DashboardOverviewDto overview = new DashboardOverviewDto();
        
        if (persistenceReadFacade.isPresent()) {
            try {
                // Get all repositories
                List<RepositoryRecord> repositories = persistenceReadFacade.get().listAllRepositories();
                List<Team> teams = persistenceReadFacade.get().listTeams();
                List<ScanSession> recentSessions = persistenceReadFacade.get().recentScanSessions(1);
            
            // Calculate totals
            int totalRepositories = repositories.size();
            int totalTeams = teams.size();
            int totalTestClasses = repositories.stream().mapToInt(RepositoryRecord::getTotalTestClasses).sum();
            int totalTestMethods = repositories.stream().mapToInt(RepositoryRecord::getTotalTestMethods).sum();
            int totalAnnotatedMethods = repositories.stream().mapToInt(RepositoryRecord::getTotalAnnotatedMethods).sum();
            
            // Calculate overall coverage rate
            double overallCoverageRate = totalTestMethods > 0 ? 
                (double) totalAnnotatedMethods / totalTestMethods * 100 : 0.0;
            
            // Set overview data
            overview.setTotalRepositories(totalRepositories);
            overview.setTotalTeams(totalTeams);
            overview.setTotalTestClasses(totalTestClasses);
            overview.setTotalTestMethods(totalTestMethods);
            overview.setTotalAnnotatedMethods(totalAnnotatedMethods);
            overview.setOverallCoverageRate(overallCoverageRate);
            
            // Set last scan date
            if (!recentSessions.isEmpty()) {
                overview.setLastScanDate(recentSessions.get(0).getScanDate()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            
            // Get top teams (by repository count)
            overview.setTopTeams(getTopTeams(teams, repositories, 5));
            
                // Get top repositories (by coverage rate)
                overview.setTopRepositories(getTopRepositories(repositories, teams, 5));
                
            } catch (Exception e) {
                // If database is not available, return empty data
                overview.setTotalRepositories(0);
                overview.setTotalTeams(0);
                overview.setTotalTestClasses(0);
                overview.setTotalTestMethods(0);
                overview.setTotalAnnotatedMethods(0);
                overview.setOverallCoverageRate(0.0);
            }
        } else {
            // Return mock data when database integration is disabled
            overview.setTotalRepositories(3);
            overview.setTotalTeams(2);
            overview.setTotalTestClasses(15);
            overview.setTotalTestMethods(127);
            overview.setTotalAnnotatedMethods(45);
            overview.setOverallCoverageRate(35.43);
        }
        
        return overview;
    }

    /**
     * Get team metrics
     */
    public List<TeamMetricsDto> getTeamMetrics() {
        if (persistenceReadFacade.isPresent()) {
            try {
                List<Team> teams = persistenceReadFacade.get().listTeams();
                List<RepositoryRecord> repositories = persistenceReadFacade.get().listAllRepositories();
                
                return teams.stream()
                    .map(team -> convertToTeamMetricsDto(team, repositories))
                    .collect(Collectors.toList());
                    
            } catch (Exception e) {
                return List.of();
            }
        } else {
            // Return mock team data
            TeamMetricsDto team1 = new TeamMetricsDto(1L, "Backend Team", "BE");
            team1.setRepositoryCount(2);
            team1.setAverageCoverageRate(88.5);
            team1.setTotalTestMethods(67);
            team1.setTotalAnnotatedMethods(23);
            
            TeamMetricsDto team2 = new TeamMetricsDto(2L, "Frontend Team", "FE");
            team2.setRepositoryCount(1);
            team2.setAverageCoverageRate(72.3);
            team2.setTotalTestMethods(60);
            team2.setTotalAnnotatedMethods(22);
            
            return List.of(team1, team2);
        }
    }

    /**
     * Get repository metrics for a specific team
     */
    public List<RepositoryMetricsDto> getRepositoryMetrics(Long teamId) {
        if (persistenceReadFacade.isPresent()) {
            try {
                List<RepositoryRecord> repositories = persistenceReadFacade.get().listRepositoriesByTeam(teamId);
                List<Team> teams = persistenceReadFacade.get().listTeams();
                
                return repositories.stream()
                    .map(repo -> convertToRepositoryMetricsDto(repo, teams))
                    .collect(Collectors.toList());
                    
            } catch (Exception e) {
                return List.of();
            }
        } else {
            // Return mock repository data
            RepositoryMetricsDto repo1 = new RepositoryMetricsDto(1L, "user-service", "https://github.com/company/user-service");
            repo1.setTeamName("Backend Team");
            repo1.setTestClassCount(8);
            repo1.setTestMethodCount(67);
            repo1.setAnnotatedMethodCount(23);
            repo1.setCoverageRate(34.33);
            
            return List.of(repo1);
        }
    }

    /**
     * Get coverage trends for the specified period
     */
    public List<DailyMetric> getCoverageTrends(int days) {
        if (persistenceReadFacade.isPresent()) {
            try {
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(days);
                return persistenceReadFacade.get().dailyMetricsRange(startDate, endDate);
            } catch (Exception e) {
                return List.of();
            }
        } else {
            // Return empty list for mock data
            return List.of();
        }
    }

    /**
     * Get recent scan sessions
     */
    public List<ScanSession> getRecentScanSessions(int limit) {
        if (persistenceReadFacade.isPresent()) {
            try {
                return persistenceReadFacade.get().recentScanSessions(limit);
            } catch (Exception e) {
                return List.of();
            }
        } else {
            // Return empty list for mock data
            return List.of();
        }
    }

    /**
     * Get repository details matching Excel Repository Details sheet format
     */
    public List<RepositoryDetailDto> getRepositoryDetails() {
        if (persistenceReadFacade.isPresent()) {
            try {
                return getRepositoryDetailsFromDatabase();
            } catch (Exception e) {
                return List.of();
            }
        } else {
            // Return mock repository details
            RepositoryDetailDto repo1 = new RepositoryDetailDto(1L, "user-service", "https://github.com/company/user-service");
            repo1.setPath("/repos/user-service");
            repo1.setTestMethodCount(67);
            repo1.setTestClasses(8);
            repo1.setAnnotatedMethods(23);
            repo1.setCoverageRate(34.33);
            repo1.setLastScan(LocalDateTime.now().minusDays(1));
            repo1.setTeamName("Backend Team");
            repo1.setTeamCode("BE");
            
            RepositoryDetailDto repo2 = new RepositoryDetailDto(2L, "payment-service", "https://github.com/company/payment-service");
            repo2.setPath("/repos/payment-service");
            repo2.setTestMethodCount(45);
            repo2.setTestClasses(6);
            repo2.setAnnotatedMethods(18);
            repo2.setCoverageRate(40.0);
            repo2.setLastScan(LocalDateTime.now().minusDays(2));
            repo2.setTeamName("Backend Team");
            repo2.setTeamCode("BE");
            
            return List.of(repo1, repo2);
        }
    }

    /**
     * Get test method details matching Excel Test Method Details sheet format
     */
    public List<TestMethodDetailDto> getTestMethodDetails(Long teamId, Integer limit) {
        if (persistenceReadFacade.isPresent()) {
            try {
                return getTestMethodDetailsFromDatabase(teamId, limit);
            } catch (Exception e) {
                return List.of();
            }
        } else {
            // Return mock test method details
            TestMethodDetailDto method1 = new TestMethodDetailDto(1L, "user-service", "UserServiceTest", "testCreateUser");
            method1.setLine(45);
            method1.setTitle("Test user creation with valid data");
            method1.setAuthor("John Doe");
            method1.setStatus("PASSED");
            method1.setTargetClass("UserService");
            method1.setTargetMethod("createUser");
            method1.setDescription("Verify that user creation works with valid input data");
            method1.setTestPoints("UC001, UC002");
            method1.setTags(List.of("smoke", "user-management"));
            method1.setRequirements(List.of("REQ001", "REQ002"));
            method1.setTestCaseIds(List.of("TC001", "TC002"));
            method1.setDefects(List.of());
            method1.setLastModified(LocalDateTime.now().minusDays(1));
            method1.setLastUpdateAuthor("Jane Smith");
            method1.setTeamName("Backend Team");
            method1.setTeamCode("BE");
            method1.setGitUrl("https://github.com/company/user-service");
            
            return List.of(method1);
        }
    }

    // Helper methods for data transformation

    private List<TeamSummaryDto> getTopTeams(List<Team> teams, List<RepositoryRecord> repositories, int limit) {
        return teams.stream()
            .map(team -> {
                TeamSummaryDto summary = new TeamSummaryDto(team.getId(), team.getTeamName(), team.getTeamCode());
                List<RepositoryRecord> teamRepos = repositories.stream()
                    .filter(repo -> team.getId().equals(repo.getTeamId()))
                    .collect(Collectors.toList());
                
                summary.setRepositoryCount(teamRepos.size());
                summary.setTotalTestMethods(teamRepos.stream().mapToInt(RepositoryRecord::getTotalTestMethods).sum());
                summary.setTotalAnnotatedMethods(teamRepos.stream().mapToInt(RepositoryRecord::getTotalAnnotatedMethods).sum());
                
                double avgCoverage = teamRepos.stream()
                    .mapToDouble(RepositoryRecord::getAnnotationCoverageRate)
                    .average()
                    .orElse(0.0);
                summary.setAverageCoverageRate(avgCoverage);
                
                return summary;
            })
            .sorted((a, b) -> Integer.compare(b.getRepositoryCount(), a.getRepositoryCount()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private List<RepositorySummaryDto> getTopRepositories(List<RepositoryRecord> repositories, List<Team> teams, int limit) {
        return repositories.stream()
            .map(repo -> {
                RepositorySummaryDto summary = new RepositorySummaryDto(repo.getId(), repo.getRepositoryName(), repo.getGitUrl());
                
                // Find team name
                String teamName = teams.stream()
                    .filter(team -> team.getId().equals(repo.getTeamId()))
                    .findFirst()
                    .map(Team::getTeamName)
                    .orElse("Unknown");
                
                summary.setTeamName(teamName);
                summary.setTestClassCount(repo.getTotalTestClasses());
                summary.setTestMethodCount(repo.getTotalTestMethods());
                summary.setAnnotatedMethodCount(repo.getTotalAnnotatedMethods());
                summary.setCoverageRate(repo.getAnnotationCoverageRate());
                summary.setLastScanDate(repo.getLastScanDate()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
                
                return summary;
            })
            .sorted((a, b) -> Double.compare(b.getCoverageRate(), a.getCoverageRate()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private TeamMetricsDto convertToTeamMetricsDto(Team team, List<RepositoryRecord> repositories) {
        TeamMetricsDto dto = new TeamMetricsDto(team.getId(), team.getTeamName(), team.getTeamCode());
        dto.setDepartment(team.getDepartment());
        
        List<RepositoryRecord> teamRepos = repositories.stream()
            .filter(repo -> team.getId().equals(repo.getTeamId()))
            .collect(Collectors.toList());
        
        dto.setRepositoryCount(teamRepos.size());
        dto.setTotalTestClasses(teamRepos.stream().mapToInt(RepositoryRecord::getTotalTestClasses).sum());
        dto.setTotalTestMethods(teamRepos.stream().mapToInt(RepositoryRecord::getTotalTestMethods).sum());
        dto.setTotalAnnotatedMethods(teamRepos.stream().mapToInt(RepositoryRecord::getTotalAnnotatedMethods).sum());
        
        double avgCoverage = teamRepos.stream()
            .mapToDouble(RepositoryRecord::getAnnotationCoverageRate)
            .average()
            .orElse(0.0);
        dto.setAverageCoverageRate(avgCoverage);
        
        // Set repositories
        List<RepositorySummaryDto> repoSummaries = teamRepos.stream()
            .map(repo -> convertToRepositorySummaryDto(repo, List.of(team)))
            .collect(Collectors.toList());
        dto.setRepositories(repoSummaries);
        
        return dto;
    }

    private RepositoryMetricsDto convertToRepositoryMetricsDto(RepositoryRecord repo, List<Team> teams) {
        RepositoryMetricsDto dto = new RepositoryMetricsDto(repo.getId(), repo.getRepositoryName(), repo.getGitUrl());
        
        // Find team name
        String teamName = teams.stream()
            .filter(team -> team.getId().equals(repo.getTeamId()))
            .findFirst()
            .map(Team::getTeamName)
            .orElse("Unknown");
        
        dto.setTeamName(teamName);
        dto.setRepositoryPath(repo.getRepositoryPath());
        dto.setGitBranch(repo.getGitBranch());
        dto.setTechnologyStack(repo.getTechnologyStack());
        dto.setTestClassCount(repo.getTotalTestClasses());
        dto.setTestMethodCount(repo.getTotalTestMethods());
        dto.setAnnotatedMethodCount(repo.getTotalAnnotatedMethods());
        dto.setCoverageRate(repo.getAnnotationCoverageRate());
        dto.setFirstScanDate(repo.getFirstScanDate()
            .atZone(ZoneId.systemDefault()).toLocalDateTime());
        dto.setLastScanDate(repo.getLastScanDate()
            .atZone(ZoneId.systemDefault()).toLocalDateTime());
        
        return dto;
    }

    private RepositorySummaryDto convertToRepositorySummaryDto(RepositoryRecord repo, List<Team> teams) {
        RepositorySummaryDto dto = new RepositorySummaryDto(repo.getId(), repo.getRepositoryName(), repo.getGitUrl());
        
        String teamName = teams.stream()
            .filter(team -> team.getId().equals(repo.getTeamId()))
            .findFirst()
            .map(Team::getTeamName)
            .orElse("Unknown");
        
        dto.setTeamName(teamName);
        dto.setTestClassCount(repo.getTotalTestClasses());
        dto.setTestMethodCount(repo.getTotalTestMethods());
        dto.setAnnotatedMethodCount(repo.getTotalAnnotatedMethods());
        dto.setCoverageRate(repo.getAnnotationCoverageRate());
        dto.setLastScanDate(repo.getLastScanDate()
            .atZone(ZoneId.systemDefault()).toLocalDateTime());
        
        return dto;
    }

    /**
     * Get repository details from database matching Excel format
     */
    private List<RepositoryDetailDto> getRepositoryDetailsFromDatabase() throws SQLException {
        List<RepositoryDetailDto> repositories = new ArrayList<>();
        
        if (!persistenceReadFacade.isPresent()) {
            return repositories;
        }
        
        // Get DataSource from the facade's database config
        try (Connection conn = com.example.annotationextractor.database.DatabaseConfig.getConnection()) {
            
            String sql = """
                SELECT 
                    r.id,
                    r.repository_name,
                    r.repository_path,
                    r.git_url,
                    r.total_test_classes,
                    r.total_test_methods,
                    r.total_annotated_methods,
                    r.annotation_coverage_rate,
                    r.last_scan_date,
                    t.team_name,
                    t.team_code
                FROM repositories r
                LEFT JOIN teams t ON r.team_id = t.id
                ORDER BY r.annotation_coverage_rate DESC
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    RepositoryDetailDto dto = new RepositoryDetailDto();
                    dto.setId(rs.getLong("id"));
                    dto.setRepository(rs.getString("repository_name"));
                    dto.setPath(rs.getString("repository_path"));
                    dto.setGitUrl(rs.getString("git_url"));
                    dto.setTestClasses(rs.getInt("total_test_classes"));
                    dto.setTestMethodCount(rs.getInt("total_test_methods"));
                    dto.setAnnotatedMethods(rs.getInt("total_annotated_methods"));
                    dto.setCoverageRate(rs.getDouble("annotation_coverage_rate"));
                    
                    Timestamp lastScan = rs.getTimestamp("last_scan_date");
                    if (lastScan != null) {
                        dto.setLastScan(lastScan.toLocalDateTime());
                    }
                    
                    dto.setTeamName(rs.getString("team_name"));
                    dto.setTeamCode(rs.getString("team_code"));
                    
                    repositories.add(dto);
                }
            }
        }
        
        return repositories;
    }

    /**
     * Get test method details from database matching Excel format
     */
    private List<TestMethodDetailDto> getTestMethodDetailsFromDatabase(Long teamId, Integer limit) throws SQLException {
        List<TestMethodDetailDto> testMethods = new ArrayList<>();
        
        if (!persistenceReadFacade.isPresent()) {
            return testMethods;
        }
        
        try (Connection conn = com.example.annotationextractor.database.DatabaseConfig.getConnection()) {
            
            StringBuilder sql = new StringBuilder("""
                SELECT 
                    tm.id,
                    r.repository_name,
                    tc.class_name,
                    tm.method_name,
                    tm.line_number,
                    tm.annotation_title,
                    tm.annotation_author,
                    tm.annotation_status,
                    tm.annotation_target_class,
                    tm.annotation_target_method,
                    tm.annotation_description,
                    tm.annotation_test_points,
                    tm.annotation_tags,
                    tm.annotation_requirements,
                    tm.annotation_testcases,
                    tm.annotation_defects,
                    tm.annotation_last_update_time,
                    tm.annotation_last_update_author,
                    t.team_name,
                    t.team_code,
                    r.git_url
                FROM test_methods tm
                JOIN test_classes tc ON tm.test_class_id = tc.id
                JOIN repositories r ON tc.repository_id = r.id
                LEFT JOIN teams t ON r.team_id = t.id
                WHERE tm.has_annotation = true
                """);
            
            // Add team filter if specified
            if (teamId != null) {
                sql.append(" AND r.team_id = ?");
            }
            
            sql.append(" ORDER BY r.repository_name, tc.class_name, tm.method_name");
            
            // Add limit if specified
            if (limit != null && limit > 0) {
                sql.append(" LIMIT ?");
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                
                if (teamId != null) {
                    stmt.setLong(paramIndex++, teamId);
                }
                
                if (limit != null && limit > 0) {
                    stmt.setInt(paramIndex, limit);
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        TestMethodDetailDto dto = new TestMethodDetailDto();
                        dto.setId(rs.getLong("id"));
                        dto.setRepository(rs.getString("repository_name"));
                        dto.setTestClass(rs.getString("class_name"));
                        dto.setTestMethod(rs.getString("method_name"));
                        dto.setLine(rs.getInt("line_number"));
                        dto.setTitle(rs.getString("annotation_title"));
                        dto.setAuthor(rs.getString("annotation_author"));
                        dto.setStatus(rs.getString("annotation_status"));
                        dto.setTargetClass(rs.getString("annotation_target_class"));
                        dto.setTargetMethod(rs.getString("annotation_target_method"));
                        dto.setDescription(rs.getString("annotation_description"));
                        dto.setTestPoints(rs.getString("annotation_test_points"));
                        
                        // Parse JSON-like fields (stored as TEXT)
                        dto.setTags(parseStringArray(rs.getString("annotation_tags")));
                        dto.setRequirements(parseStringArray(rs.getString("annotation_requirements")));
                        dto.setTestCaseIds(parseStringArray(rs.getString("annotation_testcases")));
                        dto.setDefects(parseStringArray(rs.getString("annotation_defects")));
                        
                        // Parse timestamp
                        String lastUpdateTime = rs.getString("annotation_last_update_time");
                        if (lastUpdateTime != null && !lastUpdateTime.trim().isEmpty()) {
                            try {
                                dto.setLastModified(LocalDateTime.parse(lastUpdateTime));
                            } catch (Exception e) {
                                // If parsing fails, set to null
                                dto.setLastModified(null);
                            }
                        }
                        
                        dto.setLastUpdateAuthor(rs.getString("annotation_last_update_author"));
                        dto.setTeamName(rs.getString("team_name"));
                        dto.setTeamCode(rs.getString("team_code"));
                        dto.setGitUrl(rs.getString("git_url"));
                        
                        testMethods.add(dto);
                    }
                }
            }
        }
        
        return testMethods;
    }
    
    /**
     * Helper method to parse comma-separated string into list
     */
    private List<String> parseStringArray(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Split by comma and clean up
        return Arrays.stream(str.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
}
