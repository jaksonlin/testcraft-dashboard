package com.example.annotationextractor.service;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.runner.RepositoryHubScanner;
import com.example.annotationextractor.util.GitRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

    // Thread-safe state tracking
    private final AtomicBoolean isScanning = new AtomicBoolean(false);
    private final AtomicReference<LocalDateTime> lastScanTime = new AtomicReference<>();
    private final AtomicReference<String> lastScanStatus = new AtomicReference<>("Never run");
    private final AtomicReference<String> lastScanError = new AtomicReference<>();

    /**
     * Daily scheduled scan - runs at 2 AM
     * Configured via application.yml: testcraft.scheduler.cron.daily-scan
     */
    @Scheduled(cron = "${testcraft.scheduler.cron.daily-scan:0 0 2 * * ?}")
    public void performDailyScan() {
        logger.info("Starting scheduled daily repository scan");
        
        // Prevent concurrent scans
        if (!isScanning.compareAndSet(false, true)) {
            logger.warn("Scan already in progress, skipping scheduled scan");
            return;
        }
        
        try {
            LocalDateTime startTime = LocalDateTime.now();
            lastScanTime.set(startTime);
            lastScanError.set(null);
            
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
                lastScanStatus.set("Success");
                logger.info("Scheduled daily scan completed successfully");
            } else {
                lastScanStatus.set("Failed");
                logger.error("Scheduled daily scan failed");
            }
            
        } catch (Exception e) {
            lastScanStatus.set("Error");
            lastScanError.set(e.getMessage());
            logger.error("Error during scheduled scan", e);
        } finally {
            isScanning.set(false);
        }
    }

    /**
     * Manual scan trigger - can be called via REST API
     */
    public boolean triggerManualScan() {
        logger.info("Starting manual repository scan");
        
        // Prevent concurrent scans
        if (!isScanning.compareAndSet(false, true)) {
            logger.warn("Scan already in progress, cannot start manual scan");
            return false;
        }
        
        try {
            LocalDateTime startTime = LocalDateTime.now();
            lastScanTime.set(startTime);
            lastScanError.set(null);
            
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
                lastScanStatus.set("Success");
                logger.info("Manual scan completed successfully");
            } else {
                lastScanStatus.set("Failed");
                logger.error("Manual scan failed");
            }
            
            return success;
            
        } catch (Exception e) {
            lastScanStatus.set("Error");
            lastScanError.set(e.getMessage());
            logger.error("Error during manual scan", e);
            return false;
        } finally {
            isScanning.set(false);
        }
    }

    /**
     * Get current scan status and configuration
     */
    public ScanStatus getScanStatus() {
        return new ScanStatus(
            isScanning.get(),
            lastScanTime.get(),
            lastScanStatus.get(),
            lastScanError.get(),
            repositoryHubPath,
            repositoryListFile,
            tempCloneMode
        );
    }

    /**
     * Data class for scan status information
     */
    public static class ScanStatus {
        private final boolean isScanning;
        private final LocalDateTime lastScanTime;
        private final String lastScanStatus;
        private final String lastScanError;
        private final String repositoryHubPath;
        private final String repositoryListFile;
        private final boolean tempCloneMode;

        public ScanStatus(boolean isScanning, LocalDateTime lastScanTime, String lastScanStatus, 
                         String lastScanError, String repositoryHubPath, String repositoryListFile, 
                         boolean tempCloneMode) {
            this.isScanning = isScanning;
            this.lastScanTime = lastScanTime;
            this.lastScanStatus = lastScanStatus;
            this.lastScanError = lastScanError;
            this.repositoryHubPath = repositoryHubPath;
            this.repositoryListFile = repositoryListFile;
            this.tempCloneMode = tempCloneMode;
        }

        // Getters
        public boolean isScanning() { return isScanning; }
        public LocalDateTime getLastScanTime() { return lastScanTime; }
        public String getLastScanStatus() { return lastScanStatus; }
        public String getLastScanError() { return lastScanError; }
        public String getRepositoryHubPath() { return repositoryHubPath; }
        public String getRepositoryListFile() { return repositoryListFile; }
        public boolean isTempCloneMode() { return tempCloneMode; }
    }
}
