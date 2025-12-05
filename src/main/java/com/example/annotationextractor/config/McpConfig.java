package com.example.annotationextractor.config;

import com.example.annotationextractor.web.dto.McpSearchRequest;

import com.example.annotationextractor.service.RepositoryDataService;
import com.example.annotationextractor.web.dto.TestMethodDetailDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

@Configuration
public class McpConfig {

    @Bean
    @Description("Search for code patterns in test methods. Can filter by repository name. Returns a list of matching test methods with their details.")
    public Function<McpSearchRequest, List<TestMethodDetailDto>> searchCodePattern(
            RepositoryDataService repositoryDataService) {
        return repositoryDataService::searchCodePattern;
    }

    @Bean
    @Description("List all available repositories in the dashboard.")
    public Function<Void, List<String>> listRepositories(RepositoryDataService repositoryDataService) {
        return unused -> repositoryDataService.listRepositoriesForMcp();
    }

    @Bean
    @Description("Get the source code for a specific test method by its ID.")
    public Function<Long, String> getTestMethodSource(RepositoryDataService repositoryDataService) {
        return repositoryDataService::getTestMethodSourceForMcp;
    }
}
