package com.example.annotationextractor.service;

import org.springframework.stereotype.Service;
import com.example.annotationextractor.service.RepositoryDataService;
import com.example.annotationextractor.web.dto.TestMethodDetailDto;
import com.example.annotationextractor.web.dto.McpSearchRequest;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ModelPreferences;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.SamplingMessage;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springaicommunity.mcp.annotation.McpProgressToken;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import java.util.List;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
@Service
public class McpService {
    private final RepositoryDataService repositoryDataService;

    public McpService(RepositoryDataService repositoryDataService) {
        this.repositoryDataService = repositoryDataService;
    }

    @McpTool(name = "searchCodePattern", description = "Search for code patterns in test methods. Can filter by repository name. Returns a list of matching test methods with their details.")
    public List<TestMethodDetailDto> searchCodePattern(
        McpSyncRequestContext context,
        @McpToolParam(description = "code pattern to search for") String codePattern) {
        context.info("Processing data: " + codePattern);
        context.progress(p -> p.progress(0.5).total(1.0).message("Processing..."));
        List<TestMethodDetailDto> result = repositoryDataService.searchCodePattern(new McpSearchRequest(codePattern, null, 10));
        context.progress(p -> p.progress(1.0).total(1.0).message("Processing complete."));
        return result;
    }

    @McpTool(name = "listRepositories", description = "List all available repositories in the dashboard.")
    public List<String> listRepositories(McpSyncRequestContext context) {
        context.info("Processing data: listRepositories");
        context.progress(p -> p.progress(0.5).total(1.0).message("Processing..."));
        List<String> result = repositoryDataService.listRepositoriesForMcp();
        context.progress(p -> p.progress(1.0).total(1.0).message("Processing complete."));
        return result;
    }

    @McpTool(name = "getTestMethodSource", description = "Get the source code for a specific test method by its ID.")
    public String getTestMethodSource(
        McpSyncRequestContext context,
        @McpToolParam(description = "test method id") Long testMethodId) {
        context.info("Processing data: " + testMethodId);
        context.progress(p -> p.progress(0.5).total(1.0).message("Processing..."));
        String result = repositoryDataService.getTestMethodSourceForMcp(testMethodId);
        context.progress(p -> p.progress(1.0).total(1.0).message("Processing complete."));
        return result;
    }
}