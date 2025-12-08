# MCP Client Test

This directory contains test clients for the MCP (Model Context Protocol) server.

## Prerequisites

1. **Build the project:**
   ```bash
   mvn clean test-compile
   ```

2. **Start the Spring Boot application** on port 8090:
   ```bash
   mvn spring-boot:run
   ```
   Or run the main application class `TestCraftDashboardApplication`

## Running ClientSse

The `ClientSse` class tests the MCP server using SSE (Server-Sent Events) transport.

### Option 1: Using Maven Exec Plugin
```bash
mvn exec:java -Dexec.mainClass="com.example.annotationextractor.mcp.ClientSse" -Dexec.classpathScope=test -Dexec.includeProjectDependencies=true
```

### Option 2: Using Java directly
```bash
# Compile test classes
mvn test-compile

# Run with Java (adjust classpath as needed)
java -cp "target/test-classes;target/classes;%USERPROFILE%\.m2\repository\*" com.example.annotationextractor.mcp.ClientSse
```

### Option 3: Run from IDE
- Right-click on `ClientSse.java` → Run As → Java Application

## Endpoint Configuration

The client connects to: `http://localhost:8090/api/mcp/message`

**Configuration:**
- MCP endpoints are configured under `/api/mcp` in `application.yml`
- The SSE message endpoint is set to: `sse-message-endpoint: /api/mcp/message`
- The client is configured to use `/api/mcp/message` as the endpoint path

**Note:** If you get a 404 error:
- Ensure the Spring Boot application is running
- Check the actual MCP endpoint in application logs
- Verify the endpoint path in `application.yml` matches: `sse-message-endpoint: /api/mcp/message`

## Troubleshooting

1. **404 Not Found**: 
   - Ensure the Spring Boot application is running
   - Check the actual MCP endpoint in application logs
   - Verify the endpoint path matches the configuration

2. **Connection Refused**:
   - Ensure the server is running on port 8090
   - Check firewall settings

3. **ClassNotFoundException**:
   - Run `mvn test-compile` to compile test classes
   - Ensure all dependencies are resolved: `mvn dependency:resolve`

