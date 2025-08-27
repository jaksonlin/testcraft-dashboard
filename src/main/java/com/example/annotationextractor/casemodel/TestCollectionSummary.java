package com.example.annotationextractor.casemodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level data model to hold information about all repositories scanned and provide summary statistics
 */
public class TestCollectionSummary {
    private String scanDirectory;
    private List<RepositoryTestInfo> repositories;
    private int totalRepositories;
    private int totalTestClasses;
    private int totalTestMethods;
    private int totalAnnotatedTestMethods;
    private long scanTimestamp;

    public TestCollectionSummary() {
        this.scanDirectory = "";
        this.repositories = new ArrayList<>();
        this.totalRepositories = 0;
        this.totalTestClasses = 0;
        this.totalTestMethods = 0;
        this.totalAnnotatedTestMethods = 0;
        this.scanTimestamp = System.currentTimeMillis();
    }

    public TestCollectionSummary(String scanDirectory) {
        this.scanDirectory = scanDirectory;
        this.repositories = new ArrayList<>();
        this.totalRepositories = 0;
        this.totalTestClasses = 0;
        this.totalTestMethods = 0;
        this.totalAnnotatedTestMethods = 0;
        this.scanTimestamp = System.currentTimeMillis();
    }

    public void addRepository(RepositoryTestInfo repository) {
        this.repositories.add(repository);
        this.totalRepositories++;
        this.totalTestClasses += repository.getTotalTestClasses();
        this.totalTestMethods += repository.getTotalTestMethods();
        this.totalAnnotatedTestMethods += repository.getTotalAnnotatedTestMethods();
    }

    // Getters and Setters
    public String getScanDirectory() {
        return scanDirectory;
    }

    public void setScanDirectory(String scanDirectory) {
        this.scanDirectory = scanDirectory;
    }

    public List<RepositoryTestInfo> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<RepositoryTestInfo> repositories) {
        this.repositories = repositories;
        this.totalRepositories = repositories.size();
        this.totalTestClasses = 0;
        this.totalTestMethods = 0;
        this.totalAnnotatedTestMethods = 0;
        for (RepositoryTestInfo repository : repositories) {
            this.totalTestClasses += repository.getTotalTestClasses();
            this.totalTestMethods += repository.getTotalTestMethods();
            this.totalAnnotatedTestMethods += repository.getTotalAnnotatedTestMethods();
        }
    }

    public int getTotalRepositories() {
        return totalRepositories;
    }

    public int getTotalTestClasses() {
        return totalTestClasses;
    }

    public int getTotalTestMethods() {
        return totalTestMethods;
    }

    public int getTotalAnnotatedTestMethods() {
        return totalAnnotatedTestMethods;
    }

    public long getScanTimestamp() {
        return scanTimestamp;
    }

    public void setScanTimestamp(long scanTimestamp) {
        this.scanTimestamp = scanTimestamp;
    }

    @Override
    public String toString() {
        return "TestCollectionSummary{" +
                "scanDirectory='" + scanDirectory + '\'' +
                ", totalRepositories=" + totalRepositories +
                ", totalTestClasses=" + totalTestClasses +
                ", totalTestMethods=" + totalTestMethods +
                ", totalAnnotatedTestMethods=" + totalAnnotatedTestMethods +
                ", scanTimestamp=" + scanTimestamp +
                ", repositories=" + repositories +
                '}';
    }
}
