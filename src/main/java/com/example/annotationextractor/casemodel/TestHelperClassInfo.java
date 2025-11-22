package com.example.annotationextractor.casemodel;

/**
 * Data model to hold information about a helper/test-related class (non-test class in test directory)
 */
public class TestHelperClassInfo {
    private String className;
    private String packageName;
    private String filePath;
    private Integer classLineNumber;
    private String helperClassContent;
    private int loc;

    public TestHelperClassInfo() {
        this.className = "";
        this.packageName = "";
        this.filePath = "";
        this.classLineNumber = null;
        this.helperClassContent = null;
        this.loc = 0;
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

    public Integer getClassLineNumber() {
        return classLineNumber;
    }

    public void setClassLineNumber(Integer classLineNumber) {
        this.classLineNumber = classLineNumber;
    }

    public String getHelperClassContent() {
        return helperClassContent;
    }

    public void setHelperClassContent(String helperClassContent) {
        this.helperClassContent = helperClassContent;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    @Override
    public String toString() {
        return "TestHelperClassInfo{" +
                "className='" + className + '\'' +
                ", packageName='" + packageName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", classLineNumber=" + classLineNumber +
                ", loc=" + loc +
                '}';
    }
}


