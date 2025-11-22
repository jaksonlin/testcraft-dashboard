package com.example.annotationextractor.application;

import com.example.annotationextractor.casemodel.RepositoryTestInfo;
import com.example.annotationextractor.casemodel.TestClassInfo;
import com.example.annotationextractor.casemodel.TestCollectionSummary;
import com.example.annotationextractor.casemodel.TestMethodInfo;
import com.example.annotationextractor.casemodel.TestHelperClassInfo;
import com.example.annotationextractor.casemodel.UnittestCaseInfoData;
import com.example.annotationextractor.database.DatabaseConfig;

import java.io.StringReader;
import java.sql.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * JDBC-based write use-case that persists a scan session and all associated data.
 * Pure Java, no Spring dependency.
 */
public class PersistScanResultsUseCase {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public long persist(TestCollectionSummary summary, long scanDurationMs) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long scanSessionId = insertScanSession(conn, summary, scanDurationMs);

                for (RepositoryTestInfo repo : summary.getRepositories()) {
                    int teamId = ensureTeamExists(conn, repo.getTeamName(), repo.getTeamCode());
                    long repositoryId = upsertRepository(conn, repo, teamId);
                    Map<String, Long> testClassIds = persistTestClassesBatch(conn, repo, repositoryId, scanSessionId);
                    persistTestMethodsBatch(conn, repo, repositoryId, scanSessionId, testClassIds);
                    persistHelperClassesBatch(conn, repo, repositoryId, scanSessionId);
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
                    Map<String, Long> testClassIds = persistTestClassesBatch(conn, repo, repositoryId, scanSessionId);
                    persistTestMethodsBatch(conn, repo, repositoryId, scanSessionId, testClassIds);
                    persistHelperClassesBatch(conn, repo, repositoryId, scanSessionId);
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

    /**
     * Update the report file path for a specific scan session
     */
    public static void updateReportFilePath(long scanSessionId, String reportFilePath) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE scan_sessions SET report_file_path = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, reportFilePath);
                stmt.setLong(2, scanSessionId);
                stmt.executeUpdate();
            }
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
            String ins = "INSERT INTO repositories (repository_name, repository_path, git_url, team_id, total_test_classes, total_test_methods, total_annotated_methods, annotation_coverage_rate, test_code_lines, test_related_code_lines, last_scan_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, repo.getRepositoryName());
                stmt.setString(2, repo.getRepositoryPathString());
                stmt.setString(3, repo.getGitUrl());
                stmt.setInt(4, teamId);
                stmt.setInt(5, repo.getTotalTestClasses());
                stmt.setInt(6, repo.getTotalTestMethods());
                stmt.setInt(7, repo.getTotalAnnotatedTestMethods());
                stmt.setDouble(8, coverageRate);
                stmt.setInt(9, repo.getTestCodeLines());
                stmt.setInt(10, repo.getTestRelatedCodeLines());
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
            String upd = "UPDATE repositories SET repository_name = ?, repository_path = ?, team_id = ?, total_test_classes = ?, total_test_methods = ?, total_annotated_methods = ?, annotation_coverage_rate = ?, test_code_lines = ?, test_related_code_lines = ?, last_scan_date = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(upd)) {
                stmt.setString(1, repo.getRepositoryName());
                stmt.setString(2, repo.getRepositoryPathString());
                stmt.setInt(3, teamId);
                stmt.setInt(4, repo.getTotalTestClasses());
                stmt.setInt(5, repo.getTotalTestMethods());
                stmt.setInt(6, repo.getTotalAnnotatedTestMethods());
                stmt.setDouble(7, coverageRate);
                stmt.setInt(8, repo.getTestCodeLines());
                stmt.setInt(9, repo.getTestRelatedCodeLines());
                stmt.setLong(10, existingId);
                stmt.executeUpdate();
            }
            return existingId;
        }
    }

    private Map<String, Long> persistTestClassesBatch(Connection conn, RepositoryTestInfo repo, long repositoryId, long scanSessionId) throws SQLException {
        Map<String, Long> testClassIds = new HashMap<>();
        if (repo.getTestClasses().isEmpty()) return testClassIds;
        for (TestClassInfo tc : repo.getTestClasses()) {
            double coverage = tc.getTotalTestMethods() > 0 ? (double) tc.getAnnotatedTestMethods() / tc.getTotalTestMethods() * 100 : 0.0;
            String importedTypesPayload = toDelimitedString(tc.getImportedTypes());
            String referencedTypesPayload = toDelimitedString(tc.getReferencedTypes());
            String updateSql = "UPDATE test_classes SET file_path = ?, total_test_methods = ?, annotated_test_methods = ?, coverage_rate = ?, class_line_number = ?, test_class_content = ?, helper_classes_line_numbers = ?, class_loc = ?, imported_types = ?, referenced_types = ? "
                    + "WHERE scan_session_id = ? AND repository_id = ? AND class_name = ? AND package_name = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, tc.getFilePath());
                updateStmt.setInt(2, tc.getTotalTestMethods());
                updateStmt.setInt(3, tc.getAnnotatedTestMethods());
                updateStmt.setDouble(4, coverage);
                updateStmt.setInt(5, tc.getClassLineNumber());
                updateStmt.setString(6, tc.getTestClassContent());
                updateStmt.setString(7, tc.getHelperClassesLineNumbers());
                updateStmt.setInt(8, tc.getClassLoc());
                setLargeString(updateStmt, 9, importedTypesPayload);
                setLargeString(updateStmt, 10, referencedTypesPayload);
                updateStmt.setLong(11, scanSessionId);
                updateStmt.setLong(12, repositoryId);
                updateStmt.setString(13, tc.getClassName());
                updateStmt.setString(14, tc.getPackageName());
                int updated = updateStmt.executeUpdate();
                if (updated > 0) {
                    long classId = getTestClassId(conn, repositoryId, tc.getClassName(), tc.getPackageName(), scanSessionId);
                    testClassIds.put(buildTestClassKey(tc.getPackageName(), tc.getClassName()), classId);
                    continue;
                }
            }

            String insertSql = "INSERT INTO test_classes (repository_id, class_name, package_name, file_path, total_test_methods, annotated_test_methods, coverage_rate, scan_session_id, class_line_number, test_class_content, helper_classes_line_numbers, class_loc, imported_types, referenced_types) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ins = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ins.setLong(1, repositoryId);
                ins.setString(2, tc.getClassName());
                ins.setString(3, tc.getPackageName());
                ins.setString(4, tc.getFilePath());
                ins.setInt(5, tc.getTotalTestMethods());
                ins.setInt(6, tc.getAnnotatedTestMethods());
                ins.setDouble(7, coverage);
                ins.setLong(8, scanSessionId);
                ins.setInt(9, tc.getClassLineNumber());
                ins.setString(10, tc.getTestClassContent());
                ins.setString(11, tc.getHelperClassesLineNumbers());
                ins.setInt(12, tc.getClassLoc());
                setLargeString(ins, 13, importedTypesPayload);
                setLargeString(ins, 14, referencedTypesPayload);
                ins.executeUpdate();
                try (ResultSet rs = ins.getGeneratedKeys()) {
                    if (rs.next()) {
                        long classId = rs.getLong(1);
                        testClassIds.put(buildTestClassKey(tc.getPackageName(), tc.getClassName()), classId);
                    } else {
                        long classId = getTestClassId(conn, repositoryId, tc.getClassName(), tc.getPackageName(), scanSessionId);
                        testClassIds.put(buildTestClassKey(tc.getPackageName(), tc.getClassName()), classId);
                    }
                }
            }
        }
        return testClassIds;
    }

    private void persistTestMethodsBatch(Connection conn, RepositoryTestInfo repo, long repositoryId, long scanSessionId, Map<String, Long> testClassIds) throws SQLException {
        java.util.List<TestMethodInfo> allMethods = new java.util.ArrayList<>();
        for (TestClassInfo tc : repo.getTestClasses()) {
            allMethods.addAll(tc.getTestMethods());
        }
        if (allMethods.isEmpty()) return;

        for (TestMethodInfo method : allMethods) {
            String key = buildTestClassKey(method.getPackageName(), method.getClassName());
            Long testClassId = testClassIds.get(key);
            if (testClassId == null) {
                testClassId = getTestClassId(conn, repositoryId, method.getClassName(), method.getPackageName(), scanSessionId);
                testClassIds.put(key, testClassId);
            }
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
            String updateSql = "UPDATE test_methods SET method_signature = ?, line_number = ?, method_loc = ?, method_body_content = ?, has_annotation = ?, annotation_data = ?, annotation_title = ?, annotation_author = ?, annotation_status = ?, annotation_target_class = ?, annotation_target_method = ?, annotation_description = ?, annotation_tags = ?, annotation_test_points = ?, annotation_requirements = ?, annotation_defects = ?, annotation_testcases = ?, annotation_last_update_time = ?, annotation_last_update_author = ? WHERE scan_session_id = ? AND test_class_id = ? AND method_name = ? AND method_signature IS NOT DISTINCT FROM ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, methodSignature);
                updateStmt.setInt(2, method.getLineNumber());
                updateStmt.setInt(3, method.getMethodLoc());
                updateStmt.setString(4, method.getMethodBodyContent() != null && !method.getMethodBodyContent().isEmpty() ? method.getMethodBodyContent() : null);
                updateStmt.setBoolean(5, has);
                updateStmt.setString(6, annotationJson);
                updateStmt.setString(7, has ? title : null);
                updateStmt.setString(8, has ? author : null);
                updateStmt.setString(9, has ? status : null);
                updateStmt.setString(10, has ? targetClass : null);
                updateStmt.setString(11, has ? targetMethod : null);
                updateStmt.setString(12, has ? description : null);
                updateStmt.setString(13, has ? tags : null);
                updateStmt.setString(14, has ? testPoints : null);
                updateStmt.setString(15, has ? requirements : null);
                updateStmt.setString(16, has ? defects : null);
                updateStmt.setString(17, has ? testcases : null);
                updateStmt.setString(18, has ? lastUpdateTime : null);
                updateStmt.setString(19, has ? lastUpdateAuthor : null);
                updateStmt.setLong(20, scanSessionId);
                updateStmt.setLong(21, testClassId);
                updateStmt.setString(22, method.getMethodName());
                updateStmt.setString(23, methodSignature);
                int updated = updateStmt.executeUpdate();
                if (updated == 0) {
                    String insertSql = "INSERT INTO test_methods (test_class_id, method_name, method_signature, line_number, method_loc, method_body_content, has_annotation, annotation_data, annotation_title, annotation_author, annotation_status, annotation_target_class, annotation_target_method, annotation_description, annotation_tags, annotation_test_points, annotation_requirements, annotation_defects, annotation_testcases, annotation_last_update_time, annotation_last_update_author, scan_session_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setLong(1, testClassId);
                        insertStmt.setString(2, method.getMethodName());
                        insertStmt.setString(3, methodSignature);
                        insertStmt.setInt(4, method.getLineNumber());
                        insertStmt.setInt(5, method.getMethodLoc());
                        insertStmt.setString(6, method.getMethodBodyContent() != null && !method.getMethodBodyContent().isEmpty() ? method.getMethodBodyContent() : null);
                        insertStmt.setBoolean(7, has);
                        insertStmt.setString(8, annotationJson);
                        insertStmt.setString(9, has ? title : null);
                        insertStmt.setString(10, has ? author : null);
                        insertStmt.setString(11, has ? status : null);
                        insertStmt.setString(12, has ? targetClass : null);
                        insertStmt.setString(13, has ? targetMethod : null);
                        insertStmt.setString(14, has ? description : null);
                        insertStmt.setString(15, has ? tags : null);
                        insertStmt.setString(16, has ? testPoints : null);
                        insertStmt.setString(17, has ? requirements : null);
                        insertStmt.setString(18, has ? defects : null);
                        insertStmt.setString(19, has ? testcases : null);
                        insertStmt.setString(20, has ? lastUpdateTime : null);
                        insertStmt.setString(21, has ? lastUpdateAuthor : null);
                        insertStmt.setLong(22, scanSessionId);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }

    private void persistHelperClassesBatch(Connection conn, RepositoryTestInfo repo, long repositoryId, long scanSessionId) throws SQLException {
        if (repo.getHelperClasses() == null || repo.getHelperClasses().isEmpty()) return;
        for (TestHelperClassInfo helperClass : repo.getHelperClasses()) {
            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO test_helper_classes (repository_id, class_name, package_name, file_path, class_line_number, helper_class_content, loc, scan_session_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ins.setLong(1, repositoryId);
                ins.setString(2, helperClass.getClassName());
                ins.setString(3, helperClass.getPackageName());
                ins.setString(4, helperClass.getFilePath());
                ins.setObject(5, helperClass.getClassLineNumber(), java.sql.Types.INTEGER);
                ins.setString(6, helperClass.getHelperClassContent());
                ins.setInt(7, helperClass.getLoc());
                ins.setLong(8, scanSessionId);
                ins.executeUpdate();
                try (ResultSet k = ins.getGeneratedKeys()) { if (k.next()) { /* id available if needed */ } }
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

    private static String buildTestClassKey(String packageName, String className) {
        return (packageName != null ? packageName : "") + "#" + className;
    }

    private void updateDailyMetrics(Connection conn, TestCollectionSummary summary) throws SQLException {
        double overallCoverage = summary.getTotalTestMethods() > 0 ? (double) summary.getTotalAnnotatedTestMethods() / summary.getTotalTestMethods() * 100 : 0.0;
        String updateSql = "UPDATE daily_metrics SET total_repositories = ?, total_test_classes = ?, total_test_methods = ?, total_annotated_methods = ?, overall_coverage_rate = ? WHERE metric_date = CURRENT_DATE";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setInt(1, summary.getTotalRepositories());
            updateStmt.setInt(2, summary.getTotalTestClasses());
            updateStmt.setInt(3, summary.getTotalTestMethods());
            updateStmt.setInt(4, summary.getTotalAnnotatedTestMethods());
            updateStmt.setDouble(5, overallCoverage);
            int updated = updateStmt.executeUpdate();
            if (updated > 0) {
                return;
            }
        }

        String insertSql = "INSERT INTO daily_metrics (metric_date, total_repositories, total_test_classes, total_test_methods, total_annotated_methods, overall_coverage_rate) VALUES (CURRENT_DATE, ?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, summary.getTotalRepositories());
            insertStmt.setInt(2, summary.getTotalTestClasses());
            insertStmt.setInt(3, summary.getTotalTestMethods());
            insertStmt.setInt(4, summary.getTotalAnnotatedTestMethods());
            insertStmt.setDouble(5, overallCoverage);
            insertStmt.executeUpdate();
        }
    }

    // Jackson handles JSON serialization; no manual JSON helpers needed

    private static void setLargeString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null || value.isEmpty()) {
            stmt.setNull(index, Types.CLOB);
            return;
        }
        stmt.setCharacterStream(index, new StringReader(value), value.length());
    }

    private static String toDelimitedString(java.util.List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join("\n", values);
    }

    private static String arrayToString(String[] array) {
        if (array == null) return null;
        if (array.length == 0) return "";
        return String.join(";", array);
    }
}


