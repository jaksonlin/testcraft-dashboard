-- Migration: Add scan_branch to scan_settings to control git clone branch
-- Version: 18

ALTER TABLE scan_settings
    ADD COLUMN IF NOT EXISTS scan_branch TEXT NOT NULL DEFAULT 'main';

UPDATE scan_settings
SET scan_branch = 'main'
WHERE scan_branch IS NULL OR trim(scan_branch) = '';


