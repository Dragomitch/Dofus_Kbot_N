# Dofus Retro Packet Decoder & Navigation System

A Java 21 + Spring Boot application that intercepts and decodes Dofus Retro network packets to enable intelligent navigation and game state awareness.

## Project Overview

This project provides a robust, enterprise-grade system for:
- Intercepting Dofus Retro network packets via MITM proxy
- Decoding and parsing game protocol messages
- Tracking real-time game state
- Intelligent pathfinding and navigation
- Turn-based combat logic
- REST API and WebSocket support

## Architecture

Multi-module Maven project with the following modules:

- **dofus-network-core** - Network interception and TCP handling
- **dofus-protocol** - Packet type definitions and protocol management
- **dofus-packet-decoder** - Packet parsing and decoding logic
- **dofus-game-state** - Game state management and tracking
- **dofus-navigation** - Pathfinding algorithms (A*, Dijkstra)
- **dofus-combat** - Turn-based combat strategies
- **dofus-persistence** - Database entities and repositories
- **dofus-api** - REST API and main application
- **dofus-web-ui** - Web dashboard (React/Vue)

## Technology Stack

- **Java:** 21 (LTS)
- **Framework:** Spring Boot 3.3.5
- **Build Tool:** Maven 3.9+
- **Database:** PostgreSQL 16+
- **Cache:** Redis 7+
- **Network:** Netty 4.1+
- **Testing:** JUnit 5, Mockito, Testcontainers

## Prerequisites

- Java 21 or higher
- Maven 3.9+
- Docker & Docker Compose (optional, for containerized deployment)
- PostgreSQL 16+ (or use Docker)
- Redis 7+ (or use Docker)

## Quick Start

### Build the Project

```bash
cd dofus-packet-decoder
mvn clean install
```

### Run the Application

```bash
cd dofus-api
mvn spring-boot:run
```

Or run with specific profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Using Docker Compose

```bash
docker-compose up
```

## Configuration

Main configuration is in `dofus-api/src/main/resources/application.yml`:

```yaml
dofus:
  network:
    proxy:
      enabled: true
      port: 5555
      target-host: 34.251.172.139
      target-port: 443
```

## API Documentation

Once the application is running, access:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs
- Health Check: http://localhost:8080/actuator/health

## Development

### Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn clean test jacoco:report
```

### Code Coverage

Coverage reports are generated in `target/site/jacoco/index.html` for each module.

## Project Structure

```
dofus-packet-decoder/
├── pom.xml (parent)
├── README.md
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── dofus-network-core/
├── dofus-protocol/
├── dofus-packet-decoder/
├── dofus-game-state/
├── dofus-navigation/
├── dofus-combat/
├── dofus-api/
├── dofus-persistence/
└── dofus-web-ui/
```

## Contributing

1. Follow Java code conventions
2. Write unit tests for new features
3. Maintain 80%+ code coverage
4. Update documentation

## License

Educational and research purposes only. Respect Dofus Terms of Service.

## References

- [PRD Document](../PRD_JAVA_DOFUS_PACKET_DECODER.md)
- [Implementation Book](../IMPLEMENTATION_BOOK.md)
- [Dofus Retro Protocol Wiki](https://github.com/Geraxi-Allan/Wiki-Dofus/blob/master/pages/Protocole-reseau.md)
