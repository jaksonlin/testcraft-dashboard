# TestCraft Dashboard - Quick Start Guide

## 🚀 Getting Started with Docker

### Prerequisites
- Docker (20.10+)
- Docker Compose (2.0+)

### Quick Start

1. **Start all services**:
   ```bash
   ./docker-start.sh
   ```
   Or manually:
   ```bash
   docker-compose up -d
   ```

2. **Access the application**:
   - Open your browser: http://localhost
   - API: http://localhost/api
   - Health check: http://localhost/health

3. **Stop services**:
   ```bash
   ./docker-stop.sh
   ```
   Or manually:
   ```bash
   docker-compose stop
   ```

### First Time Setup

1. **Copy environment file** (optional):
   ```bash
   cp env.example .env
   ```
   Edit `.env` to customize database credentials and ports if needed.

2. **Start services** (will build on first run):
   ```bash
   ./docker-start.sh --build
   ```

3. **Monitor logs**:
   ```bash
   docker-compose logs -f
   ```

4. **Wait for initialization**:
   - Backend startup: ~30-60 seconds
   - Database migrations: Automatic via Flyway
   - First time: May take 2-3 minutes

### Architecture

```
Port 80 (Nginx) → Frontend (React)
                → Backend API (Spring Boot) → PostgreSQL
```

### Common Commands

```bash
# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f backend

# Restart a service
docker-compose restart backend

# Stop all services
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop and remove everything including data
docker-compose down -v

# Rebuild containers
docker-compose up -d --build

# Check service status
docker-compose ps
```

### Troubleshooting

**Port 80 already in use?**
```bash
# Option 1: Stop conflicting service
sudo systemctl stop nginx  # or apache2

# Option 2: Change port in .env
echo "NGINX_PORT=8080" >> .env
# Then access at http://localhost:8080
```

**Database connection issues?**
```bash
# Check PostgreSQL logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

**Backend not starting?**
```bash
# View backend logs
docker-compose logs backend

# Rebuild backend
docker-compose build backend
docker-compose up -d backend
```

### Development Mode

For local development (without Docker):

1. **Start PostgreSQL** (only):
   ```bash
   docker-compose up -d postgres
   ```

2. **Backend** (from project root):
   ```bash
   mvn spring-boot:run
   ```

3. **Frontend** (from frontend directory):
   ```bash
   cd frontend
   cp .env.local.example .env.local  # Configure API URL
   npm install
   npm run dev
   ```

4. **Access**:
   - Frontend: http://localhost:5173
   - Backend: http://localhost:8090/api

### Next Steps

- Read [DOCKER_SETUP.md](DOCKER_SETUP.md) for detailed configuration
- Check [README.md](README.md) for application features
- View [frontend/README.md](frontend/README.md) for frontend details

## 📞 Need Help?

- Check service health: `curl http://localhost/health`
- View all logs: `docker-compose logs -f`
- Check container status: `docker-compose ps`

---

**Happy Testing! 🧪**

