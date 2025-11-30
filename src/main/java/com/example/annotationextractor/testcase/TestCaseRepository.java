package com.example.annotationextractor.testcase;

import com.example.annotationextractor.casemodel.TestMethodInfo;
import com.example.annotationextractor.casemodel.UnittestCaseInfoData;
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
     * Returns SaveResult with breakdown of created vs updated
     */
    public SaveResult saveAll(List<TestCase> testCases) throws SQLException {
        int created = 0;
        int updated = 0;
        int skipped = 0;

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (TestCase testCase : testCases) {
                    try {
                        boolean isNew = save(conn, testCase);
                        if (isNew) {
                            created++;
                        } else {
                            updated++;
                        }
                    } catch (SQLException rowEx) {
                        // Skip this row if it violates DB constraints or other errors occur
                        skipped++;
                        // Optionally log the failure for diagnostics
                        System.err.println("Skipping test case due to DB error (externalId=" + testCase.getExternalId()
                                + "): " + rowEx.getMessage());
                        // Continue with next row without failing entire batch
                    }
                }
                conn.commit();
                return new SaveResult(created, updated, skipped);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Result of save operation
     */
    public static class SaveResult {
        private final int created;
        private final int updated;
        private final int skipped;

        public SaveResult(int created, int updated, int skipped) {
            this.created = created;
            this.updated = updated;
            this.skipped = skipped;
        }

        public int getCreated() {
            return created;
        }

        public int getUpdated() {
            return updated;
        }

        public int getSkipped() {
            return skipped;
        }

        public int getTotal() {
            return created + updated;
        }
    }

    /**
     * Internal save method with connection
     * Uses external_id + organization for conflict resolution
     * Returns true if new record was created, false if existing record was updated
     */
    private boolean save(Connection conn, TestCase testCase) throws SQLException {
        // Check if test case already exists
        boolean exists = false;
        String checkSql = "SELECT internal_id FROM test_cases WHERE external_id = ? AND organization = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, testCase.getExternalId());
            checkStmt.setString(2, testCase.getOrganization());
            try (ResultSet rs = checkStmt.executeQuery()) {
                exists = rs.next();
            }
        }

        // Perform upsert
        String sql = "INSERT INTO test_cases (external_id, title, steps, setup, teardown, expected_result, " +
                "priority, type, status, tags, requirements, custom_fields, created_by, organization, team_id, team_name) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?) " +
                "ON CONFLICT (external_id, organization) DO UPDATE SET " +
                "title = EXCLUDED.title, steps = EXCLUDED.steps, setup = EXCLUDED.setup, " +
                "teardown = EXCLUDED.teardown, expected_result = EXCLUDED.expected_result, " +
                "priority = EXCLUDED.priority, type = EXCLUDED.type, status = EXCLUDED.status, " +
                "tags = EXCLUDED.tags, requirements = EXCLUDED.requirements, " +
                "custom_fields = EXCLUDED.custom_fields, team_id = EXCLUDED.team_id, team_name = EXCLUDED.team_name, " +
                "updated_date = CURRENT_TIMESTAMP " +
                "RETURNING internal_id";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, testCase.getExternalId());
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

            // Team ID (nullable)
            if (testCase.getTeamId() != null) {
                stmt.setLong(15, testCase.getTeamId());
            } else {
                stmt.setNull(15, java.sql.Types.BIGINT);
            }

            // Team Name (nullable, denormalized)
            stmt.setString(16, testCase.getTeamName());

            // Execute and get generated internal_id
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    testCase.setInternalId(rs.getLong("internal_id"));
                }
            }
        } catch (Exception e) {
            throw new SQLException("Failed to save test case: " + testCase.getExternalId(), e);
        }

        return !exists; // true if it was a new record, false if it was an update
    }

    /**
     * Find test case by internal ID (primary key)
     */
    public TestCase findById(Long internalId) throws SQLException {
        String sql = "SELECT tc.*, COALESCE(t.team_name, tc.team_name) as team_name FROM test_cases tc " +
                "LEFT JOIN teams t ON tc.team_id = t.id " +
                "WHERE tc.internal_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, internalId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTestCase(rs);
                }
            }
        }

        return null;
    }

    /**
     * Find test case by external ID (from test management system)
     * Note: External ID may not be unique across organizations
     */
    public TestCase findByExternalId(String externalId, String organization) throws SQLException {
        String sql = "SELECT * FROM test_cases WHERE external_id = ? AND organization = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, externalId);
            stmt.setString(2, organization);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTestCase(rs);
                }
            }
        }

        return null;
    }

    /**
     * Legacy method - finds by external ID (deprecated, use findById with internal
     * ID)
     */
    @Deprecated
    public TestCase findByIdLegacy(String externalId) throws SQLException {
        String sql = "SELECT * FROM test_cases WHERE external_id = ? LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, externalId);

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
        return findAll(organization, type, priority, null);
    }

    /**
     * Find test cases with filters including team
     */
    public List<TestCase> findAll(String organization, String type, String priority, Long teamId) throws SQLException {
        return findAll(organization, type, priority, teamId, null, null);
    }

    /**
     * Find test cases with all filters including status and search
     */
    public List<TestCase> findAll(String organization, String type, String priority, Long teamId, String status,
            String search) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT tc.*, COALESCE(t.team_name, tc.team_name) as team_name FROM test_cases tc " +
                        "LEFT JOIN teams t ON tc.team_id = t.id " +
                        "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (organization != null) {
            sql.append(" AND tc.organization = ?");
            params.add(organization);
        }
        if (type != null) {
            sql.append(" AND tc.type = ?");
            params.add(type);
        }
        if (priority != null) {
            sql.append(" AND tc.priority = ?");
            params.add(priority);
        }
        if (teamId != null) {
            sql.append(" AND tc.team_id = ?");
            params.add(teamId);
        }
        if (status != null) {
            sql.append(" AND tc.status = ?");
            params.add(status);
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(tc.external_id) LIKE ? OR LOWER(tc.title) LIKE ?)");
            String searchPattern = "%" + search.toLowerCase() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        sql.append(" ORDER BY tc.internal_id");

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
     * Find test cases with pagination
     */
    public List<TestCase> findAllPaged(String organization, String type, String priority, int offset, int limit)
            throws SQLException {
        return findAllPaged(organization, type, priority, null, offset, limit);
    }

    /**
     * Find test cases with pagination including team filter
     */
    public List<TestCase> findAllPaged(String organization, String type, String priority, Long teamId, int offset,
            int limit) throws SQLException {
        return findAllPaged(organization, type, priority, teamId, null, null, offset, limit);
    }

    /**
     * Find test cases with pagination including all filters
     */
    public List<TestCase> findAllPaged(String organization, String type, String priority, Long teamId, String status,
            String search, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT tc.*, COALESCE(t.team_name, tc.team_name) as team_name FROM test_cases tc " +
                        "LEFT JOIN teams t ON tc.team_id = t.id " +
                        "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (organization != null) {
            sql.append(" AND tc.organization = ?");
            params.add(organization);
        }
        if (type != null) {
            sql.append(" AND tc.type = ?");
            params.add(type);
        }
        if (priority != null) {
            sql.append(" AND tc.priority = ?");
            params.add(priority);
        }
        if (teamId != null) {
            sql.append(" AND tc.team_id = ?");
            params.add(teamId);
        }
        if (status != null) {
            sql.append(" AND tc.status = ?");
            params.add(status);
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(tc.external_id) LIKE ? OR LOWER(tc.title) LIKE ?)");
            String searchPattern = "%" + search.toLowerCase() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        sql.append(" ORDER BY tc.internal_id OFFSET ? LIMIT ?");
        params.add(offset);
        params.add(limit);

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
        return countAll(null, null, null, null);
    }

    /**
     * Count test cases with filters
     */
    public int countAll(String organization, String type, String priority, Long teamId) throws SQLException {
        return countAll(organization, type, priority, teamId, null, null);
    }

    /**
     * Count test cases with all filters
     */
    public int countAll(String organization, String type, String priority, Long teamId, String status, String search)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM test_cases WHERE 1=1");
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
        if (teamId != null) {
            sql.append(" AND team_id = ?");
            params.add(teamId);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(external_id) LIKE ? OR LOWER(title) LIKE ?)");
            String searchPattern = "%" + search.toLowerCase() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    /**
     * Count test cases with coverage (linked to test methods)
     */
    public int countWithCoverage() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT test_case_internal_id) FROM test_case_coverage";

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
     * Count test cases without coverage
     */
    public int countWithoutCoverage() throws SQLException {
        String sql = "SELECT COUNT(*) FROM test_cases tc " +
                "LEFT JOIN test_case_coverage tcc ON tc.internal_id = tcc.test_case_internal_id " +
                "WHERE tcc.test_case_internal_id IS NULL";

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
                "LEFT JOIN test_case_coverage tcc ON tc.internal_id = tcc.test_case_internal_id " +
                "WHERE tcc.test_case_internal_id IS NULL " +
                "ORDER BY tc.priority DESC, tc.internal_id";

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
     * Find untested cases with pagination
     */
    public List<TestCase> findWithoutCoveragePaged(int offset, int limit) throws SQLException {
        String sql = "SELECT tc.* FROM test_cases tc " +
                "LEFT JOIN test_case_coverage tcc ON tc.internal_id = tcc.test_case_internal_id " +
                "WHERE tcc.test_case_internal_id IS NULL " +
                "ORDER BY tc.priority DESC, tc.internal_id OFFSET ? LIMIT ?";

        List<TestCase> testCases = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, offset);
            stmt.setInt(2, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    testCases.add(mapResultSetToTestCase(rs));
                }
            }
        }

        return testCases;
    }

    /**
     * Link test case to test method (create coverage record)
     * Uses internal ID to link test case to implementation
     */
    public void linkTestCaseToMethod(Long testCaseInternalId, String repositoryName,
            String packageName, String className, String methodName,
            String filePath, int lineNumber) throws SQLException {
        String sql = "INSERT INTO test_case_coverage " +
                "(test_case_internal_id, repository_name, package_name, class_name, method_name, file_path, line_number) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (test_case_internal_id, repository_name, package_name, class_name, method_name) " +
                "DO UPDATE SET file_path = EXCLUDED.file_path, line_number = EXCLUDED.line_number, " +
                "scan_date = CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, testCaseInternalId);
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
     * Delete test case by internal ID
     */
    public boolean deleteById(Long internalId) throws SQLException {
        String sql = "DELETE FROM test_cases WHERE internal_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, internalId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * Delete test case by external ID and organization
     */
    public boolean deleteByExternalId(String externalId, String organization) throws SQLException {
        String sql = "DELETE FROM test_cases WHERE external_id = ? AND organization = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, externalId);
            stmt.setString(2, organization);
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
     * Delete all test cases matching filters (for bulk operations)
     * WARNING: This performs bulk deletion - use with caution!
     * Returns the number of deleted records.
     */
    public int deleteAllWithFilters(String organization, String type, String priority,
            Long teamId, String status, String search) throws SQLException {
        StringBuilder sql = new StringBuilder("DELETE FROM test_cases WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Apply filters (same logic as findAll)
        if (organization != null && !organization.trim().isEmpty()) {
            sql.append(" AND organization = ?");
            params.add(organization);
        }

        if (type != null && !type.trim().isEmpty()) {
            sql.append(" AND type = ?");
            params.add(type);
        }

        if (priority != null && !priority.trim().isEmpty()) {
            sql.append(" AND priority = ?");
            params.add(priority);
        }

        if (teamId != null) {
            sql.append(" AND team_id = ?");
            params.add(teamId);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(external_id) LIKE LOWER(?) OR LOWER(title) LIKE LOWER(?))");
            String searchPattern = "%" + search + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            int deleted = stmt.executeUpdate();
            System.out.println("Bulk delete: removed " + deleted + " test cases with filters: " +
                    "org=" + organization + ", team=" + teamId + ", type=" + type +
                    ", priority=" + priority + ", status=" + status + ", search=" + search);

            return deleted;
        }
    }

    /**
     * Get distinct organization values from test cases
     */
    public List<String> findDistinctOrganizations() throws SQLException {
        String sql = "SELECT DISTINCT organization FROM test_cases WHERE organization IS NOT NULL ORDER BY organization";
        List<String> organizations = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String org = rs.getString("organization");
                if (org != null && !org.trim().isEmpty()) {
                    organizations.add(org);
                }
            }
        }

        return organizations;
    }

    /**
     * Get all teams for filter dropdown (returns id and name)
     */
    public List<Map<String, Object>> findAllTeams() throws SQLException {
        String sql = "SELECT id, team_name FROM teams ORDER BY team_name";
        List<Map<String, Object>> teams = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> team = new HashMap<>();
                team.put("id", rs.getLong("id"));
                team.put("name", rs.getString("team_name"));
                teams.add(team);
            }
        }

        return teams;
    }

    /**
     * Look up team ID by team name
     * Returns null if team not found
     */
    public Long findTeamIdByName(String teamName) throws SQLException {
        if (teamName == null || teamName.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id FROM teams WHERE LOWER(team_name) = LOWER(?)";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, teamName.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        return null;
    }

    /**
     * Map ResultSet to TestCase object
     */
    private TestCase mapResultSetToTestCase(ResultSet rs) throws SQLException {
        TestCase testCase = new TestCase();

        testCase.setInternalId(rs.getLong("internal_id"));
        testCase.setExternalId(rs.getString("external_id"));
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

        // Team fields
        Long teamId = rs.getLong("team_id");
        if (!rs.wasNull()) {
            testCase.setTeamId(teamId);
        }
        testCase.setTeamName(rs.getString("team_name"));

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
                        objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
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

    /**
     * Fetch all test methods that have annotations
     * Used for refreshing coverage data
     */
    public List<TestMethodInfo> fetchAnnotatedTestMethods() throws SQLException {
        String sql = "SELECT tm.*, r.repository_name, tc.package_name, tc.class_name, tc.file_path " +
                "FROM test_methods tm " +
                "JOIN test_classes tc ON tm.test_class_id = tc.id " +
                "JOIN repositories r ON tc.repository_id = r.id " +
                "WHERE tm.has_annotation = true";

        List<TestMethodInfo> methods = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                TestMethodInfo method = new TestMethodInfo();
                method.setMethodName(rs.getString("method_name"));
                method.setClassName(rs.getString("class_name"));
                method.setPackageName(rs.getString("package_name"));
                method.setFilePath(rs.getString("file_path"));
                method.setLineNumber(rs.getInt("line_number"));
                method.setMethodLoc(rs.getInt("method_loc"));
                method.setMethodBodyContent(rs.getString("method_body_content"));

                // Parse annotation data
                String annotationJson = rs.getString("annotation_data");
                if (annotationJson != null) {
                    try {
                        UnittestCaseInfoData data = objectMapper.readValue(annotationJson, UnittestCaseInfoData.class);
                        method.setAnnotationData(data);

                        // Extract test case IDs from annotation data
                        // This logic mirrors what's in TestClassParser
                        List<String> ids = new ArrayList<>();
                        if (data.getRelatedTestcases() != null) {
                            for (String id : data.getRelatedTestcases()) {
                                if (id != null && !id.trim().isEmpty()) {
                                    ids.add(id.trim());
                                }
                            }
                        }
                        method.setTestCaseIds(ids.toArray(new String[0]));

                    } catch (Exception e) {
                        // Log error but continue
                        System.err.println("Error parsing annotation JSON for method " + method.getMethodName() + ": "
                                + e.getMessage());
                    }
                }

                methods.add(method);
            }
        }

        return methods;
    }
}
