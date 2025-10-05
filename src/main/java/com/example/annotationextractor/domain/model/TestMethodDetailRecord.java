package com.example.annotationextractor.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain model for test method details matching Excel Test Method Details sheet format
 */
public class TestMethodDetailRecord {
    
    private final Long id;
    private final String repositoryName;
    private final String testClassName;
    private final String testMethodName;
    private final Integer lineNumber;
    private final String annotationTitle;
    private final String annotationAuthor;
    private final String annotationStatus;
    private final String annotationTargetClass;
    private final String annotationTargetMethod;
    private final String annotationDescription;
    private final String annotationTestPoints;
    private final List<String> annotationTags;
    private final List<String> annotationRequirements;
    private final List<String> annotationTestcases;
    private final List<String> annotationDefects;
    private final LocalDateTime annotationLastUpdateTime;
    private final String annotationLastUpdateAuthor;
    private final String teamName;
    private final String teamCode;
    private final String gitUrl;
    
    public TestMethodDetailRecord(Long id, String repositoryName, String testClassName, String testMethodName,
                                Integer lineNumber, String annotationTitle, String annotationAuthor, String annotationStatus,
                                String annotationTargetClass, String annotationTargetMethod, String annotationDescription,
                                String annotationTestPoints, List<String> annotationTags, List<String> annotationRequirements,
                                List<String> annotationTestcases, List<String> annotationDefects, LocalDateTime annotationLastUpdateTime,
                                String annotationLastUpdateAuthor, String teamName, String teamCode, String gitUrl) {
        this.id = id;
        this.repositoryName = repositoryName;
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.lineNumber = lineNumber;
        this.annotationTitle = annotationTitle;
        this.annotationAuthor = annotationAuthor;
        this.annotationStatus = annotationStatus;
        this.annotationTargetClass = annotationTargetClass;
        this.annotationTargetMethod = annotationTargetMethod;
        this.annotationDescription = annotationDescription;
        this.annotationTestPoints = annotationTestPoints;
        this.annotationTags = annotationTags;
        this.annotationRequirements = annotationRequirements;
        this.annotationTestcases = annotationTestcases;
        this.annotationDefects = annotationDefects;
        this.annotationLastUpdateTime = annotationLastUpdateTime;
        this.annotationLastUpdateAuthor = annotationLastUpdateAuthor;
        this.teamName = teamName;
        this.teamCode = teamCode;
        this.gitUrl = gitUrl;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getRepositoryName() { return repositoryName; }
    public String getTestClassName() { return testClassName; }
    public String getTestMethodName() { return testMethodName; }
    public Integer getLineNumber() { return lineNumber; }
    public String getAnnotationTitle() { return annotationTitle; }
    public String getAnnotationAuthor() { return annotationAuthor; }
    public String getAnnotationStatus() { return annotationStatus; }
    public String getAnnotationTargetClass() { return annotationTargetClass; }
    public String getAnnotationTargetMethod() { return annotationTargetMethod; }
    public String getAnnotationDescription() { return annotationDescription; }
    public String getAnnotationTestPoints() { return annotationTestPoints; }
    public List<String> getAnnotationTags() { return annotationTags; }
    public List<String> getAnnotationRequirements() { return annotationRequirements; }
    public List<String> getAnnotationTestcases() { return annotationTestcases; }
    public List<String> getAnnotationDefects() { return annotationDefects; }
    public LocalDateTime getAnnotationLastUpdateTime() { return annotationLastUpdateTime; }
    public String getAnnotationLastUpdateAuthor() { return annotationLastUpdateAuthor; }
    public String getTeamName() { return teamName; }
    public String getTeamCode() { return teamCode; }
    public String getGitUrl() { return gitUrl; }
}
