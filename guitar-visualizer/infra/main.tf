terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region                      = var.aws_region
  access_key                  = "test"
  secret_key                  = "test"
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    iam            = "http://localstack:4566"
    secretsmanager = "http://localstack:4566"
  }
}

# Secrets Manager for database credentials
resource "aws_secretsmanager_secret" "guitar_chord_db_secret" {
  name = "guitar-chord-db-credentials"
  
  tags = {
    Name        = "guitar-chord-db-secret"
    Environment = var.environment
  }
}

resource "aws_secretsmanager_secret_version" "guitar_chord_db_secret_version" {
  secret_id = aws_secretsmanager_secret.guitar_chord_db_secret.id
  secret_string = jsonencode({
    username = var.database_username
    password = var.database_password
    host     = var.database_host
    port     = var.database_port
    dbname   = var.database_name
    url      = "postgresql://${var.database_username}:${var.database_password}@${var.database_host}:${var.database_port}/${var.database_name}"
  })
}

# IAM Role for backend application
resource "aws_iam_role" "guitar_chord_app_role" {
  name = "guitar-chord-app-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      },
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          AWS = "*"
        }
      }
    ]
  })

  tags = {
    Name        = "guitar-chord-app-role"
    Environment = var.environment
  }
}

# IAM Policy for accessing Secrets Manager
resource "aws_iam_role_policy" "guitar_chord_secrets_policy" {
  name = "guitar-chord-secrets-policy"
  role = aws_iam_role.guitar_chord_app_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ]
        Resource = aws_secretsmanager_secret.guitar_chord_db_secret.arn
      }
    ]
  })
}

# IAM Instance Profile (for potential EC2 usage)
resource "aws_iam_instance_profile" "guitar_chord_app_profile" {
  name = "guitar-chord-app-profile"
  role = aws_iam_role.guitar_chord_app_role.name
}
