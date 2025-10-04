@echo off
echo Testing PostgreSQL Database Connection...
echo ========================================

REM Test database connection using your existing database configuration
java -cp "target/classes;target/dependency/*" com.example.annotationextractor.database.DatabaseConnectionTester

pause
