package com.example.annotationextractor.web.dto;

import java.time.LocalDateTime;

/**
 * DTO for test class summary data
 */
public class TestClassSummaryDto {
    private Long id;
    private String className;
    private String packageName;
    private String filePath;
    private int testMethodCount;
    private int annotatedMethodCount;
    private double coverageRate;
    private LocalDateTime firstSeenDate;
    private LocalDateTime lastModifiedDate;

    // Constructors
    public TestClassSummaryDto() {}

    public TestClassSummaryDto(Long id, String className, String packageName) {
        this.id = id;
        this.className = className;
        this.packageName = packageName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getTestMethodCount() {
        return testMethodCount;
    }

    public void setTestMethodCount(int testMethodCount) {
        this.testMethodCount = testMethodCount;
    }

    public int getAnnotatedMethodCount() {
        return annotatedMethodCount;
    }

    public void setAnnotatedMethodCount(int annotatedMethodCount) {
        this.annotatedMethodCount = annotatedMethodCount;
    }

    public double getCoverageRate() {
        return coverageRate;
    }

    public void setCoverageRate(double coverageRate) {
        this.coverageRate = coverageRate;
    }

    public LocalDateTime getFirstSeenDate() {
        return firstSeenDate;
    }

    public void setFirstSeenDate(LocalDateTime firstSeenDate) {
        this.firstSeenDate = firstSeenDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
