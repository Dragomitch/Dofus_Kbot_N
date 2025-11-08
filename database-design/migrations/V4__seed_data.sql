-- ============================================================================
-- Flyway Migration: V4 - Seed Data
-- ============================================================================
-- Description: Inserts initial/reference data for testing and development
-- Author: Agent A2 - Database Architect
-- Date: 2025-11-08
-- ============================================================================

-- ============================================================================
-- SYSTEM PLAYER
-- ============================================================================

-- Insert SYSTEM player for server-generated events
INSERT INTO players (id, character_name, level, breed) VALUES
    ('00000000-0000-0000-0000-000000000000', 'SYSTEM', 1, 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

COMMENT ON TABLE players IS 'SYSTEM player (id all zeros) used for server-generated events';

-- ============================================================================
-- SAMPLE MAPS (for testing)
-- ============================================================================

INSERT INTO maps (map_id, map_name, width, height) VALUES
    (7411, 'Incarnam Village', 14, 20),
    (191, 'Amakna Village', 14, 20),
    (4629, 'Bonta City Center', 14, 20),
    (10298, 'Brakmar City Center', 14, 20),
    (4519, 'Astrub Village', 14, 20)
ON CONFLICT (map_id) DO NOTHING;

-- ============================================================================
-- SAMPLE SPELLS (common spells for testing)
-- ============================================================================

-- Iop spells
INSERT INTO spells (spell_id, spell_name, spell_description, min_range, max_range, ap_cost, element, spell_type) VALUES
    (101, 'Pressure', 'Deals neutral damage', 1, 1, 3, 'neutral', 'damage'),
    (102, 'Compulsion', 'Deals earth damage', 1, 6, 4, 'earth', 'damage'),
    (103, 'Jump', 'Increases mobility', 0, 0, 2, 'neutral', 'buff'),
    (104, 'Bravery', 'Increases strength', 0, 0, 3, 'neutral', 'buff')
ON CONFLICT (spell_id) DO NOTHING;

-- Sadida spells
INSERT INTO spells (spell_id, spell_name, spell_description, min_range, max_range, ap_cost, element, spell_type) VALUES
    (201, 'Poisoned Wind', 'Deals air damage over time', 1, 6, 4, 'air', 'damage'),
    (202, 'Bramble', 'Summons a bramble', 1, 4, 3, 'neutral', 'summon'),
    (203, 'Wild Grass', 'Summons wild grass', 1, 5, 3, 'neutral', 'summon'),
    (204, 'Healing Word', 'Heals an ally', 1, 6, 4, 'neutral', 'heal')
ON CONFLICT (spell_id) DO NOTHING;

-- Eniripsa spells
INSERT INTO spells (spell_id, spell_name, spell_description, min_range, max_range, ap_cost, element, spell_type) VALUES
    (301, 'Healing Word', 'Heals an ally', 1, 6, 3, 'neutral', 'heal'),
    (302, 'Preventing Word', 'Increases resistances', 1, 5, 2, 'neutral', 'buff'),
    (303, 'Vampiric Word', 'Steals HP', 1, 6, 4, 'water', 'damage'),
    (304, 'Forbidden Word', 'Deals fire damage', 1, 6, 5, 'fire', 'damage')
ON CONFLICT (spell_id) DO NOTHING;

-- ============================================================================
-- MATERIALIZED VIEWS
-- ============================================================================

-- Combat statistics materialized view
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

CREATE UNIQUE INDEX idx_combat_stats_player ON combat_statistics(player_id);

COMMENT ON MATERIALIZED VIEW combat_statistics IS 'Pre-aggregated combat statistics (refresh hourly)';

-- Packet statistics materialized view
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

CREATE INDEX idx_packet_stats_type ON packet_statistics(packet_type);
CREATE INDEX idx_packet_stats_hour ON packet_statistics(hour_bucket DESC);

COMMENT ON MATERIALIZED VIEW packet_statistics IS 'Packet type distribution for monitoring (refresh every 15 minutes)';

-- ============================================================================
-- SAMPLE TEST DATA (optional - comment out for production)
-- ============================================================================

-- Uncomment for development/testing environments

-- Sample player
-- INSERT INTO players (id, character_name, level, experience, breed, map_id, cell_id) VALUES
--     ('11111111-1111-1111-1111-111111111111', 'TestWarrior', 50, 1000000, 'Iop', 191, 250)
-- ON CONFLICT (id) DO NOTHING;

-- Sample player stats
-- INSERT INTO player_stats (player_id, hp, max_hp, mp, max_mp, ap, max_ap, strength, intelligence, agility, vitality, wisdom, chance, kamas) VALUES
--     ('11111111-1111-1111-1111-111111111111', 500, 500, 3, 3, 6, 6, 100, 50, 30, 50, 40, 20, 50000)
-- ON CONFLICT (player_id) DO NOTHING;

-- Sample inventory items
-- INSERT INTO inventory_items (player_id, item_id, item_name, quantity, position, equipped, item_type) VALUES
--     ('11111111-1111-1111-1111-111111111111', 7001, 'Adventurer Sword', 1, 0, TRUE, 'weapon'),
--     ('11111111-1111-1111-1111-111111111111', 7002, 'Leather Armor', 1, 1, TRUE, 'armor'),
--     ('11111111-1111-1111-1111-111111111111', 7003, 'Health Potion', 10, 10, FALSE, 'consumable'),
--     ('11111111-1111-1111-1111-111111111111', 7004, 'Wheat', 50, 11, FALSE, 'resource')
-- ON CONFLICT (player_id, item_id, position) DO NOTHING;

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================
