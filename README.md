# UnittestCaseInfo Annotation Extractor

This project provides a Java utility to extract all fields and values from the `UnittestCaseInfo` annotation using JavaParser 3.26.3 and JDK 1.8.

## Project Structure

```
annotation-extractor/
├── pom.xml
├── README.md
└── src/
    ├── main/java/com/example/annotationextractor/
    │   ├── UnittestCaseInfoData.java          # Data model class
    │   └── UnittestCaseInfoExtractor.java     # Main extractor class
    └── test/java/com/example/annotationextractor/
        └── UnittestCaseInfoExtractorTest.java  # Test cases
```

## Features

- **Complete Annotation Extraction**: Extracts all fields from `UnittestCaseInfo` annotation
- **Type-Safe Data Model**: Structured data model for easy inspection and manipulation
- **Flexible Input Handling**: Supports both single-member and normal annotations
- **Array Support**: Handles string arrays for fields like `testPoints`, `tags`, etc.
- **Default Value Handling**: Properly handles default values when fields are not specified
- **Comprehensive Testing**: Includes unit tests for various annotation scenarios

## Dependencies

- **Java**: JDK 1.8
- **JavaParser**: 3.26.3
- **JUnit**: 4.13.2 (for testing)

## Usage

### Basic Usage

```java
import com.example.annotationextractor.UnittestCaseInfoExtractor;
import com.example.annotationextractor.UnittestCaseInfoData;
import com.github.javaparser.ast.expr.AnnotationExpr;

// Get your AnnotationExpr from JavaParser
AnnotationExpr annotation = // ... your annotation expression

// Extract all values
UnittestCaseInfoData extractedData = UnittestCaseInfoExtractor.extractAnnotationValues(annotation);

// Now you can inspect all the extracted values
System.out.println("Author: " + extractedData.getAuthor());
System.out.println("Title: " + extractedData.getTitle());
System.out.println("Test Points: " + Arrays.toString(extractedData.getTestPoints()));
```

### Example with Real Java Code

```java
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

// Parse Java source code
String javaCode = 
    "public class TestClass {\n" +
    "    @UnittestCaseInfo(\n" +
    "        author = \"John Doe\",\n" +
    "        title = \"Test User Login\",\n" +
    "        testPoints = {\"auth\", \"login\"}\n" +
    "    )\n" +
    "    public void testMethod() {}\n" +
    "}";

JavaParser parser = new JavaParser();
CompilationUnit cu = parser.parse(javaCode).getResult().get();

// Find method with annotation
MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();
AnnotationExpr annotation = method.getAnnotationByName("UnittestCaseInfo").get();

// Extract values
UnittestCaseInfoData data = UnittestCaseInfoExtractor.extractAnnotationValues(annotation);
```

## Data Model

The `UnittestCaseInfoData` class contains all the fields from the annotation:

### Required Fields
- `author` (String) - Author information
- `title` (String) - Test case title

### Optional Fields
- `targetClass` (String) - Target class for the test case
- `targetMethod` (String) - Target method for the test case
- `testPoints` (String[]) - Test points involved
- `description` (String) - Test case description
- `tags` (String[]) - Test case tags
- `status` (String) - Test case status (defaults to "TODO")
- `relatedRequirements` (String[]) - Related requirements
- `relatedDefects` (String[]) - Related defects
- `relatedTestcases` (String[]) - Related test cases
- `lastUpdateTime` (String) - Last update time
- `lastUpdateAuthor` (String) - Last maintainer
- `methodSignature` (String) - Method signature

## Supported Annotation Formats

### Normal Annotation
```java
@UnittestCaseInfo(
    author = "John Doe",
    title = "Test Title",
    testPoints = {"point1", "point2"}
)
```

### Single Member Annotation
```java
@UnittestCaseInfo("Simple Test Title")
```

## Building and Testing

### Build the Project
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

### Package
```bash
mvn package
```

## Error Handling

The extractor handles various edge cases:
- **Null AnnotationExpr**: Throws `IllegalArgumentException`
- **Missing Fields**: Uses default values (empty strings/arrays)
- **Unknown Fields**: Ignored silently
- **Type Mismatches**: Attempts to convert to string representation

## Extensibility

The extractor is designed to be easily extensible:
- Add new fields to `UnittestCaseInfoData`
- Update the switch statement in `processNormalAnnotation`
- Add new extraction methods for different data types

## License

This project is provided as-is for educational and development purposes.
