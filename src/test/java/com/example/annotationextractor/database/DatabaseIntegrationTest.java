package com.example.annotationextractor.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * Integration tests for the database layer
 */
public class DatabaseIntegrationTest {

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 5432;
    private static final String TEST_DATABASE = "test_analytics_test";
    private static final String TEST_USERNAME = "postgres";
    private static final String TEST_PASSWORD = "postgres";

    @Before
    public void setUp() {
        // Initialize database connection
        DatabaseConfig.initialize(TEST_HOST, TEST_PORT, TEST_DATABASE, TEST_USERNAME, TEST_PASSWORD);
    }

    @After
    public void tearDown() {
        // Clean up database - Flyway handles this through clean operation
        try {
            Flyway flyway = Flyway.configure()
                .dataSource(DatabaseConfig.getDataSource())
                .cleanDisabled(false) // Enable clean for tests
                .load();
            flyway.clean(); // This will drop all tables
        } catch (Exception e) {
            // Ignore cleanup errors
        }
        DatabaseConfig.close();
    }

    @Test
    public void testDatabaseConnectionAndSchema() throws SQLException {
        // Test database connection
        try (Connection conn = DatabaseConfig.getConnection()) {
            assertNotNull("Database connection should not be null", conn);
            assertFalse("Database connection should not be closed", conn.isClosed());
            assertTrue("Database connection should be valid", conn.isValid(5));
        }
        
        // Test schema initialization with Flyway
        Flyway flyway = Flyway.configure()
            .dataSource(DatabaseConfig.getDataSource())
            .load();
        flyway.migrate();
        
        // Verify schema exists by checking if tables exist
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'repositories')");
            assertTrue("Schema should exist after Flyway migration", rs.next() && rs.getBoolean(1));
        }
        
        // Test basic table operations
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Test repositories table
            stmt.execute("INSERT INTO repositories (repository_name, repository_path) VALUES ('test-repo', '/test/path')");
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM repositories WHERE repository_name = 'test-repo'");
            rs.next();
            assertEquals("Should have one test repository", 1, rs.getInt(1));
            
            // Test scan_sessions table
            stmt.execute("INSERT INTO scan_sessions (scan_directory, total_repositories) VALUES ('/test/scan', 1)");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM scan_sessions WHERE scan_directory = '/test/scan'");
            rs.next();
            assertEquals("Should have one test scan session", 1, rs.getInt(1));
            
            // Test test_classes table
            long repoId = getRepositoryId("test-repo");
            stmt.execute("INSERT INTO test_classes (repository_id, class_name, package_name) VALUES (" + repoId + ", 'TestClass', 'com.example')");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM test_classes WHERE class_name = 'TestClass'");
            rs.next();
            assertEquals("Should have one test class", 1, rs.getInt(1));
            
            // Test test_methods table
            long classId = getTestClassId(repoId, "TestClass");
            stmt.execute("INSERT INTO test_methods (test_class_id, method_name, line_number, has_annotation) VALUES (" + classId + ", 'testMethod', 10, false)");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM test_methods WHERE method_name = 'testMethod'");
            rs.next();
            assertEquals("Should have one test method", 1, rs.getInt(1));
            
            // Test daily_metrics table
            stmt.execute("INSERT INTO daily_metrics (metric_date, total_repositories) VALUES (CURRENT_DATE, 1)");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM daily_metrics WHERE metric_date = CURRENT_DATE");
            rs.next();
            assertEquals("Should have daily metrics for today", 1, rs.getInt(1));
        }
    }

    @Test
    public void testSchemaRecreation() throws SQLException {
        Flyway flyway = Flyway.configure()
            .dataSource(DatabaseConfig.getDataSource())
            .cleanDisabled(false) // Enable clean for tests
            .load();
        
        // Initialize schema first time
        flyway.migrate();
        assertTrue("Schema should exist after first migration", checkSchemaExists());
        
        // Drop all tables
        flyway.clean();
        assertFalse("Schema should not exist after clean", checkSchemaExists());
        
        // Reinitialize schema
        flyway.migrate();
        assertTrue("Schema should exist after reinitialization", checkSchemaExists());
    }
    
    private boolean checkSchemaExists() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'repositories')");
            return rs.next() && rs.getBoolean(1);
        }
    }

    @Test
    public void testConnectionPooling() throws SQLException {
        // Get multiple connections to test pooling
        Connection conn1 = DatabaseConfig.getConnection();
        Connection conn2 = DatabaseConfig.getConnection();
        Connection conn3 = DatabaseConfig.getConnection();
        
        assertNotNull("First connection should not be null", conn1);
        assertNotNull("Second connection should not be null", conn2);
        assertNotNull("Third connection should not be null", conn3);
        
        // Verify they are different connections
        assertNotSame("Connections should be different instances", conn1, conn2);
        assertNotSame("Connections should be different instances", conn1, conn3);
        assertNotSame("Connections should be different instances", conn2, conn3);
        
        // Test that all connections work
        assertTrue("First connection should be valid", conn1.isValid(5));
        assertTrue("Second connection should be valid", conn2.isValid(5));
        assertTrue("Third connection should be valid", conn3.isValid(5));
        
        // Close connections
        conn1.close();
        conn2.close();
        conn3.close();
    }

    @Test
    public void testDatabaseConstraints() throws SQLException {
        // Initialize schema
        Flyway flyway = Flyway.configure()
            .dataSource(DatabaseConfig.getDataSource())
            .load();
        flyway.migrate();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Test unique constraint on repositories
            stmt.execute("INSERT INTO repositories (repository_name, repository_path) VALUES ('unique-repo', '/unique/path')");
            
            try {
                stmt.execute("INSERT INTO repositories (repository_name, repository_path) VALUES ('unique-repo', '/unique/path')");
                fail("Should not allow duplicate repository name and path");
            } catch (SQLException e) {
                // Expected - unique constraint violation
                assertTrue("Should be constraint violation", 
                          e.getMessage().toLowerCase().contains("duplicate") || 
                          e.getMessage().toLowerCase().contains("unique") || 
                          e.getMessage().toLowerCase().contains("constraint"));
            }
            
            // Test foreign key constraint
            try {
                stmt.execute("INSERT INTO test_classes (repository_id, class_name, package_name) VALUES (99999, 'TestClass', 'com.example')");
                fail("Should not allow test class with non-existent repository ID");
            } catch (SQLException e) {
                // Expected - foreign key constraint violation
                assertTrue("Should be foreign key constraint violation", 
                          e.getMessage().toLowerCase().contains("foreign") || 
                          e.getMessage().toLowerCase().contains("constraint"));
            }
        }
    }

    @Test
    public void testIndexes() throws SQLException {
        // Initialize schema
        Flyway flyway = Flyway.configure()
            .dataSource(DatabaseConfig.getDataSource())
            .load();
        flyway.migrate();
        
        // Verify indexes were created
        assertTrue("Repository name index should exist", indexExists("idx_repositories_name", "repositories"));
        assertTrue("Repository team index should exist", indexExists("idx_repositories_team", "repositories"));
        assertTrue("Test classes repository index should exist", indexExists("idx_test_classes_repo", "test_classes"));
        assertTrue("Test methods class index should exist", indexExists("idx_test_methods_class", "test_methods"));
        assertTrue("Scan sessions date index should exist", indexExists("idx_scan_sessions_date", "scan_sessions"));
        assertTrue("Daily metrics date index should exist", indexExists("idx_daily_metrics_date", "daily_metrics"));
        assertTrue("Annotation data GIN index should exist", indexExists("idx_annotation_data", "test_methods"));
    }

    /**
     * Helper method to get repository ID by name
     */
    private long getRepositoryId(String repoName) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT id FROM repositories WHERE repository_name = ?")) {
            stmt.setString(1, repoName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Repository not found: " + repoName);
        }
    }

    /**
     * Helper method to get test class ID by repository ID and class name
     */
    private long getTestClassId(long repoId, String className) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT id FROM test_classes WHERE repository_id = ? AND class_name = ?")) {
            stmt.setLong(1, repoId);
            stmt.setString(2, className);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Test class not found: " + className);
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
}
