import { Box, Table, Text, Button, Skeleton, Alert } from '@mantine/core';
import { PageHeader } from '../../components/shared/PageHeader';
import { Container } from '../../components/shared/Container';
import { StatusIndicator } from '../../components/shared/StatusIndicator';
import { useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { fetchWithAuth } from '../../api';
import { EmptyState } from '../../components/shared/EmptyState';
import { IconGavel } from '@tabler/icons-react';

interface Assignment {
  id: string;
  submissionId: string;
  eventId: string;
  status: string; // PENDING, SCORED, RECUSED
  submissionTitle?: string;
  trackName?: string;
}

export function JudgeDashboard() {
  const navigate = useNavigate();
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await fetchWithAuth('/api/judging/my-assignments');
        setAssignments(Array.isArray(data) ? data : []);
      } catch (err: any) {
        setError(err.message || 'Failed to load assignments');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const scored = assignments.filter(a => a.status === 'SCORED').length;

  return (
    <Box>
      <PageHeader
        title="Judge Portal"
        breadcrumbs={[{ label: 'Home', href: '/' }, { label: 'Assigned Projects' }]}
        description="Your secure workspace for evaluating projects."
        actions={<Button variant="default" onClick={() => navigate('/judge/coi')}>Declare Conflicts</Button>}
      />

      <Container
        title="Assigned projects"
        count={assignments.length}
        denseBody
      >
        {loading ? (
          <Box p={20}>
            {[1, 2, 3].map(i => <Skeleton key={i} height={40} mb={8} />)}
          </Box>
        ) : error ? (
          <Box p={20}>
            <Alert color="red" title="Error">{error}</Alert>
          </Box>
        ) : assignments.length === 0 ? (
          <EmptyState
            icon={<IconGavel size={24} />}
            title="No assignments yet"
            description="The organizer hasn't assigned any projects to you yet."
          />
        ) : (
          <>
            <Box p={20} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
              <Text size="sm" style={{ color: 'var(--text-muted)' }}>
                {scored} of {assignments.length} reviewed
              </Text>
            </Box>
            <Table className="design-table">
              <thead>
                <tr className="design-th">
                  <th>Submission ID</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {assignments.map((row) => (
                  <tr key={row.id} className="design-tr">
                    <td className="design-td" style={{ fontFamily: 'var(--font-mono)', fontSize: 13 }}>
                      {row.submissionId}
                    </td>
                    <td className="design-td">
                      <StatusIndicator status={row.status === 'SCORED' ? 'Approved' : row.status === 'RECUSED' ? 'Draft' : 'Under review'} />
                    </td>
                    <td className="design-td" style={{ textAlign: 'right' }}>
                      {row.status !== 'RECUSED' && (
                        <Button
                          variant="default"
                          size="sm"
                          onClick={() => navigate(`/judge/score/${row.submissionId}`, { state: { eventId: row.eventId, assignmentId: row.id } })}
                        >
                          {row.status === 'SCORED' ? 'Review' : 'Score'}
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </>
        )}
      </Container>
    </Box>
  );
}
