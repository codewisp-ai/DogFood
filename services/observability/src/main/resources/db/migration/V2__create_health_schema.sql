CREATE TABLE IF NOT EXISTS service_health (
    id UUID PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    last_checked TIMESTAMP NOT NULL,
    details TEXT
);
