# TestCraft Dashboard API Documentation

## Overview

The TestCraft Dashboard API provides comprehensive endpoints for accessing test analytics data in the same format as the Excel reports. The API is designed to support dashboard visualization and data drill-down capabilities.

**Base URL**: `http://localhost:8090/api`

## API Endpoints

### 1. Dashboard Overview

#### GET `/dashboard/overview`
Get comprehensive overview metrics for the main dashboard.

**Response**: `DashboardOverviewDto`
```json
{
  "totalRepositories": 15,
  "totalTeams": 3,
  "totalTestClasses": 127,
  "totalTestMethods": 1045,
  "totalAnnotatedMethods": 312,
  "overallCoverageRate": 29.86,
  "lastScanDate": "2025-10-04T10:00:00",
  "topTeams": [
    {
      "teamId": 1,
      "teamName": "Backend Team",
      "teamCode": "BE",
      "repositoryCount": 8,
      "averageCoverageRate": 35.2
    }
  ],
  "topRepositories": [
    {
      "repositoryId": 1,
      "repositoryName": "user-service",
      "gitUrl": "https://github.com/company/user-service",
      "teamName": "Backend Team",
      "coverageRate": 45.5
    }
  ]
}
```

### 2. Team Metrics

#### GET `/dashboard/teams`
Get team-based performance metrics.

**Response**: `List<TeamMetricsDto>`
```json
[
  {
    "teamId": 1,
    "teamName": "Backend Team",
    "teamCode": "BE",
    "department": "Engineering",
    "repositoryCount": 8,
    "averageCoverageRate": 35.2,
    "totalTestClasses": 67,
    "totalTestMethods": 445,
    "totalAnnotatedMethods": 156,
    "repositories": [
      {
        "repositoryId": 1,
        "repositoryName": "user-service",
        "gitUrl": "https://github.com/company/user-service",
        "testClassCount": 8,
        "testMethodCount": 67,
        "annotatedMethodCount": 23,
        "coverageRate": 34.33
      }
    ]
  }
]
```

#### GET `/dashboard/teams/{teamId}/repositories`
Get repository metrics for a specific team.

**Parameters**:
- `teamId` (path): Team ID

**Response**: `List<RepositoryMetricsDto>`

### 3. Repository Details (Excel Format)

#### GET `/dashboard/repositories/details`
Get detailed repository information matching Excel Repository Details sheet format.

**Response**: `List<RepositoryDetailDto>`
```json
[
  {
    "id": 1,
    "repository": "user-service",
    "path": "/repos/user-service",
    "gitUrl": "https://github.com/company/user-service",
    "testClasses": 8,
    "testMethods": 67,
    "annotatedMethods": 23,
    "coverageRate": 34.33,
    "lastScan": "2025-10-04T09:30:00",
    "teamName": "Backend Team",
    "teamCode": "BE"
  }
]
```

### 4. Test Method Details (Excel Format)

#### GET `/dashboard/test-methods/details`
Get detailed test method information matching Excel Test Method Details sheet format.

**Parameters**:
- `teamId` (query, optional): Filter by team ID
- `limit` (query, default: 100): Maximum number of results

**Response**: `List<TestMethodDetailDto>`
```json
[
  {
    "id": 1,
    "repository": "user-service",
    "testClass": "UserServiceTest",
    "testMethod": "testCreateUser",
    "line": 45,
    "title": "Test user creation with valid data",
    "author": "John Doe",
    "status": "PASSED",
    "targetClass": "UserService",
    "targetMethod": "createUser",
    "description": "Verify that user creation works with valid input data",
    "testPoints": "UC001, UC002",
    "tags": ["smoke", "user-management"],
    "requirements": ["REQ001", "REQ002"],
    "testCaseIds": ["TC001", "TC002"],
    "defects": [],
    "lastModified": "2025-10-04T08:15:00",
    "lastUpdateAuthor": "Jane Smith",
    "teamName": "Backend Team",
    "teamCode": "BE",
    "gitUrl": "https://github.com/company/user-service"
  }
]
```

### 5. Coverage Trends

#### GET `/dashboard/trends/coverage`
Get coverage trends for the specified period.

**Parameters**:
- `days` (query, default: 30): Number of days to include

**Response**: `List<DailyMetric>`
```json
[
  {
    "metricDate": "2025-10-04",
    "totalRepositories": 15,
    "totalTestClasses": 127,
    "totalTestMethods": 1045,
    "totalAnnotatedMethods": 312,
    "overallCoverageRate": 29.86
  }
]
```

### 6. Scan Sessions

#### GET `/dashboard/scan-sessions/recent`
Get recent scan sessions.

**Parameters**:
- `limit` (query, default: 10): Maximum number of sessions

**Response**: `List<ScanSession>`

### 7. Health Check

#### GET `/dashboard/health`
Get dashboard health status.

**Response**: `String`
```json
"Dashboard is healthy"
```

#### GET `/health`
Get application health status.

**Response**: `HealthStatusDto`
```json
{
  "service": "testcraft-dashboard",
  "version": "1.0.0",
  "status": "UP",
  "timestamp": "2025-10-04T10:04:28.5615597"
}
```

## Data Transfer Objects (DTOs)

### DashboardOverviewDto
Main dashboard overview metrics including totals and top performers.

### TeamMetricsDto
Team performance metrics with repository breakdown.

### RepositoryMetricsDto
Repository-level metrics and statistics.

### RepositoryDetailDto
Detailed repository information matching Excel Repository Details sheet.

### TestMethodDetailDto
Comprehensive test method information matching Excel Test Method Details sheet.

### TeamSummaryDto
Summary information for top-performing teams.

### RepositorySummaryDto
Summary information for top-performing repositories.

## Excel Report Mapping

The API endpoints are designed to match the Excel report structure:

| Excel Sheet | API Endpoint | DTO |
|-------------|--------------|-----|
| Summary Sheet | `/dashboard/overview` | `DashboardOverviewDto` |
| Repository Details | `/dashboard/repositories/details` | `RepositoryDetailDto` |
| Team Summary | `/dashboard/teams` | `TeamMetricsDto` |
| Test Method Details | `/dashboard/test-methods/details` | `TestMethodDetailDto` |

## Error Handling

All endpoints return appropriate HTTP status codes:
- `200 OK`: Successful request
- `400 Bad Request`: Invalid parameters
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

## CORS Configuration

The API is configured to accept requests from `http://localhost:3000` (React development server).

## Database Integration

The API connects to PostgreSQL database with the following configuration:
- **Host**: localhost:5432
- **Database**: test_analytics
- **Connection Pool**: HikariCP with 5 max connections

## Mock Data

When database is not available or contains no data, endpoints return mock data for development and testing purposes.

## Usage Examples

### Get Dashboard Overview
```bash
curl -X GET "http://localhost:8090/api/dashboard/overview"
```

### Get Team Metrics
```bash
curl -X GET "http://localhost:8090/api/dashboard/teams"
```

### Get Repository Details
```bash
curl -X GET "http://localhost:8090/api/dashboard/repositories/details"
```

### Get Test Method Details for Specific Team
```bash
curl -X GET "http://localhost:8090/api/dashboard/test-methods/details?teamId=1&limit=50"
```

### Get Coverage Trends for 7 Days
```bash
curl -X GET "http://localhost:8090/api/dashboard/trends/coverage?days=7"
```

## Next Steps

1. **Populate Database**: Run repository scans to populate the database with real data
2. **Frontend Development**: Use these APIs to build the React dashboard
3. **Real-time Updates**: Implement WebSocket support for live updates
4. **Authentication**: Add security and user authentication
5. **Performance Optimization**: Implement caching and pagination for large datasets
