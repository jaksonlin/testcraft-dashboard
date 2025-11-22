package com.example.annotationextractor.web.dto;

/**
 * DTO representing the source code for a specific test method's class and metadata
 * to help the UI scroll to the appropriate line.
 */
public class TestMethodSourceDto {

    private Long testMethodId;
    private String testMethodName;
    private Integer methodLine;

    private Long testClassId;
    private String testClassName;
    private String packageName;
    private String filePath;
    private Integer classLineNumber;
    private String classContent;

    public Long getTestMethodId() {
        return testMethodId;
    }

    public void setTestMethodId(Long testMethodId) {
        this.testMethodId = testMethodId;
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    public void setTestMethodName(String testMethodName) {
        this.testMethodName = testMethodName;
    }

    public Integer getMethodLine() {
        return methodLine;
    }

    public void setMethodLine(Integer methodLine) {
        this.methodLine = methodLine;
    }

    public Long getTestClassId() {
        return testClassId;
    }

    public void setTestClassId(Long testClassId) {
        this.testClassId = testClassId;
    }

    public String getTestClassName() {
        return testClassName;
    }

    public void setTestClassName(String testClassName) {
        this.testClassName = testClassName;
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

    public Integer getClassLineNumber() {
        return classLineNumber;
    }

    public void setClassLineNumber(Integer classLineNumber) {
        this.classLineNumber = classLineNumber;
    }

    public String getClassContent() {
        return classContent;
    }

    public void setClassContent(String classContent) {
        this.classContent = classContent;
    }
}

