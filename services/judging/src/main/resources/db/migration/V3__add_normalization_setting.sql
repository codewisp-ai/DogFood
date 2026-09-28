ALTER TABLE judging.rubrics
ADD COLUMN IF NOT EXISTS normalization_enabled BOOLEAN NOT NULL DEFAULT true;
