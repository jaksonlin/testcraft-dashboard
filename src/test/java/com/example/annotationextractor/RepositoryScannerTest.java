package com.example.annotationextractor;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for RepositoryScanner functionality
 */
public class RepositoryScannerTest {
    
    @Test
    public void testScanEmptyDirectory() throws IOException {
        // Create a temporary empty directory
        Path tempDir = Files.createTempDirectory("test-repos");
        
        try {
            TestCollectionSummary summary = RepositoryScanner.scanRepositories(tempDir.toString());
            
            assertNotNull("Summary should not be null", summary);
            assertEquals("Should have correct scan directory", tempDir.toString(), summary.getScanDirectory());
            assertEquals("Should have 0 repositories", 0, summary.getTotalRepositories());
            assertEquals("Should have 0 test classes", 0, summary.getTotalTestClasses());
            assertEquals("Should have 0 test methods", 0, summary.getTotalTestMethods());
            
        } finally {
            // Clean up
            Files.deleteIfExists(tempDir);
        }
    }
    
    @Test
    public void testScanNonExistentDirectory() {
        try {
            RepositoryScanner.scanRepositories("/non/existent/path");
            fail("Should throw IOException for non-existent directory");
        } catch (IOException e) {
            // Expected
            assertTrue("Error message should contain directory path", 
                      e.getMessage().contains("/non/existent/path"));
        }
    }
    
    @Test
    public void testDataModelCreation() {
        // Test the data models can be created and used
        TestMethodInfo methodInfo = new TestMethodInfo();
        methodInfo.setMethodName("testMethod");
        methodInfo.setClassName("TestClass");
        
        TestClassInfo classInfo = new TestClassInfo();
        classInfo.setClassName("TestClass");
        classInfo.addTestMethod(methodInfo);
        
        RepositoryTestInfo repoInfo = new RepositoryTestInfo();
        repoInfo.setRepositoryName("test-repo");
        repoInfo.addTestClass(classInfo);
        
        TestCollectionSummary summary = new TestCollectionSummary();
        summary.addRepository(repoInfo);
        
        assertEquals("Should have 1 repository", 1, summary.getTotalRepositories());
        assertEquals("Should have 1 test class", 1, summary.getTotalTestClasses());
        assertEquals("Should have 1 test method", 1, summary.getTotalTestMethods());
    }
}
