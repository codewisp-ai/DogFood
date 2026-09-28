
ALTER TABLE judging.scores ENABLE ROW LEVEL SECURITY;
ALTER TABLE judging.scores FORCE ROW LEVEL SECURITY;

CREATE POLICY judge_scores_select ON judging.scores
    FOR SELECT USING (
        judge_id = current_setting('app.current_judge_id', true)::uuid
        OR current_setting('app.current_role', true) IN ('ORGANIZER', 'ADMIN')
    );

CREATE POLICY judge_scores_insert ON judging.scores
    FOR INSERT WITH CHECK (
        judge_id = current_setting('app.current_judge_id', true)::uuid
    );

CREATE POLICY judge_scores_update ON judging.scores
    FOR UPDATE USING (
        judge_id = current_setting('app.current_judge_id', true)::uuid
    );

ALTER TABLE judging.normalized_scores ENABLE ROW LEVEL SECURITY;
ALTER TABLE judging.normalized_scores FORCE ROW LEVEL SECURITY;

CREATE POLICY judge_normalized_select ON judging.normalized_scores
    FOR SELECT USING (
        judge_id = current_setting('app.current_judge_id', true)::uuid
        OR current_setting('app.current_role', true) IN ('ORGANIZER', 'ADMIN')
    );
