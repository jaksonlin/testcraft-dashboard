@echo off
echo Temporary Clone Runner
echo =====================
echo.

if "%1"=="" (
    echo Usage: run-temp-clone.bat <repository_hub_path> <repository_list_file> [options]
    echo.
    echo Options:
    echo   --db-host <host>     Database host (default: localhost)
    echo   --db-port <port>     Database port (default: 5432)
    echo   --db-name <name>     Database name (default: test_analytics)
    echo   --db-user <user>     Database username (default: postgres)
    echo   --db-pass <pass>     Database password (default: postgres)
    echo.
    echo Examples:
    echo   run-temp-clone.bat ./temp-repos ./repo-list.txt
    echo   run-temp-clone.bat ./temp-repos ./repo-list.txt --db-host localhost --db-name test_db
    echo.
    echo Note: This mode clones repositories one by one, scans each, and deletes it immediately
    echo       to save disk space. Useful for large repositories or limited disk space scenarios.
    pause
    exit /b 1
)

if "%2"=="" (
    echo Error: Missing repository list file
    echo Usage: run-temp-clone.bat <repository_hub_path> <repository_list_file> [options]
    pause
    exit /b 1
)

echo Starting temporary clone operation...
echo Repository Hub: %1
echo Repository List: %2
echo.

echo Building project...
call mvn compile -q
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Running temporary clone operation...
java -cp target/classes com.example.annotationextractor.TempCloneRunner %*

echo.
echo Operation completed.
pause
