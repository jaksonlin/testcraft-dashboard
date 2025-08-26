package com.example.annotationextractor.database;

import java.sql.*;

/**
 * Service for handling database migrations and schema updates
 */
public class DatabaseMigrationService {
    
    /**
     * Migrate database to fix duplicate repository issues
     */
    public static void migrateToFixDuplicateRepositories() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                System.out.println("Starting database migration to fix duplicate repositories...");
                
                // Step 1: Clean up duplicate repositories
                cleanupDuplicateRepositories(conn);
                
                // Step 2: Drop the old constraint if it exists
                dropOldConstraint(conn);
                
                // Step 3: Add the new unique constraint on repository_name
                addNewConstraint(conn);
                
                conn.commit();
                System.out.println("Database migration completed successfully!");
                
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Migration failed: " + e.getMessage());
                throw e;
            }
        }
    }
    
    /**
     * Clean up duplicate repositories by keeping the most recent one
     */
    private static void cleanupDuplicateRepositories(Connection conn) throws SQLException {
        System.out.println("Cleaning up duplicate repositories...");
        
        // Find repositories with duplicate names
        String findDuplicatesSql = 
            "SELECT repository_name, COUNT(*) as count " +
            "FROM repositories " +
            "GROUP BY repository_name " +
            "HAVING COUNT(*) > 1";
        
        try (PreparedStatement stmt = conn.prepareStatement(findDuplicatesSql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String repoName = rs.getString("repository_name");
                int count = rs.getInt("count");
                
                System.out.println("Found " + count + " duplicates for repository: " + repoName);
                
                // Keep the most recent one, delete the others
                cleanupRepositoryDuplicates(conn, repoName);
            }
        }
    }
    
    /**
     * Clean up duplicates for a specific repository name
     */
    private static void cleanupRepositoryDuplicates(Connection conn, String repoName) throws SQLException {
        // Get all repositories with this name, ordered by last_scan_date (keep the most recent)
        String getDuplicatesSql = 
            "SELECT id, repository_path, last_scan_date " +
            "FROM repositories " +
            "WHERE repository_name = ? " +
            "ORDER BY last_scan_date DESC";
        
        try (PreparedStatement stmt = conn.prepareStatement(getDuplicatesSql)) {
            stmt.setString(1, repoName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (first) {
                        // Keep the first (most recent) one
                        long keepId = rs.getLong("id");
                        String keepPath = rs.getString("repository_path");
                        System.out.println("  Keeping repository ID " + keepId + " with path: " + keepPath);
                        first = false;
                    } else {
                        // Delete the duplicate
                        long deleteId = rs.getLong("id");
                        String deletePath = rs.getString("repository_path");
                        System.out.println("  Deleting duplicate repository ID " + deleteId + " with path: " + deletePath);
                        
                        // Delete related test classes and methods first (due to foreign key constraints)
                        deleteRelatedTestData(conn, deleteId);
                        
                        // Delete the repository
                        deleteRepository(conn, deleteId);
                    }
                }
            }
        }
    }
    
    /**
     * Delete test data related to a repository
     */
    private static void deleteRelatedTestData(Connection conn, long repositoryId) throws SQLException {
        // Delete test methods first (they reference test classes)
        String deleteMethodsSql = 
            "DELETE FROM test_methods " +
            "WHERE test_class_id IN (SELECT id FROM test_classes WHERE repository_id = ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(deleteMethodsSql)) {
            stmt.setLong(1, repositoryId);
            int deletedMethods = stmt.executeUpdate();
            System.out.println("    Deleted " + deletedMethods + " test methods");
        }
        
        // Delete test classes
        String deleteClassesSql = "DELETE FROM test_classes WHERE repository_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteClassesSql)) {
            stmt.setLong(1, repositoryId);
            int deletedClasses = stmt.executeUpdate();
            System.out.println("    Deleted " + deletedClasses + " test classes");
        }
    }
    
    /**
     * Delete a repository
     */
    private static void deleteRepository(Connection conn, long repositoryId) throws SQLException {
        String deleteSql = "DELETE FROM repositories WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setLong(1, repositoryId);
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                System.out.println("    Deleted repository ID " + repositoryId);
            }
        }
    }
    
    /**
     * Drop the old composite unique constraint
     */
    private static void dropOldConstraint(Connection conn) throws SQLException {
        System.out.println("Dropping old constraint...");
        
        try {
            // Try to drop the constraint if it exists
            String dropConstraintSql = 
                "ALTER TABLE repositories DROP CONSTRAINT IF EXISTS repositories_repository_name_repository_path_key";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(dropConstraintSql);
                System.out.println("  Old constraint dropped successfully");
            }
        } catch (SQLException e) {
            // Constraint might not exist, which is fine
            System.out.println("  Old constraint not found (this is normal)");
        }
    }
    
    /**
     * Add new unique constraint on repository_name only
     */
    private static void addNewConstraint(Connection conn) throws SQLException {
        System.out.println("Adding new unique constraint on repository_name...");
        
        String addConstraintSql = 
            "ALTER TABLE repositories ADD CONSTRAINT repositories_repository_name_key UNIQUE (repository_name)";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(addConstraintSql);
            System.out.println("  New constraint added successfully");
        }
    }
    
    /**
     * Check if migration is needed
     */
    public static boolean isMigrationNeeded() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Check if there are duplicate repository names
            String checkDuplicatesSql = 
                "SELECT COUNT(*) FROM (" +
                "  SELECT repository_name, COUNT(*) as count " +
                "  FROM repositories " +
                "  GROUP BY repository_name " +
                "  HAVING COUNT(*) > 1" +
                ") as duplicates";
            
            try (PreparedStatement stmt = conn.prepareStatement(checkDuplicatesSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    int duplicateCount = rs.getInt(1);
                    return duplicateCount > 0;
                }
            }
            
            return false;
        }
    }
}
