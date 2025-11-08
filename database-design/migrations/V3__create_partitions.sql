-- ============================================================================
-- Flyway Migration: V3 - Create Partitions
-- ============================================================================
-- Description: Creates initial partitions for packets table
-- Author: Agent A2 - Database Architect
-- Date: 2025-11-08
-- ============================================================================

-- ============================================================================
-- PACKETS PARTITIONS
-- ============================================================================

-- Create partitions for current month + next 3 months
CREATE TABLE packets_2025_11 PARTITION OF packets
    FOR VALUES FROM ('2025-11-01 00:00:00') TO ('2025-12-01 00:00:00');

CREATE TABLE packets_2025_12 PARTITION OF packets
    FOR VALUES FROM ('2025-12-01 00:00:00') TO ('2026-01-01 00:00:00');

CREATE TABLE packets_2026_01 PARTITION OF packets
    FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-02-01 00:00:00');

CREATE TABLE packets_2026_02 PARTITION OF packets
    FOR VALUES FROM ('2026-02-01 00:00:00') TO ('2026-03-01 00:00:00');

-- ============================================================================
-- PARTITION MANAGEMENT FUNCTIONS
-- ============================================================================

-- Function to automatically create next month's partition
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

COMMENT ON FUNCTION create_next_packets_partition() IS 'Creates next month partition for packets table';
COMMENT ON FUNCTION drop_old_packets_partitions() IS 'Drops packet partitions older than 90 days';

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================
