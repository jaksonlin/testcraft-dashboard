package com.example.annotationextractor;

import org.junit.Test;

import com.example.annotationextractor.runner.RepositoryHubRunnerConfig;
import com.example.annotationextractor.runner.RepositoryHubScanner;
import com.example.annotationextractor.runner.RepositoryListProcessor;
import com.example.annotationextractor.util.GitRepositoryManager;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Test class for RepositoryHubScanner functionality
 */
public class RepositoryHubScannerTest {
    
    @Test
    public void testRepositoryListProcessor() throws IOException {
        // Test creating and reading a sample repository list
        String testFile = "test-repos.txt";
        
        try {
            // Create a test repository list
            RepositoryListProcessor.createSampleRepositoryList(testFile);
            
            // Verify file was created
            assertTrue("Sample repository list file should exist", Files.exists(Paths.get(testFile)));
            
            // Read the repository list
            List<RepositoryHubRunnerConfig> urls = RepositoryListProcessor.readRepositoryHubRunnerConfigs(testFile);
            
            // Should have some example URLs
            assertTrue("Should have some repository URLs", urls.size() > 0);
            
            // Test statistics
            String stats = RepositoryListProcessor.getFileStatistics(testFile);
            assertNotNull("Statistics should not be null", stats);
            assertTrue("Statistics should contain file information", stats.contains("Total lines"));
            
        } finally {
            // Clean up test file
            try {
                Files.deleteIfExists(Paths.get(testFile));
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }
    
    @Test
    public void testGitRepositoryManagerCreation() {
        // Test that GitRepositoryManager can be created
        GitRepositoryManager manager = new GitRepositoryManager("./test-repos");
        assertNotNull("GitRepositoryManager should not be null", manager);
        assertEquals("Repository hub path should match", "./test-repos", manager.getRepositoryHubPath());
    }
    
    @Test
    public void testRepositoryHubScannerCreation() {
        // Test that RepositoryHubScanner can be created
        RepositoryHubScanner scanner = new RepositoryHubScanner("./test-repos", "test-list.txt");
        assertNotNull("RepositoryHubScanner should not be null", scanner);
        assertNotNull("Git manager should not be null", scanner.getGitManager());
    }
    
    @Test
    public void testUrlValidation() throws IOException {
        // Test URL validation by creating a test file with valid URLs
        String testFile = "test-urls.txt";
        
        try {
            // Create a test file with valid URLs
            List<String> validUrls = List.of(
                "https://github.com/example/repo1.git,team1,team1_code",
                "https://github.com/example/repo2,team2,team2_code",
                "git@github.com:example/repo3.git,team3,team3_code"
            );
            
            Files.write(Paths.get(testFile), validUrls);
            
            // Read and validate
            List<RepositoryHubRunnerConfig> urls = RepositoryListProcessor.readRepositoryHubRunnerConfigs(testFile);
            assertEquals("Should read all valid URLs", 3, urls.size());
            
        } finally {
            // Clean up test file
            try {
                Files.deleteIfExists(Paths.get(testFile));
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    public void testScanningSingleRepository() {
        // Test scanning a single repository
        RepositoryHubScanner scanner = new RepositoryHubScanner("d:/testlab", "d:/testlab/testrepo.txt");
        boolean success = scanner.executeFullScan();
        assertTrue("Scan should be successful", success);
    }
}
