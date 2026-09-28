CREATE SCHEMA IF NOT EXISTS submissions;

CREATE TABLE IF NOT EXISTS submissions.submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    team_id UUID NOT NULL,
    track_id UUID,
    name VARCHAR(255) NOT NULL,
    tagline VARCHAR(300),
    description TEXT,
    thumbnail_url VARCHAR(512),
    image_gallery JSONB DEFAULT '[]',
    demo_video_url VARCHAR(512),
    repository_url VARCHAR(512),
    live_link VARCHAR(512),
    tech_tags TEXT[] DEFAULT '{}',
    custom_answers JSONB DEFAULT '{}',
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SUBMITTED', 'FLAGGED', 'DISQUALIFIED')),
    submitted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    version INTEGER DEFAULT 0,
    search_vector TSVECTOR,
    UNIQUE(event_id, team_id)
);

CREATE INDEX idx_submissions_event ON submissions.submissions(event_id);
CREATE INDEX idx_submissions_track ON submissions.submissions(track_id);
CREATE INDEX idx_submissions_status ON submissions.submissions(status);
CREATE INDEX idx_submissions_search ON submissions.submissions USING GIN(search_vector);
CREATE INDEX idx_submissions_tags ON submissions.submissions USING GIN(tech_tags);

CREATE OR REPLACE FUNCTION submissions.update_search_vector() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := to_tsvector('english',
        coalesce(NEW.name, '') || ' ' ||
        coalesce(NEW.tagline, '') || ' ' ||
        coalesce(NEW.description, '') || ' ' ||
        coalesce(array_to_string(NEW.tech_tags, ' '), '')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_search_vector
    BEFORE INSERT OR UPDATE ON submissions.submissions
    FOR EACH ROW EXECUTE FUNCTION submissions.update_search_vector();
