#!/bin/bash

# Скрипт для управления банковским приложением в Docker

set -e

echo "🏦 Bank REST API Docker Management Script"
echo "=========================================="

case "$1" in
    "build")
        echo "🔨 Building application..."
        docker compose build --no-cache
        ;;
    "up")
        echo "🚀 Starting all services..."
        docker compose up -d
        echo "✅ Services started!"
        echo "📊 Application: http://localhost:8080"
        echo "📖 Swagger UI: http://localhost:8080/swagger-ui.html"
        echo "🗄️  PgAdmin: http://localhost:8081"
        ;;
    "down")
        echo "🛑 Stopping all services..."
        docker compose down
        ;;
    "logs")
        echo "📋 Showing logs..."
        docker compose logs -f
        ;;
    "status")
        echo "📊 Service status:"
        docker compose ps
        ;;
    "clean")
        echo "🧹 Cleaning up..."
        docker compose down -v --rmi all
        docker system prune -f
        ;;
    "restart")
        echo "🔄 Restarting services..."
        docker compose restart
        ;;
    *)
        echo "Usage: $0 {build|up|down|logs|status|clean|restart}"
        echo ""
        echo "Commands:"
        echo "  build   - Build the application image"
        echo "  up      - Start all services"
        echo "  down    - Stop all services"
        echo "  logs    - Show service logs"
        echo "  status  - Show service status"
        echo "  clean   - Clean up all containers, volumes and images"
        echo "  restart - Restart all services"
        exit 1
        ;;
esac
