# Architecture Documentation
## Dofus Retro Packet Decoder

**Version:** 1.0
**Last Updated:** 2025-11-08
**Status:** Planning Phase

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Architecture Patterns](#2-architecture-patterns)
3. [Module Breakdown](#3-module-breakdown)
4. [Data Flow](#4-data-flow)
5. [Technology Stack](#5-technology-stack)
6. [Design Decisions](#6-design-decisions)
7. [Security Considerations](#7-security-considerations)
8. [Performance Considerations](#8-performance-considerations)
9. [Scalability](#9-scalability)
10. [Extension Points](#10-extension-points)

---

## 1. System Overview

### 1.1 Purpose and Goals

The Dofus Retro Packet Decoder is an enterprise-grade Java application designed to intercept, decode, and analyze network packets from the Dofus Retro MMORPG. Unlike traditional image-based automation tools, this system operates at the network protocol layer, providing:

- **Real-time game state awareness** through packet interception
- **Resolution-independent operation** (no screen scraping)
- **Low CPU usage** compared to image recognition
- **Complete access** to underlying game data
- **Background operation** capability
- **Extensible architecture** for custom logic

### 1.2 High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │   REST API   │  │   WebSocket  │  │   Admin UI   │              │
│  │  Controller  │  │   Endpoint   │  │  (React/Vue) │              │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘              │
│         │                  │                  │                       │
│  ┌──────┴──────────────────┴──────────────────┴───────┐             │
│  │           Application Service Layer                 │             │
│  │  ┌──────────────┐  ┌──────────────┐               │             │
│  │  │ Navigation   │  │   Combat     │               │             │
│  │  │   Service    │  │  Service     │               │             │
│  │  └──────────────┘  └──────────────┘               │             │
│  └─────────────────────────────────────────────────────┘            │
│         │                                                             │
│  ┌──────┴──────────────────────────────────────────────┐            │
│  │         Packet Processing Layer                      │            │
│  │  ┌──────────────┐  ┌──────────────┐                │            │
│  │  │   Decoder    │  │   Encoder    │                │            │
│  │  │   Service    │  │   Service    │                │            │
│  │  └──────┬───────┘  └──────┬───────┘                │            │
│  │         │                  │                         │            │
│  │  ┌──────┴──────────────────┴───────┐               │            │
│  │  │    Packet Registry               │               │            │
│  │  │  (Packet Type Definitions)       │               │            │
│  │  └──────────────────────────────────┘               │            │
│  └─────────────────────────────────────────────────────┘            │
│         │                                                             │
│  ┌──────┴──────────────────────────────────────────────┐            │
│  │         Network Interception Layer                   │            │
│  │  ┌──────────────┐  ┌──────────────┐                │            │
│  │  │ MITM Proxy   │  │  TCP Socket  │                │            │
│  │  │   Service    │  │   Handler    │                │            │
│  │  └──────────────┘  └──────────────┘                │            │
│  └─────────────────────────────────────────────────────┘            │
│         │                                                             │
│  ┌──────┴──────────────────────────────────────────────┐            │
│  │         Data Persistence Layer                       │            │
│  │  ┌──────────────┐  ┌──────────────┐                │            │
│  │  │  PostgreSQL  │  │    Redis     │                │            │
│  │  │  (JPA/Repo)  │  │   (Cache)    │                │            │
│  │  └──────────────┘  └──────────────┘                │            │
│  └─────────────────────────────────────────────────────┘            │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
         │                                      │
    ┌────▼────┐                           ┌────▼────┐
    │  Dofus  │ ◄────── Network ─────────►│  Dofus  │
    │ Client  │          Traffic           │ Server  │
    └─────────┘                           └─────────┘
```

### 1.3 Key Components

1. **MITM Proxy Server**: Intercepts TCP traffic between Dofus client and server
2. **Packet Decoder/Encoder**: Parses raw bytes into structured objects
3. **Game State Manager**: Maintains real-time representation of game state
4. **Business Logic Services**: Navigation, combat, inventory management
5. **REST API & WebSocket**: External interfaces for monitoring and control
6. **Persistence Layer**: Long-term storage of packets and game data

---

## 2. Architecture Patterns

### 2.1 Hexagonal Architecture (Ports and Adapters)

The system follows hexagonal architecture principles to maintain clear separation between business logic and infrastructure concerns.

```
┌─────────────────────────────────────────────┐
│           Application Core                   │
│  ┌──────────────────────────────────┐       │
│  │     Domain Model                 │       │
│  │  (Game State, Entities, Rules)   │       │
│  └──────────────────────────────────┘       │
│              │                               │
│  ┌──────────────────────────────────┐       │
│  │      Business Services           │       │
│  │  (GameStateService, Navigation)  │       │
│  └──────────────────────────────────┘       │
└─────────────────────────────────────────────┘
         │                      │
    ┌────▼──────┐         ┌────▼──────┐
    │   Ports   │         │   Ports   │
    │ (Inbound) │         │ (Outbound)│
    └────┬──────┘         └────┬──────┘
         │                      │
    ┌────▼──────┐         ┌────▼──────┐
    │ Adapters  │         │ Adapters  │
    │  (REST,   │         │ (JPA,     │
    │WebSocket) │         │  Redis)   │
    └───────────┘         └───────────┘
```

**Benefits:**
- Business logic independent of infrastructure
- Easy to swap implementations (e.g., replace PostgreSQL with MongoDB)
- Highly testable with mock adapters
- Clear dependency flow (inward only)

### 2.2 Event-Driven Architecture

The system uses Spring's event mechanism for loose coupling between components.

```
┌──────────────┐        ┌──────────────┐        ┌──────────────┐
│   Packet     │──────►│   Game       │──────►│  Navigation  │
│   Decoder    │ Event  │   State      │ Event  │  Service     │
│              │        │   Manager    │        │              │
└──────────────┘        └──────────────┘        └──────────────┘
       │                       │                       │
       │                       │                       │
       ▼                       ▼                       ▼
┌──────────────┐        ┌──────────────┐        ┌──────────────┐
│   Combat     │        │   WebSocket  │        │   Database   │
│   Service    │        │   Publisher  │        │   Persister  │
└──────────────┘        └──────────────┘        └──────────────┘
```

**Event Types:**
- `PacketDecodedEvent`: Published when a packet is successfully decoded
- `MapChangedEvent`: Published when player enters a new map
- `CombatStartEvent`: Published when combat begins
- `TurnStartEvent`: Published when player's turn starts
- `StateChangedEvent`: Generic state change notification

**Example:**
```java
@Component
public class CombatService {

    @EventListener
    public void onCombatStart(CombatStartEvent event) {
        // Initialize combat state
        CombatState state = initializeCombat(event);
        eventPublisher.publish(new StateChangedEvent(state));
    }

    @EventListener
    public void onTurnStart(TurnStartEvent event) {
        if (isPlayerTurn(event)) {
            executeCombatStrategy();
        }
    }
}
```

### 2.3 Modular Monolith Approach

The application is organized as a modular monolith that can evolve into microservices if needed.

```
┌─────────────────────────────────────────────────────┐
│                 Maven Multi-Module                   │
├─────────────────────────────────────────────────────┤
│  dofus-network-core      (Network layer)            │
│  dofus-protocol          (Packet definitions)       │
│  dofus-packet-decoder    (Codec logic)              │
│  dofus-game-state        (Domain model)             │
│  dofus-navigation        (Pathfinding)              │
│  dofus-combat            (Combat logic)             │
│  dofus-api               (REST/WebSocket)           │
│  dofus-persistence       (Database)                 │
│  dofus-web-ui            (Frontend)                 │
└─────────────────────────────────────────────────────┘
```

**Benefits:**
- Clear module boundaries
- Independent compilation
- Explicit dependencies via Maven/Gradle
- Can extract modules into services later
- Single deployment for simplicity

---

## 3. Module Breakdown

### 3.1 dofus-network-core

**Responsibilities:**
- TCP connection management
- MITM proxy implementation
- Raw packet capture
- Connection pooling
- SSL/TLS passthrough

**Key Classes:**
```java
@Component
public class DofusProxyServer {
    private ServerBootstrap bootstrap;
    private Channel serverChannel;

    public void start(int port);
    public void stop();
}

@Sharable
public class ProxyHandler extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise);
}

@Service
public class PacketCaptureService {
    public void captureInbound(byte[] data, String sessionId);
    public void captureOutbound(byte[] data, String sessionId);
}
```

**Dependencies:**
- Netty 4.1+
- Spring Boot (for configuration)

**Configuration:**
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
```

### 3.2 dofus-protocol

**Responsibilities:**
- Packet type definitions
- Protocol versioning
- Base classes and interfaces
- Serialization/deserialization contracts

**Packet Type Hierarchy:**
```
Packet (abstract)
├── AuthenticationPacket
│   ├── AAPacket (auth request)
│   ├── AXPacket (character list)
│   └── ASPacket (character selection)
├── GameWorldPacket
│   ├── GDMPacket (map data)
│   ├── GAPacket (game action)
│   └── GPPacket (player position)
├── CombatPacket
│   ├── GSPacket (combat start)
│   ├── GTSPacket (turn start)
│   └── GEPacket (combat end)
├── InventoryPacket
│   ├── OTPacket (object list)
│   ├── OAPacket (add object)
│   └── ORPacket (remove object)
├── DialogPacket
│   ├── DCPacket (dialog create)
│   └── DQPacket (dialog question)
└── ChatPacket
    └── cMKPacket (chat message)
```

**Codec Pattern:**
```java
public abstract class Packet {
    protected String packetId;
    protected LocalDateTime timestamp;
    protected Direction direction;

    public abstract void decode(String rawData) throws PacketDecodingException;
    public abstract String encode() throws PacketEncodingException;
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketId {
    String value();
}
```

**Protocol Versioning:**
```java
@Configuration
public class ProtocolConfig {
    @Value("${dofus.protocol.version}")
    private String protocolVersion;

    @Bean
    public ProtocolVersionManager versionManager() {
        return new ProtocolVersionManager(protocolVersion);
    }
}
```

### 3.3 dofus-packet-decoder

**Responsibilities:**
- Decode raw bytes to packet objects
- Encode packet objects to bytes
- Packet registry management
- Validation and error handling

**Decoder Service:**
```java
@Service
public class PacketDecoderService implements PacketDecoder {
    private final PacketRegistry registry;

    @Override
    public Packet decode(byte[] rawData) throws PacketDecodingException {
        // 1. Convert bytes to string
        String data = new String(rawData, StandardCharsets.UTF_8);

        // 2. Extract packet ID
        String packetId = extractPacketId(data);

        // 3. Find packet class from registry
        Class<? extends Packet> packetClass = registry.get(packetId);

        // 4. Instantiate and decode
        Packet packet = instantiate(packetClass);
        packet.decode(extractPayload(data));

        return packet;
    }
}
```

**Registry Pattern:**
```java
@Component
public class PacketRegistry {
    private final Map<String, Class<? extends Packet>> registry;

    @PostConstruct
    public void initialize() {
        // Scan classpath for @PacketId annotations
        Reflections reflections = new Reflections("com.dofus.protocol");
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(PacketId.class);

        for (Class<?> clazz : annotated) {
            PacketId annotation = clazz.getAnnotation(PacketId.class);
            registry.put(annotation.value(), (Class<? extends Packet>) clazz);
        }
    }

    public Class<? extends Packet> get(String packetId) {
        return registry.get(packetId);
    }
}
```

**Extension Points:**
- Custom packet decoders via `PacketDecoder` interface
- Validation interceptors
- Decryption/encryption handlers

### 3.4 dofus-game-state

**Responsibilities:**
- Game state management
- Entity tracking
- State update events
- Concurrency control

**State Management:**
```java
@Service
public class GameStateService {
    private final ConcurrentHashMap<String, PlayerState> playerStates;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onPacketDecoded(PacketDecodedEvent event) {
        Packet packet = event.getPacket();

        if (packet instanceof GameDataMapPacket) {
            handleMapData((GameDataMapPacket) packet);
        } else if (packet instanceof GameTurnStartPacket) {
            handleTurnStart((GameTurnStartPacket) packet);
        }
        // ... handle other packet types
    }

    private void handleMapData(GameDataMapPacket packet) {
        PlayerState state = getCurrentState();
        state.setMapId(packet.getMapId());
        state.setEntities(packet.getEntities());

        eventPublisher.publishEvent(new MapChangedEvent(state));
    }

    public PlayerState getCurrentState(String playerId) {
        return playerStates.computeIfAbsent(playerId, PlayerState::new);
    }
}
```

**Event System:**
```java
public class StateChangedEvent extends ApplicationEvent {
    private final PlayerState newState;

    public StateChangedEvent(PlayerState source) {
        super(source);
        this.newState = source;
    }
}
```

**Concurrency Model:**
- Uses `ConcurrentHashMap` for thread-safe state storage
- Event listeners executed asynchronously via `@Async`
- Virtual Threads (Java 26) for high concurrency

### 3.5 dofus-navigation

**Responsibilities:**
- Pathfinding algorithms (A*)
- Map graph representation
- Cell walkability detection
- Movement cost calculation

**Pathfinding Algorithm:**
```java
@Service
public class PathfindingService {

    public Path findPath(Position start, Position end, MapData map) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(
            Comparator.comparingInt(n -> n.f)
        );
        Set<Node> closedSet = new HashSet<>();
        Map<Position, Node> allNodes = new HashMap<>();

        Node startNode = new Node(start, 0, heuristic(start, end));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.position.equals(end)) {
                return reconstructPath(current);
            }

            closedSet.add(current);

            for (Position neighbor : getNeighbors(current.position, map)) {
                if (closedSet.contains(allNodes.get(neighbor))) {
                    continue;
                }

                int tentativeG = current.g + movementCost(current.position, neighbor, map);

                Node neighborNode = allNodes.computeIfAbsent(
                    neighbor,
                    pos -> new Node(pos, Integer.MAX_VALUE, heuristic(pos, end))
                );

                if (tentativeG < neighborNode.g) {
                    neighborNode.g = tentativeG;
                    neighborNode.f = tentativeG + neighborNode.h;
                    neighborNode.parent = current;

                    if (!openSet.contains(neighborNode)) {
                        openSet.add(neighborNode);
                    }
                }
            }
        }

        return null; // No path found
    }

    private int heuristic(Position a, Position b) {
        // Manhattan distance
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}
```

**Map Representation:**
```java
@Data
public class MapData {
    private int mapId;
    private int width;
    private int height;
    private boolean[][] walkable;
    private int[][] movementCost;

    public boolean isWalkable(Position pos) {
        return walkable[pos.getY()][pos.getX()];
    }
}
```

**Performance Considerations:**
- A* guaranteed to find optimal path
- Priority queue for efficient node selection
- Caching of common paths in Redis
- Target: < 50ms for 50-cell path

### 3.6 dofus-combat

**Responsibilities:**
- Turn-based combat logic
- Spell management
- Target selection
- Combat strategies

**Combat State Machine:**
```java
public enum CombatPhase {
    PLACEMENT,      // Initial positioning
    IN_PROGRESS,    // Combat turns
    ENDED           // Combat finished
}

@Service
public class CombatService {
    private final GameStateService gameStateService;
    private final CombatStrategyFactory strategyFactory;

    @EventListener
    public void onCombatStart(CombatStartEvent event) {
        CombatState state = new CombatState();
        state.setPhase(CombatPhase.PLACEMENT);
        state.setStartTime(LocalDateTime.now());

        gameStateService.setCombatState(state);
    }

    @EventListener
    public void onTurnStart(TurnStartEvent event) {
        if (isPlayerTurn(event)) {
            executeTurn();
        }
    }

    private void executeTurn() {
        CombatState state = gameStateService.getCombatState();
        CombatStrategy strategy = strategyFactory.getStrategy(state);

        List<CombatAction> actions = strategy.computeActions(state);
        for (CombatAction action : actions) {
            executeAction(action);
        }

        passTurn();
    }
}
```

**Strategy Pattern:**
```java
public interface CombatStrategy {
    List<CombatAction> computeActions(CombatState state);
    int priority();
}

@Component
public class AggressiveStrategy implements CombatStrategy {
    @Override
    public List<CombatAction> computeActions(CombatState state) {
        List<CombatAction> actions = new ArrayList<>();

        // 1. Select weakest enemy
        Entity target = selectWeakestEnemy(state);

        // 2. Move closer if needed
        if (!isInRange(state.getPlayerPosition(), target)) {
            actions.add(moveTowards(target));
        }

        // 3. Cast highest damage spell
        Spell spell = getHighestDamageSpell(state);
        actions.add(castSpell(spell, target));

        return actions;
    }
}
```

**Turn Management:**
- Timeout handling (45 seconds per turn)
- Action queueing
- Spell cooldown tracking
- Resource management (AP/MP)

### 3.7 dofus-api

**Responsibilities:**
- REST API endpoints
- WebSocket real-time updates
- Request/response DTOs
- API authentication

**REST Endpoints:**
```java
@RestController
@RequestMapping("/api/v1/game")
public class GameStateController {
    private final GameStateService gameStateService;

    @GetMapping("/state")
    public ResponseEntity<PlayerStateDTO> getCurrentState(
        @RequestParam(required = false) String playerId
    ) {
        PlayerState state = gameStateService.getCurrentState(playerId);
        return ResponseEntity.ok(toDTO(state));
    }

    @GetMapping("/state/map")
    public ResponseEntity<MapStateDTO> getCurrentMap() {
        PlayerState state = gameStateService.getCurrentState();
        MapData mapData = state.getCurrentMap();
        return ResponseEntity.ok(toDTO(mapData));
    }
}

@RestController
@RequestMapping("/api/v1/packets")
public class PacketController {
    private final PacketLogRepository packetRepository;

    @GetMapping
    public ResponseEntity<Page<PacketDTO>> getPackets(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime to,
        Pageable pageable
    ) {
        Page<PacketLog> packets = packetRepository.findByFilters(type, from, to, pageable);
        return ResponseEntity.ok(packets.map(this::toDTO));
    }
}
```

**WebSocket Design:**
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/packets").withSockJS();
        registry.addEndpoint("/ws/game-state").withSockJS();
    }
}

@Component
public class PacketWebSocketPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onPacketDecoded(PacketDecodedEvent event) {
        PacketDTO dto = toDTO(event.getPacket());
        messagingTemplate.convertAndSend("/topic/packets", dto);
    }
}
```

**Security Model:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### 3.8 dofus-persistence

**Responsibilities:**
- JPA entities
- Repository interfaces
- Database migrations
- Caching layer

**Database Schema:**
```sql
-- Players table
CREATE TABLE players (
    id VARCHAR(36) PRIMARY KEY,
    character_name VARCHAR(100) NOT NULL UNIQUE,
    level INT NOT NULL,
    experience BIGINT NOT NULL,
    breed VARCHAR(50),
    map_id INT,
    cell_id INT,
    last_seen TIMESTAMP
);

-- Packets table (partitioned by timestamp)
CREATE TABLE packets (
    id BIGSERIAL PRIMARY KEY,
    packet_type VARCHAR(10) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    raw_data BYTEA NOT NULL,
    parsed_data JSONB,
    timestamp TIMESTAMP NOT NULL,
    session_id VARCHAR(36)
) PARTITION BY RANGE (timestamp);

CREATE INDEX idx_packet_timestamp ON packets(timestamp);
CREATE INDEX idx_packet_type ON packets(packet_type);
CREATE INDEX idx_session ON packets(session_id);

-- Maps table
CREATE TABLE maps (
    map_id INT PRIMARY KEY,
    map_name VARCHAR(200),
    width INT NOT NULL,
    height INT NOT NULL,
    cell_data TEXT
);

-- Combat sessions table
CREATE TABLE combat_sessions (
    id BIGSERIAL PRIMARY KEY,
    player_id VARCHAR(36) REFERENCES players(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    result VARCHAR(20)
);
```

**JPA Entities:**
```java
@Entity
@Table(name = "players")
public class Player {
    @Id
    private String id;

    @Column(name = "character_name", nullable = false, unique = true)
    private String characterName;

    private Integer level;
    private Long experience;
    private String breed;

    @Column(name = "map_id")
    private Integer mapId;

    @Column(name = "cell_id")
    private Integer cellId;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY)
    private List<CombatSession> combatSessions;
}
```

**Repository Pattern:**
```java
@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {
    Optional<Player> findByCharacterName(String name);
    List<Player> findByLevelGreaterThan(int level);

    @Query("SELECT p FROM Player p WHERE p.lastSeen > :since")
    List<Player> findRecentlyActive(@Param("since") LocalDateTime since);
}
```

**Caching Strategy:**
```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}

@Service
public class MapService {

    @Cacheable(value = "maps", key = "#mapId")
    public MapData getMap(int mapId) {
        // Load from database
    }
}
```

### 3.9 dofus-web-ui

**Responsibilities:**
- Frontend dashboard
- Real-time visualizations
- Configuration management
- User interaction

**Technology:**
- React 18+ with TypeScript
- Vite for build
- TailwindCSS for styling
- WebSocket client for real-time updates

**Component Structure:**
```
src/
├── components/
│   ├── PacketViewer/
│   │   ├── PacketList.tsx
│   │   ├── PacketDetails.tsx
│   │   └── PacketFilter.tsx
│   ├── MapViewer/
│   │   ├── MapCanvas.tsx
│   │   └── EntityOverlay.tsx
│   ├── CombatLog/
│   │   └── CombatTurn.tsx
│   └── Dashboard/
│       └── StatisticsPanel.tsx
├── services/
│   ├── api.ts
│   └── websocket.ts
└── App.tsx
```

---

## 4. Data Flow

### 4.1 Packet Capture Flow

```
1. Dofus Client ──────► MITM Proxy ──────► Dofus Server
                           │
                           ▼
                   Packet Capture Service
                           │
                           ▼
                   ┌───────┴───────┐
                   │               │
                   ▼               ▼
           Async Processing    Database Storage
                   │               (PostgreSQL)
                   ▼
           Packet Decoder
                   │
                   ▼
          PacketDecodedEvent
                   │
     ┌─────────────┼─────────────┐
     │             │             │
     ▼             ▼             ▼
Game State    Combat Service   WebSocket
  Manager                       Publisher
```

### 4.2 Decoding Pipeline

```
Raw Bytes ──────────────────────────────────────► Packet Object
    │                                                   │
    ▼                                                   ▼
UTF-8 Decode                                   Business Logic
    │                                                   │
    ▼                                                   ▼
Extract Packet ID                              State Updates
    │                                                   │
    ▼                                                   ▼
Registry Lookup                                Event Publishing
    │                                                   │
    ▼                                                   ▼
Instantiate Class                              Downstream Services
    │
    ▼
Parse Fields
```

### 4.3 State Update Flow

```
PacketDecodedEvent
    │
    ▼
GameStateService
    │
    ├─► Update PlayerState
    ├─► Update MapState
    ├─► Update CombatState
    └─► Update InventoryState
    │
    ▼
StateChangedEvent
    │
    ├─► WebSocket Publisher ──► Frontend
    ├─► Combat Service ──► Action Execution
    ├─► Navigation Service ──► Path Calculation
    └─► Database Persister ──► PostgreSQL
```

### 4.4 API Request Flow

```
HTTP Request
    │
    ▼
Spring Controller
    │
    ▼
Service Layer
    │
    ├─► GameStateService ──► In-Memory State
    ├─► PacketRepository ──► PostgreSQL
    └─► MapService ──► Redis Cache
    │
    ▼
DTO Mapping
    │
    ▼
HTTP Response (JSON)
```

---

## 5. Technology Stack

### 5.1 Core Technologies

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Language | Java | 26 | Modern language features (Virtual Threads, Records) |
| Framework | Spring Boot | 3.3.x | Application framework |
| Build Tool | Maven | 3.9+ | Dependency management |
| Database | PostgreSQL | 16+ | Primary data store |
| Cache | Redis | 7+ | High-speed caching |
| Networking | Netty | 4.1+ | High-performance TCP/IP |
| Frontend | React | 18+ | User interface |

### 5.2 Spring Boot Modules Used

- **Spring Web**: REST API controllers
- **Spring WebSocket**: Real-time communication
- **Spring Data JPA**: Database access
- **Spring Data Redis**: Caching
- **Spring Security**: Authentication/authorization
- **Spring Boot Actuator**: Monitoring and health checks
- **Spring Cache**: Caching abstraction
- **Spring Events**: Event-driven architecture

### 5.3 Third-Party Libraries

```xml
<!-- Network -->
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.100.Final</version>
</dependency>

<!-- Utilities -->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>32.1.3-jre</version>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>

<!-- JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
```

### 5.4 Rationale for Technology Choices

**Java 26:**
- Virtual Threads for high concurrency (10,000+ connections)
- Pattern matching for cleaner code
- Records for immutable DTOs
- Strong typing and tooling support

**Spring Boot:**
- Comprehensive ecosystem
- Excellent dependency injection
- Built-in monitoring (Actuator)
- Large community and documentation

**Netty:**
- Industry-standard for high-performance networking
- Non-blocking I/O
- Excellent performance (millions of messages/sec)
- Flexible pipeline architecture

**PostgreSQL:**
- ACID compliance
- JSONB for flexible packet storage
- Table partitioning for large datasets
- Excellent performance and reliability

**Redis:**
- Sub-millisecond latency
- Perfect for caching game state
- Pub/sub for real-time updates
- TTL support for automatic cleanup

---

## 6. Design Decisions

### 6.1 Why MITM Proxy vs Passive Sniffing?

**Decision:** Use MITM (Man-in-the-Middle) proxy approach

**Rationale:**
- **Bidirectional control**: Can intercept both client → server and server → client
- **Modification capability**: Future feature to inject packets
- **Simpler setup**: No need for network adapter promiscuous mode
- **Cross-platform**: Works on Windows, Linux, macOS
- **No admin privileges**: Doesn't require root/administrator

**Trade-offs:**
- Requires client to connect through proxy
- Slight latency increase (< 5ms)
- More complex than passive sniffing

### 6.2 Why Event-Driven Architecture?

**Decision:** Use Spring Events for component communication

**Rationale:**
- **Loose coupling**: Components don't know about each other
- **Easy to extend**: Add new listeners without modifying publishers
- **Testability**: Mock event listeners in tests
- **Async processing**: Events can be processed asynchronously
- **Natural fit**: Game state changes are inherently event-driven

**Example:**
```java
// Publisher doesn't know about listeners
eventPublisher.publishEvent(new MapChangedEvent(state));

// Multiple listeners can respond
@EventListener
public void onMapChanged(MapChangedEvent event) { ... }
```

### 6.3 Database Partitioning Strategy

**Decision:** Partition `packets` table by timestamp (range partitioning)

**Rationale:**
- **Query performance**: Most queries filter by date
- **Automatic archival**: Drop old partitions to free space
- **Maintenance**: Vacuum only recent partitions
- **Index efficiency**: Smaller indexes per partition

**Implementation:**
```sql
CREATE TABLE packets_2025_11 PARTITION OF packets
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');

CREATE TABLE packets_2025_12 PARTITION OF packets
    FOR VALUES FROM ('2025-12-01') TO ('2026-01-01');
```

### 6.4 Caching Strategy

**Decision:** Three-tier caching strategy

```
Level 1: In-Memory (ConcurrentHashMap)
  - Current game state
  - Sub-millisecond access
  - No serialization overhead

Level 2: Redis Cache
  - Map data
  - Spell definitions
  - TTL: 10 minutes

Level 3: PostgreSQL
  - All data
  - Long-term storage
```

**Rationale:**
- **Performance**: Hot data in memory, warm data in Redis
- **Consistency**: Single source of truth in PostgreSQL
- **Scalability**: Redis can scale independently

---

## 7. Security Considerations

### 7.1 Read-Only by Default

**Design Principle:** System operates in read-only mode by default

**Implementation:**
- Packet capture only
- No packet injection by default
- Requires explicit configuration to enable writing

**Configuration:**
```yaml
dofus:
  network:
    read-only: true  # Default
    allow-packet-injection: false
```

### 7.2 API Authentication

**Mechanism:** JWT (JSON Web Tokens)

```java
POST /api/v1/auth/login
{
  "username": "admin",
  "password": "password"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}

# Use token in subsequent requests
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 7.3 Data Privacy

**Principles:**
- Passwords never logged or stored
- Personal data encrypted at rest
- Configurable data retention policies
- GDPR compliance considerations

### 7.4 Ethical Use Guidelines

**Disclaimer:**
```
This software is for EDUCATIONAL PURPOSES ONLY.

- Do not use for commercial gain
- Do not use to gain unfair advantage in-game
- Respect game Terms of Service
- Use responsibly and ethically
```

---

## 8. Performance Considerations

### 8.1 Packet Processing Throughput

**Target:** 10,000+ packets per second

**Optimizations:**
- Async processing with Virtual Threads
- Bounded queues to prevent memory overflow
- Batch database writes
- Zero-copy where possible (Netty)

**Benchmark:**
```java
@BenchmarkMode(Mode.Throughput)
public class PacketDecoderBenchmark {

    @Benchmark
    public Packet decode(Blackhole bh) {
        Packet packet = decoder.decode(SAMPLE_PACKET);
        bh.consume(packet);
        return packet;
    }
}

// Target: > 100,000 ops/sec
```

### 8.2 Memory Management

**Strategy:**
- Limit in-memory packet queue size
- Use weak references for old game states
- Periodic garbage collection hints
- Monitor with Actuator metrics

**Configuration:**
```yaml
dofus:
  capture:
    queue-size: 10000
    max-memory-mb: 512
  state:
    keep-history-minutes: 5
```

### 8.3 Database Query Optimization

**Techniques:**
- Proper indexing (timestamp, packet_type, session_id)
- Table partitioning for large tables
- Connection pooling (HikariCP)
- Query result pagination

**Example:**
```java
@Query("""
    SELECT p FROM PacketLog p
    WHERE p.packetType = :type
    AND p.timestamp BETWEEN :from AND :to
    ORDER BY p.timestamp DESC
    """)
Page<PacketLog> findByTypeAndTimeRange(
    @Param("type") String type,
    @Param("from") LocalDateTime from,
    @Param("to") LocalDateTime to,
    Pageable pageable
);
```

### 8.4 Caching Strategy

**Cache Hierarchy:**
1. **L1 (In-Memory)**: Current player state (< 1ms)
2. **L2 (Redis)**: Map data, spell info (< 5ms)
3. **L3 (PostgreSQL)**: Historical data (< 50ms)

**Cache Invalidation:**
- TTL-based for static data (maps, spells)
- Event-based for dynamic data (player state)

---

## 9. Scalability

### 9.1 Horizontal Scaling Approach

**Design for Scale:**
- Stateless API layer (scales easily)
- Shared state in Redis
- Database connection pooling
- Load balancer ready

```
                  ┌──────────────┐
        ┌────────►│  Instance 1  │
        │         └──────────────┘
        │                │
Load ───┤                ├────► Redis (shared state)
Balancer│                │
        │         ┌──────────────┐
        └────────►│  Instance 2  │
                  └──────────────┘
                         │
                         ▼
                   PostgreSQL
```

### 9.2 Database Sharding

**Sharding Strategy:** Shard by player ID

```
Player ID hash % shard_count = shard_number

Shard 1: players a-m
Shard 2: players n-z
```

**Implementation:**
```java
@Configuration
public class ShardingConfig {

    @Bean
    public DataSource routingDataSource() {
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put("shard1", dataSource1());
        dataSources.put("shard2", dataSource2());

        RoutingDataSource routing = new RoutingDataSource();
        routing.setTargetDataSources(dataSources);
        return routing;
    }
}
```

### 9.3 Load Balancing

**Options:**
- Nginx reverse proxy
- Kubernetes Service (if using K8s)
- AWS Application Load Balancer

**Session Affinity:**
- Not required (stateless design)
- WebSocket connections can reconnect

### 9.4 Stateless Design

**Principles:**
- No session state in application servers
- All state in Redis or database
- API tokens in JWT (self-contained)
- Any instance can serve any request

---

## 10. Extension Points

### 10.1 Custom Packet Handlers

**Interface:**
```java
public interface PacketHandler<T extends Packet> {
    void handle(T packet);
    Class<T> getPacketType();
    int priority();
}

@Component
public class CustomMapPacketHandler implements PacketHandler<GameDataMapPacket> {

    @Override
    public void handle(GameDataMapPacket packet) {
        // Custom logic here
    }

    @Override
    public Class<GameDataMapPacket> getPacketType() {
        return GameDataMapPacket.class;
    }
}
```

**Registration:**
```java
@Configuration
public class HandlerConfig {

    @Autowired
    private List<PacketHandler<?>> handlers;

    @PostConstruct
    public void registerHandlers() {
        handlers.forEach(handlerRegistry::register);
    }
}
```

### 10.2 Plugin System

**Architecture:**
```
┌─────────────────┐
│   Core System   │
└────────┬────────┘
         │
    ┌────┴────┐
    │ Plugins │
    ├─────────┤
    │ Plugin A│ ─► Custom combat strategy
    │ Plugin B│ ─► Custom navigation
    │ Plugin C│ ─► Analytics
    └─────────┘
```

**Plugin Interface:**
```java
public interface DofusPlugin {
    String getName();
    String getVersion();
    void onEnable();
    void onDisable();
}

@Component
public class PluginManager {

    public void loadPlugin(Path pluginJar) {
        URLClassLoader classLoader = new URLClassLoader(
            new URL[] { pluginJar.toUri().toURL() }
        );

        ServiceLoader<DofusPlugin> loader = ServiceLoader.load(
            DofusPlugin.class,
            classLoader
        );

        loader.forEach(plugin -> {
            plugin.onEnable();
            registeredPlugins.add(plugin);
        });
    }
}
```

### 10.3 Strategy Customization

**Combat Strategies:**
```java
// Register custom strategy
@Component
@Profile("custom")
public class MyCustomStrategy implements CombatStrategy {

    @Override
    public List<CombatAction> computeActions(CombatState state) {
        // Your custom combat logic
        return actions;
    }

    @Override
    public int priority() {
        return 100; // Higher priority than default
    }
}
```

### 10.4 Event Listeners

**Custom Event Handling:**
```java
@Component
public class CustomAnalytics {

    @EventListener
    public void onPacketDecoded(PacketDecodedEvent event) {
        // Track custom metrics
        metricsCollector.increment("packets." + event.getPacket().getPacketId());
    }

    @EventListener
    public void onCombatEnd(CombatEndEvent event) {
        // Analyze combat performance
        combatAnalyzer.analyze(event.getCombatSession());
    }
}
```

---

## Appendix A: Architecture Diagrams

### Deployment Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Production Environment                │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  ┌──────────────┐         ┌──────────────┐              │
│  │   Nginx      │────────►│  Spring Boot │              │
│  │   (Reverse   │         │  Application │              │
│  │    Proxy)    │         └──────┬───────┘              │
│  └──────────────┘                │                       │
│         │                         │                       │
│         │                ┌────────┴────────┐             │
│         │                │                 │             │
│         │         ┌──────▼──────┐   ┌─────▼──────┐      │
│         │         │ PostgreSQL  │   │   Redis    │      │
│         │         │  (Primary)  │   │  (Cache)   │      │
│         │         └─────────────┘   └────────────┘      │
│         │                                                 │
│         ▼                                                 │
│  ┌──────────────┐                                        │
│  │  Static      │                                        │
│  │  Assets      │                                        │
│  │  (React UI)  │                                        │
│  └──────────────┘                                        │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

### Component Interaction Sequence

```
Client    Proxy    Decoder    GameState    API    Database
  │         │         │           │         │         │
  ├────────►│         │           │         │         │  1. Send packet
  │         ├────────►│           │         │         │  2. Capture
  │         │         ├──────────►│         │         │  3. Decode
  │         │         │           ├─────────┼────────►│  4. Update state
  │         │         │           │         │         │  5. Persist
  │         │         │           ├────────►│         │  6. Event
  │         │         │           │         ├────────►│  7. WebSocket push
  │◄────────┼─────────┼───────────┼─────────┤         │  8. Real-time update
  │         │         │           │         │         │
```

---

**END OF ARCHITECTURE DOCUMENTATION**

**Document Version:** 1.0
**Total Pages:** ~25
**Last Updated:** 2025-11-08
