.PHONY: help build test clean install run docker-build docker-up docker-down docker-logs

# Default target
help:
	@echo "Dofus Packet Decoder - Available Commands"
	@echo "=========================================="
	@echo "  make build         - Build all modules"
	@echo "  make test          - Run all tests"
	@echo "  make install       - Build and install to local repository"
	@echo "  make clean         - Clean build artifacts"
	@echo "  make run           - Run the application"
	@echo "  make docker-build  - Build Docker image"
	@echo "  make docker-up     - Start Docker Compose stack"
	@echo "  make docker-down   - Stop Docker Compose stack"
	@echo "  make docker-logs   - View Docker logs"
	@echo "  make dev           - Start in development mode"

# Build commands
build:
	mvn clean compile

test:
	mvn test

install:
	mvn clean install

clean:
	mvn clean
	rm -rf logs/

# Run commands
run:
	cd dofus-api && mvn spring-boot:run

run-dev:
	cd dofus-api && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Docker commands
docker-build:
	docker-compose build

docker-up:
	docker-compose up -d
	@echo "Services starting..."
	@echo "Application: http://localhost:8080"
	@echo "Swagger UI: http://localhost:8080/swagger-ui.html"
	@echo "PostgreSQL: localhost:5432"
	@echo "Redis: localhost:6379"

docker-down:
	docker-compose down

docker-logs:
	docker-compose logs -f app

docker-clean:
	docker-compose down -v
	docker system prune -f

# Development mode with tools
dev:
	docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
	@echo "Development mode started"
	@echo "pgAdmin: http://localhost:5050"
	@echo "Redis Commander: http://localhost:8081"

# Database commands
db-migrate:
	mvn flyway:migrate

db-clean:
	mvn flyway:clean

# Coverage report
coverage:
	mvn clean test jacoco:report
	@echo "Coverage report: target/site/jacoco/index.html"

# Code quality
checkstyle:
	mvn checkstyle:check

# Dependency updates
dependency-updates:
	mvn versions:display-dependency-updates
	mvn versions:display-plugin-updates
