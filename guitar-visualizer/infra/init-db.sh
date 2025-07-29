#!/bin/bash

# Wait for database to be available
echo "Waiting for database to be available..."
sleep 10

# Get database connection details from Terraform outputs or use defaults
if [ -f /app/outputs/terraform-outputs.json ]; then
    echo "Reading database configuration from Terraform outputs..."
    DB_HOST=$(cat /app/outputs/terraform-outputs.json | jq -r '.database_host.value // "postgres"')
    DB_PORT=$(cat /app/outputs/terraform-outputs.json | jq -r '.database_port.value // 5432')
    DB_NAME=$(cat /app/outputs/terraform-outputs.json | jq -r '.database_name.value // "guitar_chords"')
else
    echo "Using default database configuration..."
    DB_HOST="postgres"
    DB_PORT="5432"
    DB_NAME="guitar_chords"
fi

DB_USER="chorduser"
DB_PASSWORD="chordpass"

echo "Connecting to database: $DB_HOST:$DB_PORT/$DB_NAME"

# Create database schema
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
-- Create tables for chord data
CREATE TABLE IF NOT EXISTS keys (
    id SERIAL PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS qualities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chords (
    id SERIAL PRIMARY KEY,
    key_id INTEGER REFERENCES keys(id),
    quality_id INTEGER REFERENCES qualities(id),
    representation VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(key_id, quality_id)
);

CREATE TABLE IF NOT EXISTS variations (
    id SERIAL PRIMARY KEY,
    chord_id INTEGER REFERENCES chords(id),
    name VARCHAR(100) NOT NULL,
    diagram TEXT NOT NULL,
    difficulty_level INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_chords_key_quality ON chords(key_id, quality_id);
CREATE INDEX IF NOT EXISTS idx_variations_chord_id ON variations(chord_id);
"

if [ $? -eq 0 ]; then
    echo "Database schema created successfully!"
else
    echo "Failed to create database schema"
    exit 1
fi

echo "Database initialization completed!"
