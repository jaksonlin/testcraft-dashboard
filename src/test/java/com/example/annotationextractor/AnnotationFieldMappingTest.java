package com.example.annotationextractor;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.sql.*;
import java.util.Arrays;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.database.DatabaseSchemaManager;
import com.example.annotationextractor.database.DataPersistenceService;
import com.example.annotationextractor.database.DataPersistenceServiceTest;

/**
 * Comprehensive test to verify UnittestCaseInfo annotation field mapping to database
 */
public class AnnotationFieldMappingTest {

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 5432;
    private static final String TEST_DATABASE = "test_analytics_test";
    private static final String TEST_USERNAME = "postgres";
    private static final String TEST_PASSWORD = "postgres";

    @Before
    public void setUp() {
        // Initialize database connection for testing
        DatabaseConfig.initialize(TEST_HOST, TEST_PORT, TEST_DATABASE, TEST_USERNAME, TEST_PASSWORD);
    }

    @After
    public void tearDown() {
        DatabaseConfig.close();
    }

    @Test
    public void testAllAnnotationFieldsAreMappedToDatabase() throws SQLException {
        // Initialize database schema
        DatabaseSchemaManager.initializeSchema();
        
        // Create a comprehensive test annotation with all fields populated
        UnittestCaseInfoData testAnnotation = createComprehensiveTestAnnotation();
        
        // Create test data structure
        TestMethodInfo testMethod = new TestMethodInfo();
        testMethod.setMethodName("testComprehensiveAnnotation");
        testMethod.setClassName("TestClass");
        testMethod.setPackageName("com.example.test");
        testMethod.setFilePath("/test/path/TestClass.java");
        testMethod.setLineNumber(42);
        testMethod.setAnnotationData(testAnnotation);
        
        TestClassInfo testClass = new TestClassInfo();
        testClass.setClassName("TestClass");
        testClass.setPackageName("com.example.test");
        testClass.setFilePath("/test/path/TestClass.java");
        testClass.addTestMethod(testMethod);
        
        RepositoryTestInfo repository = new RepositoryTestInfo();
        repository.setRepositoryName("test-repo");
        repository.setRepositoryPath("/test/repo");
        repository.addTestClass(testClass);
        
        TestCollectionSummary summary = new TestCollectionSummary();
        summary.addRepository(repository);
        
        // Persist to database
        long scanSessionId = DataPersistenceService.persistScanSession(summary, 1000L);
        
        // Verify all fields are properly stored in database
        verifyAllAnnotationFieldsInDatabase(testAnnotation, scanSessionId);
    }

    @Test
    public void testDatabaseSchemaHasAllRequiredColumns() throws SQLException {
        // Initialize database schema
        DatabaseSchemaManager.initializeSchema();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Get table metadata
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "test_methods", null);
            
            // Expected columns for annotation data
            String[] expectedColumns = {
                "annotation_title",
                "annotation_author", 
                "annotation_status",
                "annotation_target_class",
                "annotation_target_method",
                "annotation_description",
                "annotation_tags",
                "annotation_test_points",
                "annotation_requirements",
                "annotation_data"  // JSONB field for complete data
            };
            
            // Check if all expected columns exist
            for (String expectedColumn : expectedColumns) {
                boolean columnFound = false;
                columns.beforeFirst(); // Reset result set
                
                while (columns.next()) {
                    if (expectedColumn.equals(columns.getString("COLUMN_NAME"))) {
                        columnFound = true;
                        break;
                    }
                }
                
                assertTrue("Database column '" + expectedColumn + "' should exist", columnFound);
            }
        }
    }

    @Test
    public void testAnnotationDataJSONBContainsAllFields() throws SQLException {
        // Initialize database schema
        DatabaseSchemaManager.initializeSchema();
        
        // Create comprehensive test annotation
        UnittestCaseInfoData testAnnotation = createComprehensiveTestAnnotation();
        
        // Create and persist test method
        TestMethodInfo testMethod = createTestMethodWithAnnotation(testAnnotation);
        long scanSessionId = persistTestMethod(testMethod);
        
        // Retrieve and verify JSONB data contains all fields
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT annotation_data FROM test_methods WHERE scan_session_id = ?")) {
            
            stmt.setLong(1, scanSessionId);
            ResultSet rs = stmt.executeQuery();
            
            assertTrue("Should have test method data", rs.next());
            
            String jsonData = rs.getString("annotation_data");
            assertNotNull("JSONB data should not be null", jsonData);
            
            // Verify all annotation fields are present in JSON
            verifyAllFieldsInJSON(jsonData, testAnnotation);
        }
    }

    @Test
    public void testArrayFieldsAreProperlyStored() throws SQLException {
        // Initialize database schema
        DatabaseSchemaManager.initializeSchema();
        
        // Create test annotation with array fields
        UnittestCaseInfoData testAnnotation = createComprehensiveTestAnnotation();
        
        // Create and persist test method
        TestMethodInfo testMethod = createTestMethodWithAnnotation(testAnnotation);
        long scanSessionId = persistTestMethod(testMethod);
        
        // Verify TEXT fields are stored correctly
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT annotation_tags, annotation_test_points, annotation_requirements " +
                 "FROM test_methods WHERE scan_session_id = ?")) {
            
            stmt.setLong(1, scanSessionId);
            ResultSet rs = stmt.executeQuery();
            
            assertTrue("Should have test method data", rs.next());
            
            // Verify tags field (stored as TEXT)
            String storedTags = rs.getString("annotation_tags");
            assertNotNull("Tags should not be null", storedTags);
            assertTrue("Tags should contain expected data", storedTags.contains("unit") || storedTags.contains("integration"));
            
            // Verify test points field (stored as TEXT)
            String storedTestPoints = rs.getString("annotation_test_points");
            assertNotNull("Test points should not be null", storedTestPoints);
            assertTrue("Test points should contain expected data", storedTestPoints.contains("TP001") || storedTestPoints.contains("TP002"));
            
            // Verify requirements field (stored as TEXT)
            String storedRequirements = rs.getString("annotation_requirements");
            assertNotNull("Requirements should not be null", storedRequirements);
            assertTrue("Requirements should contain expected data", storedRequirements.contains("REQ-001") || storedRequirements.contains("REQ-002"));
        }
    }

    @Test
    public void testMissingFieldsAreHandledGracefully() throws SQLException {
        // Initialize database schema
        DatabaseSchemaManager.initializeSchema();
        
        // Create test annotation with missing fields
        UnittestCaseInfoData testAnnotation = new UnittestCaseInfoData();
        testAnnotation.setTitle("Test with missing fields");
        testAnnotation.setAuthor("Test Author");
        // Don't set other fields to test default handling
        
        // Create and persist test method
        TestMethodInfo testMethod = createTestMethodWithAnnotation(testAnnotation);
        long scanSessionId = persistTestMethod(testMethod);
        
        // Verify missing fields are handled gracefully
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT annotation_title, annotation_author, annotation_status, " +
                 "annotation_target_class, annotation_target_method, annotation_description " +
                 "FROM test_methods WHERE scan_session_id = ?")) {
            
            stmt.setLong(1, scanSessionId);
            ResultSet rs = stmt.executeQuery();
            
            assertTrue("Should have test method data", rs.next());
            
            assertEquals("Title should match", testAnnotation.getTitle(), rs.getString("annotation_title"));
            assertEquals("Author should match", testAnnotation.getAuthor(), rs.getString("annotation_author"));
            assertEquals("Status should have default value", "TODO", rs.getString("annotation_status"));
            assertEquals("Target class should be empty string", "", rs.getString("annotation_target_class"));
            assertEquals("Target method should be empty string", "", rs.getString("annotation_target_method"));
            assertEquals("Description should be empty string", "", rs.getString("annotation_description"));
        }
    }

    // Helper methods
    
    private UnittestCaseInfoData createComprehensiveTestAnnotation() {
        UnittestCaseInfoData annotation = new UnittestCaseInfoData();
        annotation.setAuthor("John Doe");
        annotation.setTitle("Comprehensive Test Case");
        annotation.setTargetClass("TargetClass");
        annotation.setTargetMethod("targetMethod");
        annotation.setTestPoints(new String[]{"TP001", "TP002", "TP003"});
        annotation.setDescription("This is a comprehensive test case with all fields populated");
        annotation.setTags(new String[]{"unit", "integration", "comprehensive"});
        annotation.setStatus("IN_PROGRESS");
        annotation.setRelatedRequirements(new String[]{"REQ-001", "REQ-002"});
        annotation.setRelatedDefects(new String[]{"BUG-001"});
        annotation.setRelatedTestcases(new String[]{"TC-001", "TC-002"});
        annotation.setLastUpdateTime("2024-01-15T10:00:00Z");
        annotation.setLastUpdateAuthor("Jane Smith");
        annotation.setMethodSignature("public void testComprehensiveAnnotation()");
        return annotation;
    }
    
    private TestMethodInfo createTestMethodWithAnnotation(UnittestCaseInfoData annotation) {
        TestMethodInfo testMethod = new TestMethodInfo();
        testMethod.setMethodName("testMethod");
        testMethod.setClassName("TestClass");
        testMethod.setPackageName("com.example.test");
        testMethod.setFilePath("/test/path/TestClass.java");
        testMethod.setLineNumber(42);
        testMethod.setAnnotationData(annotation);
        return testMethod;
    }
    
    private long persistTestMethod(TestMethodInfo testMethod) throws SQLException {
        TestClassInfo testClass = new TestClassInfo();
        testClass.setClassName("TestClass");
        testClass.setPackageName("com.example.test");
        testClass.setFilePath("/test/path/TestClass.java");
        testClass.addTestMethod(testMethod);
        
        RepositoryTestInfo repository = new RepositoryTestInfo();
        repository.setRepositoryName("test-repo");
        repository.setRepositoryPath("/test/repo");
        repository.addTestClass(testClass);
        
        TestCollectionSummary summary = new TestCollectionSummary();
        summary.addRepository(repository);
        
        return DataPersistenceService.persistScanSession(summary, 1000L);
    }
    
    private void verifyAllAnnotationFieldsInDatabase(UnittestCaseInfoData expected, long scanSessionId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT annotation_title, annotation_author, annotation_status, " +
                 "annotation_target_class, annotation_target_method, annotation_description, " +
                 "annotation_tags, annotation_test_points, annotation_requirements, " +
                 "annotation_defects, annotation_testcases, annotation_last_update_time, " +
                 "annotation_last_update_author " +
                 "FROM test_methods WHERE scan_session_id = ?")) {
            
            stmt.setLong(1, scanSessionId);
            ResultSet rs = stmt.executeQuery();
            
            assertTrue("Should have test method data", rs.next());
            
            // Verify all mapped fields
            assertEquals("Title should match", expected.getTitle(), rs.getString("annotation_title"));
            assertEquals("Author should match", expected.getAuthor(), rs.getString("annotation_author"));
            assertEquals("Status should match", expected.getStatus(), rs.getString("annotation_status"));
            assertEquals("Target class should match", expected.getTargetClass(), rs.getString("annotation_target_class"));
            assertEquals("Target method should match", expected.getTargetMethod(), rs.getString("annotation_target_method"));
            assertEquals("Description should match", expected.getDescription(), rs.getString("annotation_description"));
            
            // Verify TEXT fields (stored as comma-separated strings or JSON)
            String storedTags = rs.getString("annotation_tags");
            assertNotNull("Tags should not be null", storedTags);
            // For now, just check if the field contains the expected data
            assertTrue("Tags should contain expected data", storedTags.contains("unit") || storedTags.contains("integration"));
            
            String storedTestPoints = rs.getString("annotation_test_points");
            assertNotNull("Test points should not be null", storedTestPoints);
            assertTrue("Test points should contain expected data", storedTestPoints.contains("TP001") || storedTestPoints.contains("TP002"));
            
            String storedRequirements = rs.getString("annotation_requirements");
            assertNotNull("Requirements should not be null", storedRequirements);
            assertTrue("Requirements should contain expected data", storedRequirements.contains("REQ-001") || storedRequirements.contains("REQ-002"));
            
            // Verify new TEXT fields
            String storedDefects = rs.getString("annotation_defects");
            assertNotNull("Defects should not be null", storedDefects);
            assertTrue("Defects should contain expected data", storedDefects.contains("BUG-001"));
            
            String storedTestcases = rs.getString("annotation_testcases");
            assertNotNull("Test cases should not be null", storedTestcases);
            assertTrue("Test cases should contain expected data", storedTestcases.contains("TC-001") || storedTestcases.contains("TC-002"));
            
            // Verify new string fields
            assertEquals("Last update time should match", expected.getLastUpdateTime(), rs.getString("annotation_last_update_time"));
            assertEquals("Last update author should match", expected.getLastUpdateAuthor(), rs.getString("annotation_last_update_author"));
        }
    }
    
    private void verifyAllFieldsInJSON(String jsonData, UnittestCaseInfoData expected) {
        // Simple JSON verification - check if all field names are present
        assertTrue("JSON should contain author field", jsonData.contains("\"author\""));
        assertTrue("JSON should contain title field", jsonData.contains("\"title\""));
        assertTrue("JSON should contain targetClass field", jsonData.contains("\"targetClass\""));
        assertTrue("JSON should contain targetMethod field", jsonData.contains("\"targetMethod\""));
        assertTrue("JSON should contain testPoints field", jsonData.contains("\"testPoints\""));
        assertTrue("JSON should contain description field", jsonData.contains("\"description\""));
        assertTrue("JSON should contain tags field", jsonData.contains("\"tags\""));
        assertTrue("JSON should contain status field", jsonData.contains("\"status\""));
        assertTrue("JSON should contain relatedRequirements field", jsonData.contains("\"relatedRequirements\""));
        assertTrue("JSON should contain relatedDefects field", jsonData.contains("\"relatedDefects\""));
        assertTrue("JSON should contain relatedTestcases field", jsonData.contains("\"relatedTestcases\""));
        assertTrue("JSON should contain lastUpdateTime field", jsonData.contains("\"lastUpdateTime\""));
        assertTrue("JSON should contain lastUpdateAuthor field", jsonData.contains("\"lastUpdateAuthor\""));
        assertTrue("JSON should contain methodSignature field", jsonData.contains("\"methodSignature\""));
    }
}
