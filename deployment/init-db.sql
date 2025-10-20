-- Initialize database script
-- This will be executed when PostgreSQL container starts for the first time

-- Ensure the database exists (already created by POSTGRES_DB env var)
\c test_analytics_v2;

-- Grant necessary privileges
GRANT ALL PRIVILEGES ON DATABASE test_analytics_v2 TO postgres;

-- Note: Schema migrations will be handled by Flyway when backend starts
-- This file is just for initial database setup and custom configurations

-- Set timezone
SET timezone = 'UTC';

-- Enable extensions if needed
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Log successful initialization
SELECT 'Database initialized successfully' AS status;

