import { Box, Card, Text, Group, Button, Stack, Loader } from '@mantine/core';
import { useState, useEffect } from 'react';
import { fetchWithAuth } from '../../api';

export function GalleryWidget() {
  const [submissions, setSubmissions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Light mode style override for embedded context
    document.body.style.background = 'transparent';
    document.body.style.padding = '0';
    document.documentElement.style.background = 'transparent';

    const fetchSubmissions = async () => {
      try {
        const data = await fetchWithAuth('/api/submissions/gallery?limit=5');
        setSubmissions(data);
      } catch (err) {
        console.error('Failed to fetch widget submissions', err);
      } finally {
        setLoading(false);
      }
    };
    fetchSubmissions();
  }, []);

  if (loading) return <Box p="md" display="flex" style={{justifyContent: 'center'}}><Loader size="sm" /></Box>;

  return (
    <Stack gap="xs" p={8}>
      {submissions.slice(0, 4).map((sub) => (
        <Card key={sub.id} p="sm" radius="md" style={{ border: '1px solid var(--border)', background: 'var(--surface)' }}>
          <Text fw={600} size="sm" mb={4}>{sub.name}</Text>
          <Text size="xs" c="dimmed" lineClamp={2} mb={8}>{sub.description}</Text>
          <Group justify="space-between" align="center">
            <Text size="xs" fw={500}>{sub.track}</Text>
            <Button component="a" href={`/gallery/${sub.id}`} target="_blank" variant="light" size="compact-xs">View</Button>
          </Group>
        </Card>
      ))}
      <Button component="a" href="/gallery" target="_blank" variant="subtle" size="xs" fullWidth>
        View Full Gallery
      </Button>
    </Stack>
  );
}
