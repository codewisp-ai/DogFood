-- Database initialization script for Dogfood platform
-- Creates schemas, application user with restricted privileges, and enables extensions

-- Create the application user (unprivileged — RLS will apply to this user)
CREATE ROLE dogfood_app WITH LOGIN PASSWORD 'dogfood_secret';

-- Create schemas
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS events;
CREATE SCHEMA IF NOT EXISTS submissions;
CREATE SCHEMA IF NOT EXISTS judging;
CREATE SCHEMA IF NOT EXISTS voting;
CREATE SCHEMA IF NOT EXISTS audit;

-- Grant schema usage and table privileges to app user
GRANT USAGE ON SCHEMA auth TO dogfood_app;
GRANT USAGE ON SCHEMA events TO dogfood_app;
GRANT USAGE ON SCHEMA submissions TO dogfood_app;
GRANT USAGE ON SCHEMA judging TO dogfood_app;
GRANT USAGE ON SCHEMA voting TO dogfood_app;
GRANT USAGE ON SCHEMA audit TO dogfood_app;

-- Grant default privileges so future tables are accessible
ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT ALL ON TABLES TO dogfood_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA events GRANT ALL ON TABLES TO dogfood_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA submissions GRANT ALL ON TABLES TO dogfood_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA judging GRANT ALL ON TABLES TO dogfood_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA voting GRANT ALL ON TABLES TO dogfood_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA audit GRANT ALL ON TABLES TO dogfood_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT ALL ON SEQUENCES TO dogfood_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA events GRANT ALL ON SEQUENCES TO dogfood_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA submissions GRANT ALL ON SEQUENCES TO dogfood_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA judging GRANT ALL ON SEQUENCES TO dogfood_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA voting GRANT ALL ON SEQUENCES TO dogfood_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA audit GRANT ALL ON SEQUENCES TO dogfood_app;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
