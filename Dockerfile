# Using multi-stage build to optimize image size
FROM eclipse-temurin:17-jdk-alpine as build

# Setting working directory
WORKDIR /app

# Copy Maven wrapper and configuration files
COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
COPY .mvn .mvn

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies (for layer caching)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Production image
FROM eclipse-temurin:17-jre-alpine

# Install curl for health check
RUN apk add --no-cache curl

# Create user for running the application (security)
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy built JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Change file ownership
RUN chown -R appuser:appgroup /app

# Switch to application user
USER appuser

# Expose port 8080
EXPOSE 8080

# Configure JVM for container
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# Healthcheck for application status monitoring
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Application startup command
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
