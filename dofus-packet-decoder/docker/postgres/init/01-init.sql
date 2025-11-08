-- Initial database setup script
-- This script runs when the PostgreSQL container is first created

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create schemas
CREATE SCHEMA IF NOT EXISTS dofus;

-- Set search path
ALTER DATABASE dofus_decoder SET search_path TO dofus, public;

-- Grant privileges
GRANT ALL PRIVILEGES ON SCHEMA dofus TO dofus;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA dofus TO dofus;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA dofus TO dofus;

-- Create basic tables will be handled by Flyway migrations
-- This is just initial setup

COMMENT ON DATABASE dofus_decoder IS 'Dofus Retro Packet Decoder Database';
