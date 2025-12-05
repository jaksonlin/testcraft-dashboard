# TestCraft Dashboard - Docker Deployment

## 📖 Overview

This project includes a complete Docker setup with:
- **Backend**: Spring Boot application (Java 17)
- **Frontend**: React + Vite application
- **Database**: PostgreSQL 16
- **Reverse Proxy**: Nginx

## 🎯 Quick Start

### Start Everything
```bash
./docker-start.sh
```

### Access Application
Open http://localhost in your browser

### Stop Everything
```bash
./docker-stop.sh
```

## 📁 Docker Files Structure

```
testcraft-dashboard/
├── docker-compose.yml          # Production configuration
├── docker-compose.dev.yml      # Development configuration
├── Dockerfile.backend          # Backend container
├── Dockerfile.frontend         # Frontend container
├── env.example                 # Environment template
├── .dockerignore              # Docker build exclusions
├── init-db.sql                # Database initialization
├── nginx/
│   ├── nginx.conf             # Nginx reverse proxy config
│   └── frontend.conf          # Frontend nginx config
├── docker-start.sh            # Quick start script
├── docker-stop.sh             # Quick stop script
├── QUICK_START.md             # Quick reference guide
└── DOCKER_SETUP.md            # Detailed documentation
```

## 🔧 Configuration Files

### 1. docker-compose.yml
Main orchestration file for production deployment:
- All services in one network
- Internal ports not exposed (except PostgreSQL for external access)
- Health checks configured
- Restart policies set

### 2. docker-compose.dev.yml
Development version with all ports exposed:
```bash
docker-compose -f docker-compose.dev.yml up -d
```

Access:
- Frontend: http://localhost:5173
- Backend: http://localhost:8090
- Nginx: http://localhost:80
- PostgreSQL: localhost:5432

### 3. Dockerfile.backend
Multi-stage build for Spring Boot:
- Build stage: Maven with all dependencies
- Runtime stage: Slim JRE image
- Health check via Spring Actuator

### 4. Dockerfile.frontend
Multi-stage build for React:
- Build stage: Node 22 with npm build
- Runtime stage: Nginx Alpine serving static files
- Production optimized

### 5. nginx/nginx.conf
Reverse proxy configuration:
- Routes `/api` → Backend (port 8090)
- Routes `/` → Frontend (port 80)
- Health endpoint at `/health`
- Gzip compression enabled
- Security headers added

### 6. env.example
Environment variables template:
```bash
cp env.example .env
# Edit .env with your values
```

## 🚀 Usage Scenarios

### Scenario 1: Production Deployment
```bash
# Copy and configure environment
cp env.example .env
nano .env  # Edit as needed

# Start all services
docker-compose up -d

# Monitor logs
docker-compose logs -f

# Access at http://localhost
```

### Scenario 2: Development with Docker
```bash
# Use development compose file
docker-compose -f docker-compose.dev.yml up -d

# Access services directly:
# - Frontend: http://localhost:5173
# - Backend: http://localhost:8090/api
# - Via Nginx: http://localhost
```

### Scenario 3: Local Development (Hybrid)
```bash
# Only run database in Docker
docker-compose up -d postgres

# Run backend locally
mvn spring-boot:run

# Run frontend locally
cd frontend
npm run dev
```

### Scenario 4: Rebuild After Changes
```bash
# Rebuild specific service
docker-compose build backend
docker-compose up -d backend

# Rebuild everything
docker-compose up -d --build

# Rebuild without cache
docker-compose build --no-cache
docker-compose up -d
```

## 🔍 Monitoring and Debugging

### Check Service Status
```bash
docker-compose ps
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
docker-compose logs -f nginx

# Last 100 lines
docker-compose logs --tail=100 backend
```

### Health Checks
```bash
# Nginx health endpoint
curl http://localhost/health

# Backend health
curl http://localhost/api/actuator/health

# Database health
docker-compose exec postgres pg_isready -U postgres
```

### Access Container Shell
```bash
# Backend
docker-compose exec backend sh

# Frontend
docker-compose exec frontend sh

# Database
docker-compose exec postgres psql -U postgres -d test_analytics_v2

# Nginx
docker-compose exec nginx sh
```

### Resource Monitoring
```bash
# Monitor resources
docker stats

# Container details
docker-compose exec backend top
```

## 🗄️ Database Management

### Access PostgreSQL
```bash
docker-compose exec postgres psql -U postgres -d test_analytics_v2
```

### Backup Database
```bash
docker-compose exec postgres pg_dump -U postgres test_analytics_v2 > backup.sql
```

### Restore Database
```bash
docker-compose exec -T postgres psql -U postgres -d test_analytics_v2 < backup.sql
```

### View Tables
```bash
docker-compose exec postgres psql -U postgres -d test_analytics_v2 -c "\dt"
```

## 🛑 Stopping Services

### Stop (containers remain)
```bash
docker-compose stop
# or
./docker-stop.sh
```

### Stop and Remove
```bash
docker-compose down
# or
./docker-stop.sh --remove
```

### Complete Cleanup (including data)
```bash
docker-compose down -v
# or
./docker-stop.sh --clean
```

## 🔐 Security Considerations

### Production Checklist
- [ ] Change default PostgreSQL password in `.env`
- [ ] Use strong passwords for all services
- [ ] Don't expose PostgreSQL port externally
- [ ] Enable HTTPS in nginx (add SSL certificates)
- [ ] Use Docker secrets for sensitive data
- [ ] Keep base images updated
- [ ] Set resource limits in docker-compose.yml
- [ ] Enable firewall rules
- [ ] Use non-root user in containers (already configured)
- [ ] Regular security audits

### SSL/HTTPS Setup (Optional)
To enable HTTPS, modify `nginx/nginx.conf`:
```nginx
server {
    listen 443 ssl http2;
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    # ... rest of config
}
```

Mount certificates in docker-compose.yml:
```yaml
nginx:
  volumes:
    - ./ssl:/etc/nginx/ssl:ro
```

## 📊 Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_DB` | test_analytics_v2 | Database name |
| `POSTGRES_USER` | postgres | Database user |
| `POSTGRES_PASSWORD` | postgres | Database password |
| `POSTGRES_PORT` | 5432 | Database port (dev mode) |
| `NGINX_PORT` | 80 | Nginx port |
| `BACKEND_PORT` | 8090 | Backend port (dev mode) |
| `FRONTEND_PORT` | 5173 | Frontend port (dev mode) |
| `SPRING_PROFILES` | prod | Spring profile (prod/dev) |
| `JAVA_OPTS` | -Xmx512m -Xms256m | JVM options |

## 🧪 Testing the Setup

### 1. Test Database
```bash
docker-compose exec postgres psql -U postgres -d test_analytics_v2 -c "SELECT version();"
```

### 2. Test Backend
```bash
curl http://localhost/api/actuator/health
```

### 3. Test Frontend
```bash
curl -I http://localhost/
```

### 4. Test Reverse Proxy
```bash
curl http://localhost/health
```

## 🚨 Troubleshooting

### Port Conflicts
```bash
# Check what's using port 80
sudo lsof -i :80

# Change nginx port
echo "NGINX_PORT=8080" >> .env
docker-compose up -d
```

### Database Connection Failed
```bash
# Check if postgres is running
docker-compose ps postgres

# Check logs
docker-compose logs postgres

# Restart postgres
docker-compose restart postgres
```

### Backend Won't Start
```bash
# Check logs
docker-compose logs backend

# Wait for postgres health check
docker-compose ps

# Rebuild backend
docker-compose build --no-cache backend
docker-compose up -d backend
```

### Nginx 502 Bad Gateway
```bash
# Backend might not be ready
docker-compose logs backend

# Check network connectivity
docker-compose exec nginx ping backend
docker-compose exec nginx ping frontend
```

### Out of Disk Space
```bash
# Clean up Docker
docker system prune -a --volumes

# Check disk usage
docker system df
```

## 📚 Additional Resources

- [QUICK_START.md](QUICK_START.md) - Quick reference guide
- [DOCKER_SETUP.md](DOCKER_SETUP.md) - Detailed setup documentation
- [README.md](README.md) - Application documentation
- [frontend/README.md](frontend/README.md) - Frontend documentation

## 🤝 Contributing

When making changes to Docker configuration:
1. Test with `docker-compose.yml` (production)
2. Test with `docker-compose.dev.yml` (development)
3. Update documentation
4. Test clean build: `docker-compose build --no-cache`
5. Test startup: `docker-compose up -d`
6. Verify health checks: `docker-compose ps`

## 📝 Notes

- First startup may take 2-3 minutes
- Database migrations run automatically via Flyway
- Frontend API calls go through nginx reverse proxy
- All containers use Alpine Linux for minimal size
- Health checks ensure proper startup order
- Volumes persist data across container restarts

---

**Questions? Check [DOCKER_SETUP.md](DOCKER_SETUP.md) for detailed information.**

