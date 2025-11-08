# Index Strategy Document
## Dofus Retro Packet Decoder - PostgreSQL Index Design

### Overview

This document explains the indexing strategy for the Dofus Retro Packet Decoder database. Proper indexing is critical for query performance, especially for high-volume tables like `packets` and `combat_actions`.

---

## Indexing Principles

### 1. **Selectivity First**
- Index columns with high cardinality (many unique values)
- Low selectivity indexes (e.g., boolean flags) benefit from partial indexes

### 2. **Query Pattern Optimization**
- Indexes based on actual query patterns from the application
- Composite indexes match WHERE clause order

### 3. **Write Performance Balance**
- Minimize indexes on high-write tables (inventory_items, player_stats)
- Each index adds overhead to INSERT/UPDATE operations

### 4. **Covering Indexes**
- Include frequently selected columns in index to avoid table lookups
- PostgreSQL supports `INCLUDE` clause for covering indexes (11+)

### 5. **Partial Indexes**
- Index subsets of data for frequently filtered queries
- Reduces index size and improves performance

---

## Table-by-Table Index Analysis

### PLAYERS Table

#### Indexes:

```sql
-- Primary key (automatic)
PRIMARY KEY (id)

-- Unique constraint (automatic index)
UNIQUE (character_name)

-- Explicit indexes
CREATE INDEX idx_players_character_name ON players(character_name);
CREATE INDEX idx_players_map_id ON players(map_id);
CREATE INDEX idx_players_level ON players(level);
CREATE INDEX idx_players_last_seen ON players(last_seen);
CREATE INDEX idx_players_breed ON players(breed);
```

#### Rationale:

| Index                       | Purpose | Query Pattern | Selectivity |
|-----------------------------|---------|---------------|-------------|
| `id` (PK)                   | Unique lookups | `SELECT * FROM players WHERE id = ?` | Very High |
| `character_name` (UNIQUE)   | Login, character lookup | `SELECT * FROM players WHERE character_name = ?` | Very High |
| `map_id`                    | Find all players on a map | `SELECT * FROM players WHERE map_id = ?` | Medium |
| `level`                     | Find players by level range | `SELECT * FROM players WHERE level BETWEEN ? AND ?` | Medium |
| `last_seen`                 | Find recently active players | `SELECT * FROM players WHERE last_seen > ?` | High |
| `breed`                     | Analytics by class | `SELECT COUNT(*) FROM players GROUP BY breed` | Low-Medium |

#### Optimization Notes:
- **Covering Index for Map Queries:**
  ```sql
  CREATE INDEX idx_players_map_covering ON players(map_id)
      INCLUDE (character_name, level, cell_id);
  ```
  - Avoids table lookup for common queries showing players on a map

---

### PACKETS Table (Partitioned)

#### Indexes:

```sql
-- Primary key (composite for partitioning)
PRIMARY KEY (id, timestamp)

-- Query performance indexes
CREATE INDEX idx_packets_type ON packets(packet_type);
CREATE INDEX idx_packets_timestamp ON packets(timestamp DESC);
CREATE INDEX idx_packets_session ON packets(session_id);
CREATE INDEX idx_packets_player ON packets(player_id);
CREATE INDEX idx_packets_direction ON packets(direction);
CREATE INDEX idx_packets_type_timestamp ON packets(packet_type, timestamp DESC);
CREATE INDEX idx_packets_parsed_data ON packets USING GIN(parsed_data);
```

#### Rationale:

| Index | Purpose | Query Pattern | Selectivity | Notes |
|-------|---------|---------------|-------------|-------|
| `(id, timestamp)` (PK) | Unique identification | Point lookups | Very High | Required for partitioning |
| `packet_type` | Filter by packet type | `WHERE packet_type = 'GDM'` | Medium | 100+ unique types |
| `timestamp DESC` | Recent packets | `ORDER BY timestamp DESC LIMIT 100` | High | DESC for newest-first queries |
| `session_id` | Session packet replay | `WHERE session_id = ? ORDER BY timestamp` | High | Group related packets |
| `player_id` | Player packet history | `WHERE player_id = ? AND timestamp > ?` | High | Player analytics |
| `direction` | Inbound vs outbound | `WHERE direction = 'INBOUND'` | Low | Only 2 values - consider partial index |
| `(packet_type, timestamp)` | Type-specific time queries | `WHERE packet_type = ? AND timestamp > ?` | High | **Composite index** |
| `parsed_data` (GIN) | JSON field queries | `WHERE parsed_data @> '{"mapId": 123}'` | Variable | JSONB search |

#### Optimization Notes:

**1. Partial Index for Recent Packets:**
```sql
CREATE INDEX idx_packets_recent ON packets(timestamp DESC)
    WHERE timestamp > NOW() - INTERVAL '7 days';
```
- Dashboard queries typically focus on recent data
- Smaller index = faster queries

**2. Partial Index for Specific Packet Types:**
```sql
CREATE INDEX idx_packets_combat ON packets(timestamp)
    WHERE packet_type IN ('GS', 'GT', 'GE', 'GA');
```
- Combat-related packets for combat analytics
- Reduces index size by 80-90%

**3. GIN Index on JSONB:**
```sql
CREATE INDEX idx_packets_parsed_data ON packets USING GIN(parsed_data);
```
- Enables fast JSON queries: `WHERE parsed_data @> '{"mapId": 432}'`
- Supports operators: `@>`, `?`, `?&`, `?|`
- Trade-off: Slower writes, faster reads

**4. Partition-Local vs Global Indexes:**
- PostgreSQL creates local indexes on each partition
- No native global indexes (all indexes are partition-local)
- Queries with partition key (timestamp) are most efficient

---

### MAPS Table

#### Indexes:

```sql
-- Primary key
PRIMARY KEY (map_id)

-- Search by name
CREATE INDEX idx_maps_name ON maps(map_name);
```

#### Rationale:

| Index | Purpose | Query Pattern | Selectivity |
|-------|---------|---------------|-------------|
| `map_id` (PK) | Direct map lookup | `WHERE map_id = ?` | Very High |
| `map_name` | Search maps by name | `WHERE map_name LIKE '%Bonta%'` | High |

#### Optimization Notes:
- **Full-Text Search Index (optional):**
  ```sql
  CREATE INDEX idx_maps_name_fts ON maps USING GIN(to_tsvector('english', map_name));
  ```
  - For fuzzy name searches: `WHERE to_tsvector('english', map_name) @@ to_tsquery('Bonta')`

---

### COMBAT_SESSIONS Table

#### Indexes:

```sql
-- Primary key
PRIMARY KEY (id)

-- Foreign keys and filters
CREATE INDEX idx_combat_player ON combat_sessions(player_id);
CREATE INDEX idx_combat_map ON combat_sessions(map_id);
CREATE INDEX idx_combat_start_time ON combat_sessions(start_time DESC);
CREATE INDEX idx_combat_end_time ON combat_sessions(end_time DESC);
CREATE INDEX idx_combat_result ON combat_sessions(result);
CREATE INDEX idx_combat_player_result ON combat_sessions(player_id, result);

-- Partial index for ongoing combats
CREATE INDEX idx_combat_ongoing ON combat_sessions(player_id, start_time)
    WHERE end_time IS NULL;
```

#### Rationale:

| Index | Purpose | Query Pattern | Selectivity | Type |
|-------|---------|---------------|-------------|------|
| `player_id` | Player combat history | `WHERE player_id = ?` | High | Single-column |
| `map_id` | Map-specific combat stats | `WHERE map_id = ?` | Medium | Single-column |
| `start_time DESC` | Recent combats | `ORDER BY start_time DESC LIMIT 50` | High | Single-column |
| `result` | Win/loss statistics | `WHERE result = 'WIN'` | Low-Medium | Single-column |
| `(player_id, result)` | Player win rate | `WHERE player_id = ? AND result = 'WIN'` | High | **Composite** |
| `(player_id, start_time)` WHERE `end_time IS NULL` | Active combat detection | `WHERE player_id = ? AND end_time IS NULL` | Very High | **Partial** |

#### Optimization Notes:

**1. Partial Index for Ongoing Combats:**
- Only indexes rows where `end_time IS NULL`
- Very small index (< 1% of rows typically)
- Perfect for "is player in combat?" queries

**2. Composite Index Strategy:**
```sql
-- Good for: WHERE player_id = ? AND result = ?
CREATE INDEX idx_combat_player_result ON combat_sessions(player_id, result);

-- Also benefits: WHERE player_id = ? (can use first column)
-- Does NOT help: WHERE result = ? (needs first column)
```

---

### COMBAT_ACTIONS Table

#### Indexes:

```sql
-- Primary key
PRIMARY KEY (id)

-- Foreign key and analysis
CREATE INDEX idx_combat_actions_session ON combat_actions(combat_session_id);
CREATE INDEX idx_combat_actions_turn ON combat_actions(combat_session_id, turn_number);
CREATE INDEX idx_combat_actions_spell ON combat_actions(spell_id);
CREATE INDEX idx_combat_actions_type ON combat_actions(action_type);
CREATE INDEX idx_combat_actions_timestamp ON combat_actions(timestamp DESC);
```

#### Rationale:

| Index | Purpose | Query Pattern | Selectivity | Type |
|-------|---------|---------------|-------------|------|
| `combat_session_id` | Load all actions for a combat | `WHERE combat_session_id = ?` | High | Single-column |
| `(combat_session_id, turn_number)` | Turn-by-turn analysis | `WHERE combat_session_id = ? ORDER BY turn_number` | Very High | **Composite** |
| `spell_id` | Spell usage analytics | `WHERE spell_id = ?` | Medium | Single-column |
| `action_type` | Action type distribution | `GROUP BY action_type` | Low-Medium | Single-column |
| `timestamp DESC` | Recent action log | `ORDER BY timestamp DESC LIMIT 100` | High | Single-column |

#### Optimization Notes:

**1. Composite Index for Combat Replay:**
```sql
CREATE INDEX idx_combat_actions_turn ON combat_actions(combat_session_id, turn_number);
```
- Covers query: `SELECT * FROM combat_actions WHERE combat_session_id = ? ORDER BY turn_number`
- More specific than just `combat_session_id` index

**2. Spell Analytics Index:**
```sql
CREATE INDEX idx_spell_analytics ON combat_actions(spell_id)
    INCLUDE (damage_dealt, critical_hit, ap_cost)
    WHERE spell_id IS NOT NULL;
```
- Covering index for spell damage statistics
- Partial index excludes non-spell actions

---

### INVENTORY_ITEMS Table

#### Indexes:

```sql
-- Primary key
PRIMARY KEY (id)

-- Unique constraint (prevents duplicate items in same slot)
UNIQUE (player_id, item_id, position)

-- Query indexes
CREATE INDEX idx_inventory_player ON inventory_items(player_id);
CREATE INDEX idx_inventory_item ON inventory_items(item_id);
CREATE INDEX idx_inventory_position ON inventory_items(player_id, position);
CREATE INDEX idx_inventory_equipped ON inventory_items(player_id) WHERE equipped = TRUE;
```

#### Rationale:

| Index | Purpose | Query Pattern | Selectivity | Type |
|-------|---------|---------------|-------------|------|
| `(player_id, item_id, position)` (UNIQUE) | Prevent duplicates | Integrity constraint | Very High | Composite |
| `player_id` | Load player inventory | `WHERE player_id = ?` | High | Single-column |
| `item_id` | Find who has item | `WHERE item_id = ?` | Medium | Single-column |
| `(player_id, position)` | Slot lookup | `WHERE player_id = ? AND position = ?` | Very High | Composite |
| `player_id WHERE equipped = TRUE` | Load equipped items | `WHERE player_id = ? AND equipped = TRUE` | High | **Partial** |

#### Optimization Notes:

**1. Partial Index for Equipped Items:**
- Only indexes equipped items (~10% of inventory)
- Fast query for "what is player wearing?"
- Reduces index size significantly

---

### PLAYER_STATS Table

#### Indexes:

```sql
-- Primary key
PRIMARY KEY (id)

-- Unique constraint (one stats record per player)
UNIQUE (player_id)

-- Temporal index
CREATE INDEX idx_player_stats_updated ON player_stats(updated_at DESC);
```

#### Rationale:

| Index | Purpose | Query Pattern | Selectivity |
|-------|---------|---------------|-------------|
| `player_id` (UNIQUE) | Direct player stats lookup | `WHERE player_id = ?` | Very High |
| `updated_at DESC` | Recently updated stats | `WHERE updated_at > ?` | High |

#### Optimization Notes:
- Minimal indexing due to 1:1 relationship with players
- Queries typically use `player_id` (already indexed via UNIQUE constraint)

---

## Index Maintenance Strategy

### 1. **Regular Monitoring**

```sql
-- Check index usage statistics
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan as index_scans,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY idx_scan ASC;
```

**Action:** Drop indexes with `idx_scan = 0` after observing for 1 month

### 2. **Index Bloat Detection**

```sql
-- Check index bloat
SELECT
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) as index_size,
    pg_size_pretty(pg_relation_size(relid)) as table_size,
    round(100 * pg_relation_size(indexrelid) / NULLIF(pg_relation_size(relid), 0), 2) as index_ratio
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY pg_relation_size(indexrelid) DESC;
```

**Action:** REINDEX if index size > 50% of table size

### 3. **Reindex Schedule**

```sql
-- Monthly reindex for high-churn tables
REINDEX TABLE CONCURRENTLY players;
REINDEX TABLE CONCURRENTLY inventory_items;
REINDEX TABLE CONCURRENTLY player_stats;

-- Quarterly reindex for partitioned tables
REINDEX TABLE CONCURRENTLY packets_2025_11;
```

### 4. **Vacuum Schedule**

```sql
-- Weekly VACUUM ANALYZE
VACUUM ANALYZE players;
VACUUM ANALYZE combat_sessions;
VACUUM ANALYZE inventory_items;
```

---

## Query Pattern Examples

### Example 1: Find Player's Recent Packets

```sql
-- Query
SELECT packet_type, timestamp, parsed_data
FROM packets
WHERE player_id = 'abc123'
  AND timestamp > NOW() - INTERVAL '1 hour'
ORDER BY timestamp DESC
LIMIT 100;

-- Index used: idx_packets_player, idx_packets_timestamp
-- Performance: < 10ms
```

### Example 2: Combat Win Rate by Map

```sql
-- Query
SELECT
    map_id,
    COUNT(*) FILTER (WHERE result = 'WIN') * 100.0 / COUNT(*) as win_rate
FROM combat_sessions
WHERE player_id = 'abc123'
  AND end_time IS NOT NULL
GROUP BY map_id;

-- Index used: idx_combat_player_result
-- Performance: < 50ms
```

### Example 3: Find Equipped Items

```sql
-- Query
SELECT item_id, item_name, position
FROM inventory_items
WHERE player_id = 'abc123'
  AND equipped = TRUE;

-- Index used: idx_inventory_equipped (partial index)
-- Performance: < 5ms
```

### Example 4: Packet Type Statistics (Last 24h)

```sql
-- Query
SELECT
    packet_type,
    COUNT(*) as count,
    MIN(timestamp) as first_seen,
    MAX(timestamp) as last_seen
FROM packets
WHERE timestamp > NOW() - INTERVAL '24 hours'
GROUP BY packet_type
ORDER BY count DESC;

-- Index used: idx_packets_timestamp, idx_packets_type
-- Performance: < 100ms (depending on volume)
```

---

## Index Size Estimates

Based on expected data volumes:

| Table | Rows (estimate) | Index Count | Total Index Size |
|-------|-----------------|-------------|------------------|
| players | 10,000 | 6 | 5 MB |
| packets | 100M (partitioned) | 8 per partition | 500 MB per month |
| maps | 50,000 | 2 | 3 MB |
| combat_sessions | 1M | 7 | 80 MB |
| combat_actions | 50M | 5 | 400 MB |
| inventory_items | 100,000 | 5 | 10 MB |
| player_stats | 10,000 | 2 | 2 MB |

**Total Estimated Index Size:** ~1.5 GB (for 3 months of packet data)

---

## Performance Tuning Recommendations

### 1. **PostgreSQL Configuration**

```ini
# postgresql.conf optimizations for indexing

# Increase memory for sorts/hash operations
work_mem = 256MB

# Larger buffer cache for frequently accessed indexes
shared_buffers = 4GB

# More aggressive autovacuum for high-write tables
autovacuum_vacuum_scale_factor = 0.05
autovacuum_analyze_scale_factor = 0.02

# Enable query plan optimization
random_page_cost = 1.1  # SSD storage
effective_cache_size = 12GB
```

### 2. **Index-Only Scans**

Enable by including frequently selected columns in indexes:

```sql
-- Before (table scan required)
CREATE INDEX idx_combat_player ON combat_sessions(player_id);

-- After (index-only scan possible)
CREATE INDEX idx_combat_player_covering ON combat_sessions(player_id)
    INCLUDE (start_time, end_time, result);
```

### 3. **Monitoring Slow Queries**

```sql
-- Enable query logging
ALTER DATABASE dofus_decoder SET log_min_duration_statement = 100;

-- Check slow queries
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
WHERE mean_time > 100
ORDER BY mean_time DESC;
```

---

## Future Index Considerations

### 1. **Bitmap Indexes (Enterprise Extension)**
- For low-cardinality columns (direction, result, breed)
- Requires PostgreSQL Enterprise or Citus extension

### 2. **Bloom Filters**
- For multi-column OR queries
- Useful for packet filtering UI

### 3. **Expression Indexes**
```sql
-- Index on computed column
CREATE INDEX idx_combat_duration ON combat_sessions(EXTRACT(EPOCH FROM (end_time - start_time)));
```

### 4. **Geospatial Indexes (PostGIS)**
- If adding map coordinate features
- `CREATE INDEX idx_maps_geometry ON maps USING GIST(geometry);`

---

## Conclusion

This index strategy balances:
- **Read Performance:** Optimized for common query patterns
- **Write Performance:** Minimal overhead on high-frequency writes
- **Storage Efficiency:** Partial indexes reduce size
- **Maintainability:** Clear purpose and monitoring strategy

**Key Takeaways:**
1. Use composite indexes for multi-column WHERE clauses
2. Partial indexes for frequently filtered subsets
3. GIN indexes for JSONB queries
4. Regular monitoring and reindexing
5. Partition-aware indexing for time-series data

---

**Document Version:** 1.0
**Last Updated:** 2025-11-08
**Author:** Agent A2 - Database Architect
