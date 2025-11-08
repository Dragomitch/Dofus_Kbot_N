-- ============================================================================
-- Flyway Migration: V1 - Initial Schema
-- ============================================================================
-- Description: Creates core tables for Dofus Retro Packet Decoder
-- Author: Agent A2 - Database Architect
-- Date: 2025-11-08
-- ============================================================================

-- ============================================================================
-- EXTENSIONS
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ============================================================================
-- TABLE: players
-- ============================================================================

CREATE TABLE players (
    id VARCHAR(36) PRIMARY KEY,
    character_name VARCHAR(100) NOT NULL UNIQUE,
    level INTEGER NOT NULL DEFAULT 1 CHECK (level >= 1 AND level <= 200),
    experience BIGINT NOT NULL DEFAULT 0 CHECK (experience >= 0),
    breed VARCHAR(50),
    map_id INTEGER,
    cell_id INTEGER CHECK (cell_id >= 0 AND cell_id <= 559),
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE players IS 'Core player/character information including position and basic stats';
COMMENT ON COLUMN players.breed IS 'Character class: Iop, Sadida, Eniripsa, Enutrof, Sram, Xelor, Ecaflip, Feca, Sacrieur, Cra, Osamodas, Pandawa';

-- ============================================================================
-- TABLE: packets (partitioned)
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
    PRIMARY KEY (id, timestamp)
) PARTITION BY RANGE (timestamp);

COMMENT ON TABLE packets IS 'Partitioned table storing all network packets (raw and parsed)';
COMMENT ON COLUMN packets.parsed_data IS 'JSON representation of decoded packet fields';

-- ============================================================================
-- TABLE: maps
-- ============================================================================

CREATE TABLE maps (
    map_id INTEGER PRIMARY KEY,
    map_name VARCHAR(200),
    width INTEGER NOT NULL DEFAULT 14,
    height INTEGER NOT NULL DEFAULT 20,
    cell_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE maps IS 'Game map definitions including dimensions and cell walkability data';

-- ============================================================================
-- TABLE: combat_sessions
-- ============================================================================

CREATE TABLE combat_sessions (
    id BIGSERIAL PRIMARY KEY,
    player_id VARCHAR(36) REFERENCES players(id) ON DELETE CASCADE,
    map_id INTEGER REFERENCES maps(map_id) ON DELETE SET NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    result VARCHAR(20) CHECK (result IN ('WIN', 'LOSS', 'FLEE', 'ONGOING', 'DRAW')),
    duration_seconds INTEGER CHECK (duration_seconds >= 0),
    experience_gained BIGINT DEFAULT 0,
    kamas_gained BIGINT DEFAULT 0,
    items_looted INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE combat_sessions IS 'Historical record of all combat engagements';

-- ============================================================================
-- TABLE: combat_actions
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

COMMENT ON TABLE combat_actions IS 'Detailed log of every action taken during combat';

-- ============================================================================
-- TABLE: inventory_items
-- ============================================================================

CREATE TABLE inventory_items (
    id BIGSERIAL PRIMARY KEY,
    player_id VARCHAR(36) REFERENCES players(id) ON DELETE CASCADE,
    item_id INTEGER NOT NULL,
    item_name VARCHAR(200),
    quantity INTEGER DEFAULT 1 CHECK (quantity >= 0),
    position INTEGER,
    equipped BOOLEAN DEFAULT FALSE,
    item_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(player_id, item_id, position)
);

COMMENT ON TABLE inventory_items IS 'Current inventory state for all players';

-- ============================================================================
-- TABLE: player_stats
-- ============================================================================

CREATE TABLE player_stats (
    id BIGSERIAL PRIMARY KEY,
    player_id VARCHAR(36) UNIQUE REFERENCES players(id) ON DELETE CASCADE,
    hp INTEGER DEFAULT 0 CHECK (hp >= 0),
    max_hp INTEGER DEFAULT 0 CHECK (max_hp >= 0),
    mp INTEGER DEFAULT 0 CHECK (mp >= 0),
    max_mp INTEGER DEFAULT 0 CHECK (max_mp >= 0),
    ap INTEGER DEFAULT 0 CHECK (ap >= 0),
    max_ap INTEGER DEFAULT 0 CHECK (max_ap >= 0),
    strength INTEGER DEFAULT 0 CHECK (strength >= 0),
    intelligence INTEGER DEFAULT 0 CHECK (intelligence >= 0),
    agility INTEGER DEFAULT 0 CHECK (agility >= 0),
    vitality INTEGER DEFAULT 0 CHECK (vitality >= 0),
    wisdom INTEGER DEFAULT 0 CHECK (wisdom >= 0),
    chance INTEGER DEFAULT 0 CHECK (chance >= 0),
    neutral_resistance INTEGER DEFAULT 0,
    earth_resistance INTEGER DEFAULT 0,
    fire_resistance INTEGER DEFAULT 0,
    water_resistance INTEGER DEFAULT 0,
    air_resistance INTEGER DEFAULT 0,
    kamas BIGINT DEFAULT 0 CHECK (kamas >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE player_stats IS 'Denormalized player statistics for fast access';

-- ============================================================================
-- TABLE: spells (reference data)
-- ============================================================================

CREATE TABLE spells (
    spell_id INTEGER PRIMARY KEY,
    spell_name VARCHAR(100) NOT NULL,
    spell_description TEXT,
    min_range INTEGER DEFAULT 0,
    max_range INTEGER DEFAULT 0,
    ap_cost INTEGER DEFAULT 0,
    cooldown INTEGER DEFAULT 0,
    element VARCHAR(20),
    spell_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE spells IS 'Reference data for all spells in the game';

-- ============================================================================
-- TABLE: sessions
-- ============================================================================

CREATE TABLE sessions (
    session_id VARCHAR(36) PRIMARY KEY,
    player_id VARCHAR(36) REFERENCES players(id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    ip_address VARCHAR(45),
    client_version VARCHAR(20),
    server_name VARCHAR(100),
    total_packets INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sessions IS 'Tracking of game sessions for grouping packets and analytics';

-- ============================================================================
-- TRIGGERS
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
-- MIGRATION COMPLETE
-- ============================================================================
