#!/bin/bash

echo "Database Schema Initializer"
echo "============================"
echo

echo "Building project..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Error: Failed to build project"
    exit 1
fi

echo
echo "Initializing database schema..."
java -cp target/classes com.example.annotationextractor.database.DatabaseInitializer

if [ $? -ne 0 ]; then
    echo "Error: Failed to initialize database"
    exit 1
fi

echo
echo "Database initialization completed successfully!"
