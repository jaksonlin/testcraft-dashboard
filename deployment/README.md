# Deployment Directory

This directory contains all Docker-related configuration files for deploying the TestCraft Dashboard.

## 📁 Contents

```
deployment/
├── docker-compose.yml              # Docker Compose configuration
├── docker-compose.dev.yml          # Development configuration
├── Dockerfile.backend              # Backend container build
├── Dockerfile.frontend             # Frontend container build
├── env.example                     # Environment variables template
├── init-db.sql                     # Database initialization
├── nginx/                          # Nginx configuration
│   ├── nginx.conf                  # Reverse proxy config
│   └── frontend.conf               # Frontend server config
├── DOCKER_SETUP.md                 # Detailed setup guide
├── DOCKER_README.md                # Usage documentation
├── QUICK_START.md                  # Quick reference
├── DOCKER_DEPLOYMENT_SUMMARY.md    # Overview
├── DOCKER_FILES_CREATED.txt        # File listing
└── README.md                       # This file
```

## 🚀 Quick Start

### Option 1: Run from Project Root (Recommended)

The project root has convenience scripts and a docker-compose.yml wrapper:

```bash
# From project root
./docker-start.sh --build
```

Or manually:

```bash
# From project root
docker-compose up -d --build
```

### Option 2: Run from Deployment Directory

```bash
# Navigate to deployment directory
cd deployment

# Copy environment file
cp env.example ../.env

# Start services (need to specify paths correctly)
cd ..
docker-compose -f deployment/docker-compose.yml up -d --build
```

## 📖 Documentation

- **[QUICK_START.md](QUICK_START.md)** - Fast reference for common tasks
- **[DOCKER_README.md](DOCKER_README.md)** - Complete usage guide
- **[DOCKER_SETUP.md](DOCKER_SETUP.md)** - Detailed configuration reference
- **[DOCKER_DEPLOYMENT_SUMMARY.md](DOCKER_DEPLOYMENT_SUMMARY.md)** - What's included

## ⚙️ Configuration

1. **Environment Variables**: Copy `env.example` to project root as `.env`
2. **Database Settings**: Configured in `.env`
3. **Nginx**: Modify `nginx/nginx.conf` for reverse proxy settings
4. **Build Context**: All Dockerfiles use project root as build context

## 🏗️ Architecture

```
nginx (Port 80)
├── / → Frontend (React on Nginx)
└── /api → Backend (Spring Boot)
           └── PostgreSQL (Port 5432)
```

## 📝 Notes

- All paths in docker-compose.yml are relative to **project root**
- Build context is **project root** (to access src/ and frontend/ directories)
- Volume mounts reference `deployment/` directory
- Scripts at project root provide convenience wrappers

## 🔧 Development vs Production

### Production (`docker-compose.yml`)
- Minimal exposed ports (only nginx on 80)
- Optimized builds
- Production Spring profile

### Development (`docker-compose.dev.yml`)
- All ports exposed for direct access
- Backend: 8090
- Frontend: 5173
- Nginx: 80
- PostgreSQL: 5432

Run dev mode:
```bash
docker-compose -f deployment/docker-compose.dev.yml up -d
```

## 🛠️ Common Commands

From project root:

```bash
# Start
./docker-start.sh

# Stop
./docker-stop.sh

# View logs
docker-compose logs -f

# Rebuild
docker-compose up -d --build

# Clean up
./docker-stop.sh --clean
```

## 📚 Getting Help

1. Check [QUICK_START.md](QUICK_START.md) for common tasks
2. Review [DOCKER_README.md](DOCKER_README.md) for troubleshooting
3. See [DOCKER_SETUP.md](DOCKER_SETUP.md) for detailed configuration

---

**Tip**: The project root has convenience scripts (`docker-start.sh`, `docker-stop.sh`) that handle everything automatically.

