package com.example.annotationextractor.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * Tests for DatabaseSchemaManager class
 */
public class DatabaseSchemaManagerTest {

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 5432;
    private static final String TEST_DATABASE = "test_analytics_test";
    private static final String TEST_USERNAME = "postgres";
    private static final String TEST_PASSWORD = "postgres";

    @Before
    public void setUp() throws SQLException {
        // Initialize database connection only
        DatabaseConfig.initialize(TEST_HOST, TEST_PORT, TEST_DATABASE, TEST_USERNAME, TEST_PASSWORD);
        // Clean up any existing tables from previous test runs
        try {
            DatabaseSchemaManager.dropAllTables();
        } catch (SQLException e) {
            // Ignore cleanup errors
        }
    }

    @After
    public void tearDown() {
        // Clean up tables after each test
        try {
            DatabaseSchemaManager.dropAllTables();
        } catch (SQLException e) {
            // Ignore cleanup errors
        }
        DatabaseConfig.close();
    }

    @Test
    public void testInitializeSchema() throws SQLException {
        // Test schema initialization
        DatabaseSchemaManager.initializeSchema();
        
        // Debug: Check what tables actually exist after initialization
        System.out.println("=== DEBUG: Checking tables after initialization ===");
        try (Connection conn = DatabaseConfig.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", null);
            System.out.println("Tables found:");
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                String schemaName = tables.getString("TABLE_SCHEM");
                System.out.println("  - " + schemaName + "." + tableName);
            }
        }
        
        // Debug: Try to create a table directly to see if it works
        System.out.println("=== DEBUG: Testing direct table creation ===");
        try (Connection conn = DatabaseConfig.getConnection()) {
            Statement stmt = conn.createStatement();
            
            // Try to create a simple test table
            stmt.execute("CREATE TABLE IF NOT EXISTS public.test_table (id INT PRIMARY KEY)");
            System.out.println("Direct table creation successful");
            
            // Check if it exists
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'test_table' AND table_schema = 'public'");
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Test table count: " + count);
            }
            
            // Clean up
            stmt.execute("DROP TABLE IF EXISTS public.test_table");
        }
        
        // Verify all tables were created
        assertTrue("Repositories table should exist", tableExists("repositories"));
        assertTrue("Scan sessions table should exist", tableExists("scan_sessions"));
        assertTrue("Test classes table should exist", tableExists("test_classes"));
        assertTrue("Test methods table should exist", tableExists("test_methods"));
        assertTrue("Daily metrics table should exist", tableExists("daily_metrics"));
    }

    @Test
    public void testSchemaExists() {
        // Test schema existence check before initialization
        assertFalse("Schema should not exist before initialization", 
                   DatabaseSchemaManager.schemaExists());
        
        try {
            // Initialize schema
            DatabaseSchemaManager.initializeSchema();
            
            // Test schema existence check after initialization
            assertTrue("Schema should exist after initialization", 
                      DatabaseSchemaManager.schemaExists());
        } catch (SQLException e) {
            fail("Schema initialization should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDropAllTables() throws SQLException {
        // First initialize schema
        DatabaseSchemaManager.initializeSchema();
        
        // Verify tables exist
        assertTrue("Tables should exist before dropping", DatabaseSchemaManager.schemaExists());
        
        // Drop all tables
        DatabaseSchemaManager.dropAllTables();
        
        // Verify tables were dropped
        assertFalse("Tables should not exist after dropping", DatabaseSchemaManager.schemaExists());
    }

    @Test
    public void testTableStructure() throws SQLException {
        // Initialize schema
        DatabaseSchemaManager.initializeSchema();
        
        // Test repositories table structure
        assertTrue("Repositories table should have correct columns", 
                  hasRequiredColumns("repositories", 
                                   new String[]{"id", "repository_name", "repository_path", 
                                               "git_url", "git_branch", "technology_stack", 
                                               "team_id", "first_scan_date", 
                                               "last_scan_date", "total_test_classes", 
                                               "total_test_methods", "total_annotated_methods", 
                                               "annotation_coverage_rate"}));
        
        // Test scan_sessions table structure
        assertTrue("Scan sessions table should have correct columns", 
                  hasRequiredColumns("scan_sessions", 
                                   new String[]{"id", "scan_date", "scan_directory", 
                                               "total_repositories", "total_test_classes", 
                                               "total_test_methods", "total_annotated_methods", 
                                               "scan_duration_ms", "scan_status", "error_log", "metadata"}));
        
        // Test test_classes table structure
        assertTrue("Test classes table should have correct columns", 
                  hasRequiredColumns("test_classes", 
                                   new String[]{"id", "repository_id", "class_name", 
                                               "package_name", "file_path", "file_size_bytes", 
                                               "total_test_methods", "annotated_test_methods", 
                                               "coverage_rate", "first_seen_date", 
                                               "last_modified_date", "scan_session_id"}));
        
        // Test test_methods table structure
        assertTrue("Test methods table should have correct columns", 
                  hasRequiredColumns("test_methods", 
                                   new String[]{"id", "test_class_id", "method_name", 
                                               "method_signature", "line_number", "has_annotation", 
                                               "annotation_data", "annotation_title", 
                                               "annotation_author", "annotation_status", 
                                               "annotation_target_class", "annotation_target_method", 
                                               "annotation_description", "annotation_tags", 
                                               "annotation_test_points", "annotation_requirements", 
                                               "annotation_defects", "annotation_testcases",
                                               "first_seen_date", "last_modified_date", "scan_session_id"}));
        
        // Test daily_metrics table structure
        assertTrue("Daily metrics table should have correct columns", 
                  hasRequiredColumns("daily_metrics", 
                                   new String[]{"id", "metric_date", "total_repositories", 
                                               "total_test_classes", "total_test_methods", 
                                               "total_annotated_methods", "overall_coverage_rate", 
                                               "new_test_methods", "new_annotated_methods"}));
        
        // Test teams table structure
        assertTrue("Teams table should have correct columns", 
                  hasRequiredColumns("teams", 
                                   new String[]{"id", "team_name", "team_code", 
                                               "department", "created_date"}));
    }

    @Test
    public void testIndexes() throws SQLException {
        // Initialize schema
        DatabaseSchemaManager.initializeSchema();
        
        // Verify indexes were created
        assertTrue("Repository name index should exist", indexExists("idx_repositories_name", "repositories"));
        assertTrue("Repository team_id index should exist", indexExists("idx_repositories_team_id", "repositories"));
        assertTrue("Test classes repository index should exist", indexExists("idx_test_classes_repo", "test_classes"));
        assertTrue("Test methods class index should exist", indexExists("idx_test_methods_class", "test_methods"));
        assertTrue("Scan sessions date index should exist", indexExists("idx_scan_sessions_date", "scan_sessions"));
        assertTrue("Daily metrics date index should exist", indexExists("idx_daily_metrics_date", "daily_metrics"));
        assertTrue("Annotation data GIN index should exist", indexExists("idx_annotation_data", "test_methods"));
    }

    @Test
    public void testReinitializeSchema() throws SQLException {
        // Initialize schema first time
        DatabaseSchemaManager.initializeSchema();
        
        // Verify tables exist
        assertTrue("Schema should exist after first initialization", 
                  DatabaseSchemaManager.schemaExists());
        
        // Initialize schema again (should not fail)
        DatabaseSchemaManager.initializeSchema();
        
        // Verify tables still exist
        assertTrue("Schema should still exist after reinitialization", 
                  DatabaseSchemaManager.schemaExists());
    }

    @Test
    public void testTableConstraints() throws SQLException {
        // Initialize schema
        DatabaseSchemaManager.initializeSchema();
        
        // Test that we can insert and retrieve data (basic constraint validation)
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Test repositories table constraints
            stmt.execute("INSERT INTO repositories (repository_name, repository_path, git_url) VALUES ('test-repo', '/test/path', 'https://github.com/test-repo')");
            
            ResultSet rs = stmt.executeQuery("SELECT repository_name FROM repositories WHERE repository_name = 'test-repo'");
            assertTrue("Should be able to retrieve inserted repository", rs.next());
            assertEquals("Repository name should match", "test-repo", rs.getString("repository_name"));
            
            // Test unique constraint
            try {
                stmt.execute("INSERT INTO repositories (repository_name, repository_path, git_url) VALUES ('test-repo', '/test/path', 'https://github.com/test-repo')");
                fail("Should not allow duplicate repository name and path");
            } catch (SQLException e) {
                // Expected - unique constraint violation
                assertTrue("Should be constraint violation", e.getMessage().contains("duplicate") || 
                          e.getMessage().contains("unique") || e.getMessage().contains("constraint"));
            }
        }
    }

    /**
     * Helper method to check if a table exists
     */
    private boolean tableExists(String tableName) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, "public", tableName, null)) {
            return rs.next();
        }
    }

    /**
     * Helper method to check if an index exists
     */
    private boolean indexExists(String indexName, String tableName) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             ResultSet rs = conn.getMetaData().getIndexInfo(null, null, tableName, false, false)) {
            while (rs.next()) {
                String indexNameFromDb = rs.getString("INDEX_NAME");
                if (indexName.equals(indexNameFromDb)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Helper method to check if a table has required columns
     */
    private boolean hasRequiredColumns(String tableName, String[] requiredColumns) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, null)) {
            
            java.util.Set<String> foundColumns = new java.util.HashSet<>();
            while (rs.next()) {
                foundColumns.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
            
            for (String requiredColumn : requiredColumns) {
                if (!foundColumns.contains(requiredColumn.toLowerCase())) {
                    return false;
                }
            }
            return true;
        }
    }
}
