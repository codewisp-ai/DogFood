import { Box, Paper, Table, Text, Badge, Group, ActionIcon, Tooltip } from '@mantine/core';
import { IconAlertTriangle, IconCheck, IconRefresh } from '@tabler/icons-react';
import { useEffect, useState } from 'react';
import { API_BASE_URL } from '../../api';

export function ForensicScans() {
  const [submissions, setSubmissions] = useState<any[]>([]);

  const fetchSubmissions = async () => {
    // We can fetch submissions from public gallery or organizer specific endpoint
    // Assuming there's an endpoint that returns submissions for the event
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE_URL}/api/events/00000000-0000-0000-0000-000000000001/gallery`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        const data = await response.json();
        setSubmissions(data.content || []);
      }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchSubmissions();
  }, []);

  return (
    <Box>
      <Group justify="space-between" mb="md">
        <Text c="dimmed">Deterministic JGit scanning results for submitted repositories.</Text>
        <ActionIcon variant="light" color="grape" onClick={fetchSubmissions}>
          <IconRefresh size="1.2rem" />
        </ActionIcon>
      </Group>

      <Paper withBorder>
        <Table striped highlightOnHover>
          <Table.Thead>
            <Table.Tr>
              <Table.Th>Project Name</Table.Th>
              <Table.Th>Repository</Table.Th>
              <Table.Th>Status</Table.Th>
              <Table.Th>Risk Score</Table.Th>
              <Table.Th>Flags</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {submissions.map(sub => (
              <Table.Tr key={sub.id}>
                <Table.Td fw={600}>{sub.name}</Table.Td>
                <Table.Td>
                  <Text size="sm" c="blue" component="a" href={sub.repositoryUrl} target="_blank">
                    {sub.repositoryUrl || 'No repo'}
                  </Text>
                </Table.Td>
                <Table.Td>
                  {sub.forensicStatus === 'SCANNED' ? (
                    <Badge color="green" variant="light">Scanned</Badge>
                  ) : sub.forensicStatus === 'FAILED' ? (
                    <Badge color="red" variant="light">Failed</Badge>
                  ) : (
                    <Badge color="yellow" variant="light">Pending</Badge>
                  )}
                </Table.Td>
                <Table.Td>
                  {sub.riskScore !== undefined ? (
                    <Badge color={sub.riskScore > 50 ? 'red' : sub.riskScore > 0 ? 'orange' : 'green'} size="lg">
                      {sub.riskScore.toFixed(1)} / 100
                    </Badge>
                  ) : '-'}
                </Table.Td>
                <Table.Td>
                  {sub.riskFlags && sub.riskFlags.length > 0 ? (
                    <Tooltip label={sub.riskFlags.join(', ')} multiline w={250}>
                      <Group gap="xs">
                        <IconAlertTriangle size="1rem" color="red" />
                        <Text size="sm" c="red" fw={500}>{sub.riskFlags.length} Flags</Text>
                      </Group>
                    </Tooltip>
                  ) : sub.forensicStatus === 'SCANNED' ? (
                    <Group gap="xs">
                      <IconCheck size="1rem" color="green" />
                      <Text size="sm" c="green" fw={500}>Clean</Text>
                    </Group>
                  ) : '-'}
                </Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Paper>
    </Box>
  );
}
