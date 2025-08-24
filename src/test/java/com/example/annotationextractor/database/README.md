# Database Layer Tests

This directory contains comprehensive tests for the database connection layer of the annotation extractor.

## ⚠️ Important: Sequential Test Execution

**These tests MUST run sequentially, not in parallel.** This is because:

- `DatabaseConfig` uses a static `HikariDataSource` member
- Parallel execution can cause connection pool conflicts
- Tests need proper isolation to avoid data corruption
- Database schema operations are not thread-safe

## Test Files

### 1. DatabaseConfigTest.java
Tests for the `DatabaseConfig` class, including:
- Connection pool initialization
- Connection management
- Pool health monitoring
- Pool statistics
- Connection timeout handling
- Concurrent connection handling

### 2. DatabaseSchemaManagerTest.java
Tests for the `DatabaseSchemaManager` class, including:
- Schema initialization
- Table creation
- Index creation
- Schema validation
- Table structure verification
- Constraint testing

### 3. DataPersistenceServiceTest.java
Tests for the `DataPersistenceService` class, including:
- Data persistence operations
- Transaction management
- Data validation
- Error handling

### 4. DatabaseIntegrationTest.java
End-to-end integration tests for the database layer, including:
- Complete database operations
- Constraint validation
- Index verification
- Connection pooling
- Schema recreation

### 5. DatabaseTestSuite.java
JUnit 4 test suite that ensures proper test execution order:
- Unit tests run first
- Integration tests run last
- All tests execute sequentially

## Running the Tests

### Prerequisites
1. PostgreSQL database running (or use H2 in-memory for unit tests)
2. Maven installed
3. Java 17 or higher

### Running with Maven

#### Option 1: Run the Complete Test Suite (Recommended)
```bash
# Run all database tests sequentially using the test suite
mvn test -Dtest=DatabaseTestSuite

# Run with verbose output
mvn test -Dtest=DatabaseTestSuite -Dmaven.test.failure.ignore=true
```

#### Option 2: Run Individual Test Classes
```bash
# Run specific test class (will still be sequential due to Maven config)
mvn test -Dtest=DatabaseConfigTest

# Run DatabaseSchemaManagerTest
mvn test -Dtest=DatabaseSchemaManagerTest

# Run DatabaseIntegrationTest
mvn test -Dtest=DatabaseIntegrationTest
```

#### Option 3: Run All Database Tests
```bash
# Run all tests matching the pattern (will be sequential)
mvn test -Dtest="*Database*Test"
```

### Using the Windows Batch Script
```bash
# Run the provided batch script for easy execution
run-database-tests.bat
```

## Test Configuration

The tests use the following configuration:
- **Database**: PostgreSQL (configurable via properties)
- **Connection Pool**: HikariCP
- **Test Framework**: JUnit 4
- **Mocking**: Mockito (for unit tests)
- **In-Memory Database**: H2 (for isolated unit tests)
- **Execution**: Sequential (no parallel execution)

### Maven Configuration
The `pom.xml` includes specific configurations:
- **Surefire Plugin**: Disables parallel execution (`parallel=none`)
- **Failsafe Plugin**: Handles integration tests sequentially
- **Test Isolation**: Each test runs in its own fork for isolation

## Test Data

Tests create and manage their own test data:
- Test repositories
- Test classes
- Test methods
- Annotations
- Scan sessions
- Daily metrics

## Cleanup

All tests automatically clean up after themselves:
- Drop test tables
- Close database connections
- Reset connection pools

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure PostgreSQL is running
   - Check database credentials in `database.properties`
   - Verify network connectivity

2. **Schema Creation Failed**
   - Check database permissions
   - Ensure database exists
   - Verify PostgreSQL version compatibility

3. **Test Timeout**
   - Increase connection timeout in test configuration
   - Check database performance
   - Verify connection pool settings

4. **Connection Pool Conflicts**
   - Ensure tests are running sequentially
   - Check that `DatabaseConfig.close()` is called in `@After` methods
   - Verify no parallel test execution

### Debug Mode

To run tests with debug output:
```bash
mvn test -Dtest=DatabaseTestSuite -Dmaven.test.failure.ignore=true -X
```

## Test Coverage

The tests cover:
- ✅ Connection management
- ✅ Schema operations
- ✅ Data persistence
- ✅ Transaction handling
- ✅ Constraint validation
- ✅ Index management
- ✅ Error handling
- ✅ Connection pooling
- ✅ Concurrent operations (within single test)

## Performance Considerations

- Tests use connection pooling for efficiency
- Database operations are batched where possible
- Test data is minimal to reduce execution time
- Cleanup operations are optimized
- **Sequential execution ensures reliability over speed**

## Future Enhancements

Potential improvements for the test suite:
- Performance benchmarking tests
- Load testing for connection pools
- More comprehensive error scenario testing
- Integration with CI/CD pipelines
- Test data generation utilities
- **Consider refactoring to instance-based datasource for parallel testing**
