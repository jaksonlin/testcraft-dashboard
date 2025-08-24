@echo off
echo Repository Hub Scanner - Windows Batch File
echo ===========================================
echo.

REM Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 17 or later and try again
    pause
    exit /b 1
)

REM Check if the JAR file exists
if not exist "target\annotation-extractor-1.0.0.jar" (
    echo Error: JAR file not found. Please build the project first:
    echo   mvn clean package
    echo.
    pause
    exit /b 1
)

REM Set default values
set REPO_HUB_PATH=repositories
set REPO_LIST_FILE=sample-repositories.txt

REM Check if repository list file exists, if not create sample
if not exist "%REPO_LIST_FILE%" (
    echo Creating sample repository list file...
    echo # Sample Repository List > "%REPO_LIST_FILE%"
    echo # Add your git repository URLs here, one per line >> "%REPO_LIST_FILE%"
    echo # Lines starting with # are comments and will be ignored >> "%REPO_LIST_FILE%"
    echo. >> "%REPO_LIST_FILE%"
    echo # Example repositories: >> "%REPO_LIST_FILE%"
    echo # https://github.com/example/repo1.git >> "%REPO_LIST_FILE%"
    echo # https://github.com/example/repo2 >> "%REPO_LIST_FILE%"
    echo # git@github.com:example/repo3.git >> "%REPO_LIST_FILE%"
    echo.
    echo Sample repository list file created: %REPO_LIST_FILE%
    echo Please edit this file and add your actual repository URLs before running the scan.
    echo.
    pause
    exit /b 0
)

echo Repository Hub Path: %REPO_HUB_PATH%
echo Repository List File: %REPO_LIST_FILE%
echo.

REM Ask user if they want to proceed
set /p CONTINUE="Do you want to proceed with the scan? (y/n): "
if /i not "%CONTINUE%"=="y" (
    echo Scan cancelled.
    pause
    exit /b 0
)

echo.
echo Starting repository hub scan...
echo.

REM Run the scanner
java -jar target\annotation-extractor-1.0.0.jar "%REPO_HUB_PATH%" "%REPO_LIST_FILE%"

echo.
echo Scan completed. Check the output above for results.
pause
