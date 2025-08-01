# Multi-stage build for Scala backend
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.8.1_1.9.6_3.3.1 AS builder

WORKDIR /app

# Copy SBT configuration files first for better caching
COPY project/ project/
COPY build.sbt ./

# Download dependencies
RUN sbt update

# Copy source code
COPY src/ src/

# Build the application
RUN sbt assembly

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install PostgreSQL client for database operations
RUN apt-get update && apt-get install -y \
    postgresql-client \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy the assembled JAR from builder stage
COPY --from=builder /app/target/scala-3.3.1/guitar-chord-backend-assembly-1.0.0.jar app.jar

# Create directory for shared chord data
RUN mkdir -p /app/shared /app/outputs

# Copy startup script
COPY docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV DATABASE_URL="postgresql://chorduser:chordpass@postgres:5432/guitar_chords"

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Use the startup script as entrypoint
ENTRYPOINT ["/app/docker-entrypoint.sh"]
