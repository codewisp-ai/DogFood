
CREATE SCHEMA IF NOT EXISTS judging;

CREATE TABLE judging.rubrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE judging.criteria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rubric_id UUID REFERENCES judging.rubrics(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    weight DECIMAL(5,4) NOT NULL,
    max_score INTEGER DEFAULT 10,
    sort_order INTEGER DEFAULT 0
);

CREATE TABLE judging.judge_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    judge_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    batch_id UUID,
    track_id UUID,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED','RECUSED')),
    assigned_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(event_id, judge_id, submission_id)
);

CREATE TABLE judging.scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    judge_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    criterion_id UUID REFERENCES judging.criteria(id),
    raw_score INTEGER NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(judge_id, submission_id, criterion_id)
);

CREATE TABLE judging.normalized_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    judge_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    criterion_id UUID REFERENCES judging.criteria(id),
    z_score DECIMAL(10,6),
    shrinkage_adjusted_z DECIMAL(10,6),
    judge_review_count INTEGER,
    computed_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(judge_id, submission_id, criterion_id)
);

CREATE TABLE judging.final_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    submission_id UUID NOT NULL UNIQUE,
    weighted_score DECIMAL(10,6),
    display_score DECIMAL(10,4),
    rank INTEGER,
    judge_count INTEGER,
    computed_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE judging.calibration_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    submission_id UUID NOT NULL
);

CREATE TABLE judging.calibration_reference_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calibration_submission_id UUID REFERENCES judging.calibration_submissions(id),
    criterion_id UUID REFERENCES judging.criteria(id),
    reference_score INTEGER NOT NULL
);

CREATE TABLE judging.calibration_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    judge_id UUID NOT NULL,
    calibration_submission_id UUID REFERENCES judging.calibration_submissions(id),
    criterion_id UUID REFERENCES judging.criteria(id),
    judge_score INTEGER NOT NULL,
    reference_score INTEGER NOT NULL,
    deviation INTEGER NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE judging.conflict_of_interest (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    judge_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    reason TEXT,
    declared_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(judge_id, submission_id)
);

CREATE TABLE judging.submission_flags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    judge_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','REVIEWED','DISMISSED')),
    flagged_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_scores_judge ON judging.scores(judge_id);
CREATE INDEX idx_scores_submission ON judging.scores(submission_id);
CREATE INDEX idx_scores_event ON judging.scores(event_id);
CREATE INDEX idx_assignments_judge ON judging.judge_assignments(judge_id);
CREATE INDEX idx_assignments_event ON judging.judge_assignments(event_id);
CREATE INDEX idx_final_scores_event ON judging.final_scores(event_id);
CREATE INDEX idx_coi_judge ON judging.conflict_of_interest(judge_id);
