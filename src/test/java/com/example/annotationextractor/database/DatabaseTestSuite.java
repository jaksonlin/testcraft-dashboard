package com.example.annotationextractor.database;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for database layer tests.
 * 
 * This suite ensures tests run sequentially to avoid conflicts with the static
 * HikariDataSource in DatabaseConfig. Tests are ordered from unit tests to
 * integration tests to ensure proper setup and teardown.
 */
@RunWith(Suite.class)
@SuiteClasses({
    // Unit tests - run first
    DatabaseConfigTest.class,
    DatabaseSchemaManagerTest.class,
    
    // Integration tests - run last
    DatabaseIntegrationTest.class,
    
    // Data persistence tests - run after schema tests
    DataPersistenceServiceTest.class
})
public class DatabaseTestSuite {
    // This class serves as a test suite configuration
    // No test methods needed
}
