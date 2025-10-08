@echo off
echo Checking Flyway Migration Status...
echo.

psql -U postgres -d test_analytics -c "SELECT version, description, installed_on, success FROM flyway_schema_history ORDER BY installed_rank;"

echo.
echo Checking if test_cases table exists...
psql -U postgres -d test_analytics -c "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN ('test_cases', 'test_case_coverage', 'test_case_import_templates');"

pause

