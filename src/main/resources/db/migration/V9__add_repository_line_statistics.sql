-- Migration: Add line count statistics to repositories table
-- Version: 9
-- Description: Track lines of JUnit test code and test-related (non-JUnit) code

-- Add new columns to repositories table
ALTER TABLE repositories 
ADD COLUMN IF NOT EXISTS test_code_lines INT DEFAULT 0,
ADD COLUMN IF NOT EXISTS test_related_code_lines INT DEFAULT 0;

-- Add comments for documentation
COMMENT ON COLUMN repositories.test_code_lines IS 'Total lines of JUnit test code in the repository';
COMMENT ON COLUMN repositories.test_related_code_lines IS 'Total lines of test-related code (non-JUnit helper classes) in the repository';
