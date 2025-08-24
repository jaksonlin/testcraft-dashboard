package com.example.annotationextractor.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for DatabaseConfig class using H2 in-memory database
 */
public class DatabaseConfigTest {

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 5432;
    private static final String TEST_DATABASE = "test_analytics";
    private static final String TEST_USERNAME = "postgres";
    private static final String TEST_PASSWORD = "postgres";

    @Before
    public void setUp() {
        // Clean up any existing connections
        DatabaseConfig.initialize();
    }

    @After
    public void tearDown() {
        // Clean up any existing connections
        DatabaseConfig.close();
    }

    @Test
    public void testInitializeWithDefaultValues() {
        // Test initialization with default values
        DatabaseConfig.initialize();
        
        // Verify the connection pool is healthy
        assertTrue("Connection pool should be healthy after initialization", 
                   DatabaseConfig.isHealthy());
        
        // Verify pool statistics are available
        String stats = DatabaseConfig.getPoolStats();
        assertNotNull("Pool stats should not be null", stats);
        assertFalse("Pool stats should not be empty", stats.isEmpty());
    }

    @Test
    public void testInitializeWithCustomValues() {
        // Test initialization with custom values
        DatabaseConfig.initialize(TEST_HOST, TEST_PORT, TEST_DATABASE, TEST_USERNAME, TEST_PASSWORD);
        
        // Verify the connection pool is healthy
        assertTrue("Connection pool should be healthy after custom initialization", 
                   DatabaseConfig.isHealthy());
    }

    @Test
    public void testGetConnection() throws SQLException {
        // Initialize the connection pool
        DatabaseConfig.initialize(TEST_HOST, TEST_PORT, TEST_DATABASE, TEST_USERNAME, TEST_PASSWORD);
        
        // Get a connection
        try (Connection connection = DatabaseConfig.getConnection()) {
            assertNotNull("Connection should not be null", connection);
            assertFalse("Connection should not be closed", connection.isClosed());
            
            // Test basic connection functionality
            assertTrue("Connection should be valid", connection.isValid(5));
        }
    }

    @Test
    public void testGetConnectionWithoutInitialization() throws SQLException {
        // Test getting connection without explicit initialization
        // This should trigger auto-initialization with defaults
        try (Connection connection = DatabaseConfig.getConnection()) {
            assertNotNull("Connection should not be null", connection);
            assertFalse("Connection should not be closed", connection.isClosed());
        }
    }

    @Test
    public void testMultipleConnections() throws SQLException {
        // Initialize the connection pool
        DatabaseConfig.initialize(TEST_HOST, TEST_PORT, TEST_DATABASE, TEST_USERNAME, TEST_PASSWORD);
        
        // Get multiple connections
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
        
        // Close connections
        conn1.close();
        conn2.close();
        conn3.close();
    }

    @Test
    public void testConnectionPoolHealth() {
        // Test health check before 
        DatabaseConfig.close();
        assertFalse("Connection pool should not be healthy before initialization", 
                   DatabaseConfig.isHealthy());
        
        // Initialize the connection pool
        DatabaseConfig.initialize();
        
        // Test health check after initialization
        assertTrue("Connection pool should be healthy after initialization", 
                   DatabaseConfig.isHealthy());
    }

    @Test
    public void testCloseConnectionPool() {
        // Initialize the connection pool
        DatabaseConfig.initialize();
        assertTrue("Connection pool should be healthy before closing", 
                   DatabaseConfig.isHealthy());
        
        // Close the connection pool
        DatabaseConfig.close();
        
        // Verify the connection pool is closed
        assertFalse("Connection pool should not be healthy after closing", 
                   DatabaseConfig.isHealthy());
    }

    @Test
    public void testReinitializeAfterClose() {
        // Initialize, close, and reinitialize
        DatabaseConfig.initialize();
        assertTrue("Connection pool should be healthy after first initialization", 
                   DatabaseConfig.isHealthy());
        
        DatabaseConfig.close();
        assertFalse("Connection pool should not be healthy after closing", 
                   DatabaseConfig.isHealthy());
        
        // Reinitialize
        DatabaseConfig.initialize();
        assertTrue("Connection pool should be healthy after reinitialization", 
                   DatabaseConfig.isHealthy());
    }

    @Test
    public void testConnectionTimeout() throws SQLException {
        // Initialize with custom timeout settings
        DatabaseConfig.initialize(TEST_HOST, TEST_PORT, TEST_DATABASE, TEST_USERNAME, TEST_PASSWORD);
        
        // Get a connection and verify timeout settings
        try (Connection connection = DatabaseConfig.getConnection()) {
            // Test that connection operations work within timeout
            assertTrue("Connection should be valid", connection.isValid(5));
            
            // Test basic SQL operations
            try (var stmt = connection.createStatement()) {
                var rs = stmt.executeQuery("SELECT 1");
                assertTrue("Should be able to execute simple query", rs.next());
                assertEquals("Query result should be 1", 1, rs.getInt(1));
            }
        }
    }



    @Test
    public void testConcurrentConnections() throws Exception {
        // Initialize the connection pool
        DatabaseConfig.initialize(TEST_HOST, TEST_PORT, TEST_DATABASE, TEST_USERNAME, TEST_PASSWORD);
        
        // Create multiple threads to get connections concurrently
        Thread[] threads = new Thread[5];
        final Connection[] connections = new Connection[5];
        final Exception[] exceptions = new Exception[5];
        
        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        connections[index] = DatabaseConfig.getConnection();
                    } catch (Exception e) {
                        exceptions[index] = e;
                    }
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Check for any exceptions
        for (int i = 0; i < 5; i++) {
            if (exceptions[i] != null) {
                fail("Thread " + i + " failed with exception: " + exceptions[i].getMessage());
            }
        }
        
        // Verify all connections were obtained
        for (int i = 0; i < 5; i++) {
            assertNotNull("Connection " + i + " should not be null", connections[i]);
            assertFalse("Connection " + i + " should not be closed", connections[i].isClosed());
        }
        
        // Close all connections
        for (Connection connection : connections) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // Ignore close errors in test
                }
            }
        }
    }
}
