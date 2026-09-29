CREATE TABLE IF NOT EXISTS events.export_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    requester_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    s3_url VARCHAR(1024),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);