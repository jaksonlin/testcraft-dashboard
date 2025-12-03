package com.example.annotationextractor.service;

import com.example.annotationextractor.application.ScanConfigService;
import com.example.annotationextractor.domain.model.ScanConfig;
import com.example.annotationextractor.domain.model.ScanRepositoryEntry;
import com.example.annotationextractor.runner.RepositoryHubScanner;
import com.example.annotationextractor.util.GitRepositoryManager;
import com.example.annotationextractor.testcase.TestCaseService;
import com.example.annotationextractor.web.dto.ScanConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Service for scheduled repository scanning
 * Runs daily scans to keep the dashboard data up to date
 */
@Service
public class ScheduledScanService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledScanService.class);

    private final ScanConfigService scanConfigService;
    private final TestCaseService testCaseService;
    private final java.util.Optional<com.example.annotationextractor.application.PersistenceReadFacade> persistenceReadFacade;

    // Thread-safe state tracking
    private final AtomicBoolean isScanning = new AtomicBoolean(false);
    private final AtomicReference<LocalDateTime> lastScanTime = new AtomicReference<>();
    private final AtomicReference<String> lastScanStatus = new AtomicReference<>("Never run");
    private final AtomicReference<String> lastScanError = new AtomicReference<>();

    public ScheduledScanService(ScanConfigService scanConfigService, TestCaseService testCaseService,
            java.util.Optional<com.example.annotationextractor.application.PersistenceReadFacade> persistenceReadFacade) {
        this.scanConfigService = scanConfigService;
        this.testCaseService = testCaseService;
        this.persistenceReadFacade = persistenceReadFacade;
    }

    /**
     * Daily scheduled scan - runs at 2 AM
     * Configured via application.yml: testcraft.scheduler.cron.daily-scan
     */
    @Scheduled(cron = "${testcraft.scheduler.cron.daily-scan:0 0 2 * * ?}")
    public void performDailyScan() {
        logger.info("Starting scheduled daily repository scan");

        if (!isScanning.compareAndSet(false, true)) {
            logger.warn("Scan already in progress, skipping scheduled scan");
            return;
        }

        try {
            LocalDateTime startTime = LocalDateTime.now();
            lastScanTime.set(startTime);
            lastScanError.set(null);

            ScanConfig config = scanConfigService.getCurrentConfig();
            if (!config.isSchedulerEnabled()) {
                logger.info("Scheduler disabled in configuration. Skipping scheduled scan.");
                lastScanStatus.set("Skipped (scheduler disabled)");
                return;
            }

            List<ScanRepositoryEntry> repositoryEntries = extractActiveRepositories(config);
            if (repositoryEntries.isEmpty()) {
                logger.warn("No repository entries configured; skipping scheduled scan.");
                lastScanStatus.set("Skipped (no repositories)");
                return;
            }

            GitRepositoryManager gitManager = buildGitManager(config);
            RepositoryHubScanner scanner = new RepositoryHubScanner(
                    gitManager,
                    repositoryEntries,
                    config.getMaxRepositoriesPerScan());

            boolean success = scanner.executeFullScan(config.isTempCloneMode());
            if (success) {
                lastScanStatus.set("Success");
                logger.info("Scheduled daily scan completed successfully");

                // Refresh test case coverage
                testCaseService.refreshCoverage();
            } else {
                lastScanStatus.set("Failed");
                logger.error("Scheduled daily scan failed");
            }

        } catch (SQLException e) {
            lastScanStatus.set("Error");
            lastScanError.set(e.getMessage());
            logger.error("Failed to load scan configuration", e);
        } catch (IOException e) {
            lastScanStatus.set("Error");
            lastScanError.set(e.getMessage());
            logger.error("Failed to initialize repository scanner", e);
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
    public boolean triggerManualScan(List<Long> repositoryIds) {
        logger.info("Starting manual repository scan");

        if (!isScanning.compareAndSet(false, true)) {
            logger.warn("Scan already in progress, cannot start manual scan");
            return false;
        }

        try {
            LocalDateTime startTime = LocalDateTime.now();
            lastScanTime.set(startTime);
            lastScanError.set(null);

            ScanConfig config = scanConfigService.getCurrentConfig();
            List<ScanRepositoryEntry> repositoryEntries = extractActiveRepositories(config);

            // Filter by repository IDs if provided
            if (repositoryIds != null && !repositoryIds.isEmpty()) {
                if (persistenceReadFacade.isPresent()) {
                    java.util.Set<String> targetUrls = new java.util.HashSet<>();
                    for (Long id : repositoryIds) {
                        persistenceReadFacade.get().getRepositoryById(id)
                                .ifPresent(repo -> targetUrls.add(repo.getGitUrl()));
                    }

                    repositoryEntries = repositoryEntries.stream()
                            .filter(entry -> targetUrls.contains(entry.getRepositoryUrl()))
                            .collect(Collectors.toList());
                    logger.info("Filtering scan for {} repositories: {}", repositoryEntries.size(), repositoryIds);
                } else {
                    logger.warn("PersistenceReadFacade not available, cannot filter by ID. Scanning all.");
                }
            }

            if (repositoryEntries.isEmpty()) {
                logger.warn("No repository entries configured or matched; skipping manual scan.");
                lastScanStatus.set("Skipped (no repositories)");
                return false;
            }

            GitRepositoryManager gitManager = buildGitManager(config);
            RepositoryHubScanner scanner = new RepositoryHubScanner(
                    gitManager,
                    repositoryEntries,
                    config.getMaxRepositoriesPerScan());

            boolean success = scanner.executeFullScan(config.isTempCloneMode());
            if (success) {
                lastScanStatus.set("Success");
                logger.info("Manual scan completed successfully");

                // Refresh test case coverage
                testCaseService.refreshCoverage();
            } else {
                lastScanStatus.set("Failed");
                logger.error("Manual scan failed");
            }
            return success;

        } catch (SQLException e) {
            lastScanStatus.set("Error");
            lastScanError.set(e.getMessage());
            logger.error("Failed to load scan configuration", e);
            return false;
        } catch (IOException e) {
            lastScanStatus.set("Error");
            lastScanError.set(e.getMessage());
            logger.error("Failed to initialize repository scanner", e);
            return false;
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
     * Overload for backward compatibility
     */
    public boolean triggerManualScan() {
        return triggerManualScan(null);
    }

    /**
     * Get current scan status and configuration
     */
    public ScanStatus getScanStatus() {
        try {
            ScanConfig config = scanConfigService.getCurrentConfig();
            return new ScanStatus(
                    isScanning.get(),
                    lastScanTime.get(),
                    lastScanStatus.get(),
                    lastScanError.get(),
                    config);
        } catch (Exception e) {
            logger.error("Failed to load scan configuration for status", e);
            lastScanError.compareAndSet(null, e.getMessage());
            return new ScanStatus(
                    isScanning.get(),
                    lastScanTime.get(),
                    "Error",
                    e.getMessage(),
                    null);
        }
    }

    /**
     * Update scan configuration
     */
    public boolean updateScanConfiguration(ScanConfigDto configDto) {
        try {
            scanConfigService.updateConfiguration(configDto);
            logger.info("Scan configuration updated: {}", configDto);
            return true;
        } catch (Exception e) {
            logger.error("Failed to update scan configuration", e);
            return false;
        }
    }

    private GitRepositoryManager buildGitManager(ScanConfig config) {
        String gitUsername = System.getenv("GIT_USERNAME");
        String gitPassword = System.getenv("GIT_PASSWORD");
        String gitSshKeyPath = System.getenv("GIT_SSH_KEY_PATH");
        String repositoryHubOverride = System.getenv("REPOSITORY_HUB_PATH");
        String finalRepoPath = repositoryHubOverride != null ? repositoryHubOverride : config.getRepositoryHubPath();

        return new GitRepositoryManager(
                finalRepoPath,
                gitUsername,
                gitPassword,
                gitSshKeyPath,
                config.getScanBranch());
    }

    private List<ScanRepositoryEntry> extractActiveRepositories(ScanConfig config) {
        return config.getRepositories().stream()
                .filter(ScanRepositoryEntry::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Data class for scan status information
     */
    public static class ScanStatus {
        private final boolean isScanning;
        private final LocalDateTime lastScanTime;
        private final String lastScanStatus;
        private final String lastScanError;
        private final ScanConfig scanConfig;

        public ScanStatus(boolean isScanning, LocalDateTime lastScanTime, String lastScanStatus,
                String lastScanError, ScanConfig scanConfig) {
            this.isScanning = isScanning;
            this.lastScanTime = lastScanTime;
            this.lastScanStatus = lastScanStatus;
            this.lastScanError = lastScanError;
            this.scanConfig = scanConfig;
        }

        public boolean isScanning() {
            return isScanning;
        }

        public LocalDateTime getLastScanTime() {
            return lastScanTime;
        }

        public String getLastScanStatus() {
            return lastScanStatus;
        }

        public String getLastScanError() {
            return lastScanError;
        }

        public String getRepositoryHubPath() {
            return scanConfig != null ? scanConfig.getRepositoryHubPath() : null;
        }

        public String getRepositoryListFile() {
            return scanConfig != null ? scanConfig.getRepositoryListFileLabel() : null;
        }

        public boolean isTempCloneMode() {
            return scanConfig != null && scanConfig.isTempCloneMode();
        }

        public int getMaxRepositoriesPerScan() {
            return scanConfig != null ? scanConfig.getMaxRepositoriesPerScan() : 0;
        }

        public boolean isSchedulerEnabled() {
            return scanConfig != null && scanConfig.isSchedulerEnabled();
        }

        public String getDailyScanCron() {
            return scanConfig != null ? scanConfig.getDailyScanCron() : null;
        }

        public String getRepositoryConfigContent() {
            return scanConfig != null ? scanConfig.getRepositoryConfigContent() : null;
        }

        public List<ScanRepositoryEntry> getRepositories() {
            return scanConfig != null ? scanConfig.getRepositories() : List.of();
        }

        public String getOrganization() {
            return scanConfig != null ? scanConfig.getOrganization() : null;
        }

        public String getScanBranch() {
            return scanConfig != null ? scanConfig.getScanBranch() : null;
        }
    }
}
