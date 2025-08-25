#!/bin/bash

echo "Temporary Clone Runner"
echo "====================="
echo

if [ $# -lt 2 ]; then
    echo "Usage: ./run-temp-clone.sh <repository_hub_path> <repository_list_file> [options]"
    echo
    echo "Options:"
    echo "  --db-host <host>     Database host (default: localhost)"
    echo "  --db-port <port>     Database port (default: 5432)"
    echo "  --db-name <name>     Database name (default: test_analytics)"
    echo "  --db-user <user>     Database username (default: postgres)"
    echo "  --db-pass <pass>     Database password (default: postgres)"
    echo
    echo "Examples:"
    echo "  ./run-temp-clone.sh ./temp-repos ./repo-list.txt"
    echo "  ./run-temp-clone.sh ./temp-repos ./repo-list.txt --db-host localhost --db-name test_db"
    echo
    echo "Note: This mode clones repositories one by one, scans each, and deletes it immediately"
    echo "      to save disk space. Useful for large repositories or limited disk space scenarios."
    exit 1
fi

REPO_HUB_PATH="$1"
REPO_LIST_FILE="$2"
shift 2

echo "Starting temporary clone operation..."
echo "Repository Hub: $REPO_HUB_PATH"
echo "Repository List: $REPO_LIST_FILE"
echo

echo "Building project..."
mvn compile -q
if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo
echo "Running temporary clone operation..."
java -cp target/classes com.example.annotationextractor.TempCloneRunner "$REPO_HUB_PATH" "$REPO_LIST_FILE" "$@"

echo
echo "Operation completed."
