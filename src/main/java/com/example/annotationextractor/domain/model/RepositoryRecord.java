package com.example.annotationextractor.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain model representing a code repository scanned for tests/annotations.
 */
public final class RepositoryRecord {

    private final Long id;
    private final String repositoryName;
    private final String repositoryPath;
    private final String gitUrl;
    private final String gitBranch;
    private final String technologyStack;
    private final Long teamId;
    private final Instant firstScanDate;
    private final Instant lastScanDate;
    private final int totalTestClasses;
    private final int totalTestMethods;
    private final int totalAnnotatedMethods;
    private final double annotationCoverageRate;
    private final int testCodeLines;
    private final int testRelatedCodeLines;

    public RepositoryRecord(Long id,
                            String repositoryName,
                            String repositoryPath,
                            String gitUrl,
                            String gitBranch,
                            String technologyStack,
                            Long teamId,
                            Instant firstScanDate,
                            Instant lastScanDate,
                            int totalTestClasses,
                            int totalTestMethods,
                            int totalAnnotatedMethods,
                            double annotationCoverageRate,
                            int testCodeLines,
                            int testRelatedCodeLines) {
        this.id = id;
        this.repositoryName = repositoryName;
        this.repositoryPath = repositoryPath;
        this.gitUrl = gitUrl;
        this.gitBranch = gitBranch;
        this.technologyStack = technologyStack;
        this.teamId = teamId;
        this.firstScanDate = firstScanDate;
        this.lastScanDate = lastScanDate;
        this.totalTestClasses = totalTestClasses;
        this.totalTestMethods = totalTestMethods;
        this.totalAnnotatedMethods = totalAnnotatedMethods;
        this.annotationCoverageRate = annotationCoverageRate;
        this.testCodeLines = testCodeLines;
        this.testRelatedCodeLines = testRelatedCodeLines;
    }

    public Long getId() { return id; }
    public String getRepositoryName() { return repositoryName; }
    public String getRepositoryPath() { return repositoryPath; }
    public String getGitUrl() { return gitUrl; }
    public String getGitBranch() { return gitBranch; }
    public String getTechnologyStack() { return technologyStack; }
    public Long getTeamId() { return teamId; }
    public Instant getFirstScanDate() { return firstScanDate; }
    public Instant getLastScanDate() { return lastScanDate; }
    public int getTotalTestClasses() { return totalTestClasses; }
    public int getTotalTestMethods() { return totalTestMethods; }
    public int getTotalAnnotatedMethods() { return totalAnnotatedMethods; }
    public double getAnnotationCoverageRate() { return annotationCoverageRate; }
    public int getTestCodeLines() { return testCodeLines; }
    public int getTestRelatedCodeLines() { return testRelatedCodeLines; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepositoryRecord that = (RepositoryRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


