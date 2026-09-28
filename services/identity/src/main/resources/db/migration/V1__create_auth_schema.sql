CREATE TABLE IF NOT EXISTS auth.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(512),
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS auth.refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS auth.user_event_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    event_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('PARTICIPANT', 'JUDGE', 'ORGANIZER', 'ADMIN')),
    assigned_track_ids UUID[] DEFAULT '{}',
    assigned_batch_id UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(user_id, event_id, role)
);

CREATE INDEX idx_users_email ON auth.users(email);
CREATE INDEX idx_refresh_tokens_user ON auth.refresh_tokens(user_id);
CREATE INDEX idx_user_event_roles_user ON auth.user_event_roles(user_id);
CREATE INDEX idx_user_event_roles_event ON auth.user_event_roles(event_id);
