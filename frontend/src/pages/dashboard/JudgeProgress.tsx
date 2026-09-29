import { Paper, Title, Text, Progress, Stack, Table, Badge, Box } from '@mantine/core';
import { useEffect, useState } from 'react';
import { API_BASE_URL } from '../../api';
import { EVENT_ID } from '../../constants';

interface JudgeStat {
  judgeId: string;
  totalAssigned: number;
  completedReviews: number;
  percentage: number;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';
}

interface ProgressData {
  completed: number;
  total: number;
  percentage?: number;
  judges: JudgeStat[];
}

export function JudgeProgress() {
  const [progress, setProgress] = useState<ProgressData>({ completed: 0, total: 0, judges: [] });

  useEffect(() => {
    const token = localStorage.getItem('token');
    const url = token
      ? `${API_BASE_URL}/api/events/${EVENT_ID}/judge-progress?token=${token}`
      : `${API_BASE_URL}/api/events/${EVENT_ID}/judge-progress`;
    const eventSource = new EventSource(url);
    eventSource.onmessage = (e) => {
      try {
        setProgress(JSON.parse(e.data));
      } catch (err) {
        console.error('Failed to parse SSE progress data', err);
      }
    };
    return () => eventSource.close();
  }, []);

  const percentage = progress.total > 0 ? (progress.completed / progress.total) * 100 : 0;

  return (
    <Paper withBorder p="md" radius="md">
      <Title order={3} mb="md">Live Judging Progress (SSE stream)</Title>
      <Stack gap="lg">
        <Box>
          <Text fw={500} mb={8}>Overall Completion</Text>
          <Progress value={percentage} size="xl" radius="xl" striped animated />
          <Text ta="center" size="sm" c="dimmed" mt={4}>
            {progress.completed} of {progress.total} reviews completed ({Math.round(percentage)}%)
          </Text>
        </Box>

        {progress.judges && progress.judges.length > 0 && (
          <Box>
            <Title order={4} mb="sm">Judge Breakdown</Title>
            <Table striped highlightOnHover withTableBorder>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th>Judge ID</Table.Th>
                  <Table.Th>Assigned</Table.Th>
                  <Table.Th>Completed</Table.Th>
                  <Table.Th>Progress</Table.Th>
                  <Table.Th>Status</Table.Th>
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {progress.judges.map((j) => (
                  <Table.Tr key={j.judgeId} style={j.status === 'NOT_STARTED' ? { backgroundColor: 'rgba(255, 0, 0, 0.05)' } : undefined}>
                    <Table.Td style={{ fontFamily: 'monospace' }}>
                      {j.judgeId.slice(0, 8)}...{j.judgeId.slice(-4)}
                    </Table.Td>
                    <Table.Td>{j.totalAssigned}</Table.Td>
                    <Table.Td>{j.completedReviews}</Table.Td>
                    <Table.Td style={{ width: 160 }}>
                      <Progress value={j.percentage} size="sm" radius="sm" color={j.status === 'COMPLETED' ? 'green' : 'blue'} />
                    </Table.Td>
                    <Table.Td>
                      {j.status === 'NOT_STARTED' && (
                        <Badge color="red" variant="filled">Not Started</Badge>
                      )}
                      {j.status === 'IN_PROGRESS' && (
                        <Badge color="blue" variant="light">In Progress ({j.percentage}%)</Badge>
                      )}
                      {j.status === 'COMPLETED' && (
                        <Badge color="green" variant="light">Completed</Badge>
                      )}
                    </Table.Td>
                  </Table.Tr>
                ))}
              </Table.Tbody>
            </Table>
          </Box>
        )}
      </Stack>
    </Paper>
  );
}
