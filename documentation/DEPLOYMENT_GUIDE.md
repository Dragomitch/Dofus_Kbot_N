# Deployment Guide
## Dofus Retro Packet Decoder

**Version:** 1.0
**Last Updated:** 2025-11-08

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Local Deployment](#local-deployment)
4. [Docker Deployment](#docker-deployment)
5. [Docker Compose Deployment](#docker-compose-deployment)
6. [Kubernetes Deployment](#kubernetes-deployment)
7. [Cloud Deployment](#cloud-deployment)
8. [Configuration](#configuration)
9. [Monitoring & Logging](#monitoring--logging)
10. [Troubleshooting](#troubleshooting)
11. [Security Considerations](#security-considerations)

---

## Overview

This guide covers various deployment strategies for the Dofus Retro Packet Decoder application, from local development to production cloud deployments.

### Deployment Options

| Option | Complexity | Best For | Scalability |
|--------|-----------|----------|-------------|
| Local JAR | Low | Development, testing | Single instance |
| Docker | Medium | Development, small deployments | Single instance |
| Docker Compose | Medium | Development, small production | Multi-service |
| Kubernetes | High | Production, enterprise | Highly scalable |
| Cloud (AWS/Azure/GCP) | High | Production, managed services | Highly scalable |

---

## Prerequisites

### Required Software

- **Java 26 JDK** (for JAR deployment)
- **Docker 24+** (for containerized deployments)
- **Docker Compose 2.0+** (for multi-container deployments)
- **kubectl** (for Kubernetes deployments)
- **PostgreSQL 16+** (if not using Docker)
- **Redis 7+** (if not using Docker)

### System Requirements

**Minimum (Development):**
- CPU: 2 cores
- RAM: 4 GB
- Disk: 10 GB
- Network: 10 Mbps

**Recommended (Production):**
- CPU: 4 cores
- RAM: 8 GB
- Disk: 50 GB (with monitoring)
- Network: 100 Mbps

---

## Local Deployment

### Option 1: Run as JAR

1. **Build the application:**
```bash
mvn clean package -DskipTests
```

2. **Set up PostgreSQL:**
```bash
# Install PostgreSQL
sudo apt install postgresql-16  # Ubuntu/Debian
brew install postgresql@16      # macOS

# Start PostgreSQL
sudo systemctl start postgresql

# Create database
sudo -u postgres psql
CREATE DATABASE dofus_decoder;
CREATE USER dofus WITH PASSWORD 'changeme';
GRANT ALL PRIVILEGES ON DATABASE dofus_decoder TO dofus;
\q
```

3. **Set up Redis:**
```bash
# Install Redis
sudo apt install redis-server  # Ubuntu/Debian
brew install redis             # macOS

# Start Redis
redis-server
```

4. **Configure application:**

Create `application-local.yml`:
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

dofus:
  network:
    proxy:
      port: 5555
      target-host: 34.251.172.139
      target-port: 443
```

5. **Run the application:**
```bash
java -jar target/dofus-packet-decoder-1.0.0.jar \
  --spring.profiles.active=local
```

6. **Verify deployment:**
```bash
# Check health
curl http://localhost:8080/actuator/health

# Test API
curl http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'
```

### Option 2: Run with Maven

```bash
# Start services (if using Docker for services only)
docker-compose up -d postgres redis

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## Docker Deployment

### Build Docker Image

1. **Create Dockerfile:**

```Dockerfile
# File: Dockerfile
FROM eclipse-temurin:26-jdk-alpine AS builder

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source
COPY src src

# Build application
RUN ./mvnw package -DskipTests

# Runtime stage
FROM eclipse-temurin:26-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 -S dofus && \
    adduser -u 1001 -S dofus -G dofus

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown -R dofus:dofus /app

USER dofus

# Expose ports
EXPOSE 8080 5555

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

2. **Build image:**
```bash
docker build -t dofus-packet-decoder:latest .

# With specific version
docker build -t dofus-packet-decoder:1.0.0 .

# Multi-platform build
docker buildx build --platform linux/amd64,linux/arm64 \
  -t dofus-packet-decoder:latest .
```

3. **Run container:**
```bash
docker run -d \
  --name dofus-decoder \
  -p 8080:8080 \
  -p 5555:5555 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/dofus_decoder \
  -e SPRING_DATASOURCE_USERNAME=dofus \
  -e SPRING_DATASOURCE_PASSWORD=changeme \
  -e SPRING_DATA_REDIS_HOST=host.docker.internal \
  -e SPRING_DATA_REDIS_PORT=6379 \
  --restart unless-stopped \
  dofus-packet-decoder:latest
```

4. **View logs:**
```bash
docker logs -f dofus-decoder
```

5. **Stop container:**
```bash
docker stop dofus-decoder
docker rm dofus-decoder
```

### Docker Image Optimization

**Multi-stage build with layer caching:**
```Dockerfile
# Cache Maven dependencies separately
FROM maven:3.9-eclipse-temurin-26 AS dependencies
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

# Build application
FROM dependencies AS builder
COPY src src
RUN mvn package -DskipTests

# Minimal runtime
FROM eclipse-temurin:26-jre-alpine
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Docker Compose Deployment

### Development Configuration

**File: docker-compose.yml**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: dofus-postgres
    environment:
      POSTGRES_DB: dofus_decoder
      POSTGRES_USER: dofus
      POSTGRES_PASSWORD: changeme
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U dofus"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: dofus-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: dofus-decoder
    ports:
      - "8080:8080"
      - "5555:5555"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/dofus_decoder
      SPRING_DATASOURCE_USERNAME: dofus
      SPRING_DATASOURCE_PASSWORD: changeme
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:
```

**Commands:**
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v

# Rebuild and restart app
docker-compose up -d --build app
```

### Production Configuration

**File: docker-compose.prod.yml**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: dofus-postgres-prod
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backup:/backup
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER}"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: dofus-redis-prod
    command: redis-server --requirepass ${REDIS_PASSWORD} --appendonly yes --maxmemory 512mb --maxmemory-policy allkeys-lru
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
    healthcheck:
      test: ["CMD", "redis-cli", "--no-auth-warning", "-a", "${REDIS_PASSWORD}", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  app:
    image: dofus-packet-decoder:${VERSION:-latest}
    container_name: dofus-decoder-prod
    ports:
      - "8080:8080"
      - "5555:5555"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
      SPRING_DATA_REDIS_PASSWORD: ${REDIS_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION:-3600}
      JAVA_OPTS: "-Xms512m -Xmx2g -XX:+UseG1GC"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    deploy:
      resources:
        limits:
          cpus: '4'
          memory: 4G
        reservations:
          cpus: '2'
          memory: 2G
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  nginx:
    image: nginx:alpine
    container_name: dofus-nginx-prod
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
    depends_on:
      - app
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M

volumes:
  postgres_data:
  redis_data:
```

**Environment file (.env):**
```bash
# Database
POSTGRES_DB=dofus_decoder
POSTGRES_USER=dofus
POSTGRES_PASSWORD=secure-password-here

# Redis
REDIS_PASSWORD=secure-redis-password

# Application
VERSION=1.0.0
JWT_SECRET=your-secret-key-minimum-256-bits
JWT_EXPIRATION=3600
```

**Run production:**
```bash
# Load environment variables
source .env

# Start production stack
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f app
```

### Nginx Configuration

**File: nginx/nginx.conf**
```nginx
events {
    worker_connections 1024;
}

http {
    upstream backend {
        server app:8080;
    }

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=100r/m;
    limit_req_zone $binary_remote_addr zone=auth_limit:10m rate=10r/m;

    server {
        listen 80;
        server_name dofus-decoder.example.com;

        # Redirect HTTP to HTTPS
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name dofus-decoder.example.com;

        # SSL configuration
        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers HIGH:!aNULL:!MD5;

        # Gzip compression
        gzip on;
        gzip_types text/plain text/css application/json application/javascript;

        # API endpoints
        location /api/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;

            # Rate limiting
            limit_req zone=api_limit burst=20 nodelay;
        }

        # Auth endpoints (stricter rate limiting)
        location /api/v1/auth/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

            limit_req zone=auth_limit burst=5 nodelay;
        }

        # WebSocket
        location /ws/ {
            proxy_pass http://backend;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }

        # Health check
        location /actuator/health {
            proxy_pass http://backend;
            access_log off;
        }

        # Static files (frontend)
        location / {
            root /usr/share/nginx/html;
            try_files $uri $uri/ /index.html;
        }
    }
}
```

---

## Kubernetes Deployment

### Prerequisites

```bash
# Install kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Verify installation
kubectl version --client
```

### Namespace

**File: k8s/namespace.yaml**
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: dofus-decoder
```

### ConfigMap

**File: k8s/configmap.yaml**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: dofus-decoder-config
  namespace: dofus-decoder
data:
  application.yml: |
    spring:
      datasource:
        url: jdbc:postgresql://postgres-service:5432/dofus_decoder
        username: dofus
      data:
        redis:
          host: redis-service
          port: 6379
    dofus:
      network:
        proxy:
          port: 5555
          target-host: 34.251.172.139
          target-port: 443
    server:
      port: 8080
```

### Secrets

**File: k8s/secrets.yaml**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: dofus-decoder-secrets
  namespace: dofus-decoder
type: Opaque
stringData:
  postgres-password: changeme
  redis-password: changeme
  jwt-secret: your-secret-key-here
```

**Create from command line:**
```bash
kubectl create secret generic dofus-decoder-secrets \
  --from-literal=postgres-password='secure-password' \
  --from-literal=redis-password='secure-redis-password' \
  --from-literal=jwt-secret='your-jwt-secret' \
  -n dofus-decoder
```

### PostgreSQL Deployment

**File: k8s/postgres-deployment.yaml**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: dofus-decoder
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:16-alpine
        ports:
        - containerPort: 5432
        env:
        - name: POSTGRES_DB
          value: dofus_decoder
        - name: POSTGRES_USER
          value: dofus
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: dofus-decoder-secrets
              key: postgres-password
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
      volumes:
      - name: postgres-storage
        persistentVolumeClaim:
          claimName: postgres-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: postgres-service
  namespace: dofus-decoder
spec:
  selector:
    app: postgres
  ports:
  - port: 5432
    targetPort: 5432
  type: ClusterIP
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: dofus-decoder
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
```

### Redis Deployment

**File: k8s/redis-deployment.yaml**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: dofus-decoder
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        command:
        - redis-server
        - --requirepass
        - $(REDIS_PASSWORD)
        - --appendonly
        - "yes"
        env:
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: dofus-decoder-secrets
              key: redis-password
        volumeMounts:
        - name: redis-storage
          mountPath: /data
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
      volumes:
      - name: redis-storage
        persistentVolumeClaim:
          claimName: redis-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
  namespace: dofus-decoder
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
  type: ClusterIP
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: redis-pvc
  namespace: dofus-decoder
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi
```

### Application Deployment

**File: k8s/app-deployment.yaml**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: dofus-decoder
  namespace: dofus-decoder
spec:
  replicas: 3
  selector:
    matchLabels:
      app: dofus-decoder
  template:
    metadata:
      labels:
        app: dofus-decoder
    spec:
      containers:
      - name: dofus-decoder
        image: dofus-packet-decoder:1.0.0
        ports:
        - containerPort: 8080
          name: http
        - containerPort: 5555
          name: proxy
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: dofus-decoder-secrets
              key: postgres-password
        - name: SPRING_DATA_REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: dofus-decoder-secrets
              key: redis-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: dofus-decoder-secrets
              key: jwt-secret
        - name: JAVA_OPTS
          value: "-Xms512m -Xmx2g -XX:+UseContainerSupport"
        volumeMounts:
        - name: config
          mountPath: /app/config
          readOnly: true
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "1Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "4000m"
      volumes:
      - name: config
        configMap:
          name: dofus-decoder-config
---
apiVersion: v1
kind: Service
metadata:
  name: dofus-decoder-service
  namespace: dofus-decoder
spec:
  selector:
    app: dofus-decoder
  ports:
  - name: http
    port: 80
    targetPort: 8080
  - name: proxy
    port: 5555
    targetPort: 5555
  type: LoadBalancer
```

### Ingress

**File: k8s/ingress.yaml**
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: dofus-decoder-ingress
  namespace: dofus-decoder
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/rate-limit: "100"
spec:
  tls:
  - hosts:
    - dofus-decoder.example.com
    secretName: dofus-decoder-tls
  rules:
  - host: dofus-decoder.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: dofus-decoder-service
            port:
              number: 80
```

### Deploy to Kubernetes

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Create ConfigMap and Secrets
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml

# Deploy PostgreSQL
kubectl apply -f k8s/postgres-deployment.yaml

# Deploy Redis
kubectl apply -f k8s/redis-deployment.yaml

# Wait for databases to be ready
kubectl wait --for=condition=ready pod -l app=postgres -n dofus-decoder --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n dofus-decoder --timeout=300s

# Deploy application
kubectl apply -f k8s/app-deployment.yaml

# Deploy Ingress
kubectl apply -f k8s/ingress.yaml

# Check status
kubectl get all -n dofus-decoder

# View logs
kubectl logs -f deployment/dofus-decoder -n dofus-decoder
```

### Horizontal Pod Autoscaler

**File: k8s/hpa.yaml**
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: dofus-decoder-hpa
  namespace: dofus-decoder
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: dofus-decoder
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

---

## Cloud Deployment

### AWS Deployment (ECS)

**Prerequisites:**
- AWS CLI installed and configured
- ECR repository created
- ECS cluster created

**Push image to ECR:**
```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin 123456789012.dkr.ecr.us-east-1.amazonaws.com

# Tag image
docker tag dofus-packet-decoder:latest \
  123456789012.dkr.ecr.us-east-1.amazonaws.com/dofus-decoder:latest

# Push image
docker push 123456789012.dkr.ecr.us-east-1.amazonaws.com/dofus-decoder:latest
```

**Task Definition (task-definition.json):**
```json
{
  "family": "dofus-decoder",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "2048",
  "memory": "4096",
  "containerDefinitions": [
    {
      "name": "dofus-decoder",
      "image": "123456789012.dkr.ecr.us-east-1.amazonaws.com/dofus-decoder:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        }
      ],
      "secrets": [
        {
          "name": "SPRING_DATASOURCE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789012:secret:db-password"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/dofus-decoder",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

**Deploy to ECS:**
```bash
# Register task definition
aws ecs register-task-definition --cli-input-json file://task-definition.json

# Create service
aws ecs create-service \
  --cluster dofus-cluster \
  --service-name dofus-decoder-service \
  --task-definition dofus-decoder \
  --desired-count 3 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-12345],securityGroups=[sg-12345],assignPublicIp=ENABLED}"
```

### Azure Deployment (AKS)

```bash
# Login to Azure
az login

# Create resource group
az group create --name dofus-decoder-rg --location eastus

# Create AKS cluster
az aks create \
  --resource-group dofus-decoder-rg \
  --name dofus-decoder-aks \
  --node-count 3 \
  --enable-addons monitoring \
  --generate-ssh-keys

# Get credentials
az aks get-credentials --resource-group dofus-decoder-rg --name dofus-decoder-aks

# Deploy using kubectl
kubectl apply -f k8s/
```

### GCP Deployment (GKE)

```bash
# Create GKE cluster
gcloud container clusters create dofus-decoder-cluster \
  --num-nodes=3 \
  --machine-type=n1-standard-2 \
  --zone=us-central1-a

# Get credentials
gcloud container clusters get-credentials dofus-decoder-cluster --zone=us-central1-a

# Deploy using kubectl
kubectl apply -f k8s/
```

---

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_PROFILES_ACTIVE` | Active profile (dev, prod) | dev | Yes |
| `SPRING_DATASOURCE_URL` | PostgreSQL URL | - | Yes |
| `SPRING_DATASOURCE_USERNAME` | Database username | dofus | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database password | - | Yes |
| `SPRING_DATA_REDIS_HOST` | Redis host | localhost | Yes |
| `SPRING_DATA_REDIS_PORT` | Redis port | 6379 | No |
| `SPRING_DATA_REDIS_PASSWORD` | Redis password | - | No |
| `JWT_SECRET` | JWT signing key | - | Yes |
| `JWT_EXPIRATION` | Token expiration (seconds) | 3600 | No |
| `DOFUS_NETWORK_PROXY_PORT` | Proxy port | 5555 | No |
| `DOFUS_NETWORK_PROXY_TARGET_HOST` | Target server host | - | Yes |
| `DOFUS_NETWORK_PROXY_TARGET_PORT` | Target server port | 443 | No |

### Production Configuration Checklist

- [ ] Change default passwords
- [ ] Generate secure JWT secret (256+ bits)
- [ ] Enable HTTPS/TLS
- [ ] Configure firewall rules
- [ ] Set up backups
- [ ] Enable monitoring
- [ ] Configure logging
- [ ] Set resource limits
- [ ] Enable health checks
- [ ] Configure rate limiting

---

## Monitoring & Logging

### Spring Boot Actuator

**Enable endpoints in application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

**Access endpoints:**
```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

### Prometheus + Grafana

**docker-compose-monitoring.yml:**
```yaml
services:
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
      - ./grafana/dashboards:/etc/grafana/provisioning/dashboards

volumes:
  prometheus_data:
  grafana_data:
```

### Logging

**Structured logging configuration:**
```yaml
logging:
  level:
    root: INFO
    com.dofus: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/dofus-decoder/application.log
    max-size: 10MB
    max-history: 30
```

---

## Troubleshooting

### Common Issues

**Application won't start:**
```bash
# Check logs
docker logs dofus-decoder

# Common causes:
# - Database not accessible
# - Incorrect configuration
# - Port already in use

# Verify database connection
docker exec -it dofus-decoder bash
wget --spider http://postgres:5432
```

**High memory usage:**
```bash
# Check memory settings
docker stats dofus-decoder

# Adjust JVM heap size
-e JAVA_OPTS="-Xms512m -Xmx2g"
```

**Slow performance:**
```bash
# Check resource usage
kubectl top pods -n dofus-decoder

# Scale horizontally
kubectl scale deployment dofus-decoder --replicas=5 -n dofus-decoder
```

### Debug Mode

```bash
# Enable debug logging
-e LOGGING_LEVEL_COM_DOFUS=DEBUG

# Remote debugging
-e JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

---

## Security Considerations

### Secrets Management

- Use Kubernetes Secrets or AWS Secrets Manager
- Never commit secrets to Git
- Rotate credentials regularly
- Use strong, unique passwords

### Network Security

- Enable HTTPS/TLS
- Configure firewall rules
- Use VPC/private networks
- Implement rate limiting

### Application Security

- Keep dependencies up to date
- Enable security headers
- Configure CORS properly
- Use JWT with short expiration

---

**END OF DEPLOYMENT GUIDE**

**Document Version:** 1.0
**Last Updated:** 2025-11-08

For questions or issues, please refer to the [main documentation](README.md) or open an issue on GitHub.
