package com.example.annotationextractor.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain model representing a scan session.
 */
public final class ScanSession {

    private final Long id;
    private final Instant scanDate;
    private final String scanDirectory;
    private final int totalRepositories;
    private final int totalTestClasses;
    private final int totalTestMethods;
    private final int totalAnnotatedMethods;
    private final long scanDurationMs;
    private final String scanStatus;
    private final String errorLog;
    private final String metadata;

    public ScanSession(Long id,
                       Instant scanDate,
                       String scanDirectory,
                       int totalRepositories,
                       int totalTestClasses,
                       int totalTestMethods,
                       int totalAnnotatedMethods,
                       long scanDurationMs,
                       String scanStatus,
                       String errorLog,
                       String metadata) {
        this.id = id;
        this.scanDate = scanDate;
        this.scanDirectory = scanDirectory;
        this.totalRepositories = totalRepositories;
        this.totalTestClasses = totalTestClasses;
        this.totalTestMethods = totalTestMethods;
        this.totalAnnotatedMethods = totalAnnotatedMethods;
        this.scanDurationMs = scanDurationMs;
        this.scanStatus = scanStatus;
        this.errorLog = errorLog;
        this.metadata = metadata;
    }

    public Long getId() { return id; }
    public Instant getScanDate() { return scanDate; }
    public String getScanDirectory() { return scanDirectory; }
    public int getTotalRepositories() { return totalRepositories; }
    public int getTotalTestClasses() { return totalTestClasses; }
    public int getTotalTestMethods() { return totalTestMethods; }
    public int getTotalAnnotatedMethods() { return totalAnnotatedMethods; }
    public long getScanDurationMs() { return scanDurationMs; }
    public String getScanStatus() { return scanStatus; }
    public String getErrorLog() { return errorLog; }
    public String getMetadata() { return metadata; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScanSession that = (ScanSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


