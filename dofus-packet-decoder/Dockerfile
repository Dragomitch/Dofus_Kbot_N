# Multi-stage build for Dofus Packet Decoder

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy parent POM and all module POMs
COPY pom.xml .
COPY dofus-network-core/pom.xml dofus-network-core/
COPY dofus-protocol/pom.xml dofus-protocol/
COPY dofus-packet-decoder/pom.xml dofus-packet-decoder/
COPY dofus-game-state/pom.xml dofus-game-state/
COPY dofus-navigation/pom.xml dofus-navigation/
COPY dofus-combat/pom.xml dofus-combat/
COPY dofus-persistence/pom.xml dofus-persistence/
COPY dofus-api/pom.xml dofus-api/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY dofus-network-core/src dofus-network-core/src
COPY dofus-protocol/src dofus-protocol/src
COPY dofus-packet-decoder/src dofus-packet-decoder/src
COPY dofus-game-state/src dofus-game-state/src
COPY dofus-navigation/src dofus-navigation/src
COPY dofus-combat/src dofus-combat/src
COPY dofus-persistence/src dofus-persistence/src
COPY dofus-api/src dofus-api/src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root user
RUN groupadd -r dofus && useradd -r -g dofus dofus

# Copy JAR from build stage
COPY --from=build /app/dofus-api/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R dofus:dofus /app

# Switch to non-root user
USER dofus

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
