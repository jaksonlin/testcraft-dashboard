# Database Configuration for Docker Deployment

## Overview

The TestCraft Dashboard uses PostgreSQL for data storage, with Flyway managing database migrations. This document explains how the database configuration works in Docker containers.

## Configuration Flow

```
Environment Variables (.env)
         ↓
Docker Compose (docker-compose.yml)
         ↓
Spring Boot Container
         ↓
application.yml (reads env vars)
         ↓
Flyway Migrations (automatic)
         ↓
Application Ready
```

## Environment Variables

### Required Variables

These are set in docker-compose.yml and can be overridden in `.env`:

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_DB` | test_analytics_v2 | PostgreSQL database name |
| `POSTGRES_USER` | postgres | PostgreSQL username |
| `POSTGRES_PASSWORD` | postgres | PostgreSQL password |

### Optional Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES` | prod | Spring Boot profile |
| `FLYWAY_ENABLED` | true | Enable Flyway migrations |
| `DB_POOL_MAX_SIZE` | 5 | HikariCP max pool size |
| `DB_POOL_MIN_IDLE` | 2 | HikariCP min idle connections |
| `DB_POOL_IDLE_TIMEOUT` | 300000 | Idle timeout (ms) |
| `DB_POOL_MAX_LIFETIME` | 1800000 | Max connection lifetime (ms) |
| `DB_POOL_CONNECTION_TIMEOUT` | 30000 | Connection timeout (ms) |
| `JAVA_OPTS` | -Xmx512m -Xms256m | JVM memory settings |

## Application Configuration

### application.yml

The `application.yml` uses Spring Boot's property placeholder syntax with defaults:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/test_analytics_v2}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}
```

**Format**: `${ENV_VAR:default_value}`
- Uses environment variable if set
- Falls back to default value if not set
- Works for both Docker and local development

### Flyway Configuration

```yaml
spring:
  flyway:
    enabled: ${FLYWAY_ENABLED:true}
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
    validate-on-migrate: true
```

**Features**:
- Automatically runs migrations on startup
- Creates baseline if database is empty
- Validates migration scripts
- Migration scripts in `src/main/resources/db/migration/`

## Docker Compose Configuration

### PostgreSQL Service

```yaml
postgres:
  image: postgres:16-alpine
  environment:
    POSTGRES_DB: ${POSTGRES_DB:-test_analytics_v2}
    POSTGRES_USER: ${POSTGRES_USER:-postgres}
    POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-postgres}
  volumes:
    - postgres_data:/var/lib/postgresql/data  # Persistent storage
    - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql:ro
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U postgres"]
    interval: 10s
    timeout: 5s
    retries: 5
```

### Backend Service

```yaml
backend:
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
    SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
    SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
    FLYWAY_ENABLED: ${FLYWAY_ENABLED:-true}
  depends_on:
    postgres:
      condition: service_healthy  # Waits for PostgreSQL to be ready
```

## Startup Sequence

### 1. PostgreSQL Container Starts
- PostgreSQL service starts
- Runs `init-db.sql` (one-time initialization)
- Health check runs every 10 seconds
- Marked healthy when `pg_isready` succeeds

### 2. Backend Waits for Database
Built-in wait script in Dockerfile:
```bash
#!/bin/bash
# Extracts host and port from JDBC URL
# Tries to connect with netcat (nc -z)
# Waits up to 60 seconds (30 tries x 2 seconds)
# Starts Spring Boot when database is ready
```

### 3. Spring Boot Starts
- Reads environment variables
- Configures HikariCP connection pool
- Connects to PostgreSQL

### 4. Flyway Migrations Run
- Checks `flyway_schema_history` table
- Runs pending migrations in order
- Creates tables if needed
- Application becomes ready

### 5. Health Check Passes
- Spring Boot Actuator health endpoint available
- Nginx can route traffic to backend
- Application fully operational

## Database Initialization

### First Time Startup

```bash
# Start containers
docker-compose up -d

# Check logs
docker-compose logs -f backend

# Expected output:
# ✓ Waiting for database...
# ✓ Database is ready!
# ✓ Starting Spring Boot...
# ✓ Flyway: Creating schema history table...
# ✓ Flyway: Migrating schema to version 1...
# ✓ Application started successfully
```

### Flyway Migration Files

Located in `src/main/resources/db/migration/`:

```
V1__initial_schema.sql       # Creates base tables
V2__add_test_cases.sql       # Adds test case tables
V3__add_organizations.sql    # Adds organization support
...
```

**Naming Convention**: `V{version}__{description}.sql`
- Version must be unique and sequential
- Double underscore separates version from description
- Run in alphanumeric order

## Local Development

### Using Docker Database Only

```bash
# Start only PostgreSQL
docker-compose up -d postgres

# Run backend locally
mvn spring-boot:run

# Application connects to Docker PostgreSQL
```

### Override Environment Variables

```bash
# Set custom database
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/my_test_db
export SPRING_DATASOURCE_USERNAME=myuser
export SPRING_DATASOURCE_PASSWORD=mypass

# Run application
mvn spring-boot:run
```

## Troubleshooting

### Backend Can't Connect to Database

**Check PostgreSQL is healthy**:
```bash
docker-compose ps postgres
# Should show "Up (healthy)"
```

**Check connection details**:
```bash
docker-compose exec backend env | grep DATASOURCE
```

**Test connection manually**:
```bash
docker-compose exec postgres psql -U postgres -d test_analytics_v2 -c "SELECT 1"
```

### Flyway Migration Failed

**Check Flyway history**:
```bash
docker-compose exec postgres psql -U postgres -d test_analytics_v2
\d flyway_schema_history
SELECT * FROM flyway_schema_history;
```

**Reset Flyway (⚠️ deletes data)**:
```bash
docker-compose down -v  # Removes volumes
docker-compose up -d    # Fresh start
```

**Fix failed migration**:
```bash
# Option 1: Repair Flyway
docker-compose exec backend java -jar app.jar --spring.flyway.command=repair

# Option 2: Manual fix
docker-compose exec postgres psql -U postgres -d test_analytics_v2
DELETE FROM flyway_schema_history WHERE success = false;
```

### Connection Pool Issues

**Increase pool size**:
```bash
# In .env file
DB_POOL_MAX_SIZE=10
DB_POOL_MIN_IDLE=5
```

**Check pool status**:
```bash
curl http://localhost/api/actuator/metrics/hikaricp.connections.active
```

### Database Performance

**Enable SQL logging**:
```yaml
# In application.yml
logging:
  level:
    org.hibernate.SQL: DEBUG
    com.zaxxer.hikari: DEBUG
```

**Check slow queries**:
```sql
-- In PostgreSQL
SELECT * FROM pg_stat_statements 
WHERE mean_time > 1000 
ORDER BY mean_time DESC;
```

## Data Persistence

### Volume Management

```bash
# View volumes
docker volume ls | grep testcraft

# Backup data
docker-compose exec postgres pg_dump -U postgres test_analytics_v2 > backup.sql

# Restore data
cat backup.sql | docker-compose exec -T postgres psql -U postgres -d test_analytics_v2

# Remove volume (⚠️ deletes all data)
docker-compose down -v
```

### Database Migrations in Production

**Best Practices**:
1. Test migrations locally first
2. Backup database before deploying
3. Review migration scripts carefully
4. Use `baseline-on-migrate` for existing databases
5. Monitor logs during deployment

**Deployment Steps**:
```bash
# 1. Backup
docker-compose exec postgres pg_dump -U postgres test_analytics_v2 > backup_$(date +%Y%m%d).sql

# 2. Deploy new version
docker-compose pull
docker-compose up -d --build

# 3. Check migrations
docker-compose logs backend | grep Flyway

# 4. Verify application
curl http://localhost/api/actuator/health
```

## Security Considerations

### Production Checklist

- [ ] Change default PostgreSQL password
- [ ] Use strong passwords (minimum 16 characters)
- [ ] Don't expose PostgreSQL port externally
- [ ] Enable SSL/TLS for PostgreSQL connections
- [ ] Use Docker secrets instead of environment variables
- [ ] Regularly backup database
- [ ] Monitor connection pool metrics
- [ ] Set up database access logs

### Using Docker Secrets

```yaml
# docker-compose.yml
services:
  backend:
    secrets:
      - db_password
    environment:
      SPRING_DATASOURCE_PASSWORD_FILE: /run/secrets/db_password

secrets:
  db_password:
    file: ./secrets/db_password.txt
```

## Monitoring

### Health Checks

```bash
# Database health
docker-compose exec postgres pg_isready

# Application health
curl http://localhost/api/actuator/health

# Detailed health
curl http://localhost/api/actuator/health | jq
```

### Metrics

```bash
# Database connections
curl http://localhost/api/actuator/metrics/hikaricp.connections

# Flyway info
curl http://localhost/api/actuator/flyway
```

## Summary

✅ **Environment-based configuration** - Easy to customize per environment  
✅ **Automatic database wait** - Backend waits for PostgreSQL to be ready  
✅ **Automatic migrations** - Flyway runs on startup  
✅ **Health checks** - Ensures services are ready before routing traffic  
✅ **Data persistence** - PostgreSQL data survives container restarts  
✅ **Connection pooling** - Efficient database connections with HikariCP  

---

**Questions? Check the logs**: `docker-compose logs -f backend postgres`

