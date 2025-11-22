-- Migration: Add class LOC to test_classes table
-- Version: 12
-- Description: Add lines of code for test class files

-- Add new column to test_classes table
ALTER TABLE test_classes 
ADD COLUMN IF NOT EXISTS class_loc INT DEFAULT 0;

-- Add comment for documentation
COMMENT ON COLUMN test_classes.class_loc IS 'Lines of code in the test class file (total file lines)';

