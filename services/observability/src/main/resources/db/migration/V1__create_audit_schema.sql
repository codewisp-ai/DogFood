CREATE SCHEMA IF NOT EXISTS audit;
CREATE TABLE audit.audit_records (
    id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    entity_id UUID NOT NULL,
    payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE audit.service_health (
    id UUID PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    last_checked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
