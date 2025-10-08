-- Migration: Refactor test case ID structure
-- Version: 3
-- Description: Add internal auto-generated ID and separate external test case ID
-- Reason: External test case IDs from different systems can conflict. We need internal IDs for proper data integrity.

-- Step 1: Drop foreign key constraint from coverage table
ALTER TABLE test_case_coverage DROP CONSTRAINT IF EXISTS test_case_coverage_test_case_id_fkey;

-- Step 2: Add new internal ID column to test_cases (will become primary key)
ALTER TABLE test_cases ADD COLUMN IF NOT EXISTS internal_id SERIAL;

-- Step 3: Rename old id column to external_id (test case ID from external system)
ALTER TABLE test_cases RENAME COLUMN id TO external_id;

-- Step 4: Drop old primary key constraint
ALTER TABLE test_cases DROP CONSTRAINT IF EXISTS test_cases_pkey;

-- Step 5: Set internal_id as new primary key
ALTER TABLE test_cases ADD PRIMARY KEY (internal_id);

-- Step 6: Add unique constraint on external_id + organization (same external ID can exist in different orgs)
ALTER TABLE test_cases ADD CONSTRAINT test_cases_external_id_org_unique UNIQUE (external_id, organization);

-- Step 7: Add index on external_id for lookups
CREATE INDEX IF NOT EXISTS idx_test_cases_external_id ON test_cases(external_id);

-- Step 8: Update coverage table - rename column and change type
ALTER TABLE test_case_coverage RENAME COLUMN test_case_id TO test_case_internal_id;
ALTER TABLE test_case_coverage ALTER COLUMN test_case_internal_id TYPE INTEGER USING test_case_internal_id::integer;

-- Step 9: Add foreign key constraint back with internal ID
ALTER TABLE test_case_coverage 
    ADD CONSTRAINT test_case_coverage_test_case_internal_id_fkey 
    FOREIGN KEY (test_case_internal_id) 
    REFERENCES test_cases(internal_id) 
    ON DELETE CASCADE;

-- Step 10: Update indexes on coverage table
DROP INDEX IF EXISTS idx_coverage_test_case;
CREATE INDEX IF NOT EXISTS idx_coverage_test_case_internal ON test_case_coverage(test_case_internal_id);

-- Step 11: Update unique constraint on coverage table
ALTER TABLE test_case_coverage DROP CONSTRAINT IF EXISTS test_case_coverage_test_case_id_repository_name_package_name_cl_key;
ALTER TABLE test_case_coverage 
    ADD CONSTRAINT test_case_coverage_unique_link 
    UNIQUE(test_case_internal_id, repository_name, package_name, class_name, method_name);

-- Update comments
COMMENT ON COLUMN test_cases.internal_id IS 'Internal auto-generated primary key';
COMMENT ON COLUMN test_cases.external_id IS 'Test case ID from external test management system (e.g., TC-1234, ID-5678)';
COMMENT ON COLUMN test_case_coverage.test_case_internal_id IS 'Reference to internal test case ID';

-- Add comment explaining the change
COMMENT ON TABLE test_cases IS 'Stores test case definitions imported from Excel or other sources. Uses internal_id as PK and external_id to store the original test case ID from source system.';

