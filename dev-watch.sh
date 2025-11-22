#!/bin/bash
# Auto-compile Java files on change for hot reload in Docker
# Usage: ./dev-watch.sh

echo "🔥 Hot Reload Watcher"
echo "===================="
echo "Watching for Java file changes and auto-compiling..."
echo "Spring DevTools will restart the app automatically when classes change."
echo ""
echo "Press Ctrl+C to stop"
echo ""

# Function to compile on change
compile_on_change() {
    echo "⚡ Change detected, compiling..."
    mvn compile -DskipTests -q
    if [ $? -eq 0 ]; then
        echo "✅ Compilation successful at $(date '+%H:%M:%S')"
    else
        echo "❌ Compilation failed at $(date '+%H:%M:%S')"
    fi
}

# Initial compile
echo "📦 Initial compilation..."
mvn compile -DskipTests
echo ""

# Check if inotifywait is available (Linux)
if command -v inotifywait &> /dev/null; then
    echo "Using inotifywait for file watching..."
    while true; do
        inotifywait -r -e modify,create,delete src/main/java src/main/resources 2>/dev/null
        compile_on_change
    done

# Check if fswatch is available (macOS)
elif command -v fswatch &> /dev/null; then
    echo "Using fswatch for file watching..."
    fswatch -o src/main/java src/main/resources | while read change; do
        compile_on_change
    done

# Fallback to polling (works everywhere but slower)
else
    echo "⚠️  No file watcher found (inotifywait/fswatch)"
    echo "Install for better performance:"
    echo "  Linux: sudo apt-get install inotify-tools"
    echo "  macOS: brew install fswatch"
    echo ""
    echo "Using polling mode (checks every 2 seconds)..."
    
    LAST_HASH=""
    while true; do
        sleep 2
        # Calculate hash of all Java files
        CURRENT_HASH=$(find src/main/java src/main/resources -type f \( -name "*.java" -o -name "*.properties" -o -name "*.yml" \) -exec md5sum {} \; 2>/dev/null | md5sum)
        
        if [ "$CURRENT_HASH" != "$LAST_HASH" ] && [ -n "$LAST_HASH" ]; then
            compile_on_change
        fi
        LAST_HASH=$CURRENT_HASH
    done
fi

