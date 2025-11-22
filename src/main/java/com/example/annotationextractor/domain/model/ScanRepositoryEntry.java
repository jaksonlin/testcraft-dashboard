package com.example.annotationextractor.domain.model;

import java.util.Objects;

/**
 * Represents a single repository entry used during scanning.
 */
public class ScanRepositoryEntry {

    private final String repositoryUrl;
    private final String teamName;
    private final String teamCode;
    private final boolean active;

    public ScanRepositoryEntry(String repositoryUrl, String teamName, String teamCode) {
        this(repositoryUrl, teamName, teamCode, true);
    }

    public ScanRepositoryEntry(String repositoryUrl, String teamName, String teamCode, boolean active) {
        this.repositoryUrl = Objects.requireNonNull(repositoryUrl, "repositoryUrl").trim();
        this.teamName = Objects.requireNonNull(teamName, "teamName").trim();
        this.teamCode = Objects.requireNonNull(teamCode, "teamCode").trim();
        this.active = active;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public boolean isActive() {
        return active;
    }

    public ScanRepositoryEntry withActive(boolean newActiveState) {
        return new ScanRepositoryEntry(repositoryUrl, teamName, teamCode, newActiveState);
    }
}

