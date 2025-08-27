package com.example.annotationextractor;

import java.util.Arrays;

/**
 * Data model to hold all the extracted values from UnittestCaseInfo annotation
 */
public class UnittestCaseInfoData {
    private String author;
    private String title;
    private String targetClass;
    private String targetMethod;
    private String[] testPoints;
    private String description;
    private String[] tags;
    private String status;
    private String[] relatedRequirements;
    private String[] relatedDefects;
    private String[] relatedTestcases;
    private String lastUpdateTime;
    private String lastUpdateAuthor;
    private String methodSignature;

    // Default constructor
    public UnittestCaseInfoData() {
        // Initialize all fields with default values
        this.author = "";
        this.title = "";
        this.targetClass = "";
        this.targetMethod = "";
        this.testPoints = new String[0];
        this.description = "";
        this.tags = new String[0];
        this.status = "TODO";
        this.relatedRequirements = new String[0];
        this.relatedDefects = new String[0];
        this.relatedTestcases = new String[0];
        this.lastUpdateTime = "";
        this.lastUpdateAuthor = "";
        this.methodSignature = "";
    }

    // Getters and Setters
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String[] getTestPoints() {
        return testPoints;
    }

    public void setTestPoints(String[] testPoints) {
        this.testPoints = testPoints;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String[] getRelatedRequirements() {
        return relatedRequirements;
    }

    public void setRelatedRequirements(String[] relatedRequirements) {
        this.relatedRequirements = relatedRequirements;
    }

    public String[] getRelatedDefects() {
        return relatedDefects;
    }

    public void setRelatedDefects(String[] relatedDefects) {
        this.relatedDefects = relatedDefects;
    }

    public String[] getRelatedTestcases() {
        return relatedTestcases;
    }

    public void setRelatedTestcases(String[] relatedTestcases) {
        this.relatedTestcases = relatedTestcases;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getLastUpdateAuthor() {
        return lastUpdateAuthor;
    }

    public void setLastUpdateAuthor(String lastUpdateAuthor) {
        this.lastUpdateAuthor = lastUpdateAuthor;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    @Override
    public String toString() {
        return "UnittestCaseInfoData{" +
                "author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", targetClass='" + targetClass + '\'' +
                ", targetMethod='" + targetMethod + '\'' +
                ", testPoints=" + Arrays.toString(testPoints) +
                ", description='" + description + '\'' +
                ", tags=" + Arrays.toString(tags) +
                ", status='" + status + '\'' +
                ", relatedRequirements=" + Arrays.toString(relatedRequirements) +
                ", relatedDefects=" + Arrays.toString(relatedDefects) +
                ", relatedTestcases=" + Arrays.toString(relatedTestcases) +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                ", lastUpdateAuthor='" + lastUpdateAuthor + '\'' +
                ", methodSignature='" + methodSignature + '\'' +
                '}';
    }
}
