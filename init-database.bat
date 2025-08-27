@echo off
echo Database Schema Initializer
echo ============================
echo.

echo Building project...
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo Error: Failed to build project
    pause
    exit /b 1
)

echo.
echo Initializing database schema...
java -cp target/classes com.example.annotationextractor.database.DatabaseInitializer

if %ERRORLEVEL% NEQ 0 (
    echo Error: Failed to initialize database
    pause
    exit /b 1
)

echo.
echo Database initialization completed successfully!
pause
