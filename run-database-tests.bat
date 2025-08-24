@echo off
echo Running Database Layer Tests (Sequential Execution)...
echo.
echo Note: Tests run sequentially to avoid conflicts with static datasource
echo.

echo Cleaning previous test results...
if exist target\surefire-reports rmdir /s /q target\surefire-reports

echo.
echo Running Database Test Suite (all tests sequentially)...
mvn test -Dtest=DatabaseTestSuite -q

echo.
echo All database tests completed!
echo Check target\surefire-reports for detailed results.
echo.
echo Test execution was sequential to ensure proper datasource isolation.
pause
