@echo off
echo Testing Phase 1 Performance Optimizations
echo ========================================
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

echo Phase 1 Optimizations Implemented:
echo ✅ Batch Database Operations (1000 records per batch)
echo ✅ Connection Pool Scaling (30 max connections)
echo ✅ Streaming Excel Generation (SXSSF)
echo ✅ Performance Database Indexes
echo ✅ Performance Monitoring
echo.

echo Testing with small dataset first...
echo.

REM Create a small test repository list
echo # Small test dataset for performance testing > test-performance-repos.txt
echo # This will test the optimizations without overwhelming the system >> test-performance-repos.txt
echo https://github.com/junit-team/junit4.git >> test-performance-repos.txt
echo https://github.com/junit-team/junit5.git >> test-performance-repos.txt
echo https://github.com/mockito/mockito.git >> test-performance-repos.txt

echo Test repository list created: test-performance-repos.txt
echo.

echo Starting performance test...
echo.

REM Run the scanner with performance monitoring
java -jar target\annotation-extractor-1.0.0.jar test-performance-repos test-performance-repos.txt --temp-clone

echo.
echo Performance test completed!
echo.
echo Check the output above for:
echo 📊 Performance counters
echo ⏱️ Timing summary  
echo 🚀 Operation start/end times
echo 💾 Memory usage tracking
echo.
echo If the test is successful, you can run with your full 146 repositories:
echo   java -jar target\annotation-extractor-1.0.0.jar ./repos ./your-repo-list.txt --temp-clone
echo.
pause
