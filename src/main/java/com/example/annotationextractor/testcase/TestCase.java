package com.example.annotationextractor.testcase;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a test case imported from Excel or other sources.
 * 
 * This is a flexible design that stores:
 * - Required fields (id, title, steps)
 * - Common optional fields (setup, teardown, expected result, etc.)
 * - Custom organization-specific fields in customFields map
 */
public class TestCase {
    
    // Required fields
    private String id;              // TC-1234, ID-5678, etc.
    private String title;
    private String steps;
    
    // Common optional fields
    private String setup;           // Precondition
    private String teardown;        // Postcondition
    private String expectedResult;
    private String priority;        // High/Medium/Low
    private String type;            // Functional/Integration/Regression
    private String status;          // Active/Deprecated/Draft
    private String[] tags;
    private String[] requirements;
    
    // Custom fields (organization-specific)
    private Map<String, Object> customFields;
    
    // Metadata
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String createdBy;
    private String organization;
    
    public TestCase() {
        this.customFields = new HashMap<>();
        this.tags = new String[0];
        this.requirements = new String[0];
        this.status = "Active";
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
    
    public TestCase(String id, String title, String steps) {
        this();
        this.id = id;
        this.title = title;
        this.steps = steps;
    }
    
    // Validation
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() &&
               title != null && !title.trim().isEmpty() &&
               steps != null && !steps.trim().isEmpty();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSteps() {
        return steps;
    }
    
    public void setSteps(String steps) {
        this.steps = steps;
    }
    
    public String getSetup() {
        return setup;
    }
    
    public void setSetup(String setup) {
        this.setup = setup;
    }
    
    public String getTeardown() {
        return teardown;
    }
    
    public void setTeardown(String teardown) {
        this.teardown = teardown;
    }
    
    public String getExpectedResult() {
        return expectedResult;
    }
    
    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String[] getTags() {
        return tags;
    }
    
    public void setTags(String[] tags) {
        this.tags = tags;
    }
    
    public String[] getRequirements() {
        return requirements;
    }
    
    public void setRequirements(String[] requirements) {
        this.requirements = requirements;
    }
    
    public Map<String, Object> getCustomFields() {
        return customFields;
    }
    
    public void setCustomFields(Map<String, Object> customFields) {
        this.customFields = customFields;
    }
    
    public void addCustomField(String key, Object value) {
        this.customFields.put(key, value);
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getOrganization() {
        return organization;
    }
    
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    @Override
    public String toString() {
        return "TestCase{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

