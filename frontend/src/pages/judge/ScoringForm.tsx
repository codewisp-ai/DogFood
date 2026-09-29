import { Paper, Title, Text, Slider, Button, Stack, Group, Textarea } from '@mantine/core';
import { useForm } from '@mantine/form';
import { useState, useEffect } from 'react';
import { fetchWithAuth } from '../../api';

interface ScoringFormProps {
  isCalibration?: boolean;
}

export function ScoringForm({ isCalibration = false }: ScoringFormProps) {
  const [rubric, setRubric] = useState<any[]>([]);
  const eventId = '1'; // Placeholder for now
  const submissionId = '1'; // Placeholder for now

  useEffect(() => {
    const loadRubric = async () => {
      try {
        const data = await fetchWithAuth(`/api/events/${eventId}/rubric`);
        if (Array.isArray(data)) {
          setRubric(data);
          form.setValues({
            scores: data.reduce((acc, c) => ({ ...acc, [c.id]: 5 }), {}),
            feedback: form.values.feedback || ''
          });
        }
      } catch (err) {
        console.error('Failed to load rubric', err);
      }
    };
    loadRubric();
  }, []);

  const form = useForm<{ scores: Record<string, number>; feedback: string }>({
    initialValues: {
      scores: {},
      feedback: ''
    }
  });

  const handleSubmit = async (values: typeof form.values) => {
    try {
      await fetchWithAuth('/api/judging/scores', {
        method: 'POST',
        body: JSON.stringify({
          submissionId,
          isCalibration,
          scores: values.scores,
          feedback: values.feedback
        })
      });
      alert('Score submitted!');
    } catch (err) {
      console.error('Failed to submit score', err);
      alert('Failed to submit score');
    }
  };

  return (
    <Paper withBorder p="md" radius="md">
      <Title order={3} mb="xs">
        {isCalibration ? 'Calibration: Control Submission' : 'Score: AutoGrader Pro'}
      </Title>
      
      {isCalibration && (
        <Text c="dimmed" size="sm" mb="md">
          This is a control submission. Your scores here will be used to calculate your baseline standard deviation ($) for the Bayesian Shrinkage algorithm before you review real participants.
        </Text>
      )}

      <form onSubmit={form.onSubmit(handleSubmit)}>
        <Stack gap="xl" mt="xl">
          {rubric.map(criterion => (
            <div key={criterion.id}>
              <Group justify="space-between" mb="xs">
                <Text fw={500}>{criterion.name}</Text>
                <Text size="sm" c="dimmed">Weight: {criterion.weight}%</Text>
              </Group>
              <Slider
                min={1}
                max={10}
                step={1}
                marks={[
                  { value: 1, label: '1' },
                  { value: 5, label: '5' },
                  { value: 10, label: '10' }
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

          <Button type="submit" mt="md" size="md">
            {isCalibration ? 'Submit Calibration' : 'Submit Final Score'}
          </Button>
        </Stack>
      </form>
    </Paper>
  );
}
