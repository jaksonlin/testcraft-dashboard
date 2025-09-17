package com.example.annotationextractor.database;

import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.RepositoryRecord;
import com.example.annotationextractor.domain.model.Team;
import com.example.annotationextractor.casemodel.TestCollectionSummary;
import com.example.annotationextractor.util.PerformanceMonitor;
import com.example.annotationextractor.application.PersistenceWriteFacade;

import org.postgresql.util.PGobject;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for persisting scan results to the database
 * Optimized with batch operations for large-scale scanning
 */
public class DataPersistenceService {
    
    // Batch size for optimal performance
    private static final int BATCH_SIZE = 1000;
    
    /**
     * Feature toggle for hexagonal read path delegation.
     * Enable by running with -Dhex.read.enabled=true
     */
    private static boolean isHexReadEnabled() {
        return Boolean.parseBoolean(System.getProperty("hex.read.enabled", "false"));
    }

    private static PersistenceReadFacade getReadFacade() {
        // Avoid global static singletons; construct on demand to honor user's preference
        return new PersistenceReadFacade();
    }

    private static boolean isHexWriteEnabled() {
        return Boolean.parseBoolean(System.getProperty("hex.write.enabled", "false"));
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
     * TODO: Move to application layer use-case
     */
    public static void assignRepositoryToTeam(String gitUrl, String teamName, String teamCode) throws SQLException {
        // For now, delegate to legacy SQL until we create a TeamManagementUseCase
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int teamId = ensureTeamExists(conn, teamName, teamCode);
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE repositories SET team_id = ? WHERE git_url = ?")) {
                    stmt.setInt(1, teamId);
                    stmt.setString(2, gitUrl);
                    int updatedRows = stmt.executeUpdate();
                    if (updatedRows == 0) {
                        System.out.println("⚠️ Warning: No repository found with git URL: " + gitUrl);
                    } else {
                        System.out.println("✅ Assigned repository " + gitUrl + " to team: " + teamName + " (" + teamCode + ")");
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Ensure team exists, create if it doesn't
     */
    private static int ensureTeamExists(Connection conn, String teamName, String teamCode) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM teams WHERE team_code = ?")) {
            stmt.setString(1, teamCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO teams (team_name, team_code) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, teamName);
            stmt.setString(2, teamCode);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println("✅ Created new team: " + teamName + " (" + teamCode + ")");
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create team: " + teamName + " (" + teamCode + ")");
    }

    /**
     * Get all teams with their repository counts
     */
    public static Map<String, Integer> getTeamRepositoryCounts() throws SQLException {
        if (isHexReadEnabled()) {
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
        } else {
            // Legacy SQL path (to be removed when read delegation is complete)
        Map<String, Integer> teamCounts = new LinkedHashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                 SELECT t.team_name, COUNT(r.id) as repo_count
                 FROM teams t
                 LEFT JOIN repositories r ON t.id = r.team_id
                 GROUP BY t.id, t.team_name
                 ORDER BY t.team_name
                 """)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String teamName = rs.getString("team_name");
                    int repoCount = rs.getInt("repo_count");
                    teamCounts.put(teamName, repoCount);
                }
            }
        }
        return teamCounts;
        }
    }
    
    /**
     * Get repositories without team assignments
     */
    public static List<String> getUnassignedRepositories() throws SQLException {
        if (isHexReadEnabled()) {
            List<String> result = new ArrayList<>();
            for (RepositoryRecord r : getReadFacade().listAllRepositories()) {
                if (r.getTeamId() == null && r.getGitUrl() != null) {
                    result.add(r.getGitUrl());
                }
            }
            result.sort(String::compareTo);
            return result;
        } else {
            // Legacy SQL path (to be removed when read delegation is complete)
        List<String> unassigned = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                 SELECT git_url FROM repositories 
                 WHERE team_id IS NULL
                 ORDER BY git_url
                 """)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    unassigned.add(rs.getString("git_url"));
                }
            }
        }
        return unassigned;
        }
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
