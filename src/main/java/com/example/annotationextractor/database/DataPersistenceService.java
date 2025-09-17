package com.example.annotationextractor.database;

import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.RepositoryRecord;
import com.example.annotationextractor.domain.model.Team;
import com.example.annotationextractor.casemodel.TestCollectionSummary;
import com.example.annotationextractor.util.PerformanceMonitor;
import com.example.annotationextractor.application.PersistenceWriteFacade;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for persisting scan results to the database
 * Optimized with batch operations for large-scale scanning
 */
public class DataPersistenceService {
    


    private static PersistenceReadFacade getReadFacade() {
        // Avoid global static singletons; construct on demand to honor user's preference
        return new PersistenceReadFacade();
    }

    private static boolean isHexWriteShadow() {
        return Boolean.parseBoolean(System.getProperty("hex.write.shadow", "false"));
    }

    private static PersistenceWriteFacade getWriteFacade() {
        return new PersistenceWriteFacade();
    }
    
    /**
     * Persist a complete scan session with all its data using batch operations
     */
    public static long persistScanSession(TestCollectionSummary summary, long scanDurationMs) throws SQLException {
        PerformanceMonitor.startOperation("Database Persistence");

        // Primary write using the new path
        long id = getWriteFacade().persistScanSession(summary, scanDurationMs);

        // Optional shadow write to separate DB for perf/mirroring
        if (isHexWriteShadow()) {
            try {
                getWriteFacade().persistScanSessionShadow(summary, scanDurationMs);
            } catch (Exception e) {
                System.err.println("[SHADOW] Shadow write failed: " + e.getMessage());
            }
        }

        PerformanceMonitor.endOperation("Database Persistence");
        return id;
    }
    



     /**
     * Assign a repository to a team
     * TODO: Move to application layer use-case - this method should be removed
     * and replaced with a proper TeamManagementUseCase in the application layer
     */
    @Deprecated
    public static void assignRepositoryToTeam(String gitUrl, String teamName, String teamCode) throws SQLException {
        throw new UnsupportedOperationException(
            "assignRepositoryToTeam is deprecated. " +
            "This functionality should be moved to a TeamManagementUseCase in the application layer. " +
            "The hexagonal architecture refactor is complete - no direct SQL should remain in this class."
        );
    }

    /**
     * Get all teams with their repository counts
     */
    public static Map<String, Integer> getTeamRepositoryCounts() throws SQLException {

            Map<String, Integer> result = new LinkedHashMap<>();
            List<Team> teams = getReadFacade().listTeams();
            List<RepositoryRecord> repos = getReadFacade().listAllRepositories();
            java.util.Map<Long, String> teamIdToName = new java.util.HashMap<>();
            for (Team t : teams) {
                if (t.getId() != null) {
                    teamIdToName.put(t.getId(), t.getTeamName());
                }
            }
            // Initialize counts to 0 for all teams
            for (Team t : teams) {
                result.put(t.getTeamName(), 0);
            }
            for (RepositoryRecord r : repos) {
                Long teamId = r.getTeamId();
                if (teamId != null) {
                    String name = teamIdToName.get(teamId);
                    if (name != null) {
                        result.put(name, result.getOrDefault(name, 0) + 1);
                    }
                }
            }
            // Return ordered by team name
            Map<String, Integer> ordered = new LinkedHashMap<>();
            result.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .forEach(e -> ordered.put(e.getKey(), e.getValue()));
            return ordered;

    }
    
    /**
     * Get repositories without team assignments
     */
    public static List<String> getUnassignedRepositories() throws SQLException {

            List<String> result = new ArrayList<>();
            for (RepositoryRecord r : getReadFacade().listAllRepositories()) {
                if (r.getTeamId() == null && r.getGitUrl() != null) {
                    result.add(r.getGitUrl());
                }
            }
            result.sort(String::compareTo);
            return result;

    }

    /**
     * Validate team assignments
     */
    public static void validateTeamAssignments() throws SQLException {
        System.out.println("\n📋 Team Assignment Validation Report");
        System.out.println("=====================================");
        
        // Get team counts
        Map<String, Integer> teamCounts = getTeamRepositoryCounts();
        System.out.println("\nTeams and Repository Counts:");
        for (Map.Entry<String, Integer> entry : teamCounts.entrySet()) {
            System.out.println("  • " + entry.getKey() + ": " + entry.getValue() + " repositories");
        }
        
        // Get unassigned repositories
        List<String> unassigned = getUnassignedRepositories();
        if (unassigned.isEmpty()) {
            System.out.println("\n✅ All repositories are assigned to teams!");
        } else {
            System.out.println("\n⚠️  Unassigned repositories (" + unassigned.size() + "):");
            for (String repo : unassigned) {
                System.out.println("  • " + repo);
            }
            System.out.println("\n💡 Use TeamManager.loadTeamAssignmentsFromCSV() to assign these repositories");
        }
        
        // Get total repository count (using new facade)
        long totalRepos = getReadFacade().listAllRepositories().size();
        int assignedRepos = (int) totalRepos - unassigned.size();
                    System.out.println("\n📊 Summary:");
                    System.out.println("  • Total repositories: " + totalRepos);
                    System.out.println("  • Assigned to teams: " + assignedRepos);
                    System.out.println("  • Unassigned: " + unassigned.size());
                    System.out.println("  • Assignment rate: " + String.format("%.1f%%", (assignedRepos * 100.0 / totalRepos)));
    }
    
}
