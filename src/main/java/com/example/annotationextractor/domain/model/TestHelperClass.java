package com.example.annotationextractor.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain model representing a helper/test-related class in a repository's test directory.
 */
public final class TestHelperClass {

    private final Long id;
    private final Long repositoryId;
    private final String className;
    private final String packageName;
    private final String filePath;
    private final Integer classLineNumber;
    private final String helperClassContent;
    private final int loc;
    private final Instant firstSeenDate;
    private final Instant lastModifiedDate;
    private final Long scanSessionId;

    public TestHelperClass(Long id,
                          Long repositoryId,
                          String className,
                          String packageName,
                          String filePath,
                          Integer classLineNumber,
                          String helperClassContent,
                          int loc,
                          Instant firstSeenDate,
                          Instant lastModifiedDate,
                          Long scanSessionId) {
        this.id = id;
        this.repositoryId = repositoryId;
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
        this.classLineNumber = classLineNumber;
        this.helperClassContent = helperClassContent;
        this.loc = loc;
        this.firstSeenDate = firstSeenDate;
        this.lastModifiedDate = lastModifiedDate;
        this.scanSessionId = scanSessionId;
    }

    public Long getId() { return id; }
    public Long getRepositoryId() { return repositoryId; }
    public String getClassName() { return className; }
    public String getPackageName() { return packageName; }
    public String getFilePath() { return filePath; }
    public Integer getClassLineNumber() { return classLineNumber; }
    public String getHelperClassContent() { return helperClassContent; }
    public int getLoc() { return loc; }
    public Instant getFirstSeenDate() { return firstSeenDate; }
    public Instant getLastModifiedDate() { return lastModifiedDate; }
    public Long getScanSessionId() { return scanSessionId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestHelperClass that = (TestHelperClass) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

