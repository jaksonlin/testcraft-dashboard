# TestCraft Dashboard - Startup Guide

## Quick Start Options

### Option 1: Standard Setup (Primary Database Only)
```bash
cd deployment
cp env.example .env
# Edit .env with your settings
docker-compose up -d
```

### Option 2: With Shadow Database (Dual-Write Pattern)
```bash
cd deployment
cp env.example .env
# Edit .env and uncomment SHADOW_DB_PASSWORD
./start-with-shadow.sh
```

### Option 3: Development Mode
```bash
cd deployment
docker-compose -f docker-compose.dev.yml up -d
```

## Service Ports

| Service | Port | Description |
|---------|------|-------------|
| **Nginx** | 80 | Main application (frontend + API proxy) |
| **Backend** | 8090 | Direct API access (bypass nginx) |
| **PostgreSQL** | 5432 | Primary database |
| **PostgreSQL Shadow** | 5433 | Shadow database (if enabled) |

## Database Configuration

### Primary Database
- **Database**: `test_analytics`
- **User**: `postgres`
- **Password**: Set via `DB_PASSWORD` in `.env`
- **Port**: `5432`

### Shadow Database (Optional)
- **Database**: `test_analytics_shadow`
- **User**: `postgres`
- **Password**: Set via `SHADOW_DB_PASSWORD` in `.env`
- **Port**: `5433`
- **Purpose**: Dual-write pattern for performance validation

## Environment Variables

### Required
```bash
DB_PASSWORD=your_secure_password
```

### Optional
```bash
# Shadow database
SHADOW_DB_PASSWORD=your_shadow_password

# Git authentication
GIT_USERNAME=your_username
GIT_PASSWORD=your_token
GIT_SSH_KEY_PATH=/app/ssh-keys/id_rsa
SSH_KEY_PATH=./ssh-keys

# Application settings
SPRING_PROFILE=docker
VITE_API_URL=/api
REPOSITORY_HUB_PATH=/app/repos
```

## Verification Steps

### 1. Check Service Status
```bash
docker-compose ps
```

### 2. Check Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f postgres
```

### 3. Test Database Connections
```bash
# Primary database
docker-compose exec backend nc -zv postgres 5432

# Shadow database (if enabled)
docker-compose exec backend nc -zv postgres-shadow 5432
```

### 4. Test API Endpoints
```bash
# Health check
curl http://localhost/api/actuator/health

# Dashboard data
curl http://localhost/api/dashboard/overview
```

### 5. Test Frontend
```bash
# Open browser
open http://localhost
# or
curl http://localhost
```

## Troubleshooting

### Database Connection Issues
```bash
# Check database logs
docker-compose logs postgres

# Test database connectivity
docker-compose exec backend psql -h postgres -U postgres -d test_analytics -c "SELECT 1;"
```

### Frontend Not Loading
```bash
# Check nginx logs
docker-compose logs nginx

# Check frontend container
docker-compose logs frontend

# Test nginx configuration
docker-compose exec nginx nginx -t
```

### Backend API Issues
```bash
# Check backend logs
docker-compose logs backend

# Test direct API access
curl http://localhost:8090/api/actuator/health
```

### Git Authentication Issues
```bash
# Check SSH keys
docker-compose exec backend ls -la /app/ssh-keys/

# Test Git connectivity
docker-compose exec backend git clone https://github.com/octocat/Hello-World.git /tmp/test
```

## Cleanup

### Stop Services
```bash
docker-compose down
```

### Remove Everything (including data)
```bash
docker-compose down -v
```

### Remove Images
```bash
docker-compose down --rmi all
```

## Network Information

- **Network**: `testcraft-network`
- **Subnet**: `172.20.0.0/16`
- **PostgreSQL**: `172.20.0.10`
- **PostgreSQL Shadow**: `172.20.0.11`
- **Backend**: `172.20.0.20`
- **Frontend**: `172.20.0.30`
- **Nginx**: `172.20.0.40`

## Advanced Usage

### Enable Shadow Database After Startup
```bash
# Start shadow database
docker-compose --profile shadow up -d postgres-shadow

# Check status
docker-compose ps
```

### Custom Configuration
```bash
# Override specific settings
DB_PASSWORD=custom_password docker-compose up -d

# Use different compose file
docker-compose -f docker-compose.dev.yml up -d
```

### Debug Mode
```bash
# Start with debug logging
docker-compose -f docker-compose.dev.yml up -d

# Access container shells
docker-compose exec backend bash
docker-compose exec postgres bash
```
