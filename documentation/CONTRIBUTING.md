# Contributing to Dofus Retro Packet Decoder

Thank you for your interest in contributing to the Dofus Retro Packet Decoder project! This document provides guidelines and instructions for contributing.

---

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [How Can I Contribute?](#how-can-i-contribute)
3. [Development Setup](#development-setup)
4. [Coding Standards](#coding-standards)
5. [Commit Guidelines](#commit-guidelines)
6. [Pull Request Process](#pull-request-process)
7. [Testing Requirements](#testing-requirements)
8. [Documentation](#documentation)
9. [Community](#community)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive experience for everyone. We expect all contributors to:

- Be respectful and considerate
- Accept constructive criticism gracefully
- Focus on what is best for the community
- Show empathy towards other community members

### Unacceptable Behavior

- Harassment or discriminatory language
- Personal attacks or trolling
- Publishing others' private information
- Any conduct that could be considered inappropriate in a professional setting

### Enforcement

Violations of the code of conduct should be reported to the project maintainers. All complaints will be reviewed and investigated promptly and fairly.

---

## How Can I Contribute?

### Reporting Bugs

Before submitting a bug report:
1. **Check existing issues** to avoid duplicates
2. **Use the latest version** to verify the bug still exists
3. **Collect relevant information** (logs, stack traces, environment)

**Bug Report Template:**
```markdown
**Describe the bug**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected behavior**
A clear description of what you expected to happen.

**Actual behavior**
What actually happened.

**Environment:**
- OS: [e.g., Ubuntu 22.04]
- Java Version: [e.g., 26]
- Spring Boot Version: [e.g., 3.3.0]
- Branch/Commit: [e.g., main/abc123]

**Logs**
```
Paste relevant logs here
```

**Additional context**
Add any other context about the problem here.
```

### Suggesting Features

Before suggesting a feature:
1. **Check the roadmap** to see if it's already planned
2. **Search existing feature requests** to avoid duplicates
3. **Consider the scope** - does it fit the project goals?

**Feature Request Template:**
```markdown
**Is your feature request related to a problem?**
A clear description of the problem. Ex. I'm frustrated when [...]

**Describe the solution you'd like**
A clear and concise description of what you want to happen.

**Describe alternatives you've considered**
Any alternative solutions or features you've considered.

**Use cases**
Describe specific use cases for this feature.

**Additional context**
Add any other context, mockups, or examples.
```

### Contributing Code

We welcome code contributions! Here are some areas where you can help:

**Good First Issues:**
- Add new packet type implementations
- Improve error messages
- Add unit tests for existing code
- Fix documentation typos
- Improve logging

**Moderate Complexity:**
- Implement new combat strategies
- Add new API endpoints
- Improve pathfinding algorithms
- Add new WebSocket events
- Performance optimizations

**Advanced:**
- Implement encryption/decryption
- Add multi-account support
- Improve architecture
- Add machine learning features
- Implement plugin system

---

## Development Setup

### Prerequisites

1. **Install required software:**
   - Java 26 (JDK)
   - Maven 3.9+
   - Docker & Docker Compose
   - Git
   - Your favorite IDE (IntelliJ IDEA recommended)

2. **Fork and clone the repository:**
```bash
# Fork the repository on GitHub, then clone your fork
git clone https://github.com/YOUR-USERNAME/dofus-packet-decoder.git
cd dofus-packet-decoder

# Add upstream remote
git remote add upstream https://github.com/ORIGINAL-OWNER/dofus-packet-decoder.git
```

3. **Set up the development environment:**
```bash
# Start services (PostgreSQL, Redis)
docker-compose up -d

# Build the project
mvn clean install

# Run tests
mvn test

# Run the application
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### IDE Configuration

**IntelliJ IDEA:**
1. Open the project as a Maven project
2. Set JDK to Java 26
3. Enable Lombok annotation processing
4. Install plugins:
   - Lombok
   - Google Java Format
   - SonarLint

**Eclipse:**
1. Import as existing Maven project
2. Install Lombok plugin
3. Install Google Java Format plugin

### Running with Hot Reload

```bash
# Spring Boot DevTools enables hot reload
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Coding Standards

### Java Style Guide

We follow the **Google Java Style Guide** with minor modifications.

**Key Points:**
- **Indentation:** 2 spaces (no tabs)
- **Line length:** 100 characters
- **Imports:** No wildcards, organize by package
- **Braces:** K&R style (opening brace on same line)
- **Naming:**
  - Classes: `PascalCase`
  - Methods: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Packages: `lowercase`

**Example:**
```java
package com.dofus.protocol.packets;

import com.dofus.protocol.Packet;
import com.dofus.protocol.PacketId;
import java.time.LocalDateTime;
import java.util.List;

@Data
@PacketId("GDM")
public class GameDataMapPacket extends Packet {
  private static final int DEFAULT_MAP_SIZE = 20;

  private int mapId;
  private String mapDate;
  private List<Entity> entities;

  @Override
  public void decode(String rawData) {
    // Decode implementation
  }

  @Override
  public String encode() {
    return String.format("GDM|%d|%s", mapId, mapDate);
  }
}
```

### Code Formatting

**Format code before committing:**
```bash
# Format all code
mvn fmt:format

# Check formatting without modifying
mvn fmt:check
```

### Code Quality

**Run static analysis:**
```bash
# Checkstyle
mvn checkstyle:check

# SpotBugs
mvn spotbugs:check

# PMD
mvn pmd:check
```

### Lombok Usage

We use Lombok to reduce boilerplate:

```java
@Data                  // Generates getters, setters, toString, equals, hashCode
@Builder               // Builder pattern
@NoArgsConstructor     // Default constructor
@AllArgsConstructor    // All-args constructor
@Slf4j                 // Logger field
```

**Avoid overusing Lombok in:**
- Entities with complex relationships
- Classes with custom logic in getters/setters
- Public APIs (prefer explicit methods)

---

## Commit Guidelines

### Commit Message Format

We follow **Conventional Commits** specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or modifying tests
- `chore`: Build process or auxiliary tool changes
- `ci`: CI/CD changes

**Examples:**
```bash
# Feature
feat(packets): add support for inventory packets (OT, OA, OR)

# Bug fix
fix(navigation): correct A* heuristic calculation for diagonal movement

# Documentation
docs(api): update REST API examples with authentication

# Refactoring
refactor(decoder): simplify packet registry initialization logic

# Performance
perf(cache): implement Redis caching for map data

# Tests
test(combat): add unit tests for aggressive strategy

# Chore
chore(deps): update Spring Boot to 3.3.1
```

### Writing Good Commits

**Do:**
- Use imperative mood ("add" not "added")
- Capitalize first letter
- No period at end of subject
- Limit subject line to 50 characters
- Wrap body at 72 characters
- Explain *what* and *why*, not *how*

**Don't:**
- Mix multiple unrelated changes
- Include WIP or debug code
- Leave commented-out code
- Commit generated files (IDE configs, build artifacts)

### Commit Signing

We recommend signing commits with GPG:

```bash
# Configure Git to sign commits
git config --global user.signingkey YOUR_GPG_KEY_ID
git config --global commit.gpgsign true

# Commit with signature
git commit -S -m "feat(packets): add new packet type"
```

---

## Pull Request Process

### Before Submitting

**Checklist:**
- [ ] Code follows project style guide
- [ ] All tests pass (`mvn clean verify`)
- [ ] New code is covered by tests (80%+ coverage)
- [ ] Documentation is updated
- [ ] Commit messages follow convention
- [ ] Branch is up-to-date with main
- [ ] No merge conflicts

### Creating a Pull Request

1. **Create a feature branch:**
```bash
git checkout -b feature/my-awesome-feature
```

2. **Make your changes and commit:**
```bash
git add .
git commit -m "feat(navigation): add multi-map pathfinding"
```

3. **Push to your fork:**
```bash
git push origin feature/my-awesome-feature
```

4. **Open a Pull Request** on GitHub with this template:

```markdown
## Description
Brief description of changes.

## Type of Change
- [ ] Bug fix (non-breaking change)
- [ ] New feature (non-breaking change)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## Related Issues
Fixes #123
Relates to #456

## Changes Made
- Added X feature
- Fixed Y bug
- Refactored Z component

## Testing
Describe tests you ran:
- [ ] Unit tests
- [ ] Integration tests
- [ ] Manual testing

## Screenshots (if applicable)
Add screenshots for UI changes.

## Checklist
- [ ] My code follows the style guidelines
- [ ] I have performed a self-review
- [ ] I have commented my code where necessary
- [ ] I have updated documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests
- [ ] New and existing tests pass
- [ ] Any dependent changes have been merged
```

### Review Process

1. **Automated checks** must pass:
   - Build must succeed
   - All tests must pass
   - Code coverage must be maintained
   - Code quality checks must pass

2. **Peer review** by maintainer(s):
   - At least 1 approval required
   - Address all review comments
   - Re-request review after changes

3. **Merge:**
   - Squash and merge (default)
   - Rebase and merge (for clean history)
   - Merge commit (for feature branches)

### After Merge

1. **Delete your branch:**
```bash
git branch -d feature/my-awesome-feature
git push origin --delete feature/my-awesome-feature
```

2. **Update your local repository:**
```bash
git checkout main
git pull upstream main
```

---

## Testing Requirements

### Test Coverage

**Minimum Requirements:**
- Overall coverage: 80%
- New code coverage: 90%
- Critical paths: 100%

**Check coverage:**
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### Unit Tests

**Guidelines:**
- One test class per production class
- Name tests clearly: `shouldReturnTrueWhenConditionMet()`
- Use AAA pattern (Arrange, Act, Assert)
- Mock external dependencies
- Test edge cases and error conditions

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class PacketDecoderServiceTest {

  @Mock
  private PacketRegistry registry;

  @InjectMocks
  private PacketDecoderService decoder;

  @Test
  void shouldDecodeValidGDMPacket() {
    // Arrange
    byte[] rawData = "GDM|432|2025-11-08".getBytes();
    when(registry.get("GDM")).thenReturn(GameDataMapPacket.class);

    // Act
    Packet packet = decoder.decode(rawData);

    // Assert
    assertThat(packet).isInstanceOf(GameDataMapPacket.class);
    GameDataMapPacket gdm = (GameDataMapPacket) packet;
    assertThat(gdm.getMapId()).isEqualTo(432);
  }

  @Test
  void shouldThrowExceptionForInvalidPacket() {
    // Arrange
    byte[] rawData = "INVALID".getBytes();

    // Act & Assert
    assertThatThrownBy(() -> decoder.decode(rawData))
        .isInstanceOf(PacketDecodingException.class)
        .hasMessageContaining("Unknown packet type");
  }
}
```

### Integration Tests

**Use Testcontainers** for database and Redis:

```java
@SpringBootTest
@Testcontainers
class PacketLogRepositoryIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  @Autowired
  private PacketLogRepository repository;

  @Test
  void shouldSaveAndRetrievePacket() {
    // Arrange
    PacketLog packet = new PacketLog();
    packet.setPacketType("GDM");
    packet.setRawData("test".getBytes());

    // Act
    PacketLog saved = repository.save(packet);
    Optional<PacketLog> retrieved = repository.findById(saved.getId());

    // Assert
    assertThat(retrieved).isPresent();
    assertThat(retrieved.get().getPacketType()).isEqualTo("GDM");
  }
}
```

### Performance Tests

**Use JMH for benchmarks:**

```java
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class PathfindingBenchmark {

  private PathfindingService pathfinder;
  private MapData testMap;

  @Setup
  public void setup() {
    pathfinder = new PathfindingService();
    testMap = createTestMap(50, 50);
  }

  @Benchmark
  public Path benchmarkAStarPathfinding() {
    return pathfinder.findPath(
        new Position(0, 0),
        new Position(49, 49),
        testMap
    );
  }
}
```

---

## Documentation

### Code Documentation

**Javadoc Requirements:**
- All public classes and interfaces
- All public methods
- Complex private methods
- Package-level documentation

**Example:**
```java
/**
 * Service for decoding Dofus Retro network packets.
 *
 * <p>This service uses a registry of packet types to dynamically
 * decode raw byte arrays into strongly-typed packet objects.
 *
 * <p>Example usage:
 * <pre>{@code
 * PacketDecoderService decoder = new PacketDecoderService(registry);
 * Packet packet = decoder.decode(rawBytes);
 * if (packet instanceof GameDataMapPacket) {
 *   // Handle map packet
 * }
 * }</pre>
 *
 * @author Your Name
 * @since 1.0.0
 * @see PacketRegistry
 * @see Packet
 */
@Service
public class PacketDecoderService implements PacketDecoder {

  /**
   * Decodes raw packet bytes into a typed packet object.
   *
   * @param rawData the raw packet bytes to decode
   * @return the decoded packet
   * @throws PacketDecodingException if the packet cannot be decoded
   * @throws IllegalArgumentException if rawData is null or empty
   */
  @Override
  public Packet decode(byte[] rawData) throws PacketDecodingException {
    // Implementation
  }
}
```

### Markdown Documentation

**Update relevant docs when:**
- Adding new features
- Changing APIs
- Modifying configuration
- Adding dependencies

**Documentation files:**
- `README.md` - Project overview
- `ARCHITECTURE.md` - System architecture
- `API_GUIDE.md` - API documentation
- `DEPLOYMENT_GUIDE.md` - Deployment instructions
- `CONTRIBUTING.md` - This file

### API Documentation

**Use OpenAPI annotations:**

```java
@RestController
@RequestMapping("/api/v1/game")
@Tag(name = "Game State", description = "Game state management endpoints")
public class GameStateController {

  @Operation(
      summary = "Get current game state",
      description = "Retrieves the complete current game state for the authenticated player"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful operation"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Player not found")
  })
  @GetMapping("/state")
  public ResponseEntity<PlayerStateDTO> getCurrentState(
      @Parameter(description = "Player ID (optional, admin only)")
      @RequestParam(required = false) String playerId
  ) {
    // Implementation
  }
}
```

---

## Community

### Communication Channels

- **GitHub Issues:** Bug reports and feature requests
- **GitHub Discussions:** General questions and discussions
- **Discord:** Real-time chat (link in README)
- **Email:** For security issues only

### Getting Help

**Before asking for help:**
1. Read the documentation
2. Search existing issues and discussions
3. Check the FAQ

**When asking for help:**
- Provide context and details
- Include error messages and logs
- Describe what you've already tried
- Be patient and respectful

### Recognition

We appreciate all contributions! Contributors will be:
- Listed in the CONTRIBUTORS file
- Mentioned in release notes
- Given credit in documentation

---

## Release Process

### Versioning

We use **Semantic Versioning** (SemVer):
- **MAJOR:** Breaking changes
- **MINOR:** New features (backward compatible)
- **PATCH:** Bug fixes

**Example:** `1.2.3`
- `1` = Major version
- `2` = Minor version
- `3` = Patch version

### Release Checklist

1. [ ] Update version in `pom.xml`
2. [ ] Update CHANGELOG.md
3. [ ] Run full test suite
4. [ ] Build and test Docker image
5. [ ] Tag release in Git
6. [ ] Push to GitHub
7. [ ] Create GitHub release
8. [ ] Deploy to production
9. [ ] Announce release

---

## Questions?

If you have questions not covered in this guide:

1. **Check existing documentation:**
   - [README.md](README.md)
   - [ARCHITECTURE.md](ARCHITECTURE.md)
   - [API_GUIDE.md](API_GUIDE.md)

2. **Search GitHub Issues and Discussions**

3. **Ask in Discord** (see README for invite link)

4. **Open a GitHub Discussion** for community help

---

## Thank You!

Thank you for contributing to Dofus Retro Packet Decoder! Your contributions help make this project better for everyone.

**Happy coding!**

---

**Document Version:** 1.0
**Last Updated:** 2025-11-08
