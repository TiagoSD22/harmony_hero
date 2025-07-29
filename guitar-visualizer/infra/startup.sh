#!/bin/sh

#!/bin/sh

echo "Starting Terraform infrastructure provisioning..."

# Wait for Localstack to be ready
echo "Waiting for Localstack to be ready..."
echo "Testing connectivity to localstack:4566..."
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    # First check basic connectivity
    if ! curl -s --connect-timeout 5 http://localstack:4566 >/dev/null 2>&1; then
        echo "Cannot connect to LocalStack at localstack:4566..."
        attempt=$((attempt + 1))
        echo "Attempt $attempt/$max_attempts - sleeping..."
        sleep 5
        continue
    fi
    
    # Check if LocalStack health endpoint is responding
    health_response=$(curl -s --connect-timeout 5 http://localstack:4566/_localstack/health 2>/dev/null)
    if [ $? -eq 0 ] && [ -n "$health_response" ]; then
        echo "LocalStack health endpoint responding..."
        # Check if secretsmanager service is mentioned and available
        if echo "$health_response" | grep -q '"secretsmanager".*"available"'; then
            echo "Localstack Secrets Manager service is ready!"
            break
        elif echo "$health_response" | grep -q '"secretsmanager"'; then
            echo "Secrets Manager found but not yet available, waiting..."
        else
            echo "Secrets Manager service not found in health response, waiting..."
        fi
    else
        echo "LocalStack health endpoint not responding yet..."
    fi
    
    attempt=$((attempt + 1))
    echo "Waiting for Secrets Manager service... attempt $attempt/$max_attempts"
    sleep 5
done

if [ $attempt -eq $max_attempts ]; then
    echo "Failed to connect to Localstack after $max_attempts attempts"
    exit 1
fi

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if pg_isready -h postgres -p 5432 >/dev/null 2>&1; then
        echo "PostgreSQL is ready!"
        break
    fi
    
    attempt=$((attempt + 1))
    echo "Waiting for PostgreSQL... attempt $attempt/$max_attempts"
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo "Failed to connect to PostgreSQL after $max_attempts attempts"
    exit 1
fi

# Additional wait for service stability
echo "Waiting for service stability..."
sleep 5

# Initialize Terraform
echo "Initializing Terraform..."
terraform init

if [ $? -ne 0 ]; then
    echo "Terraform initialization failed"
    exit 1
fi

# Apply Terraform configuration
echo "Applying Terraform configuration..."
terraform apply -auto-approve

if [ $? -ne 0 ]; then
    echo "Terraform apply failed"
    exit 1
fi

# Export Terraform outputs
echo "Exporting Terraform outputs..."
terraform output -json > /app/outputs/terraform-outputs.json

if [ $? -ne 0 ]; then
    echo "Failed to export Terraform outputs"
    exit 1
fi

# Extract database connection details from Terraform outputs
echo "Extracting database connection details..."
DB_HOST=$(terraform output -raw database_host 2>/dev/null || echo "postgres")
DB_PORT=$(terraform output -raw database_port 2>/dev/null || echo "5432")
DB_NAME=$(terraform output -raw database_name 2>/dev/null || echo "guitar_chords")
DB_USER="chorduser"
DB_PASSWORD="chordpass"

echo "Database details: $DB_HOST:$DB_PORT/$DB_NAME"

# Export database URL for backend service
echo "DATABASE_URL=postgresql://$DB_USER:$DB_PASSWORD@$DB_HOST:$DB_PORT/$DB_NAME" > /app/outputs/database.env

echo "Infrastructure provisioned successfully!"

echo "Infrastructure setup completed successfully!"
