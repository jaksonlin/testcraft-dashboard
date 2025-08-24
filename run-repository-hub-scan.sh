#!/bin/bash

echo "Repository Hub Scanner - Unix/Linux/Mac Shell Script"
echo "==================================================="
echo

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 17 or later and try again"
    exit 1
fi

# Check if the JAR file exists
if [ ! -f "target/annotation-extractor-1.0.0.jar" ]; then
    echo "Error: JAR file not found. Please build the project first:"
    echo "  mvn clean package"
    echo
    exit 1
fi

# Set default values
REPO_HUB_PATH="repositories"
REPO_LIST_FILE="sample-repositories.txt"

# Check if repository list file exists, if not create sample
if [ ! -f "$REPO_LIST_FILE" ]; then
    echo "Creating sample repository list file..."
    cat > "$REPO_LIST_FILE" << 'EOF'
# Sample Repository List
# Add your git repository URLs here, one per line
# Lines starting with # are comments and will be ignored

# Example repositories:
# https://github.com/example/repo1.git
# https://github.com/example/repo2
# git@github.com:example/repo3.git
EOF
    echo
    echo "Sample repository list file created: $REPO_LIST_FILE"
    echo "Please edit this file and add your actual repository URLs before running the scan."
    echo
    exit 0
fi

echo "Repository Hub Path: $REPO_HUB_PATH"
echo "Repository List File: $REPO_LIST_FILE"
echo

# Ask user if they want to proceed
read -p "Do you want to proceed with the scan? (y/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Scan cancelled."
    exit 0
fi

echo
echo "Starting repository hub scan..."
echo

# Run the scanner
java -jar target/annotation-extractor-1.0.0.jar "$REPO_HUB_PATH" "$REPO_LIST_FILE"

echo
echo "Scan completed. Check the output above for results."
