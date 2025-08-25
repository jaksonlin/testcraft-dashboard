# Database Connection from Command Line

This document explains how to specify database connections from the command line for the Annotation Extractor project.

## Overview

The system now supports overriding database connection parameters via command line arguments. These parameters take precedence over the `database.properties` file and provide flexibility for different environments.

## Available CLI Options

All database-related classes support these command line options:

| Option | Description | Default Value |
|--------|-------------|---------------|
| `--db-host` | Database host/IP address | `localhost` |
| `--db-port` | Database port number | `5432` |
| `--db-name` | Database name | `test_analytics` |
| `--db-user` | Database username | `postgres` |
| `--db-pass` | Database password | `postgres` |

## Usage Examples

### 1. RepositoryHubRunner

```bash
# Basic usage with custom database
java RepositoryHubRunner ./repos ./repo-list.txt --db-host mydb.example.com --db-name production_db

# Full database override
java RepositoryHubRunner ./repos ./repo-list.txt --db-host 192.168.1.100 --db-port 5433 --db-name mydb --db-user myuser --db-pass mypass

# Mix with authentication
java RepositoryHubRunner ./repos ./repo-list.txt myuser mytoken --db-host localhost --db-name test_db
```

### 2. TestCollectionRunner

```bash
# Initialize database with custom connection
java TestCollectionRunner /path/to/repositories --init-db --db-host mydb.example.com --db-name production_db

# Full example with all options
java TestCollectionRunner /path/to/repositories --init-db --generate-report --db-host localhost --db-port 5433 --db-name test_db --db-user testuser --db-pass testpass

# Override only specific parameters
java TestCollectionRunner /path/to/repositories --init-db --db-host 10.0.0.50
```

### 3. DatabaseConnectionTester

```bash
# Test default connection
java DatabaseConnectionTester

# Test custom connection
java DatabaseConnectionTester --host mydb.example.com --port 5433 --db mydb --user myuser --pass mypass

# Test with different database
java DatabaseConnectionTester --db production_db

# Test with different host
java DatabaseConnectionTester --host 192.168.1.100
```

## Parameter Precedence

The system follows this order of precedence:

1. **CLI parameters** (highest priority)
2. **database.properties file**
3. **Hardcoded defaults** (lowest priority)

## Fallback Behavior

When you specify only some CLI parameters, the system will:

1. Use your specified CLI parameters
2. Fall back to `database.properties` for missing parameters
3. Use hardcoded defaults if neither CLI nor properties are available

**Example:**
```bash
java RepositoryHubRunner ./repos ./repo-list.txt --db-host mydb.example.com --db-name production_db
```

This will use:
- Host: `mydb.example.com` (from CLI)
- Port: `5432` (from properties file or default)
- Database: `production_db` (from CLI)
- Username: `test_user` (from properties file or default)
- Password: `123456` (from properties file or default)

## Testing Database Connections

Use the `DatabaseConnectionTester` class to verify connections before running the main applications:

```bash
# Test with properties file settings
java DatabaseConnectionTester

# Test with custom settings
java DatabaseConnectionTester --host mydb.example.com --db production_db --user myuser --pass mypass
```

The tester will:
- Attempt to connect to the database
- Display connection information
- Show PostgreSQL version
- Display connection pool statistics
- Provide clear success/error messages

## Environment-Specific Examples

### Development Environment
```bash
java RepositoryHubRunner ./repos ./repo-list.txt --db-host localhost --db-name dev_analytics
```

### Staging Environment
```bash
java RepositoryHubRunner ./repos ./repo-list.txt --db-host staging-db.company.com --db-name staging_analytics --db-user staging_user --db-pass staging_pass
```

### Production Environment
```bash
java RepositoryHubRunner ./repos ./repo-list.txt --db-host prod-db.company.com --db-port 5433 --db-name prod_analytics --db-user prod_user --db-pass prod_pass
```

### Docker Environment
```bash
java RepositoryHubRunner ./repos ./repo-list.txt --db-host postgres-container --db-name analytics --db-user postgres --db-pass postgres
```

## Security Considerations

- **Passwords in CLI**: Be aware that passwords passed via command line may be visible in process lists and shell history
- **Environment Variables**: Consider using environment variables for sensitive information in production
- **Network Security**: Ensure database connections use appropriate network security (firewalls, VPNs, etc.)

## Troubleshooting

### Common Issues

1. **Connection Refused**: Check if database is running and accessible
2. **Authentication Failed**: Verify username/password
3. **Database Not Found**: Ensure database name exists
4. **Port Issues**: Verify port number and firewall settings

### Debug Mode

Add verbose logging by checking the console output for connection details:

```bash
java RepositoryHubRunner ./repos ./repo-list.txt --db-host mydb.example.com --db-name test_db
```

The system will display:
- Database connection parameters being used
- Connection initialization status
- Any connection errors with details

## Batch Files and Scripts

Use the provided batch/shell scripts for quick testing:

**Windows:**
```cmd
test-database-connection.bat
```

**Linux/Mac:**
```bash
./test-database-connection.sh
```

These scripts demonstrate various connection scenarios and can be modified for your specific needs.
