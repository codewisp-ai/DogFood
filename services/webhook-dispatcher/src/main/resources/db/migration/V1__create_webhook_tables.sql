CREATE SCHEMA IF NOT EXISTS webhooks;
CREATE TABLE webhooks.webhook_endpoints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    url VARCHAR(512) NOT NULL,
    secret VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE TABLE webhooks.webhook_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint_id UUID REFERENCES webhooks.webhook_endpoints(id),
    payload JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    attempts INTEGER DEFAULT 0,
    last_attempt_at TIMESTAMPTZ,
    response_code INTEGER,
    error_message TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_webhook_event ON webhooks.webhook_endpoints(event_id);
CREATE INDEX idx_delivery_status ON webhooks.webhook_deliveries(status);
