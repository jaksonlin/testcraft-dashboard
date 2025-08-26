# Repository Hub Scanner

Automatically clone, update, and scan multiple git repositories for test annotations.

## Quick Start

### 1. Build the Project
```bash
mvn clean package
```

### 2. Create Repository List
Create `my-repositories.txt`:
```txt
# My Test Repositories
https://github.com/mycompany/backend-tests.git
https://github.com/mycompany/frontend-tests.git
```

### 3. Run Scanner
```bash
# Using JAR directly
java -jar target/annotation-extractor-1.0.0.jar ./repositories ./my-repositories.txt

# Using scripts
./run-repository-hub-scan.sh          # Unix/Linux/Mac
run-repository-hub-scan.bat           # Windows
```

## Command Line Usage

```bash
java -jar annotation-extractor-1.0.0.jar <repository_hub_path> <repository_list_file> [username] [password]
```

### Arguments
- `repository_hub_path`: Directory for repositories
- `repository_list_file`: Text file with git URLs
- `username`: Git username (optional, for private repos)
- `password`: Git password/token (optional, for private repos)

## How It Works

1. **Initialization**: Creates hub directory and initializes database
2. **Repository Management**: Clones new repos, pulls updates for existing ones
3. **Scanning**: Scans all repositories for Java test files and annotations
4. **Storage**: Stores results in PostgreSQL database

## Repository List Format

```txt
# Comments start with #
https://github.com/example/repo1.git
https://github.com/example/repo2
git@github.com:example/repo3.git
```

## Features

- ✅ Automatic repository cloning and updating
- ✅ Centralized repository hub
- ✅ Database storage of scan results
- ✅ Support for public and private repositories
- ✅ Pattern filtering capabilities
- ✅ Batch processing of multiple repositories
- ✅ Comprehensive Excel reporting with test method details for review

## Troubleshooting

- **Repository not found**: Check URL and access permissions
- **Authentication failed**: Verify username/password or SSH keys
- **Database error**: Check database configuration
- **Permission denied**: Ensure hub directory is writable

## Integration

### CI/CD Pipeline
```yaml
- name: Run Repository Hub Scan
  run: java -jar annotation-extractor-1.0.0.jar ./repos ./repo-list.txt
```

### Scheduled Scans
```bash
# Daily scan at 2 AM
0 2 * * * cd /path/to/project && java -jar target/annotation-extractor-1.0.0.jar ./repos ./repo-list.txt
```

For more details, see the main project documentation.
