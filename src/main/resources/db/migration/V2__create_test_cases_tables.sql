-- Migration: Create test cases and import templates tables
-- Version: 2
-- Description: Support test case upload and flexible column mapping

-- Test Cases table
CREATE TABLE IF NOT EXISTS test_cases (
    id VARCHAR(100) PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    steps TEXT NOT NULL,
    setup TEXT,
    teardown TEXT,
    expected_result TEXT,
    priority VARCHAR(20),
    type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'Active',
    tags TEXT[],
    requirements TEXT[],
    custom_fields JSONB,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    organization VARCHAR(100)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_test_cases_org ON test_cases(organization);
CREATE INDEX IF NOT EXISTS idx_test_cases_type ON test_cases(type);
CREATE INDEX IF NOT EXISTS idx_test_cases_priority ON test_cases(priority);
CREATE INDEX IF NOT EXISTS idx_test_cases_status ON test_cases(status);
CREATE INDEX IF NOT EXISTS idx_test_cases_created ON test_cases(created_date);

-- GIN index for array columns
CREATE INDEX IF NOT EXISTS idx_test_cases_tags ON test_cases USING GIN(tags);
CREATE INDEX IF NOT EXISTS idx_test_cases_requirements ON test_cases USING GIN(requirements);

-- GIN index for JSONB custom fields
CREATE INDEX IF NOT EXISTS idx_test_cases_custom_fields ON test_cases USING GIN(custom_fields);

-- Test Case to Test Method linkage table
CREATE TABLE IF NOT EXISTS test_case_coverage (
    id SERIAL PRIMARY KEY,
    test_case_id VARCHAR(100) NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    repository_name VARCHAR(200) NOT NULL,
    package_name VARCHAR(500) NOT NULL,
    class_name VARCHAR(200) NOT NULL,
    method_name VARCHAR(200) NOT NULL,
    file_path TEXT NOT NULL,
    line_number INTEGER,
    scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(test_case_id, repository_name, package_name, class_name, method_name)
);

-- Indexes for coverage lookups
CREATE INDEX IF NOT EXISTS idx_coverage_test_case ON test_case_coverage(test_case_id);
CREATE INDEX IF NOT EXISTS idx_coverage_repository ON test_case_coverage(repository_name);
CREATE INDEX IF NOT EXISTS idx_coverage_class ON test_case_coverage(class_name);
CREATE INDEX IF NOT EXISTS idx_coverage_method ON test_case_coverage(method_name);

-- Import Templates table
CREATE TABLE IF NOT EXISTS test_case_import_templates (
    id SERIAL PRIMARY KEY,
    template_name VARCHAR(200) NOT NULL,
    organization VARCHAR(100),
    column_mappings JSONB NOT NULL,
    data_start_row INTEGER NOT NULL DEFAULT 2,
    sheet_name VARCHAR(100),
    description TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_date TIMESTAMP,
    usage_count INTEGER DEFAULT 0,
    UNIQUE(template_name, organization)
);

-- Index for template lookups
CREATE INDEX IF NOT EXISTS idx_templates_org ON test_case_import_templates(organization);
CREATE INDEX IF NOT EXISTS idx_templates_last_used ON test_case_import_templates(last_used_date);

-- Function to update updated_date automatically
CREATE OR REPLACE FUNCTION update_test_case_updated_date()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_date = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to auto-update updated_date
CREATE TRIGGER trigger_update_test_case_updated_date
    BEFORE UPDATE ON test_cases
    FOR EACH ROW
    EXECUTE FUNCTION update_test_case_updated_date();

-- Comments for documentation
COMMENT ON TABLE test_cases IS 'Stores test case definitions imported from Excel or other sources';
COMMENT ON TABLE test_case_coverage IS 'Links test cases to actual test method implementations';
COMMENT ON TABLE test_case_import_templates IS 'Stores Excel column mapping templates for reuse';

COMMENT ON COLUMN test_cases.id IS 'Unique test case identifier (e.g., TC-1234, ID-5678)';
COMMENT ON COLUMN test_cases.custom_fields IS 'Stores additional organization-specific fields as JSON';
COMMENT ON COLUMN test_case_import_templates.column_mappings IS 'JSON mapping of Excel columns to system fields';
COMMENT ON COLUMN test_case_import_templates.data_start_row IS 'Row number where data starts (after headers)';

