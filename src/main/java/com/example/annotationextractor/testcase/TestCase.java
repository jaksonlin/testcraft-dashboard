package com.example.annotationextractor.testcase;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a test case imported from Excel or other sources.
 * 
 * This is a flexible design that stores:
 * - Internal ID (auto-generated primary key)
 * - External ID (test case ID from external system like Jira, TestRail, etc.)
 * - Required fields (title, steps)
 * - Common optional fields (setup, teardown, expected result, etc.)
 * - Custom organization-specific fields in customFields map
 */
public class TestCase {
    
    // Database primary key (auto-generated)
    private Long internalId;        // Internal database ID
    
    // Required fields
    private String externalId;      // TC-1234, ID-5678, etc. (from external test management system)
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
    
    // Team association
    private Long teamId;           // Foreign key to teams table
    private String teamName;        // Team name (denormalized for convenience)
    
    public TestCase() {
        this.customFields = new HashMap<>();
        this.tags = new String[0];
        this.requirements = new String[0];
        this.status = "Active";
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
    
    public TestCase(String externalId, String title, String steps) {
        this();
        this.externalId = externalId;
        this.title = title;
        this.steps = steps;
    }
    
    // Validation - external ID is required for new imports
    public boolean isValid() {
        return externalId != null && !externalId.trim().isEmpty() &&
               title != null && !title.trim().isEmpty() &&
               steps != null && !steps.trim().isEmpty();
    }
    
    // Getters and Setters
    public Long getInternalId() {
        return internalId;
    }
    
    public void setInternalId(Long internalId) {
        this.internalId = internalId;
    }
    
    public String getExternalId() {
        return externalId;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    // Legacy getter for backward compatibility (returns external ID)
    @Deprecated
    public String getId() {
        return externalId;
    }
    
    // Legacy setter for backward compatibility (sets external ID)
    @Deprecated
    public void setId(String id) {
        this.externalId = id;
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
    
    public Long getTeamId() {
        return teamId;
    }
    
    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
    
    public String getTeamName() {
        return teamName;
    }
    
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    @Override
    public String toString() {
        return "TestCase{" +
                "internalId=" + internalId +
                ", externalId='" + externalId + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

