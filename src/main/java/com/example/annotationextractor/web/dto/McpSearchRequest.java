package com.example.annotationextractor.web.dto;

public record McpSearchRequest(
        String pattern,
        String repositoryName,
        Integer limit) {
}
