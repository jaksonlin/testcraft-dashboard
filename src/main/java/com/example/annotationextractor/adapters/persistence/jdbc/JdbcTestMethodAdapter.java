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
    public List<TestMethodDetailRecord> findTestMethodDetailsByTeamIdAndScanSessionId(Long teamId, Long scanSessionId, Integer limit) {
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
                        annotationLastUpdateAuthor, teamName, teamCode, gitUrl
                    ));
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
    public List<TestMethodDetailRecord> findTestMethodDetailsByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId, Integer limit) {
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
                        annotationLastUpdateAuthor, teamName, teamCode, gitUrl
                    ));
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
                        annotationLastUpdateAuthor, teamName, teamCode, gitUrl
                    ));
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
                        annotationLastUpdateAuthor, teamName, teamCode, gitUrl
                    ));
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
            scanSessionId
        );
    }
}


