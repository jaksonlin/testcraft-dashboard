# TestCraft Dashboard - Deployment Guide

## Quick Start

### 1. Setup Environment

Copy the example environment file and configure:

```bash
cd deployment
cp env.example .env
# Edit .env with your settings
```

### 2. Configure Git Authentication (Optional)

**Method 1: SSH Keys (Recommended)**
```bash
# Place your SSH private key
mkdir -p ssh-keys
cp ~/.ssh/id_rsa ssh-keys/
chmod 600 ssh-keys/id_rsa
```

Update `.env`:
```bash
SSH_KEY_PATH=./ssh-keys
GIT_SSH_KEY_PATH=/home/spring/.ssh/id_rsa
```

**Method 2: HTTPS with Token**

Update `.env`:
```bash
GIT_USERNAME=your_username
GIT_PASSWORD=your_token_or_password
```

### 3. Start Services

```bash
# Start all services
docker-compose -f docker-compose.yml build
docker-compose up -d

# View logs
docker-compose logs -f

# Check status
docker-compose ps

chmod -R 777 ./reports (or corresponding report directory)
```

### 4. Access Application

- **Frontend**: http://localhost (via Nginx)
- **Backend API**: http://localhost/api
- **Direct Backend**: http://localhost:8090

## Configuration

### Database Settings

The application uses two PostgreSQL databases:
- **Primary**: For application data
- **Shadow**: For Flyway migrations (optional, requires `--profile shadow`)

Configure in `.env`:
```bash
DB_PASSWORD=your_password
SHADOW_DB_PASSWORD=your_shadow_password
```

### Network Configuration

The application uses a custom bridge network (`testcraft-network`) with subnet `172.20.0.0/16` to avoid conflicts with host networks.

Service IPs:
- PostgreSQL: 172.20.0.10
- Backend: 172.20.0.20
- Frontend: 172.20.0.30
- Nginx: 172.20.0.40

## Export/Import for Isolated Environments

### Export Images

```bash
# Save images to tar files
docker save testcraft-dashboard-backend:latest -o backend.tar
docker save testcraft-dashboard-frontend:latest -o frontend.tar
docker save postgres:16-alpine -o postgres.tar
docker save nginx:alpine -o nginx.tar
```

### Import and Run in Isolated Environment

```bash
# Load images
docker load -i backend.tar
docker load -i frontend.tar
docker load -i postgres.tar
docker load -i nginx.tar

# Start services
cd deployment
docker-compose up -d
```

## Troubleshooting

### Check Service Health

```bash
# View all services
docker-compose ps

# Check specific service logs
docker-compose logs backend
docker-compose logs frontend

# Check database connection
docker-compose exec backend wget -O- http://localhost:8090/api/actuator/health
```

### Restart Services

```bash
# Restart specific service
docker-compose restart backend

# Rebuild and restart
docker-compose up -d --build backend
```

### Clean Up

```bash
# Stop all services
docker-compose down

# Remove volumes (WARNING: deletes data)
docker-compose down -v

# Remove images
docker-compose down --rmi all
```

## Documentation

- `STARTUP_GUIDE.md` - Detailed startup instructions
- `GIT_AUTH_SETUP.md` - Git authentication configuration
- `NETWORK_SETUP.md` - Docker network details
- `ENVIRONMENT_VARIABLES.md` - All available environment variables

