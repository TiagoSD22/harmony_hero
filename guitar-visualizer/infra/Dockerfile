FROM hashicorp/terraform:1.6

# Install AWS CLI and PostgreSQL client
RUN apk add --no-cache \
    aws-cli \
    postgresql-client \
    jq \
    curl

WORKDIR /app

# Copy Terraform files
COPY *.tf ./
COPY init-db.sh ./
COPY startup.sh ./

# Make the scripts executable
RUN chmod +x init-db.sh startup.sh

# Set environment variables for Localstack
ENV AWS_DEFAULT_REGION=us-east-1
ENV AWS_ACCESS_KEY_ID=test
ENV AWS_SECRET_ACCESS_KEY=test

# Create outputs directory
RUN mkdir -p /app/outputs

# Override the default terraform entrypoint and use shell
ENTRYPOINT ["/bin/sh"]
CMD ["./startup.sh"]
