package com.example.annotationextractor.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain model representing a test class inside a repository.
 */
public final class TestClass {

    private final Long id;
    private final Long repositoryId;
    private final String className;
    private final String packageName;
    private final String filePath;
    private final Long fileSizeBytes;
    private final int totalTestMethods;
    private final int annotatedTestMethods;
    private final double coverageRate;
    private final Instant firstSeenDate;
    private final Instant lastModifiedDate;
    private final Long scanSessionId;

    public TestClass(Long id,
                     Long repositoryId,
                     String className,
                     String packageName,
                     String filePath,
                     Long fileSizeBytes,
                     int totalTestMethods,
                     int annotatedTestMethods,
                     double coverageRate,
                     Instant firstSeenDate,
                     Instant lastModifiedDate,
                     Long scanSessionId) {
        this.id = id;
        this.repositoryId = repositoryId;
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
        this.fileSizeBytes = fileSizeBytes;
        this.totalTestMethods = totalTestMethods;
        this.annotatedTestMethods = annotatedTestMethods;
        this.coverageRate = coverageRate;
        this.firstSeenDate = firstSeenDate;
        this.lastModifiedDate = lastModifiedDate;
        this.scanSessionId = scanSessionId;
    }

    public Long getId() { return id; }
    public Long getRepositoryId() { return repositoryId; }
    public String getClassName() { return className; }
    public String getPackageName() { return packageName; }
    public String getFilePath() { return filePath; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public int getTotalTestMethods() { return totalTestMethods; }
    public int getAnnotatedTestMethods() { return annotatedTestMethods; }
    public double getCoverageRate() { return coverageRate; }
    public Instant getFirstSeenDate() { return firstSeenDate; }
    public Instant getLastModifiedDate() { return lastModifiedDate; }
    public Long getScanSessionId() { return scanSessionId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestClass testClass = (TestClass) o;
        return Objects.equals(id, testClass.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


