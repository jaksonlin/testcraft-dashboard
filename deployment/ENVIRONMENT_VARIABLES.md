# Environment Variables for Git Authentication

## How Environment Variables Work

The TestCraft Dashboard application reads Git authentication settings from environment variables. Here's how it works:

### Environment Variable Flow

1. **Docker Compose** sets environment variables in `docker-compose.yml`
2. **Container** receives these variables at startup
3. **Java Application** reads them using `System.getenv()`
4. **GitRepositoryManager** uses them for Git operations

### Required Environment Variables

```bash
# SSH Key Path (for SSH authentication)
GIT_SSH_KEY_PATH=/app/ssh-keys/id_rsa

# Repository Storage Path
REPOSITORY_HUB_PATH=/app/repos

# Optional: HTTPS Authentication
GIT_USERNAME=your_username
GIT_PASSWORD=your_token
```

### How the Application Uses Them

The application reads environment variables in `ScheduledScanService.java`:

```java
// Read Git authentication from environment variables
String gitUsername = System.getenv("GIT_USERNAME");
String gitPassword = System.getenv("GIT_PASSWORD");
String gitSshKeyPath = System.getenv("GIT_SSH_KEY_PATH");
String repositoryHubPath = System.getenv("REPOSITORY_HUB_PATH");

// Use environment variables or fallback to configuration
String finalRepoPath = repositoryHubPath != null ? repositoryHubPath : repositoryHubPathRef.get();

GitRepositoryManager gitManager = new GitRepositoryManager(
    finalRepoPath, 
    gitUsername, // username from environment
    gitPassword, // password from environment
    gitSshKeyPath  // sshKeyPath from environment
);
```

### Docker Compose Configuration

In `docker-compose.yml`, these variables are set:

```yaml
environment:
  # Git Authentication Settings
  GIT_USERNAME: ${GIT_USERNAME:-}
  GIT_PASSWORD: ${GIT_PASSWORD:-}
  GIT_SSH_KEY_PATH: ${GIT_SSH_KEY_PATH:-/app/ssh-keys/id_rsa}
  REPOSITORY_HUB_PATH: ${REPOSITORY_HUB_PATH:-/app/repos}
```

### .env File Configuration

Create `deployment/.env`:

```bash
# Git Authentication
GIT_SSH_KEY_PATH=/app/ssh-keys/id_rsa
SSH_KEY_PATH=./ssh-keys
REPOSITORY_HUB_PATH=/app/repos

# Optional HTTPS
# GIT_USERNAME=your_username
# GIT_PASSWORD=your_token
```

## Testing Environment Variables

### 1. Check Environment Variables
```bash
# Test environment variable reading
docker-compose exec backend bash /app/test-env.sh
```

### 2. Check Application Logs
```bash
# Look for environment variable usage in logs
docker-compose logs backend | grep -E "(GIT_|SSH_|REPO_)"
```

### 3. Manual Environment Check
```bash
# Check environment variables inside container
docker-compose exec backend env | grep -E "(GIT|SSH|REPO)"
```

## Troubleshooting

### Issue: Environment Variables Not Set

**Symptoms**: Application uses default values instead of SSH key

**Solution**:
```bash
# Check if .env file exists
ls -la deployment/.env

# Create .env file if missing
cp deployment/env.example deployment/.env

# Edit .env file
nano deployment/.env
```

### Issue: SSH Key Path Wrong

**Symptoms**: "Could not load key" errors

**Solution**:
```bash
# Check SSH key path in container
docker-compose exec backend ls -la /app/ssh-keys/

# Verify environment variable
docker-compose exec backend echo $GIT_SSH_KEY_PATH
```

### Issue: Repository Path Not Set

**Symptoms**: Repositories cloned to wrong location

**Solution**:
```bash
# Check repository path
docker-compose exec backend echo $REPOSITORY_HUB_PATH

# Verify directory exists
docker-compose exec backend ls -la /app/repos
```

## Environment Variable Priority

1. **Environment Variables** (highest priority)
2. **Configuration Properties** (fallback)
3. **Default Values** (lowest priority)

## Complete Setup Example

```bash
# 1. Create .env file
cd deployment
cp env.example .env

# 2. Edit .env file
cat >> .env << EOF
GIT_SSH_KEY_PATH=/app/ssh-keys/id_rsa
SSH_KEY_PATH=./ssh-keys
REPOSITORY_HUB_PATH=/app/repos
EOF

# 3. Set up SSH keys
mkdir -p ssh-keys
cp ~/.ssh/id_rsa ssh-keys/
chmod 600 ssh-keys/id_rsa

# 4. Start services
docker-compose up -d

# 5. Test environment variables
docker-compose exec backend bash /app/test-env.sh
```

## Expected Results

After proper setup:

✅ **Environment variables are set correctly**  
✅ **SSH key path points to mounted file**  
✅ **Repository path is accessible**  
✅ **Application uses SSH key for Git operations**  
✅ **No "Permission denied" errors**  

The application will now automatically use the SSH key from `/app/ssh-keys/id_rsa` for all Git operations!
