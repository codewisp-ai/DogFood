import { Box, Button, Text, Stack, Paper, Group, Alert } from '@mantine/core';
import { IconDownload, IconDatabaseExport, IconFileSpreadsheet } from '@tabler/icons-react';
import { useState } from 'react';
import { fetchWithAuth } from '../../api';
import { EVENT_ID } from '../../constants';

export function DataExports() {
  const [loading, setLoading] = useState<string | null>(null);
  const [jobId, setJobId] = useState<string | null>(null);

  const startBulkExport = async () => {
    setLoading('bulk');
    try {
      const res = await fetchWithAuth(`/api/events/${EVENT_ID}/bulk/export`, { method: 'POST' });
      if (res && res.jobId) {
        setJobId(res.jobId);
        alert('Bulk export job started. Job ID: ' + res.jobId);
      }
    } catch (e) {
      console.error(e);
      alert('Failed to start bulk export.');
    } finally {
      setLoading(null);
    }
  };

  const downloadCsv = (type: string) => {
    let url = '';
    if (type === 'submissions') url = `/api/events/${EVENT_ID}/submissions/export.csv`;
    if (type === 'users') url = `/api/auth/events/${EVENT_ID}/users/export.csv`;
    if (type === 'votes') url = `/api/voting/${EVENT_ID}/votes/export.csv`;
    if (type === 'results') url = `/api/events/${EVENT_ID}/results/export`;

    if (url) {
      // Direct window open to download via browser standard GET request. 
      // Note: for production, this should pass the JWT auth token properly, 
      // e.g., via a short-lived download token or by downloading via fetch and creating an object URL.
      // We will use the fetch approach to include auth headers.
      fetchWithAuth(url).then(text => {
         const blob = new Blob([text], { type: 'text/csv' });
         const blobUrl = URL.createObjectURL(blob);
         const a = document.createElement('a');
         a.href = blobUrl;
         a.download = `${type}.csv`;
         document.body.appendChild(a);
         a.click();
         a.remove();
      }).catch(e => {
         console.error(e);
         alert(`Failed to download ${type} CSV`);
      });
    }
  };

  return (
    <Box>
      <Text c="dimmed" mb="lg">Download CSV reports for individual subsystems or run a full JSON data lake export via MinIO.</Text>

      <Stack gap="md">
        <Paper withBorder p="md" radius="md">
          <Group justify="space-between">
            <Box>
              <Text fw={600} mb={4} display="flex" style={{alignItems: 'center', gap: 8}}><IconDatabaseExport size={18} /> Full JSON Bulk Export</Text>
              <Text size="sm" c="dimmed">Asynchronously exports all schemas to S3/MinIO for long-term archiving.</Text>
            </Box>
            <Button onClick={startBulkExport} loading={loading === 'bulk'} variant="default">Run Export Job</Button>
          </Group>
          {jobId && <Alert mt="md" title="Job Submitted" color="blue">Export Job {jobId} is processing in the background.</Alert>}
        </Paper>

        <Paper withBorder p="md" radius="md">
          <Text fw={600} mb="md" display="flex" style={{alignItems: 'center', gap: 8}}><IconFileSpreadsheet size={18} /> CSV Stage Reports</Text>
          <Group>
            <Button variant="light" leftSection={<IconDownload size={16}/>} onClick={() => downloadCsv('users')}>Export Users</Button>
            <Button variant="light" leftSection={<IconDownload size={16}/>} onClick={() => downloadCsv('submissions')}>Export Submissions</Button>
            <Button variant="light" leftSection={<IconDownload size={16}/>} onClick={() => downloadCsv('votes')}>Export Votes</Button>
            <Button variant="light" leftSection={<IconDownload size={16}/>} onClick={() => downloadCsv('results')}>Export Results</Button>
          </Group>
        </Paper>
      </Stack>
    </Box>
  );
}
