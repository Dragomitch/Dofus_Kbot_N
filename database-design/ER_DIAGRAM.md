# Entity-Relationship Diagram
## Dofus Retro Packet Decoder - Database Schema

### ER Diagram (Mermaid Format)

```mermaid
erDiagram
    PLAYERS ||--o{ PACKETS : generates
    PLAYERS ||--o{ INVENTORY_ITEMS : owns
    PLAYERS ||--o{ COMBAT_SESSIONS : participates
    PLAYERS ||--|| PLAYER_STATS : has
    MAPS ||--o{ PLAYERS : contains
    MAPS ||--o{ COMBAT_SESSIONS : hosts
    COMBAT_SESSIONS ||--o{ COMBAT_ACTIONS : contains

    PLAYERS {
        varchar id PK "UUID"
        varchar character_name UK "Unique character name"
        integer level "Character level (default 1)"
        bigint experience "Total XP (default 0)"
        varchar breed "Class type (Iop, Sadida, etc)"
        integer map_id FK "Current map location"
        integer cell_id "Current cell position"
        timestamp last_seen "Last activity timestamp"
        timestamp created_at "Record creation"
        timestamp updated_at "Last update"
    }

    PACKETS {
        bigserial id PK "Auto-increment"
        varchar packet_type "Packet ID (AA, GDM, etc)"
        varchar direction "INBOUND or OUTBOUND"
        bytea raw_data "Raw packet bytes"
        jsonb parsed_data "Parsed JSON data"
        timestamp timestamp "Capture time (partition key)"
        varchar session_id "Session identifier"
        varchar player_id FK "Associated player"
    }

    MAPS {
        integer map_id PK "Game map ID"
        varchar map_name "Map display name"
        integer width "Map width in cells"
        integer height "Map height in cells"
        text cell_data "Walkability matrix JSON"
        timestamp created_at "Record creation"
        timestamp updated_at "Last update"
    }

    COMBAT_SESSIONS {
        bigserial id PK "Auto-increment"
        varchar player_id FK "Player participant"
        integer map_id FK "Combat location"
        timestamp start_time "Combat start"
        timestamp end_time "Combat end (nullable)"
        varchar result "WIN, LOSS, FLEE, ONGOING"
        integer duration_seconds "Total duration"
        timestamp created_at "Record creation"
    }

    COMBAT_ACTIONS {
        bigserial id PK "Auto-increment"
        bigint combat_session_id FK "Parent session"
        integer turn_number "Turn sequence"
        varchar action_type "CAST_SPELL, MOVE, PASS, etc"
        integer spell_id "Spell used (nullable)"
        integer target_cell "Target cell position"
        integer target_entity_id "Target entity (nullable)"
        timestamp timestamp "Action timestamp"
        timestamp created_at "Record creation"
    }

    INVENTORY_ITEMS {
        bigserial id PK "Auto-increment"
        varchar player_id FK "Item owner"
        integer item_id "Game item ID"
        varchar item_name "Item display name"
        integer quantity "Stack size (default 1)"
        integer position "Inventory slot"
        timestamp created_at "Record creation"
        timestamp updated_at "Last update"
    }

    PLAYER_STATS {
        bigserial id PK "Auto-increment"
        varchar player_id FK_UK "Player (unique)"
        integer hp "Current health"
        integer max_hp "Maximum health"
        integer mp "Current movement points"
        integer max_mp "Maximum movement points"
        integer ap "Current action points"
        integer max_ap "Maximum action points"
        integer strength "Strength stat"
        integer intelligence "Intelligence stat"
        integer agility "Agility stat"
        integer vitality "Vitality stat"
        integer wisdom "Wisdom stat"
        bigint kamas "Currency amount"
        timestamp created_at "Record creation"
        timestamp updated_at "Last update"
    }
```

### ASCII Art ER Diagram

```
┌─────────────────┐          ┌─────────────────┐
│    PLAYERS      │          │   PLAYER_STATS  │
├─────────────────┤          ├─────────────────┤
│ *id (PK)        │──────────│ *player_id (FK) │
│  character_name │   1:1    │  hp, max_hp     │
│  level          │          │  mp, max_mp     │
│  experience     │          │  ap, max_ap     │
│  breed          │          │  strength       │
│  map_id (FK)────┼──┐       │  intelligence   │
│  cell_id        │  │       │  agility        │
│  last_seen      │  │       │  vitality       │
└────────┬────────┘  │       │  wisdom, kamas  │
         │           │       └─────────────────┘
         │ 1:N       │
         │           │       ┌─────────────────┐
         ▼           └──────►│      MAPS       │
┌─────────────────┐          ├─────────────────┤
│ INVENTORY_ITEMS │          │ *map_id (PK)    │
├─────────────────┤          │  map_name       │
│ *id (PK)        │          │  width, height  │
│  player_id (FK) │          │  cell_data      │
│  item_id        │          └────────┬────────┘
│  item_name      │                   │
│  quantity       │                   │ 1:N
│  position       │                   ▼
└─────────────────┘          ┌─────────────────┐
                             │ COMBAT_SESSIONS │
┌─────────────────┐          ├─────────────────┤
│    PACKETS      │          │ *id (PK)        │
├─────────────────┤          │  player_id (FK) │
│ *id (PK)        │          │  map_id (FK)    │
│  packet_type    │◄─────────│  start_time     │
│  direction      │   1:N    │  end_time       │
│  raw_data       │          │  result         │
│  parsed_data    │          │  duration_sec   │
│  timestamp      │          └────────┬────────┘
│  session_id     │                   │
│  player_id (FK) │                   │ 1:N
└─────────────────┘                   ▼
  (Partitioned by                ┌─────────────────┐
   timestamp)                     │ COMBAT_ACTIONS  │
                                  ├─────────────────┤
                                  │ *id (PK)        │
                                  │  session_id (FK)│
                                  │  turn_number    │
                                  │  action_type    │
                                  │  spell_id       │
                                  │  target_cell    │
                                  │  target_entity  │
                                  │  timestamp      │
                                  └─────────────────┘
```

### Relationships Summary

| Parent Table      | Child Table       | Relationship | Cardinality | Notes                           |
|-------------------|-------------------|--------------|-------------|---------------------------------|
| PLAYERS           | PLAYER_STATS      | has          | 1:1         | One stats record per player     |
| PLAYERS           | INVENTORY_ITEMS   | owns         | 1:N         | Multiple items per player       |
| PLAYERS           | COMBAT_SESSIONS   | participates | 1:N         | Player history of combats       |
| PLAYERS           | PACKETS           | generates    | 1:N         | Player's packet history         |
| MAPS              | PLAYERS           | contains     | 1:N         | Current player locations        |
| MAPS              | COMBAT_SESSIONS   | hosts        | 1:N         | Combats on specific maps        |
| COMBAT_SESSIONS   | COMBAT_ACTIONS    | contains     | 1:N         | Actions within a combat session |

### Key Design Decisions

#### 1. **Partitioning Strategy**
- **Table:** `PACKETS`
- **Strategy:** Range partitioning by `timestamp` (monthly partitions)
- **Rationale:** High-volume table with time-series data. Partitioning improves:
  - Query performance for time-range queries
  - Maintenance (drop old partitions instead of DELETE)
  - Index size management

#### 2. **JSONB for Parsed Data**
- **Column:** `packets.parsed_data`
- **Rationale:**
  - Flexible schema for 100+ packet types
  - Native PostgreSQL JSON operators for querying
  - GIN indexing for fast JSON field searches
  - Avoids creating 100+ packet-specific tables

#### 3. **Denormalized Stats Table**
- **Table:** `PLAYER_STATS`
- **Rationale:**
  - Stats change frequently during gameplay
  - Separate table reduces UPDATE contention on PLAYERS
  - Easier to track stat history (future enhancement)

#### 4. **UUID for Player IDs**
- **Type:** `VARCHAR(36)`
- **Rationale:**
  - Globally unique identifiers
  - Support for distributed systems
  - No collision risk in multi-instance deployments

#### 5. **Composite Unique Constraint**
- **Table:** `INVENTORY_ITEMS`
- **Constraint:** `(player_id, item_id, position)`
- **Rationale:**
  - Prevents duplicate items in same slot
  - Allows same item in different slots (stacking)

#### 6. **Nullable Timestamps**
- **Column:** `combat_sessions.end_time`
- **Rationale:**
  - Supports ongoing combats (end_time IS NULL)
  - Enables real-time combat tracking

### Scalability Considerations

1. **Packet Table Growth**
   - Expected: ~10,000 packets/hour per active player
   - Monthly partition = ~7.2M packets/month
   - Retention policy: 90 days (auto-drop old partitions)

2. **Index Bloat Prevention**
   - Periodic REINDEX on high-write tables
   - FILLFACTOR tuning for UPDATE-heavy tables

3. **Archive Strategy**
   - Move old combat sessions to archive table after 1 year
   - Export historical packets to cold storage (S3/filesystem)

4. **Read Replicas**
   - Separate analytics queries to read replicas
   - Master for writes, replicas for dashboards/reports
