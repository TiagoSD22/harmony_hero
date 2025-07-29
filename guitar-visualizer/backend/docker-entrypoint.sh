#!/bin/bash

echo "Starting Guitar Chord Backend..."

# Check if we're running with infrastructure services or just PostgreSQL
if [ "${PROFILE:-full}" = "local" ]; then
    echo "Running in local mode (PostgreSQL only)..."
    # Use environment variables directly for local mode
    export DATABASE_URL=${DATABASE_URL:-"postgresql://chorduser:chordpass@postgres:5432/guitar_chords"}
else
    # Wait for infrastructure to be ready
    echo "Waiting for infrastructure to be ready..."
    while [ ! -f /app/outputs/database.env ]; do
        echo "Waiting for database configuration..."
        sleep 5
    done

    # Source database configuration
    if [ -f /app/outputs/database.env ]; then
        echo "Loading database configuration..."
        export $(cat /app/outputs/database.env | xargs)
    fi
fi

# Wait for database to be available
echo "Waiting for database to be available..."
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if pg_isready -h $(echo $DATABASE_URL | sed 's/.*@\([^:]*\):.*/\1/') -p $(echo $DATABASE_URL | sed 's/.*:\([0-9]*\)\/.*/\1/'); then
        echo "Database is ready!"
        break
    fi
    
    attempt=$((attempt + 1))
    echo "Database not ready, attempt $attempt/$max_attempts"
    sleep 5
done

if [ $attempt -eq $max_attempts ]; then
    echo "Failed to connect to database after $max_attempts attempts"
    exit 1
fi

echo "Starting application..."
exec java $JAVA_OPTS -jar app.jar
