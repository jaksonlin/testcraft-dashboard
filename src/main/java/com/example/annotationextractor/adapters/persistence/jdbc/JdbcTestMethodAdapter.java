package com.example.annotationextractor.adapters.persistence.jdbc;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.domain.model.TestMethod;
import com.example.annotationextractor.domain.model.TestMethodDetailRecord;
import com.example.annotationextractor.domain.port.TestMethodPort;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class JdbcTestMethodAdapter implements TestMethodPort {

    @Override
    public Optional<TestMethod> findById(Long id) {
        String sql = "SELECT * FROM test_methods WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TestMethod> findByTestClassId(Long testClassId) {
        String sql = "SELECT * FROM test_methods WHERE test_class_id = ? ORDER BY id";
        List<TestMethod> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, testClassId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public List<TestMethod> findByScanSessionId(Long scanSessionId) {
        String sql = "SELECT * FROM test_methods WHERE scan_session_id = ? ORDER BY id";
        List<TestMethod> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, scanSessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public List<TestMethod> findAnnotatedByRepositoryAndScanSessionId(Long repositoryId, Long scanSessionId) {
        String sql = "SELECT tm.* FROM test_methods tm JOIN test_classes tc ON tm.test_class_id = tc.id WHERE tc.repository_id = ? AND tc.scan_session_id = ? AND tm.has_annotation = TRUE ORDER BY tm.id";
        List<TestMethod> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, repositoryId);
            stmt.setLong(2, scanSessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public List<TestMethodDetailRecord> findTestMethodDetailsByTeamIdAndScanSessionId(Long teamId, Long scanSessionId,
            Integer limit) {
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
                WHERE tc.scan_session_id = ? AND r.team_id = ?
                ORDER BY r.repository_name, tc.class_name, tm.method_name
                """);

        if (limit != null && limit > 0) {
            sql.append(" LIMIT ?");
        }

        List<TestMethodDetailRecord> testMethods = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setLong(1, scanSessionId);
            stmt.setLong(2, teamId);
            if (limit != null && limit > 0) {
                stmt.setInt(3, limit);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String repositoryName = rs.getString("repository_name");
                    String testClassName = rs.getString("class_name");
                    String testMethodName = rs.getString("method_name");
                    Integer lineNumber = rs.getInt("line_number");
                    String annotationTitle = rs.getString("annotation_title");
                    String annotationAuthor = rs.getString("annotation_author");
                    String annotationStatus = rs.getString("annotation_status");
                    String annotationTargetClass = rs.getString("annotation_target_class");
                    String annotationTargetMethod = rs.getString("annotation_target_method");
                    String annotationDescription = rs.getString("annotation_description");
                    String annotationTestPoints = rs.getString("annotation_test_points");

                    // Parse comma-separated strings into lists
                    List<String> annotationTags = parseStringArray(rs.getString("annotation_tags"));
                    List<String> annotationRequirements = parseStringArray(rs.getString("annotation_requirements"));
                    List<String> annotationTestcases = parseStringArray(rs.getString("annotation_testcases"));
                    List<String> annotationDefects = parseStringArray(rs.getString("annotation_defects"));

                    // Parse timestamp
                    String lastUpdateTime = rs.getString("annotation_last_update_time");
                    LocalDateTime lastUpdateDateTime = null;
                    if (lastUpdateTime != null && !lastUpdateTime.trim().isEmpty()) {
                        try {
                            lastUpdateDateTime = LocalDateTime.parse(lastUpdateTime);
                        } catch (Exception e) {
                            // If parsing fails, set to null
                            lastUpdateDateTime = null;
                        }
                    }

                    String annotationLastUpdateAuthor = rs.getString("annotation_last_update_author");
                    String teamName = rs.getString("team_name");
                    String teamCode = rs.getString("team_code");
                    String gitUrl = rs.getString("git_url");

                    testMethods.add(new TestMethodDetailRecord(
                            id, repositoryName, testClassName, testMethodName,
                            lineNumber, annotationTitle, annotationAuthor, annotationStatus,
                            annotationTargetClass, annotationTargetMethod, annotationDescription,
                            annotationTestPoints, annotationTags, annotationRequirements,
                            annotationTestcases, annotationDefects, lastUpdateDateTime,
                            annotationLastUpdateAuthor, teamName, teamCode, gitUrl));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return testMethods;
    }

    @Override
    public long countByTeamIdAndScanSessionId(Long teamId, Long scanSessionId) {
        String sql = "SELECT COUNT(*) FROM test_methods WHERE team_id = ? AND scan_session_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, teamId);
            stmt.setLong(2, scanSessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TestMethodDetailRecord> findTestMethodDetailsByRepositoryIdAndScanSessionId(Long repositoryId,
            Long scanSessionId, Integer limit) {
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
                WHERE tc.scan_session_id = ? AND r.id = ?
                ORDER BY r.repository_name, tc.class_name, tm.method_name
                """);

        if (limit != null && limit > 0) {
            sql.append(" LIMIT ?");
        }

        List<TestMethodDetailRecord> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setLong(1, scanSessionId);
            stmt.setLong(2, repositoryId);
            if (limit != null && limit > 0) {
                stmt.setInt(3, limit);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String repositoryName = rs.getString("repository_name");
                    String testClassName = rs.getString("class_name");
                    String testMethodName = rs.getString("method_name");
                    Integer lineNumber = rs.getInt("line_number");
                    String annotationTitle = rs.getString("annotation_title");
                    String annotationAuthor = rs.getString("annotation_author");
                    String annotationStatus = rs.getString("annotation_status");
                    String annotationTargetClass = rs.getString("annotation_target_class");
                    String annotationTargetMethod = rs.getString("annotation_target_method");
                    String annotationDescription = rs.getString("annotation_description");
                    String annotationTestPoints = rs.getString("annotation_test_points");

                    // Parse comma-separated strings into lists
                    List<String> annotationTags = parseStringArray(rs.getString("annotation_tags"));
                    List<String> annotationRequirements = parseStringArray(rs.getString("annotation_requirements"));
                    List<String> annotationTestcases = parseStringArray(rs.getString("annotation_testcases"));
                    List<String> annotationDefects = parseStringArray(rs.getString("annotation_defects"));

                    // Parse timestamp
                    String lastUpdateTime = rs.getString("annotation_last_update_time");
                    LocalDateTime lastUpdateDateTime = null;
                    if (lastUpdateTime != null && !lastUpdateTime.trim().isEmpty()) {
                        try {
                            lastUpdateDateTime = LocalDateTime.parse(lastUpdateTime);
                        } catch (Exception e) {
                            // If parsing fails, set to null
                            lastUpdateDateTime = null;
                        }
                    }

                    String annotationLastUpdateAuthor = rs.getString("annotation_last_update_author");
                    String teamName = rs.getString("team_name");
                    String teamCode = rs.getString("team_code");
                    String gitUrl = rs.getString("git_url");

                    result.add(new TestMethodDetailRecord(
                            id, repositoryName, testClassName, testMethodName,
                            lineNumber, annotationTitle, annotationAuthor, annotationStatus,
                            annotationTargetClass, annotationTargetMethod, annotationDescription,
                            annotationTestPoints, annotationTags, annotationRequirements,
                            annotationTestcases, annotationDefects, lastUpdateDateTime,
                            annotationLastUpdateAuthor, teamName, teamCode, gitUrl));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;

    }

    @Override
    public long countByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId) {
        String sql = "SELECT COUNT(*) FROM test_methods WHERE repository_id = ? AND scan_session_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, repositoryId);
            stmt.setLong(2, scanSessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TestMethodDetailRecord> findTestMethodDetailsByClassId(Long classId, Integer limit) {
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
                WHERE tc.id = ?
                ORDER BY r.repository_name, tc.class_name, tm.method_name
                """);

        if (limit != null && limit > 0) {
            sql.append(" LIMIT ?");
        }

        List<TestMethodDetailRecord> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setLong(1, classId);
            if (limit != null && limit > 0) {
                stmt.setInt(2, limit);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String repositoryName = rs.getString("repository_name");
                    String testClassName = rs.getString("class_name");
                    String testMethodName = rs.getString("method_name");
                    Integer lineNumber = rs.getInt("line_number");
                    String annotationTitle = rs.getString("annotation_title");
                    String annotationAuthor = rs.getString("annotation_author");
                    String annotationStatus = rs.getString("annotation_status");
                    String annotationTargetClass = rs.getString("annotation_target_class");
                    String annotationTargetMethod = rs.getString("annotation_target_method");
                    String annotationDescription = rs.getString("annotation_description");
                    String annotationTestPoints = rs.getString("annotation_test_points");

                    // Parse comma-separated strings into lists
                    List<String> annotationTags = parseStringArray(rs.getString("annotation_tags"));
                    List<String> annotationRequirements = parseStringArray(rs.getString("annotation_requirements"));
                    List<String> annotationTestcases = parseStringArray(rs.getString("annotation_testcases"));
                    List<String> annotationDefects = parseStringArray(rs.getString("annotation_defects"));

                    // Parse timestamp
                    String lastUpdateTime = rs.getString("annotation_last_update_time");
                    LocalDateTime lastUpdateDateTime = null;
                    if (lastUpdateTime != null && !lastUpdateTime.trim().isEmpty()) {
                        try {
                            lastUpdateDateTime = LocalDateTime.parse(lastUpdateTime);
                        } catch (Exception e) {
                            // If parsing fails, set to null
                            lastUpdateDateTime = null;
                        }
                    }

                    String annotationLastUpdateAuthor = rs.getString("annotation_last_update_author");
                    String teamName = rs.getString("team_name");
                    String teamCode = rs.getString("team_code");
                    String gitUrl = rs.getString("git_url");

                    result.add(new TestMethodDetailRecord(
                            id, repositoryName, testClassName, testMethodName,
                            lineNumber, annotationTitle, annotationAuthor, annotationStatus,
                            annotationTargetClass, annotationTargetMethod, annotationDescription,
                            annotationTestPoints, annotationTags, annotationRequirements,
                            annotationTestcases, annotationDefects, lastUpdateDateTime,
                            annotationLastUpdateAuthor, teamName, teamCode, gitUrl));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;

    }

    @Override
    public long countByClassId(Long classId) {
        String sql = "SELECT COUNT(*) FROM test_methods WHERE test_class_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, classId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TestMethodDetailRecord> findTestMethodDetailsByScanSessionId(Long scanSessionId, Integer limit) {
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
                WHERE tc.scan_session_id = ?
                ORDER BY r.repository_name, tc.class_name, tm.method_name
                """);

        if (limit != null && limit > 0) {
            sql.append(" LIMIT ?");
        }

        List<TestMethodDetailRecord> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setLong(1, scanSessionId);
            if (limit != null && limit > 0) {
                stmt.setInt(2, limit);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String repositoryName = rs.getString("repository_name");
                    String testClassName = rs.getString("class_name");
                    String testMethodName = rs.getString("method_name");
                    Integer lineNumber = rs.getInt("line_number");
                    String annotationTitle = rs.getString("annotation_title");
                    String annotationAuthor = rs.getString("annotation_author");
                    String annotationStatus = rs.getString("annotation_status");
                    String annotationTargetClass = rs.getString("annotation_target_class");
                    String annotationTargetMethod = rs.getString("annotation_target_method");
                    String annotationDescription = rs.getString("annotation_description");
                    String annotationTestPoints = rs.getString("annotation_test_points");

                    // Parse comma-separated strings into lists
                    List<String> annotationTags = parseStringArray(rs.getString("annotation_tags"));
                    List<String> annotationRequirements = parseStringArray(rs.getString("annotation_requirements"));
                    List<String> annotationTestcases = parseStringArray(rs.getString("annotation_testcases"));
                    List<String> annotationDefects = parseStringArray(rs.getString("annotation_defects"));

                    // Parse timestamp
                    String lastUpdateTime = rs.getString("annotation_last_update_time");
                    LocalDateTime lastUpdateDateTime = null;
                    if (lastUpdateTime != null && !lastUpdateTime.trim().isEmpty()) {
                        try {
                            lastUpdateDateTime = LocalDateTime.parse(lastUpdateTime);
                        } catch (Exception e) {
                            // If parsing fails, set to null
                            lastUpdateDateTime = null;
                        }
                    }

                    String annotationLastUpdateAuthor = rs.getString("annotation_last_update_author");
                    String teamName = rs.getString("team_name");
                    String teamCode = rs.getString("team_code");
                    String gitUrl = rs.getString("git_url");

                    result.add(new TestMethodDetailRecord(
                            id, repositoryName, testClassName, testMethodName,
                            lineNumber, annotationTitle, annotationAuthor, annotationStatus,
                            annotationTargetClass, annotationTargetMethod, annotationDescription,
                            annotationTestPoints, annotationTags, annotationRequirements,
                            annotationTestcases, annotationDefects, lastUpdateDateTime,
                            annotationLastUpdateAuthor, teamName, teamCode, gitUrl));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;

    }

    @Override
    public long countByScanSessionId(Long scanSessionId) {
        String sql = "SELECT COUNT(*) FROM test_methods WHERE scan_session_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, scanSessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    private TestMethod mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long testClassId = rs.getLong("test_class_id");
        String methodName = rs.getString("method_name");
        String methodSignature = rs.getString("method_signature");
        Integer lineNumber = (Integer) rs.getObject("line_number");
        Integer methodLoc = (Integer) rs.getObject("method_loc");
        boolean hasAnnotation = rs.getBoolean("has_annotation");
        String annotationData = rs.getString("annotation_data");
        String annotationTitle = rs.getString("annotation_title");
        String annotationAuthor = rs.getString("annotation_author");
        String annotationStatus = rs.getString("annotation_status");
        String annotationTargetClass = rs.getString("annotation_target_class");
        String annotationTargetMethod = rs.getString("annotation_target_method");
        String annotationDescription = rs.getString("annotation_description");
        String annotationTags = rs.getString("annotation_tags");
        String annotationTestPoints = rs.getString("annotation_test_points");
        String annotationRequirements = rs.getString("annotation_requirements");
        String annotationDefects = rs.getString("annotation_defects");
        String annotationTestcases = rs.getString("annotation_testcases");
        String annotationLastUpdateTime = rs.getString("annotation_last_update_time");
        String annotationLastUpdateAuthor = rs.getString("annotation_last_update_author");
        Timestamp firstSeen = rs.getTimestamp("first_seen_date");
        Timestamp lastModified = rs.getTimestamp("last_modified_date");
        Long scanSessionId = (Long) rs.getObject("scan_session_id");

        return new TestMethod(
                id,
                testClassId,
                methodName,
                methodSignature,
                lineNumber,
                methodLoc,
                hasAnnotation,
                annotationData,
                annotationTitle,
                annotationAuthor,
                annotationStatus,
                annotationTargetClass,
                annotationTargetMethod,
                annotationDescription,
                annotationTags,
                annotationTestPoints,
                annotationRequirements,
                annotationDefects,
                annotationTestcases,
                annotationLastUpdateTime,
                annotationLastUpdateAuthor,
                firstSeen != null ? firstSeen.toInstant() : null,
                lastModified != null ? lastModified.toInstant() : null,
                scanSessionId);
    }

    /**
     * Find test method details with filtering at DATABASE level (no client-side
     * filtering)
     * All filtering is done via SQL WHERE clauses for maximum performance
     */
    public List<TestMethodDetailRecord> findTestMethodDetailsWithFilters(
            Long scanSessionId,
            String teamName,
            String repositoryName,
            String packageName,
            String className,
            Boolean annotated,
            String searchTerm,
            String codePattern,
            Integer offset,
            Integer limit) {

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
                WHERE tc.scan_session_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(scanSessionId);

        // Search term filter (searches across multiple fields)
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(
                    " AND (LOWER(tm.method_name) LIKE LOWER(?) OR LOWER(tc.class_name) LIKE LOWER(?) OR LOWER(r.repository_name) LIKE LOWER(?) OR LOWER(tm.annotation_title) LIKE LOWER(?))");
            String searchPattern = "%" + searchTerm + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        // Team name filter (case-insensitive)
        if (teamName != null && !teamName.trim().isEmpty()) {
            sql.append(" AND LOWER(t.team_name) LIKE LOWER(?)");
            params.add("%" + teamName + "%");
        }

        // Repository name filter (case-insensitive)
        if (repositoryName != null && !repositoryName.trim().isEmpty()) {
            sql.append(" AND LOWER(r.repository_name) LIKE LOWER(?)");
            params.add("%" + repositoryName + "%");
        }

        // Package name filter (uses dedicated package_name column)
        if (packageName != null && !packageName.trim().isEmpty()) {
            sql.append(" AND LOWER(tc.package_name) LIKE LOWER(?)");
            params.add("%" + packageName + "%");
        }

        // Class name filter (uses class_name column - simple class name,
        // case-insensitive)
        if (className != null && !className.trim().isEmpty()) {
            sql.append(" AND LOWER(tc.class_name) LIKE LOWER(?)");
            params.add("%" + className + "%");
        }

        // Annotation status filter
        if (annotated != null) {
            if (annotated) {
                sql.append(" AND tm.annotation_title IS NOT NULL AND tm.annotation_title != ''");
            } else {
                sql.append(" AND (tm.annotation_title IS NULL OR tm.annotation_title = '')");
            }
        }

        // Code pattern filter (searches in target class, target method, and method body
        // content)
        if (codePattern != null && !codePattern.trim().isEmpty()) {
            sql.append(
                    " AND (LOWER(tm.annotation_target_class) LIKE LOWER(?) OR LOWER(tm.annotation_target_method) LIKE LOWER(?) OR LOWER(tm.method_body_content) LIKE LOWER(?))");
            String codePatternSearch = "%" + codePattern + "%";
            params.add(codePatternSearch);
            params.add(codePatternSearch);
            params.add(codePatternSearch);
        }

        sql.append(" ORDER BY r.repository_name, tc.class_name, tm.method_name");

        // Pagination
        if (limit != null && limit > 0) {
            sql.append(" LIMIT ?");
            params.add(limit);
        }
        if (offset != null && offset > 0) {
            sql.append(" OFFSET ?");
            params.add(offset);
        }

        List<TestMethodDetailRecord> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String repoName = rs.getString("repository_name");
                    String testClassName = rs.getString("class_name");
                    String testMethodName = rs.getString("method_name");
                    Integer lineNumber = rs.getInt("line_number");
                    String annotationTitle = rs.getString("annotation_title");
                    String annotationAuthor = rs.getString("annotation_author");
                    String annotationStatus = rs.getString("annotation_status");
                    String annotationTargetClass = rs.getString("annotation_target_class");
                    String annotationTargetMethod = rs.getString("annotation_target_method");
                    String annotationDescription = rs.getString("annotation_description");
                    String annotationTestPoints = rs.getString("annotation_test_points");

                    List<String> annotationTags = parseStringArray(rs.getString("annotation_tags"));
                    List<String> annotationRequirements = parseStringArray(rs.getString("annotation_requirements"));
                    List<String> annotationTestcases = parseStringArray(rs.getString("annotation_testcases"));
                    List<String> annotationDefects = parseStringArray(rs.getString("annotation_defects"));

                    String lastUpdateTime = rs.getString("annotation_last_update_time");
                    LocalDateTime lastUpdateDateTime = null;
                    if (lastUpdateTime != null && !lastUpdateTime.trim().isEmpty()) {
                        try {
                            lastUpdateDateTime = LocalDateTime.parse(lastUpdateTime);
                        } catch (Exception e) {
                            // Ignore parse errors
                        }
                    }

                    String annotationLastUpdateAuthor = rs.getString("annotation_last_update_author");
                    String teamNameResult = rs.getString("team_name");
                    String teamCode = rs.getString("team_code");
                    String gitUrl = rs.getString("git_url");

                    result.add(new TestMethodDetailRecord(
                            id, repoName, testClassName, testMethodName, lineNumber,
                            annotationTitle, annotationAuthor, annotationStatus,
                            annotationTargetClass, annotationTargetMethod, annotationDescription,
                            annotationTestPoints, annotationTags, annotationRequirements,
                            annotationTestcases, annotationDefects, lastUpdateDateTime,
                            annotationLastUpdateAuthor, teamNameResult, teamCode, gitUrl));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during filtered query: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Count test method details with filters (for pagination)
     * Uses same WHERE clauses as findTestMethodDetailsWithFilters
     */
    public long countTestMethodDetailsWithFilters(
            Long scanSessionId,
            String teamName,
            String repositoryName,
            String packageName,
            String className,
            Boolean annotated,
            String searchTerm,
            String codePattern) {

        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM test_methods tm
                JOIN test_classes tc ON tm.test_class_id = tc.id
                JOIN repositories r ON tc.repository_id = r.id
                LEFT JOIN teams t ON r.team_id = t.id
                WHERE tc.scan_session_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(scanSessionId);

        // Search term filter (searches across multiple fields)
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(
                    " AND (LOWER(tm.method_name) LIKE LOWER(?) OR LOWER(tc.class_name) LIKE LOWER(?) OR LOWER(r.repository_name) LIKE LOWER(?) OR LOWER(tm.annotation_title) LIKE LOWER(?))");
            String searchPattern = "%" + searchTerm + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        // Apply same filters as in findTestMethodDetailsWithFilters
        if (teamName != null && !teamName.trim().isEmpty()) {
            sql.append(" AND LOWER(t.team_name) LIKE LOWER(?)");
            params.add("%" + teamName + "%");
        }

        if (repositoryName != null && !repositoryName.trim().isEmpty()) {
            sql.append(" AND LOWER(r.repository_name) LIKE LOWER(?)");
            params.add("%" + repositoryName + "%");
        }

        if (packageName != null && !packageName.trim().isEmpty()) {
            sql.append(" AND LOWER(tc.package_name) LIKE LOWER(?)");
            params.add("%" + packageName + "%");
        }

        if (className != null && !className.trim().isEmpty()) {
            sql.append(" AND LOWER(tc.class_name) LIKE LOWER(?)");
            params.add("%" + className + "%");
        }

        if (annotated != null) {
            if (annotated) {
                sql.append(" AND tm.annotation_title IS NOT NULL AND tm.annotation_title != ''");
            } else {
                sql.append(" AND (tm.annotation_title IS NULL OR tm.annotation_title = '')");
            }
        }

        // Code pattern filter (searches in target class, target method, and method body
        // content)
        if (codePattern != null && !codePattern.trim().isEmpty()) {
            sql.append(
                    " AND (LOWER(tm.annotation_target_class) LIKE LOWER(?) OR LOWER(tm.annotation_target_method) LIKE LOWER(?) OR LOWER(tm.method_body_content) LIKE LOWER(?))");
            String codePatternSearch = "%" + codePattern + "%";
            params.add(codePatternSearch);
            params.add(codePatternSearch);
            params.add(codePatternSearch);
        }

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error during count query: " + e.getMessage(), e);
        }
    }

    /**
     * Get hierarchical summary by team (for progressive loading)
     * Returns aggregated data grouped by team with counts and coverage
     */
    public List<Map<String, Object>> getHierarchyByTeam(Long scanSessionId) {
        String sql = """
                SELECT
                    t.id as team_id,
                    t.team_name,
                    t.team_code,
                    COUNT(DISTINCT tc.id) as class_count,
                    COUNT(tm.id) as method_count,
                    SUM(CASE WHEN tm.annotation_title IS NOT NULL AND tm.annotation_title != '' THEN 1 ELSE 0 END) as annotated_count
                FROM test_methods tm
                JOIN test_classes tc ON tm.test_class_id = tc.id
                JOIN repositories r ON tc.repository_id = r.id
                LEFT JOIN teams t ON r.team_id = t.id
                WHERE tc.scan_session_id = ?
                GROUP BY t.id, t.team_name, t.team_code
                ORDER BY t.team_name
                """;

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, scanSessionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long methodCount = rs.getLong("method_count");
                    long annotatedCount = rs.getLong("annotated_count");
                    double coverage = methodCount > 0 ? (annotatedCount * 100.0 / methodCount) : 0.0;

                    result.add(Map.of(
                            "type", "TEAM",
                            "id", rs.getObject("team_id") != null ? rs.getLong("team_id") : 0L,
                            "name", rs.getString("team_name") != null ? rs.getString("team_name") : "Unknown",
                            "code", rs.getString("team_code") != null ? rs.getString("team_code") : "",
                            "classCount", rs.getLong("class_count"),
                            "methodCount", methodCount,
                            "annotatedCount", annotatedCount,
                            "coverageRate", coverage));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during hierarchy query: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Get hierarchical summary by package within a team
     * Uses the dedicated package_name column for accurate grouping
     */
    public List<Map<String, Object>> getHierarchyByPackage(Long scanSessionId, String teamName) {
        String sql = """
                SELECT
                    tc.package_name,
                    COUNT(DISTINCT tc.id) as class_count,
                    COUNT(tm.id) as method_count,
                    SUM(CASE WHEN tm.annotation_title IS NOT NULL AND tm.annotation_title != '' THEN 1 ELSE 0 END) as annotated_count
                FROM test_methods tm
                JOIN test_classes tc ON tm.test_class_id = tc.id
                JOIN repositories r ON tc.repository_id = r.id
                LEFT JOIN teams t ON r.team_id = t.id
                WHERE tc.scan_session_id = ?
                AND LOWER(t.team_name) = LOWER(?)
                AND tc.package_name IS NOT NULL
                AND tc.package_name != ''
                GROUP BY tc.package_name
                ORDER BY tc.package_name
                """;

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, scanSessionId);
            stmt.setString(2, teamName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String packageName = rs.getString("package_name");
                    if (packageName == null || packageName.isEmpty()) {
                        packageName = "(default package)";
                    }

                    long methodCount = rs.getLong("method_count");
                    long annotatedCount = rs.getLong("annotated_count");
                    double coverage = methodCount > 0 ? (annotatedCount * 100.0 / methodCount) : 0.0;

                    result.add(Map.of(
                            "type", "PACKAGE",
                            "name", packageName,
                            "classCount", rs.getLong("class_count"),
                            "methodCount", methodCount,
                            "annotatedCount", annotatedCount,
                            "coverageRate", coverage));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during package hierarchy query: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Get hierarchical summary by class within a package
     * Uses the dedicated package_name column for filtering
     */
    public List<Map<String, Object>> getHierarchyByClass(Long scanSessionId, String teamName, String packageName) {
        String sql = """
                SELECT
                    tc.id as class_id,
                    tc.class_name,
                    tc.package_name,
                    COUNT(tm.id) as method_count,
                    SUM(CASE WHEN tm.annotation_title IS NOT NULL AND tm.annotation_title != '' THEN 1 ELSE 0 END) as annotated_count
                FROM test_methods tm
                JOIN test_classes tc ON tm.test_class_id = tc.id
                JOIN repositories r ON tc.repository_id = r.id
                LEFT JOIN teams t ON r.team_id = t.id
                WHERE tc.scan_session_id = ?
                AND LOWER(t.team_name) = LOWER(?)
                AND LOWER(tc.package_name) = LOWER(?)
                GROUP BY tc.id, tc.class_name, tc.package_name
                ORDER BY tc.class_name
                """;

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, scanSessionId);
            stmt.setString(2, teamName);
            stmt.setString(3, packageName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long methodCount = rs.getLong("method_count");
                    long annotatedCount = rs.getLong("annotated_count");
                    double coverage = methodCount > 0 ? (annotatedCount * 100.0 / methodCount) : 0.0;

                    String className = rs.getString("class_name");
                    String pkgName = rs.getString("package_name");
                    String fullName = pkgName != null && !pkgName.isEmpty()
                            ? pkgName + "." + className
                            : className;

                    result.add(Map.of(
                            "type", "CLASS",
                            "id", rs.getLong("class_id"),
                            "name", className, // Simple class name
                            "fullName", fullName, // Fully qualified name
                            "methodCount", methodCount,
                            "annotatedCount", annotatedCount,
                            "coverageRate", coverage));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during class hierarchy query: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public List<TestMethodDetailRecord> findTestMethodDetailsWithFilters(
            List<Long> scanSessionIds,
            String teamName,
            String repositoryName,
            String packageName,
            String className,
            Boolean annotated,
            String searchTerm,
            String codePattern,
            Integer offset,
            Integer limit) {

        if (scanSessionIds == null || scanSessionIds.isEmpty()) {
            return new ArrayList<>();
        }

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
                WHERE tc.scan_session_id IN (
                """);

        // Add placeholders for IN clause
        for (int i = 0; i < scanSessionIds.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        List<Object> params = new ArrayList<>(scanSessionIds);

        // Search term filter (searches across multiple fields)
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(
                    " AND (LOWER(tm.method_name) LIKE LOWER(?) OR LOWER(tc.class_name) LIKE LOWER(?) OR LOWER(r.repository_name) LIKE LOWER(?) OR LOWER(tm.annotation_title) LIKE LOWER(?))");
            String searchPattern = "%" + searchTerm + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        // Team name filter (case-insensitive)
        if (teamName != null && !teamName.trim().isEmpty()) {
            sql.append(" AND LOWER(t.team_name) LIKE LOWER(?)");
            params.add("%" + teamName + "%");
        }

        // Repository name filter (case-insensitive)
        if (repositoryName != null && !repositoryName.trim().isEmpty()) {
            sql.append(" AND LOWER(r.repository_name) LIKE LOWER(?)");
            params.add("%" + repositoryName + "%");
        }

        // Package name filter (uses dedicated package_name column)
        if (packageName != null && !packageName.trim().isEmpty()) {
            sql.append(" AND LOWER(tc.package_name) LIKE LOWER(?)");
            params.add("%" + packageName + "%");
        }

        // Class name filter (uses class_name column - simple class name,
        // case-insensitive)
        if (className != null && !className.trim().isEmpty()) {
            sql.append(" AND LOWER(tc.class_name) LIKE LOWER(?)");
            params.add("%" + className + "%");
        }

        // Annotation status filter
        if (annotated != null) {
            if (annotated) {
                sql.append(" AND tm.annotation_title IS NOT NULL AND tm.annotation_title != ''");
            } else {
                sql.append(" AND (tm.annotation_title IS NULL OR tm.annotation_title = '')");
            }
        }

        // Code pattern filter (searches in target class, target method, and method body
        // content)
        if (codePattern != null && !codePattern.trim().isEmpty()) {
            sql.append(
                    " AND (LOWER(tm.annotation_target_class) LIKE LOWER(?) OR LOWER(tm.annotation_target_method) LIKE LOWER(?) OR LOWER(tm.method_body_content) LIKE LOWER(?))");
            String codePatternSearch = "%" + codePattern + "%";
            params.add(codePatternSearch);
            params.add(codePatternSearch);
            params.add(codePatternSearch);
        }

        sql.append(" ORDER BY r.repository_name, tc.class_name, tm.method_name");

        // Pagination
        if (limit != null && limit > 0) {
            sql.append(" LIMIT ?");
            params.add(limit);
        }
        if (offset != null && offset > 0) {
            sql.append(" OFFSET ?");
            params.add(offset);
        }

        List<TestMethodDetailRecord> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String repoName = rs.getString("repository_name");
                    String testClassName = rs.getString("class_name");
                    String testMethodName = rs.getString("method_name");
                    Integer lineNumber = rs.getInt("line_number");
                    String annotationTitle = rs.getString("annotation_title");
                    String annotationAuthor = rs.getString("annotation_author");
                    String annotationStatus = rs.getString("annotation_status");
                    String annotationTargetClass = rs.getString("annotation_target_class");
                    String annotationTargetMethod = rs.getString("annotation_target_method");
                    String annotationDescription = rs.getString("annotation_description");
                    String annotationTestPoints = rs.getString("annotation_test_points");

                    List<String> annotationTags = parseStringArray(rs.getString("annotation_tags"));
                    List<String> annotationRequirements = parseStringArray(rs.getString("annotation_requirements"));
                    List<String> annotationTestcases = parseStringArray(rs.getString("annotation_testcases"));
                    List<String> annotationDefects = parseStringArray(rs.getString("annotation_defects"));

                    String lastUpdateTime = rs.getString("annotation_last_update_time");
                    LocalDateTime lastUpdateDateTime = null;
                    if (lastUpdateTime != null && !lastUpdateTime.trim().isEmpty()) {
                        try {
                            lastUpdateDateTime = LocalDateTime.parse(lastUpdateTime);
                        } catch (Exception e) {
                            // Ignore parse errors
                        }
                    }

                    String annotationLastUpdateAuthor = rs.getString("annotation_last_update_author");
                    String teamNameResult = rs.getString("team_name");
                    String teamCode = rs.getString("team_code");
                    String gitUrl = rs.getString("git_url");

                    result.add(new TestMethodDetailRecord(
                            id, repoName, testClassName, testMethodName, lineNumber, annotationTitle, annotationAuthor,
                            annotationStatus,
                            annotationTargetClass, annotationTargetMethod, annotationDescription, annotationTestPoints,
                            annotationTags,
                            annotationRequirements, annotationTestcases, annotationDefects, lastUpdateDateTime,
                            annotationLastUpdateAuthor,
                            teamNameResult, teamCode, gitUrl));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public long countTestMethodDetailsWithFilters(
            List<Long> scanSessionIds,
            String teamName,
            String repositoryName,
            String packageName,
            String className,
            Boolean annotated,
            String searchTerm,
            String codePattern) {

        if (scanSessionIds == null || scanSessionIds.isEmpty()) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM test_methods tm
                JOIN test_classes tc ON tm.test_class_id = tc.id
                JOIN repositories r ON tc.repository_id = r.id
                LEFT JOIN teams t ON r.team_id = t.id
                WHERE tc.scan_session_id IN (
                """);

        // Add placeholders for IN clause
        for (int i = 0; i < scanSessionIds.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        List<Object> params = new ArrayList<>(scanSessionIds);

        // Search term filter
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(
                    " AND (LOWER(tm.method_name) LIKE LOWER(?) OR LOWER(tc.class_name) LIKE LOWER(?) OR LOWER(r.repository_name) LIKE LOWER(?) OR LOWER(tm.annotation_title) LIKE LOWER(?))");
            String searchPattern = "%" + searchTerm + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        // Team name filter
        if (teamName != null && !teamName.trim().isEmpty()) {
            sql.append(" AND LOWER(t.team_name) LIKE LOWER(?)");
            params.add("%" + teamName + "%");
        }

        // Repository name filter
        if (repositoryName != null && !repositoryName.trim().isEmpty()) {
            sql.append(" AND LOWER(r.repository_name) LIKE LOWER(?)");
            params.add("%" + repositoryName + "%");
        }

        // Package name filter
        if (packageName != null && !packageName.trim().isEmpty()) {
            sql.append(" AND LOWER(tc.package_name) LIKE LOWER(?)");
            params.add("%" + packageName + "%");
        }

        // Class name filter
        if (className != null && !className.trim().isEmpty()) {
            sql.append(" AND LOWER(tc.class_name) LIKE LOWER(?)");
            params.add("%" + className + "%");
        }

        // Annotation status filter
        if (annotated != null) {
            if (annotated) {
                sql.append(" AND tm.annotation_title IS NOT NULL AND tm.annotation_title != ''");
            } else {
                sql.append(" AND (tm.annotation_title IS NULL OR tm.annotation_title = '')");
            }
        }

        // Code pattern filter
        if (codePattern != null && !codePattern.trim().isEmpty()) {
            sql.append(
                    " AND (LOWER(tm.annotation_target_class) LIKE LOWER(?) OR LOWER(tm.annotation_target_method) LIKE LOWER(?) OR LOWER(tm.method_body_content) LIKE LOWER(?))");
            String codePatternSearch = "%" + codePattern + "%";
            params.add(codePatternSearch);
            params.add(codePatternSearch);
            params.add(codePatternSearch);
        }

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error during count query: " + e.getMessage(), e);
        }
    }

    }

    @Override
    public List<Map<String, Object>> getHierarchyByTeam(List<Long> scanSessionIds) {
        if (scanSessionIds == null || scanSessionIds.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder sql = new StringBuilder(
                """
                        SELECT
                            t.id as team_id,
                            t.team_name,
                            t.team_code,
                            COUNT(DISTINCT tc.id) as class_count,
                            COUNT(tm.id) as method_count,
                            SUM(CASE WHEN tm.annotation_title IS NOT NULL AND tm.annotation_title != '' THEN 1 ELSE 0 END) as annotated_count
                        FROM test_methods tm
                        JOIN test_classes tc ON tm.test_class_id = tc.id
                        JOIN repositories r ON tc.repository_id = r.id
                        LEFT JOIN teams t ON r.team_id = t.id
                        WHERE tc.scan_session_id IN (
                        """);

        // Add placeholders for IN clause
        for (int i = 0; i < scanSessionIds.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        sql.append("""
                GROUP BY t.id, t.team_name, t.team_code
                ORDER BY t.team_name
                """);

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < scanSessionIds.size(); i++) {
                stmt.setLong(i + 1, scanSessionIds.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long methodCount = rs.getLong("method_count");
                    long annotatedCount = rs.getLong("annotated_count");
                    double coverage = methodCount > 0 ? (annotatedCount * 100.0 / methodCount) : 0.0;

                    result.add(Map.of(
                            "type", "TEAM",
                            "id", rs.getObject("team_id") != null ? rs.getLong("team_id") : 0L,
                            "name", rs.getString("team_name") != null ? rs.getString("team_name") : "Unknown",
                            "code", rs.getString("team_code") != null ? rs.getString("team_code") : "",
                            "classCount", rs.getLong("class_count"),
                            "methodCount", methodCount,
                            "annotatedCount", annotatedCount,
                            "coverageRate", coverage));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during hierarchy query: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getHierarchyByPackage(List<Long> scanSessionIds, String teamName) {
        if (scanSessionIds == null || scanSessionIds.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder sql = new StringBuilder(
                """
                        SELECT
                            tc.package_name,
                            COUNT(DISTINCT tc.id) as class_count,
                            COUNT(tm.id) as method_count,
                            SUM(CASE WHEN tm.annotation_title IS NOT NULL AND tm.annotation_title != '' THEN 1 ELSE 0 END) as annotated_count
                        FROM test_methods tm
                        JOIN test_classes tc ON tm.test_class_id = tc.id
                        JOIN repositories r ON tc.repository_id = r.id
                        LEFT JOIN teams t ON r.team_id = t.id
                        WHERE tc.scan_session_id IN (
                        """);

        // Add placeholders for IN clause
        for (int i = 0; i < scanSessionIds.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        sql.append("""
                AND LOWER(t.team_name) = LOWER(?)
                AND tc.package_name IS NOT NULL
                AND tc.package_name != ''
                GROUP BY tc.package_name
                ORDER BY tc.package_name
                """);

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            for (Long id : scanSessionIds) {
                stmt.setLong(paramIndex++, id);
            }
            stmt.setString(paramIndex, teamName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String packageName = rs.getString("package_name");
                    if (packageName == null || packageName.isEmpty()) {
                        packageName = "(default package)";
                    }

                    long methodCount = rs.getLong("method_count");
                    long annotatedCount = rs.getLong("annotated_count");
                    double coverage = methodCount > 0 ? (annotatedCount * 100.0 / methodCount) : 0.0;

                    result.add(Map.of(
                            "type", "PACKAGE",
                            "name", packageName,
                            "classCount", rs.getLong("class_count"),
                            "methodCount", methodCount,
                            "annotatedCount", annotatedCount,
                            "coverageRate", coverage));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during package hierarchy query: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getHierarchyByClass(List<Long> scanSessionIds, String teamName,
            String packageName) {
        if (scanSessionIds == null || scanSessionIds.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder sql = new StringBuilder(
                """
                        SELECT
                            tc.id as class_id,
                            tc.class_name,
                            tc.package_name,
                            COUNT(tm.id) as method_count,
                            SUM(CASE WHEN tm.annotation_title IS NOT NULL AND tm.annotation_title != '' THEN 1 ELSE 0 END) as annotated_count
                        FROM test_methods tm
                        JOIN test_classes tc ON tm.test_class_id = tc.id
                        JOIN repositories r ON tc.repository_id = r.id
                        LEFT JOIN teams t ON r.team_id = t.id
                        WHERE tc.scan_session_id IN (
                        """);

        // Add placeholders for IN clause
        for (int i = 0; i < scanSessionIds.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        sql.append("""
                AND LOWER(t.team_name) = LOWER(?)
                AND LOWER(tc.package_name) = LOWER(?)
                GROUP BY tc.id, tc.class_name, tc.package_name
                ORDER BY tc.class_name
                """);

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            for (Long id : scanSessionIds) {
                stmt.setLong(paramIndex++, id);
            }
            stmt.setString(paramIndex++, teamName);
            stmt.setString(paramIndex, packageName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long methodCount = rs.getLong("method_count");
                    long annotatedCount = rs.getLong("annotated_count");
                    double coverage = methodCount > 0 ? (annotatedCount * 100.0 / methodCount) : 0.0;

                    String className = rs.getString("class_name");
                    String pkgName = rs.getString("package_name");
                    String fullName = pkgName != null && !pkgName.isEmpty()
                            ? pkgName + "." + className
                            : className;

                    result.add(Map.of(
                            "type", "CLASS",
                            "id", rs.getLong("class_id"),
                            "name", className,
                            "fullName", fullName,
                            "methodCount", methodCount,
                            "annotatedCount", annotatedCount,
                            "coverageRate", coverage));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during class hierarchy query: " + e.getMessage(), e);
        }

        return result;
    }
}
