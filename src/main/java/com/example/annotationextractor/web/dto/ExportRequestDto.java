package com.example.annotationextractor.web.dto;

import java.util.Map;

/**
 * DTO for export request parameters
 */
public class ExportRequestDto {
    private String dataType; // 'test-methods', 'repositories', 'teams', etc.
    private String format; // 'csv', 'excel', 'json'
    private String scope; // 'all', 'filtered'
    private Map<String, Object> filters; // teamName, repositoryName, annotated, etc.
    private String filename;

    public ExportRequestDto() {
    }

    public ExportRequestDto(String dataType, String format, String scope, Map<String, Object> filters, String filename) {
        this.dataType = dataType;
        this.format = format;
        this.scope = scope;
        this.filters = filters;
        this.filename = filename;
    }

    // Getters and Setters
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
