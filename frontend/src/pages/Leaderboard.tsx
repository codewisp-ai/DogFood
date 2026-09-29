import { Box, Table, Group, Text, Skeleton } from '@mantine/core';
import { PageHeader } from '../components/shared/PageHeader';
import { Container } from '../components/shared/Container';
import { StatusIndicator } from '../components/shared/StatusIndicator';
import { EmptyState } from '../components/shared/EmptyState';
import { IconTrophy } from '@tabler/icons-react';
import { useEffect, useState } from 'react';
import { fetchWithAuth } from '../api';
import { EVENT_ID } from '../constants';

interface FinalScore {
  id: string;
  submissionId: string;
  displayScore: number | null;
  rank: number | null;
  judgeCount: number | null;
  weightedScore: number | null;
}

export function Leaderboard() {
  const [scores, setScores] = useState<FinalScore[]>([]);
  const [loading, setLoading] = useState(true);
  const [updatedAt, setUpdatedAt] = useState<string>('');

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const data = await fetchWithAuth(`/api/events/${EVENT_ID}/results`);
        const list: FinalScore[] = Array.isArray(data) ? data : [];
        // Sort by rank ascending (nulls last)
        list.sort((a, b) => (a.rank ?? 9999) - (b.rank ?? 9999));
        setScores(list);
        setUpdatedAt(new Date().toLocaleTimeString());
      } catch (err) {
        console.error('Failed to fetch leaderboard', err);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  return (
    <Box>
      <PageHeader
        title="Leaderboard"
        breadcrumbs={[{ label: 'Home', href: '/' }, { label: 'Leaderboard' }]}
        description="Live rankings based on judge scoring and community votes."
        actions={
          <Group gap={16} align="center">
            {updatedAt && <Text size="sm" style={{ color: 'var(--text-muted)' }}>Updated {updatedAt}</Text>}
            <StatusIndicator status="Live" />
          </Group>
        }
      />

      <Container denseBody>
        {loading ? (
          <Box p={20}>
            {[1, 2, 3, 4, 5].map(i => <Skeleton key={i} height={44} mb={8} />)}
          </Box>
        ) : scores.length === 0 ? (
          <EmptyState
            icon={<IconTrophy size={24} />}
            title="No results yet"
            description="Judging is still in progress. Results will appear here once judges have submitted scores."
          />
        ) : (
          <Table className="design-table">
            <thead>
              <tr className="design-th">
                <th style={{ width: 80 }}>Rank</th>
                <th>Submission ID</th>
                <th style={{ textAlign: 'right' }}>Judges</th>
                <th style={{ textAlign: 'right' }}>Score</th>
              </tr>
            </thead>
            <tbody>
              {scores.map((row) => (
                <tr key={row.id} className="design-tr">
                  <td className="design-td" style={{ fontFamily: 'var(--font-mono)', fontWeight: 700 }}>
                    {row.rank != null ? `#${row.rank}` : '—'}
                  </td>
                  <td className="design-td" style={{ fontFamily: 'var(--font-mono)', fontSize: 13 }}>
                    {row.submissionId}
                  </td>
                  <td className="design-td" style={{ textAlign: 'right', color: 'var(--text-muted)' }}>
                    {row.judgeCount ?? '—'}
                  </td>
                  <td className="design-td" style={{ textAlign: 'right', fontVariantNumeric: 'tabular-nums', fontWeight: 700 }}>
                    {row.displayScore != null ? Number(row.displayScore).toFixed(2) : '—'}
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
