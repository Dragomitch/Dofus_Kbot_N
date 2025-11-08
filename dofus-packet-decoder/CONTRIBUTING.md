# Contributing to Dofus Packet Decoder

Thank you for your interest in contributing to the Dofus Packet Decoder project!

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- Docker & Docker Compose (optional)
- Git

### Setting Up Development Environment

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/dofus-packet-decoder.git
   cd dofus-packet-decoder
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run tests**
   ```bash
   mvn test
   ```

4. **Start the application**
   ```bash
   cd dofus-api
   mvn spring-boot:run
   ```

## Code Style

### Java Code Conventions

- Follow standard Java naming conventions
- Use meaningful variable and method names
- Keep methods short and focused (single responsibility)
- Write JavaDoc for public APIs
- Use Lombok annotations where appropriate

### Code Formatting

- Indentation: 4 spaces
- Line length: 120 characters max
- Use checkstyle for validation: `mvn checkstyle:check`

## Testing

### Writing Tests

- Write unit tests for all new functionality
- Aim for 80%+ code coverage
- Use meaningful test names (should_doSomething_when_condition)
- Use JUnit 5 and Mockito

### Running Tests

```bash
# Unit tests
mvn test

# Integration tests
mvn verify -Pintegration-tests

# Coverage report
mvn clean test jacoco:report
```

## Commit Messages

Follow conventional commits format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

Examples:
```
feat(decoder): add support for GDM packet type

fix(network): resolve connection timeout issue

docs(readme): update installation instructions
```

## Pull Request Process

1. Create a feature branch from `develop`
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes and commit
   ```bash
   git add .
   git commit -m "feat: your feature description"
   ```

3. Push to your fork
   ```bash
   git push origin feature/your-feature-name
   ```

4. Create a Pull Request to `develop` branch

5. Ensure all CI checks pass

6. Request review from maintainers

### PR Checklist

- [ ] Code builds successfully
- [ ] All tests pass
- [ ] Code coverage maintained or improved
- [ ] Documentation updated
- [ ] Commit messages follow conventions
- [ ] No merge conflicts

## Module Structure

When adding new functionality, follow the module architecture:

- `dofus-network-core`: Low-level network operations
- `dofus-protocol`: Packet definitions and DTOs
- `dofus-packet-decoder`: Parsing and decoding logic
- `dofus-game-state`: State management
- `dofus-navigation`: Pathfinding algorithms
- `dofus-combat`: Combat logic
- `dofus-persistence`: Database entities
- `dofus-api`: REST endpoints and controllers

## License

By contributing, you agree that your contributions will be licensed under the project's license.

## Questions?

Feel free to open an issue for questions or discussion.
