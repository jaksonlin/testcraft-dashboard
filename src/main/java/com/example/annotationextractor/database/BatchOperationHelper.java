package com.example.annotationextractor.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Helper class for vendor-neutral batch operations with duplicate handling.
 * 
 * Best Practices:
 * 1. Uses "try batch, fallback to individual" pattern to handle duplicates gracefully
 * 2. Vendor-neutral approach that works across PostgreSQL, MySQL, SQLite, etc.
 * 3. Provides error tolerance for batch operations
 * 
 * Usage:
 *   BatchOperationHelper.executeBatchWithFallback(conn, items, batchSize, (stmt, item) -> {
 *       stmt.setString(1, item.getValue());
 *       stmt.addBatch();
 *   }, insertSql);
 */
public class BatchOperationHelper {

    /**
     * Database vendor types
     */
    public enum DatabaseVendor {
        POSTGRESQL,
        MYSQL,
        SQLITE,
        H2,
        ORACLE,
        SQLSERVER,
        UNKNOWN
    }

    /**
     * Detect database vendor from connection metadata
     */
    public static DatabaseVendor detectDatabaseVendor(Connection conn) throws SQLException {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            String productName = metaData.getDatabaseProductName().toLowerCase();
            
            if (productName.contains("postgresql")) {
                return DatabaseVendor.POSTGRESQL;
            } else if (productName.contains("mysql")) {
                return DatabaseVendor.MYSQL;
            } else if (productName.contains("sqlite")) {
                return DatabaseVendor.SQLITE;
            } else if (productName.contains("h2")) {
                return DatabaseVendor.H2;
            } else if (productName.contains("oracle")) {
                return DatabaseVendor.ORACLE;
            } else if (productName.contains("microsoft sql server") || productName.contains("sql server")) {
                return DatabaseVendor.SQLSERVER;
            }
        } catch (SQLException e) {
            // If detection fails, return UNKNOWN
        }
        return DatabaseVendor.UNKNOWN;
    }

    /**
     * Execute batch operation with automatic fallback to individual operations on failure.
     * 
     * This is the recommended vendor-neutral approach that:
     * 1. Tries to execute as a batch for performance
     * 2. If batch fails (e.g., due to duplicates), falls back to individual operations
     * 3. Continues processing even if some items fail
     * 
     * @param conn Database connection
     * @param items List of items to process
     * @param batchSize Size of each batch (typically 100-1000)
     * @param parameterSetter Function to set parameters on PreparedStatement for each item
     * @param sql SQL statement to execute
     * @param <T> Type of items being processed
     * @return List of items that failed to process (for error reporting)
     */
    public static <T> List<T> executeBatchWithFallback(
            Connection conn,
            List<T> items,
            int batchSize,
            BiConsumer<PreparedStatement, T> parameterSetter,
            String sql) throws SQLException {
        
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> failedItems = new ArrayList<>();
        List<T> batchItems = new ArrayList<>();

        // Process in chunks
        for (int i = 0; i < items.size(); i++) {
            batchItems.add(items.get(i));

            // Execute batch when we reach batchSize or at the end
            if (batchItems.size() >= batchSize || i == items.size() - 1) {
                List<T> batchFailed = executeBatchOrFallback(conn, batchItems, parameterSetter, sql);
                failedItems.addAll(batchFailed);
                batchItems.clear();
            }
        }

        return failedItems;
    }

    /**
     * Try to execute as batch, fallback to individual if batch fails
     */
    private static <T> List<T> executeBatchOrFallback(
            Connection conn,
            List<T> items,
            BiConsumer<PreparedStatement, T> parameterSetter,
            String sql) throws SQLException {
        
        // Try batch operation first
        try {
            executeBatch(conn, items, parameterSetter, sql);
            return new ArrayList<>(); // Success - no failures
        } catch (SQLException batchException) {
            // Check if it's a duplicate/key constraint error
            if (isDuplicateKeyError(batchException)) {
                // Fallback to individual operations with error tolerance
                return executeIndividuallyWithTolerance(conn, items, parameterSetter, sql);
            } else {
                // Non-duplicate error - rethrow
                throw batchException;
            }
        }
    }

    /**
     * Execute batch operation
     */
    private static <T> void executeBatch(
            Connection conn,
            List<T> items,
            BiConsumer<PreparedStatement, T> parameterSetter,
            String sql) throws SQLException {
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (T item : items) {
                parameterSetter.accept(stmt, item);
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            
            // Check if any batch item failed
            for (int i = 0; i < results.length; i++) {
                if (results[i] == Statement.EXECUTE_FAILED) {
                    // This indicates a batch item failed
                    throw new SQLException("Batch operation failed at index " + i);
                }
            }
        }
    }

    /**
     * Execute items individually, collecting failures
     */
    private static <T> List<T> executeIndividuallyWithTolerance(
            Connection conn,
            List<T> items,
            BiConsumer<PreparedStatement, T> parameterSetter,
            String sql) throws SQLException {
        
        List<T> failedItems = new ArrayList<>();
        
        for (T item : items) {
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    parameterSetter.accept(stmt, item);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                // Log individual failure but continue processing
                if (isDuplicateKeyError(e)) {
                    // Duplicate - expected in some cases, can be ignored or logged
                    failedItems.add(item);
                } else {
                    // Unexpected error - might want to rethrow or log differently
                    failedItems.add(item);
                }
            }
        }
        
        return failedItems;
    }

    /**
     * Check if SQLException is a duplicate key/constraint violation error
     * Works across different database vendors
     */
    public static boolean isDuplicateKeyError(SQLException e) {
        String sqlState = e.getSQLState();
        String message = e.getMessage().toLowerCase();
        int errorCode = e.getErrorCode();

        // PostgreSQL: 23505 = unique_violation
        if ("23505".equals(sqlState)) {
            return true;
        }
        
        // MySQL: Error code 1062 = Duplicate entry
        if (errorCode == 1062) {
            return true;
        }
        
        // SQLite: Error code 19 = UNIQUE constraint failed
        if (errorCode == 19 && message.contains("unique")) {
            return true;
        }
        
        // H2: Error code 23505 (similar to PostgreSQL)
        if ("23505".equals(sqlState) || (errorCode == 23505)) {
            return true;
        }
        
        // Oracle: ORA-00001 = unique constraint violated
        if (errorCode == 1 && message.contains("unique constraint")) {
            return true;
        }
        
        // SQL Server: Error 2627 = Violation of PRIMARY KEY constraint
        if (errorCode == 2627) {
            return true;
        }
        
        // Generic check for common error messages
        if (message.contains("duplicate") || 
            message.contains("unique constraint") ||
            message.contains("primary key violation")) {
            return true;
        }
        
        return false;
    }

    /**
     * Build UPSERT SQL statement based on database vendor.
     * This allows using native UPSERT for better performance while remaining vendor-aware.
     * 
     * Example usage:
     *   String upsertSql = buildUpsertSql(vendor, "test_methods", 
     *       "scan_session_id, test_class_id, method_name",
     *       "method_signature = ?, line_number = ?");
     */
    public static String buildUpsertSql(
            DatabaseVendor vendor,
            String tableName,
            String conflictColumns,  // Comma-separated column names for conflict detection
            String updateClause) {   // SET clause for update (without SET keyword)
        
        String conflictCols = conflictColumns.replaceAll("\\s+", ""); // Remove spaces
        
        switch (vendor) {
            case POSTGRESQL:
            case H2:
                // PostgreSQL: INSERT ... ON CONFLICT ... DO UPDATE
                return String.format(
                    "INSERT INTO %s (%s) VALUES (%s) " +
                    "ON CONFLICT (%s) DO UPDATE SET %s",
                    tableName, conflictColumns, buildPlaceholders(conflictCols.split(",").length),
                    conflictCols, updateClause);
            
            case MYSQL:
                // MySQL: INSERT ... ON DUPLICATE KEY UPDATE
                return String.format(
                    "INSERT INTO %s (%s) VALUES (%s) " +
                    "ON DUPLICATE KEY UPDATE %s",
                    tableName, conflictColumns, buildPlaceholders(conflictCols.split(",").length),
                    updateClause);
            
            case SQLITE:
                // SQLite: INSERT ... ON CONFLICT ... DO UPDATE
                return String.format(
                    "INSERT INTO %s (%s) VALUES (%s) " +
                    "ON CONFLICT (%s) DO UPDATE SET %s",
                    tableName, conflictColumns, buildPlaceholders(conflictCols.split(",").length),
                    conflictCols, updateClause);
            
            case ORACLE:
            case SQLSERVER:
                // Use MERGE statement for Oracle and SQL Server
                // Note: This is a simplified version - may need table alias
                return String.format(
                    "MERGE INTO %s AS target " +
                    "USING (SELECT ? AS col1) AS source " +
                    "ON (target.%s = source.col1) " +
                    "WHEN MATCHED THEN UPDATE SET %s " +
                    "WHEN NOT MATCHED THEN INSERT (%s) VALUES (%s)",
                    tableName, conflictCols.split(",")[0], updateClause,
                    conflictColumns, buildPlaceholders(conflictCols.split(",").length));
            
            default:
                // Fallback: Use vendor-neutral try-update-then-insert pattern
                throw new UnsupportedOperationException(
                    "UPSERT not supported for database vendor. Use executeBatchWithFallback instead.");
        }
    }

    private static String buildPlaceholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(", ");
            sb.append("?");
        }
        return sb.toString();
    }
}

