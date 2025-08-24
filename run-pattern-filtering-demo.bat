@echo off
echo ========================================
echo Path Pattern Filtering Demo
echo ========================================
echo.
echo This script demonstrates how to use path pattern filtering
echo to scan only specific subdirectories while excluding others.
echo.

echo Building the project...
call mvn clean package -q
if %ERRORLEVEL% neq 0 (
    echo Build failed! Please check the errors above.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Demo 1: Scan with Include Patterns
echo ========================================
echo Scanning only finance-related view and DAO projects...
echo.
java -jar target/annotation-extractor-1.0.0.jar "D:\dev-docker-images\annotation-extractor\annotation-extractor" --include "**/repository_group_finance_*/sub_project_for_view" --include "**/repository_group_finance_*/sub_project_for_dao"

echo.
echo ========================================
echo Demo 2: Scan with Exclude Patterns
echo ========================================
echo Scanning everything except expired projects...
echo.
java -jar target/annotation-extractor-1.0.0.jar "D:\dev-docker-images\annotation-extractor\annotation-extractor" --exclude "**/expired_project*" --exclude "**/legacy_*"

echo.
echo ========================================
echo Demo 3: Combined Include/Exclude
echo ========================================
echo Scanning only finance projects but excluding expired ones...
echo.
java -jar target/annotation-extractor-1.0.0.jar "D:\dev-docker-images\annotation-extractor\annotation-extractor" --include "**/repository_group_finance_*/sub_project_for_view" --include "**/repository_group_finance_*/sub_project_for_dao" --exclude "**/expired_project*"

echo.
echo ========================================
echo Demo 4: Show Help
echo ========================================
echo.
java -jar target/annotation-extractor-1.0.0.jar

echo.
echo ========================================
echo Pattern Examples
echo ========================================
echo.
echo Include Patterns:
echo   --include "**/repository_group_finance_*/sub_project_for_view"
echo   --include "**/repository_group_finance_*/sub_project_for_dao"
echo   --include "**/active_project*"
echo.
echo Exclude Patterns:
echo   --exclude "**/expired_project*"
echo   --exclude "**/legacy_*"
echo   --exclude "**/deprecated_*"
echo.
echo Pattern Syntax:
echo   *     - matches any sequence except path separators
echo   **    - matches any sequence including path separators
echo   ?     - matches any single character except path separators
echo.
echo Examples for your use case:
echo   d:\abc\my_repository_hub\repository_group_finance_*\sub_project_for_view
echo   d:\abc\my_repository_hub\repository_group_finance_*\sub_project_for_dao
echo.
echo Command format:
echo   java -jar target/annotation-extractor-1.0.0.jar "d:\abc\my_repository_hub" --include "**/repository_group_finance_*/sub_project_for_view" --include "**/repository_group_finance_*/sub_project_for_dao"
echo.
pause
