# Product Requirements Document (PRD)
## Dofus Retro Packet Decoder & Navigation System
### Java 26 + Spring Boot Implementation

---

## Document Information

**Version:** 1.0
**Date:** 2025-11-08
**Target Platform:** Java 26, Spring Boot 3.x
**Source Project:** Dofus_Kbot_N (Python)
**Author:** Based on analysis of existing Python automation project

---

## Executive Summary

This PRD outlines the requirements for building a **Dofus Retro Packet Decoder and Navigation System** in Java 26 with Spring Boot. The goal is to create a robust, enterprise-grade application that intercepts, decodes, and processes Dofus Retro network packets to enable intelligent navigation and game state awareness.

**Key Difference from Source Project:**
- **Current Python Project:** Uses image recognition (OpenCV) for game state detection
- **New Java Project:** Will use **network packet interception and decoding** for game state awareness

---

## 1. Project Overview

### 1.1 Current State Analysis (Python Project)

The existing Python project (`Dofus_Kbot_N`) is an **image-based automation bot** with the following characteristics:

**Architecture:**
- **Technology:** Python 3.9+, PyAutoGUI, OpenCV, CustomTkinter
- **Approach:** Screen scraping + mouse/keyboard automation
- **Detection Method:** Template matching with pre-defined PNG images
- **Game Versions:** Dofus Retro and Dofus 2.0 (partial)

**Core Components:**
1. **Image Recognition Engine** (`donjon_utils.py`, `utils/vision.py`)
   - OpenCV template matching (cv2.matchTemplate)
   - Confidence threshold: 0.7-0.9
   - Detects: mobs, buttons, avatars, NPCs, combat states

2. **Combat Automation** (`main.py`, `main2.py`, `main_donjon.py`)
   - Turn-based combat execution
   - Multi-room dungeon farming
   - Class-specific spell rotations (e.g., Sadida)

3. **Configuration System**
   - Text-based config (`config.txt`)
   - JSON-based dungeon config (`config_donjon.json`)
   - Per-room combat strategies

4. **GUI Interface** (`interface.py`)
   - CustomTkinter dark-themed UI
   - Two modes: Map farming, Dungeon farming
   - Real-time configuration

**Limitations of Image-Based Approach:**
- ❌ High CPU usage (continuous screen capture)
- ❌ Resolution-dependent (breaks on different screen sizes)
- ❌ Fragile (UI updates break the bot)
- ❌ No access to underlying game data
- ❌ Cannot detect invisible game states
- ❌ Requires exclusive screen control

### 1.2 Proposed Java Solution: Packet-Based Architecture

The new Java implementation will use **Man-in-the-Middle (MITM) packet interception** to read game network traffic instead of screen scraping.

**Advantages:**
- ✅ Resolution-independent
- ✅ Low CPU usage
- ✅ Access to complete game state
- ✅ Can run in background
- ✅ More reliable detection
- ✅ Real-time data access
- ✅ Easier to extend with new features

---

## 2. Dofus Retro Network Protocol

### 2.1 Protocol Overview

Based on research and existing implementations (retroproto, dofus-protocol):

**Protocol Characteristics:**
- **Type:** Custom text-based protocol over TCP
- **Encoding:** UTF-8
- **Transport:** TCP sockets
- **Port:** 443, 5555 (depending on server)
- **Message Format:** `[ID][Separator][Data]\n`
- **Separator:** Pipe character `|`
- **Delimiter:** Newline `\n` or null byte `\x00`

**Example Packets:**
```
# Login packet
AAXxRamboPLxX|1|0|3635424|855309|16053493

# Movement packet
GA<direction>

# Map data packet
GDM|<map_data>|<entities>|...

# Game action
GA<action_id>|<parameters>

# Chat message
cMK|1|<sender>|<message>
```

### 2.2 Packet Categories

Based on retroproto library and reverse engineering sources:

#### 2.2.1 Authentication & Connection (A*)
- `AA` - Authentication request
- `AV` - Server version
- `AT` - Ticket authentication
- `Af` - Server list
- `AH` - Server selection
- `AX` - Character list
- `AS` - Character selection

#### 2.2.2 Game World & Map (G*)
- `GM` - Map data
- `GDM` - Map data with entities
- `GDF` - Map fight data
- `GA` - Game action
- `GC` - Map creation complete
- `GV` - Game version
- `GP` - Player position
- `GI` - Map info

#### 2.2.3 Movement & Navigation (G*)
- `GA` - Movement action
- `GAF` - Movement validation
- `GK` - Cell movement

#### 2.2.4 Combat (G*)
- `GS` - Game fight start
- `GT` - Turn start
- `GE` - Fight end
- `GA` - Action in combat (spells, movement)
- `GTS` - Fight turn start
- `GTF` - Fight turn finish
- `GTR` - Fight turn ready

#### 2.2.5 Inventory & Items (O*)
- `OT` - Object list
- `OA` - Add object
- `OR` - Remove object
- `OM` - Move object
- `OQ` - Quantity update

#### 2.2.6 NPC & Dialog (D*)
- `DQ` - Dialog question
- `DR` - Dialog response
- `DC` - Dialog creation
- `DV` - Dialog validation

#### 2.2.7 Stats & Character (A*, S*)
- `As` - Character stats
- `SL` - Spell list
- `SM` - Spell movement
- `SK` - Spell upgrade

#### 2.2.8 Chat & Social (c*, F*, P*)
- `cMK` - Chat message (Kamas)
- `cMKF` - Chat message from
- `BAT` - Basic information
- `FO` - Friend list
- `FL` - Friend list update

#### 2.2.9 Exchange & Trade (E*)
- `EC` - Exchange create
- `EK` - Exchange OK
- `EV` - Exchange validation
- `EL` - Exchange local
- `ER` - Exchange remote

### 2.3 Packet Structure Specifications

**Generic Packet Format:**
```
[Packet ID (1-3 chars)][Separator][Data Fields (pipe-delimited)]\n
```

**Data Field Types:**
- String: UTF-8 text
- Integer: Decimal numbers
- Boolean: 0 or 1
- List: Semicolon-delimited `;`
- Nested: Pipe-delimited with sub-structures

**Example Packet Breakdown:**
```
Packet: GDM|432|1024;768|player1;100;50|mob1;200;75|mob2;250;80

GDM        → Packet ID (Game Data Map)
|          → Separator
432        → Map ID
|          → Separator
1024;768   → Map dimensions (width;height)
|          → Separator
player1;100;50  → Entity (name;x;y)
|          → Separator
mob1;200;75     → Entity (mob;x;y)
|          → Separator
mob2;250;80     → Entity (mob;x;y)
```

### 2.4 Encryption & Security

**Dofus Retro Protocol:**
- Early versions: **No encryption** (plain text)
- Recent versions: **Basic XOR obfuscation** with static keys
- Modern versions: **Custom encryption** (requires key extraction)

**Approach for Java Implementation:**
- Start with plain text support
- Add XOR deobfuscation layer
- Implement custom decryption as needed
- Support multiple protocol versions

---

## 3. System Architecture (Java Implementation)

### 3.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                  │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   REST API   │  │   WebSocket  │  │   Admin UI   │      │
│  │  Controller  │  │   Endpoint   │  │  (React/Vue) │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                  │                  │               │
│  ┌──────┴──────────────────┴──────────────────┴───────┐     │
│  │           Application Service Layer                 │     │
│  │  ┌──────────────┐  ┌──────────────┐               │     │
│  │  │ Navigation   │  │   Combat     │               │     │
│  │  │   Service    │  │  Service     │               │     │
│  │  └──────────────┘  └──────────────┘               │     │
│  └─────────────────────────────────────────────────────┘    │
│         │                                                     │
│  ┌──────┴──────────────────────────────────────────────┐    │
│  │         Packet Processing Layer                      │    │
│  │  ┌──────────────┐  ┌──────────────┐                │    │
│  │  │   Decoder    │  │   Encoder    │                │    │
│  │  │   Service    │  │   Service    │                │    │
│  │  └──────┬───────┘  └──────┬───────┘                │    │
│  │         │                  │                         │    │
│  │  ┌──────┴──────────────────┴───────┐               │    │
│  │  │    Packet Registry               │               │    │
│  │  │  (Packet Type Definitions)       │               │    │
│  │  └──────────────────────────────────┘               │    │
│  └─────────────────────────────────────────────────────┘    │
│         │                                                     │
│  ┌──────┴──────────────────────────────────────────────┐    │
│  │         Network Interception Layer                   │    │
│  │  ┌──────────────┐  ┌──────────────┐                │    │
│  │  │ MITM Proxy   │  │  TCP Socket  │                │    │
│  │  │   Service    │  │   Handler    │                │    │
│  │  └──────────────┘  └──────────────┘                │    │
│  └─────────────────────────────────────────────────────┘    │
│         │                                                     │
│  ┌──────┴──────────────────────────────────────────────┐    │
│  │         Data Persistence Layer                       │    │
│  │  ┌──────────────┐  ┌──────────────┐                │    │
│  │  │  PostgreSQL  │  │    Redis     │                │    │
│  │  │  (JPA/Repo)  │  │   (Cache)    │                │    │
│  │  └──────────────┘  └──────────────┘                │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                               │
└─────────────────────────────────────────────────────────────┘
         │                                      │
    ┌────▼────┐                           ┌────▼────┐
    │  Dofus  │ ◄────── Network ─────────►│  Dofus  │
    │ Client  │          Traffic           │ Server  │
    └─────────┘                           └─────────┘
```

### 3.2 Module Breakdown

#### 3.2.1 Core Modules

**1. `dofus-network-core`**
- MITM proxy implementation
- TCP socket handling
- Raw packet capture
- Connection management

**2. `dofus-protocol`**
- Packet type definitions (enums, DTOs)
- Serialization/Deserialization
- Protocol version management
- Encryption/Decryption handlers

**3. `dofus-packet-decoder`**
- Packet parsing logic
- Field extraction
- Validation
- Error handling

**4. `dofus-game-state`**
- Game state management
- Entity tracking (players, mobs, NPCs)
- Map state
- Combat state
- Inventory state

**5. `dofus-navigation`**
- Pathfinding algorithms (A*, Dijkstra)
- Map graph representation
- Cell-based movement
- Obstacle detection

**6. `dofus-combat`**
- Turn-based combat logic
- Spell management
- Target selection
- Combat strategy patterns

**7. `dofus-api`**
- REST API endpoints
- WebSocket real-time updates
- Request/Response DTOs
- OpenAPI documentation

**8. `dofus-persistence`**
- JPA entities
- Repository interfaces
- Database migrations (Flyway/Liquibase)
- Caching layer (Redis)

**9. `dofus-web-ui`**
- React/Vue admin interface
- Real-time packet viewer
- Configuration panel
- Game state visualizer

---

## 4. Detailed Requirements

### 4.1 Functional Requirements

#### FR-1: Packet Interception
**Priority:** Critical
**Description:** System must intercept network traffic between Dofus client and server

**Acceptance Criteria:**
- Support MITM proxy mode
- Support sniffer mode (read-only)
- Handle TCP connections on ports 443, 5555
- Maintain bidirectional packet flow
- Zero packet loss during interception
- Transparent to game client (no disconnections)

**Technical Details:**
- Use Java NIO for non-blocking I/O
- Implement SSL/TLS passthrough if needed
- Buffer management for high throughput
- Connection pooling

#### FR-2: Packet Decoding
**Priority:** Critical
**Description:** Decode raw bytes into structured packet objects

**Acceptance Criteria:**
- Parse packet ID (1-3 characters)
- Extract data fields (pipe-delimited)
- Handle nested structures
- Support all packet categories (A*, G*, O*, D*, c*, etc.)
- Validate packet structure
- Handle malformed packets gracefully

**Technical Details:**
```java
public interface PacketDecoder {
    Packet decode(byte[] rawData) throws PacketDecodingException;
    <T extends Packet> T decode(byte[] rawData, Class<T> packetType);
    boolean canDecode(byte[] rawData);
}

// Example packet classes
@Data
@PacketId("GDM")
public class GameDataMapPacket extends Packet {
    private int mapId;
    private MapDimensions dimensions;
    private List<Entity> entities;
    private String mapKey;
    private LocalDateTime date;
}

@Data
@PacketId("AA")
public class AuthenticationPacket extends Packet {
    private String username;
    private String encryptedPassword;
    private List<Long> serverIds;
}
```

#### FR-3: Packet Encoding
**Priority:** High
**Description:** Encode structured objects into raw bytes for transmission

**Acceptance Criteria:**
- Serialize packet objects to byte arrays
- Apply proper formatting (ID + separator + data)
- Handle special characters escaping
- Support encryption/obfuscation
- Validate before encoding

**Technical Details:**
```java
public interface PacketEncoder {
    byte[] encode(Packet packet) throws PacketEncodingException;
    byte[] encodeWithEncryption(Packet packet, EncryptionKey key);
}
```

#### FR-4: Game State Management
**Priority:** Critical
**Description:** Maintain real-time game state based on packet data

**Acceptance Criteria:**
- Track player position (map ID, cell coordinates)
- Track player stats (HP, MP, AP, level, kamas)
- Track inventory items
- Track surrounding entities (players, mobs, NPCs)
- Track combat state (in fight, turn number, available actions)
- Track dialog state (open dialogs, NPC interactions)
- Event-driven state updates

**Technical Details:**
```java
@Service
public class GameStateService {
    private final ConcurrentHashMap<String, PlayerState> playerStates;
    private final EventPublisher eventPublisher;

    public void updateFromPacket(Packet packet) {
        // Update state based on packet type
        if (packet instanceof GameDataMapPacket) {
            updateMapState((GameDataMapPacket) packet);
        }
        // Publish events for listeners
        eventPublisher.publish(new StateChangedEvent(packet));
    }

    public PlayerState getCurrentState(String playerId) {
        return playerStates.get(playerId);
    }
}

@Data
public class PlayerState {
    private String characterName;
    private int mapId;
    private Position position;
    private Stats stats;
    private Inventory inventory;
    private CombatState combatState;
    private List<Entity> nearbyEntities;
}
```

#### FR-5: Pathfinding & Navigation
**Priority:** High
**Description:** Calculate optimal paths between cells on maps

**Acceptance Criteria:**
- Implement A* algorithm for pathfinding
- Support weighted cells (different terrain costs)
- Detect obstacles (blocked cells)
- Handle multi-map navigation
- Calculate movement cost (MP required)
- Find nearest entity of type X

**Technical Details:**
```java
public interface PathfindingService {
    Path findPath(Position start, Position end, MapData map);
    Path findPathWithCost(Position start, Position end, int maxMpCost);
    List<Position> getReachableCells(Position current, int availableMp);
    Position findNearestEntity(Position current, EntityType type);
}

@Data
public class Path {
    private List<Position> cells;
    private int mpCost;
    private int estimatedTimeSeconds;
}
```

#### FR-6: Combat System
**Priority:** High
**Description:** Manage turn-based combat logic

**Acceptance Criteria:**
- Detect combat start/end
- Track turn order
- Identify player's turn
- Calculate spell ranges
- Select optimal targets
- Execute spell sequences
- Handle turn timeout
- Detect combat events (damage, death, effects)

**Technical Details:**
```java
@Service
public class CombatService {
    public void onCombatStart(CombatStartPacket packet) {
        // Initialize combat state
    }

    public void onTurnStart(TurnStartPacket packet) {
        if (isPlayerTurn(packet)) {
            executeCombatStrategy();
        }
    }

    private void executeCombatStrategy() {
        CombatStrategy strategy = strategyFactory.getStrategy();
        List<CombatAction> actions = strategy.computeActions(gameState);
        actions.forEach(this::executeAction);
    }
}

public interface CombatStrategy {
    List<CombatAction> computeActions(GameState state);
}

@Data
public class CombatAction {
    private ActionType type; // CAST_SPELL, MOVE, PASS_TURN
    private int spellId;
    private Position targetCell;
    private int entityId;
}
```

#### FR-7: Configuration Management
**Priority:** Medium
**Description:** Flexible configuration system

**Acceptance Criteria:**
- YAML/Properties file configuration
- Environment-specific configs (dev, prod)
- Hot-reload configuration
- Configuration validation
- Default values

**Technical Details:**
```yaml
# application.yml
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
      key: "default-key"

  game:
    auto-reconnect: true
    packet-logging: true
    state-persistence: true

  combat:
    strategy: AGGRESSIVE
    spell-priorities:
      - id: 101
        priority: 1
      - id: 102
        priority: 2

  navigation:
    algorithm: A_STAR
    max-path-length: 50
    avoid-aggressive-mobs: true
```

#### FR-8: REST API
**Priority:** Medium
**Description:** Expose functionality via REST API

**Endpoints:**
```
GET    /api/v1/game/state           - Get current game state
GET    /api/v1/game/state/player    - Get player state
GET    /api/v1/game/state/map       - Get current map state
GET    /api/v1/game/state/combat    - Get combat state

POST   /api/v1/navigation/path      - Calculate path
POST   /api/v1/navigation/move      - Execute movement

GET    /api/v1/packets              - Get packet history
GET    /api/v1/packets/{id}         - Get packet by ID
POST   /api/v1/packets/send         - Send custom packet

GET    /api/v1/combat/actions       - Get available combat actions
POST   /api/v1/combat/execute       - Execute combat action

GET    /api/v1/config               - Get configuration
PUT    /api/v1/config               - Update configuration

WS     /ws/packets                  - Real-time packet stream
WS     /ws/game-state               - Real-time state updates
```

#### FR-9: Web Dashboard
**Priority:** Low
**Description:** Web-based admin interface

**Features:**
- Real-time packet viewer (filterable)
- Game state visualization
- Map viewer with entity positions
- Combat log
- Configuration editor
- Statistics dashboard

#### FR-10: Packet Logging & Replay
**Priority:** Medium
**Description:** Log packets for analysis and replay

**Acceptance Criteria:**
- Log all packets to database
- Filter by packet type, time range
- Export to JSON/CSV
- Replay packet sequences
- Analyze packet patterns

### 4.2 Non-Functional Requirements

#### NFR-1: Performance
- Decode 10,000+ packets per second
- Max latency: 5ms per packet
- Memory usage: < 512MB for 1M packets in cache
- CPU usage: < 20% on modern hardware

#### NFR-2: Reliability
- 99.9% uptime
- Graceful degradation on errors
- Automatic reconnection on network failures
- Zero data loss on crashes (persist state)

#### NFR-3: Scalability
- Support multiple concurrent game sessions
- Horizontal scaling via Spring Cloud
- Database partitioning for large datasets

#### NFR-4: Security
- No packet injection (read-only by default)
- API authentication (JWT tokens)
- Rate limiting on API endpoints
- Audit logging for all actions

#### NFR-5: Maintainability
- Clean architecture (hexagonal/onion)
- 80%+ unit test coverage
- Integration tests for critical flows
- Comprehensive JavaDoc
- OpenAPI 3.0 specification

#### NFR-6: Observability
- Structured logging (JSON format)
- Prometheus metrics
- Distributed tracing (Jaeger/Zipkin)
- Health check endpoints

---

## 5. Data Models

### 5.1 Core Entities

```java
// Player Entity
@Entity
@Table(name = "players")
public class Player {
    @Id
    private String id;
    private String characterName;
    private int level;
    private long experience;
    private String breed; // Iop, Sadida, etc.
    private int mapId;
    private int cellId;
    private LocalDateTime lastSeen;

    @OneToMany(mappedBy = "player")
    private List<InventoryItem> inventory;

    @Embedded
    private Stats stats;
}

// Packet Entity
@Entity
@Table(name = "packets")
@Index(name = "idx_packet_timestamp", columnList = "timestamp")
@Index(name = "idx_packet_type", columnList = "packetType")
public class PacketLog {
    @Id
    @GeneratedValue
    private Long id;

    private String packetType;
    private Direction direction; // INBOUND, OUTBOUND

    @Lob
    private byte[] rawData;

    @Lob
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> parsedData;

    private LocalDateTime timestamp;
    private String sessionId;
}

// Map Entity
@Entity
@Table(name = "maps")
public class GameMap {
    @Id
    private Integer mapId;

    private String mapName;
    private int width;
    private int height;

    @Lob
    private String cellData; // Walkability matrix

    @OneToMany(mappedBy = "map")
    private List<MapEntity> entities;
}

// Combat Session
@Entity
@Table(name = "combat_sessions")
public class CombatSession {
    @Id
    @GeneratedValue
    private Long id;

    private String playerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private CombatResult result; // WIN, LOSS, FLEE

    @OneToMany(mappedBy = "combatSession")
    private List<CombatAction> actions;
}
```

### 5.2 Packet DTOs

```java
// Base Packet
public abstract class Packet {
    private String packetId;
    private LocalDateTime timestamp;
    private Direction direction;

    public abstract void decode(String rawData);
    public abstract String encode();
}

// Specific Packets
@PacketId("GDM")
public class GameDataMapPacket extends Packet {
    private int mapId;
    private String mapDate;
    private String mapKey;
    private List<GameEntity> entities;
}

@PacketId("GA")
public class GameActionPacket extends Packet {
    private int actionType;
    private int actorId;
    private String parameters;
}

@PacketId("GTS")
public class GameTurnStartPacket extends Packet {
    private int playerId;
    private int remainingTime;
}
```

---

## 6. Technical Stack

### 6.1 Core Technologies

**Language & Runtime:**
- Java 26 (LTS features: Virtual Threads, Pattern Matching, Records)
- GraalVM (optional for native compilation)

**Framework:**
- Spring Boot 3.3.x
- Spring Web (REST API)
- Spring WebSocket (Real-time communication)
- Spring Data JPA (Database access)
- Spring Cache (Redis integration)
- Spring Security (API authentication)

**Build Tool:**
- Maven 3.9+ or Gradle 8+

**Database:**
- PostgreSQL 16+ (Primary data store)
- Redis 7+ (Caching & session management)

**Networking:**
- Netty 4.1+ (High-performance TCP/IP)
- OkHttp 4+ (HTTP client)

**Testing:**
- JUnit 5
- Mockito
- Testcontainers (Integration tests)
- WireMock (API mocking)

**Observability:**
- SLF4J + Logback (Logging)
- Micrometer + Prometheus (Metrics)
- OpenTelemetry (Tracing)

**Documentation:**
- SpringDoc OpenAPI 3
- Javadoc
- AsciiDoc (Architecture docs)

**Frontend (Optional):**
- React 18+ or Vue 3+
- TypeScript
- Vite
- TailwindCSS

### 6.2 Third-Party Libraries

```xml
<dependencies>
    <!-- Network -->
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
        <version>4.1.100.Final</version>
    </dependency>

    <!-- Packet Parsing -->
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>32.1.3-jre</version>
    </dependency>

    <!-- JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>

    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
    </dependency>
</dependencies>
```

---

## 7. Implementation Phases

### Phase 1: Foundation (Weeks 1-3)
**Goal:** Basic packet capture and decoding

**Tasks:**
- [ ] Set up Spring Boot project structure
- [ ] Implement TCP proxy with Netty
- [ ] Create packet model hierarchy
- [ ] Implement basic packet decoder (10 packet types)
- [ ] Set up PostgreSQL + JPA entities
- [ ] Unit tests for decoder

**Deliverables:**
- Working MITM proxy
- Decoder for auth, map, movement packets
- Database schema

### Phase 2: Game State Management (Weeks 4-6)
**Goal:** Real-time game state tracking

**Tasks:**
- [ ] Implement GameStateService
- [ ] Event-driven state updates
- [ ] Redis caching layer
- [ ] Expand packet support (30+ types)
- [ ] Create REST API endpoints
- [ ] Integration tests

**Deliverables:**
- Complete game state tracking
- REST API (v1)
- 30+ packet types decoded

### Phase 3: Navigation & Pathfinding (Weeks 7-9)
**Goal:** Intelligent navigation

**Tasks:**
- [ ] Implement A* pathfinding
- [ ] Map graph representation
- [ ] Cell walkability detection
- [ ] Multi-map navigation
- [ ] Navigation API endpoints

**Deliverables:**
- Pathfinding engine
- Navigation service
- Map database

### Phase 4: Combat System (Weeks 10-12)
**Goal:** Turn-based combat logic

**Tasks:**
- [ ] Combat state machine
- [ ] Spell range calculation
- [ ] Target selection algorithms
- [ ] Combat strategies (configurable)
- [ ] Combat API endpoints

**Deliverables:**
- Combat service
- Strategy pattern implementations
- Combat logging

### Phase 5: Web Dashboard (Weeks 13-15)
**Goal:** Admin interface

**Tasks:**
- [ ] React/Vue setup
- [ ] Real-time packet viewer
- [ ] Map visualizer
- [ ] Configuration UI
- [ ] WebSocket integration

**Deliverables:**
- Web dashboard
- Real-time updates
- Packet replay feature

### Phase 6: Polish & Production (Weeks 16-18)
**Goal:** Production-ready system

**Tasks:**
- [ ] Performance optimization
- [ ] Security hardening
- [ ] Comprehensive testing (80% coverage)
- [ ] Documentation
- [ ] Docker deployment
- [ ] CI/CD pipeline

**Deliverables:**
- Production deployment
- Complete documentation
- Docker Compose setup

---

## 8. What Can Be Reused from Python Project

### 8.1 Reusable Concepts ✅

**1. Combat Logic Patterns**
- Turn-based execution flow
- Spell rotation strategies (e.g., Sadida Fourbe)
- Combat state detection logic
- Post-combat cleanup flow

**2. Configuration Structure**
- Per-room dungeon configuration
- Combat mode selection (Mode 1, 2, 3)
- Spell priority system
- Movement enablement flags

**3. Business Logic**
- NPC interaction sequences (entry/exit)
- Multi-room dungeon progression
- Combat turn timeout handling
- Level-up/loot detection flow

**4. UI/UX Patterns**
- Tabbed interface (Map vs Dungeon)
- Real-time configuration
- Start/Stop controls
- Version display

**5. Testing Approach**
- Comprehensive test categories
- Real-world testing suite
- Configuration validation tests

### 8.2 NOT Reusable (Technology-Specific) ❌

**1. Image Recognition Code**
- OpenCV template matching
- Screen capture logic
- Image file dependencies
- Confidence threshold tuning

**Reason:** New system uses packet decoding, not screen scraping

**2. Mouse/Keyboard Automation**
- PyAutoGUI click/press commands
- Coordinate-based targeting
- Screen coordinate system

**Reason:** Packet-based system doesn't control client directly

**3. Python-Specific Libraries**
- CustomTkinter GUI
- Python config parsing
- Python async/threading

**Reason:** Java has different ecosystem

**4. File-Based Configuration**
- Text file parsing (`config.txt`)
- Manual JSON handling

**Reason:** Spring Boot uses YAML + annotation-based config

### 8.3 Architectural Lessons Learned

**From Python Project:**
- ✅ Modular design (separate utils, main, interface)
- ✅ Configuration-driven behavior
- ✅ Multi-mode support (flexibility)
- ❌ Tight coupling to GUI framework
- ❌ Limited error handling
- ❌ No logging infrastructure

**For Java Project:**
- ✅ Hexagonal architecture (ports & adapters)
- ✅ Dependency injection (Spring)
- ✅ Comprehensive logging
- ✅ Event-driven design
- ✅ API-first approach

---

## 9. Risks & Mitigation

### Risk 1: Protocol Changes
**Probability:** Medium
**Impact:** High
**Mitigation:**
- Version detection in packets
- Protocol abstraction layer
- Easy packet definition updates
- Automated regression tests

### Risk 2: Encryption/Obfuscation
**Probability:** High
**Impact:** High
**Mitigation:**
- Start with known unencrypted versions
- Research existing deobfuscation tools
- Community collaboration (retroproto, etc.)
- Incremental encryption support

### Risk 3: Account Bans
**Probability:** High
**Impact:** Medium
**Mitigation:**
- Read-only mode by default
- No packet injection in initial version
- Educational purpose disclaimer
- Respect game ToS

### Risk 4: Performance Bottlenecks
**Probability:** Low
**Impact:** Medium
**Mitigation:**
- Use Virtual Threads (Java 26)
- Async packet processing
- Connection pooling
- Caching strategy

### Risk 5: Incomplete Protocol Documentation
**Probability:** High
**Impact:** Medium
**Mitigation:**
- Reverse engineer from existing tools
- Community knowledge sharing
- Packet capture analysis
- Incremental implementation (start with critical packets)

---

## 10. Success Metrics

### Technical Metrics
- [ ] Decode 100+ unique packet types
- [ ] < 10ms average packet decode time
- [ ] 99%+ packet decode success rate
- [ ] 80%+ unit test coverage
- [ ] 0 critical bugs in production

### Functional Metrics
- [ ] Accurately track player position
- [ ] Detect combat start/end with 100% accuracy
- [ ] Calculate valid paths in < 50ms
- [ ] Support 3+ combat strategies
- [ ] Handle 1000+ packets/minute without lag

### Business Metrics
- [ ] Complete documentation
- [ ] Working demo deployment
- [ ] Open-source community adoption
- [ ] Educational value for protocol learning

---

## 11. Future Enhancements (Post-MVP)

1. **Multi-Account Support**
   - Track multiple game sessions
   - Coordinated actions
   - Leader-follower patterns

2. **Machine Learning Integration**
   - Combat strategy optimization
   - Mob behavior prediction
   - Optimal spell selection

3. **Advanced Analytics**
   - Drop rate tracking
   - Experience optimization
   - Economy analysis (Kama/hour)

4. **Cloud Deployment**
   - Kubernetes orchestration
   - Multi-region support
   - Scalable architecture

5. **Plugin System**
   - Custom packet handlers
   - Third-party extensions
   - Strategy marketplace

6. **Mobile App**
   - Remote monitoring
   - Push notifications
   - Mobile configuration

---

## 12. References & Resources

### Documentation
- [Dofus Retro Protocol Wiki](https://github.com/Geraxi-Allan/Wiki-Dofus/blob/master/pages/Protocole-reseau.md)
- [retroproto Go Library](https://github.com/kralamoure/retroproto)
- [AstrubTools Protocol](https://github.com/AstrubTools/dofus-protocol)
- [Guinness-Bot MITM](https://github.com/Romain-P/Guinness-Bot)

### Technical Papers
- "Game Hacking: Reverse engineering Dofus" (UPC Thesis)

### Tools
- Wireshark (Packet analysis)
- mitmproxy (HTTP/TCP proxy)
- Netty (Java NIO framework)
- Spring Boot DevTools

---

## 13. Glossary

**MITM:** Man-in-the-Middle - Network interception technique
**Packet:** Network message between client and server
**Codec:** Encoder/Decoder for data transformation
**Cell:** Grid position on Dofus map (tactical combat grid)
**AP:** Action Points (combat resource)
**MP:** Movement Points (movement resource)
**DTO:** Data Transfer Object
**JPA:** Java Persistence API
**A*:** A-star pathfinding algorithm
**Retro:** Dofus 1.x version (older game version)

---

## 14. Approval & Sign-off

**Document Status:** Draft v1.0
**Next Review Date:** TBD
**Approved By:** TBD

---

## Appendix A: Packet Type Reference

### Complete Packet List (100+ types)

```
Authentication (A*)
├── AA - Auth request
├── Ac - Create character
├── Ad - Delete character
├── Ae - Server list
├── Af - Server list full
├── Ag - Gift list
├── AH - Server selection
├── AI - Account info
├── AK - Account ticket
├── AL - Account list
├── AM - Message of the day
├── AP - Account pseudo
├── AQ - Account queue
├── AR - Realm list
├── AS - Character selection
├── AT - Ticket auth
├── AV - Version check
├── AX - Character list
└── Ax - Character list entry

Game World (G*)
├── GA - Game action
├── GC - Map creation
├── GDF - Map fight
├── GDK - Map key
├── GDM - Map data
├── GE - Fight end
├── GF - Fight flag
├── GI - Map info
├── GK - Cell movement
├── GM - Map movement
├── GP - Player position
├── GS - Fight start
├── GT - Turn
├── GTF - Turn finish
├── GTM - Turn middle
├── GTR - Turn ready
├── GTS - Turn start
└── GV - Game version

Inventory (O*)
├── OA - Add object
├── Od - Delete object
├── OM - Move object
├── OQ - Object quantity
├── OR - Remove object
├── OS - Object set
└── OT - Object list

NPC & Dialog (D*)
├── DC - Dialog create
├── DQ - Dialog question
├── DR - Dialog response
└── DV - Dialog validate

Stats (A*, S*)
├── As - Stats
├── SB - Boost stat
├── SE - Spell error
├── SF - Spell forget
├── SL - Spell list
├── SM - Spell move
└── SK - Spell upgrade

Chat (c*)
├── cC - Channel
├── cM - Message
└── cMK - Message kamas

Exchange (E*)
├── EA - Exchange accept
├── EC - Exchange create
├── EH - Exchange movement
├── EK - Exchange OK
├── EL - Exchange local
├── ER - Exchange remote
└── EV - Exchange validate

Bank (G*)
├── G! - Bank open
├── G+ - Bank add
└── G- - Bank remove
```

---

## Appendix B: Example Implementation

### Sample Packet Decoder

```java
@Component
public class PacketDecoderImpl implements PacketDecoder {

    private final Map<String, Class<? extends Packet>> packetRegistry;

    @Autowired
    public PacketDecoderImpl(PacketRegistry registry) {
        this.packetRegistry = registry.getPackets();
    }

    @Override
    public Packet decode(byte[] rawData) throws PacketDecodingException {
        try {
            String data = new String(rawData, StandardCharsets.UTF_8);

            // Extract packet ID (1-3 chars before first separator)
            int separatorIndex = data.indexOf('|');
            if (separatorIndex == -1) {
                separatorIndex = data.length();
            }

            String packetId = data.substring(0, Math.min(3, separatorIndex));

            // Find packet class
            Class<? extends Packet> packetClass = packetRegistry.get(packetId);
            if (packetClass == null) {
                throw new UnknownPacketException(packetId);
            }

            // Instantiate and decode
            Packet packet = packetClass.getDeclaredConstructor().newInstance();
            packet.setPacketId(packetId);
            packet.setTimestamp(LocalDateTime.now());
            packet.decode(data.substring(separatorIndex + 1));

            return packet;

        } catch (Exception e) {
            throw new PacketDecodingException("Failed to decode packet", e);
        }
    }
}
```

### Sample Packet Implementation

```java
@Data
@PacketId("GDM")
public class GameDataMapPacket extends Packet {

    private int mapId;
    private String mapDate;
    private String mapKey;
    private List<GameEntity> entities = new ArrayList<>();

    @Override
    public void decode(String rawData) {
        String[] parts = rawData.split("\\|");

        if (parts.length >= 1) {
            this.mapId = Integer.parseInt(parts[0]);
        }

        if (parts.length >= 2) {
            this.mapDate = parts[1];
        }

        if (parts.length >= 3) {
            this.mapKey = parts[2];
        }

        // Decode entities (format: id;type;cellId;name)
        for (int i = 3; i < parts.length; i++) {
            String[] entityParts = parts[i].split(";");
            if (entityParts.length >= 4) {
                GameEntity entity = new GameEntity();
                entity.setId(Integer.parseInt(entityParts[0]));
                entity.setType(EntityType.valueOf(entityParts[1]));
                entity.setCellId(Integer.parseInt(entityParts[2]));
                entity.setName(entityParts[3]);
                entities.add(entity);
            }
        }
    }

    @Override
    public String encode() {
        StringBuilder sb = new StringBuilder(getPacketId());
        sb.append(mapId).append('|');
        sb.append(mapDate).append('|');
        sb.append(mapKey);

        for (GameEntity entity : entities) {
            sb.append('|');
            sb.append(entity.getId()).append(';');
            sb.append(entity.getType()).append(';');
            sb.append(entity.getCellId()).append(';');
            sb.append(entity.getName());
        }

        return sb.toString();
    }
}
```

---

**END OF DOCUMENT**

---

**Total Pages:** 25
**Word Count:** ~8,500
**Last Updated:** 2025-11-08
