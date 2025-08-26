@echo off
echo Database Migration Tool - Fix Duplicate Repositories
echo ==================================================
echo.

REM Check if JAR file exists
if not exist "target\annotation-extractor-1.0.0.jar" (
    echo Error: JAR file not found. Please build the project first:
    echo   mvn clean package
    echo.
    pause
    exit /b 1
)

echo Running database migration to fix duplicate repositories...
echo.

REM Run the migration
java -cp target\annotation-extractor-1.0.0.jar com.example.annotationextractor.database.DatabaseMigrationRunner localhost 5432 test_analytics postgres

echo.
echo Migration completed. Check the output above for details.
pause
