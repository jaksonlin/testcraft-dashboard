-- Migration: Add team support to test cases
-- Version: 4
-- Description: Add team_id column to test_cases table for team-based organization

-- Add team_id column to test_cases table
ALTER TABLE test_cases ADD COLUMN IF NOT EXISTS team_id BIGINT;

-- Add foreign key constraint to teams table
ALTER TABLE test_cases ADD CONSTRAINT fk_test_cases_team 
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL;

-- Add index for team_id for better query performance
CREATE INDEX IF NOT EXISTS idx_test_cases_team ON test_cases(team_id);

-- Add comment for documentation
COMMENT ON COLUMN test_cases.team_id IS 'Foreign key to teams table - identifies which team owns this test case';

