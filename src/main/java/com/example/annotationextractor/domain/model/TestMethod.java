package com.example.annotationextractor.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain model representing a test method and its annotation metadata.
 */
public final class TestMethod {

    private final Long id;
    private final Long testClassId;
    private final String methodName;
    private final String methodSignature;
    private final Integer lineNumber;
    private final boolean hasAnnotation;
    private final String annotationData;
    private final String annotationTitle;
    private final String annotationAuthor;
    private final String annotationStatus;
    private final String annotationTargetClass;
    private final String annotationTargetMethod;
    private final String annotationDescription;
    private final String annotationTags;
    private final String annotationTestPoints;
    private final String annotationRequirements;
    private final String annotationDefects;
    private final String annotationTestcases;
    private final String annotationLastUpdateTime;
    private final String annotationLastUpdateAuthor;
    private final Instant firstSeenDate;
    private final Instant lastModifiedDate;
    private final Long scanSessionId;

    public TestMethod(Long id,
                      Long testClassId,
                      String methodName,
                      String methodSignature,
                      Integer lineNumber,
                      boolean hasAnnotation,
                      String annotationData,
                      String annotationTitle,
                      String annotationAuthor,
                      String annotationStatus,
                      String annotationTargetClass,
                      String annotationTargetMethod,
                      String annotationDescription,
                      String annotationTags,
                      String annotationTestPoints,
                      String annotationRequirements,
                      String annotationDefects,
                      String annotationTestcases,
                      String annotationLastUpdateTime,
                      String annotationLastUpdateAuthor,
                      Instant firstSeenDate,
                      Instant lastModifiedDate,
                      Long scanSessionId) {
        this.id = id;
        this.testClassId = testClassId;
        this.methodName = methodName;
        this.methodSignature = methodSignature;
        this.lineNumber = lineNumber;
        this.hasAnnotation = hasAnnotation;
        this.annotationData = annotationData;
        this.annotationTitle = annotationTitle;
        this.annotationAuthor = annotationAuthor;
        this.annotationStatus = annotationStatus;
        this.annotationTargetClass = annotationTargetClass;
        this.annotationTargetMethod = annotationTargetMethod;
        this.annotationDescription = annotationDescription;
        this.annotationTags = annotationTags;
        this.annotationTestPoints = annotationTestPoints;
        this.annotationRequirements = annotationRequirements;
        this.annotationDefects = annotationDefects;
        this.annotationTestcases = annotationTestcases;
        this.annotationLastUpdateTime = annotationLastUpdateTime;
        this.annotationLastUpdateAuthor = annotationLastUpdateAuthor;
        this.firstSeenDate = firstSeenDate;
        this.lastModifiedDate = lastModifiedDate;
        this.scanSessionId = scanSessionId;
    }

    public Long getId() { return id; }
    public Long getTestClassId() { return testClassId; }
    public String getMethodName() { return methodName; }
    public String getMethodSignature() { return methodSignature; }
    public Integer getLineNumber() { return lineNumber; }
    public boolean isHasAnnotation() { return hasAnnotation; }
    public String getAnnotationData() { return annotationData; }
    public String getAnnotationTitle() { return annotationTitle; }
    public String getAnnotationAuthor() { return annotationAuthor; }
    public String getAnnotationStatus() { return annotationStatus; }
    public String getAnnotationTargetClass() { return annotationTargetClass; }
    public String getAnnotationTargetMethod() { return annotationTargetMethod; }
    public String getAnnotationDescription() { return annotationDescription; }
    public String getAnnotationTags() { return annotationTags; }
    public String getAnnotationTestPoints() { return annotationTestPoints; }
    public String getAnnotationRequirements() { return annotationRequirements; }
    public String getAnnotationDefects() { return annotationDefects; }
    public String getAnnotationTestcases() { return annotationTestcases; }
    public String getAnnotationLastUpdateTime() { return annotationLastUpdateTime; }
    public String getAnnotationLastUpdateAuthor() { return annotationLastUpdateAuthor; }
    public Instant getFirstSeenDate() { return firstSeenDate; }
    public Instant getLastModifiedDate() { return lastModifiedDate; }
    public Long getScanSessionId() { return scanSessionId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestMethod that = (TestMethod) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


