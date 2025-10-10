-- Migration: Add team_name column to test_cases
-- Version: 5
-- Description: Add denormalized team_name column to preserve team names when team doesn't exist in teams table

-- Add team_name column (denormalized for cases where team doesn't exist in teams table yet)
ALTER TABLE test_cases ADD COLUMN IF NOT EXISTS team_name VARCHAR(255);

-- Add comment for documentation
COMMENT ON COLUMN test_cases.team_name IS 'Team name (denormalized) - useful when team_id is not set or for display purposes';

-- Update existing test cases to populate team_name from teams table where team_id exists
UPDATE test_cases tc
SET team_name = t.team_name
FROM teams t
WHERE tc.team_id = t.id AND tc.team_name IS NULL;

