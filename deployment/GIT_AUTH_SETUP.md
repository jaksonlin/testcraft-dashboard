# Git Authentication Setup for TestCraft Dashboard

The TestCraft Dashboard needs to clone Git repositories for scanning. Here are the supported authentication methods:

## Method 1: SSH Keys (Recommended)

### Setup Steps:

1. **Create SSH key directory:**
   ```bash
   mkdir -p deployment/ssh-keys
   ```

2. **Add your SSH private key:**
   ```bash
   # Copy your existing SSH key
   cp ~/.ssh/id_rsa deployment/ssh-keys/
   chmod 600 deployment/ssh-keys/id_rsa
   
   # Or generate a new one
   ssh-keygen -t rsa -b 4096 -f deployment/ssh-keys/id_rsa -N ""
   ```

3. **Add public key to Git provider:**
   ```bash
   # Display public key to add to GitHub/GitLab
   cat deployment/ssh-keys/id_rsa.pub
   ```

4. **Configure environment:**
   ```bash
   # Create .env file
   cp deployment/env.example deployment/.env
   
   # Edit .env and ensure these are set:
   GIT_SSH_KEY_PATH=/app/ssh-keys/id_rsa
   SSH_KEY_PATH=./ssh-keys
   ```

## Method 2: HTTPS with Username/Token

### Setup Steps:

1. **Create GitHub/GitLab Personal Access Token:**
   - GitHub: Settings → Developer settings → Personal access tokens
   - GitLab: User Settings → Access Tokens

2. **Configure environment:**
   ```bash
   # Create .env file
   cp deployment/env.example deployment/.env
   
   # Edit .env and set:
   GIT_USERNAME=your_username
   GIT_PASSWORD=your_token_or_password
   ```

## Method 3: Public Repositories Only

If you only need to scan public repositories:

```bash
# Create .env file
cp deployment/env.example deployment/.env

# Leave GIT_USERNAME and GIT_PASSWORD empty
# The application will clone public repos without authentication
```

## Testing Git Authentication

After setup, test with:

```bash
# Build and start
cd deployment
docker-compose build backend
docker-compose up -d

# Check logs for Git operations
docker-compose logs -f backend
```

## Repository URL Formats

The application supports these Git URL formats:

- **SSH**: `git@github.com:user/repo.git`
- **HTTPS**: `https://github.com/user/repo.git`
- **Custom SSH**: `ssh://git@git.example.com:2222/user/repo.git`

## Troubleshooting

### SSH Key Issues:
- Ensure private key has correct permissions (600)
- Verify public key is added to Git provider
- Check SSH key format (OpenSSH, PEM supported)

### HTTPS Issues:
- Verify username/token are correct
- Check token has repository access permissions
- Ensure repository URLs use HTTPS format

### SSH Host Key Issues:
- **First-time clone prompts**: The application automatically handles SSH host key verification
- **Known hosts**: Common Git servers (GitHub) are pre-populated in known_hosts
- **Custom servers**: For custom Git servers, the application uses `StrictHostKeyChecking=no` to avoid interactive prompts
- **Security**: Host keys are verified but interactive prompts are disabled for automation

### Permission Issues:
- SSH keys are mounted read-only (`:ro`)
- Repository storage uses Docker volume
- Application runs as non-root user

### Interactive Prompt Issues:
The application is designed to handle Git operations without user interaction:
- SSH host key verification is handled automatically
- Common Git servers are pre-populated in known_hosts
- Interactive prompts are disabled for automated operations
- If you still see prompts, check that the SSH key path is correct
