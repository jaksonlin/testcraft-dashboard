# TestCraft Dashboard - Docker Setup Guide

This guide explains how to run the TestCraft Dashboard using Docker Compose with a complete setup including backend, frontend, PostgreSQL database, and nginx reverse proxy.

## 📋 Prerequisites

- Docker (20.10+)
- Docker Compose (2.0+)
- At least 2GB of free disk space
- Ports 80 and 5432 available on your host machine

## 🏗️ Architecture

The Docker setup includes:

```
┌─────────────────────────────────────────────────────────┐
│                     Nginx (Port 80)                     │
│                   Reverse Proxy                         │
└────────────┬─────────────────────────┬─────────────────┘
             │                         │
             │ /api                    │ /
             ↓                         ↓
    ┌────────────────┐        ┌────────────────┐
    │     Backend    │        │    Frontend    │
    │  (Spring Boot) │        │  (React+Nginx) │
    │   Port: 8090   │        │   Port: 80     │
    └────────┬───────┘        └────────────────┘
             │
             │ JDBC
             ↓
    ┌────────────────┐
    │   PostgreSQL   │
    │   Port: 5432   │
    └────────────────┘
```

## 🚀 Quick Start

### 1. Create Environment File

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` to customize your configuration (optional):

```bash
# Database Configuration
POSTGRES_DB=test_analytics_v2
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change_this_password

# Nginx Port
NGINX_PORT=80

# Spring Profile (prod/dev)
SPRING_PROFILES=prod

# Java Memory Settings
JAVA_OPTS=-Xmx512m -Xms256m
```

### 2. Start All Services

```bash
docker-compose up -d
```

This will:
- Build the backend Spring Boot application
- Build the frontend React application
- Start PostgreSQL database
- Start nginx reverse proxy
- Set up all networking and health checks

### 3. Access the Application

Open your browser and navigate to:
- **Main Application**: http://localhost
- **API Endpoints**: http://localhost/api/*
- **Health Check**: http://localhost/health
- **Backend Health**: http://localhost/api/actuator/health

### 4. Monitor Logs

View logs for all services:
```bash
docker-compose logs -f
```

View logs for a specific service:
```bash
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
docker-compose logs -f nginx
```

## 📦 Container Details

### Backend Container
- **Image**: Custom build from `Dockerfile.backend`
- **Base**: OpenJDK 17 Alpine
- **Port**: 8090 (internal)
- **Health Check**: `/api/actuator/health`
- **Database**: Connects to PostgreSQL via `jdbc:postgresql://postgres:5432/test_analytics_v2`

### Frontend Container
- **Image**: Custom build from `Dockerfile.frontend`
- **Base**: Node 22 Alpine (build) + Nginx Alpine (runtime)
- **Port**: 80 (internal)
- **Serves**: Built React application

### PostgreSQL Container
- **Image**: postgres:16-alpine
- **Port**: 5432 (exposed)
- **Volume**: `postgres_data` for data persistence
- **Health Check**: `pg_isready`

### Nginx Container
- **Image**: nginx:alpine
- **Port**: 80 (exposed)
- **Configuration**: Custom nginx.conf for reverse proxy
- **Routes**:
  - `/` → Frontend
  - `/api/` → Backend

## 🛠️ Common Commands

### Start Services
```bash
# Start in detached mode
docker-compose up -d

# Start with rebuild
docker-compose up -d --build

# Start specific service
docker-compose up -d backend
```

### Stop Services
```bash
# Stop all services
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop and remove containers + volumes
docker-compose down -v
```

### Rebuild Services
```bash
# Rebuild all services
docker-compose build

# Rebuild specific service
docker-compose build backend

# Rebuild without cache
docker-compose build --no-cache
```

### View Status
```bash
# List running containers
docker-compose ps

# View resource usage
docker stats
```

### Access Container Shell
```bash
# Backend
docker-compose exec backend sh

# Frontend
docker-compose exec frontend sh

# PostgreSQL
docker-compose exec postgres psql -U postgres -d test_analytics_v2

# Nginx
docker-compose exec nginx sh
```

### Database Operations
```bash
# Access PostgreSQL CLI
docker-compose exec postgres psql -U postgres -d test_analytics_v2

# Backup database
docker-compose exec postgres pg_dump -U postgres test_analytics_v2 > backup.sql

# Restore database
docker-compose exec -T postgres psql -U postgres -d test_analytics_v2 < backup.sql
```

## 🔧 Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_DB` | test_analytics_v2 | PostgreSQL database name |
| `POSTGRES_USER` | postgres | PostgreSQL username |
| `POSTGRES_PASSWORD` | postgres | PostgreSQL password |
| `NGINX_PORT` | 80 | Nginx exposed port |
| `SPRING_PROFILES` | prod | Spring Boot active profile |
| `JAVA_OPTS` | -Xmx512m -Xms256m | Java memory settings |

### Nginx Configuration

The nginx configuration (`nginx/nginx.conf`) provides:
- Reverse proxy to backend and frontend
- Gzip compression
- Security headers
- Connection pooling
- Health check endpoint at `/health`

### Backend Configuration

Backend uses `src/main/resources/application.yml` but overrides database settings via environment variables:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## 🐛 Troubleshooting

### Database Connection Issues

**Problem**: Backend can't connect to PostgreSQL

**Solution**:
```bash
# Check if PostgreSQL is healthy
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres

# Verify database exists
docker-compose exec postgres psql -U postgres -c "\l"
```

### Port Already in Use

**Problem**: Port 80 or 5432 is already in use

**Solution**:
```bash
# Option 1: Change port in .env
echo "NGINX_PORT=8080" >> .env

# Option 2: Stop conflicting service
sudo systemctl stop nginx  # Example for system nginx
```

### Backend Not Starting

**Problem**: Backend container exits immediately

**Solution**:
```bash
# Check backend logs
docker-compose logs backend

# Rebuild backend
docker-compose build --no-cache backend

# Verify Java version in container
docker-compose run backend java -version
```

### Frontend Not Building

**Problem**: Frontend build fails

**Solution**:
```bash
# Check if node_modules exists locally (should be clean)
rm -rf frontend/node_modules frontend/dist

# Rebuild frontend
docker-compose build --no-cache frontend

# Check build logs
docker-compose logs frontend
```

### Database Migration Issues

**Problem**: Flyway migration fails

**Solution**:
```bash
# Connect to database and check migration status
docker-compose exec postgres psql -U postgres -d test_analytics_v2

# In psql:
SELECT * FROM flyway_schema_history;

# If needed, clean and restart (WARNING: drops all data)
docker-compose down -v
docker-compose up -d
```

### Memory Issues

**Problem**: Backend running out of memory

**Solution**:
```bash
# Increase Java heap size in .env
JAVA_OPTS=-Xmx1024m -Xms512m

# Restart backend
docker-compose restart backend
```

## 📊 Monitoring

### Health Checks

All services have health checks configured:

```bash
# Check service health
docker-compose ps

# Test nginx health endpoint
curl http://localhost/health

# Test backend health
curl http://localhost/api/actuator/health
```

### Logs

```bash
# Real-time logs for all services
docker-compose logs -f

# Last 100 lines
docker-compose logs --tail=100

# Specific service logs
docker-compose logs -f backend
```

### Resource Usage

```bash
# Monitor container resources
docker stats

# Detailed container info
docker-compose exec backend top
```

## 🔒 Security Considerations

### Production Deployment

For production deployments, consider:

1. **Change default passwords**:
   ```bash
   POSTGRES_PASSWORD=strong_random_password
   ```

2. **Use secrets management**:
   - Docker secrets
   - Environment variable injection
   - External secret stores (Vault, AWS Secrets Manager)

3. **Enable HTTPS**:
   - Configure SSL certificates in nginx
   - Use Let's Encrypt for certificates
   - Redirect HTTP to HTTPS

4. **Network isolation**:
   - Don't expose PostgreSQL port externally
   - Use internal Docker networks only

5. **Resource limits**:
   ```yaml
   deploy:
     resources:
       limits:
         cpus: '1.0'
         memory: 1G
   ```

6. **Update base images regularly**:
   ```bash
   docker-compose pull
   docker-compose up -d
   ```

## 🔄 Updates and Maintenance

### Updating the Application

```bash
# Pull latest code
git pull

# Rebuild and restart
docker-compose down
docker-compose up -d --build
```

### Database Backup

```bash
# Create backup
docker-compose exec postgres pg_dump -U postgres test_analytics_v2 > backup_$(date +%Y%m%d).sql

# Restore from backup
docker-compose exec -T postgres psql -U postgres -d test_analytics_v2 < backup_20251020.sql
```

### Cleaning Up

```bash
# Remove stopped containers
docker-compose down

# Remove volumes (WARNING: deletes all data)
docker-compose down -v

# Remove unused images
docker image prune -a

# Full cleanup
docker system prune -a --volumes
```

## 📝 Development Mode

To expose backend and frontend ports directly for development:

1. Uncomment ports in `docker-compose.yml`:
   ```yaml
   backend:
     ports:
       - "8090:8090"
   
   frontend:
     ports:
       - "5173:80"
   ```

2. Restart services:
   ```bash
   docker-compose up -d
   ```

3. Access directly:
   - Backend: http://localhost:8090/api
   - Frontend: http://localhost:5173

## 🆘 Getting Help

If you encounter issues:

1. Check this documentation
2. Review logs: `docker-compose logs -f`
3. Check container status: `docker-compose ps`
4. Verify health checks: `curl http://localhost/health`
5. Review GitHub issues

## 📄 File Structure

```
testcraft-dashboard/
├── docker-compose.yml          # Main orchestration file
├── Dockerfile.backend          # Backend container definition
├── Dockerfile.frontend         # Frontend container definition
├── .env                        # Environment variables (create from .env.example)
├── .env.example               # Example environment file
├── .dockerignore              # Files to exclude from Docker build
├── init-db.sql                # Database initialization script
├── nginx/
│   ├── nginx.conf             # Main nginx configuration
│   └── frontend.conf          # Frontend nginx configuration
├── src/                       # Backend source code
├── frontend/                  # Frontend source code
└── DOCKER_SETUP.md           # This file
```

---

**Happy Containerizing! 🐳**

