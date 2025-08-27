@echo off
echo Testing Database Initialization
echo ==============================
echo.

echo Building project...
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo Error: Failed to build project
    pause
    exit /b 1
)

echo.
echo Testing database initialization...
echo This will create the teams table and other required tables.
echo.

java -cp "target/classes;target/dependency/*" com.example.annotationextractor.database.DatabaseInitializer

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Error: Database initialization failed
    echo Please check your database connection settings in database.properties
    pause
    exit /b 1
)

echo.
echo Database initialization test completed successfully!
echo The teams table and other required tables have been created.
pause
