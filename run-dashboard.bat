@echo off
echo Starting TestCraft Dashboard...
echo ==============================

REM Check if Maven is available
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Maven and try again
    pause
    exit /b 1
)

REM Clean and compile
echo Building application...
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo Error: Build failed
    pause
    exit /b 1
)

REM Start the Spring Boot application
echo Starting Spring Boot application...
echo Dashboard will be available at: http://localhost:8080/api
echo Health check: http://localhost:8080/api/health
echo.
call mvn spring-boot:run

pause
