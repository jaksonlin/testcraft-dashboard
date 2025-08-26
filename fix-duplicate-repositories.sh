#!/bin/bash

echo "Database Migration Tool - Fix Duplicate Repositories"
echo "=================================================="
echo

# Check if JAR file exists
if [ ! -f "target/annotation-extractor-1.0.0.jar" ]; then
    echo "Error: JAR file not found. Please build the project first:"
    echo "  mvn clean package"
    echo
    exit 1
fi

echo "Running database migration to fix duplicate repositories..."
echo

# Run the migration
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.database.DatabaseMigrationRunner localhost 5432 test_analytics postgres

echo
echo "Migration completed. Check the output above for details."
