package com.example.annotationextractor.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages database schema creation and initialization
 */
public class DatabaseSchemaManager {
    
    /**
     * Initialize the database schema - creates all necessary tables
     */
    public static void initializeSchema() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                createRepositoriesTable(conn);
                createScanSessionsTable(conn);
                createTestClassesTable(conn);
                createTestMethodsTable(conn);
                createDailyMetricsTable(conn);
                createIndexes(conn);
                
                conn.commit();
                System.out.println("Database schema initialized successfully");
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    
    /**
     * Create repositories table
     */
    private static void createRepositoriesTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS repositories (" +
            "id BIGSERIAL PRIMARY KEY," +
            "repository_name VARCHAR(255) NOT NULL," +
            "repository_path VARCHAR(500) NOT NULL," +
            "git_url VARCHAR(500)," +
            "git_branch VARCHAR(100) DEFAULT 'main'," +
            "technology_stack TEXT[]," +
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
        }
    }
    
    /**
     * Create scan sessions table
     */
    private static void createScanSessionsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS scan_sessions (" +
            "id BIGSERIAL PRIMARY KEY," +
            "scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "scan_directory VARCHAR(500) NOT NULL," +
            "total_repositories INT DEFAULT 0," +
            "total_test_classes INT DEFAULT 0," +
            "total_test_methods INT DEFAULT 0," +
            "total_annotated_methods INT DEFAULT 0," +
            "scan_duration_ms BIGINT DEFAULT 0," +
            "scan_status VARCHAR(50) DEFAULT 'COMPLETED'," +
            "error_log TEXT," +
            "metadata JSONB" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create test classes table
     */
    private static void createTestClassesTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS test_classes (" +
            "id BIGSERIAL PRIMARY KEY," +
            "repository_id BIGINT REFERENCES repositories(id) ON DELETE CASCADE," +
            "class_name VARCHAR(255) NOT NULL," +
            "package_name VARCHAR(500)," +
            "file_path VARCHAR(500)," +
            "file_size_bytes BIGINT," +
            "total_test_methods INT DEFAULT 0," +
            "annotated_test_methods INT DEFAULT 0," +
            "coverage_rate DECIMAL(5,2) DEFAULT 0.00," +
            "first_seen_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "scan_session_id BIGINT REFERENCES scan_sessions(id)," +
            "UNIQUE(repository_id, class_name, package_name)" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create test methods table
     */
    private static void createTestMethodsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS test_methods (" +
            "id BIGSERIAL PRIMARY KEY," +
            "test_class_id BIGINT REFERENCES test_classes(id) ON DELETE CASCADE," +
            "method_name VARCHAR(255) NOT NULL," +
            "method_signature TEXT," +
            "line_number INT," +
            "has_annotation BOOLEAN DEFAULT FALSE," +
            "annotation_data JSONB," +
            "annotation_title VARCHAR(500)," +
            "annotation_author VARCHAR(255)," +
            "annotation_status VARCHAR(100)," +
            "annotation_target_class VARCHAR(255)," +
            "annotation_target_method VARCHAR(255)," +
            "annotation_description TEXT," +
            "annotation_tags TEXT[]," +
            "annotation_test_points TEXT[]," +
            "annotation_requirements TEXT[]," +
            "annotation_defects TEXT[]," +
            "annotation_testcases TEXT[]," +
            "annotation_last_update_time VARCHAR(255)," +
            "annotation_last_update_author VARCHAR(255)," +
            "first_seen_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "scan_session_id BIGINT REFERENCES scan_sessions(id)," +
            "UNIQUE(test_class_id, method_name, method_signature)" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create daily metrics table
     */
    private static void createDailyMetricsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS daily_metrics (" +
            "id BIGSERIAL PRIMARY KEY," +
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
        }
    }
    
    /**
     * Create performance indexes
     */
    private static void createIndexes(Connection conn) throws SQLException {
        String[] indexSqls = {
            "CREATE INDEX IF NOT EXISTS idx_repositories_name ON repositories(repository_name)",
            "CREATE INDEX IF NOT EXISTS idx_repositories_team ON repositories(team_ownership)",
            "CREATE INDEX IF NOT EXISTS idx_test_classes_repo ON test_classes(repository_id)",
            "CREATE INDEX IF NOT EXISTS idx_test_methods_class ON test_methods(test_class_id)",
            "CREATE INDEX IF NOT EXISTS idx_scan_sessions_date ON scan_sessions(scan_date)",
            "CREATE INDEX IF NOT EXISTS idx_daily_metrics_date ON daily_metrics(metric_date)",
            "CREATE INDEX IF NOT EXISTS idx_annotation_data ON test_methods USING GIN(annotation_data)"
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String sql : indexSqls) {
                stmt.execute(sql);
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
