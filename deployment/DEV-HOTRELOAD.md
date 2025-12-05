# 🔥 Hot Reload Development Setup

This guide explains how to use hot reload during development so you don't need to rebuild Docker images on every code change.

## 🚀 Quick Start

### Option 1: Automatic Hot Reload (Recommended)

**Terminal 1** - Start Docker services:
```bash
cd deployment
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

**Terminal 2** - Start the auto-compiler watcher:
```bash
cd ..
./dev-watch.sh
```

Now when you edit Java files, they'll be automatically compiled, and Spring DevTools will restart the app in Docker! ⚡

### Option 2: Manual Compilation

Start Docker as above, then manually compile when needed:
```bash
mvn compile -DskipTests
```

The app will restart automatically after each compilation.

## 🔧 How It Works

1. **Spring Boot DevTools** watches the `target/classes` directory for changes
2. **Volume mount** shares your `target` directory between host and Docker
3. **Auto-compiler** (`dev-watch.sh`) detects file changes and runs `mvn compile`
4. **DevTools detects** the new `.class` files and **restarts the Spring app** (fast restart, ~2-3 seconds)

### Architecture:
```
┌─────────────────────────────────────────┐
│ Host Machine                            │
│                                         │
│  ┌──────────┐         ┌──────────┐    │
│  │ You edit │ ──────> │ dev-watch│    │
│  │ .java    │         │ compiles │    │
│  └──────────┘         └─────┬────┘    │
│                              │         │
│                              v         │
│                        target/classes  │
│                              │         │
└──────────────────────────────┼─────────┘
                               │ (volume mount)
┌──────────────────────────────┼─────────┐
│ Docker Container             v         │
│                                         │
│  ┌────────────────────────────────┐   │
│  │ Spring Boot DevTools           │   │
│  │  - Watches target/classes      │   │
│  │  - Detects changes             │   │
│  │  - Restarts app (2-3s)         │   │
│  └────────────────────────────────┘   │
│                                         │
└─────────────────────────────────────────┘
```

## 🎯 What Triggers Restart?

### ✅ These changes trigger hot reload:
- Java source files (`.java`)
- Resource files (`.properties`, `.yml`, `.xml`)
- Configuration changes in `application.yml`

### ❌ These require full rebuild:
- `pom.xml` dependency changes
- Dockerfile changes
- docker-compose configuration changes

For full rebuilds:
```bash
cd deployment
docker-compose -f docker-compose.yml -f docker-compose.dev.yml down
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

## 📊 Monitoring Hot Reload

Watch the Docker logs to see restarts:
```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml logs -f backend
```

You'll see messages like:
```
testcraft-backend | 2025-10-22 06:15:23 - Restarting due to 5 class path changes
testcraft-backend | 2025-10-22 06:15:26 - Started TestCraftDashboardApplication in 2.847 seconds
```

## 🐛 Remote Debugging

The dev setup also enables remote debugging on port **5005**.

### IntelliJ IDEA:
1. **Run** → **Edit Configurations**
2. **Add New Configuration** → **Remote JVM Debug**
3. Set **Host**: `localhost`, **Port**: `5005`
4. Click **Debug** to attach

### VS Code:
Add to `.vscode/launch.json`:
```json
{
  "type": "java",
  "request": "attach",
  "name": "Debug TestCraft Backend",
  "hostName": "localhost",
  "port": 5005
}
```

## 🔍 Debug Endpoints

The backend exposes several debug endpoints to help verify the system state:

### Database & Connection
- `GET /debug/database-info`: Check database connection status
- `GET /debug/table-counts`: View record counts for key tables

### Repository Data
- `GET /debug/repositories-raw`: List all repositories directly from the database
- `GET /debug/repository/{id}/test-methods-raw`: View test methods for a specific repository
  - Shows data for the **latest completed scan session**
  - Includes comparison counts from recent sessions
  - Useful for verifying data ingestion without UI filtering logic

## 🔍 Troubleshooting

### Restart not happening?
1. Check if `target/classes` has new `.class` files:
   ```bash
   ls -lt target/classes/com/example/annotationextractor/ | head
   ```

2. Check DevTools is enabled in logs:
   ```bash
   docker-compose logs backend | grep -i devtools
   ```

### Compilation errors?
```bash
mvn clean compile
```

### Complete reset?
```bash
cd deployment
docker-compose -f docker-compose.yml -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

## ⚙️ Configuration

### DevTools Settings

Already configured in `docker-compose.dev.yml`:
- `SPRING_DEVTOOLS_RESTART_ENABLED: "true"` - Enable DevTools
- `SPRING_DEVTOOLS_RESTART_POLL_INTERVAL: "1000"` - Check every 1 second
- `SPRING_DEVTOOLS_RESTART_QUIET_PERIOD: "500"` - Wait 500ms before restart

### Excluding Files from Restart

Add to `src/main/resources/application-dev.yml`:
```yaml
spring:
  devtools:
    restart:
      exclude: static/**,public/**,templates/**
```

## 📈 Performance Tips

1. **Use SSD** - File watching is much faster on SSD
2. **Limit file watching scope** - The watcher only monitors `src/`
3. **Incremental compilation** - Maven only recompiles changed files
4. **DevTools restart** - Much faster than full JVM restart (~2-3s vs ~30s)

## 🎓 Best Practices

1. **Keep Docker running** - Leave `docker-compose up` running in Terminal 1
2. **Keep watcher running** - Leave `./dev-watch.sh` running in Terminal 2
3. **Make changes** - Edit files normally in your IDE
4. **Wait for restart** - Give it 2-5 seconds to compile + restart
5. **Test immediately** - No need to rebuild Docker!

## 📝 Development Workflow

```bash
# Day 1: Initial setup
cd deployment
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build

# Open another terminal
cd ..
./dev-watch.sh

# Day 2+: Just start the containers
cd deployment
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up

# Keep dev-watch.sh running for auto-compile
cd ..
./dev-watch.sh
```

## 🚫 When NOT to Use Hot Reload

Use full rebuild when:
- Adding new dependencies to `pom.xml`
- Changing Docker configuration
- Changing environment variables in docker-compose
- Deploying to production (always use production build)

For production deployment, use:
```bash
docker-compose -f docker-compose.yml up --build
```

## 💡 Tips

- **File watcher performance**: Install `inotify-tools` (Linux) or `fswatch` (macOS) for instant detection
- **IDE integration**: Most IDEs can trigger compilation on save
- **Logs**: Use `docker-compose logs -f backend` to watch restart progress
- **Database changes**: Flyway migrations run automatically on restart

Happy coding! 🎉

