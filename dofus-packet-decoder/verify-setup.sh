#!/bin/bash

# Dofus Packet Decoder - Setup Verification Script
# This script verifies that the project setup is complete and correct

set -e

echo "====================================="
echo "Dofus Packet Decoder Setup Verification"
echo "====================================="
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check functions
check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $1 is installed (version: $($1 --version | head -n1))"
        return 0
    else
        echo -e "${RED}✗${NC} $1 is not installed"
        return 1
    fi
}

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1 exists"
        return 0
    else
        echo -e "${RED}✗${NC} $1 is missing"
        return 1
    fi
}

check_directory() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $1 exists"
        return 0
    else
        echo -e "${RED}✗${NC} $1 is missing"
        return 1
    fi
}

# Prerequisites
echo "Checking Prerequisites..."
echo "------------------------"
check_command java
check_command mvn
check_command docker
check_command docker-compose
echo ""

# Project Structure
echo "Checking Project Structure..."
echo "----------------------------"
check_file "pom.xml"
check_file "README.md"
check_file ".gitignore"
check_file "Dockerfile"
check_file "docker-compose.yml"
check_file "Makefile"
echo ""

# Modules
echo "Checking Modules..."
echo "-------------------"
check_directory "dofus-network-core"
check_directory "dofus-protocol"
check_directory "dofus-packet-decoder"
check_directory "dofus-game-state"
check_directory "dofus-navigation"
check_directory "dofus-combat"
check_directory "dofus-persistence"
check_directory "dofus-api"
echo ""

# Module POMs
echo "Checking Module POMs..."
echo "----------------------"
check_file "dofus-network-core/pom.xml"
check_file "dofus-protocol/pom.xml"
check_file "dofus-packet-decoder/pom.xml"
check_file "dofus-game-state/pom.xml"
check_file "dofus-navigation/pom.xml"
check_file "dofus-combat/pom.xml"
check_file "dofus-persistence/pom.xml"
check_file "dofus-api/pom.xml"
echo ""

# Spring Boot Configuration
echo "Checking Spring Boot Configuration..."
echo "------------------------------------"
check_file "dofus-api/src/main/java/com/dofus/api/DofusPacketDecoderApplication.java"
check_file "dofus-api/src/main/resources/application.yml"
check_file "dofus-api/src/main/resources/application-dev.yml"
check_file "dofus-api/src/main/resources/application-test.yml"
check_file "dofus-api/src/main/resources/application-prod.yml"
check_file "dofus-api/src/main/resources/logback-spring.xml"
echo ""

# Controllers
echo "Checking Controllers..."
echo "----------------------"
check_file "dofus-api/src/main/java/com/dofus/api/controller/HealthController.java"
check_file "dofus-api/src/main/java/com/dofus/api/controller/GameStateController.java"
echo ""

# Configuration Classes
echo "Checking Configuration Classes..."
echo "--------------------------------"
check_file "dofus-api/src/main/java/com/dofus/api/config/DofusConfiguration.java"
check_file "dofus-api/src/main/java/com/dofus/api/config/WebConfig.java"
check_file "dofus-api/src/main/java/com/dofus/api/config/OpenApiConfig.java"
echo ""

# Docker
echo "Checking Docker Files..."
echo "-----------------------"
check_file "Dockerfile"
check_file "docker-compose.yml"
check_file "docker-compose.dev.yml"
check_file ".dockerignore"
check_file "docker/postgres/init/01-init.sql"
echo ""

# CI/CD
echo "Checking CI/CD Configuration..."
echo "------------------------------"
check_file ".github/workflows/build.yml"
check_file ".github/workflows/release.yml"
check_file ".github/workflows/dependency-update.yml"
check_file ".github/workflows/security-scan.yml"
check_file ".github/dependabot.yml"
echo ""

# Documentation
echo "Checking Documentation..."
echo "------------------------"
check_file "README.md"
check_file "CONTRIBUTING.md"
check_file "QUICKSTART.md"
echo ""

# Summary
echo ""
echo "====================================="
echo "Verification Complete!"
echo "====================================="
echo ""
echo "Next steps:"
echo "1. Ensure network connectivity for Maven dependencies"
echo "2. Run: make install  (or mvn clean install)"
echo "3. Run: make docker-up  (or docker-compose up)"
echo "4. Access: http://localhost:8080/swagger-ui.html"
echo ""
echo "For quick start, see: QUICKSTART.md"
echo ""
