-- ============================================================================
-- Flyway Migration: V2 - Add Indexes
-- ============================================================================
-- Description: Creates all indexes for query performance optimization
-- Author: Agent A2 - Database Architect
-- Date: 2025-11-08
-- ============================================================================

-- ============================================================================
-- INDEXES: players
-- ============================================================================

CREATE INDEX idx_players_character_name ON players(character_name);
CREATE INDEX idx_players_map_id ON players(map_id);
CREATE INDEX idx_players_level ON players(level);
CREATE INDEX idx_players_last_seen ON players(last_seen);
CREATE INDEX idx_players_breed ON players(breed);

-- ============================================================================
-- INDEXES: packets (applied to parent, inherited by partitions)
-- ============================================================================

CREATE INDEX idx_packets_type ON packets(packet_type);
CREATE INDEX idx_packets_timestamp ON packets(timestamp DESC);
CREATE INDEX idx_packets_session ON packets(session_id);
CREATE INDEX idx_packets_player ON packets(player_id);
CREATE INDEX idx_packets_direction ON packets(direction);
CREATE INDEX idx_packets_type_timestamp ON packets(packet_type, timestamp DESC);
CREATE INDEX idx_packets_parsed_data ON packets USING GIN(parsed_data);

-- ============================================================================
-- INDEXES: maps
-- ============================================================================

CREATE INDEX idx_maps_name ON maps(map_name);

-- ============================================================================
-- INDEXES: combat_sessions
-- ============================================================================

CREATE INDEX idx_combat_player ON combat_sessions(player_id);
CREATE INDEX idx_combat_map ON combat_sessions(map_id);
CREATE INDEX idx_combat_start_time ON combat_sessions(start_time DESC);
CREATE INDEX idx_combat_end_time ON combat_sessions(end_time DESC);
CREATE INDEX idx_combat_result ON combat_sessions(result);
CREATE INDEX idx_combat_player_result ON combat_sessions(player_id, result);

-- Partial index for ongoing combats (frequently queried)
CREATE INDEX idx_combat_ongoing ON combat_sessions(player_id, start_time)
    WHERE end_time IS NULL;

-- ============================================================================
-- INDEXES: combat_actions
-- ============================================================================

CREATE INDEX idx_combat_actions_session ON combat_actions(combat_session_id);
CREATE INDEX idx_combat_actions_turn ON combat_actions(combat_session_id, turn_number);
CREATE INDEX idx_combat_actions_spell ON combat_actions(spell_id);
CREATE INDEX idx_combat_actions_type ON combat_actions(action_type);
CREATE INDEX idx_combat_actions_timestamp ON combat_actions(timestamp DESC);

-- ============================================================================
-- INDEXES: inventory_items
-- ============================================================================

CREATE INDEX idx_inventory_player ON inventory_items(player_id);
CREATE INDEX idx_inventory_item ON inventory_items(item_id);
CREATE INDEX idx_inventory_position ON inventory_items(player_id, position);

-- Partial index for equipped items
CREATE INDEX idx_inventory_equipped ON inventory_items(player_id)
    WHERE equipped = TRUE;

-- ============================================================================
-- INDEXES: player_stats
-- ============================================================================

CREATE INDEX idx_player_stats_updated ON player_stats(updated_at DESC);

-- ============================================================================
-- INDEXES: spells
-- ============================================================================

CREATE INDEX idx_spells_name ON spells(spell_name);
CREATE INDEX idx_spells_element ON spells(element);

-- ============================================================================
-- INDEXES: sessions
-- ============================================================================

CREATE INDEX idx_sessions_player ON sessions(player_id);
CREATE INDEX idx_sessions_start_time ON sessions(start_time DESC);

-- Partial index for active sessions
CREATE INDEX idx_sessions_active ON sessions(player_id)
    WHERE end_time IS NULL;

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================
