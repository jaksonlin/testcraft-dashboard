package com.example.annotationextractor.application;

import com.example.annotationextractor.casemodel.RepositoryTestInfo;
import com.example.annotationextractor.casemodel.TestClassInfo;
import com.example.annotationextractor.casemodel.TestCollectionSummary;
import com.example.annotationextractor.casemodel.TestMethodInfo;
import com.example.annotationextractor.casemodel.TestHelperClassInfo;
import com.example.annotationextractor.casemodel.UnittestCaseInfoData;
import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.database.BatchOperationHelper;

import java.io.StringReader;
import java.sql.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * JDBC-based write use-case that persists a scan session and all associated data.
 * Pure Java, no Spring dependency.
 */
public class PersistScanResultsUseCase {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Batch size for batch operations - optimal balance between performance and memory
    private static final int BATCH_SIZE = 500;
    
    // Helper class to hold prepared test class data for batch operations
    private static class TestClassBatchData {
        final TestClassInfo tc;
        final double coverage;
        final String importedTypes;
        final String referencedTypes;
        
        TestClassBatchData(TestClassInfo tc, double coverage, String importedTypes, String referencedTypes) {
            this.tc = tc;
            this.coverage = coverage;
            this.importedTypes = importedTypes;
            this.referencedTypes = referencedTypes;
        }
    }
    
    // Helper class to hold prepared test method data for batch operations
    private static class TestMethodBatchData {
        final TestMethodInfo method;
        final Long testClassId;
        final String methodSignature;
        final boolean hasAnnotation;
        final String annotationJson;
        final String title;
        final String author;
        final String status;
        final String targetClass;
        final String targetMethod;
        final String description;
        final String tags;
        final String testPoints;
        final String requirements;
        final String defects;
        final String testcases;
        final String lastUpdateTime;
        final String lastUpdateAuthor;
        
        TestMethodBatchData(TestMethodInfo method, Long testClassId, String methodSignature,
                boolean hasAnnotation, String annotationJson, String title, String author,
                String status, String targetClass, String targetMethod, String description,
                String tags, String testPoints, String requirements, String defects,
                String testcases, String lastUpdateTime, String lastUpdateAuthor) {
            this.method = method;
            this.testClassId = testClassId;
            this.methodSignature = methodSignature;
            this.hasAnnotation = hasAnnotation;
            this.annotationJson = annotationJson;
            this.title = title;
            this.author = author;
            this.status = status;
            this.targetClass = targetClass;
            this.targetMethod = targetMethod;
            this.description = description;
            this.tags = tags;
            this.testPoints = testPoints;
            this.requirements = requirements;
            this.defects = defects;
            this.testcases = testcases;
            this.lastUpdateTime = lastUpdateTime;
            this.lastUpdateAuthor = lastUpdateAuthor;
        }
    }

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
        String sql = """
                INSERT INTO scan_sessions 
                (scan_date, scan_directory, total_repositories, total_test_classes, 
                 total_test_methods, total_annotated_methods, scan_duration_ms) 
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
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
            String fallbackSql = """
                    SELECT id FROM scan_sessions 
                    WHERE scan_directory = ? AND scan_date = ? 
                    ORDER BY id DESC 
                    LIMIT 1
                    """;
            try (PreparedStatement sel = conn.prepareStatement(fallbackSql)) {
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
            String sql = """
                    UPDATE scan_sessions 
                    SET report_file_path = ? 
                    WHERE id = ?
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, reportFilePath);
                stmt.setLong(2, scanSessionId);
                stmt.executeUpdate();
            }
        }
    }

    private int ensureTeamExists(Connection conn, String teamName, String teamCode) throws SQLException {
        String selectSql = "SELECT id FROM teams WHERE team_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, teamCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        String insertSql = """
                INSERT INTO teams (team_name, team_code) 
                VALUES (?, ?)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
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
        Long existingId = getRepositoryIdByGitUrl(conn, repo.getGitUrl());
        double coverageRate = repo.getTotalTestMethods() > 0 
            ? (double) repo.getTotalAnnotatedTestMethods() / repo.getTotalTestMethods() * 100 
            : 0.0;
            
        if (existingId == null) {
            String insertSql = """
                    INSERT INTO repositories 
                    (repository_name, repository_path, git_url, team_id, total_test_classes, 
                     total_test_methods, total_annotated_methods, annotation_coverage_rate, 
                     test_code_lines, test_related_code_lines, last_scan_date) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
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
                try (ResultSet keys = stmt.getGeneratedKeys()) { 
                    if (keys.next()) return keys.getLong(1); 
                }
            }
            // Fallback if driver doesn't support generated keys
            existingId = getRepositoryIdByGitUrl(conn, repo.getGitUrl());
            if (existingId != null) {
                return existingId;
            }
            throw new SQLException("Failed to get repository ID after insert");
        } else {
            String updateSql = """
                    UPDATE repositories 
                    SET repository_name = ?, 
                        repository_path = ?, 
                        team_id = ?, 
                        total_test_classes = ?, 
                        total_test_methods = ?, 
                        total_annotated_methods = ?, 
                        annotation_coverage_rate = ?, 
                        test_code_lines = ?, 
                        test_related_code_lines = ?, 
                        last_scan_date = CURRENT_TIMESTAMP 
                    WHERE id = ?
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
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
    
    /**
     * Get repository ID by git URL. Returns null if not found.
     */
    private Long getRepositoryIdByGitUrl(Connection conn, String gitUrl) throws SQLException {
        String sql = "SELECT id FROM repositories WHERE git_url = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, gitUrl);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    private Map<String, Long> persistTestClassesBatch(Connection conn, RepositoryTestInfo repo, long repositoryId, long scanSessionId) throws SQLException {
        Map<String, Long> testClassIds = new HashMap<>();
        List<TestClassInfo> testClasses = repo.getTestClasses();
        if (testClasses.isEmpty()) return testClassIds;

        // Prepare all data upfront for batch operations
        List<TestClassBatchData> batchData = new ArrayList<>();
        for (TestClassInfo tc : testClasses) {
            double coverage = tc.getTotalTestMethods() > 0 ? (double) tc.getAnnotatedTestMethods() / tc.getTotalTestMethods() * 100 : 0.0;
            String importedTypesPayload = toDelimitedString(tc.getImportedTypes());
            String referencedTypesPayload = toDelimitedString(tc.getReferencedTypes());
            batchData.add(new TestClassBatchData(tc, coverage, importedTypesPayload, referencedTypesPayload));
        }

        // Step 1: Try UPDATE in batch
        String updateSql = """
                UPDATE test_classes 
                SET file_path = ?, total_test_methods = ?, annotated_test_methods = ?,
                    coverage_rate = ?, class_line_number = ?, test_class_content = ?,
                    helper_classes_line_numbers = ?, class_loc = ?, imported_types = ?,
                    referenced_types = ?
                WHERE scan_session_id = ? AND repository_id = ? 
                  AND class_name = ? AND package_name = ?
                """;
        
        List<TestClassBatchData> toInsert = new ArrayList<>();
        
        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            for (TestClassBatchData data : batchData) {
                TestClassInfo tc = data.tc;
                updateStmt.setString(1, tc.getFilePath());
                updateStmt.setInt(2, tc.getTotalTestMethods());
                updateStmt.setInt(3, tc.getAnnotatedTestMethods());
                updateStmt.setDouble(4, data.coverage);
                updateStmt.setInt(5, tc.getClassLineNumber());
                updateStmt.setString(6, tc.getTestClassContent());
                updateStmt.setString(7, tc.getHelperClassesLineNumbers());
                updateStmt.setInt(8, tc.getClassLoc());
                setLargeString(updateStmt, 9, data.importedTypes);
                setLargeString(updateStmt, 10, data.referencedTypes);
                updateStmt.setLong(11, scanSessionId);
                updateStmt.setLong(12, repositoryId);
                updateStmt.setString(13, tc.getClassName());
                updateStmt.setString(14, tc.getPackageName());
                updateStmt.addBatch();
            }
            
            int[] updateResults = updateStmt.executeBatch();
            
            // Separate updated items from items needing insert
            for (int i = 0; i < updateResults.length; i++) {
                if (updateResults[i] > 0) {
                    // Updated successfully - get ID
                    TestClassInfo tc = batchData.get(i).tc;
                    long classId = getTestClassId(conn, repositoryId, tc.getClassName(), tc.getPackageName(), scanSessionId);
                    testClassIds.put(buildTestClassKey(tc.getPackageName(), tc.getClassName()), classId);
                } else {
                    // No update - needs insert
                    toInsert.add(batchData.get(i));
                }
            }
        }

        // Step 2: INSERT new items using batch helper (handles duplicates gracefully)
        if (!toInsert.isEmpty()) {
            String insertSql = """
                    INSERT INTO test_classes 
                    (repository_id, class_name, package_name, file_path, total_test_methods, 
                     annotated_test_methods, coverage_rate, scan_session_id, class_line_number, 
                     test_class_content, helper_classes_line_numbers, class_loc, 
                     imported_types, referenced_types) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            
            BiConsumer<PreparedStatement, TestClassBatchData> setter = (stmt, data) -> {
                try {
                    TestClassInfo tc = data.tc;
                    stmt.setLong(1, repositoryId);
                    stmt.setString(2, tc.getClassName());
                    stmt.setString(3, tc.getPackageName());
                    stmt.setString(4, tc.getFilePath());
                    stmt.setInt(5, tc.getTotalTestMethods());
                    stmt.setInt(6, tc.getAnnotatedTestMethods());
                    stmt.setDouble(7, data.coverage);
                    stmt.setLong(8, scanSessionId);
                    stmt.setInt(9, tc.getClassLineNumber());
                    stmt.setString(10, tc.getTestClassContent());
                    stmt.setString(11, tc.getHelperClassesLineNumbers());
                    stmt.setInt(12, tc.getClassLoc());
                    setLargeString(stmt, 13, data.importedTypes);
                    setLargeString(stmt, 14, data.referencedTypes);
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to set parameters for test class batch", e);
                }
            };
            
            // Use batch helper - automatically handles duplicates with fallback
            List<TestClassBatchData> failedItems = BatchOperationHelper.executeBatchWithFallback(
                conn, toInsert, BATCH_SIZE, setter, insertSql);
            
            // Retrieve IDs for all inserted items (including fallback inserts)
            // Note: Failed items (duplicates) will be skipped - they should already exist
            for (TestClassBatchData data : toInsert) {
                if (!failedItems.contains(data)) {
                    TestClassInfo tc = data.tc;
                    try {
                        long classId = getTestClassId(conn, repositoryId, tc.getClassName(), tc.getPackageName(), scanSessionId);
                        testClassIds.put(buildTestClassKey(tc.getPackageName(), tc.getClassName()), classId);
                    } catch (SQLException e) {
                        // If getTestClassId fails, item might have been a duplicate - skip it
                    }
                }
            }
        }
        
        return testClassIds;
    }

    private void persistTestMethodsBatch(Connection conn, RepositoryTestInfo repo, long repositoryId, long scanSessionId, Map<String, Long> testClassIds) throws SQLException {
        List<TestMethodInfo> allMethods = new ArrayList<>();
        for (TestClassInfo tc : repo.getTestClasses()) {
            allMethods.addAll(tc.getTestMethods());
        }
        if (allMethods.isEmpty()) return;

        // Prepare all method data upfront
        List<TestMethodBatchData> batchData = new ArrayList<>();
        for (TestMethodInfo method : allMethods) {
            String key = buildTestClassKey(method.getPackageName(), method.getClassName());
            Long testClassId = testClassIds.get(key);
            if (testClassId == null) {
                testClassId = getTestClassId(conn, repositoryId, method.getClassName(), method.getPackageName(), scanSessionId);
                testClassIds.put(key, testClassId);
            }
            
            TestMethodBatchData methodData = prepareTestMethodData(method, testClassId);
            batchData.add(methodData);
        }

        // Step 1: UPDATE existing methods in batch
        String updateSql = """
                UPDATE test_methods 
                SET method_signature = ?, line_number = ?, method_loc = ?, 
                    method_body_content = ?, has_annotation = ?, annotation_data = ?,
                    annotation_title = ?, annotation_author = ?, annotation_status = ?,
                    annotation_target_class = ?, annotation_target_method = ?,
                    annotation_description = ?, annotation_tags = ?, 
                    annotation_test_points = ?, annotation_requirements = ?,
                    annotation_defects = ?, annotation_testcases = ?,
                    annotation_last_update_time = ?, annotation_last_update_author = ?
                WHERE scan_session_id = ? AND test_class_id = ? 
                  AND method_name = ? AND method_signature IS NOT DISTINCT FROM ?
                """;
        
        List<TestMethodBatchData> toInsert = new ArrayList<>();
        
        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            for (TestMethodBatchData data : batchData) {
                setUpdateParameters(updateStmt, data, scanSessionId);
                updateStmt.addBatch();
            }
            
            int[] updateResults = updateStmt.executeBatch();
            
            for (int i = 0; i < updateResults.length; i++) {
                if (updateResults[i] == 0) {
                    toInsert.add(batchData.get(i));
                }
            }
        }

        // Step 2: INSERT new methods using batch helper (handles duplicates gracefully)
        if (!toInsert.isEmpty()) {
            String insertSql = """
                    INSERT INTO test_methods 
                    (test_class_id, method_name, method_signature, line_number, method_loc,
                     method_body_content, has_annotation, annotation_data, annotation_title,
                     annotation_author, annotation_status, annotation_target_class,
                     annotation_target_method, annotation_description, annotation_tags,
                     annotation_test_points, annotation_requirements, annotation_defects,
                     annotation_testcases, annotation_last_update_time,
                     annotation_last_update_author, scan_session_id) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            
            BiConsumer<PreparedStatement, TestMethodBatchData> setter = (stmt, data) -> {
                try {
                    setInsertParameters(stmt, data, scanSessionId);
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to set parameters for test method batch", e);
                }
            };
            
            // Use batch helper - automatically handles duplicates with fallback
            BatchOperationHelper.executeBatchWithFallback(conn, toInsert, BATCH_SIZE, setter, insertSql);
        }
    }
    
    /**
     * Prepare test method data for batch operations
     */
    private TestMethodBatchData prepareTestMethodData(TestMethodInfo method, Long testClassId) throws SQLException {
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
        
        return new TestMethodBatchData(method, testClassId, methodSignature, has,
                annotationJson, title, author, status, targetClass, targetMethod,
                description, tags, testPoints, requirements, defects, testcases,
                lastUpdateTime, lastUpdateAuthor);
    }
    
    /**
     * Set parameters for UPDATE statement
     */
    private void setUpdateParameters(PreparedStatement stmt, TestMethodBatchData data, long scanSessionId) throws SQLException {
        TestMethodInfo method = data.method;
        String methodBodyContent = method.getMethodBodyContent() != null && !method.getMethodBodyContent().isEmpty() 
                ? method.getMethodBodyContent() : null;
        
        stmt.setString(1, data.methodSignature);
        stmt.setInt(2, method.getLineNumber());
        stmt.setInt(3, method.getMethodLoc());
        stmt.setString(4, methodBodyContent);
        stmt.setBoolean(5, data.hasAnnotation);
        stmt.setString(6, data.annotationJson);
        stmt.setString(7, data.hasAnnotation ? data.title : null);
        stmt.setString(8, data.hasAnnotation ? data.author : null);
        stmt.setString(9, data.hasAnnotation ? data.status : null);
        stmt.setString(10, data.hasAnnotation ? data.targetClass : null);
        stmt.setString(11, data.hasAnnotation ? data.targetMethod : null);
        stmt.setString(12, data.hasAnnotation ? data.description : null);
        stmt.setString(13, data.hasAnnotation ? data.tags : null);
        stmt.setString(14, data.hasAnnotation ? data.testPoints : null);
        stmt.setString(15, data.hasAnnotation ? data.requirements : null);
        stmt.setString(16, data.hasAnnotation ? data.defects : null);
        stmt.setString(17, data.hasAnnotation ? data.testcases : null);
        stmt.setString(18, data.hasAnnotation ? data.lastUpdateTime : null);
        stmt.setString(19, data.hasAnnotation ? data.lastUpdateAuthor : null);
        stmt.setLong(20, scanSessionId);
        stmt.setLong(21, data.testClassId);
        stmt.setString(22, method.getMethodName());
        stmt.setString(23, data.methodSignature);
    }
    
    /**
     * Set parameters for INSERT statement
     */
    private void setInsertParameters(PreparedStatement stmt, TestMethodBatchData data, long scanSessionId) throws SQLException {
        TestMethodInfo method = data.method;
        String methodBodyContent = method.getMethodBodyContent() != null && !method.getMethodBodyContent().isEmpty() 
                ? method.getMethodBodyContent() : null;
        
        stmt.setLong(1, data.testClassId);
        stmt.setString(2, method.getMethodName());
        stmt.setString(3, data.methodSignature);
        stmt.setInt(4, method.getLineNumber());
        stmt.setInt(5, method.getMethodLoc());
        stmt.setString(6, methodBodyContent);
        stmt.setBoolean(7, data.hasAnnotation);
        stmt.setString(8, data.annotationJson);
        stmt.setString(9, data.hasAnnotation ? data.title : null);
        stmt.setString(10, data.hasAnnotation ? data.author : null);
        stmt.setString(11, data.hasAnnotation ? data.status : null);
        stmt.setString(12, data.hasAnnotation ? data.targetClass : null);
        stmt.setString(13, data.hasAnnotation ? data.targetMethod : null);
        stmt.setString(14, data.hasAnnotation ? data.description : null);
        stmt.setString(15, data.hasAnnotation ? data.tags : null);
        stmt.setString(16, data.hasAnnotation ? data.testPoints : null);
        stmt.setString(17, data.hasAnnotation ? data.requirements : null);
        stmt.setString(18, data.hasAnnotation ? data.defects : null);
        stmt.setString(19, data.hasAnnotation ? data.testcases : null);
        stmt.setString(20, data.hasAnnotation ? data.lastUpdateTime : null);
        stmt.setString(21, data.hasAnnotation ? data.lastUpdateAuthor : null);
        stmt.setLong(22, scanSessionId);
    }

    private void persistHelperClassesBatch(Connection conn, RepositoryTestInfo repo, long repositoryId, long scanSessionId) throws SQLException {
        List<TestHelperClassInfo> helperClasses = repo.getHelperClasses();
        if (helperClasses == null || helperClasses.isEmpty()) return;
        
        String insertSql = """
                INSERT INTO test_helper_classes 
                (repository_id, class_name, package_name, file_path, class_line_number, 
                 helper_class_content, loc, scan_session_id) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        
        BiConsumer<PreparedStatement, TestHelperClassInfo> setter = (stmt, helperClass) -> {
            try {
                stmt.setLong(1, repositoryId);
                stmt.setString(2, helperClass.getClassName());
                stmt.setString(3, helperClass.getPackageName());
                stmt.setString(4, helperClass.getFilePath());
                stmt.setObject(5, helperClass.getClassLineNumber(), java.sql.Types.INTEGER);
                stmt.setString(6, helperClass.getHelperClassContent());
                stmt.setInt(7, helperClass.getLoc());
                stmt.setLong(8, scanSessionId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        
        // Use batch helper - automatically handles duplicates with fallback
        BatchOperationHelper.executeBatchWithFallback(conn, helperClasses, BATCH_SIZE, setter, insertSql);
    }

    private long getTestClassId(Connection conn, long repositoryId, String className, String packageName, long scanSessionId) throws SQLException {
        String sql = """
                SELECT id FROM test_classes 
                WHERE repository_id = ? AND class_name = ? 
                  AND package_name = ? AND scan_session_id = ? 
                ORDER BY id DESC 
                LIMIT 1
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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

    /**
     * Build IN clause placeholders for SQL queries.
     * Example: buildInClausePlaceholders(3) returns "?, ?, ?"
     */
    private static String buildInClausePlaceholders(int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder("?");
        for (int i = 1; i < count; i++) {
            sb.append(", ?");
        }
        return sb.toString();
    }

    private void updateDailyMetrics(Connection conn, TestCollectionSummary summary) throws SQLException {
        double overallCoverage = summary.getTotalTestMethods() > 0 
            ? (double) summary.getTotalAnnotatedTestMethods() / summary.getTotalTestMethods() * 100 
            : 0.0;
        
        String updateSql = """
                UPDATE daily_metrics 
                SET total_repositories = ?, 
                    total_test_classes = ?, 
                    total_test_methods = ?, 
                    total_annotated_methods = ?, 
                    overall_coverage_rate = ? 
                WHERE metric_date = CURRENT_DATE
                """;
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

        String insertSql = """
                INSERT INTO daily_metrics 
                (metric_date, total_repositories, total_test_classes, total_test_methods, 
                 total_annotated_methods, overall_coverage_rate) 
                VALUES (CURRENT_DATE, ?, ?, ?, ?, ?)
                """;
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

    /**
     * Merge repository scan results into an existing scan session.
     * This deletes old data for the specified repositories from the session and inserts new data.
     * Used for repository-level scans that should update the latest scan session instead of creating a new one.
     * 
     * @param summary The scan summary containing repository results to merge
     * @param scanSessionId The existing scan session ID to merge into
     * @param repositoryIds The repository IDs being rescanned (for cleanup) - can be null to use git URLs from summary
     * @throws SQLException if database operations fail
     */
    public void mergeIntoExistingSession(TestCollectionSummary summary, long scanSessionId, 
            java.util.List<Long> repositoryIds) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete old data for the repositories being rescanned from this session
                // Use git URLs from summary if repositoryIds not provided
                if (repositoryIds != null && !repositoryIds.isEmpty()) {
                    deleteRepositoryDataFromSession(conn, scanSessionId, repositoryIds);
                } else {
                    // Fallback: delete by git URLs from the scan summary
                    java.util.List<String> gitUrls = summary.getRepositories().stream()
                            .map(RepositoryTestInfo::getGitUrl)
                            .filter(url -> url != null && !url.isEmpty())
                            .collect(java.util.stream.Collectors.toList());
                    deleteRepositoryDataFromSessionByGitUrls(conn, scanSessionId, gitUrls);
                }

                // Insert/update new data for the scanned repositories
                java.util.List<Long> scannedRepositoryIds = new java.util.ArrayList<>();
                for (RepositoryTestInfo repo : summary.getRepositories()) {
                    int teamId = ensureTeamExists(conn, repo.getTeamName(), repo.getTeamCode());
                    long repositoryId = upsertRepository(conn, repo, teamId);
                    scannedRepositoryIds.add(repositoryId);
                    Map<String, Long> testClassIds = persistTestClassesBatch(conn, repo, repositoryId, scanSessionId);
                    persistTestMethodsBatch(conn, repo, repositoryId, scanSessionId, testClassIds);
                    persistHelperClassesBatch(conn, repo, repositoryId, scanSessionId);
                }

                // Update scan session metadata (recalculate totals)
                updateScanSessionTotals(conn, scanSessionId);
                
                // Update daily metrics
                updateDailyMetrics(conn, summary);
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Delete old test data for specific repositories from a scan session.
     * This cleans up existing data before merging new results.
     */
    private void deleteRepositoryDataFromSession(Connection conn, long scanSessionId, 
            java.util.List<Long> repositoryIds) throws SQLException {
        if (repositoryIds == null || repositoryIds.isEmpty()) {
            return;
        }

        String inClause = buildInClausePlaceholders(repositoryIds.size());

        // Delete test methods for these repositories in this session
        String deleteMethodsSql = String.format("""
                DELETE FROM test_methods 
                WHERE scan_session_id = ? AND test_class_id IN (
                  SELECT id FROM test_classes 
                  WHERE scan_session_id = ? AND repository_id IN (%s)
                )
                """, inClause);
        
        try (PreparedStatement stmt = conn.prepareStatement(deleteMethodsSql)) {
            int paramIndex = 1;
            stmt.setLong(paramIndex++, scanSessionId);
            stmt.setLong(paramIndex++, scanSessionId);
            for (Long repoId : repositoryIds) {
                stmt.setLong(paramIndex++, repoId);
            }
            stmt.executeUpdate();
        }

        // Delete test classes for these repositories in this session
        String deleteClassesSql = String.format("""
                DELETE FROM test_classes 
                WHERE scan_session_id = ? AND repository_id IN (%s)
                """, inClause);
        try (PreparedStatement stmt = conn.prepareStatement(deleteClassesSql)) {
            int paramIndex = 1;
            stmt.setLong(paramIndex++, scanSessionId);
            for (Long repoId : repositoryIds) {
                stmt.setLong(paramIndex++, repoId);
            }
            stmt.executeUpdate();
        }

        // Delete helper classes for these repositories in this session
        String deleteHelperClassesSql = String.format("""
                DELETE FROM test_helper_classes 
                WHERE scan_session_id = ? AND repository_id IN (%s)
                """, inClause);
        try (PreparedStatement stmt = conn.prepareStatement(deleteHelperClassesSql)) {
            int paramIndex = 1;
            stmt.setLong(paramIndex++, scanSessionId);
            for (Long repoId : repositoryIds) {
                stmt.setLong(paramIndex++, repoId);
            }
            stmt.executeUpdate();
        }
    }

    /**
     * Delete old test data for specific repositories (identified by git URLs) from a scan session.
     * Alternative method that uses git URLs instead of repository IDs.
     */
    private void deleteRepositoryDataFromSessionByGitUrls(Connection conn, long scanSessionId, 
            java.util.List<String> gitUrls) throws SQLException {
        if (gitUrls == null || gitUrls.isEmpty()) {
            return;
        }

        String inClause = buildInClausePlaceholders(gitUrls.size());

        // Delete test methods for these repositories in this session
        String deleteMethodsSql = String.format("""
                DELETE FROM test_methods 
                WHERE scan_session_id = ? AND test_class_id IN (
                  SELECT id FROM test_classes 
                  WHERE scan_session_id = ? AND repository_id IN (
                    SELECT id FROM repositories 
                    WHERE git_url IN (%s)
                  )
                )
                """, inClause);
        
        try (PreparedStatement stmt = conn.prepareStatement(deleteMethodsSql)) {
            int paramIndex = 1;
            stmt.setLong(paramIndex++, scanSessionId);
            stmt.setLong(paramIndex++, scanSessionId);
            for (String gitUrl : gitUrls) {
                stmt.setString(paramIndex++, gitUrl);
            }
            stmt.executeUpdate();
        }

        // Delete test classes for these repositories in this session
        String deleteClassesSql = String.format("""
                DELETE FROM test_classes 
                WHERE scan_session_id = ? AND repository_id IN (
                  SELECT id FROM repositories 
                  WHERE git_url IN (%s)
                )
                """, inClause);
        try (PreparedStatement stmt = conn.prepareStatement(deleteClassesSql)) {
            int paramIndex = 1;
            stmt.setLong(paramIndex++, scanSessionId);
            for (String gitUrl : gitUrls) {
                stmt.setString(paramIndex++, gitUrl);
            }
            stmt.executeUpdate();
        }

        // Delete helper classes for these repositories in this session
        String deleteHelperClassesSql = String.format("""
                DELETE FROM test_helper_classes 
                WHERE scan_session_id = ? AND repository_id IN (
                  SELECT id FROM repositories 
                  WHERE git_url IN (%s)
                )
                """, inClause);
        try (PreparedStatement stmt = conn.prepareStatement(deleteHelperClassesSql)) {
            int paramIndex = 1;
            stmt.setLong(paramIndex++, scanSessionId);
            for (String gitUrl : gitUrls) {
                stmt.setString(paramIndex++, gitUrl);
            }
            stmt.executeUpdate();
        }
    }

    /**
     * Update scan session totals based on current data in the session.
     */
    private void updateScanSessionTotals(Connection conn, long scanSessionId) throws SQLException {
        // Recalculate totals from actual data in the session
        String updateSql = """
                UPDATE scan_sessions 
                SET total_repositories = (
                      SELECT COUNT(DISTINCT repository_id) 
                      FROM test_classes 
                      WHERE scan_session_id = ?
                    ),
                    total_test_classes = (
                      SELECT COUNT(*) 
                      FROM test_classes 
                      WHERE scan_session_id = ?
                    ),
                    total_test_methods = (
                      SELECT COUNT(*) 
                      FROM test_methods 
                      WHERE scan_session_id = ?
                    ),
                    total_annotated_methods = (
                      SELECT COUNT(*) 
                      FROM test_methods 
                      WHERE scan_session_id = ? AND has_annotation = TRUE
                    )
                WHERE id = ?
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setLong(1, scanSessionId);
            stmt.setLong(2, scanSessionId);
            stmt.setLong(3, scanSessionId);
            stmt.setLong(4, scanSessionId);
            stmt.setLong(5, scanSessionId);
            stmt.executeUpdate();
        }
    }
}


