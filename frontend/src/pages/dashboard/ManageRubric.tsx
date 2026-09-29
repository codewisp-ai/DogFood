import { Button, Paper, Title, Stack, TextInput, NumberInput, Group, Switch, Divider } from '@mantine/core';
import { useForm } from '@mantine/form';
import { fetchWithAuth } from '../../api';
import { EVENT_ID } from '../../constants';

export function ManageRubric() {
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

  const addCriterion = () => form.insertListItem('criteria', { name: '', weight: 10 });

  const totalWeight = form.values.criteria.reduce((sum, c) => sum + (c.weight || 0), 0);

  const saveSettings = async (values: typeof form.values) => {
    try {
      await fetchWithAuth(`/api/events/${EVENT_ID}/rubric`, {
        method: 'POST',
        body: JSON.stringify(values.criteria)
      });
      await fetchWithAuth(`/api/events/${EVENT_ID}/settings/normalization?enabled=${values.normalizationEnabled}`, {
        method: 'PUT'
      });
      alert('Settings saved successfully');
    } catch (err) {
      console.error('Failed to save settings', err);
      alert('Failed to save settings');
    }
  };

  return (
    <Paper withBorder p="md" radius="md">
      <Title order={3} mb="md">Judging Configuration</Title>
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
