import { TextInput, Button, Paper, Title, Stack } from '@mantine/core';
import { useForm } from '@mantine/form';
import { fetchWithAuth } from '../../api';

export function CreateEvent() {
  const form = useForm({
    initialValues: {
      name: '',
      description: '',
      submissionDeadline: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
    },
  });

  const handleSubmit = async (values: typeof form.values) => {
    try {
      await fetchWithAuth('/api/events', {
        method: 'POST',
        body: JSON.stringify({
          name: values.name,
          description: values.description,
          submissionDeadline: values.submissionDeadline,
          eligibilityRules: [],
          customQuestions: []
        }),
      });
      alert('Event created successfully');
    } catch (err) {
      console.error('Failed to create event', err);
      alert('Failed to create event');
    }
  };

  return (
    <Paper withBorder p="md" radius="md">
      <Title order={3} mb="md">Create New Event</Title>
      <form onSubmit={form.onSubmit(handleSubmit)}>
        <Stack>
          <TextInput label="Event Name" required {...form.getInputProps('name')} />
          <TextInput label="Description" required {...form.getInputProps('description')} />
          <TextInput label="Submission Deadline (ISO Date)" required {...form.getInputProps('submissionDeadline')} />
          
          <Button type="submit" mt="md">Save Event</Button>
        </Stack>
      </form>
    </Paper>
  );
}
