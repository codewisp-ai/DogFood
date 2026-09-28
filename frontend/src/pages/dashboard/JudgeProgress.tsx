import { Paper, Title, Text, Progress, Stack, Group } from '@mantine/core';
import { useEffect, useState } from 'react';
import { API_BASE_URL } from '../../api';

export function JudgeProgress() {
  const [progress, setProgress] = useState({ completed: 0, total: 0, judges: [] });

  useEffect(() => {
    const eventId = '1';
    const eventSource = new EventSource(`${API_BASE_URL}/api/events/${eventId}/judge-progress`);
    eventSource.onmessage = (e) => setProgress(JSON.parse(e.data));
    return () => eventSource.close();
  }, []);

  const percentage = progress.total > 0 ? (progress.completed / progress.total) * 100 : 0;

  return (
    <Paper withBorder shadow="sm" p="md" radius="md">
      <Title order={3} mb="md">Live Judging Progress (SSE stream)</Title>
      <Stack>
        <Text fw={500}>Overall Completion</Text>
        <Progress value={percentage} size="xl" radius="xl" striped animated />
        <Text ta="center" size="sm" c="dimmed">{progress.completed} of {progress.total} reviews completed</Text>
      </Stack>
    </Paper>
  );
}
