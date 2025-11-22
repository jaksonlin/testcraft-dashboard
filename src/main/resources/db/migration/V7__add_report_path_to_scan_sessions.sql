-- V7: Add report_file_path column to scan_sessions table
-- This column stores the absolute path of the Excel report generated for each scan

ALTER TABLE scan_sessions ADD COLUMN IF NOT EXISTS report_file_path VARCHAR(1000);

-- Add index for faster lookups
CREATE INDEX IF NOT EXISTS idx_scan_sessions_report_path ON scan_sessions(report_file_path);
