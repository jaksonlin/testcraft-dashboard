-- Migration: add referenced_types column to test_classes
-- Version: 13
-- Adds storage for serialized dependency type references captured during parsing

ALTER TABLE test_classes
ADD COLUMN IF NOT EXISTS imported_types TEXT,
ADD COLUMN IF NOT EXISTS referenced_types TEXT;

COMMENT ON COLUMN test_classes.imported_types IS 'Newline-delimited list of imported types declared in the test class file';
COMMENT ON COLUMN test_classes.referenced_types IS 'Newline-delimited list of fully qualified types referenced by the test class';

