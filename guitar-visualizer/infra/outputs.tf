output "database_host" {
  description = "Database host"
  value       = var.database_host
}

output "database_port" {
  description = "Database port"
  value       = var.database_port
}

output "database_name" {
  description = "Database name"
  value       = var.database_name
}

output "database_url" {
  description = "Full database connection URL"
  value       = "postgresql://${var.database_username}:${var.database_password}@${var.database_host}:${var.database_port}/${var.database_name}"
  sensitive   = true
}

output "secret_arn" {
  description = "ARN of the database credentials secret"
  value       = aws_secretsmanager_secret.guitar_chord_db_secret.arn
}

output "app_role_arn" {
  description = "ARN of the application IAM role"
  value       = aws_iam_role.guitar_chord_app_role.arn
}

output "app_instance_profile_name" {
  description = "Name of the application instance profile"
  value       = aws_iam_instance_profile.guitar_chord_app_profile.name
}
