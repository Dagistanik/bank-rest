#!/bin/bash

# Bank REST API Quick Start Script

echo "🏦 Bank REST API - Quick Start"
echo "=============================="

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Install Docker and try again."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Install Docker Compose and try again."
    exit 1
fi

echo "✅ Docker found"

# Stop and remove old containers
echo "🧹 Cleaning up old containers..."
docker-compose down -v

# Build and start
echo "🚀 Starting application..."
docker-compose up -d --build

# Wait for startup
echo "⏳ Waiting for application startup..."
sleep 30

# Check status
echo "🔍 Checking application status..."
if curl -f http://localhost:8080/actuator/health &> /dev/null; then
    echo "✅ Application started successfully!"
    echo ""
    echo "📍 Available URLs:"
    echo "   • Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "   • API Documentation: http://localhost:8080/v3/api-docs"
    echo "   • Health Check: http://localhost:8080/actuator/health"
    echo ""
    echo "🔑 Test users:"
    echo "   • user/password (USER role)"
    echo "   • admin/password (ADMIN role)"
    echo ""
    echo "💡 Get JWT token:"
    echo "   curl -X POST http://localhost:8080/api/auth/login \\"
    echo "     -H \"Content-Type: application/json\" \\"
    echo "     -d '{\"username\": \"user\", \"password\": \"password\"}'"
else
    echo "❌ Application is not responding. Check logs:"
    echo "   docker-compose logs app"
fi

echo ""
echo "🛠️ Useful commands:"
echo "   • View logs: docker-compose logs -f app"
echo "   • Stop: docker-compose down"
echo "   • Restart: docker-compose restart"
