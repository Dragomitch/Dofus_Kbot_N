# API Usage Guide
## Dofus Retro Packet Decoder REST API

**Version:** 1.0
**Last Updated:** 2025-11-08
**API Version:** v1
**Base URL:** `http://localhost:8080/api/v1`

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Game State Endpoints](#2-game-state-endpoints)
3. [Packet Endpoints](#3-packet-endpoints)
4. [Navigation Endpoints](#4-navigation-endpoints)
5. [Combat Endpoints](#5-combat-endpoints)
6. [WebSocket Endpoints](#6-websocket-endpoints)
7. [Authentication](#7-authentication)
8. [Error Handling](#8-error-handling)
9. [Rate Limiting](#9-rate-limiting)
10. [Code Examples](#10-code-examples)

---

## 1. Getting Started

### 1.1 Base URL

All API requests should be made to:

```
http://localhost:8080/api/v1
```

For production:
```
https://your-domain.com/api/v1
```

### 1.2 Authentication

Most endpoints require authentication via JWT token. See [Authentication](#7-authentication) section for details.

**Example authenticated request:**
```http
GET /api/v1/game/state HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### 1.3 Rate Limiting

- **Default (unauthenticated):** 100 requests per minute per IP
- **Authenticated:** 1000 requests per minute per user
- **WebSocket:** No rate limiting (handled by connection limits)

Headers returned with each request:
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 995
X-RateLimit-Reset: 1699459200
```

### 1.4 Common Response Format

**Success Response:**
```json
{
  "status": "success",
  "data": { ... },
  "timestamp": "2025-11-08T12:00:00Z"
}
```

**Error Response:**
```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Packet with ID 12345 not found",
  "path": "/api/v1/packets/12345"
}
```

---

## 2. Game State Endpoints

### 2.1 Get Current Game State

Retrieve the complete current game state for the authenticated player.

**Endpoint:** `GET /api/v1/game/state`

**Authentication:** Required

**Query Parameters:**
- `playerId` (optional): Specific player ID (admin only)

**Request:**
```http
GET /api/v1/game/state HTTP/1.1
Host: localhost:8080
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "playerId": "550e8400-e29b-41d4-a716-446655440000",
  "characterName": "PlayerName",
  "level": 50,
  "experience": 1234567,
  "breed": "Iop",
  "mapId": 432,
  "position": {
    "x": 100,
    "y": 50,
    "cellId": 250
  },
  "stats": {
    "hp": 500,
    "maxHp": 500,
    "mp": 3,
    "ap": 6,
    "strength": 100,
    "vitality": 150,
    "wisdom": 50,
    "chance": 80,
    "agility": 70,
    "intelligence": 60
  },
  "inCombat": false,
  "combatState": null,
  "nearbyEntities": [
    {
      "id": 12345,
      "name": "MobName",
      "type": "MOB",
      "level": 45,
      "position": {
        "x": 120,
        "y": 60,
        "cellId": 270
      }
    }
  ],
  "lastUpdated": "2025-11-08T12:00:00Z"
}
```

**cURL Example:**
```bash
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/game/state
```

---

### 2.2 Get Current Map State

Retrieve detailed information about the current map.

**Endpoint:** `GET /api/v1/game/state/map`

**Authentication:** Required

**Request:**
```http
GET /api/v1/game/state/map HTTP/1.1
Host: localhost:8080
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "mapId": 432,
  "mapName": "Astrub Village",
  "dimensions": {
    "width": 20,
    "height": 15
  },
  "entities": [
    {
      "id": 1,
      "name": "NPC Merchant",
      "type": "NPC",
      "position": { "x": 10, "y": 5, "cellId": 150 }
    },
    {
      "id": 2,
      "name": "Tofu",
      "type": "MOB",
      "level": 1,
      "position": { "x": 15, "y": 8, "cellId": 230 }
    }
  ],
  "walkable": true,
  "exits": [
    { "direction": "NORTH", "toMapId": 431 },
    { "direction": "EAST", "toMapId": 433 }
  ]
}
```

---

### 2.3 Get Combat State

Retrieve current combat state (only available when in combat).

**Endpoint:** `GET /api/v1/game/state/combat`

**Authentication:** Required

**Request:**
```http
GET /api/v1/game/state/combat HTTP/1.1
Host: localhost:8080
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "inCombat": true,
  "phase": "IN_PROGRESS",
  "turnNumber": 5,
  "isPlayerTurn": true,
  "remainingTime": 42,
  "playerTeam": [
    {
      "id": "player-1",
      "name": "PlayerName",
      "hp": 450,
      "maxHp": 500,
      "ap": 6,
      "mp": 3,
      "position": { "x": 10, "y": 5, "cellId": 150 }
    }
  ],
  "enemyTeam": [
    {
      "id": "mob-123",
      "name": "Tofu",
      "hp": 80,
      "maxHp": 100,
      "ap": 4,
      "mp": 2,
      "position": { "x": 15, "y": 8, "cellId": 230 }
    }
  ],
  "availableActions": [
    {
      "type": "CAST_SPELL",
      "spellId": 101,
      "spellName": "Pressure",
      "apCost": 3,
      "range": "1-5",
      "canCast": true
    },
    {
      "type": "MOVE",
      "availableCells": [150, 151, 152, 165, 166],
      "mpCost": 1
    }
  ]
}
```

**Response (404 Not Found) - When not in combat:**
```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Player is not currently in combat"
}
```

---

### 2.4 Get Player Stats

Retrieve detailed player statistics.

**Endpoint:** `GET /api/v1/game/state/player/stats`

**Authentication:** Required

**Response (200 OK):**
```json
{
  "characterName": "PlayerName",
  "level": 50,
  "experience": 1234567,
  "experienceToNextLevel": 50000,
  "breed": "Iop",
  "stats": {
    "hp": 500,
    "maxHp": 500,
    "mp": 3,
    "ap": 6,
    "strength": 100,
    "vitality": 150,
    "wisdom": 50,
    "chance": 80,
    "agility": 70,
    "intelligence": 60
  },
  "equipment": [
    {
      "slot": "WEAPON",
      "itemId": 123,
      "itemName": "Sword of Power",
      "level": 40
    }
  ],
  "kamas": 1500000,
  "spells": [
    {
      "spellId": 101,
      "name": "Pressure",
      "level": 5,
      "apCost": 3,
      "range": "1-5"
    }
  ]
}
```

---

## 3. Packet Endpoints

### 3.1 List Packets (Paginated)

Retrieve a paginated list of captured packets with optional filtering.

**Endpoint:** `GET /api/v1/packets`

**Authentication:** Required

**Query Parameters:**
- `type` (optional): Filter by packet type (e.g., "GDM", "GTS")
- `direction` (optional): Filter by direction ("INBOUND", "OUTBOUND")
- `from` (optional): Start datetime (ISO 8601 format)
- `to` (optional): End datetime (ISO 8601 format)
- `sessionId` (optional): Filter by session ID
- `page` (optional, default: 0): Page number (0-indexed)
- `size` (optional, default: 20): Page size (max: 100)
- `sort` (optional, default: "timestamp,desc"): Sort criteria

**Request:**
```http
GET /api/v1/packets?type=GDM&from=2025-11-08T00:00:00Z&page=0&size=50 HTTP/1.1
Host: localhost:8080
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 12345,
      "packetType": "GDM",
      "direction": "INBOUND",
      "timestamp": "2025-11-08T12:00:00Z",
      "sessionId": "session-abc-123",
      "parsedData": {
        "mapId": 432,
        "mapDate": "2025-11-08",
        "entities": [...]
      },
      "rawDataSize": 512
    },
    {
      "id": 12346,
      "packetType": "GA",
      "direction": "OUTBOUND",
      "timestamp": "2025-11-08T12:00:01Z",
      "sessionId": "session-abc-123",
      "parsedData": {
        "actionType": 1,
        "parameters": "..."
      },
      "rawDataSize": 128
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 1523,
  "totalPages": 31,
  "last": false,
  "first": true,
  "numberOfElements": 50
}
```

**cURL Example:**
```bash
# Get all GDM packets from the last hour
curl -H "Authorization: Bearer {token}" \
  "http://localhost:8080/api/v1/packets?type=GDM&from=$(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%SZ)&size=100"
```

---

### 3.2 Get Packet by ID

Retrieve a specific packet by its ID, including full raw data.

**Endpoint:** `GET /api/v1/packets/{id}`

**Authentication:** Required

**Path Parameters:**
- `id`: Packet ID (integer)

**Request:**
```http
GET /api/v1/packets/12345 HTTP/1.1
Host: localhost:8080
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": 12345,
  "packetType": "GDM",
  "direction": "INBOUND",
  "timestamp": "2025-11-08T12:00:00Z",
  "sessionId": "session-abc-123",
  "rawData": "R0RNfDQzMnwxMDI0Ozc2OHxwbGF5ZXIxOzEwMDs1MHxtb2IxOzIwMDs3NXxtb2IyOzI1MDs4MA==",
  "rawDataDecoded": "GDM|432|1024;768|player1;100;50|mob1;200;75|mob2;250;80",
  "parsedData": {
    "mapId": 432,
    "mapDate": "2025-11-08",
    "mapKey": "abc123",
    "entities": [
      {
        "name": "player1",
        "x": 100,
        "y": 50
      },
      {
        "name": "mob1",
        "x": 200,
        "y": 75
      }
    ]
  }
}
```

**Response (404 Not Found):**
```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Packet with ID 12345 not found"
}
```

---

### 3.3 Send Custom Packet (Advanced)

Send a custom packet through the proxy (requires admin privileges).

**Endpoint:** `POST /api/v1/packets/send`

**Authentication:** Required (Admin role)

**Request Body:**
```json
{
  "packetType": "GA",
  "direction": "OUTBOUND",
  "data": {
    "actionType": 1,
    "parameters": "100;50"
  }
}
```

**Request:**
```http
POST /api/v1/packets/send HTTP/1.1
Host: localhost:8080
Authorization: Bearer {token}
Content-Type: application/json

{
  "packetType": "GA",
  "direction": "OUTBOUND",
  "data": {
    "actionType": 1,
    "parameters": "100;50"
  }
}
```

**Response (200 OK):**
```json
{
  "status": "sent",
  "packetId": 12347,
  "timestamp": "2025-11-08T12:00:02Z"
}
```

**Response (403 Forbidden):**
```json
{
  "timestamp": "2025-11-08T12:00:02Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Packet sending requires admin privileges and must be explicitly enabled in configuration"
}
```

---

### 3.4 Get Packet Statistics

Retrieve statistics about captured packets.

**Endpoint:** `GET /api/v1/packets/stats`

**Authentication:** Required

**Query Parameters:**
- `from` (optional): Start datetime
- `to` (optional): End datetime

**Response (200 OK):**
```json
{
  "totalPackets": 15234,
  "byType": {
    "GDM": 523,
    "GA": 3421,
    "GTS": 234,
    "OT": 156
  },
  "byDirection": {
    "INBOUND": 8234,
    "OUTBOUND": 7000
  },
  "avgPacketsPerMinute": 42.5,
  "peakPacketsPerSecond": 125,
  "timeRange": {
    "from": "2025-11-08T00:00:00Z",
    "to": "2025-11-08T12:00:00Z"
  }
}
```

---

## 4. Navigation Endpoints

### 4.1 Calculate Path

Calculate the optimal path between two positions on the current map.

**Endpoint:** `POST /api/v1/navigation/path`

**Authentication:** Required

**Request Body:**
```json
{
  "start": {
    "x": 100,
    "y": 50
  },
  "end": {
    "x": 200,
    "y": 150
  },
  "maxMpCost": 10,
  "avoidAggressive": true
}
```

**Request:**
```http
POST /api/v1/navigation/path HTTP/1.1
Host: localhost:8080
Authorization: Bearer {token}
Content-Type: application/json

{
  "start": {"x": 100, "y": 50},
  "end": {"x": 200, "y": 150},
  "maxMpCost": 10
}
```

**Response (200 OK):**
```json
{
  "pathFound": true,
  "path": [
    {"x": 100, "y": 50, "cellId": 250},
    {"x": 110, "y": 60, "cellId": 260},
    {"x": 120, "y": 70, "cellId": 270},
    {"x": 200, "y": 150, "cellId": 450}
  ],
  "mpCost": 8,
  "estimatedTimeSeconds": 4,
  "distance": 150,
  "obstacles": [
    {"x": 115, "y": 65, "type": "MOB", "aggressive": true}
  ]
}
```

**Response (404 Not Found) - No path available:**
```json
{
  "pathFound": false,
  "message": "No valid path found within MP cost constraint",
  "reason": "INSUFFICIENT_MP",
  "requiredMp": 15,
  "availableMp": 10
}
```

---

### 4.2 Get Reachable Cells

Get all cells reachable from current position with available MP.

**Endpoint:** `GET /api/v1/navigation/reachable`

**Authentication:** Required

**Query Parameters:**
- `availableMp` (optional): Available movement points (defaults to player's current MP)
- `avoidAggressive` (optional, default: false): Avoid aggressive mobs

**Response (200 OK):**
```json
{
  "currentPosition": {"x": 100, "y": 50, "cellId": 250},
  "availableMp": 3,
  "reachableCells": [
    {"x": 100, "y": 51, "cellId": 251, "mpCost": 1},
    {"x": 101, "y": 50, "cellId": 252, "mpCost": 1},
    {"x": 100, "y": 52, "cellId": 253, "mpCost": 2},
    {"x": 102, "y": 50, "cellId": 254, "mpCost": 2},
    {"x": 100, "y": 53, "cellId": 255, "mpCost": 3}
  ],
  "totalReachable": 5
}
```

---

### 4.3 Execute Movement

Execute a movement to a target position.

**Endpoint:** `POST /api/v1/navigation/move`

**Authentication:** Required

**Request Body:**
```json
{
  "target": {
    "x": 110,
    "y": 60,
    "cellId": 260
  }
}
```

**Response (200 OK):**
```json
{
  "status": "moving",
  "path": [
    {"x": 100, "y": 50, "cellId": 250},
    {"x": 105, "y": 55, "cellId": 255},
    {"x": 110, "y": 60, "cellId": 260}
  ],
  "estimatedArrivalTime": "2025-11-08T12:00:05Z"
}
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Target cell is not reachable with current MP",
  "details": {
    "requiredMp": 5,
    "availableMp": 3
  }
}
```

---

### 4.4 Find Nearest Entity

Find the nearest entity of a specific type.

**Endpoint:** `GET /api/v1/navigation/nearest`

**Authentication:** Required

**Query Parameters:**
- `entityType`: Entity type (MOB, NPC, PLAYER, RESOURCE)
- `filter` (optional): Additional filter (e.g., level range, name pattern)

**Request:**
```http
GET /api/v1/navigation/nearest?entityType=MOB&filter=level:1-5 HTTP/1.1
Host: localhost:8080
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "entity": {
    "id": 12345,
    "name": "Tofu",
    "type": "MOB",
    "level": 1,
    "position": {"x": 120, "y": 70, "cellId": 270}
  },
  "distance": 25,
  "path": [
    {"x": 100, "y": 50, "cellId": 250},
    {"x": 110, "y": 60, "cellId": 260},
    {"x": 120, "y": 70, "cellId": 270}
  ],
  "mpCost": 2
}
```

---

## 5. Combat Endpoints

### 5.1 Get Available Combat Actions

Get all available actions for the current combat turn.

**Endpoint:** `GET /api/v1/combat/actions`

**Authentication:** Required

**Response (200 OK):**
```json
{
  "isPlayerTurn": true,
  "remainingTime": 42,
  "availableAp": 6,
  "availableMp": 3,
  "actions": [
    {
      "type": "CAST_SPELL",
      "spellId": 101,
      "spellName": "Pressure",
      "apCost": 3,
      "range": {
        "min": 1,
        "max": 5,
        "modifiable": false
      },
      "targets": [
        {"id": "mob-123", "name": "Tofu", "cellId": 270}
      ],
      "canCast": true
    },
    {
      "type": "CAST_SPELL",
      "spellId": 102,
      "spellName": "Intimidation",
      "apCost": 4,
      "range": {
        "min": 1,
        "max": 3
      },
      "targets": [],
      "canCast": true,
      "cooldownRemaining": 0
    },
    {
      "type": "MOVE",
      "mpCost": 1,
      "availableCells": [251, 252, 253, 265, 266, 267]
    },
    {
      "type": "PASS_TURN",
      "apCost": 0,
      "mpCost": 0
    }
  ]
}
```

---

### 5.2 Execute Combat Action

Execute a specific combat action.

**Endpoint:** `POST /api/v1/combat/execute`

**Authentication:** Required

**Request Body (Cast Spell):**
```json
{
  "type": "CAST_SPELL",
  "spellId": 101,
  "targetCellId": 270
}
```

**Request Body (Move):**
```json
{
  "type": "MOVE",
  "targetCellId": 260
}
```

**Request Body (Pass Turn):**
```json
{
  "type": "PASS_TURN"
}
```

**Response (200 OK):**
```json
{
  "status": "executed",
  "action": {
    "type": "CAST_SPELL",
    "spellId": 101,
    "spellName": "Pressure",
    "targetCellId": 270
  },
  "result": {
    "success": true,
    "damage": 45,
    "effects": ["DAMAGE"],
    "targetHpBefore": 100,
    "targetHpAfter": 55
  },
  "remainingAp": 3,
  "remainingMp": 3
}
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot cast spell: insufficient AP",
  "details": {
    "requiredAp": 4,
    "availableAp": 3
  }
}
```

---

### 5.3 Get Combat Strategy

Get the current combat strategy configuration.

**Endpoint:** `GET /api/v1/combat/strategy`

**Authentication:** Required

**Response (200 OK):**
```json
{
  "strategyName": "AGGRESSIVE",
  "priority": "DAMAGE",
  "spellPriorities": [
    {"spellId": 101, "priority": 1, "conditions": ["hp_above_50"]},
    {"spellId": 102, "priority": 2, "conditions": ["multiple_enemies"]}
  ],
  "targetingStrategy": "WEAKEST_FIRST",
  "autoPassTurn": false,
  "maxTurnTime": 40
}
```

---

### 5.4 Update Combat Strategy

Update the combat strategy (requires configuration change).

**Endpoint:** `PUT /api/v1/combat/strategy`

**Authentication:** Required

**Request Body:**
```json
{
  "strategyName": "DEFENSIVE",
  "priority": "SURVIVAL",
  "spellPriorities": [
    {"spellId": 103, "priority": 1},
    {"spellId": 101, "priority": 2}
  ],
  "targetingStrategy": "CLOSEST_FIRST",
  "autoPassTurn": true,
  "maxTurnTime": 30
}
```

**Response (200 OK):**
```json
{
  "status": "updated",
  "message": "Combat strategy updated successfully",
  "activeStrategy": "DEFENSIVE"
}
```

---

## 6. WebSocket Endpoints

### 6.1 Real-Time Packet Stream

Connect to receive real-time packet updates as they are captured and decoded.

**Endpoint:** `ws://localhost:8080/ws/packets`

**Authentication:** JWT token in query parameter or header

**Connection URL:**
```
ws://localhost:8080/ws/packets?token={jwt-token}
```

**JavaScript Example:**
```javascript
const socket = new WebSocket('ws://localhost:8080/ws/packets?token=' + token);

socket.onopen = () => {
  console.log('Connected to packet stream');

  // Subscribe to specific packet types
  socket.send(JSON.stringify({
    action: 'subscribe',
    filters: {
      types: ['GDM', 'GA', 'GTS'],
      direction: 'INBOUND'
    }
  }));
};

socket.onmessage = (event) => {
  const packet = JSON.parse(event.data);
  console.log('Received packet:', packet);

  // Handle packet
  if (packet.packetType === 'GDM') {
    updateMap(packet.parsedData);
  }
};

socket.onerror = (error) => {
  console.error('WebSocket error:', error);
};

socket.onclose = () => {
  console.log('Disconnected from packet stream');
};
```

**Message Format (Server → Client):**
```json
{
  "type": "PACKET",
  "timestamp": "2025-11-08T12:00:00Z",
  "data": {
    "id": 12345,
    "packetType": "GDM",
    "direction": "INBOUND",
    "parsedData": {
      "mapId": 432,
      "entities": [...]
    }
  }
}
```

**Subscription Message (Client → Server):**
```json
{
  "action": "subscribe",
  "filters": {
    "types": ["GDM", "GA"],
    "direction": "INBOUND"
  }
}
```

---

### 6.2 Real-Time Game State Updates

Connect to receive real-time game state changes.

**Endpoint:** `ws://localhost:8080/ws/game-state`

**Connection URL:**
```
ws://localhost:8080/ws/game-state?token={jwt-token}
```

**JavaScript Example:**
```javascript
const socket = new WebSocket('ws://localhost:8080/ws/game-state?token=' + token);

socket.onmessage = (event) => {
  const update = JSON.parse(event.data);

  switch (update.type) {
    case 'MAP_CHANGED':
      console.log('Entered new map:', update.data.mapId);
      break;

    case 'COMBAT_STARTED':
      console.log('Combat started!');
      break;

    case 'TURN_STARTED':
      console.log('Your turn! Available actions:', update.data.actions);
      break;

    case 'STATS_UPDATED':
      console.log('HP:', update.data.stats.hp);
      break;
  }
};
```

**Message Types:**

**MAP_CHANGED:**
```json
{
  "type": "MAP_CHANGED",
  "timestamp": "2025-11-08T12:00:00Z",
  "data": {
    "oldMapId": 431,
    "newMapId": 432,
    "mapName": "Astrub Village",
    "position": {"x": 100, "y": 50, "cellId": 250}
  }
}
```

**COMBAT_STARTED:**
```json
{
  "type": "COMBAT_STARTED",
  "timestamp": "2025-11-08T12:00:01Z",
  "data": {
    "combatId": "combat-123",
    "allies": [...],
    "enemies": [...]
  }
}
```

**TURN_STARTED:**
```json
{
  "type": "TURN_STARTED",
  "timestamp": "2025-11-08T12:00:02Z",
  "data": {
    "turnNumber": 5,
    "isPlayerTurn": true,
    "remainingTime": 45,
    "availableActions": [...]
  }
}
```

---

### 6.3 WebSocket Connection Management

**Ping/Pong (Heartbeat):**
```javascript
// Client sends ping every 30 seconds
setInterval(() => {
  socket.send(JSON.stringify({ action: 'ping' }));
}, 30000);

// Server responds with pong
socket.onmessage = (event) => {
  const message = JSON.parse(event.data);
  if (message.type === 'PONG') {
    console.log('Connection alive');
  }
};
```

**Reconnection Strategy:**
```javascript
let reconnectAttempts = 0;
const maxReconnectAttempts = 5;

function connect() {
  const socket = new WebSocket(wsUrl);

  socket.onclose = () => {
    if (reconnectAttempts < maxReconnectAttempts) {
      reconnectAttempts++;
      const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000);
      console.log(`Reconnecting in ${delay}ms...`);
      setTimeout(connect, delay);
    }
  };

  socket.onopen = () => {
    reconnectAttempts = 0;
  };
}
```

---

## 7. Authentication

### 7.1 Login

Obtain a JWT token for API authentication.

**Endpoint:** `POST /api/v1/auth/login`

**Authentication:** None required

**Request Body:**
```json
{
  "username": "admin",
  "password": "password"
}
```

**Request:**
```http
POST /api/v1/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY5OTQ1NTYwMCwiZXhwIjoxNjk5NDU5MjAwfQ.signature",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshToken": "refresh-token-here",
  "user": {
    "username": "admin",
    "roles": ["ROLE_USER", "ROLE_ADMIN"]
  }
}
```

**Response (401 Unauthorized):**
```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password"
}
```

---

### 7.2 Refresh Token

Refresh an expired JWT token using a refresh token.

**Endpoint:** `POST /api/v1/auth/refresh`

**Authentication:** None required (uses refresh token)

**Request Body:**
```json
{
  "refreshToken": "refresh-token-here"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

### 7.3 Using the Token

Include the JWT token in the `Authorization` header of all authenticated requests:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**cURL Example:**
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  http://localhost:8080/api/v1/game/state
```

**JavaScript Fetch Example:**
```javascript
fetch('http://localhost:8080/api/v1/game/state', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

---

### 7.4 Logout

Invalidate the current JWT token.

**Endpoint:** `POST /api/v1/auth/logout`

**Authentication:** Required

**Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

---

## 8. Error Handling

### 8.1 Error Response Format

All errors follow a consistent format:

```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for field 'mapId'",
  "path": "/api/v1/navigation/path",
  "details": {
    "field": "mapId",
    "rejectedValue": -1,
    "reason": "must be greater than 0"
  }
}
```

### 8.2 Common HTTP Status Codes

| Status Code | Error Type | Description |
|-------------|-----------|-------------|
| 400 | Bad Request | Invalid request parameters or body |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | Insufficient permissions for the requested action |
| 404 | Not Found | Requested resource does not exist |
| 409 | Conflict | Request conflicts with current state (e.g., already in combat) |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Unexpected server error |
| 503 | Service Unavailable | Server temporarily unavailable (e.g., maintenance) |

### 8.3 Error Examples

**Validation Error (400):**
```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "end.x",
      "message": "must not be null"
    },
    {
      "field": "maxMpCost",
      "message": "must be greater than 0"
    }
  ]
}
```

**Authentication Error (401):**
```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token has expired",
  "details": {
    "expiredAt": "2025-11-08T11:00:00Z"
  }
}
```

**Permission Error (403):**
```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Admin role required to send custom packets"
}
```

**Resource Not Found (404):**
```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Packet with ID 99999 not found"
}
```

**Rate Limit Error (429):**
```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later.",
  "retryAfter": 60,
  "limit": 1000,
  "remaining": 0,
  "resetAt": "2025-11-08T12:01:00Z"
}
```

---

## 9. Rate Limiting

### 9.1 Rate Limit Configuration

**Default Limits:**
- Unauthenticated requests: 100 requests/minute per IP
- Authenticated requests: 1000 requests/minute per user
- WebSocket connections: 10 concurrent connections per user

### 9.2 Rate Limit Headers

Every response includes rate limit headers:

```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 995
X-RateLimit-Reset: 1699459200
```

**Header Descriptions:**
- `X-RateLimit-Limit`: Maximum requests allowed in the time window
- `X-RateLimit-Remaining`: Number of requests remaining in current window
- `X-RateLimit-Reset`: Unix timestamp when the rate limit resets

### 9.3 Handling Rate Limits

When rate limit is exceeded, the API returns HTTP 429:

```json
{
  "timestamp": "2025-11-08T12:00:00Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded",
  "retryAfter": 60
}
```

**Best Practices:**
- Monitor `X-RateLimit-Remaining` header
- Implement exponential backoff on 429 responses
- Cache responses when possible
- Use WebSockets for real-time data instead of polling

---

## 10. Code Examples

### 10.1 Java Client Example

**Using RestTemplate:**
```java
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class DofusApiClient {
    private final String baseUrl = "http://localhost:8080/api/v1";
    private final String token;
    private final RestTemplate restTemplate;

    public DofusApiClient(String token) {
        this.token = token;
        this.restTemplate = new RestTemplate();
    }

    public GameState getCurrentGameState() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<GameState> response = restTemplate.exchange(
            baseUrl + "/game/state",
            HttpMethod.GET,
            entity,
            GameState.class
        );

        return response.getBody();
    }

    public Path calculatePath(Position start, Position end, int maxMp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        PathRequest request = new PathRequest(start, end, maxMp);
        HttpEntity<PathRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Path> response = restTemplate.exchange(
            baseUrl + "/navigation/path",
            HttpMethod.POST,
            entity,
            Path.class
        );

        return response.getBody();
    }
}

// Usage
DofusApiClient client = new DofusApiClient("your-jwt-token");
GameState state = client.getCurrentGameState();
System.out.println("Current map: " + state.getMapId());
```

---

### 10.2 JavaScript/TypeScript Client Example

**Axios-based client:**
```typescript
import axios, { AxiosInstance } from 'axios';

interface GameState {
  playerId: string;
  characterName: string;
  mapId: number;
  position: Position;
  stats: Stats;
  inCombat: boolean;
}

interface Position {
  x: number;
  y: number;
  cellId: number;
}

class DofusApiClient {
  private api: AxiosInstance;

  constructor(baseURL: string, token: string) {
    this.api = axios.create({
      baseURL,
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    // Add response interceptor for error handling
    this.api.interceptors.response.use(
      response => response,
      error => {
        if (error.response?.status === 429) {
          const retryAfter = error.response.headers['x-ratelimit-reset'];
          console.warn(`Rate limited. Retry after ${retryAfter}s`);
        }
        return Promise.reject(error);
      }
    );
  }

  async getGameState(): Promise<GameState> {
    const response = await this.api.get<GameState>('/game/state');
    return response.data;
  }

  async calculatePath(
    start: Position,
    end: Position,
    maxMpCost: number
  ): Promise<Path> {
    const response = await this.api.post<Path>('/navigation/path', {
      start,
      end,
      maxMpCost
    });
    return response.data;
  }

  async getPackets(filters: PacketFilters): Promise<PacketPage> {
    const response = await this.api.get<PacketPage>('/packets', {
      params: filters
    });
    return response.data;
  }
}

// Usage
const client = new DofusApiClient(
  'http://localhost:8080/api/v1',
  'your-jwt-token'
);

async function main() {
  try {
    const state = await client.getGameState();
    console.log('Current map:', state.mapId);
    console.log('HP:', state.stats.hp);

    if (!state.inCombat) {
      const path = await client.calculatePath(
        state.position,
        { x: 200, y: 150, cellId: 450 },
        10
      );
      console.log('Path found:', path.path);
    }
  } catch (error) {
    console.error('API error:', error);
  }
}
```

---

### 10.3 Python Client Example

**Using requests library:**
```python
import requests
from typing import Dict, List, Optional
from datetime import datetime

class DofusApiClient:
    def __init__(self, base_url: str, token: str):
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({
            'Authorization': f'Bearer {token}',
            'Content-Type': 'application/json'
        })

    def get_game_state(self, player_id: Optional[str] = None) -> Dict:
        """Get current game state."""
        params = {'playerId': player_id} if player_id else {}
        response = self.session.get(
            f'{self.base_url}/game/state',
            params=params
        )
        response.raise_for_status()
        return response.json()

    def calculate_path(self, start: Dict, end: Dict, max_mp: int) -> Dict:
        """Calculate path between two positions."""
        payload = {
            'start': start,
            'end': end,
            'maxMpCost': max_mp
        }
        response = self.session.post(
            f'{self.base_url}/navigation/path',
            json=payload
        )
        response.raise_for_status()
        return response.json()

    def get_packets(
        self,
        packet_type: Optional[str] = None,
        from_time: Optional[datetime] = None,
        to_time: Optional[datetime] = None,
        page: int = 0,
        size: int = 20
    ) -> Dict:
        """Get paginated packet list."""
        params = {
            'page': page,
            'size': size
        }
        if packet_type:
            params['type'] = packet_type
        if from_time:
            params['from'] = from_time.isoformat()
        if to_time:
            params['to'] = to_time.isoformat()

        response = self.session.get(
            f'{self.base_url}/packets',
            params=params
        )
        response.raise_for_status()
        return response.json()

# Usage
client = DofusApiClient(
    base_url='http://localhost:8080/api/v1',
    token='your-jwt-token'
)

# Get game state
state = client.get_game_state()
print(f"Current map: {state['mapId']}")
print(f"HP: {state['stats']['hp']}/{state['stats']['maxHp']}")

# Calculate path
path = client.calculate_path(
    start={'x': 100, 'y': 50},
    end={'x': 200, 'y': 150},
    max_mp=10
)
print(f"Path found with MP cost: {path['mpCost']}")

# Get recent packets
packets = client.get_packets(
    packet_type='GDM',
    page=0,
    size=50
)
print(f"Total packets: {packets['totalElements']}")
```

---

### 10.4 cURL Examples

**Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'
```

**Get game state:**
```bash
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/game/state
```

**Get packets with filters:**
```bash
curl -H "Authorization: Bearer {token}" \
  "http://localhost:8080/api/v1/packets?type=GDM&from=2025-11-08T00:00:00Z&size=50"
```

**Calculate path:**
```bash
curl -X POST http://localhost:8080/api/v1/navigation/path \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "start": {"x": 100, "y": 50},
    "end": {"x": 200, "y": 150},
    "maxMpCost": 10
  }'
```

**Get available combat actions:**
```bash
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/combat/actions
```

**Execute combat action:**
```bash
curl -X POST http://localhost:8080/api/v1/combat/execute \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "CAST_SPELL",
    "spellId": 101,
    "targetCellId": 270
  }'
```

---

## Appendix: OpenAPI/Swagger Documentation

The API is fully documented using OpenAPI 3.0 specification.

**Access Swagger UI:**
```
http://localhost:8080/swagger-ui.html
```

**Access OpenAPI JSON:**
```
http://localhost:8080/v3/api-docs
```

**Generate client libraries:**
```bash
# Using openapi-generator
openapi-generator generate \
  -i http://localhost:8080/v3/api-docs \
  -g java \
  -o ./generated-client
```

---

**END OF API GUIDE**

**Document Version:** 1.0
**Total Pages:** ~30
**Last Updated:** 2025-11-08

For additional support, see:
- [Architecture Documentation](ARCHITECTURE.md)
- [Deployment Guide](DEPLOYMENT_GUIDE.md)
- [Contributing Guidelines](CONTRIBUTING.md)
