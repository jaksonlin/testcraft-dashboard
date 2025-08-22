package com.example.annotationextractor.database;

import com.example.annotationextractor.*;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.List;

/**
 * Service for persisting scan results to the database
 */
public class DataPersistenceService {
    
    /**
     * Persist a complete scan session with all its data
     */
    public static long persistScanSession(TestCollectionSummary summary, long scanDurationMs) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Insert scan session
                long scanSessionId = insertScanSession(conn, summary, scanDurationMs);
                
                // Persist repositories and their data
                for (RepositoryTestInfo repo : summary.getRepositories()) {
                    long repositoryId = persistRepository(conn, repo, scanSessionId);
                    persistTestClasses(conn, repo, repositoryId, scanSessionId);
                }
                
                // Update daily metrics
                updateDailyMetrics(conn, summary);
                
                conn.commit();
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
    private static long persistRepository(Connection conn, RepositoryTestInfo repo, long scanSessionId) throws SQLException {
        String sql = "INSERT INTO repositories (repository_name, repository_path, total_test_classes, " +
                     "total_test_methods, total_annotated_methods, annotation_coverage_rate, last_scan_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                     "ON CONFLICT (repository_name, repository_path) DO UPDATE SET " +
                     "total_test_classes = EXCLUDED.total_test_classes, " +
                     "total_test_methods = EXCLUDED.total_test_methods, " +
                     "total_annotated_methods = EXCLUDED.total_annotated_methods, " +
                     "annotation_coverage_rate = EXCLUDED.annotation_coverage_rate, " +
                     "last_scan_date = CURRENT_TIMESTAMP " +
                     "RETURNING id";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, repo.getRepositoryName());
            stmt.setString(2, repo.getRepositoryPath());
            stmt.setInt(3, repo.getTotalTestClasses());
            stmt.setInt(4, repo.getTotalTestMethods());
            stmt.setInt(5, repo.getTotalAnnotatedTestMethods());
            
            double coverageRate = repo.getTotalTestMethods() > 0 ? 
                (double) repo.getTotalAnnotatedTestMethods() / repo.getTotalTestMethods() * 100 : 0.0;
            stmt.setDouble(6, coverageRate);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to get repository ID");
            }
        }
    }
    
    /**
     * Persist test classes for a repository
     */
    private static void persistTestClasses(Connection conn, RepositoryTestInfo repo, long repositoryId, long scanSessionId) throws SQLException {
        for (TestClassInfo testClass : repo.getTestClasses()) {
            long testClassId = insertTestClass(conn, testClass, repositoryId, scanSessionId);
            persistTestMethods(conn, testClass, testClassId, scanSessionId);
        }
    }
    
    /**
     * Insert test class record
     */
    private static long insertTestClass(Connection conn, TestClassInfo testClass, long repositoryId, long scanSessionId) throws SQLException {
        String sql = "INSERT INTO test_classes (repository_id, class_name, package_name, file_path, " +
                     "total_test_methods, annotated_test_methods, coverage_rate, scan_session_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (repository_id, class_name, package_name) DO UPDATE SET " +
                     "total_test_methods = EXCLUDED.total_test_methods, " +
                     "annotated_test_methods = EXCLUDED.annotated_test_methods, " +
                     "coverage_rate = EXCLUDED.coverage_rate, " +
                     "last_modified_date = CURRENT_TIMESTAMP " +
                     "RETURNING id";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, repositoryId);
            stmt.setString(2, testClass.getClassName());
            stmt.setString(3, testClass.getPackageName());
            stmt.setString(4, testClass.getFilePath());
            stmt.setInt(5, testClass.getTotalTestMethods());
            stmt.setInt(6, testClass.getAnnotatedTestMethods());
            
            double coverageRate = testClass.getTotalTestMethods() > 0 ? 
                (double) testClass.getAnnotatedTestMethods() / testClass.getTotalTestMethods() * 100 : 0.0;
            stmt.setDouble(7, coverageRate);
            stmt.setLong(8, scanSessionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to get test class ID");
            }
        }
    }
    
    /**
     * Persist test methods for a test class
     */
    private static void persistTestMethods(Connection conn, TestClassInfo testClass, long testClassId, long scanSessionId) throws SQLException {
        for (TestMethodInfo method : testClass.getTestMethods()) {
            insertTestMethod(conn, method, testClassId, scanSessionId);
        }
    }
    
    /**
     * Insert test method record
     */
    private static void insertTestMethod(Connection conn, TestMethodInfo method, long testClassId, long scanSessionId) throws SQLException {
        String sql = "INSERT INTO test_methods (test_class_id, method_name, line_number, has_annotation, " +
                     "annotation_data, annotation_title, annotation_author, annotation_status, " +
                     "annotation_target_class, annotation_target_method, annotation_description, " +
                     "annotation_tags, annotation_test_points, annotation_requirements, scan_session_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
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
                     "last_modified_date = CURRENT_TIMESTAMP";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, testClassId);
            stmt.setString(2, method.getMethodName());
            stmt.setInt(3, method.getLineNumber());
            
            UnittestCaseInfoData annotationData = method.getAnnotationData();
            boolean hasAnnotation = annotationData != null && !annotationData.getTitle().isEmpty();
            stmt.setBoolean(4, hasAnnotation);
            
            // Set annotation data as JSONB
            if (hasAnnotation) {
                PGobject jsonObject = new PGobject();
                jsonObject.setType("jsonb");
                jsonObject.setValue(convertToJson(annotationData));
                stmt.setObject(5, jsonObject);
                
                stmt.setString(6, annotationData.getTitle());
                stmt.setString(7, annotationData.getAuthor());
                stmt.setString(8, annotationData.getStatus());
                stmt.setString(9, annotationData.getTargetClass());
                stmt.setString(10, annotationData.getTargetMethod());
                stmt.setString(11, annotationData.getDescription());
                
                // Convert arrays to PostgreSQL arrays
                stmt.setArray(12, conn.createArrayOf("text", annotationData.getTags()));
                stmt.setArray(13, conn.createArrayOf("text", annotationData.getTestPoints()));
                stmt.setArray(14, conn.createArrayOf("text", annotationData.getRelatedRequirements()));
            } else {
                stmt.setObject(5, null);
                stmt.setString(6, null);
                stmt.setString(7, null);
                stmt.setString(8, null);
                stmt.setString(9, null);
                stmt.setString(10, null);
                stmt.setString(11, null);
                stmt.setArray(12, null);
                stmt.setArray(13, null);
                stmt.setArray(14, null);
            }
            
            stmt.setLong(15, scanSessionId);
            stmt.executeUpdate();
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
        json.append("\"title\":\"").append(escapeJson(data.getTitle())).append("\",");
        json.append("\"author\":\"").append(escapeJson(data.getAuthor())).append("\",");
        json.append("\"status\":\"").append(escapeJson(data.getStatus())).append("\",");
        json.append("\"targetClass\":\"").append(escapeJson(data.getTargetClass())).append("\",");
        json.append("\"targetMethod\":\"").append(escapeJson(data.getTargetMethod())).append("\",");
        json.append("\"description\":\"").append(escapeJson(data.getDescription())).append("\"");
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
}
