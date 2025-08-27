@echo off
echo Cleaning up duplicate test methods...
echo.

REM Build the project first
echo Building project...
call mvn clean package -q
if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Running test methods cleanup...
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.database.DatabaseMigrationRunner localhost 5432 test_analytics postgres

echo.
echo Cleanup completed!
pause
