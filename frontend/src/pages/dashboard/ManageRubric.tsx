import { Button, Paper, Title, Stack, TextInput, NumberInput, Group } from '@mantine/core';
import { useForm } from '@mantine/form';
import { fetchWithAuth } from '../../api';

export function ManageRubric() {
  const form = useForm({
    initialValues: {
      criteria: [
        { name: 'Technical Complexity', weight: 40 },
        { name: 'Originality', weight: 30 },
        { name: 'Design', weight: 30 },
      ]
    },
  });

  const addCriterion = () => form.insertListItem('criteria', { name: '', weight: 10 });

  const totalWeight = form.values.criteria.reduce((sum, c) => sum + (c.weight || 0), 0);

  return (
    <Paper withBorder shadow="sm" p="md" radius="md">
      <Title order={3} mb="md">Judging Rubric (Total Weight: {totalWeight}%)</Title>
      <form onSubmit={form.onSubmit(async (values) => {
        try {
          const eventId = '1'; // Placeholder for now
          await fetchWithAuth(`/api/events/${eventId}/rubric`, {
            method: 'POST',
            body: JSON.stringify(values.criteria)
          });
          alert('Rubric saved successfully');
        } catch (err) {
          console.error('Failed to save rubric', err);
          alert('Failed to save rubric');
        }
      })}>
        <Stack>
          {form.values.criteria.map((item, index) => (
            <Group key={index} align="flex-end">
              <TextInput label="Criterion Name" required {...form.getInputProps(`criteria.${index}.name`)} />
              <NumberInput label="Weight (%)" required {...form.getInputProps(`criteria.${index}.weight`)} />
              <Button color="red" variant="light" onClick={() => form.removeListItem('criteria', index)}>Remove</Button>
            </Group>
          ))}
          <Group mt="md">
            <Button variant="outline" onClick={addCriterion}>Add Criterion</Button>
            <Button type="submit" disabled={totalWeight !== 100}>Save Rubric</Button>
          </Group>
        </Stack>
      </form>
    </Paper>
  );
}
