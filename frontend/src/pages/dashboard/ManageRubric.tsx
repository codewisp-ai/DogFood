import { Button, Paper, Title, Stack, TextInput, NumberInput, Group, Switch, Divider, Alert } from '@mantine/core';
import { useForm } from '@mantine/form';
import { useEffect, useState } from 'react';
import { fetchWithAuth } from '../../api';
import { EVENT_ID } from '../../constants';

export function ManageRubric() {
  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const form = useForm({
    initialValues: {
      normalizationEnabled: true,
      criteria: [
        { name: 'Technical Complexity', weight: 40 },
        { name: 'Originality', weight: 30 },
        { name: 'Design', weight: 30 },
      ]
    },
  });

  useEffect(() => {
    fetchWithAuth(`/api/events/${EVENT_ID}/rubric`)
      .then((data: any) => {
        if (data && data.criteria && data.criteria.length > 0) {
          form.setValues({
            normalizationEnabled: data.rubric?.normalizationEnabled ?? true,
            criteria: data.criteria.map((c: any) => ({
              name: c.name,
              weight: Math.round(Number(c.weight) * 100),
            })),
          });
        }
      })
      .catch((err) => console.log('No existing rubric or load error', err));
  }, []);

  const addCriterion = () => form.insertListItem('criteria', { name: '', weight: 10 });

  const totalWeight = form.values.criteria.reduce((sum, c) => sum + (c.weight || 0), 0);

  const saveSettings = async (values: typeof form.values) => {
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const payload = {
        criteria: values.criteria.map((c) => ({
          name: c.name,
          weight: Number((c.weight / 100.0).toFixed(4)),
          description: '',
          maxScore: 10,
        })),
      };

      await fetchWithAuth(`/api/events/${EVENT_ID}/rubric`, {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      await fetchWithAuth(`/api/events/${EVENT_ID}/settings/normalization?enabled=${values.normalizationEnabled}`, {
        method: 'PUT'
      });
      setSuccess('Rubric and normalization configuration saved successfully!');
    } catch (err: any) {
      console.error('Failed to save settings', err);
      setError(err.message || 'Failed to save settings');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Paper withBorder p="md" radius="md">
      <Title order={3} mb="md">Judging Configuration</Title>
      {error && <Alert color="red" mb="md" title="Error">{error}</Alert>}
      {success && <Alert color="green" mb="md" title="Success">{success}</Alert>}
      <form onSubmit={form.onSubmit(saveSettings)}>
        <Stack>
          <Switch 
            label="Enable Z-Score Normalization & Shrinkage" 
            description="Automatically corrects for harsh/lenient judges and aligns scores mathematically."
            {...form.getInputProps('normalizationEnabled', { type: 'checkbox' })}
          />
          
          <Divider my="sm" />
          
          <Title order={4}>Rubric Criteria (Total Weight: {totalWeight}%)</Title>
          {form.values.criteria.map((_, index) => (
            <Group key={index} align="flex-end">
              <TextInput label="Criterion Name" required {...form.getInputProps(`criteria.${index}.name`)} />
              <NumberInput label="Weight (%)" required {...form.getInputProps(`criteria.${index}.weight`)} />
              <Button color="red" variant="light" onClick={() => form.removeListItem('criteria', index)}>Remove</Button>
            </Group>
          ))}
          <Group mt="md">
            <Button variant="outline" onClick={addCriterion}>Add Criterion</Button>
            <Button type="submit" disabled={totalWeight !== 100}>Save Configuration</Button>
          </Group>
        </Stack>
      </form>
    </Paper>
  );
}
