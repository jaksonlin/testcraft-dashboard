# Annotation Extractor

A Java library for extracting and analyzing `UnittestCaseInfo` annotations from Java test classes. This tool can scan entire directories of Java git repositories to collect comprehensive test information and store it in PostgreSQL for long-term analysis and reporting.

## Features

- **Annotation Extraction**: Parse `UnittestCaseInfo` annotations and extract structured data
- **Repository Scanning**: Automatically discover and scan Java git repositories
- **Test Discovery**: Find test classes and methods following standard Java conventions
- **Comprehensive Data Model**: Organized collection of test information across multiple repositories
- **Standard Test Directory Support**: Recognizes common test directory patterns (`src/test/java`, `test`, etc.)
- **PostgreSQL Integration**: Store scan results in PostgreSQL for long-term analysis
- **Excel Reporting**: Generate comprehensive Excel reports with trends and analysis
- **Connection Pooling**: Efficient database connections with HikariCP
- **Trend Analysis**: Track test coverage and annotation usage over time

## Data Models

The system uses a hierarchical data model to organize collected information:

- **`TestMethodInfo`**: Individual test method details including annotations
- **`TestClassInfo`**: Test class information with all its test methods
- **`RepositoryTestInfo`**: Repository-level test information
- **`TestCollectionSummary`**: Top-level summary of all scanned repositories

## Database Schema

The PostgreSQL database includes:

- **`repositories`**: Repository information and metadata
- **`scan_sessions`**: Scan execution history and performance metrics
- **`test_classes`**: Test class details and coverage rates
- **`test_methods`**: Individual test method data with annotation details
- **`daily_metrics`**: Aggregated daily statistics for trend analysis

## Usage

### Basic Repository Scanning

```java
import com.example.annotationextractor.*;

// Scan a directory for Java git repositories
String directoryPath = "/path/to/repositories";
TestCollectionSummary summary = RepositoryScanner.scanRepositories(directoryPath);

// Access collected information
System.out.println("Total repositories: " + summary.getTotalRepositories());
System.out.println("Total test classes: " + summary.getTotalTestClasses());
System.out.println("Total test methods: " + summary.getTotalTestMethods());
```

### Database Integration

```java
import com.example.annotationextractor.database.*;

// Initialize database connection
DatabaseConfig.initialize("localhost", 5432, "test_analytics", "username", "password");

// Create database schema
DatabaseSchemaManager.initializeSchema();

// Persist scan results
long sessionId = DataPersistenceService.persistScanSession(summary, scanDurationMs);
```

### Excel Report Generation

```java
import com.example.annotationextractor.reporting.*;

// Generate comprehensive weekly report
ExcelReportGenerator.generateWeeklyReport("weekly_report.xlsx");
```

### Command Line Usage

```bash
# Initialize database and scan repositories
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.TestCollectionRunner /path/to/repositories --init-db

# Scan and generate Excel report
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.TestCollectionRunner /path/to/repositories --generate-report --report-path weekly_report.xlsx

# Full workflow: initialize DB, scan, and generate report
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.TestCollectionRunner /path/to/repositories --init-db --generate-report
```

## Database Setup

### PostgreSQL Installation

1. Install PostgreSQL (version 12 or higher recommended)
2. Create a database:
   ```sql
   CREATE DATABASE test_analytics;
   CREATE USER test_user WITH PASSWORD '123456';
   GRANT ALL PRIVILEGES ON DATABASE test_analytics TO test_user;
   ```

### Configuration

Edit `src/main/resources/database.properties`:
```properties
db.host=localhost
db.port=5432
db.name=test_analytics
db.username=test_user
db.password=123456
```

## Test Directory Recognition

The scanner automatically recognizes standard Java test directory patterns:

- `src/test/java` (Maven/Gradle standard)
- `src/test`
- `test`
- `tests`
- `test/java`
- `tests/java`

## Test Class Detection

A class is considered a test class if it:
- Has a name ending with "Test" or "Tests"
- Is annotated with `@Test` (JUnit 4/5)
- Is in a recognized test directory

## Test Method Detection

A method is considered a test method if it:
- Is annotated with `@Test` (JUnit 4/5)
- Is in a recognized test class

## Excel Reports

The system generates comprehensive Excel reports with:

- **Weekly Summary**: High-level metrics and overview
- **Repository Details**: Individual repository performance
- **Trends & Analysis**: Historical data and trends
- **Annotation Coverage**: Coverage analysis and recommendations

## Dependencies

- Java 8 or higher
- JavaParser 3.26.3+ for Java code parsing
- PostgreSQL 12+ for data storage
- Apache POI 5.2.4+ for Excel generation
- HikariCP 5.1.0+ for connection pooling
- JUnit 4.13.2+ for testing

## Building

```bash
mvn clean compile
mvn test
mvn package
```

## Example Output

```
================================================================================
TEST COLLECTION SUMMARY
================================================================================
Scan Directory: /path/to/repositories
Scan Timestamp: Mon Jan 20 10:30:00 UTC 2024
Scan Duration: 15420 ms
Total Repositories: 3
Total Test Classes: 15
Total Test Methods: 127
Total Annotated Test Methods: 45
Overall Annotation Coverage: 35.43%

--------------------------------------------------------------------------------
REPOSITORY DETAILS
--------------------------------------------------------------------------------

Repository: user-service
Path: /path/to/repositories/user-service
Test Classes: 8
Test Methods: 67
Annotated Methods: 23
Coverage Rate: 34.33%

  Test Classes:
    UserServiceTest (com.example.userservice)
      File: /path/to/repositories/user-service/src/test/java/com/example/userservice/UserServiceTest.java
      Methods: 12 (Annotated: 5)
      Coverage: 41.67%
        testUserRegistration - Test user registration with valid data
        testUserLogin - Test user login functionality
```

## Architecture

The system is designed with a clear separation of concerns:

1. **`UnittestCaseInfoExtractor`**: Core annotation parsing logic
2. **`TestClassParser`**: Java file parsing and test method extraction
3. **`RepositoryScanner`**: Directory traversal and repository discovery
4. **Data Models**: Hierarchical organization of collected information
5. **`DatabaseConfig`**: PostgreSQL connection management
6. **`DatabaseSchemaManager`**: Database schema creation and management
7. **`DataPersistenceService`**: Data persistence and batch operations
8. **`ExcelReportGenerator`**: Excel report generation with Apache POI
9. **`TestCollectionRunner`**: Main entry point with database integration

## Future Enhancements

- Advanced chart generation with Apache POI charts
- REST API for web-based reporting
- Email automation for scheduled reports
- Test execution history integration
- Custom test directory pattern configuration
- Parallel processing for large repositories
- Machine learning insights for test quality
- Integration with CI/CD pipelines
