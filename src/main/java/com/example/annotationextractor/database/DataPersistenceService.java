package com.example.annotationextractor.database;

import com.example.annotationextractor.casemodel.RepositoryTestInfo;
import com.example.annotationextractor.casemodel.TestClassInfo;
import com.example.annotationextractor.casemodel.TestCollectionSummary;
import com.example.annotationextractor.casemodel.TestMethodInfo;
import com.example.annotationextractor.casemodel.UnittestCaseInfoData;
import com.example.annotationextractor.runner.RepositoryHubRunnerConfig;
import com.example.annotationextractor.runner.RepositoryListProcessor;
import com.example.annotationextractor.util.PerformanceMonitor;

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
     * Persist a complete scan session with all its data using batch operations
     */
    public static long persistScanSession(TestCollectionSummary summary, long scanDurationMs) throws SQLException {
        PerformanceMonitor.startOperation("Database Persistence");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                long startTime = System.currentTimeMillis();
                int totalMethods = 0;
                
                // Insert scan session
                PerformanceMonitor.startOperation("Insert Scan Session");
                long scanSessionId = insertScanSession(conn, summary, scanDurationMs);
                PerformanceMonitor.endOperation("Insert Scan Session");
                
                // Persist repositories and their data using batch operations
                PerformanceMonitor.startOperation("Persist Repositories");
                for (RepositoryTestInfo repo : summary.getRepositories()) {

                    int teamId = ensureTeamExists(conn, repo.getTeamName(), repo.getTeamCode());

                    PerformanceMonitor.startOperation("Repository: " + repo.getRepositoryName());
                    long repositoryId = persistRepository(conn, repo, teamId);
                    
                    // Use batch operations for test classes and methods
                    persistTestClassesBatch(conn, repo, repositoryId, scanSessionId);
                    
                    totalMethods += repo.getTotalTestMethods();
                    PerformanceMonitor.endOperation("Repository: " + repo.getRepositoryName());
                    PerformanceMonitor.incrementCounter("Repositories Processed");
                    PerformanceMonitor.incrementCounter("Total Test Methods");
                }
                PerformanceMonitor.endOperation("Persist Repositories");

                
                // Update daily metrics
                PerformanceMonitor.startOperation("Update Daily Metrics");
                updateDailyMetrics(conn, summary);
                PerformanceMonitor.endOperation("Update Daily Metrics");
                
                conn.commit();
                
                long totalTime = System.currentTimeMillis() - startTime;
                System.out.println("Total database persistence: " + totalTime + "ms for " + totalMethods + " methods");
                System.out.println("Average time per method: " + (totalTime / (double) totalMethods) + "ms");
                
                PerformanceMonitor.endOperation("Database Persistence");
                return scanSessionId;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    

    
    /**
     * Insert scan session record
     */
    private static long insertScanSession(Connection conn, TestCollectionSummary summary, long scanDurationMs) throws SQLException {
        String sql = "INSERT INTO scan_sessions (scan_date, scan_directory, total_repositories, " +
                     "total_test_classes, total_test_methods, total_annotated_methods, scan_duration_ms) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(summary.getScanTimestamp()));
            stmt.setString(2, summary.getScanDirectory());
            stmt.setInt(3, summary.getTotalRepositories());
            stmt.setInt(4, summary.getTotalTestClasses());
            stmt.setInt(5, summary.getTotalTestMethods());
            stmt.setInt(6, summary.getTotalAnnotatedTestMethods());
            stmt.setLong(7, scanDurationMs);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to get scan session ID");
            }
        }
    }
    
    /**
     * Persist repository information
     */
    private static long persistRepository(Connection conn, RepositoryTestInfo repo, int teamId) throws SQLException {
        
        String sql = "INSERT INTO repositories (repository_name, repository_path, git_url, team_id, total_test_classes, " +
                     "total_test_methods, total_annotated_methods, annotation_coverage_rate, last_scan_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                     "ON CONFLICT (git_url) DO UPDATE SET " +
                     "repository_name = EXCLUDED.repository_name, " +
                     "repository_path = EXCLUDED.repository_path, " +
                     "team_id = EXCLUDED.team_id, " +
                     "total_test_classes = EXCLUDED.total_test_classes, " +
                     "total_test_methods = EXCLUDED.total_test_methods, " +
                     "total_annotated_methods = EXCLUDED.total_annotated_methods, " +
                     "annotation_coverage_rate = EXCLUDED.annotation_coverage_rate, " +
                     "last_scan_date = CURRENT_TIMESTAMP " +
                     "RETURNING id";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, repo.getRepositoryName());
            stmt.setString(2, repo.getRepositoryPathString());
            stmt.setString(3, repo.getGitUrl());
            stmt.setInt(4, teamId);
            stmt.setInt(5, repo.getTotalTestClasses());
            stmt.setInt(6, repo.getTotalTestMethods());
            stmt.setInt(7, repo.getTotalAnnotatedTestMethods());
            
            double coverageRate = repo.getTotalTestMethods() > 0 ? 
                (double) repo.getTotalAnnotatedTestMethods() / repo.getTotalTestMethods() * 100 : 0.0;
            stmt.setDouble(8, coverageRate);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to get repository ID");
            }
        }
    }
    
    /**
     * Persist test classes for a repository using batch operations
     */
    private static void persistTestClassesBatch(Connection conn, RepositoryTestInfo repo, long repositoryId, long scanSessionId) throws SQLException {
        if (repo.getTestClasses().isEmpty()) {
            return;
        }
        
        // Batch insert test classes
        String testClassSql = "INSERT INTO test_classes (repository_id, class_name, package_name, file_path, " +
                             "total_test_methods, annotated_test_methods, coverage_rate, scan_session_id) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                             "ON CONFLICT (repository_id, class_name, package_name) DO UPDATE SET " +
                             "total_test_methods = EXCLUDED.total_test_methods, " +
                             "annotated_test_methods = EXCLUDED.annotated_test_methods, " +
                             "coverage_rate = EXCLUDED.coverage_rate, " +
                             "last_modified_date = CURRENT_TIMESTAMP " +
                             "RETURNING id";
        
        try (PreparedStatement testClassStmt = conn.prepareStatement(testClassSql)) {
            int batchCount = 0;
            
            for (TestClassInfo testClass : repo.getTestClasses()) {
                // Set parameters for test class
                testClassStmt.setLong(1, repositoryId);
                testClassStmt.setString(2, testClass.getClassName());
                testClassStmt.setString(3, testClass.getPackageName());
                testClassStmt.setString(4, testClass.getFilePath());
                testClassStmt.setInt(5, testClass.getTotalTestMethods());
                testClassStmt.setInt(6, testClass.getAnnotatedTestMethods());
                
                double coverageRate = testClass.getTotalTestMethods() > 0 ? 
                    (double) testClass.getAnnotatedTestMethods() / testClass.getTotalTestMethods() * 100 : 0.0;
                testClassStmt.setDouble(7, coverageRate);
                testClassStmt.setLong(8, scanSessionId);
                
                // Add to batch
                testClassStmt.addBatch();
                batchCount++;
                
                // Execute batch when it reaches the batch size
                if (batchCount % BATCH_SIZE == 0) {
                    testClassStmt.executeBatch();
                    testClassStmt.clearBatch();
                }
            }
            
            // Execute remaining items in batch
            if (batchCount % BATCH_SIZE != 0) {
                testClassStmt.executeBatch();
            }
        }
        
        // Now batch insert all test methods for this repository
        persistTestMethodsBatch(conn, repo, repositoryId, scanSessionId);
    }
    
    /**
     * Persist test methods for a repository using batch operations
     */
    private static void persistTestMethodsBatch(Connection conn, RepositoryTestInfo repo, long repositoryId, long scanSessionId) throws SQLException {
        if (repo.getTestClasses().isEmpty()) {
            return;
        }
        
        // Collect all test methods from all test classes
        List<TestMethodInfo> allMethods = new java.util.ArrayList<>();
        for (TestClassInfo testClass : repo.getTestClasses()) {
            allMethods.addAll(testClass.getTestMethods());
        }
        
        if (allMethods.isEmpty()) {
            return;
        }
        
        // Batch insert test methods
        String testMethodSql = "INSERT INTO test_methods (test_class_id, method_name, line_number, has_annotation, " +
                              "annotation_data, annotation_title, annotation_author, annotation_status, " +
                              "annotation_target_class, annotation_target_method, annotation_description, " +
                              "annotation_tags, annotation_test_points, annotation_requirements, " +
                              "annotation_defects, annotation_testcases, annotation_last_update_time, " +
                              "annotation_last_update_author, scan_session_id) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                              "ON CONFLICT (test_class_id, method_name, method_signature) DO UPDATE SET " +
                              "has_annotation = EXCLUDED.has_annotation, " +
                              "annotation_data = EXCLUDED.annotation_data, " +
                              "annotation_title = EXCLUDED.annotation_title, " +
                              "annotation_author = EXCLUDED.annotation_author, " +
                              "annotation_status = EXCLUDED.annotation_status, " +
                              "annotation_target_class = EXCLUDED.annotation_target_class, " +
                              "annotation_target_method = EXCLUDED.annotation_target_method, " +
                              "annotation_description = EXCLUDED.annotation_description, " +
                              "annotation_tags = EXCLUDED.annotation_tags, " +
                              "annotation_test_points = EXCLUDED.annotation_test_points, " +
                              "annotation_requirements = EXCLUDED.annotation_requirements, " +
                              "annotation_defects = EXCLUDED.annotation_defects, " +
                              "annotation_testcases = EXCLUDED.annotation_testcases, " +
                              "annotation_last_update_time = EXCLUDED.annotation_last_update_time, " +
                              "annotation_last_update_author = EXCLUDED.annotation_last_update_author, " +
                              "last_modified_date = CURRENT_TIMESTAMP";
        
        try (PreparedStatement testMethodStmt = conn.prepareStatement(testMethodSql)) {
            int batchCount = 0;
            
            for (TestMethodInfo method : allMethods) {
                // Get test class ID for this method
                long testClassId = getTestClassId(conn, repositoryId, method.getClassName(), method.getPackageName());
                
                // Set basic parameters
                testMethodStmt.setLong(1, testClassId);
                testMethodStmt.setString(2, method.getMethodName());
                testMethodStmt.setInt(3, method.getLineNumber());
                
                // Handle annotation data
                UnittestCaseInfoData annotationData = method.getAnnotationData();
                boolean hasAnnotation = annotationData != null && !annotationData.getTitle().isEmpty();
                testMethodStmt.setBoolean(4, hasAnnotation);
                if (hasAnnotation) {
                    PGobject jsonObject = new PGobject();
                    jsonObject.setType("jsonb");
                    jsonObject.setValue(convertToJson(annotationData));
                    testMethodStmt.setObject(5, jsonObject);
                    
                    testMethodStmt.setString(6, annotationData.getTitle());
                    testMethodStmt.setString(7, annotationData.getAuthor());
                    testMethodStmt.setString(8, annotationData.getStatus());
                    testMethodStmt.setString(9, annotationData.getTargetClass());
                    testMethodStmt.setString(10, annotationData.getTargetMethod());
                    testMethodStmt.setString(11, annotationData.getDescription());
                    
                    // Convert arrays to comma-separated strings for TEXT fields
                    testMethodStmt.setString(12, arrayToString(annotationData.getTags()));
                    testMethodStmt.setString(13, arrayToString(annotationData.getTestPoints()));
                    testMethodStmt.setString(14, arrayToString(annotationData.getRelatedRequirements()));
                    testMethodStmt.setString(15, arrayToString(annotationData.getRelatedDefects()));
                    testMethodStmt.setString(16, arrayToString(annotationData.getRelatedTestcases()));
                    testMethodStmt.setString(17, annotationData.getLastUpdateTime());
                    testMethodStmt.setString(18, annotationData.getLastUpdateAuthor());
                } else {
                    testMethodStmt.setObject(5, null);
                    testMethodStmt.setString(6, null);
                    testMethodStmt.setString(7, null);
                    testMethodStmt.setString(8, null);
                    testMethodStmt.setString(9, null);
                    testMethodStmt.setString(10, null);
                    testMethodStmt.setString(11, null);
                    testMethodStmt.setString(12, null);
                    testMethodStmt.setString(13, null);
                    testMethodStmt.setString(14, null);
                    testMethodStmt.setString(15, null);
                    testMethodStmt.setString(16, null);
                    testMethodStmt.setString(17, null);
                    testMethodStmt.setString(18, null);
                }
                
                testMethodStmt.setLong(19, scanSessionId);
                
                // Add to batch
                testMethodStmt.addBatch();
                batchCount++;
                
                // Execute batch when it reaches the batch size
                if (batchCount % BATCH_SIZE == 0) {
                    testMethodStmt.executeBatch();
                    testMethodStmt.clearBatch();
                }
            }
            
            // Execute remaining items in batch
            if (batchCount % BATCH_SIZE != 0) {
                testMethodStmt.executeBatch();
            }
        }
    }
    
    /**
     * Get test class ID for a given repository, class name, and package
     */
    private static long getTestClassId(Connection conn, long repositoryId, String className, String packageName) throws SQLException {
        String sql = "SELECT id FROM test_classes WHERE repository_id = ? AND class_name = ? AND package_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, repositoryId);
            stmt.setString(2, className);
            stmt.setString(3, packageName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Test class not found: " + packageName + "." + className);
            }
        }
    }
    
    /**
     * Update daily metrics
     */
    private static void updateDailyMetrics(Connection conn, TestCollectionSummary summary) throws SQLException {
        String sql = "INSERT INTO daily_metrics (metric_date, total_repositories, total_test_classes, " +
                     "total_test_methods, total_annotated_methods, overall_coverage_rate) " +
                     "VALUES (CURRENT_DATE, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (metric_date) DO UPDATE SET " +
                     "total_repositories = EXCLUDED.total_repositories, " +
                     "total_test_classes = EXCLUDED.total_test_classes, " +
                     "total_test_methods = EXCLUDED.total_test_methods, " +
                     "total_annotated_methods = EXCLUDED.total_annotated_methods, " +
                     "overall_coverage_rate = EXCLUDED.overall_coverage_rate";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, summary.getTotalRepositories());
            stmt.setInt(2, summary.getTotalTestClasses());
            stmt.setInt(3, summary.getTotalTestMethods());
            stmt.setInt(4, summary.getTotalAnnotatedTestMethods());
            
            double overallCoverage = summary.getTotalTestMethods() > 0 ? 
                (double) summary.getTotalAnnotatedTestMethods() / summary.getTotalTestMethods() * 100 : 0.0;
            stmt.setDouble(5, overallCoverage);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Convert UnittestCaseInfoData to JSON string
     */
    private static String convertToJson(UnittestCaseInfoData data) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // Basic fields
        json.append("\"title\":\"").append(escapeJson(data.getTitle())).append("\",");
        json.append("\"author\":\"").append(escapeJson(data.getAuthor())).append("\",");
        json.append("\"status\":\"").append(escapeJson(data.getStatus())).append("\",");
        json.append("\"targetClass\":\"").append(escapeJson(data.getTargetClass())).append("\",");
        json.append("\"targetMethod\":\"").append(escapeJson(data.getTargetMethod())).append("\",");
        json.append("\"description\":\"").append(escapeJson(data.getDescription())).append("\",");
        
        // Array fields
        json.append("\"testPoints\":").append(arrayToJson(data.getTestPoints())).append(",");
        json.append("\"tags\":").append(arrayToJson(data.getTags())).append(",");
        json.append("\"relatedRequirements\":").append(arrayToJson(data.getRelatedRequirements())).append(",");
        json.append("\"relatedDefects\":").append(arrayToJson(data.getRelatedDefects())).append(",");
        json.append("\"relatedTestcases\":").append(arrayToJson(data.getRelatedTestcases())).append(",");
        
        // Additional fields
        json.append("\"lastUpdateTime\":\"").append(escapeJson(data.getLastUpdateTime())).append("\",");
        json.append("\"lastUpdateAuthor\":\"").append(escapeJson(data.getLastUpdateAuthor())).append("\",");
        json.append("\"methodSignature\":\"").append(escapeJson(data.getMethodSignature())).append("\"");
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Escape JSON string values
     */
    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Convert string array to JSON array
     */
    private static String arrayToJson(String[] array) {
        if (array == null || array.length == 0) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < array.length; i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(escapeJson(array[i])).append("\"");
        }
        
        json.append("]");
        return json.toString();
    }

    /**
     * Convert string array to comma-separated string
     */
    private static String arrayToString(String[] array) {
        if (array == null) {
            return null;  // Return null for null arrays
        }
        if (array.length == 0) {
            return "";    // Return empty string for empty arrays
        }
        // Use semicolon as delimiter to avoid issues with comma in content
        return String.join(";", array);
    }

     /**
     * Assign a repository to a team
     */
    public static void assignRepositoryToTeam(String gitUrl, String teamName, String teamCode) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // First, ensure team exists
                int teamId = ensureTeamExists(conn, teamName, teamCode);
                
                // Update repository with team assignment
                try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE repositories SET team_id = ? WHERE git_url = ?")) {
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
        // Check if team already exists by team_code (should be unique)
        try (PreparedStatement stmt = conn.prepareStatement(
            "SELECT id FROM teams WHERE team_code = ?")) {
            stmt.setString(1, teamCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        
        // Create new team
        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO teams (team_name, team_code) VALUES (?, ?)", 
            Statement.RETURN_GENERATED_KEYS)) {
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
    
    /**
     * Get repositories without team assignments
     */
    public static List<String> getUnassignedRepositories() throws SQLException {
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
        
        // Get total repository count
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM repositories")) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalRepos = rs.getInt(1);
                    int assignedRepos = totalRepos - unassigned.size();
                    System.out.println("\n📊 Summary:");
                    System.out.println("  • Total repositories: " + totalRepos);
                    System.out.println("  • Assigned to teams: " + assignedRepos);
                    System.out.println("  • Unassigned: " + unassigned.size());
                    System.out.println("  • Assignment rate: " + String.format("%.1f%%", (assignedRepos * 100.0 / totalRepos)));
                }
            }
        }
    }
    
}
