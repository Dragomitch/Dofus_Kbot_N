-- ============================================================================
-- Dofus Retro Packet Decoder - Database Schema
-- ============================================================================
-- Version: 1.0
-- Date: 2025-11-08
-- Database: PostgreSQL 16+
-- Description: Complete schema for packet capture, game state, and analytics
-- ============================================================================

-- ============================================================================
-- EXTENSION SETUP
-- ============================================================================

-- Enable UUID generation (if using UUID type in future)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable pg_trgm for fuzzy text search (optional for player/item names)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ============================================================================
-- TABLE: PLAYERS
-- ============================================================================
-- Description: Core player/character information
-- Estimated rows: 1K-10K (moderate growth)
-- Update frequency: High (position updates)
-- ============================================================================

CREATE TABLE players (
    id VARCHAR(36) PRIMARY KEY,
    character_name VARCHAR(100) NOT NULL UNIQUE,
    level INTEGER NOT NULL DEFAULT 1 CHECK (level >= 1 AND level <= 200),
    experience BIGINT NOT NULL DEFAULT 0 CHECK (experience >= 0),
    breed VARCHAR(50),
    map_id INTEGER,
    cell_id INTEGER CHECK (cell_id >= 0 AND cell_id <= 559), -- Dofus has max 559 cells
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for players table
CREATE INDEX idx_players_character_name ON players(character_name);
CREATE INDEX idx_players_map_id ON players(map_id);
CREATE INDEX idx_players_level ON players(level);
CREATE INDEX idx_players_last_seen ON players(last_seen);
CREATE INDEX idx_players_breed ON players(breed);

-- Comments for documentation
COMMENT ON TABLE players IS 'Core player/character information including position and basic stats';
COMMENT ON COLUMN players.id IS 'Unique player identifier (UUID format)';
COMMENT ON COLUMN players.character_name IS 'In-game character name (unique constraint)';
COMMENT ON COLUMN players.breed IS 'Character class: Iop, Sadida, Eniripsa, Enutrof, Sram, Xelor, Ecaflip, Feca, Sacrieur, Cra, Osamodas, Pandawa';
COMMENT ON COLUMN players.cell_id IS 'Current cell position on map (0-559 for standard Dofus maps)';

-- ============================================================================
-- TABLE: PACKETS (PARTITIONED)
-- ============================================================================
-- Description: All captured network packets with raw and parsed data
-- Estimated rows: Millions (10K packets/hour/player)
-- Partition strategy: Monthly range partitioning by timestamp
-- Retention: 90 days (drop old partitions)
-- ============================================================================

CREATE TABLE packets (
    id BIGSERIAL,
    packet_type VARCHAR(10) NOT NULL,
    direction VARCHAR(10) NOT NULL CHECK (direction IN ('INBOUND', 'OUTBOUND')),
    raw_data BYTEA NOT NULL,
    parsed_data JSONB,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    session_id VARCHAR(36),
    player_id VARCHAR(36) REFERENCES players(id) ON DELETE SET NULL,
    PRIMARY KEY (id, timestamp)  -- Composite key required for partitioning
) PARTITION BY RANGE (timestamp);

-- Create initial partitions (current month + next 3 months)
CREATE TABLE packets_2025_11 PARTITION OF packets
    FOR VALUES FROM ('2025-11-01 00:00:00') TO ('2025-12-01 00:00:00');

CREATE TABLE packets_2025_12 PARTITION OF packets
    FOR VALUES FROM ('2025-12-01 00:00:00') TO ('2026-01-01 00:00:00');

CREATE TABLE packets_2026_01 PARTITION OF packets
    FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-02-01 00:00:00');

CREATE TABLE packets_2026_02 PARTITION OF packets
    FOR VALUES FROM ('2026-02-01 00:00:00') TO ('2026-03-01 00:00:00');

-- Indexes for packets table (created on parent, inherited by partitions)
CREATE INDEX idx_packets_type ON packets(packet_type);
CREATE INDEX idx_packets_timestamp ON packets(timestamp DESC);
CREATE INDEX idx_packets_session ON packets(session_id);
CREATE INDEX idx_packets_player ON packets(player_id);
CREATE INDEX idx_packets_direction ON packets(direction);
CREATE INDEX idx_packets_type_timestamp ON packets(packet_type, timestamp DESC);
CREATE INDEX idx_packets_parsed_data ON packets USING GIN(parsed_data);

-- Comments
COMMENT ON TABLE packets IS 'Partitioned table storing all network packets (raw and parsed). Partitioned monthly for performance.';
COMMENT ON COLUMN packets.packet_type IS 'Packet identifier (AA, GDM, GA, etc.) - 1-3 characters';
COMMENT ON COLUMN packets.direction IS 'Packet direction: INBOUND (server->client) or OUTBOUND (client->server)';
COMMENT ON COLUMN packets.raw_data IS 'Raw packet bytes as captured from network';
COMMENT ON COLUMN packets.parsed_data IS 'JSON representation of decoded packet fields';
COMMENT ON COLUMN packets.session_id IS 'Game session identifier for grouping related packets';

-- ============================================================================
-- TABLE: MAPS
-- ============================================================================
-- Description: Game map metadata and walkability data
-- Estimated rows: 10K-50K (Dofus has thousands of maps)
-- Update frequency: Low (static reference data)
-- ============================================================================

CREATE TABLE maps (
    map_id INTEGER PRIMARY KEY,
    map_name VARCHAR(200),
    width INTEGER NOT NULL DEFAULT 14,
    height INTEGER NOT NULL DEFAULT 20,
    cell_data TEXT,  -- JSON string of cell properties (walkable, obstacle, etc.)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for maps
CREATE INDEX idx_maps_name ON maps(map_name);

-- Comments
COMMENT ON TABLE maps IS 'Game map definitions including dimensions and cell walkability data';
COMMENT ON COLUMN maps.map_id IS 'Game-assigned map identifier (from GDM packets)';
COMMENT ON COLUMN maps.cell_data IS 'JSON array of cell properties: [{cellId: 0, walkable: true, lineOfSight: true}, ...]';

-- ============================================================================
-- TABLE: COMBAT_SESSIONS
-- ============================================================================
-- Description: Combat engagement history
-- Estimated rows: 100K-1M (depends on usage)
-- Update frequency: Medium (start/end updates)
-- ============================================================================

CREATE TABLE combat_sessions (
    id BIGSERIAL PRIMARY KEY,
    player_id VARCHAR(36) REFERENCES players(id) ON DELETE CASCADE,
    map_id INTEGER REFERENCES maps(map_id) ON DELETE SET NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,  -- NULL if combat is ongoing
    result VARCHAR(20) CHECK (result IN ('WIN', 'LOSS', 'FLEE', 'ONGOING', 'DRAW')),
    duration_seconds INTEGER CHECK (duration_seconds >= 0),
    experience_gained BIGINT DEFAULT 0,
    kamas_gained BIGINT DEFAULT 0,
    items_looted INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for combat_sessions
CREATE INDEX idx_combat_player ON combat_sessions(player_id);
CREATE INDEX idx_combat_map ON combat_sessions(map_id);
CREATE INDEX idx_combat_start_time ON combat_sessions(start_time DESC);
CREATE INDEX idx_combat_end_time ON combat_sessions(end_time DESC);
CREATE INDEX idx_combat_result ON combat_sessions(result);
CREATE INDEX idx_combat_player_result ON combat_sessions(player_id, result);

-- Partial index for ongoing combats (frequently queried)
CREATE INDEX idx_combat_ongoing ON combat_sessions(player_id, start_time)
    WHERE end_time IS NULL;

-- Comments
COMMENT ON TABLE combat_sessions IS 'Historical record of all combat engagements';
COMMENT ON COLUMN combat_sessions.result IS 'Combat outcome: WIN, LOSS, FLEE, ONGOING, DRAW';
COMMENT ON COLUMN combat_sessions.duration_seconds IS 'Calculated from end_time - start_time';

-- ============================================================================
-- TABLE: COMBAT_ACTIONS
-- ============================================================================
-- Description: Individual actions within combat sessions
-- Estimated rows: Millions (20-50 actions per combat)
-- Update frequency: High during combat
-- ============================================================================

CREATE TABLE combat_actions (
    id BIGSERIAL PRIMARY KEY,
    combat_session_id BIGINT REFERENCES combat_sessions(id) ON DELETE CASCADE,
    turn_number INTEGER NOT NULL CHECK (turn_number >= 1),
    action_type VARCHAR(50) NOT NULL,
    spell_id INTEGER,
    spell_name VARCHAR(100),
    target_cell INTEGER,
    target_entity_id INTEGER,
    damage_dealt INTEGER DEFAULT 0,
    ap_cost INTEGER DEFAULT 0,
    mp_cost INTEGER DEFAULT 0,
    critical_hit BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for combat_actions
CREATE INDEX idx_combat_actions_session ON combat_actions(combat_session_id);
CREATE INDEX idx_combat_actions_turn ON combat_actions(combat_session_id, turn_number);
CREATE INDEX idx_combat_actions_spell ON combat_actions(spell_id);
CREATE INDEX idx_combat_actions_type ON combat_actions(action_type);
CREATE INDEX idx_combat_actions_timestamp ON combat_actions(timestamp DESC);

-- Comments
COMMENT ON TABLE combat_actions IS 'Detailed log of every action taken during combat';
COMMENT ON COLUMN combat_actions.action_type IS 'Type: CAST_SPELL, MOVE, PASS_TURN, USE_ITEM, FLEE_ATTEMPT, etc.';
COMMENT ON COLUMN combat_actions.turn_number IS 'Turn sequence number within the combat session';

-- ============================================================================
-- TABLE: INVENTORY_ITEMS
-- ============================================================================
-- Description: Player inventory state
-- Estimated rows: 10K-100K (60 items per player average)
-- Update frequency: High (frequent item changes)
-- ============================================================================

CREATE TABLE inventory_items (
    id BIGSERIAL PRIMARY KEY,
    player_id VARCHAR(36) REFERENCES players(id) ON DELETE CASCADE,
    item_id INTEGER NOT NULL,
    item_name VARCHAR(200),
    quantity INTEGER DEFAULT 1 CHECK (quantity >= 0),
    position INTEGER,  -- Inventory slot number
    equipped BOOLEAN DEFAULT FALSE,
    item_type VARCHAR(50),  -- weapon, armor, consumable, resource, quest, etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(player_id, item_id, position)  -- Prevent duplicate items in same slot
);

-- Indexes for inventory_items
CREATE INDEX idx_inventory_player ON inventory_items(player_id);
CREATE INDEX idx_inventory_item ON inventory_items(item_id);
CREATE INDEX idx_inventory_position ON inventory_items(player_id, position);
CREATE INDEX idx_inventory_equipped ON inventory_items(player_id) WHERE equipped = TRUE;

-- Comments
COMMENT ON TABLE inventory_items IS 'Current inventory state for all players';
COMMENT ON COLUMN inventory_items.position IS 'Inventory slot number (0-based index)';
COMMENT ON COLUMN inventory_items.equipped IS 'TRUE if item is currently equipped';

-- ============================================================================
-- TABLE: PLAYER_STATS
-- ============================================================================
-- Description: Denormalized player statistics
-- Estimated rows: 1K-10K (one per player)
-- Update frequency: Very high (stats change frequently)
-- ============================================================================

CREATE TABLE player_stats (
    id BIGSERIAL PRIMARY KEY,
    player_id VARCHAR(36) UNIQUE REFERENCES players(id) ON DELETE CASCADE,

    -- Health
    hp INTEGER DEFAULT 0 CHECK (hp >= 0),
    max_hp INTEGER DEFAULT 0 CHECK (max_hp >= 0),

    -- Action/Movement points
    mp INTEGER DEFAULT 0 CHECK (mp >= 0),
    max_mp INTEGER DEFAULT 0 CHECK (max_mp >= 0),
    ap INTEGER DEFAULT 0 CHECK (ap >= 0),
    max_ap INTEGER DEFAULT 0 CHECK (max_ap >= 0),

    -- Core stats
    strength INTEGER DEFAULT 0 CHECK (strength >= 0),
    intelligence INTEGER DEFAULT 0 CHECK (intelligence >= 0),
    agility INTEGER DEFAULT 0 CHECK (agility >= 0),
    vitality INTEGER DEFAULT 0 CHECK (vitality >= 0),
    wisdom INTEGER DEFAULT 0 CHECK (wisdom >= 0),
    chance INTEGER DEFAULT 0 CHECK (chance >= 0),

    -- Resistances
    neutral_resistance INTEGER DEFAULT 0,
    earth_resistance INTEGER DEFAULT 0,
    fire_resistance INTEGER DEFAULT 0,
    water_resistance INTEGER DEFAULT 0,
    air_resistance INTEGER DEFAULT 0,

    -- Currency
    kamas BIGINT DEFAULT 0 CHECK (kamas >= 0),

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for player_stats (unique constraint already creates index on player_id)
CREATE INDEX idx_player_stats_updated ON player_stats(updated_at DESC);

-- Comments
COMMENT ON TABLE player_stats IS 'Denormalized player statistics for fast access (separate from players table to reduce UPDATE contention)';
COMMENT ON COLUMN player_stats.hp IS 'Current health points';
COMMENT ON COLUMN player_stats.ap IS 'Action points (used for spells in combat)';
COMMENT ON COLUMN player_stats.mp IS 'Movement points (used for moving)';

-- ============================================================================
-- TABLE: SPELLS
-- ============================================================================
-- Description: Reference table for spell definitions
-- Estimated rows: 500-1000 (static game data)
-- Update frequency: Very low (game updates only)
-- ============================================================================

CREATE TABLE spells (
    spell_id INTEGER PRIMARY KEY,
    spell_name VARCHAR(100) NOT NULL,
    spell_description TEXT,
    min_range INTEGER DEFAULT 0,
    max_range INTEGER DEFAULT 0,
    ap_cost INTEGER DEFAULT 0,
    cooldown INTEGER DEFAULT 0,
    element VARCHAR(20),  -- neutral, earth, fire, water, air
    spell_type VARCHAR(50),  -- damage, heal, buff, debuff, summon
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for spells
CREATE INDEX idx_spells_name ON spells(spell_name);
CREATE INDEX idx_spells_element ON spells(element);

-- Comments
COMMENT ON TABLE spells IS 'Reference data for all spells in the game';

-- ============================================================================
-- TABLE: SESSIONS
-- ============================================================================
-- Description: Game session tracking
-- Estimated rows: 10K-100K
-- Update frequency: Medium
-- ============================================================================

CREATE TABLE sessions (
    session_id VARCHAR(36) PRIMARY KEY,
    player_id VARCHAR(36) REFERENCES players(id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    ip_address VARCHAR(45),  -- IPv6 support
    client_version VARCHAR(20),
    server_name VARCHAR(100),
    total_packets INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for sessions
CREATE INDEX idx_sessions_player ON sessions(player_id);
CREATE INDEX idx_sessions_start_time ON sessions(start_time DESC);
CREATE INDEX idx_sessions_active ON sessions(player_id) WHERE end_time IS NULL;

-- Comments
COMMENT ON TABLE sessions IS 'Tracking of game sessions for grouping packets and analytics';

-- ============================================================================
-- MATERIALIZED VIEW: COMBAT_STATISTICS
-- ============================================================================
-- Description: Pre-aggregated combat statistics for performance
-- Refresh: Hourly via cron job
-- ============================================================================

CREATE MATERIALIZED VIEW combat_statistics AS
SELECT
    player_id,
    COUNT(*) as total_combats,
    COUNT(*) FILTER (WHERE result = 'WIN') as wins,
    COUNT(*) FILTER (WHERE result = 'LOSS') as losses,
    COUNT(*) FILTER (WHERE result = 'FLEE') as flees,
    ROUND(AVG(duration_seconds), 2) as avg_duration_seconds,
    SUM(experience_gained) as total_experience_gained,
    SUM(kamas_gained) as total_kamas_gained,
    SUM(items_looted) as total_items_looted,
    MAX(start_time) as last_combat_time
FROM combat_sessions
WHERE end_time IS NOT NULL
GROUP BY player_id;

-- Index for materialized view
CREATE UNIQUE INDEX idx_combat_stats_player ON combat_statistics(player_id);

-- Comments
COMMENT ON MATERIALIZED VIEW combat_statistics IS 'Pre-aggregated combat statistics for dashboard queries (refresh hourly)';

-- ============================================================================
-- MATERIALIZED VIEW: PACKET_STATISTICS
-- ============================================================================
-- Description: Packet type distribution for monitoring
-- Refresh: Every 15 minutes
-- ============================================================================

CREATE MATERIALIZED VIEW packet_statistics AS
SELECT
    packet_type,
    direction,
    COUNT(*) as packet_count,
    MIN(timestamp) as first_seen,
    MAX(timestamp) as last_seen,
    DATE_TRUNC('hour', timestamp) as hour_bucket
FROM packets
WHERE timestamp > NOW() - INTERVAL '24 hours'
GROUP BY packet_type, direction, DATE_TRUNC('hour', timestamp);

-- Index for materialized view
CREATE INDEX idx_packet_stats_type ON packet_statistics(packet_type);
CREATE INDEX idx_packet_stats_hour ON packet_statistics(hour_bucket DESC);

-- ============================================================================
-- FUNCTIONS AND TRIGGERS
-- ============================================================================

-- Function to automatically update 'updated_at' timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply update trigger to relevant tables
CREATE TRIGGER update_players_updated_at BEFORE UPDATE ON players
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_maps_updated_at BEFORE UPDATE ON maps
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_inventory_items_updated_at BEFORE UPDATE ON inventory_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_player_stats_updated_at BEFORE UPDATE ON player_stats
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_spells_updated_at BEFORE UPDATE ON spells
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to calculate combat duration automatically
CREATE OR REPLACE FUNCTION calculate_combat_duration()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.end_time IS NOT NULL AND NEW.start_time IS NOT NULL THEN
        NEW.duration_seconds = EXTRACT(EPOCH FROM (NEW.end_time - NEW.start_time))::INTEGER;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER calculate_combat_duration_trigger
    BEFORE INSERT OR UPDATE ON combat_sessions
    FOR EACH ROW EXECUTE FUNCTION calculate_combat_duration();

-- ============================================================================
-- PARTITION MANAGEMENT FUNCTION
-- ============================================================================
-- Description: Automatically creates next month's partition
-- Usage: Call monthly via cron or pg_cron extension
-- ============================================================================

CREATE OR REPLACE FUNCTION create_next_packets_partition()
RETURNS void AS $$
DECLARE
    next_month_start DATE;
    next_month_end DATE;
    partition_name TEXT;
BEGIN
    next_month_start := DATE_TRUNC('month', CURRENT_DATE + INTERVAL '1 month');
    next_month_end := DATE_TRUNC('month', CURRENT_DATE + INTERVAL '2 months');
    partition_name := 'packets_' || TO_CHAR(next_month_start, 'YYYY_MM');

    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF packets FOR VALUES FROM (%L) TO (%L)',
        partition_name,
        next_month_start,
        next_month_end
    );

    RAISE NOTICE 'Created partition: %', partition_name;
END;
$$ LANGUAGE plpgsql;

-- Function to drop old partitions (older than 90 days)
CREATE OR REPLACE FUNCTION drop_old_packets_partitions()
RETURNS void AS $$
DECLARE
    partition_record RECORD;
    cutoff_date DATE;
BEGIN
    cutoff_date := CURRENT_DATE - INTERVAL '90 days';

    FOR partition_record IN
        SELECT tablename
        FROM pg_tables
        WHERE schemaname = 'public'
          AND tablename LIKE 'packets_%'
          AND tablename ~ '^\w+_\d{4}_\d{2}$'
    LOOP
        -- Extract date from partition name and check if old
        IF TO_DATE(SUBSTRING(partition_record.tablename FROM '\d{4}_\d{2}$'), 'YYYY_MM') < cutoff_date THEN
            EXECUTE format('DROP TABLE IF EXISTS %I', partition_record.tablename);
            RAISE NOTICE 'Dropped old partition: %', partition_record.tablename;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- INITIAL DATA / REFERENCE DATA
-- ============================================================================

-- Insert common breeds
INSERT INTO players (id, character_name, level, breed) VALUES
    ('00000000-0000-0000-0000-000000000000', 'SYSTEM', 1, 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

COMMENT ON TABLE players IS 'SYSTEM player (id all zeros) used for server-generated events';

-- ============================================================================
-- GRANTS AND PERMISSIONS
-- ============================================================================
-- Note: Adjust these based on your security requirements

-- Example: Create read-only user for analytics
-- CREATE ROLE analytics_user WITH LOGIN PASSWORD 'secure_password';
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO analytics_user;
-- GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO analytics_user;

-- Example: Application user with full access
-- CREATE ROLE app_user WITH LOGIN PASSWORD 'secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- ============================================================================
-- MAINTENANCE RECOMMENDATIONS
-- ============================================================================

-- 1. Vacuum schedule (weekly for high-update tables)
-- VACUUM ANALYZE players;
-- VACUUM ANALYZE player_stats;
-- VACUUM ANALYZE inventory_items;

-- 2. Reindex schedule (monthly)
-- REINDEX TABLE CONCURRENTLY players;
-- REINDEX TABLE CONCURRENTLY packets;

-- 3. Refresh materialized views
-- REFRESH MATERIALIZED VIEW CONCURRENTLY combat_statistics;
-- REFRESH MATERIALIZED VIEW CONCURRENTLY packet_statistics;

-- 4. Partition management (monthly)
-- SELECT create_next_packets_partition();
-- SELECT drop_old_packets_partitions();

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
