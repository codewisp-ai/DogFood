import { Box, Text, Slider, Button, Stack, Group, Textarea, Skeleton, Alert } from '@mantine/core';
import { useForm } from '@mantine/form';
import { useState, useEffect } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { fetchWithAuth } from '../../api';
import { PageHeader } from '../../components/shared/PageHeader';
import { Container } from '../../components/shared/Container';

interface Criterion {
  id: string;
  name: string;
  weight: number;
  maxScore: number;
}

// The backend needs one POST per criterion, so we loop and send each one
export function ScoringForm() {
  const { submissionId } = useParams<{ submissionId: string }>();
  const location = useLocation();
  const navigate = useNavigate();

  // eventId and assignmentId are passed via navigate state from JudgeDashboard
  const eventId: string | undefined = location.state?.eventId;

  const [rubric, setRubric] = useState<Criterion[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const form = useForm<{ scores: Record<string, number>; feedback: string }>({
    initialValues: { scores: {}, feedback: '' }
  });

  useEffect(() => {
    if (!eventId) return;
    const load = async () => {
      setLoading(true);
      try {
        const data = await fetchWithAuth(`/api/events/${eventId}/rubric`);
        const criteria: Criterion[] = Array.isArray(data?.criteria) ? data.criteria : Array.isArray(data) ? data : [];
        setRubric(criteria);
        form.setValues({
          scores: criteria.reduce((acc: Record<string, number>, c: Criterion) => ({ ...acc, [c.id]: Math.ceil((c.maxScore || 10) / 2) }), {}),
          feedback: ''
        });
      } catch (err: any) {
        setError(err.message || 'Failed to load rubric');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [eventId]);

  const handleSubmit = async (values: typeof form.values) => {
    if (!submissionId || !eventId) return;
    setSubmitting(true);
    setError(null);
    try {
      // The backend expects one score per criterion, each with a unique Idempotency-Key
      for (const criterion of rubric) {
        await fetchWithAuth('/api/judging/scores', {
          method: 'POST',
          headers: {
            'Idempotency-Key': `${submissionId}-${criterion.id}-${Date.now()}`
          },
          body: JSON.stringify({
            eventId,
            submissionId,
            criterionId: criterion.id,
            rawScore: values.scores[criterion.id] ?? 5
          })
        });
      }
      navigate('/judge');
    } catch (err: any) {
      setError(err.message || 'Failed to submit scores');
    } finally {
      setSubmitting(false);
    }
  };

  if (!eventId) {
    return (
      <Box p={24}>
        <Alert color="red" title="Missing context">
          Please navigate here from the Judge Dashboard.
        </Alert>
        <Button mt={16} onClick={() => navigate('/judge')}>Back to Dashboard</Button>
      </Box>
    );
  }

  return (
    <Box>
      <PageHeader
        title="Score Submission"
        breadcrumbs={[{ label: 'Home', href: '/' }, { label: 'Judge', href: '/judge' }, { label: 'Score' }]}
        description={`Submission ID: ${submissionId}`}
      />

      <Container title="Scoring Form">
        {loading ? (
          <Box p={24}>
            {[1, 2, 3].map(i => <Skeleton key={i} height={60} mb={20} />)}
          </Box>
        ) : rubric.length === 0 ? (
          <Box p={24}>
            <Alert color="yellow" title="No rubric configured">
              The organizer hasn't set up a rubric for this event yet.
            </Alert>
          </Box>
        ) : (
          <Box p={24}>
            {error && <Alert color="red" title="Error" mb={20}>{error}</Alert>}
            <form onSubmit={form.onSubmit(handleSubmit)}>
              <Stack gap="xl">
                {rubric.map(criterion => (
                  <div key={criterion.id}>
                    <Group justify="space-between" mb={8}>
                      <Text fw={600}>{criterion.name}</Text>
                      <Text size="sm" style={{ color: 'var(--text-muted)' }}>Weight: {criterion.weight}%</Text>
                    </Group>
                    <Slider
                      min={1}
                      max={criterion.maxScore || 10}
                      step={1}
                      marks={[
                        { value: 1, label: '1' },
                        { value: Math.ceil((criterion.maxScore || 10) / 2), label: String(Math.ceil((criterion.maxScore || 10) / 2)) },
                        { value: criterion.maxScore || 10, label: String(criterion.maxScore || 10) }
                      ]}
                      {...form.getInputProps(`scores.${criterion.id}`)}
                    />
                  </div>
                ))}

                <Textarea
                  label="Constructive Feedback"
                  placeholder="What did they do well? What could be improved?"
                  minRows={4}
                  {...form.getInputProps('feedback')}
                />

                <Group>
                  <Button variant="default" onClick={() => navigate('/judge')}>Cancel</Button>
                  <Button type="submit" loading={submitting}>Submit Scores</Button>
                </Group>
              </Stack>
            </form>
          </Box>
        )}
      </Container>
    </Box>
  );
}
