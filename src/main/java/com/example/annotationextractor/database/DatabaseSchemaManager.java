package com.example.annotationextractor.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;

/**
 * Manages database schema creation and initialization
 */
public class DatabaseSchemaManager {
    
    /**
     * Get the appropriate auto-increment syntax for the current database
     */
    private static String getAutoIncrementSyntax(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        String databaseProductName = metaData.getDatabaseProductName().toLowerCase();
        
        if (databaseProductName.contains("postgresql")) {
            return "BIGSERIAL";
        } else if (databaseProductName.contains("mysql") || databaseProductName.contains("mariadb")) {
            return "BIGINT AUTO_INCREMENT";
        } else if (databaseProductName.contains("sql server")) {
            return "BIGINT IDENTITY(1,1)";
        } else if (databaseProductName.contains("oracle")) {
            return "NUMBER GENERATED ALWAYS AS IDENTITY";
        } else if (databaseProductName.contains("h2")) {
            return "BIGINT AUTO_INCREMENT";
        } else if (databaseProductName.contains("hsqldb")) {
            return "BIGINT IDENTITY";
        } else {
            // Default to BIGINT for unknown databases
            return "BIGINT";
        }
    }
    
    /**
     * Initialize the database schema - creates all necessary tables
     */
    public static void initializeSchema() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                System.out.println("=== DEBUG: Starting schema initialization ===");
                
                // Get auto-increment syntax for current database
                String autoIncrementSyntax = getAutoIncrementSyntax(conn);
                System.out.println("Using auto-increment syntax: " + autoIncrementSyntax);
                
                // Create tables in dependency order
                createScanSessionsTable(conn, autoIncrementSyntax);
                System.out.println("Scan sessions table created successfully");
                
                createRepositoriesTable(conn, autoIncrementSyntax);
                System.out.println("Repositories table created successfully");
                
                createTestClassesTable(conn, autoIncrementSyntax);
                System.out.println("Test classes table created successfully");
                
                createTestMethodsTable(conn, autoIncrementSyntax);
                System.out.println("Test methods table created successfully");
                
                createDailyMetricsTable(conn, autoIncrementSyntax);
                System.out.println("Daily metrics table created successfully");
                
                createIndexes(conn);
                System.out.println("Indexes created successfully");
                
                // Verify tables were actually created
                System.out.println("=== DEBUG: Verifying tables after creation ===");
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_name IN ('repositories', 'scan_sessions', 'test_classes', 'test_methods', 'daily_metrics')");
                    System.out.println("Tables found in information_schema:");
                    while (rs.next()) {
                        System.out.println("  - " + rs.getString("table_name"));
                    }
                }
                
                conn.commit();
                System.out.println("Database schema initialized successfully");
                
            } catch (SQLException e) {
                System.err.println("ERROR during schema initialization: " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
                throw e;
            }
        }
    }
    
    /**
     * Create repositories table
     */
    private static void createRepositoriesTable(Connection conn, String autoIncrementSyntax) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS repositories (" +
            "id " + autoIncrementSyntax + " PRIMARY KEY," +
            "repository_name VARCHAR(255) NOT NULL," +
            "repository_path VARCHAR(500) NOT NULL," +
            "git_url VARCHAR(500)," +
            "git_branch VARCHAR(100) DEFAULT 'main'," +
            "technology_stack TEXT," +
            "team_ownership VARCHAR(255)," +
            "first_scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "total_test_classes INT DEFAULT 0," +
            "total_test_methods INT DEFAULT 0," +
            "total_annotated_methods INT DEFAULT 0," +
            "annotation_coverage_rate DECIMAL(5,2) DEFAULT 0.00," +
            "UNIQUE(repository_name, repository_path)" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Repositories table creation SQL executed");
        } catch (SQLException e) {
            System.err.println("ERROR creating repositories table: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Create scan sessions table
     */
    private static void createScanSessionsTable(Connection conn, String autoIncrementSyntax) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS scan_sessions (" +
            "id " + autoIncrementSyntax + " PRIMARY KEY," +
            "scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "scan_directory VARCHAR(500) NOT NULL," +
            "total_repositories INT DEFAULT 0," +
            "total_test_classes INT DEFAULT 0," +
            "total_test_methods INT DEFAULT 0," +
            "total_annotated_methods INT DEFAULT 0," +
            "scan_duration_ms BIGINT DEFAULT 0," +
            "scan_status VARCHAR(50) DEFAULT 'COMPLETED'," +
            "error_log TEXT," +
            "metadata TEXT" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("ERROR creating scan_sessions table: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Create test classes table
     */
    private static void createTestClassesTable(Connection conn, String autoIncrementSyntax) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS test_classes (" +
            "id " + autoIncrementSyntax + " PRIMARY KEY," +
            "repository_id BIGINT," +
            "class_name VARCHAR(255) NOT NULL," +
            "package_name VARCHAR(500)," +
            "file_path VARCHAR(500)," +
            "file_size_bytes BIGINT," +
            "total_test_methods INT DEFAULT 0," +
            "annotated_test_methods INT DEFAULT 0," +
            "coverage_rate DECIMAL(5,2) DEFAULT 0.00," +
            "first_seen_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "scan_session_id BIGINT," +
            "UNIQUE(repository_id, class_name, package_name)," +
            "FOREIGN KEY (repository_id) REFERENCES repositories(id) ON DELETE CASCADE," +
            "FOREIGN KEY (scan_session_id) REFERENCES scan_sessions(id)" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("ERROR creating test_classes table: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Create test methods table
     */
    private static void createTestMethodsTable(Connection conn, String autoIncrementSyntax) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS test_methods (" +
            "id " + autoIncrementSyntax + " PRIMARY KEY," +
            "test_class_id BIGINT," +
            "method_name VARCHAR(255) NOT NULL," +
            "method_signature TEXT," +
            "line_number INT," +
            "has_annotation BOOLEAN DEFAULT FALSE," +
            "annotation_data TEXT," +
            "annotation_title VARCHAR(500)," +
            "annotation_author VARCHAR(255)," +
            "annotation_status VARCHAR(100)," +
            "annotation_target_class VARCHAR(255)," +
            "annotation_target_method VARCHAR(255)," +
            "annotation_description TEXT," +
            "annotation_tags TEXT," +
            "annotation_test_points TEXT," +
            "annotation_requirements TEXT," +
            "annotation_defects TEXT," +
            "annotation_testcases TEXT," +
            "annotation_last_update_time VARCHAR(255)," +
            "annotation_last_update_author VARCHAR(255)," +
            "first_seen_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "scan_session_id BIGINT," +
            "UNIQUE(test_class_id, method_name, method_signature)," +
            "FOREIGN KEY (test_class_id) REFERENCES test_classes(id) ON DELETE CASCADE," +
            "FOREIGN KEY (scan_session_id) REFERENCES scan_sessions(id)" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("ERROR creating test_methods table: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Create daily metrics table
     */
    private static void createDailyMetricsTable(Connection conn, String autoIncrementSyntax) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS daily_metrics (" +
            "id " + autoIncrementSyntax + " PRIMARY KEY," +
            "metric_date DATE NOT NULL," +
            "total_repositories INT DEFAULT 0," +
            "total_test_classes INT DEFAULT 0," +
            "total_test_methods INT DEFAULT 0," +
            "total_annotated_methods INT DEFAULT 0," +
            "overall_coverage_rate DECIMAL(5,2) DEFAULT 0.00," +
            "new_test_methods INT DEFAULT 0," +
            "new_annotated_methods INT DEFAULT 0," +
            "UNIQUE(metric_date)" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("ERROR creating daily_metrics table: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Create performance indexes
     */
    private static void createIndexes(Connection conn) throws SQLException {
        System.out.println("Creating basic indexes...");
        
        String[] indexSqls = {
            "CREATE INDEX IF NOT EXISTS idx_repositories_name ON repositories(repository_name)",
            "CREATE INDEX IF NOT EXISTS idx_repositories_team ON repositories(team_ownership)",
            "CREATE INDEX IF NOT EXISTS idx_test_classes_repo ON test_classes(repository_id)",
            "CREATE INDEX IF NOT EXISTS idx_test_methods_class ON test_methods(test_class_id)",
            "CREATE INDEX IF NOT EXISTS idx_scan_sessions_date ON scan_sessions(scan_date)",
            "CREATE INDEX IF NOT EXISTS idx_daily_metrics_date ON daily_metrics(metric_date)",
            "CREATE INDEX IF NOT EXISTS idx_annotation_data ON test_methods(annotation_data)"
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String sql : indexSqls) {
                stmt.execute(sql);
            }
        }
        
        System.out.println("Basic indexes created successfully");
        
        // Create additional performance indexes for large-scale scanning
        createPerformanceIndexes(conn);
    }
    
    /**
     * Create performance indexes for large-scale scanning
     */
    private static void createPerformanceIndexes(Connection conn) throws SQLException {
        System.out.println("Creating performance indexes...");
        
        // Index for test class lookups
        createIndexIfNotExists(conn, "idx_test_classes_repo_class", 
            "CREATE INDEX IF NOT EXISTS idx_test_classes_repo_class ON test_classes (repository_id, class_name, package_name)");
        
        // Index for annotation queries
        createIndexIfNotExists(conn, "idx_test_methods_annotation", 
            "CREATE INDEX IF NOT EXISTS idx_test_methods_annotation ON test_methods (has_annotation)");
        
        // Index for date-based queries
        createIndexIfNotExists(conn, "idx_repositories_last_scan", 
            "CREATE INDEX IF NOT EXISTS idx_repositories_last_scan ON repositories (last_scan_date)");
        
        // Index for coverage rate queries
        createIndexIfNotExists(conn, "idx_repositories_coverage", 
            "CREATE INDEX IF NOT EXISTS idx_repositories_coverage ON repositories (annotation_coverage_rate DESC)");
        
        // Composite index for common query patterns
        createIndexIfNotExists(conn, "idx_test_methods_composite", 
            "CREATE INDEX IF NOT EXISTS idx_test_methods_composite ON test_methods (test_class_id, has_annotation, scan_session_id)");
        
        System.out.println("Performance indexes created successfully");
    }
    
    /**
     * Create index if it doesn't exist
     */
    private static void createIndexIfNotExists(Connection conn, String indexName, String createSql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createSql);
        } catch (SQLException e) {
            // Index might already exist, check if it's a duplicate key error
            if (!e.getMessage().contains("already exists")) {
                throw e;
            }
        }
    }
    
    /**
     * Check if schema exists
     */
    public static boolean schemaExists() {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'repositories')")) {
            
            java.sql.ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getBoolean(1);
            
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Drop all tables (for testing/reset purposes)
     */
    public static void dropAllTables() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                String[] dropSqls = {
                    "DROP TABLE IF EXISTS test_methods CASCADE",
                    "DROP TABLE IF EXISTS test_classes CASCADE",
                    "DROP TABLE IF EXISTS scan_sessions CASCADE",
                    "DROP TABLE IF EXISTS repositories CASCADE",
                    "DROP TABLE IF EXISTS daily_metrics CASCADE"
                };
                
                try (Statement stmt = conn.createStatement()) {
                    for (String sql : dropSqls) {
                        stmt.execute(sql);
                    }
                }
                
                conn.commit();
                System.out.println("All tables dropped successfully");
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
