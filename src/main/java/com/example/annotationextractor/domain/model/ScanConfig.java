package com.example.annotationextractor.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Aggregate representing the scan configuration settings persisted in the database.
 */
public class ScanConfig {

    private final String repositoryHubPath;
    private final boolean tempCloneMode;
    private final int maxRepositoriesPerScan;
    private final boolean schedulerEnabled;
    private final String dailyScanCron;
    private final String repositoryListFileLabel;
    private final String repositoryConfigContent;
    private final List<ScanRepositoryEntry> repositories;
    private final String organization;
    private final String scanBranch;

    public ScanConfig(
            String repositoryHubPath,
            boolean tempCloneMode,
            int maxRepositoriesPerScan,
            boolean schedulerEnabled,
            String dailyScanCron,
            String repositoryListFileLabel,
            String repositoryConfigContent,
            List<ScanRepositoryEntry> repositories,
            String organization,
            String scanBranch) {
        this.repositoryHubPath = Objects.requireNonNull(repositoryHubPath, "repositoryHubPath");
        this.tempCloneMode = tempCloneMode;
        this.maxRepositoriesPerScan = maxRepositoriesPerScan;
        this.schedulerEnabled = schedulerEnabled;
        this.dailyScanCron = Objects.requireNonNull(dailyScanCron, "dailyScanCron");
        this.repositoryListFileLabel = repositoryListFileLabel;
        this.repositoryConfigContent = repositoryConfigContent;
        this.repositories = repositories == null ? List.of() : List.copyOf(repositories);
        this.organization = organization == null ? "" : organization;
        this.scanBranch = (scanBranch == null || scanBranch.isBlank()) ? "main" : scanBranch;
    }

    public String getRepositoryHubPath() {
        return repositoryHubPath;
    }

    public boolean isTempCloneMode() {
        return tempCloneMode;
    }

    public int getMaxRepositoriesPerScan() {
        return maxRepositoriesPerScan;
    }

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public String getDailyScanCron() {
        return dailyScanCron;
    }

    /**
     * Returns a descriptive label explaining that repository definitions are managed in the database.
     * Maintains compatibility with legacy clients expecting a repository list file path.
     */
    public String getRepositoryListFileLabel() {
        return repositoryListFileLabel;
    }

    /**
     * Returns the raw configuration content (CSV style) used by the UI to manage repository entries.
     */
    public String getRepositoryConfigContent() {
        return repositoryConfigContent;
    }

    public List<ScanRepositoryEntry> getRepositories() {
        return repositories;
    }

    public String getOrganization() {
        return organization;
    }

    public String getScanBranch() {
        return scanBranch;
    }
}

