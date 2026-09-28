CREATE TABLE voting.votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    voter_id UUID,
    voter_email VARCHAR(255),
    voter_ip INET,
    device_fingerprint VARCHAR(255),
    credits_spent INTEGER DEFAULT 1,
    vote_influence DECIMAL(10,6) DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE UNIQUE INDEX idx_votes_voter_submission ON voting.votes(event_id, submission_id, voter_id) WHERE voter_id IS NOT NULL;
CREATE UNIQUE INDEX idx_votes_email_submission ON voting.votes(event_id, submission_id, voter_email) WHERE voter_email IS NOT NULL;

CREATE TABLE voting.quadratic_budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    voter_id UUID NOT NULL,
    total_budget INTEGER NOT NULL DEFAULT 100,
    spent INTEGER DEFAULT 0,
    UNIQUE(event_id, voter_id)
);

CREATE TABLE voting.comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    user_id UUID NOT NULL,
    content TEXT NOT NULL CHECK (length(content) <= 2000),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE voting.vote_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    total_votes INTEGER DEFAULT 0,
    total_influence DECIMAL(10,6) DEFAULT 0,
    rank INTEGER,
    computed_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(event_id, submission_id)
);

CREATE INDEX idx_votes_event ON voting.votes(event_id);
CREATE INDEX idx_comments_submission ON voting.comments(submission_id);
CREATE INDEX idx_vote_results_event ON voting.vote_results(event_id);
