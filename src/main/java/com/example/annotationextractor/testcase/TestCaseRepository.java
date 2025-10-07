package com.example.annotationextractor.testcase;

import com.example.annotationextractor.database.DatabaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for test case database operations using JDBC.
 * Follows the project's pattern of direct JDBC usage with Spring DI.
 */
@Repository
public class TestCaseRepository {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Save a single test case
     */
    public void save(TestCase testCase) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            save(conn, testCase);
        }
    }
    
    /**
     * Save multiple test cases in a transaction
     */
    public int saveAll(List<TestCase> testCases) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (TestCase testCase : testCases) {
                    save(conn, testCase);
                }
                conn.commit();
                return testCases.size();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    
    /**
     * Internal save method with connection
     */
    private void save(Connection conn, TestCase testCase) throws SQLException {
        String sql = "INSERT INTO test_cases (id, title, steps, setup, teardown, expected_result, " +
                    "priority, type, status, tags, requirements, custom_fields, created_by, organization) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?) " +
                    "ON CONFLICT (id) DO UPDATE SET " +
                    "title = EXCLUDED.title, steps = EXCLUDED.steps, setup = EXCLUDED.setup, " +
                    "teardown = EXCLUDED.teardown, expected_result = EXCLUDED.expected_result, " +
                    "priority = EXCLUDED.priority, type = EXCLUDED.type, status = EXCLUDED.status, " +
                    "tags = EXCLUDED.tags, requirements = EXCLUDED.requirements, " +
                    "custom_fields = EXCLUDED.custom_fields, updated_date = CURRENT_TIMESTAMP";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, testCase.getId());
            stmt.setString(2, testCase.getTitle());
            stmt.setString(3, testCase.getSteps());
            stmt.setString(4, testCase.getSetup());
            stmt.setString(5, testCase.getTeardown());
            stmt.setString(6, testCase.getExpectedResult());
            stmt.setString(7, testCase.getPriority());
            stmt.setString(8, testCase.getType());
            stmt.setString(9, testCase.getStatus());
            
            // Array types
            Array tagsArray = conn.createArrayOf("TEXT", testCase.getTags());
            stmt.setArray(10, tagsArray);
            
            Array reqsArray = conn.createArrayOf("TEXT", testCase.getRequirements());
            stmt.setArray(11, reqsArray);
            
            // JSONB custom fields
            String customFieldsJson = objectMapper.writeValueAsString(testCase.getCustomFields());
            stmt.setString(12, customFieldsJson);
            
            stmt.setString(13, testCase.getCreatedBy());
            stmt.setString(14, testCase.getOrganization());
            
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new SQLException("Failed to save test case: " + testCase.getId(), e);
        }
    }
    
    /**
     * Find test case by ID
     */
    public TestCase findById(String id) throws SQLException {
        String sql = "SELECT * FROM test_cases WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTestCase(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find all test cases
     */
    public List<TestCase> findAll() throws SQLException {
        return findAll(null, null, null);
    }
    
    /**
     * Find test cases with filters
     */
    public List<TestCase> findAll(String organization, String type, String priority) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM test_cases WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (organization != null) {
            sql.append(" AND organization = ?");
            params.add(organization);
        }
        if (type != null) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        if (priority != null) {
            sql.append(" AND priority = ?");
            params.add(priority);
        }
        
        sql.append(" ORDER BY id");
        
        List<TestCase> testCases = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    testCases.add(mapResultSetToTestCase(rs));
                }
            }
        }
        
        return testCases;
    }
    
    /**
     * Count total test cases
     */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM test_cases";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Count test cases with coverage (linked to test methods)
     */
    public int countWithCoverage() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT test_case_id) FROM test_case_coverage";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Find test cases without coverage (not linked to any test methods)
     */
    public List<TestCase> findWithoutCoverage() throws SQLException {
        String sql = "SELECT tc.* FROM test_cases tc " +
                    "LEFT JOIN test_case_coverage tcc ON tc.id = tcc.test_case_id " +
                    "WHERE tcc.test_case_id IS NULL " +
                    "ORDER BY tc.priority DESC, tc.id";
        
        List<TestCase> testCases = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                testCases.add(mapResultSetToTestCase(rs));
            }
        }
        
        return testCases;
    }
    
    /**
     * Link test case to test method (create coverage record)
     */
    public void linkTestCaseToMethod(String testCaseId, String repositoryName, 
                                     String packageName, String className, String methodName,
                                     String filePath, int lineNumber) throws SQLException {
        String sql = "INSERT INTO test_case_coverage " +
                    "(test_case_id, repository_name, package_name, class_name, method_name, file_path, line_number) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (test_case_id, repository_name, package_name, class_name, method_name) " +
                    "DO UPDATE SET file_path = EXCLUDED.file_path, line_number = EXCLUDED.line_number, " +
                    "scan_date = CURRENT_TIMESTAMP";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, testCaseId);
            stmt.setString(2, repositoryName);
            stmt.setString(3, packageName);
            stmt.setString(4, className);
            stmt.setString(5, methodName);
            stmt.setString(6, filePath);
            stmt.setInt(7, lineNumber);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete test case by ID
     */
    public boolean deleteById(String id) throws SQLException {
        String sql = "DELETE FROM test_cases WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }
    
    /**
     * Delete all test cases (for testing or reset)
     */
    public int deleteAll() throws SQLException {
        String sql = "DELETE FROM test_cases";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            return stmt.executeUpdate();
        }
    }
    
    /**
     * Map ResultSet to TestCase object
     */
    private TestCase mapResultSetToTestCase(ResultSet rs) throws SQLException {
        TestCase testCase = new TestCase();
        
        testCase.setId(rs.getString("id"));
        testCase.setTitle(rs.getString("title"));
        testCase.setSteps(rs.getString("steps"));
        testCase.setSetup(rs.getString("setup"));
        testCase.setTeardown(rs.getString("teardown"));
        testCase.setExpectedResult(rs.getString("expected_result"));
        testCase.setPriority(rs.getString("priority"));
        testCase.setType(rs.getString("type"));
        testCase.setStatus(rs.getString("status"));
        testCase.setCreatedBy(rs.getString("created_by"));
        testCase.setOrganization(rs.getString("organization"));
        
        // Arrays
        Array tagsArray = rs.getArray("tags");
        if (tagsArray != null) {
            testCase.setTags((String[]) tagsArray.getArray());
        }
        
        Array reqsArray = rs.getArray("requirements");
        if (reqsArray != null) {
            testCase.setRequirements((String[]) reqsArray.getArray());
        }
        
        // JSONB custom fields
        String customFieldsJson = rs.getString("custom_fields");
        if (customFieldsJson != null) {
            try {
                Map<String, Object> customFields = objectMapper.readValue(
                    customFieldsJson, 
                    objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class)
                );
                testCase.setCustomFields(customFields);
            } catch (Exception e) {
                testCase.setCustomFields(new HashMap<>());
            }
        }
        
        // Timestamps
        Timestamp created = rs.getTimestamp("created_date");
        if (created != null) {
            testCase.setCreatedDate(created.toLocalDateTime());
        }
        
        Timestamp updated = rs.getTimestamp("updated_date");
        if (updated != null) {
            testCase.setUpdatedDate(updated.toLocalDateTime());
        }
        
        return testCase;
    }
}

