import { TextInput, Button, Paper, Title, Stack, Checkbox, Select, NumberInput } from '@mantine/core';
import { useForm } from '@mantine/form';
import { fetchWithAuth } from '../../api';

export function CreateEvent() {
  const form = useForm({
    initialValues: {
      name: '',
      description: '',
      judgingMode: 'Z_SCORE',
      votingMode: 'QUADRATIC',
      quadraticVoteBudget: 100,
      calibrationRequired: true,
      webhooksEnabled: false,
    },
  });

  const handleSubmit = async (values: typeof form.values) => {
    try {
      await fetchWithAuth('/api/events', {
        method: 'POST',
        body: JSON.stringify(values),
      });
      alert('Event created successfully');
    } catch (err) {
      console.error('Failed to create event', err);
      alert('Failed to create event');
    }
  };

  return (
    <Paper withBorder shadow="sm" p="md" radius="md">
      <Title order={3} mb="md">Create New Event</Title>
      <form onSubmit={form.onSubmit(handleSubmit)}>
        <Stack>
          <TextInput label="Event Name" required {...form.getInputProps('name')} />
          <TextInput label="Description" required {...form.getInputProps('description')} />
          
          <Select 
            label="Judging Mode" 
            data={['Z_SCORE', 'PAIRWISE']} 
            {...form.getInputProps('judgingMode')} 
          />
          
          <Select 
            label="Public Voting Mode" 
            data={['QUADRATIC', 'ONE_PERSON_ONE_VOTE']} 
            {...form.getInputProps('votingMode')} 
          />
          
          {form.values.votingMode === 'QUADRATIC' && (
            <NumberInput label="Quadratic Budget" {...form.getInputProps('quadraticVoteBudget')} />
          )}

          <Checkbox label="Require Judge Calibration Round" {...form.getInputProps('calibrationRequired', { type: 'checkbox' })} />
          <Checkbox label="Enable Webhooks" {...form.getInputProps('webhooksEnabled', { type: 'checkbox' })} />
          
          <Button type="submit" mt="md">Save Event</Button>
        </Stack>
      </form>
    </Paper>
  );
}
