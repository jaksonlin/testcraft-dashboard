package com.example.annotationextractor.database;

import com.example.annotationextractor.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for DataPersistenceService class
 */
public class DataPersistenceServiceTest {

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 5432;
    private static final String TEST_DATABASE = "test_analytics_test";
    private static final String TEST_USERNAME = "postgres";
    private static final String TEST_PASSWORD = "postgres";

    @Before
    public void setUp() {
        // Initialize database connection and schema
        DatabaseConfig.initialize(TEST_HOST, TEST_PORT, TEST_DATABASE, TEST_USERNAME, TEST_PASSWORD);
        // Clean up database
        try {
            DatabaseSchemaManager.dropAllTables();
        } catch (SQLException e) {
            // Ignore cleanup errors
        }
        try {
            DatabaseSchemaManager.initializeSchema();
        } catch (SQLException e) {
            fail("Failed to initialize database schema: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        DatabaseConfig.close();
    }

    @Test
    public void testPersistScanSession() throws SQLException {
        // Create test data
        TestCollectionSummary summary = createTestSummary();
        
        // Persist the scan session
        long scanSessionId = DataPersistenceService.persistScanSession(summary, 1500L);
        
        // Verify scan session was created
        assertTrue("Scan session ID should be positive", scanSessionId > 0);
        
        // Verify data was persisted correctly
        verifyScanSessionData(scanSessionId, summary);
    }

    @Test
    public void testPersistRepository() throws SQLException {
        // Create test data
        RepositoryTestInfo repo = createTestRepository();
        TestCollectionSummary summary = new TestCollectionSummary();
        summary.setRepositories(Arrays.asList(repo));
        
        // Persist the scan session
        long scanSessionId = DataPersistenceService.persistScanSession(summary, 1000L);
        
        // Verify repository was persisted
        verifyRepositoryData(scanSessionId, repo);
    }

    @Test
    public void testPersistTestClasses() throws SQLException {
        // Create test data with test classes
        RepositoryTestInfo repo = createTestRepositoryWithClasses();
        TestCollectionSummary summary = new TestCollectionSummary();
        summary.setRepositories(Arrays.asList(repo));
        
        // Persist the scan session
        long scanSessionId = DataPersistenceService.persistScanSession(summary, 2000L);
        
        // Verify test classes were persisted
        verifyTestClassesData(scanSessionId, repo);
    }

    @Test
    public void testPersistTestMethods() throws SQLException {
        // Create test data with test methods
        RepositoryTestInfo repo = createTestRepositoryWithMethods();
        TestCollectionSummary summary = new TestCollectionSummary();
        summary.setRepositories(Arrays.asList(repo));
        
        // Persist the scan session
        long scanSessionId = DataPersistenceService.persistScanSession(summary, 2500L);
        
        // Verify test methods were persisted
        verifyTestMethodsData(scanSessionId, repo);
    }

    @Test
    public void testPersistAnnotatedMethods() throws SQLException {
        // Create test data with annotated methods
        RepositoryTestInfo repo = createTestRepositoryWithAnnotations();
        TestCollectionSummary summary = new TestCollectionSummary();
        summary.setRepositories(Arrays.asList(repo));
        
        // Persist the scan session
        long scanSessionId = DataPersistenceService.persistScanSession(summary, 3000L);
        
        // Verify annotated methods were persisted
        verifyAnnotatedMethodsData(scanSessionId, repo);
    }

    @Test
    public void testUpdateDailyMetrics() throws SQLException {
        // Create test data
        TestCollectionSummary summary = createTestSummary();
        
        // Persist the scan session
        DataPersistenceService.persistScanSession(summary, 1500L);
        
        // Verify daily metrics were updated
        verifyDailyMetrics(summary);
    }

    @Test
    public void testTransactionRollback() throws SQLException {
        // Create test data
        TestCollectionSummary summary = createTestSummary();
        
        // Manually test transaction rollback by causing an error
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            // Insert scan session
            String sql = "INSERT INTO scan_sessions (scan_date, scan_directory, total_repositories, " +
                         "total_test_classes, total_test_methods, total_annotated_methods, scan_duration_ms) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
            long scanSessionId = 0;
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setTimestamp(1, new java.sql.Timestamp(summary.getScanTimestamp()));
                stmt.setString(2, summary.getScanDirectory());
                stmt.setInt(3, summary.getTotalRepositories());
                stmt.setInt(4, summary.getTotalTestClasses());
                stmt.setInt(5, summary.getTotalTestMethods());
                stmt.setInt(6, summary.getTotalAnnotatedTestMethods());
                stmt.setLong(7, 1500L);
                
                ResultSet rs = stmt.executeQuery();
                assertTrue("Should get scan session ID", rs.next());
                scanSessionId = rs.getLong(1);
                
                // Now try to insert invalid data to cause rollback
                stmt.execute("INSERT INTO invalid_table (id) VALUES (1)");
                fail("Should have thrown exception for invalid table");
                
            } catch (SQLException e) {
                // Expected - rollback should occur
                conn.rollback();
                
                // Verify no data was persisted
                try (var checkStmt = conn.createStatement()) {
                    ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM scan_sessions WHERE id = " + Long.toString(scanSessionId));
                    rs.next();
                    assertEquals("No scan session should exist after rollback", 0, rs.getInt(1));
                }
            }
        }
    }

    /**
     * Create a test TestCollectionSummary
     */
    private TestCollectionSummary createTestSummary() {
        TestCollectionSummary summary = new TestCollectionSummary("/test/scan/directory");
        summary.setScanTimestamp(System.currentTimeMillis());
        
        List<RepositoryTestInfo> repos = new ArrayList<>();
        repos.add(createTestRepository());
        repos.add(createTestRepository2());
        summary.setRepositories(repos);
        
        return summary;
    }

    /**
     * Create a test RepositoryTestInfo
     */
    private RepositoryTestInfo createTestRepository() {
        RepositoryTestInfo repo = new RepositoryTestInfo("test-repo-1", "/test/repo1");
        
        List<TestClassInfo> testClasses = new ArrayList<>();
        testClasses.add(createTestClass("TestClass1", "com.example.test"));
        testClasses.add(createTestClass("TestClass2", "com.example.test"));
        testClasses.add(createTestClass("TestClass3", "com.example.test"));
        repo.setTestClasses(testClasses);
        
        return repo;
    }

    /**
     * Create a second test RepositoryTestInfo
     */
    private RepositoryTestInfo createTestRepository2() {
        RepositoryTestInfo repo = new RepositoryTestInfo("test-repo-2", "/test/repo2");
        
        List<TestClassInfo> testClasses = new ArrayList<>();
        testClasses.add(createTestClass("TestClass4", "com.example.test2"));
        testClasses.add(createTestClass("TestClass5", "com.example.test2"));
        repo.setTestClasses(testClasses);
        
        return repo;
    }

    /**
     * Create a test RepositoryTestInfo with test classes
     */
    private RepositoryTestInfo createTestRepositoryWithClasses() {
        RepositoryTestInfo repo = new RepositoryTestInfo();
        repo.setRepositoryName("test-repo-classes");
        repo.setRepositoryPath("/test/repo-classes");
        repo.setTotalTestClasses(2);
        repo.setTotalTestMethods(8);
        repo.setTotalAnnotatedTestMethods(4);
        
        List<TestClassInfo> testClasses = new ArrayList<>();
        testClasses.add(createTestClassWithMethods("TestClassWithMethods1", "com.example.test"));
        testClasses.add(createTestClassWithMethods("TestClassWithMethods2", "com.example.test"));
        repo.setTestClasses(testClasses);
        
        return repo;
    }

    /**
     * Create a test RepositoryTestInfo with test methods
     */
    private RepositoryTestInfo createTestRepositoryWithMethods() {
        RepositoryTestInfo repo = new RepositoryTestInfo();
        repo.setRepositoryName("test-repo-methods");
        repo.setRepositoryPath("/test/repo-methods");
        repo.setTotalTestClasses(1);
        repo.setTotalTestMethods(5);
        repo.setTotalAnnotatedTestMethods(3);
        
        List<TestClassInfo> testClasses = new ArrayList<>();
        testClasses.add(createTestClassWithMethods("TestClassMethods", "com.example.test"));
        repo.setTestClasses(testClasses);
        
        return repo;
    }

    /**
     * Create a test RepositoryTestInfo with annotations
     */
    private RepositoryTestInfo createTestRepositoryWithAnnotations() {
        RepositoryTestInfo repo = new RepositoryTestInfo();
        repo.setRepositoryName("test-repo-annotations");
        repo.setRepositoryPath("/test/repo-annotations");
        repo.setTotalTestClasses(1);
        repo.setTotalTestMethods(3);
        repo.setTotalAnnotatedTestMethods(3);
        
        List<TestClassInfo> testClasses = new ArrayList<>();
        testClasses.add(createTestClassWithAnnotations("TestClassAnnotations", "com.example.test"));
        repo.setTestClasses(testClasses);
        
        return repo;
    }

    /**
     * Create a test TestClassInfo
     */
    private TestClassInfo createTestClass(String className, String packageName) {
        TestClassInfo testClass = new TestClassInfo();
        testClass.setClassName(className);
        testClass.setPackageName(packageName);
        testClass.setFilePath("/test/" + className + ".java");
        testClass.setTotalTestMethods(5);
        testClass.setAnnotatedTestMethods(3);
        
        List<TestMethodInfo> methods = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            TestMethodInfo method = new TestMethodInfo();
            method.setMethodName("testMethod" + i);
            method.setClassName(className);
            method.setPackageName(packageName);
            method.setLineNumber(10 + i);
            method.setAnnotationData(i <= 3 ? createTestAnnotation() : null);
            methods.add(method);
        }
        testClass.setTestMethods(methods);
        
        return testClass;
    }

    /**
     * Create a test TestClassInfo with test methods
     */
    private TestClassInfo createTestClassWithMethods(String className, String packageName) {
        TestClassInfo testClass = new TestClassInfo();
        testClass.setClassName(className);
        testClass.setPackageName(packageName);
        testClass.setFilePath("/test/" + className + ".java");
        testClass.setTotalTestMethods(4);
        testClass.setAnnotatedTestMethods(2);
        
        List<TestMethodInfo> methods = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            TestMethodInfo method = new TestMethodInfo();
            method.setMethodName("testMethod" + i);
            method.setClassName(className);
            method.setPackageName(packageName);
            method.setLineNumber(20 + i);
            method.setAnnotationData(i <= 2 ? createTestAnnotation() : null);
            methods.add(method);
        }
        testClass.setTestMethods(methods);
        
        return testClass;
    }

    /**
     * Create a test TestClassInfo with annotations
     */
    private TestClassInfo createTestClassWithAnnotations(String className, String packageName) {
        TestClassInfo testClass = new TestClassInfo();
        testClass.setClassName(className);
        testClass.setPackageName(packageName);
        testClass.setFilePath("/test/" + className + ".java");
        testClass.setTotalTestMethods(3);
        testClass.setAnnotatedTestMethods(3);
        
        List<TestMethodInfo> methods = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            TestMethodInfo method = new TestMethodInfo();
            method.setMethodName("testMethod" + i);
            method.setClassName(className);
            method.setPackageName(packageName);
            method.setLineNumber(30 + i);
            method.setAnnotationData(createTestAnnotation());
            methods.add(method);
        }
        testClass.setTestMethods(methods);
        
        return testClass;
    }

    /**
     * Create a test UnittestCaseInfoData
     */
    private UnittestCaseInfoData createTestAnnotation() {
        UnittestCaseInfoData annotation = new UnittestCaseInfoData();
        annotation.setTitle("Test Case " + System.currentTimeMillis());
        annotation.setAuthor("Test Author");
        annotation.setStatus("PASSED");
        annotation.setTargetClass("TargetClass");
        annotation.setTargetMethod("targetMethod");
        annotation.setDescription("Test description");
        annotation.setTags(new String[]{"unit", "test"});
        annotation.setTestPoints(new String[]{"TP001", "TP002"});
        annotation.setRelatedRequirements(new String[]{"REQ001", "REQ002"});
        return annotation;
    }

    /**
     * Verify scan session data was persisted correctly
     */
    private void verifyScanSessionData(long scanSessionId, TestCollectionSummary summary) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT * FROM scan_sessions WHERE id = ?")) {
            
            stmt.setLong(1, scanSessionId);
            ResultSet rs = stmt.executeQuery();
            
            assertTrue("Scan session should exist", rs.next());
            assertEquals("Scan directory should match", summary.getScanDirectory(), rs.getString("scan_directory"));
            assertEquals("Total repositories should match", summary.getTotalRepositories(), rs.getInt("total_repositories"));
            assertEquals("Total test classes should match", summary.getTotalTestClasses(), rs.getInt("total_test_classes"));
            assertEquals("Total test methods should match", summary.getTotalTestMethods(), rs.getInt("total_test_methods"));
            assertEquals("Total annotated methods should match", summary.getTotalAnnotatedTestMethods(), rs.getInt("total_annotated_methods"));
        }
    }

    /**
     * Verify repository data was persisted correctly
     */
    private void verifyRepositoryData(long scanSessionId, RepositoryTestInfo repo) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT * FROM repositories WHERE repository_name = ?")) {
            
            stmt.setString(1, repo.getRepositoryName());
            ResultSet rs = stmt.executeQuery();
            
            assertTrue("Repository should exist", rs.next());
            assertEquals("Repository path should match", repo.getRepositoryPath(), rs.getString("repository_path"));
            assertEquals("Total test classes should match", repo.getTotalTestClasses(), rs.getInt("total_test_classes"));
            assertEquals("Total test methods should match", repo.getTotalTestMethods(), rs.getInt("total_test_methods"));
            assertEquals("Total annotated methods should match", repo.getTotalAnnotatedTestMethods(), rs.getInt("total_annotated_methods"));
        }
    }

    /**
     * Verify test classes data was persisted correctly
     */
    private void verifyTestClassesData(long scanSessionId, RepositoryTestInfo repo) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT tc.* FROM test_classes tc " +
                                            "JOIN repositories r ON tc.repository_id = r.id " +
                                            "WHERE r.repository_name = ?")) {
            
            stmt.setString(1, repo.getRepositoryName());
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                assertNotNull("Class name should not be null", rs.getString("class_name"));
                assertNotNull("Package name should not be null", rs.getString("package_name"));
            }
            assertEquals("Should have correct number of test classes", repo.getTotalTestClasses(), count);
        }
    }

    /**
     * Verify test methods data was persisted correctly
     */
    private void verifyTestMethodsData(long scanSessionId, RepositoryTestInfo repo) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT tm.* FROM test_methods tm " +
                                            "JOIN test_classes tc ON tm.test_class_id = tc.id " +
                                            "JOIN repositories r ON tc.repository_id = r.id " +
                                            "WHERE r.repository_name = ?")) {
            
            stmt.setString(1, repo.getRepositoryName());
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                assertNotNull("Method name should not be null", rs.getString("method_name"));
                assertTrue("Line number should be positive", rs.getInt("line_number") > 0);
            }
            assertEquals("Should have correct number of test methods", repo.getTotalTestMethods(), count);
        }
    }

    /**
     * Verify annotated methods data was persisted correctly
     */
    private void verifyAnnotatedMethodsData(long scanSessionId, RepositoryTestInfo repo) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT tm.* FROM test_methods tm " +
                                            "JOIN test_classes tc ON tm.test_class_id = tc.id " +
                                            "JOIN repositories r ON tc.repository_id = r.id " +
                                            "WHERE r.repository_name = ? AND tm.has_annotation = true")) {
            
            stmt.setString(1, repo.getRepositoryName());
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                assertTrue("Method should have annotation", rs.getBoolean("has_annotation"));
                assertNotNull("Annotation title should not be null", rs.getString("annotation_title"));
                assertNotNull("Annotation author should not be null", rs.getString("annotation_author"));
            }
            assertEquals("Should have correct number of annotated methods", repo.getTotalAnnotatedTestMethods(), count);
        }
    }

    /**
     * Verify daily metrics were updated correctly
     */
    private void verifyDailyMetrics(TestCollectionSummary summary) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement("SELECT * FROM daily_metrics WHERE metric_date = CURRENT_DATE")) {
            
            ResultSet rs = stmt.executeQuery();
            
            assertTrue("Daily metrics should exist for today", rs.next());
            assertEquals("Total repositories should match", summary.getTotalRepositories(), rs.getInt("total_repositories"));
            assertEquals("Total test classes should match", summary.getTotalTestClasses(), rs.getInt("total_test_classes"));
            assertEquals("Total test methods should match", summary.getTotalTestMethods(), rs.getInt("total_test_methods"));
            assertEquals("Total annotated methods should match", summary.getTotalAnnotatedTestMethods(), rs.getInt("total_annotated_methods"));
        }
    }
}
