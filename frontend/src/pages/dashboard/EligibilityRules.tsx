import { Button, Paper, Title, Stack, Select, NumberInput, Group, Box, Skeleton, Alert } from '@mantine/core';
import { useForm } from '@mantine/form';
import { useEffect, useState } from 'react';
import { fetchWithAuth } from '../../api';
import { EVENT_ID } from '../../constants';

export function EligibilityRules() {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const form = useForm({
    initialValues: {
      rules: [{ type: 'MAX_TEAM_SIZE', value: 4 }]
    },
  });

  useEffect(() => {
    const loadRules = async () => {
      try {
        const event = await fetchWithAuth(`/api/events/sample-hack-2026`);
        if (event && event.eligibilityRules && event.eligibilityRules.length > 0) {
          form.setValues({ rules: event.eligibilityRules });
        }
      } catch (err) {
        console.error('Failed to load eligibility rules', err);
        setError('Failed to load current rules');
      } finally {
        setLoading(false);
      }
    };
    loadRules();
  }, []);

  const addRule = () => form.insertListItem('rules', { type: 'MIN_TEAM_SIZE', value: 1 });

  const handleSubmit = async (values: typeof form.values) => {
    setSaving(true);
    setError(null);
    try {
      await fetchWithAuth(`/api/events/${EVENT_ID}`, {
        method: 'PUT',
        body: JSON.stringify({
          eligibilityRules: values.rules
        })
      });
      alert('Rules saved successfully');
    } catch (err: any) {
      console.error('Failed to save rules', err);
      setError(err.message || 'Failed to save rules');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <Box p={20}><Skeleton height={200} /></Box>;
  }

  return (
    <Paper withBorder p="md" radius="md">
      <Title order={3} mb="md">Eligibility Rules</Title>
      
      {error && <Alert color="red" mb="md">{error}</Alert>}

      <form onSubmit={form.onSubmit(handleSubmit)}>
        <Stack>
          {form.values.rules.map((rule, index) => (
            <Group key={index} align="flex-end">
              <Select
                label="Rule Type"
                data={['MAX_TEAM_SIZE', 'MIN_TEAM_SIZE', 'ONE_SUBMISSION_PER_TEAM']}
                {...form.getInputProps(`rules.${index}.type`)}
              />
              {rule.type.includes('TEAM_SIZE') && (
                <NumberInput
                  label="Value"
                  {...form.getInputProps(`rules.${index}.value`)}
                />
              )}
              <Button color="red" variant="light" onClick={() => form.removeListItem('rules', index)}>Remove</Button>
            </Group>
          ))}
          <Group mt="md">
            <Button variant="outline" onClick={addRule}>Add Rule</Button>
            <Button type="submit" loading={saving}>Save Rules</Button>
          </Group>
        </Stack>
      </form>
    </Paper>
  );
}
