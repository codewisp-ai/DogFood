import { Paper, Title, Text, Checkbox, Button, Stack, Alert } from '@mantine/core';
import { useForm } from '@mantine/form';

export function CoiDeclaration() {
  const form = useForm({
    initialValues: {
      teamIds: [] as string[]
    }
  });

  // Mock list of teams the judge has been assigned to review
  const assignedTeams = [
    { id: '101', name: 'Team Alpha (AutoGrader Pro)' },
    { id: '102', name: 'Team Beta (EcoTrack)' }
  ];

  const handleSubmit = (values: typeof form.values) => {
    console.log('Declared COIs for teams:', values.teamIds);
    // Wire to JudgingService POST /coi
  };

  return (
    <Paper withBorder shadow="sm" p="md" radius="md">
      <Title order={3} mb="sm">Conflict of Interest Declaration</Title>
      <Alert color="red" title="Strict Policy" mb="md">
        You must declare any conflicts (e.g. mentoring, financial interest, personal relationship) with these teams before you can begin scoring.
      </Alert>
      
      <form onSubmit={form.onSubmit(handleSubmit)}>
        <Stack>
          <Text fw={500}>Select any teams you have a conflict with:</Text>
          {assignedTeams.map(team => (
            <Checkbox
              key={team.id}
              label={team.name}
              value={team.id}
              {...form.getInputProps('teamIds')}
            />
          ))}
          <Button type="submit" color="red" w={200} mt="md">
            Submit Declarations
          </Button>
        </Stack>
      </form>
    </Paper>
  );
}
