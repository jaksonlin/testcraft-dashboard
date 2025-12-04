-- Migration: Add file_path to test_classes unique constraint
-- Version: 19
-- Description: Update unique constraint to include file_path to prevent conflicts when 
--              the same repository has classes with same name/package in different files
-- Date: 2025-12-04

-- Drop the old unique constraint
DROP INDEX IF EXISTS ux_test_classes_session_repo_class_package;

-- Create new unique constraint that includes file_path
-- This allows multiple classes with same name/package in different file paths within the same repository
CREATE UNIQUE INDEX IF NOT EXISTS ux_test_classes_session_repo_class_package_path
    ON test_classes (scan_session_id, repository_id, class_name, package_name, COALESCE(file_path, ''));

-- Add comment explaining the constraint
COMMENT ON INDEX ux_test_classes_session_repo_class_package_path IS 
    'Ensures uniqueness per scan session: repository, class name, package, and file path. ' ||
    'Allows same class name/package in different files within the same repository.';

