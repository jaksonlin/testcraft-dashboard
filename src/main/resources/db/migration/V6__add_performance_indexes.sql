-- ============================================================================
-- Migration V6: Add Performance Indexes for Test Methods at Scale
-- ============================================================================
-- Purpose: Optimize queries for 200,000+ test methods with hierarchical filtering
-- Created: October 19, 2025
-- Impact: Reduces query time from seconds to milliseconds
-- ============================================================================

-- Index for scan session filtering (used in every query)
CREATE INDEX IF NOT EXISTS idx_test_classes_scan_session 
ON test_classes(scan_session_id);

-- Index for team filtering via repositories
CREATE INDEX IF NOT EXISTS idx_repositories_team 
ON repositories(team_id);

-- Index for repository name filtering
CREATE INDEX IF NOT EXISTS idx_repositories_name 
ON repositories(repository_name);

-- Index for class name filtering (supports LIKE queries)
CREATE INDEX IF NOT EXISTS idx_test_classes_name 
ON test_classes(class_name);

-- Index for package name filtering (critical for package-level filtering)
CREATE INDEX IF NOT EXISTS idx_test_classes_package 
ON test_classes(package_name);

-- Index for class repository lookup
CREATE INDEX IF NOT EXISTS idx_test_classes_repository 
ON test_classes(repository_id);

-- Index for test method class lookup
CREATE INDEX IF NOT EXISTS idx_test_methods_class 
ON test_methods(test_class_id);

-- Index for annotation status filtering
CREATE INDEX IF NOT EXISTS idx_test_methods_annotation 
ON test_methods(annotation_title)
WHERE annotation_title IS NOT NULL AND annotation_title != '';

-- Composite index for team + repository filtering (most common combination)
CREATE INDEX IF NOT EXISTS idx_test_classes_scan_repo 
ON test_classes(scan_session_id, repository_id);

-- Composite index for efficient package extraction
-- Supports queries like: WHERE class_name LIKE 'com.acme.tests.%'
CREATE INDEX IF NOT EXISTS idx_test_classes_name_pattern 
ON test_classes(class_name varchar_pattern_ops);

-- Index for team name lookups
CREATE INDEX IF NOT EXISTS idx_teams_name 
ON teams(team_name);

-- Index for team code lookups (for organization derivation)
CREATE INDEX IF NOT EXISTS idx_teams_code 
ON teams(team_code);

-- ============================================================================
-- Performance Impact Estimates
-- ============================================================================
-- Without indexes:
--   - Team filter query: 2-5 seconds for 200k methods
--   - Package filter query: 3-8 seconds
--   - Class filter query: 1-3 seconds
--   - Combined filters: 5-15 seconds (may timeout)
--
-- With indexes:
--   - Team filter query: <100ms
--   - Package filter query: <200ms
--   - Class filter query: <100ms
--   - Combined filters: <300ms
--
-- Expected improvement: 10-50x faster queries
-- ============================================================================

-- Analyze tables to update statistics for query planner
ANALYZE test_methods;
ANALYZE test_classes;
ANALYZE repositories;
ANALYZE teams;

