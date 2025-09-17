package com.example.annotationextractor.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database configuration and connection management for PostgreSQL
 */
public class DatabaseConfig {
    
    private static HikariDataSource dataSource;
    private static HikariDataSource shadowDataSource;
    private static Properties dbProperties;
    
    // Default database configuration
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_DATABASE = "test_analytics";
    private static final String DEFAULT_USERNAME = "postgres";
    private static final String DEFAULT_PASSWORD = "postgres";
    
    static {
        loadProperties();
    }
    
    /**
     * Load database properties from the properties file
     */
    private static void loadProperties() {
        dbProperties = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input != null) {
                dbProperties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load database.properties, using defaults: " + e.getMessage());
        }
    }
    
    /**
     * Get a property value with fallback to default
     */
    private static String getProperty(String key, String defaultValue) {
        return dbProperties != null ? dbProperties.getProperty(key, defaultValue) : defaultValue;
    }
    
    /**
     * Get an integer property value with fallback to default
     */
    private static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Initialize the database connection pool using properties file
     */
    public static void initialize() {
        String host = getProperty("db.host", DEFAULT_HOST);
        int port = getIntProperty("db.port", Integer.parseInt(DEFAULT_PORT));
        String database = getProperty("db.name", DEFAULT_DATABASE);
        String username = getProperty("db.username", DEFAULT_USERNAME);
        String password = getProperty("db.password", DEFAULT_PASSWORD);
        
        initialize(host, port, database, username, password);
    }
    
    /**
     * Initialize the database connection pool with custom settings
     */
    public static void initialize(String host, int port, String database, String username, String password) {
        HikariConfig config = new HikariConfig();
        
        // Database connection settings
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", host, port, database));
        config.setUsername(username);
        config.setPassword(password);
        
        // Connection pool settings from properties or defaults
        config.setMaximumPoolSize(getIntProperty("db.pool.maxSize", 10));
        config.setMinimumIdle(getIntProperty("db.pool.minIdle", 5));
        config.setIdleTimeout(getIntProperty("db.pool.idleTimeout", 300000)); // 5 minutes
        config.setMaxLifetime(getIntProperty("db.pool.maxLifetime", 1800000)); // 30 minutes
        config.setConnectionTimeout(getIntProperty("db.pool.connectionTimeout", 30000)); // 30 seconds
        
        // PostgreSQL specific settings
        config.addDataSourceProperty("cachePrepStmts", getProperty("db.postgres.cachePrepStmts", "true"));
        config.addDataSourceProperty("prepStmtCacheSize", getProperty("db.postgres.prepStmtCacheSize", "250"));
        config.addDataSourceProperty("prepStmtCacheSqlLimit", getProperty("db.postgres.prepStmtCacheSqlLimit", "2048"));
        config.addDataSourceProperty("useServerPrepStmts", getProperty("db.postgres.useServerPrepStmts", "true"));
        config.addDataSourceProperty("useLocalSessionState", getProperty("db.postgres.useLocalSessionState", "true"));
        config.addDataSourceProperty("rewriteBatchedStatements", getProperty("db.postgres.rewriteBatchedStatements", "true"));
        config.addDataSourceProperty("cacheResultSetMetadata", getProperty("db.postgres.cacheResultSetMetadata", "true"));
        config.addDataSourceProperty("cacheServerConfiguration", getProperty("db.postgres.cacheServerConfiguration", "true"));
        config.addDataSourceProperty("elideSetAutoCommits", getProperty("db.postgres.elideSetAutoCommits", "true"));
        config.addDataSourceProperty("maintainTimeStats", getProperty("db.postgres.maintainTimeStats", "false"));
        
        dataSource = new HikariDataSource(config);
        
        System.out.println("Database connection initialized:");
        System.out.println("  Host: " + host);
        System.out.println("  Port: " + port);
        System.out.println("  Database: " + database);
        System.out.println("  Username: " + username);
    }

    /**
     * Initialize a separate shadow database pool if configured.
     * Uses shadow.db.* properties; falls back to primary db if not set.
     */
    public static void initializeShadowIfConfigured() {
        String shadowHost = getProperty("shadow.db.host", null);
        String shadowPortStr = getProperty("shadow.db.port", null);
        String shadowDb = getProperty("shadow.db.name", null);
        String shadowUser = getProperty("shadow.db.username", null);
        String shadowPass = getProperty("shadow.db.password", null);

        if (shadowHost == null || shadowDb == null || shadowUser == null || shadowPass == null) {
            // No shadow config present; skip initialization
            return;
        }

        int shadowPort;
        try {
            shadowPort = Integer.parseInt(shadowPortStr != null ? shadowPortStr : DEFAULT_PORT);
        } catch (NumberFormatException e) {
            shadowPort = Integer.parseInt(DEFAULT_PORT);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", shadowHost, shadowPort, shadowDb));
        config.setUsername(shadowUser);
        config.setPassword(shadowPass);

        // Pool settings (reuse primaries unless overridden)
        config.setMaximumPoolSize(getIntProperty("shadow.db.pool.maxSize", getIntProperty("db.pool.maxSize", 10)));
        config.setMinimumIdle(getIntProperty("shadow.db.pool.minIdle", getIntProperty("db.pool.minIdle", 5)));
        config.setIdleTimeout(getIntProperty("shadow.db.pool.idleTimeout", getIntProperty("db.pool.idleTimeout", 300000)));
        config.setMaxLifetime(getIntProperty("shadow.db.pool.maxLifetime", getIntProperty("db.pool.maxLifetime", 1800000)));
        config.setConnectionTimeout(getIntProperty("shadow.db.pool.connectionTimeout", getIntProperty("db.pool.connectionTimeout", 30000)));

        shadowDataSource = new HikariDataSource(config);
        System.out.println("Shadow database connection initialized: host=" + shadowHost + ", db=" + shadowDb);
    }
    
    /**
     * Initialize database with CLI parameters (overrides properties file)
     */
    public static void initializeFromCli(String host, String port, String database, String username, String password) {
        int portNum;
        try {
            portNum = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + port + ", using default: 5432");
            portNum = 5432;
        }
        initialize(host, portNum, database, username, password);
    }
    
    /**
     * Get a database connection from the pool
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initialize();
        }
        return dataSource.getConnection();
    }

    /**
     * Get a connection for shadow writes if configured; otherwise returns a primary connection.
     */
    public static Connection getShadowConnection() throws SQLException {
        if (shadowDataSource == null) {
            initializeShadowIfConfigured();
        }
        if (shadowDataSource != null) {
            return shadowDataSource.getConnection();
        }
        return getConnection();
    }
    
    /**
     * Close the connection pool
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        if (shadowDataSource != null && !shadowDataSource.isClosed()) {
            shadowDataSource.close();
        }
    }
    
    /**
     * Check if the connection pool is healthy
     */
    public static boolean isHealthy() {
        return dataSource != null && !dataSource.isClosed();
    }
    
    /**
     * Get connection pool statistics
     */
    public static String getPoolStats() {
        if (dataSource == null) {
            return "Connection pool not initialized";
        }
        
        return String.format(
            "Pool Stats - Active: %d, Idle: %d, Total: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections()
        );
    }
}
