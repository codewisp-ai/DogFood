import { Paper, Title, Text, Progress, Stack } from '@mantine/core';
import { useEffect, useState } from 'react';
import { API_BASE_URL } from '../../api';
import { EVENT_ID } from '../../constants';

export function JudgeProgress() {
  const [progress, setProgress] = useState({ completed: 0, total: 0, judges: [] });

  useEffect(() => {
    const eventSource = new EventSource(`${API_BASE_URL}/api/events/${EVENT_ID}/judge-progress`);
    eventSource.onmessage = (e) => setProgress(JSON.parse(e.data));
    return () => eventSource.close();
  }, []);

  const percentage = progress.total > 0 ? (progress.completed / progress.total) * 100 : 0;

  return (
    <Paper withBorder p="md" radius="md">
      <Title order={3} mb="md">Live Judging Progress (SSE stream)</Title>
      <Stack>
        <Text fw={500}>Overall Completion</Text>
        <Progress value={percentage} size="xl" radius="xl" striped animated />
        <Text ta="center" size="sm" c="dimmed">{progress.completed} of {progress.total} reviews completed</Text>
      </Stack>
    </Paper>
  );
}
