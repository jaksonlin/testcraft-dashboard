package com.example.annotationextractor.application;

import com.example.annotationextractor.casemodel.RepositoryTestInfo;
import com.example.annotationextractor.casemodel.TestClassInfo;
import com.example.annotationextractor.casemodel.TestCollectionSummary;
import com.example.annotationextractor.casemodel.TestMethodInfo;
import com.example.annotationextractor.casemodel.UnittestCaseInfoData;
import com.example.annotationextractor.database.DatabaseConfig;

import java.sql.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JDBC-based write use-case that persists a scan session and all associated data.
 * Pure Java, no Spring dependency.
 */
public class PersistScanResultsUseCase {

    private static final int BATCH_SIZE = 1000;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public long persist(TestCollectionSummary summary, long scanDurationMs) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long scanSessionId = insertScanSession(conn, summary, scanDurationMs);

                for (RepositoryTestInfo repo : summary.getRepositories()) {
                    int teamId = ensureTeamExists(conn, repo.getTeamName(), repo.getTeamCode());
                    long repositoryId = upsertRepository(conn, repo, teamId);
                    persistTestClassesBatch(conn, repo, repositoryId, scanSessionId);
                    persistTestMethodsBatch(conn, repo, repositoryId, scanSessionId);
                }

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
     * Shadow persist that targets the shadow database if configured.
     * It commits like the primary to allow real performance testing.
     */
    public long persistToShadow(TestCollectionSummary summary, long scanDurationMs) throws SQLException {
        try (Connection conn = DatabaseConfig.getShadowConnection()) {
            conn.setAutoCommit(false);
            try {
                long scanSessionId = insertScanSession(conn, summary, scanDurationMs);
                for (RepositoryTestInfo repo : summary.getRepositories()) {
                    int teamId = ensureTeamExists(conn, repo.getTeamName(), repo.getTeamCode());
                    long repositoryId = upsertRepository(conn, repo, teamId);
                    persistTestClassesBatch(conn, repo, repositoryId, scanSessionId);
                    persistTestMethodsBatch(conn, repo, repositoryId, scanSessionId);
                }
                updateDailyMetrics(conn, summary);
                conn.commit();
                return scanSessionId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private long insertScanSession(Connection conn, TestCollectionSummary summary, long scanDurationMs) throws SQLException {
        String sql = "INSERT INTO scan_sessions (scan_date, scan_directory, total_repositories, total_test_classes, total_test_methods, total_annotated_methods, scan_duration_ms) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, new Timestamp(summary.getScanTimestamp()));
            stmt.setString(2, summary.getScanDirectory());
            stmt.setInt(3, summary.getTotalRepositories());
            stmt.setInt(4, summary.getTotalTestClasses());
            stmt.setInt(5, summary.getTotalTestMethods());
            stmt.setInt(6, summary.getTotalAnnotatedTestMethods());
            stmt.setLong(7, scanDurationMs);
            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("No rows inserted for scan_sessions");
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            // Fallback if driver doesn't support generated keys: query last inserted via max(id) with matching scan_directory & timestamp
            try (PreparedStatement sel = conn.prepareStatement("SELECT id FROM scan_sessions WHERE scan_directory = ? AND scan_date = ? ORDER BY id DESC")) {
                sel.setString(1, summary.getScanDirectory());
                sel.setTimestamp(2, new Timestamp(summary.getScanTimestamp()));
                try (ResultSet rs = sel.executeQuery()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
            throw new SQLException("Failed to retrieve scan session ID");
        }
    }

    private int ensureTeamExists(Connection conn, String teamName, String teamCode) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM teams WHERE team_code = ?")) {
            stmt.setString(1, teamCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO teams (team_name, team_code) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, teamName);
            stmt.setString(2, teamCode);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create team: " + teamName + " (" + teamCode + ")");
    }

    private long upsertRepository(Connection conn, RepositoryTestInfo repo, int teamId) throws SQLException {
        Long existingId = null;
        try (PreparedStatement sel = conn.prepareStatement("SELECT id FROM repositories WHERE git_url = ?")) {
            sel.setString(1, repo.getGitUrl());
            try (ResultSet rs = sel.executeQuery()) {
                if (rs.next()) existingId = rs.getLong(1);
            }
        }
        double coverageRate = repo.getTotalTestMethods() > 0 ? (double) repo.getTotalAnnotatedTestMethods() / repo.getTotalTestMethods() * 100 : 0.0;
        if (existingId == null) {
            String ins = "INSERT INTO repositories (repository_name, repository_path, git_url, team_id, total_test_classes, total_test_methods, total_annotated_methods, annotation_coverage_rate, last_scan_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, repo.getRepositoryName());
                stmt.setString(2, repo.getRepositoryPathString());
                stmt.setString(3, repo.getGitUrl());
                stmt.setInt(4, teamId);
                stmt.setInt(5, repo.getTotalTestClasses());
                stmt.setInt(6, repo.getTotalTestMethods());
                stmt.setInt(7, repo.getTotalAnnotatedTestMethods());
                stmt.setDouble(8, coverageRate);
                int affected = stmt.executeUpdate();
                if (affected == 0) throw new SQLException("No rows inserted for repositories");
                try (ResultSet keys = stmt.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
            }
            // Fallback
            try (PreparedStatement sel2 = conn.prepareStatement("SELECT id FROM repositories WHERE git_url = ?")) {
                sel2.setString(1, repo.getGitUrl());
                try (ResultSet rs = sel2.executeQuery()) { if (rs.next()) return rs.getLong(1); }
            }
            throw new SQLException("Failed to get repository ID after insert");
        } else {
            String upd = "UPDATE repositories SET repository_name = ?, repository_path = ?, team_id = ?, total_test_classes = ?, total_test_methods = ?, total_annotated_methods = ?, annotation_coverage_rate = ?, last_scan_date = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(upd)) {
                stmt.setString(1, repo.getRepositoryName());
                stmt.setString(2, repo.getRepositoryPathString());
                stmt.setInt(3, teamId);
                stmt.setInt(4, repo.getTotalTestClasses());
                stmt.setInt(5, repo.getTotalTestMethods());
                stmt.setInt(6, repo.getTotalAnnotatedTestMethods());
                stmt.setDouble(7, coverageRate);
                stmt.setLong(8, existingId);
                stmt.executeUpdate();
            }
            return existingId;
        }
    }

    private void persistTestClassesBatch(Connection conn, RepositoryTestInfo repo, long repositoryId, long scanSessionId) throws SQLException {
        if (repo.getTestClasses().isEmpty()) return;
        for (TestClassInfo tc : repo.getTestClasses()) {
            double coverage = tc.getTotalTestMethods() > 0 ? (double) tc.getAnnotatedTestMethods() / tc.getTotalTestMethods() * 100 : 0.0;
            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO test_classes (repository_id, class_name, package_name, file_path, total_test_methods, annotated_test_methods, coverage_rate, scan_session_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ins.setLong(1, repositoryId);
                ins.setString(2, tc.getClassName());
                ins.setString(3, tc.getPackageName());
                ins.setString(4, tc.getFilePath());
                ins.setInt(5, tc.getTotalTestMethods());
                ins.setInt(6, tc.getAnnotatedTestMethods());
                ins.setDouble(7, coverage);
                ins.setLong(8, scanSessionId);
                ins.executeUpdate();
                try (ResultSet k = ins.getGeneratedKeys()) { if (k.next()) { /* id available if needed */ } }
            }
        }
    }

    private void persistTestMethodsBatch(Connection conn, RepositoryTestInfo repo, long repositoryId, long scanSessionId) throws SQLException {
        java.util.List<TestMethodInfo> allMethods = new java.util.ArrayList<>();
        for (TestClassInfo tc : repo.getTestClasses()) {
            allMethods.addAll(tc.getTestMethods());
        }
        if (allMethods.isEmpty()) return;

        for (TestMethodInfo method : allMethods) {
            long testClassId = getTestClassId(conn, repositoryId, method.getClassName(), method.getPackageName(), scanSessionId);
            UnittestCaseInfoData data = method.getAnnotationData();
            String methodSignature = (data != null) ? data.getMethodSignature() : null;

            String title = (data != null) ? data.getTitle() : null;
            boolean has = title != null && !title.isEmpty();
            String annotationJson = null;
            String author = null;
            String status = null;
            String targetClass = null;
            String targetMethod = null;
            String description = null;
            String tags = null;
            String testPoints = null;
            String requirements = null;
            String defects = null;
            String testcases = null;
            String lastUpdateTime = null;
            String lastUpdateAuthor = null;
            if (has && data != null) {
                try {
                    annotationJson = objectMapper.writeValueAsString(data);
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new SQLException("Failed to serialize annotation JSON", e);
                }
                author = data.getAuthor();
                status = data.getStatus();
                targetClass = data.getTargetClass();
                targetMethod = data.getTargetMethod();
                description = data.getDescription();
                tags = arrayToString(data.getTags());
                testPoints = arrayToString(data.getTestPoints());
                requirements = arrayToString(data.getRelatedRequirements());
                defects = arrayToString(data.getRelatedDefects());
                testcases = arrayToString(data.getRelatedTestcases());
                lastUpdateTime = data.getLastUpdateTime();
                lastUpdateAuthor = data.getLastUpdateAuthor();
            }
            String ins = "INSERT INTO test_methods (test_class_id, method_name, method_signature, line_number, has_annotation, annotation_data, annotation_title, annotation_author, annotation_status, annotation_target_class, annotation_target_method, annotation_description, annotation_tags, annotation_test_points, annotation_requirements, annotation_defects, annotation_testcases, annotation_last_update_time, annotation_last_update_author, scan_session_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(ins)) {
                stmt.setLong(1, testClassId);
                stmt.setString(2, method.getMethodName());
                stmt.setString(3, methodSignature);
                stmt.setInt(4, method.getLineNumber());
                stmt.setBoolean(5, has);
                stmt.setString(6, annotationJson);
                stmt.setString(7, has ? title : null);
                stmt.setString(8, has ? author : null);
                stmt.setString(9, has ? status : null);
                stmt.setString(10, has ? targetClass : null);
                stmt.setString(11, has ? targetMethod : null);
                stmt.setString(12, has ? description : null);
                stmt.setString(13, has ? tags : null);
                stmt.setString(14, has ? testPoints : null);
                stmt.setString(15, has ? requirements : null);
                stmt.setString(16, has ? defects : null);
                stmt.setString(17, has ? testcases : null);
                stmt.setString(18, has ? lastUpdateTime : null);
                stmt.setString(19, has ? lastUpdateAuthor : null);
                stmt.setLong(20, scanSessionId);
                stmt.executeUpdate();
            }
        }
    }

    private long getTestClassId(Connection conn, long repositoryId, String className, String packageName, long scanSessionId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM test_classes WHERE repository_id = ? AND class_name = ? AND package_name = ? AND scan_session_id = ? ORDER BY id DESC")) {
            stmt.setLong(1, repositoryId);
            stmt.setString(2, className);
            stmt.setString(3, packageName);
            stmt.setLong(4, scanSessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Test class not found for session: " + scanSessionId + ", " + packageName + "." + className);
    }

    private void updateDailyMetrics(Connection conn, TestCollectionSummary summary) throws SQLException {
        String sql = "INSERT INTO daily_metrics (metric_date, total_repositories, total_test_classes, total_test_methods, total_annotated_methods, overall_coverage_rate) VALUES (CURRENT_DATE, ?, ?, ?, ?, ?) ON CONFLICT (metric_date) DO UPDATE SET total_repositories = EXCLUDED.total_repositories, total_test_classes = EXCLUDED.total_test_classes, total_test_methods = EXCLUDED.total_test_methods, total_annotated_methods = EXCLUDED.total_annotated_methods, overall_coverage_rate = EXCLUDED.overall_coverage_rate";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, summary.getTotalRepositories());
            stmt.setInt(2, summary.getTotalTestClasses());
            stmt.setInt(3, summary.getTotalTestMethods());
            stmt.setInt(4, summary.getTotalAnnotatedTestMethods());
            double overallCoverage = summary.getTotalTestMethods() > 0 ? (double) summary.getTotalAnnotatedTestMethods() / summary.getTotalTestMethods() * 100 : 0.0;
            stmt.setDouble(5, overallCoverage);
            stmt.executeUpdate();
        }
    }

    // Jackson handles JSON serialization; no manual JSON helpers needed

    private static String arrayToString(String[] array) {
        if (array == null) return null;
        if (array.length == 0) return "";
        return String.join(";", array);
    }
}


