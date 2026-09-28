CREATE SCHEMA IF NOT EXISTS events;

CREATE TABLE IF NOT EXISTS events.events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    banner_url VARCHAR(512),
    organizer_id UUID NOT NULL,
    registration_opens_at TIMESTAMPTZ,
    registration_closes_at TIMESTAMPTZ,
    submission_opens_at TIMESTAMPTZ,
    submission_deadline TIMESTAMPTZ NOT NULL,
    judging_opens_at TIMESTAMPTZ,
    judging_closes_at TIMESTAMPTZ,
    voting_opens_at TIMESTAMPTZ,
    voting_closes_at TIMESTAMPTZ,
    results_published_at TIMESTAMPTZ,
    judging_mode VARCHAR(20) DEFAULT 'RUBRIC' CHECK (judging_mode IN ('RUBRIC', 'PAIRWISE')),
    voting_mode VARCHAR(20) DEFAULT 'SIMPLE' CHECK (voting_mode IN ('SIMPLE', 'QUADRATIC')),
    calibration_required BOOLEAN DEFAULT TRUE,
    webhooks_enabled BOOLEAN DEFAULT FALSE,
    voting_access_mode VARCHAR(20) DEFAULT 'AUTHENTICATED' CHECK (voting_access_mode IN ('OPEN', 'EMAIL_GATED', 'AUTHENTICATED')),
    quadratic_vote_budget INTEGER DEFAULT 100,
    eligibility_rules JSONB DEFAULT '[]',
    custom_questions JSONB DEFAULT '[]',
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'OPEN', 'SUBMISSIONS', 'JUDGING', 'VOTING', 'CLOSED')),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS events.tracks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES events.events(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS events.prizes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES events.events(id) ON DELETE CASCADE,
    track_id UUID REFERENCES events.tracks(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS events.teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES events.events(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(event_id, name)
);

CREATE TABLE IF NOT EXISTS events.team_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID REFERENCES events.teams(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(20) DEFAULT 'MEMBER' CHECK (role IN ('LEADER', 'MEMBER')),
    joined_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(team_id, user_id)
);

CREATE TABLE IF NOT EXISTS events.team_invites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID REFERENCES events.teams(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255),
    expires_at TIMESTAMPTZ NOT NULL,
    accepted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_events_slug ON events.events(slug);
CREATE INDEX idx_events_status ON events.events(status);
CREATE INDEX idx_tracks_event ON events.tracks(event_id);
CREATE INDEX idx_teams_event ON events.teams(event_id);
CREATE INDEX idx_team_members_user ON events.team_members(user_id);
CREATE INDEX idx_team_invites_token ON events.team_invites(token);
