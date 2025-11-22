#!/bin/bash
# Quick start script for development with hot reload
# Usage: ./dev-start.sh

set -e

echo "🚀 TestCraft Development Environment"
echo "===================================="
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ Please run this script from the project root directory"
    exit 1
fi

echo "📦 Initial compilation..."
mvn clean compile -DskipTests -q
echo "✅ Compilation complete"
echo ""

echo "🐳 Starting Docker services..."
cd deployment
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
cd ..

echo ""
echo "⏳ Waiting for services to be healthy..."
sleep 5

# Wait for backend to be ready
MAX_ATTEMPTS=30
ATTEMPT=0
while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if curl -s http://localhost:8090/api/actuator/health > /dev/null 2>&1; then
        echo "✅ Backend is ready!"
        break
    fi
    ATTEMPT=$((ATTEMPT + 1))
    if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
        echo "⚠️  Backend is taking longer than expected. Check logs with:"
        echo "   docker-compose -f deployment/docker-compose.yml -f deployment/docker-compose.dev.yml logs -f backend"
        break
    fi
    echo -n "."
    sleep 2
done

echo ""
echo "✨ Development environment is ready!"
echo ""
echo "📍 Services:"
echo "   Backend:    http://localhost:8090/api"
echo "   Frontend:   http://localhost:5173"
echo "   Database:   localhost:5432"
echo "   pgAdmin:    http://localhost:5050"
echo "   Debug Port: 5005"
echo ""
echo "🔥 Hot Reload:"
echo "   Option 1 (Recommended): Run './dev-watch.sh' in another terminal for auto-compilation"
echo "   Option 2: Manually run 'mvn compile' after making changes"
echo ""
echo "📊 Useful commands:"
echo "   View logs:    docker-compose -f deployment/docker-compose.yml -f deployment/docker-compose.dev.yml logs -f backend"
echo "   Stop:         docker-compose -f deployment/docker-compose.yml -f deployment/docker-compose.dev.yml down"
echo "   Restart:      docker-compose -f deployment/docker-compose.yml -f deployment/docker-compose.dev.yml restart backend"
echo ""
echo "📖 Full guide: deployment/DEV-HOTRELOAD.md"
echo ""
echo "Happy coding! 🎉"

