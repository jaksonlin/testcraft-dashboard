-- Migration: Add additional metadata to test_classes table
-- Version: 8
-- Description: Add line numbers, file content, and helper class information

-- Add new columns to test_classes table
ALTER TABLE test_classes 
ADD COLUMN IF NOT EXISTS class_line_number INT,
ADD COLUMN IF NOT EXISTS test_class_content TEXT,
ADD COLUMN IF NOT EXISTS helper_classes_line_numbers TEXT;

-- Add comment for documentation
COMMENT ON COLUMN test_classes.class_line_number IS 'Line number where the test class is defined';
COMMENT ON COLUMN test_classes.test_class_content IS 'Complete source code content of the test class';
COMMENT ON COLUMN test_classes.helper_classes_line_numbers IS 'Comma-separated line numbers of helper classes in the test file';
