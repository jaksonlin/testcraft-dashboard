#!/bin/bash

echo "Starting TestCraft Dashboard..."
echo "=============================="

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven and try again"
    exit 1
fi

# Clean and compile
echo "Building application..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "Error: Build failed"
    exit 1
fi

# Start the Spring Boot application
echo "Starting Spring Boot application..."
echo "Dashboard will be available at: http://localhost:8080/api"
echo "Health check: http://localhost:8080/api/health"
echo ""
mvn spring-boot:run
