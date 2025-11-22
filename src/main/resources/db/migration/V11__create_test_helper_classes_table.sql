-- Migration: Create test helper classes table
-- Version: 11
-- Description: Track non-test classes in test directories (helper classes, fixtures, etc.)

-- Create table for test helper/related classes
CREATE TABLE IF NOT EXISTS test_helper_classes (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT NOT NULL,
    class_name VARCHAR(255) NOT NULL,
    package_name VARCHAR(500),
    file_path VARCHAR(500) NOT NULL,
    class_line_number INT,
    helper_class_content TEXT,
    loc INT DEFAULT 0,
    scan_session_id BIGINT NOT NULL,
    first_seen_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_helper_classes_repo FOREIGN KEY (repository_id) REFERENCES repositories(id) ON DELETE CASCADE,
    CONSTRAINT fk_helper_classes_scan FOREIGN KEY (scan_session_id) REFERENCES scan_sessions(id)
);

-- Add comment for documentation
COMMENT ON COLUMN test_helper_classes.loc IS 'Lines of code in the helper class file';
COMMENT ON COLUMN test_helper_classes.helper_class_content IS 'Complete source code content of the helper class';

-- Index for performance
CREATE INDEX IF NOT EXISTS idx_helper_classes_repo ON test_helper_classes(repository_id);
CREATE INDEX IF NOT EXISTS idx_helper_classes_session ON test_helper_classes(scan_session_id);
CREATE INDEX IF NOT EXISTS idx_helper_classes_path ON test_helper_classes(file_path);
