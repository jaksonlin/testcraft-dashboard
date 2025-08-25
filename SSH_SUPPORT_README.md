# SSH Support for Repository Hub Scanner

## Overview

The Repository Hub Scanner now supports SSH authentication for cloning Git repositories, allowing you to use your existing SSH key setup instead of converting SSH URLs to HTTPS.

## SSH Key Format Support

### 1. OpenSSH Format (Recommended) ⭐
- **File patterns**: `id_rsa`, `id_ed25519`, `id_ecdsa`
- **Generation**: `ssh-keygen -t rsa -b 4096`
- **Location**: `~/.ssh/id_rsa`
- **Compatibility**: Full support, best performance

### 2. PEM Format
- **File patterns**: `.pem`, `.key` files
- **Compatibility**: Full support, OpenSSH compatible
- **Use case**: AWS, cloud provider keys, converted keys

### 3. PuTTY Format (.ppk) ⚠️
- **File patterns**: `.ppk` files
- **Compatibility**: Limited support, may require conversion
- **Recommendation**: Convert to OpenSSH format for best results

## Usage Examples

### SSH Authentication (Recommended)

```bash
# Use default SSH configuration (ssh-agent, ~/.ssh/id_rsa, etc.)
java -jar annotation-extractor-1.0.0.jar ./repos ./sample-repositories.txt

# Use specific SSH key
java -jar annotation-extractor-1.0.0.jar ./repos ./sample-repositories.txt ~/.ssh/github_key

# Use specific SSH key with username (if needed)
java -jar annotation-extractor-1.0.0.jar ./repos ./sample-repositories.txt myuser mytoken ~/.ssh/github_key
```

### HTTPS Authentication (Alternative)

```bash
# Use username/password for HTTPS
java -jar annotation-extractor-1.0.0.jar ./repos ./sample-repositories.txt myuser mytoken
```

### No Authentication (Public Repos Only)

```bash
# Clone public repositories only
java -jar annotation-extractor-1.0.0.jar ./repos ./sample-repositories.txt
```

## SSH Key Setup

### 1. Generate OpenSSH Key (Recommended)

```bash
# Generate RSA key
ssh-keygen -t rsa -b 4096 -C "your_email@example.com"

# Generate Ed25519 key (more secure, smaller)
ssh-keygen -t ed25519 -C "your_email@example.com"
```

### 2. Add Key to SSH Agent

```bash
# Start ssh-agent
eval "$(ssh-agent -s)"

# Add your key
ssh-add ~/.ssh/id_rsa
```

### 3. Add Public Key to GitHub

```bash
# Copy public key content
cat ~/.ssh/id_rsa.pub

# Add to GitHub: Settings > SSH and GPG keys > New SSH key
```

## Converting PuTTY Keys to OpenSSH

If you have PuTTY keys (.ppk files), convert them to OpenSSH format:

### Using PuTTYgen (Windows)

1. Open PuTTYgen
2. Load your .ppk file
3. Go to **Conversions** > **Export OpenSSH key**
4. Save as `id_rsa` (without extension)
5. Use the converted key with the tool

### Using ssh-keygen (Linux/macOS)

```bash
# Convert PuTTY key to OpenSSH format
ssh-keygen -p -f your_key.ppk -m pem
```

## Troubleshooting

### Common SSH Issues

1. **"remote hung up unexpectedly"**
   - Check SSH key is added to GitHub account
   - Verify ssh-agent is running: `ssh-add -l`
   - Test SSH connection: `ssh -T git@github.com`

2. **"Authentication failed"**
   - Verify SSH key format is supported
   - Check key permissions: `chmod 600 ~/.ssh/id_rsa`
   - Ensure key is loaded in ssh-agent

3. **"Permission denied"**
   - Check repository access permissions
   - Verify SSH key is associated with correct GitHub account

### Getting Help

```bash
# Show SSH key guidance
java -jar annotation-extractor-1.0.0.jar --ssh-help

# Show general help
java -jar annotation-extractor-1.0.0.jar --help
```

## Repository List Format

Your `sample-repositories.txt` file can contain both SSH and HTTPS URLs:

```txt
# SSH URLs (will use SSH authentication)
git@github.com:jaksonlin/testcraft-dashboard.git
git@github.com:jaksonlin/db2h2.git

# HTTPS URLs (will use username/password if provided)
https://github.com/example/repo1.git
https://github.com/example/repo2
```

## Best Practices

1. **Use OpenSSH format keys** for best compatibility
2. **Keep SSH keys secure** with proper permissions (600)
3. **Use ssh-agent** to avoid typing passphrases repeatedly
4. **Test SSH connection** before running the tool
5. **Convert PuTTY keys** to OpenSSH format when possible

## Security Notes

- SSH private keys should have permissions 600 (`chmod 600 ~/.ssh/id_rsa`)
- Public keys can have permissions 644 (`chmod 644 ~/.ssh/id_rsa.pub`)
- Never share or commit private keys to version control
- Use strong passphrases for SSH keys
- Regularly rotate SSH keys for security

## Dependencies

The tool uses:
- **JGit**: Git operations
- **JSch**: SSH transport support
- **JZlib**: Compression support

All dependencies are included in the shaded JAR file.
