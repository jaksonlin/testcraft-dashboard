@echo off
echo Testing PostgreSQL Connection...
echo ===============================

REM Use psql if available
where psql >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Using psql to test connection...
    psql -h localhost -p 5432 -U test_user -d test_analytics -c "SELECT version();"
) else (
    echo psql not found, trying alternative connection test...
    echo Connection details:
    echo   Host: localhost
    echo   Port: 5432
    echo   Database: test_analytics
    echo   User: test_user
    echo.
    echo Please check if PostgreSQL is running and accessible.
)

pause
