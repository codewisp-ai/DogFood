import { Paper, Title, Text, Checkbox, Button, Stack, Alert, Box, Skeleton } from '@mantine/core';
import { useForm } from '@mantine/form';
import { useEffect, useState } from 'react';
import { fetchWithAuth } from '../../api';
import { EVENT_ID } from '../../constants';
import { useNavigate } from 'react-router-dom';

interface AssignedTeam {
  submissionId: string;
  name: string;
}

export function CoiDeclaration() {
  const [assignedTeams, setAssignedTeams] = useState<AssignedTeam[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const form = useForm({
    initialValues: {
      submissionIds: [] as string[]
    }
  });

  useEffect(() => {
    const loadAssignments = async () => {
      try {
        const assignments: any[] = await fetchWithAuth(`/api/judging/my-assignments`);
        
        // Fetch submission details to get names
        const teamDetails = await Promise.all(
          assignments.map(async (a) => {
            try {
              const sub = await fetchWithAuth(`/api/submissions/${a.submissionId}`);
              return { submissionId: a.submissionId, name: sub.name };
            } catch {
              return { submissionId: a.submissionId, name: 'Unknown Project' };
            }
          })
        );
        
        // Filter out duplicates if multiple assignments somehow occur
        const uniqueTeams = Array.from(new Map(teamDetails.map(item => [item.submissionId, item])).values());
        setAssignedTeams(uniqueTeams);
      } catch (err) {
        console.error('Failed to load assignments', err);
        setError('Failed to load assignments');
      } finally {
        setLoading(false);
      }
    };

    loadAssignments();
  }, []);

  const handleSubmit = async (values: typeof form.values) => {
    if (values.submissionIds.length === 0) {
      alert("No conflicts declared. You can proceed to judging.");
      navigate('/judge');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      await Promise.all(
        values.submissionIds.map(subId => 
          fetchWithAuth('/api/judging/coi', {
            method: 'POST',
            body: JSON.stringify({
              eventId: EVENT_ID,
              submissionId: subId,
              reason: 'Declared via UI'
            })
          })
        )
      );
      alert('Conflicts declared successfully.');
      navigate('/judge');
    } catch (err: any) {
      console.error('Failed to declare COI', err);
      setError(err.message || 'Failed to declare conflicts');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <Box p={20}><Skeleton height={200} /></Box>;
  }

  return (
    <Paper withBorder p="md" radius="md">
      <Title order={3} mb="sm">Conflict of Interest Declaration</Title>
      
      {error && <Alert color="red" mb="md">{error}</Alert>}
      
      <Alert color="red" title="Strict Policy" mb="md">
        You must declare any conflicts (e.g. mentoring, financial interest, personal relationship) with these teams before you can begin scoring. Declaring a conflict will recuse you from judging that project.
      </Alert>
      
      <form onSubmit={form.onSubmit(handleSubmit)}>
        <Stack>
          <Text fw={500}>Select any teams you have a conflict with:</Text>
          
          {assignedTeams.length === 0 ? (
            <Text c="dimmed">You have no assigned projects yet.</Text>
          ) : (
            assignedTeams.map(team => (
              <Checkbox
                key={team.submissionId}
                label={team.name}
                value={team.submissionId}
                {...form.getInputProps('submissionIds')}
              />
            ))
          )}

          <Button type="submit" color="red" w={200} mt="md" loading={submitting}>
            Submit Declarations
          </Button>
        </Stack>
      </form>
    </Paper>
  );
}
