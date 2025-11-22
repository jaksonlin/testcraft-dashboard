#!/bin/bash
# TestCraft Dashboard - Start with Shadow Database

echo "Starting TestCraft Dashboard with Shadow Database..."
echo "=================================================="

# Check if .env file exists
if [ ! -f .env ]; then
    echo "Creating .env file from template..."
    cp env.example .env
    echo "Please edit .env file with your configuration before continuing."
    echo "Press Enter to continue or Ctrl+C to exit..."
    read
fi

# Start with shadow profile
echo "Starting services with shadow database..."
docker-compose --profile shadow up -d

echo ""
echo "Services started:"
echo "- PostgreSQL Primary: localhost:5432"
echo "- PostgreSQL Shadow: localhost:5433"
echo "- Backend API: localhost:8090"
echo "- Frontend: localhost:80"
echo ""
echo "To view logs:"
echo "  docker-compose logs -f"
echo ""
echo "To stop:"
echo "  docker-compose down"
