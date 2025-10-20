# TestCraft Dashboard - Deployment Structure

## 📁 New Organization

All Docker deployment files have been organized into the `deployment/` directory for better project structure.

## 🗂️ Directory Structure

```
testcraft-dashboard/
├── deployment/                          # ⭐ All Docker deployment files
│   ├── docker-compose.yml               # Production orchestration
│   ├── docker-compose.dev.yml           # Development with exposed ports
│   ├── Dockerfile.backend               # Backend container build
│   ├── Dockerfile.frontend              # Frontend container build
│   ├── .dockerignore                    # Build exclusions
│   ├── env.example                      # Environment template
│   ├── init-db.sql                      # PostgreSQL initialization
│   ├── nginx/                           # Nginx configuration
│   │   ├── nginx.conf                   # Reverse proxy
│   │   └── frontend.conf                # Frontend server
│   ├── docker-start.sh                  # Start script (internal)
│   ├── docker-stop.sh                   # Stop script (internal)
│   ├── DOCKER_SETUP.md                  # Detailed guide
│   ├── DOCKER_README.md                 # Usage documentation
│   ├── QUICK_START.md                   # Quick reference
│   ├── DOCKER_DEPLOYMENT_SUMMARY.md     # Overview
│   ├── DOCKER_FILES_CREATED.txt         # File listing
│   └── README.md                        # Deployment directory guide
│
├── docker-compose.yml                   # ⭐ Root wrapper (convenience)
├── docker-start.sh                      # ⭐ Root start script (recommended)
├── docker-stop.sh                       # ⭐ Root stop script (recommended)
├── .dockerignore                        # Docker build exclusions
├── .env                                 # Environment variables (create from deployment/env.example)
│
├── src/                                 # Backend source code
├── frontend/                            # Frontend source code
├── pom.xml                              # Maven configuration
└── README.md                            # Project documentation
```

## 🚀 How to Use

### Method 1: Using Root Scripts (Recommended)

The project root has convenience wrapper scripts:

```bash
# From project root
./docker-start.sh --build    # Start all services
./docker-stop.sh             # Stop services
```

### Method 2: Using Docker Compose Directly

```bash
# From project root
docker-compose up -d --build    # Start
docker-compose logs -f          # View logs
docker-compose stop             # Stop
```

### Method 3: From Deployment Directory

```bash
cd deployment
# Then use docker-compose.yml in current directory
# (requires proper path adjustments)
```

## 📋 What's at the Root vs Deployment Directory

### Root Directory Files
- `docker-compose.yml` - Wrapper that references deployment files
- `docker-start.sh` - Convenient start script
- `docker-stop.sh` - Convenient stop script
- `.dockerignore` - Build exclusions (Docker needs this at root)
- `.env` - Environment variables (create from deployment/env.example)

### Deployment Directory Files
- All Docker configuration files
- Dockerfiles for backend and frontend
- Nginx configuration
- Database initialization scripts
- Comprehensive documentation
- Original scripts (still work but use root scripts instead)

## 🔧 Configuration

### 1. Create Environment File
```bash
# Copy from deployment directory to root
cp deployment/env.example .env
# Edit as needed
nano .env
```

### 2. Start Services
```bash
# Simple start
./docker-start.sh

# With rebuild
./docker-start.sh --build
```

### 3. Access Application
- **Main App**: http://localhost
- **API**: http://localhost/api
- **Health**: http://localhost/health

## 📚 Documentation

All detailed documentation is in the `deployment/` directory:

| File | Purpose |
|------|---------|
| `deployment/README.md` | Deployment directory overview |
| `deployment/QUICK_START.md` | Quick reference guide |
| `deployment/DOCKER_README.md` | Complete usage guide |
| `deployment/DOCKER_SETUP.md` | Detailed configuration |
| `deployment/DOCKER_DEPLOYMENT_SUMMARY.md` | What's included |

## 🎯 Key Points

1. **Root scripts are recommended** - They handle everything automatically
2. **Environment file goes at root** - Create `.env` from `deployment/env.example`
3. **All docker config is in deployment/** - Clean project structure
4. **Build context is project root** - Dockerfiles access src/ and frontend/
5. **Documentation is comprehensive** - Check `deployment/` for details

## 🔄 Migration from Old Structure

If you have an existing `.env` file from before the reorganization:
- ✅ Keep it at the project root - it still works!
- ✅ No changes needed to environment variables
- ✅ Same commands work: `./docker-start.sh`, `docker-compose up`, etc.

## 🌐 Access Points

After starting (`./docker-start.sh`):

```
http://localhost              → Main Application (via nginx)
http://localhost/api          → Backend API (via nginx)
http://localhost/health       → Health check endpoint
localhost:5432                → PostgreSQL (direct access)
```

## 💡 Tips

- Use `./docker-start.sh --build` for first time or after changes
- Use `./docker-stop.sh --clean` to reset everything (⚠️ deletes data!)
- Check `docker-compose logs -f` to monitor startup
- See `deployment/README.md` for more details

## 🛠️ Development Mode

For development with all ports exposed:

```bash
docker-compose -f deployment/docker-compose.dev.yml up -d
```

Access:
- Frontend direct: http://localhost:5173
- Backend direct: http://localhost:8090
- Via nginx: http://localhost
- Database: localhost:5432

## ✅ Benefits of New Structure

1. **Cleaner root directory** - Deployment files organized separately
2. **Better organization** - All related files in one place
3. **Easier to find** - Documentation grouped with configs
4. **Maintains convenience** - Root scripts still work the same way
5. **Professional structure** - Standard for larger projects

## 📞 Need Help?

1. Quick start: `deployment/QUICK_START.md`
2. Full guide: `deployment/DOCKER_README.md`
3. Troubleshooting: `deployment/DOCKER_SETUP.md`
4. Overview: `deployment/DOCKER_DEPLOYMENT_SUMMARY.md`

---

**TL;DR**: Run `./docker-start.sh --build` from project root. Everything works as before, just better organized! 🎉

