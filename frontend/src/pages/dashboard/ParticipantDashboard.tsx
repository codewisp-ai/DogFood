import { Box, Table, Group, Button, Skeleton } from '@mantine/core';
import { PageHeader } from '../../components/shared/PageHeader';
import { Container } from '../../components/shared/Container';
import { KeyValue } from '../../components/shared/KeyValue';
import { StatusIndicator } from '../../components/shared/StatusIndicator';
import { EmptyState } from '../../components/shared/EmptyState';
import { IconUpload } from '@tabler/icons-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchWithAuth } from '../../api';
import { EVENT_ID } from '../../constants';

interface Submission {
  id: string;
  name: string;
  trackId?: string;
  status: string;
  submittedAt?: string;
}

interface EventInfo {
  name: string;
  submissionDeadline?: string;
  status?: string;
}

export function ParticipantDashboard() {
  const navigate = useNavigate();
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [event, setEvent] = useState<EventInfo | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        // Load event info and participant's own submissions in parallel
        const [eventData, submissionData] = await Promise.allSettled([
          fetchWithAuth(`/api/events/sample-hack-2026`),
          fetchWithAuth(`/api/events/${EVENT_ID}/submissions/export`)
        ]);

        if (eventData.status === 'fulfilled' && eventData.value) {
          setEvent(eventData.value);
        }
        if (submissionData.status === 'fulfilled') {
          const list = Array.isArray(submissionData.value) ? submissionData.value : [];
          setSubmissions(list);
        }
      } catch (err) {
        console.error('Failed to load dashboard', err);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const deadlineStr = event?.submissionDeadline
    ? new Date(event.submissionDeadline).toLocaleString()
    : 'Not set';

  return (
    <Box>
      <PageHeader
        title="Participant Dashboard"
        breadcrumbs={[{ label: 'Home', href: '/' }, { label: 'Dashboard' }]}
        actions={<Button variant="filled" onClick={() => navigate('/submit')}>Submit Project</Button>}
      />

      <Container title="Event Status">
        <KeyValue
          items={[
            { label: 'Event', value: event?.name ?? '—' },
            { label: 'Submission Deadline', value: deadlineStr },
            { label: 'Event Status', value: event?.status ?? '—' },
          ]}
        />
      </Container>

      <Container title="My Submissions" count={submissions.length} denseBody>
        {loading ? (
          <Box p={20}>{[1, 2].map(i => <Skeleton key={i} height={40} mb={8} />)}</Box>
        ) : submissions.length === 0 ? (
          <EmptyState
            icon={<IconUpload size={24} />}
            title="No submissions yet"
            description="Submit your project to participate in the hackathon."
            actionLabel="Submit Project"
            onAction={() => navigate('/submit')}
          />
        ) : (
          <Table className="design-table">
            <thead>
              <tr className="design-th">
                <th>Project Name</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {submissions.map((row) => (
                <tr key={row.id} className="design-tr">
                  <td className="design-td" style={{ fontWeight: 700 }}>{row.name}</td>
                  <td className="design-td">
                    <StatusIndicator status={row.status === 'SUBMITTED' ? 'Approved' : row.status === 'DRAFT' ? 'Draft' : 'Under review'} />
                  </td>
                  <td className="design-td" style={{ textAlign: 'right' }}>
                    <Group gap={8} justify="flex-end">
                      <Button variant="default" size="sm" onClick={() => navigate('/submit')}>Edit</Button>
                    </Group>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        )}
      </Container>
    </Box>
  );
}
