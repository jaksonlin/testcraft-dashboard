-- Migration: Add method LOC to test_methods table
-- Version: 10
-- Description: Track lines of code in test method bodies

-- Add new column to test_methods table
ALTER TABLE test_methods 
ADD COLUMN IF NOT EXISTS method_loc INT DEFAULT 0;

-- Add comment for documentation
COMMENT ON COLUMN test_methods.method_loc IS 'Lines of code in the test method body (from start line to end line of method)';
