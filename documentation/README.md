# Dofus Retro Packet Decoder

[![Java Version](https://img.shields.io/badge/Java-26-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Educational-blue.svg)](#license)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](#)

A high-performance Java 26 + Spring Boot application for intercepting, decoding, and analyzing Dofus Retro network packets. This project provides real-time game state awareness through network protocol analysis rather than image recognition.

---

## Features

- **Network Packet Interception**
  - MITM proxy for transparent packet capture
  - Support for bidirectional traffic (client ↔ server)
  - Zero packet loss with high-throughput processing

- **Comprehensive Packet Decoding**
  - 100+ packet types supported
  - Automatic packet registry with annotations
  - Protocol version management

- **Real-Time Game State Tracking**
  - Player position and stats
  - Map and entity tracking
  - Combat state awareness
  - Inventory management

- **Intelligent Navigation**
  - A* pathfinding algorithm
  - Obstacle detection and avoidance
  - MP cost calculation
  - Multi-map navigation support

- **Turn-Based Combat System**
  - Combat state machine
  - Configurable strategies (Aggressive, Defensive, Custom)
  - Spell management and targeting
  - Turn timeout handling

- **REST API & WebSocket**
  - RESTful endpoints for all features
  - Real-time WebSocket updates
  - JWT authentication
  - OpenAPI/Swagger documentation

- **Web-Based Dashboard**
  - Real-time packet viewer
  - Map visualizer
  - Combat log
  - Configuration management

- **Persistence & Caching**
  - PostgreSQL for long-term storage
  - Redis for high-speed caching
  - Automatic data archival
  - Query optimization

---

## Table of Contents

- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)
- [Disclaimer](#disclaimer)

---

## Quick Start

### Prerequisites

- **Java 26** (JDK 26 or later)
- **Docker & Docker Compose** (for services)
- **Maven 3.9+** (for building)
- **Git** (for cloning)

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/your-username/dofus-packet-decoder.git
cd dofus-packet-decoder
```

2. **Start required services (PostgreSQL & Redis):**
```bash
docker-compose up -d
```

3. **Build the application:**
```bash
mvn clean install
```

4. **Run the application:**
```bash
mvn spring-boot:run
```

5. **Access the application:**
   - **API:** http://localhost:8080/api/v1
   - **Dashboard:** http://localhost:8080/dashboard
   - **Swagger UI:** http://localhost:8080/swagger-ui.html
   - **Health Check:** http://localhost:8080/actuator/health

### First Steps

1. **Configure your Dofus client** to connect through the proxy (port 5555)
2. **Start the Dofus client** and log in
3. **Access the dashboard** to view real-time packet capture
4. **Use the API** to access game state and control features

---

## Architecture

The Dofus Retro Packet Decoder follows a modular monolith architecture with clear separation of concerns.

### High-Level Overview

```
┌─────────────────────────────────────────────────────┐
│              Spring Boot Application                 │
├─────────────────────────────────────────────────────┤
│                                                       │
│  REST API ──► Business Logic ──► Packet Decoder     │
│  WebSocket                      Network Layer       │
│                                                       │
│  PostgreSQL ◄──► Redis Cache ◄──► Game State       │
└─────────────────────────────────────────────────────┘
         │                              │
    ┌────▼────┐                    ┌────▼────┐
    │  Dofus  │ ◄─── Proxy ──────► │  Dofus  │
    │ Client  │                     │ Server  │
    └─────────┘                     └─────────┘
```

### Module Structure

The project is organized into multiple Maven modules:

| Module | Description |
|--------|-------------|
| **dofus-network-core** | TCP proxy and packet capture using Netty |
| **dofus-protocol** | Packet type definitions and protocol specs |
| **dofus-packet-decoder** | Decoding/encoding services |
| **dofus-game-state** | Game state management and domain model |
| **dofus-navigation** | A* pathfinding and map management |
| **dofus-combat** | Turn-based combat system |
| **dofus-api** | REST API and WebSocket endpoints |
| **dofus-persistence** | JPA entities and repositories |
| **dofus-web-ui** | React frontend dashboard |

For detailed architecture documentation, see [ARCHITECTURE.md](ARCHITECTURE.md).

---

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 26**
   ```bash
   # Check Java version
   java -version
   # Should output: java version "26" or higher
   ```

   Download from:
   - [OpenJDK](https://openjdk.org/)
   - [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)

2. **Maven 3.9+**
   ```bash
   # Check Maven version
   mvn -version
   # Should output: Apache Maven 3.9.x or higher
   ```

3. **Docker & Docker Compose**
   ```bash
   # Check Docker version
   docker --version
   docker-compose --version
   ```

   Download from: [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### Optional Tools

- **Node.js 18+** (for frontend development)
- **PostgreSQL Client** (for database management)
- **Redis CLI** (for cache inspection)
- **Postman** or **Insomnia** (for API testing)

---

## Installation

### Option 1: Docker Compose (Recommended)

The easiest way to run the entire stack:

1. **Clone the repository:**
```bash
git clone https://github.com/your-username/dofus-packet-decoder.git
cd dofus-packet-decoder
```

2. **Start all services:**
```bash
docker-compose up -d
```

This will start:
- PostgreSQL (port 5432)
- Redis (port 6379)
- Dofus Packet Decoder application (port 8080)
- Frontend dashboard (port 3000)

3. **Check service status:**
```bash
docker-compose ps
```

4. **View logs:**
```bash
docker-compose logs -f app
```

### Option 2: Manual Installation

1. **Install PostgreSQL:**
```bash
# Ubuntu/Debian
sudo apt install postgresql-16

# macOS
brew install postgresql@16

# Start PostgreSQL
sudo systemctl start postgresql
```

2. **Install Redis:**
```bash
# Ubuntu/Debian
sudo apt install redis-server

# macOS
brew install redis

# Start Redis
redis-server
```

3. **Create database:**
```bash
psql -U postgres
CREATE DATABASE dofus_decoder;
CREATE USER dofus WITH PASSWORD 'changeme';
GRANT ALL PRIVILEGES ON DATABASE dofus_decoder TO dofus;
\q
```

4. **Clone and build:**
```bash
git clone https://github.com/your-username/dofus-packet-decoder.git
cd dofus-packet-decoder
mvn clean install
```

5. **Configure application:**
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dofus_decoder
    username: dofus
    password: changeme

  data:
    redis:
      host: localhost
      port: 6379
```

6. **Run application:**
```bash
mvn spring-boot:run
```

---

## Configuration

### Application Configuration

The main configuration file is `application.yml`:

```yaml
dofus:
  network:
    proxy:
      enabled: true
      port: 5555
      target-host: 34.251.172.139
      target-port: 443
    capture:
      buffer-size: 8192
      max-connections: 100

  protocol:
    version: 1.30.0
    encryption:
      enabled: false
      algorithm: XOR

  game:
    auto-reconnect: true
    packet-logging: true
    state-persistence: true

  combat:
    strategy: AGGRESSIVE
    auto-pass-turn: false
    max-turn-time: 45

  navigation:
    algorithm: A_STAR
    max-path-length: 50
    avoid-aggressive-mobs: true

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dofus_decoder
    username: dofus
    password: changeme

  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

server:
  port: 8080
  compression:
    enabled: true

logging:
  level:
    com.dofus: DEBUG
    org.springframework: INFO
```

### Environment Variables

Override configuration using environment variables:

```bash
export DOFUS_NETWORK_PROXY_PORT=5555
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/dofus_decoder
export SPRING_DATASOURCE_PASSWORD=your-password
export SPRING_PROFILES_ACTIVE=prod
```

### Profile-Specific Configuration

Create profile-specific files:
- `application-dev.yml` (development)
- `application-test.yml` (testing)
- `application-prod.yml` (production)

Activate a profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Usage

### Configuring Dofus Client

To use the packet decoder, configure your Dofus client to connect through the proxy:

**Option 1: Modify hosts file**
```bash
# Add to /etc/hosts (Linux/macOS) or C:\Windows\System32\drivers\etc\hosts (Windows)
127.0.0.1 dofus-server.ankama.com
```

Then ensure the proxy is running on port 443.

**Option 2: Use proxy settings**
Configure your Dofus client to use proxy:
- Proxy host: `localhost`
- Proxy port: `5555`

### Starting the Application

```bash
# Development mode (with hot reload)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production mode
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# With custom JVM options
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx2048m -Xms512m"
```

### Accessing the Dashboard

Navigate to http://localhost:8080/dashboard to access the web interface.

**Features:**
- Real-time packet viewer with filtering
- Map visualization with entity positions
- Combat log and turn tracker
- Configuration editor
- Statistics and metrics

### Using the REST API

**Example: Get current game state**
```bash
# 1. Login to get JWT token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' \
  | jq -r '.token')

# 2. Get game state
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/game/state | jq
```

**Example: Calculate path**
```bash
curl -X POST http://localhost:8080/api/v1/navigation/path \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "start": {"x": 100, "y": 50},
    "end": {"x": 200, "y": 150},
    "maxMpCost": 10
  }' | jq
```

For complete API documentation, see [API_GUIDE.md](API_GUIDE.md).

---

## API Documentation

### Interactive Documentation

Access the interactive Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Specification

Download the OpenAPI 3.0 specification:
```
http://localhost:8080/v3/api-docs
```

### Key Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/game/state` | GET | Get current game state |
| `/api/v1/game/state/map` | GET | Get current map state |
| `/api/v1/game/state/combat` | GET | Get combat state |
| `/api/v1/packets` | GET | List packets (paginated) |
| `/api/v1/packets/{id}` | GET | Get specific packet |
| `/api/v1/navigation/path` | POST | Calculate path |
| `/api/v1/navigation/move` | POST | Execute movement |
| `/api/v1/combat/actions` | GET | Get available combat actions |
| `/api/v1/combat/execute` | POST | Execute combat action |
| `/ws/packets` | WS | Real-time packet stream |
| `/ws/game-state` | WS | Real-time state updates |

For detailed API documentation with examples, see [API_GUIDE.md](API_GUIDE.md).

---

## Development

### Project Structure

```
dofus-packet-decoder/
├── dofus-network-core/
│   └── src/main/java/com/dofus/network/
│       ├── proxy/          # MITM proxy implementation
│       ├── capture/        # Packet capture service
│       └── handler/        # Netty handlers
├── dofus-protocol/
│   └── src/main/java/com/dofus/protocol/
│       ├── packets/        # Packet definitions
│       ├── codec/          # Encoder/decoder
│       └── registry/       # Packet registry
├── dofus-game-state/
│   └── src/main/java/com/dofus/game/
│       ├── model/          # Domain models
│       ├── service/        # Business logic
│       └── event/          # Event definitions
├── dofus-api/
│   └── src/main/java/com/dofus/api/
│       ├── controller/     # REST controllers
│       ├── dto/            # Data transfer objects
│       └── websocket/      # WebSocket endpoints
└── dofus-web-ui/
    └── src/
        ├── components/     # React components
        ├── services/       # API clients
        └── App.tsx
```

### Building from Source

```bash
# Clean build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl dofus-network-core

# Build with code coverage
mvn clean test jacoco:report
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PacketDecoderTest

# Run integration tests
mvn verify -P integration-tests

# Generate coverage report
mvn jacoco:report
# View report at: target/site/jacoco/index.html
```

### Code Style

The project follows Google Java Style Guide. Format code using:

```bash
# Format all code
mvn fmt:format

# Check formatting
mvn fmt:check
```

### Adding New Packet Types

1. **Create packet class:**
```java
@Data
@PacketId("GDM")
public class GameDataMapPacket extends GameWorldPacket {
    private int mapId;
    private String mapDate;
    private List<Entity> entities;

    @Override
    public void decode(String rawData) {
        String[] parts = rawData.split("\\|");
        this.mapId = Integer.parseInt(parts[0]);
        // ... decode other fields
    }

    @Override
    public String encode() {
        return String.format("%d|%s|...", mapId, mapDate);
    }
}
```

2. **Add test:**
```java
@Test
void testDecode() {
    String raw = "432|2025-11-08|abc123";
    GameDataMapPacket packet = new GameDataMapPacket();
    packet.decode(raw);

    assertEquals(432, packet.getMapId());
    assertEquals("2025-11-08", packet.getMapDate());
}
```

3. **Register automatically** via `@PacketId` annotation

---

## Testing

### Unit Tests

```bash
# Run unit tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

### Integration Tests

```bash
# Run integration tests (requires Docker)
mvn verify -P integration-tests
```

Integration tests use Testcontainers to spin up PostgreSQL and Redis automatically.

### Manual Testing

1. **Start the application** in dev mode
2. **Connect Dofus client** through proxy
3. **Monitor packets** in dashboard
4. **Test API endpoints** with Postman/cURL

### Performance Testing

```bash
# Run JMH benchmarks
mvn clean install -P benchmarks
java -jar target/benchmarks.jar
```

---

## Deployment

### Docker Deployment

**Build Docker image:**
```bash
docker build -t dofus-packet-decoder:latest .
```

**Run container:**
```bash
docker run -d \
  -p 8080:8080 \
  -p 5555:5555 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/dofus_decoder \
  --name dofus-decoder \
  dofus-packet-decoder:latest
```

### Docker Compose

**Production deployment:**
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes

**Deploy to Kubernetes:**
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
```

### Environment Variables for Production

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/dofus_decoder
SPRING_DATASOURCE_USERNAME=dofus
SPRING_DATASOURCE_PASSWORD=secure-password

# Redis
SPRING_DATA_REDIS_HOST=redis-host
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_PASSWORD=redis-password

# Application
DOFUS_NETWORK_PROXY_PORT=5555
DOFUS_NETWORK_PROXY_TARGET_HOST=34.251.172.139
DOFUS_NETWORK_PROXY_TARGET_PORT=443

# Security
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=3600
```

For detailed deployment instructions, see [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md).

---

## Contributing

We welcome contributions! Please read our [Contributing Guidelines](CONTRIBUTING.md) before submitting pull requests.

### Development Workflow

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Make your changes**
4. **Write tests** for your changes
5. **Ensure all tests pass** (`mvn clean verify`)
6. **Commit your changes** (`git commit -m 'Add amazing feature'`)
7. **Push to the branch** (`git push origin feature/amazing-feature`)
8. **Open a Pull Request**

### Code Quality Standards

- 80%+ test coverage
- All tests must pass
- Follow Google Java Style Guide
- Document public APIs with Javadoc
- Add integration tests for new features

---

## License

This project is licensed for **educational purposes only**.

**Restrictions:**
- Not for commercial use
- Not for distribution
- Use at your own risk
- Respect Dofus Terms of Service

See the [LICENSE](LICENSE) file for details.

---

## Disclaimer

**IMPORTANT:** This software is for educational purposes only.

- **No warranty:** Use at your own risk
- **Terms of Service:** Using this software may violate Dofus Terms of Service
- **Account bans:** You may be banned for using network interception tools
- **Educational use:** This project is intended for learning about network protocols and reverse engineering
- **No support for cheating:** This tool should not be used to gain unfair advantages in-game

The developers are not responsible for any consequences of using this software.

---

## Credits

### Built With

- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [Netty](https://netty.io/) - Network application framework
- [PostgreSQL](https://www.postgresql.org/) - Database
- [Redis](https://redis.io/) - Cache
- [React](https://reactjs.org/) - Frontend framework

### Inspired By

- [retroproto](https://github.com/kralamoure/retroproto) - Dofus Retro protocol in Go
- [Guinness-Bot](https://github.com/Romain-P/Guinness-Bot) - Dofus bot with MITM
- [dofus-protocol](https://github.com/AstrubTools/dofus-protocol) - Protocol documentation

### Contributors

Thanks to all contributors who have helped with this project!

---

## Support

### Documentation

- [Architecture Documentation](ARCHITECTURE.md)
- [API Guide](API_GUIDE.md)
- [Deployment Guide](DEPLOYMENT_GUIDE.md)
- [Contributing Guidelines](CONTRIBUTING.md)

### Resources

- **Issues:** [GitHub Issues](https://github.com/your-username/dofus-packet-decoder/issues)
- **Discussions:** [GitHub Discussions](https://github.com/your-username/dofus-packet-decoder/discussions)
- **Wiki:** [Project Wiki](https://github.com/your-username/dofus-packet-decoder/wiki)

### Community

- **Discord:** [Join our Discord](https://discord.gg/your-invite)
- **Forum:** [Community Forum](https://forum.example.com)

---

## Roadmap

### Current Version (v1.0)
- [x] Packet interception and decoding
- [x] Game state tracking
- [x] A* pathfinding
- [x] Turn-based combat
- [x] REST API
- [x] Web dashboard

### Future Releases

**v1.1 - Enhanced Features**
- [ ] Multi-account support
- [ ] Advanced combat strategies
- [ ] Machine learning integration
- [ ] Mobile app

**v2.0 - Scalability**
- [ ] Microservices architecture
- [ ] Kubernetes deployment
- [ ] Cloud-native features
- [ ] Plugin system

---

## Acknowledgments

Special thanks to:
- The Dofus Retro community for protocol documentation
- Open source projects that inspired this work
- Contributors and testers
- Everyone who provided feedback

---

**Made with ❤️ for educational purposes**

**Version:** 1.0.0
**Last Updated:** 2025-11-08
