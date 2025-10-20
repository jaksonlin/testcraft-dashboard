#!/bin/bash

# TestCraft Dashboard - Docker Stop Script

echo "╔════════════════════════════════════════════════════════════╗"
echo "║       TestCraft Dashboard - Stopping Services              ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Check command line arguments
if [ "$1" == "--remove" ] || [ "$1" == "-r" ]; then
    echo "🗑️  Stopping and removing containers..."
    docker-compose down
    echo "✅ Containers stopped and removed"
elif [ "$1" == "--clean" ] || [ "$1" == "-c" ]; then
    echo "🧹 Stopping and removing containers, networks, and volumes..."
    echo "⚠️  WARNING: This will delete all data!"
    read -p "Are you sure? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose down -v
        echo "✅ Complete cleanup done"
    else
        echo "❌ Cleanup cancelled"
    fi
else
    echo "⏹️  Stopping containers..."
    docker-compose stop
    echo "✅ Containers stopped (data preserved)"
    echo ""
    echo "💡 Tip: Use 'docker-compose start' to restart without rebuilding"
fi

echo ""
echo "Current status:"
docker-compose ps

