# Annotation Extractor

A Java library for extracting and analyzing `UnittestCaseInfo` annotations from Java test classes. This tool can scan entire directories of Java git repositories to collect comprehensive test information.

## Features

- **Annotation Extraction**: Parse `UnittestCaseInfo` annotations and extract structured data
- **Repository Scanning**: Automatically discover and scan Java git repositories
- **Test Discovery**: Find test classes and methods following standard Java conventions
- **Comprehensive Data Model**: Organized collection of test information across multiple repositories
- **Standard Test Directory Support**: Recognizes common test directory patterns (`src/test/java`, `test`, etc.)

## Data Models

The system uses a hierarchical data model to organize collected information:

- **`TestMethodInfo`**: Individual test method details including annotations
- **`TestClassInfo`**: Test class information with all its test methods
- **`RepositoryTestInfo`**: Repository-level test information
- **`TestCollectionSummary`**: Top-level summary of all scanned repositories

## Usage

### Basic Annotation Extraction

```java
import com.example.annotationextractor.*;

// Extract annotation values
UnittestCaseInfoData data = UnittestCaseInfoExtractor.extractAnnotationValues(annotationExpr);
System.out.println("Title: " + data.getTitle());
System.out.println("Author: " + data.getAuthor());
```

### Repository Scanning

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

### Command Line Usage

```bash
# Run the main collection runner
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.TestCollectionRunner /path/to/repositories

# Run the example usage class
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.ExampleUsage /path/to/repositories
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

## Dependencies

- Java 8 or higher
- JavaParser 3.26.3+ for Java code parsing
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
Total Repositories: 3
Total Test Classes: 15
Total Test Methods: 127
Total Annotated Test Methods: 45

--------------------------------------------------------------------------------
REPOSITORY DETAILS
--------------------------------------------------------------------------------

Repository: user-service
Path: /path/to/repositories/user-service
Test Classes: 8
Test Methods: 67
Annotated Methods: 23

  Test Classes:
    UserServiceTest (com.example.userservice)
      File: /path/to/repositories/user-service/src/test/java/com/example/userservice/UserServiceTest.java
      Methods: 12 (Annotated: 5)
        testUserRegistration - Test user registration with valid data
        testUserLogin - Test user login functionality
```

## Architecture

The system is designed with a clear separation of concerns:

1. **`UnittestCaseInfoExtractor`**: Core annotation parsing logic
2. **`TestClassParser`**: Java file parsing and test method extraction
3. **`RepositoryScanner`**: Directory traversal and repository discovery
4. **Data Models**: Hierarchical organization of collected information
5. **`TestCollectionRunner`**: Main entry point for command-line usage

## Future Enhancements

- Report generation in various formats (HTML, PDF, Excel)
- Test coverage analysis
- Test execution history integration
- Custom test directory pattern configuration
- Parallel processing for large repositories
