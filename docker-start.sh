#!/bin/bash

# TestCraft Dashboard - Docker Quick Start Script
# This is a convenience wrapper that works with the deployment directory structure

echo "╔════════════════════════════════════════════════════════════╗"
echo "║       TestCraft Dashboard - Docker Quick Start             ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Error: Docker is not installed"
    echo "Please install Docker from https://docs.docker.com/get-docker/"
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ Error: Docker Compose is not installed"
    echo "Please install Docker Compose from https://docs.docker.com/compose/install/"
    exit 1
fi

# Check if .env file exists, if not create from example
if [ ! -f .env ]; then
    echo "📝 Creating .env file from deployment/env.example..."
    if [ -f deployment/env.example ]; then
        cp deployment/env.example .env
        echo "✅ Created .env file. You may want to customize it."
    else
        echo "⚠️  Warning: deployment/env.example not found. Using default values."
    fi
    echo ""
fi

# Function to check if port is available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 1
    else
        return 0
    fi
}

# Check if ports are available
echo "🔍 Checking if required ports are available..."
if ! check_port 80; then
    echo "⚠️  Warning: Port 80 is already in use"
    echo "   You can change the port in .env file (NGINX_PORT=8080)"
    read -p "   Do you want to continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

if ! check_port 5432; then
    echo "⚠️  Warning: Port 5432 is already in use"
    echo "   The PostgreSQL container might conflict with an existing database"
    read -p "   Do you want to continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo "✅ Port check completed"
echo ""

# Parse command line arguments
BUILD_FLAG=""
if [ "$1" == "--build" ] || [ "$1" == "-b" ]; then
    BUILD_FLAG="--build"
    echo "🔨 Building containers from scratch..."
elif [ "$1" == "--rebuild" ]; then
    BUILD_FLAG="--build --no-cache"
    echo "🔨 Rebuilding containers without cache..."
else
    echo "🚀 Starting containers..."
fi

echo ""

# Start Docker Compose (uses docker-compose.yml at project root)
if [ -n "$BUILD_FLAG" ]; then
    docker-compose up -d $BUILD_FLAG
else
    docker-compose up -d
fi

# Check if startup was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║             🎉 Successfully Started!                       ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""
    echo "📍 Application URLs:"
    echo "   Main Application: http://localhost"
    echo "   API Endpoints:    http://localhost/api"
    echo "   Health Check:     http://localhost/health"
    echo ""
    echo "🔧 Useful Commands:"
    echo "   View logs:        docker-compose logs -f"
    echo "   Stop services:    docker-compose stop"
    echo "   Restart services: docker-compose restart"
    echo "   Remove all:       docker-compose down"
    echo ""
    echo "📊 Checking service status..."
    echo ""
    docker-compose ps
    echo ""
    echo "⏳ Services are starting up. This may take a minute..."
    echo "   You can monitor the startup with: docker-compose logs -f"
    echo ""
    echo "📚 Documentation available in: deployment/"
else
    echo ""
    echo "❌ Failed to start containers"
    echo "   Check the logs with: docker-compose logs"
    exit 1
fi

