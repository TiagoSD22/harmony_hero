.PHONY: help dev dev-infra dev-full clean build logs infra-up backend-up frontend-up down test

# Default target
help:
	@echo "Guitar Chord Visualizer - Development Commands"
	@echo "=============================================="
	@echo "make dev-infra    - Start only infrastructure services (localstack + db-provisioner)"
	@echo "make dev-full     - Start all services (full application stack)"
	@echo "make dev-local    - Start with local PostgreSQL only (no localstack)"
	@echo "make dev          - Alias for dev-full"
	@echo "make build        - Build all Docker containers"
	@echo "make infra-up     - Start infrastructure services"
	@echo "make backend-up   - Start backend service (requires infra)"
	@echo "make frontend-up  - Start frontend service (requires backend)"
	@echo "make logs         - Show logs for all services"
	@echo "make logs-infra   - Show logs for infrastructure services only"
	@echo "make down         - Stop all services"
	@echo "make clean        - Clean up containers and volumes"
	@echo "make test         - Run all tests"

# Start only infrastructure services
dev-infra:
	@echo "🏗️ Starting infrastructure services (localstack + db-provisioner)..."
	docker-compose --profile infra up --build

# Start all services (full stack)
dev-full:
	@echo "🚀 Starting full application stack..."
	docker-compose --profile full up --build

# Start with local PostgreSQL only (no localstack)
dev-local:
	@echo "🏠 Starting with local PostgreSQL only..."
	docker-compose --profile local up --build

# Development workflow
dev: dev-full
	@echo "🎸 Guitar Chord Visualizer is running!"
	@echo "Frontend: http://localhost:3000"
	@echo "Backend:  http://localhost:8080"
	@echo "Use 'make logs' to view service logs"

# Build all containers
build:
	@echo "🔨 Building Docker containers..."
	docker-compose build

# Start infrastructure only
infra-up:
	@echo "🏗️ Starting infrastructure services..."
	docker-compose --profile infra up -d

# Start backend only (requires infra)
backend-up:
	@echo "⚙️ Starting backend service..."
	docker-compose up -d backend

# Start frontend only (requires backend)
frontend-up:
	@echo "🎨 Starting frontend service..."
	docker-compose up -d frontend

# View logs
logs:
	docker-compose logs -f

# View logs for infrastructure services only
logs-infra:
	docker-compose --profile infra logs -f

# Stop all services
down:
	@echo "🛑 Stopping all services..."
	docker-compose down

# Clean up everything
clean:
	@echo "🧹 Cleaning up containers and volumes..."
	docker-compose down -v --remove-orphans
	docker system prune -f

# Run tests
test:
	@echo "🧪 Running tests..."
	cd backend && sbt test
	cd frontend && npm test

# Development helpers
frontend-dev:
	@echo "🎨 Starting frontend in development mode..."
	cd frontend && npm install && npm run dev

backend-dev:
	@echo "⚙️ Starting backend in development mode..."
	cd backend && sbt run

# Database helpers
db-migrate:
	@echo "📊 Running database migrations..."
	cd backend && sbt flywayMigrate

db-reset:
	@echo "🔄 Resetting database..."
	cd backend && sbt flywayClean flywayMigrate

db-shell:
	@echo "🗄️ Connecting to PostgreSQL shell..."
	docker-compose exec postgres psql -U chorduser -d guitar_chords

db-logs:
	@echo "📋 Showing PostgreSQL logs..."
	docker-compose logs postgres

# Quick quality checks
lint:
	cd frontend && npm run lint
	cd backend && sbt scalafmtCheck

format:
	cd frontend && npm run format
	cd backend && sbt scalafmt
