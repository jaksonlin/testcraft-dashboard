# Scheduled Scanning Service

## Overview

The TestCraft Dashboard includes a robust scheduled scanning service that automatically monitors Git repositories and updates test analytics data. This service runs daily scans to ensure the dashboard data remains current and accurate.

## Features

### 🕒 **Automated Scheduling**
- **Daily Scans**: Automatically runs at 2 AM (configurable)
- **Cron Expression**: `0 0 2 * * ?` (configurable via `testcraft.scheduler.cron.daily-scan`)
- **Concurrent Protection**: Prevents multiple scans from running simultaneously

### 🔧 **Manual Operations**
- **On-Demand Scanning**: Trigger scans manually via REST API
- **Status Monitoring**: Real-time scan status and progress tracking
- **Error Handling**: Comprehensive error logging and recovery

### 📊 **Monitoring & Analytics**
- **Scan History**: Track all scan sessions with detailed metrics
- **Performance Metrics**: Scan duration, repository counts, success rates
- **Health Checks**: Service health monitoring and database connectivity

## Configuration

### Application Properties (`application.yml`)

```yaml
testcraft:
  # Scheduling configuration
  scheduler:
    enabled: true
    cron:
      daily-scan: "0 0 2 * * ?"  # Run daily at 2 AM
  
  # Repository scanning configuration
  scanning:
    temp-clone-mode: false
    max-repositories-per-scan: 100

  # Scanner paths
  scanner:
    repository-hub-path: "./repositories"
    repository-list-file: "./sample-repositories.txt"
    temp-clone-mode: false
```

### Environment Variables

- `TESTCRAFT_SCHEDULER_ENABLED`: Enable/disable scheduling (default: true)
- `TESTCRAFT_DAILY_SCAN_CRON`: Custom cron expression for daily scans
- `TESTCRAFT_REPOSITORY_HUB_PATH`: Path to repository hub directory
- `TESTCRAFT_REPOSITORY_LIST_FILE`: Path to repository list file

## API Endpoints

### 📡 **Scan Management**

#### Trigger Manual Scan
```http
POST /api/scan/trigger
```

**Response:**
```json
{
  "success": true,
  "message": "Scan completed successfully",
  "timestamp": 1759627371203
}
```

**Status Codes:**
- `200 OK`: Scan completed successfully
- `409 Conflict`: Scan already in progress
- `500 Internal Server Error`: Scan failed

#### Get Scan Status
```http
GET /api/scan/status
```

**Response:**
```json
{
  "isScanning": false,
  "lastScanTime": "2025-10-05T09:13:04",
  "lastScanStatus": "Success",
  "lastScanError": null,
  "repositoryHubPath": "./repositories",
  "repositoryListFile": "./sample-repositories.txt",
  "tempCloneMode": false,
  "timestamp": 1759627371203
}
```

#### Get Scan Configuration
```http
GET /api/scan/config
```

**Response:**
```json
{
  "tempCloneMode": false,
  "repositoryHubPath": "./repositories",
  "repositoryListFile": "./sample-repositories.txt",
  "timestamp": 1759627371203
}
```

#### Get Recent Scan Sessions
```http
GET /api/scan/sessions?limit=10
```

**Response:**
```json
[
  {
    "id": 4,
    "scanDate": "2025-10-03T01:04:21.475Z",
    "scanDirectory": "e:\\testlab",
    "totalRepositories": 2,
    "totalTestClasses": 11,
    "totalTestMethods": 79,
    "totalAnnotatedMethods": 10,
    "scanDurationMs": 1710,
    "scanStatus": "COMPLETED",
    "errorLog": null,
    "metadata": null
  }
]
```

#### Health Check
```http
GET /api/scan/health
```

**Response:**
```json
{
  "status": "healthy",
  "service": "scan-service",
  "databaseAvailable": true,
  "timestamp": 1759627391039
}
```

## Architecture

### 🏗️ **Service Components**

#### ScheduledScanService
- **Purpose**: Core scanning logic and scheduling
- **Features**: Thread-safe state management, error handling, status tracking
- **Thread Safety**: Uses `AtomicBoolean` and `AtomicReference` for concurrent access

#### ScanController
- **Purpose**: REST API endpoints for scan management
- **Features**: Manual scan triggering, status monitoring, configuration access

#### RepositoryHubScanner
- **Purpose**: Actual repository scanning implementation
- **Integration**: Uses existing `GitRepositoryManager` and scanning logic

### 🔄 **Scan Process Flow**

1. **Trigger**: Scheduled (cron) or manual (API)
2. **Validation**: Check for concurrent scans
3. **Initialization**: Setup database connections and Git manager
4. **Execution**: Run full repository scan
5. **Recording**: Log scan session to database
6. **Status Update**: Update scan status and metrics

### 🛡️ **Error Handling & Recovery**

- **Concurrent Protection**: Prevents multiple scans from running
- **Exception Handling**: Comprehensive try-catch with logging
- **State Management**: Thread-safe status tracking
- **Database Recovery**: Graceful handling of database connection issues

## Monitoring & Observability

### 📈 **Metrics Tracked**

- **Scan Frequency**: How often scans run
- **Success Rate**: Percentage of successful scans
- **Duration**: Average scan execution time
- **Repository Coverage**: Number of repositories scanned
- **Test Discovery**: Test classes and methods found

### 🔍 **Logging**

- **INFO**: Scan start/completion, configuration details
- **WARN**: Concurrent scan attempts, configuration issues
- **ERROR**: Scan failures, database errors, exceptions

### 📊 **Database Integration**

- **Scan Sessions**: Complete scan history in `scan_sessions` table
- **Real-time Status**: In-memory status tracking with persistence
- **Metrics Storage**: Scan results stored in database tables

## Usage Examples

### 🔧 **Development Setup**

1. **Configure Repository Path**:
   ```yaml
   testcraft:
     scanner:
       repository-hub-path: "/path/to/your/repositories"
       repository-list-file: "/path/to/repositories.txt"
   ```

2. **Test Manual Scan**:
   ```bash
   curl -X POST http://localhost:8090/api/scan/trigger
   ```

3. **Monitor Scan Status**:
   ```bash
   curl http://localhost:8090/api/scan/status
   ```

### 🚀 **Production Deployment**

1. **Enable Scheduling**:
   ```yaml
   testcraft:
     scheduler:
       enabled: true
       cron:
         daily-scan: "0 0 2 * * ?"  # 2 AM daily
   ```

2. **Configure Monitoring**:
   - Set up health check monitoring on `/api/scan/health`
   - Monitor scan success rates via `/api/scan/sessions`
   - Set up alerts for scan failures

3. **Performance Tuning**:
   ```yaml
   testcraft:
     scanning:
       max-repositories-per-scan: 50  # Limit for large environments
       temp-clone-mode: false         # Use persistent clones
   ```

## Troubleshooting

### ❌ **Common Issues**

#### Scan Not Running
- **Check**: `testcraft.scheduler.enabled: true`
- **Verify**: Cron expression syntax
- **Monitor**: Application logs for scheduling errors

#### Concurrent Scan Conflicts
- **Cause**: Manual scan triggered while scheduled scan is running
- **Solution**: Wait for current scan to complete, or check status first

#### Database Connection Issues
- **Check**: Database connectivity via `/api/scan/health`
- **Verify**: Database credentials and connection pool settings
- **Monitor**: Connection pool exhaustion

#### Repository Access Issues
- **Verify**: SSH keys configured for Git access
- **Check**: Repository paths and permissions
- **Monitor**: Git clone errors in logs

### 🔧 **Debug Commands**

```bash
# Check scan service health
curl http://localhost:8090/api/scan/health

# Get current scan status
curl http://localhost:8090/api/scan/status

# View recent scan sessions
curl http://localhost:8090/api/scan/sessions?limit=5

# Trigger manual scan (for testing)
curl -X POST http://localhost:8090/api/scan/trigger
```

## Integration with Dashboard

The scheduled scanning service seamlessly integrates with the TestCraft Dashboard:

- **Real-time Updates**: Dashboard shows current scan status
- **Historical Data**: Scan history displayed in dashboard charts
- **Manual Controls**: Dashboard provides manual scan triggers
- **Health Monitoring**: Dashboard health checks include scan service status

This ensures the dashboard always displays the most current test analytics data with minimal manual intervention! 🚀
