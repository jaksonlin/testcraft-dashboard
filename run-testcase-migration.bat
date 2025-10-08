@echo off
echo Running Test Case Tables Migration...
echo.

REM Run the SQL migration directly
psql -U postgres -d test_analytics -f src\main\resources\db\migration\V2__create_test_cases_tables.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================
    echo Migration completed successfully!
    echo ===================================
    echo.
    echo Tables created:
    echo   - test_cases
    echo   - test_case_coverage
    echo   - test_case_import_templates
    echo.
    echo You can now restart the application.
) else (
    echo.
    echo ===================================
    echo Migration failed!
    echo ===================================
    echo.
    echo Please check:
    echo   1. PostgreSQL is running
    echo   2. Database 'test_analytics' exists
    echo   3. User 'postgres' has access
)

pause

