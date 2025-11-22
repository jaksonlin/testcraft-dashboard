-- Migration: Add method body content to test_methods table
-- Version: 15
-- Description: Store the actual method body content for efficient code pattern searching

-- Add new column to test_methods table
ALTER TABLE test_methods 
ADD COLUMN IF NOT EXISTS method_body_content TEXT;

-- Add comment for documentation
COMMENT ON COLUMN test_methods.method_body_content IS 'Complete source code content of the test method body (extracted from test class content)';

-- Add index for full-text search performance (optional, can be added later if needed)
-- CREATE INDEX IF NOT EXISTS idx_test_methods_body_content ON test_methods USING gin(to_tsvector('english', method_body_content));

