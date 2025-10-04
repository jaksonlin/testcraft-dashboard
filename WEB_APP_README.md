# TestCraft Dashboard - Web Application

A Spring Boot web application for continuous monitoring of Git repositories and unit test activity trends.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Node.js 18+ (for React frontend)

### Backend (Spring Boot)

1. **Start the application:**
   ```bash
   # Windows
   run-dashboard.bat
   
   # Linux/Mac
   ./run-dashboard.sh
   
   # Or manually
   mvn spring-boot:run
   ```

2. **Verify the application is running:**
   - Health check: http://localhost:8080/api/health
   - Dashboard API: http://localhost:8080/api/dashboard/overview

### Frontend (React - Coming Next)

The React frontend will be created in the next phase and will connect to the Spring Boot API.

## 📊 API Endpoints

### Dashboard
- `GET /api/dashboard/overview` - Main dashboard overview
- `GET /api/dashboard/teams` - Team metrics
- `GET /api/dashboard/teams/{id}/repositories` - Repository metrics by team
- `GET /api/dashboard/trends/coverage?days=30` - Coverage trends
- `GET /api/dashboard/scan-sessions/recent?limit=10` - Recent scan sessions

### Scanning
- `POST /api/scan/trigger` - Trigger manual scan
- `GET /api/scan/config` - Get scan configuration
- `GET /api/scan/health` - Scan service health

### Health
- `GET /api/health` - Application health
- `GET /api/health/ready` - Readiness check

## ⚙️ Configuration

The application is configured via `src/main/resources/application.yml`:

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/test_analytics
    username: test_user
    password: 123456

testcraft:
  scheduler:
    cron:
      daily-scan: "0 0 2 * * ?"  # Daily at 2 AM
  scanning:
    temp-clone-mode: false
```

## 🔄 Scheduled Scanning

The application includes a scheduled service that runs daily scans:

- **Schedule**: Daily at 2 AM (configurable)
- **Purpose**: Keep dashboard data up to date
- **Manual Trigger**: Available via REST API

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React Frontend │    │  Spring Boot API │    │  PostgreSQL DB  │
│   (Coming Next)  │◄──►│  (REST Endpoints)│◄──►│  (Existing Schema)│
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │ Scheduled Scanner │
                       │ (Daily Monitoring) │
                       └─────────────────┘
```

## 📁 Project Structure

```
src/main/java/com/example/annotationextractor/
├── TestCraftDashboardApplication.java    # Main Spring Boot app
├── config/
│   └── WebConfig.java                    # CORS and web configuration
├── service/
│   └── ScheduledScanService.java         # Daily scanning service
└── web/
    ├── controller/
    │   ├── DashboardController.java      # Dashboard API endpoints
    │   ├── ScanController.java           # Scan operations
    │   └── HealthController.java         # Health checks
    └── dto/                              # Data Transfer Objects
        ├── DashboardOverviewDto.java
        ├── TeamMetricsDto.java
        ├── RepositoryMetricsDto.java
        └── ...
```

## 🔧 Development

### Adding New Endpoints

1. Create DTO classes in `web/dto/`
2. Add controller methods in `web/controller/`
3. Implement business logic in `service/` classes
4. Update this README with new endpoints

### Database Integration

The application integrates with your existing PostgreSQL schema and uses the `PersistenceReadFacade` for data access.

## 🚀 Next Steps

1. ✅ **Spring Boot Backend** - Complete
2. 🔄 **React Frontend** - Next phase
3. 📊 **Dashboard Implementation** - Connect to existing data
4. 🔔 **Notifications** - Email/Slack alerts
5. 📈 **Advanced Analytics** - ML insights

## 🐛 Troubleshooting

### Common Issues

1. **Port 8080 already in use:**
   - Change port in `application.yml`
   - Kill existing process: `netstat -ano | findstr :8080`

2. **Database connection failed:**
   - Check PostgreSQL is running
   - Verify credentials in `application.yml`
   - Run database initialization scripts

3. **Build failures:**
   - Check Java version: `java -version`
   - Clean Maven cache: `mvn clean`

### Logs

Application logs are available in the console output. For production, configure logging in `application.yml`.
