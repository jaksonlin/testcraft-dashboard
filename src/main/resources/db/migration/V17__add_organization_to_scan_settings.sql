-- Migration: Add organization to scan_settings for instance-wide org setting
-- Version: 17

ALTER TABLE scan_settings
    ADD COLUMN IF NOT EXISTS organization TEXT NOT NULL DEFAULT '';

-- No destructive changes; keep default empty string for existing instances.


