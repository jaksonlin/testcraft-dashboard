package com.example.annotationextractor.service;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.runner.RepositoryHubScanner;
import com.example.annotationextractor.util.GitRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service for scheduled repository scanning
 * Runs daily scans to keep the dashboard data up to date
 */
@Service
public class ScheduledScanService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledScanService.class);

    @Value("${testcraft.scanner.repository-hub-path:./repositories}")
    private String repositoryHubPath;

    @Value("${testcraft.scanner.repository-list-file:./sample-repositories.txt}")
    private String repositoryListFile;

    @Value("${testcraft.scanner.temp-clone-mode:false}")
    private boolean tempCloneMode;

    /**
     * Daily scheduled scan - runs at 2 AM
     * Configured via application.yml: testcraft.scheduler.cron.daily-scan
     */
    @Scheduled(cron = "${testcraft.scheduler.cron.daily-scan:0 0 2 * * ?}")
    public void performDailyScan() {
        logger.info("Starting scheduled daily repository scan");
        
        try {
            // Initialize database connection
            DatabaseConfig.initialize();
            
            // Create git manager and scanner
            GitRepositoryManager gitManager = new GitRepositoryManager(
                repositoryHubPath, 
                null, // username - use SSH keys
                null, // password - use SSH keys
                null  // sshKeyPath - use default
            );
            
            RepositoryHubScanner scanner = new RepositoryHubScanner(gitManager);
            
            // Execute the scan
            boolean success = scanner.executeFullScan(tempCloneMode);
            
            if (success) {
                logger.info("Scheduled daily scan completed successfully");
            } else {
                logger.error("Scheduled daily scan failed");
            }
            
        } catch (Exception e) {
            logger.error("Error during scheduled scan", e);
        }
    }

    /**
     * Manual scan trigger - can be called via REST API
     */
    public boolean triggerManualScan() {
        logger.info("Starting manual repository scan");
        
        try {
            // Initialize database connection
            DatabaseConfig.initialize();
            
            // Create git manager and scanner
            GitRepositoryManager gitManager = new GitRepositoryManager(
                repositoryHubPath, 
                null, // username - use SSH keys
                null, // password - use SSH keys
                null  // sshKeyPath - use default
            );
            
            RepositoryHubScanner scanner = new RepositoryHubScanner(gitManager);
            
            // Execute the scan
            boolean success = scanner.executeFullScan(tempCloneMode);
            
            if (success) {
                logger.info("Manual scan completed successfully");
            } else {
                logger.error("Manual scan failed");
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error during manual scan", e);
            return false;
        }
    }
}
