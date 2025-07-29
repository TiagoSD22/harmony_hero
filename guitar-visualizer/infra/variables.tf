variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "us-east-1"
}

variable "database_name" {
  description = "Name of the PostgreSQL database"
  type        = string
  default     = "guitar_chords"
}

variable "database_host" {
  description = "Database host"
  type        = string
  default     = "postgres"
}

variable "database_port" {
  description = "Database port"
  type        = number
  default     = 5432
}

variable "database_username" {
  description = "Database username"
  type        = string
  default     = "admin"
}

variable "database_password" {
  description = "Database password"
  type        = string
  default     = "password"
  sensitive   = true
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "local"
}
