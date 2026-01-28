#!/bin/bash
# Render.com build script

echo "🔨 Building Spring Boot Backend for Render.com..."

# Clean and package
./mvnw clean package -DskipTests

echo "✅ Build completed!"
echo "📦 JAR file location: target/*.jar"
