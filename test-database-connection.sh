#!/bin/bash

echo "Database Connection Tester"
echo "========================="
echo

echo "Testing default connection (from database.properties)..."
java -cp target/classes com.example.annotationextractor.database.DatabaseConnectionTester
echo

echo "Testing custom connection..."
java -cp target/classes com.example.annotationextractor.database.DatabaseConnectionTester --host localhost --port 5432 --db test_analytics --user test_user --pass 123456
echo

echo "Testing with different database name..."
java -cp target/classes com.example.annotationextractor.database.DatabaseConnectionTester --db production_db
echo

echo "Testing with different host and port..."
java -cp target/classes com.example.annotationextractor.database.DatabaseConnectionTester --host 192.168.1.100 --port 5433
echo

echo "Press Enter to continue..."
read
