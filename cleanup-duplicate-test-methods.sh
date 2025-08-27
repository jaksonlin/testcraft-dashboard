#!/bin/bash

echo "Cleaning up duplicate test methods..."
echo

# Build the project first
echo "Building project..."
mvn clean package -q
if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo
echo "Running test methods cleanup..."
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.database.DatabaseMigrationRunner localhost 5432 test_analytics postgres

echo
echo "Cleanup completed!"
