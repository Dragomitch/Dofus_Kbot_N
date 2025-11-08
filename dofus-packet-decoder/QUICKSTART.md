# Quick Start Guide

Get the Dofus Packet Decoder up and running in 5 minutes.

## Option 1: Docker Compose (Recommended)

The fastest way to get started:

```bash
# Clone the repository
git clone https://github.com/your-org/dofus-packet-decoder.git
cd dofus-packet-decoder

# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f app
```

Access the application:
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

## Option 2: Local Development

### Prerequisites
- Java 21
- Maven 3.9+
- PostgreSQL 16
- Redis 7

### Steps

1. **Start PostgreSQL**
   ```bash
   # Using Docker
   docker run -d \
     --name dofus-postgres \
     -e POSTGRES_DB=dofus_decoder \
     -e POSTGRES_USER=dofus \
     -e POSTGRES_PASSWORD=changeme \
     -p 5432:5432 \
     postgres:16-alpine
   ```

2. **Start Redis**
   ```bash
   docker run -d \
     --name dofus-redis \
     -p 6379:6379 \
     redis:7-alpine
   ```

3. **Build and run**
   ```bash
   # Build
   mvn clean install -DskipTests

   # Run
   cd dofus-api
   mvn spring-boot:run
   ```

## Verify Installation

### 1. Check Health
```bash
curl http://localhost:8080/api/v1/health
```

Expected response:
```json
{
  "status": "UP",
  "timestamp": "2025-11-08T12:00:00",
  "version": "1.0.0",
  "message": "Dofus Packet Decoder is running"
}
```

### 2. Test Ping
```bash
curl http://localhost:8080/api/v1/health/ping
```

Expected response: `pong`

### 3. View API Documentation
Open in browser: http://localhost:8080/swagger-ui.html

## Using the API

### Get Game State
```bash
curl http://localhost:8080/api/v1/game/state
```

### Get Player State
```bash
curl http://localhost:8080/api/v1/game/state/player
```

## Configuration

Edit `dofus-api/src/main/resources/application.yml`:

```yaml
dofus:
  network:
    proxy:
      enabled: true
      port: 5555
      target-host: 34.251.172.139
      target-port: 443
```

## Development Mode

For development with hot reload:

```bash
# Start with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or use Docker Compose dev setup
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

This enables:
- Debug logging
- Hot reload
- H2 console
- pgAdmin (http://localhost:5050)
- Redis Commander (http://localhost:8081)

## Troubleshooting

### Port Already in Use
```bash
# Check what's using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Database Connection Failed
```bash
# Verify PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs dofus-postgres
```

### Cannot Access API
```bash
# Check application logs
docker logs dofus-app

# Or for local:
tail -f logs/dofus-packet-decoder.log
```

## Next Steps

- Read the [README.md](README.md) for detailed documentation
- Check [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines
- Explore the [PRD](../PRD_JAVA_DOFUS_PACKET_DECODER.md) for architecture details

## Stopping Services

### Docker Compose
```bash
docker-compose down

# With volume cleanup
docker-compose down -v
```

### Local Services
```bash
# Stop application
Ctrl+C

# Stop Docker containers
docker stop dofus-postgres dofus-redis
docker rm dofus-postgres dofus-redis
```
