import { Box, Table, Group, Text } from '@mantine/core';
import { PageHeader } from '../components/shared/PageHeader';
import { Container } from '../components/shared/Container';
import { StatusIndicator } from '../components/shared/StatusIndicator';
import { Link } from 'react-router-dom';

export function Leaderboard() {
  const dummyData = [
    { id: '1', rank: 1, name: 'Quantum DB', team: 'Data Ninjas', track: 'FinTech', score: '98.4', votes: 342 },
    { id: '2', rank: 2, name: 'AI Code Reviewer', team: 'Byte Me', track: 'EdTech', score: '96.1', votes: 289 },
    { id: '3', rank: 3, name: 'Green Chain', team: 'Eco Devs', track: 'Sustainability', score: '94.8', votes: 412 },
    { id: '4', rank: 4, name: 'Auto Deploy', team: 'Ship It', track: 'DevOps', score: '91.2', votes: 156 },
    { id: '5', rank: 5, name: 'Smart Tutor', team: 'LearnCo', track: 'EdTech', score: '89.5', votes: 201 },
  ];

  return (
    <Box>
      <PageHeader 
        title="Leaderboard"
        breadcrumbs={[{ label: 'Home', href: '/' }, { label: 'Leaderboard' }]}
        description="Live rankings based on judge scoring and community votes."
        actions={
          <Group gap={16} align="center">
            <Text size="sm" style={{ color: 'var(--text-muted)' }}>Updated just now</Text>
            <StatusIndicator status="Live" />
          </Group>
        }
      />

      <Container denseBody>
        <Table className="design-table">
          <thead>
            <tr className="design-th">
              <th style={{ width: 80 }}>Rank</th>
              <th>Project</th>
              <th>Team</th>
              <th>Track</th>
              <th style={{ textAlign: 'right' }}>Score</th>
              <th style={{ textAlign: 'right' }}>Votes</th>
            </tr>
          </thead>
          <tbody>
            {dummyData.map((row) => (
              <tr key={row.id} className="design-tr">
                <td className="design-td" style={{ fontFamily: 'var(--font-mono)' }}>{row.rank}</td>
                <td className="design-td">
                  <Link to={`/gallery/${row.id}`} style={{ color: 'var(--link)', textDecoration: 'none', fontWeight: 700 }}>
                    {row.name}
                  </Link>
                </td>
                <td className="design-td">{row.team}</td>
                <td className="design-td">{row.track}</td>
                <td className="design-td" style={{ textAlign: 'right', fontVariantNumeric: 'tabular-nums' }}>{row.score}</td>
                <td className="design-td" style={{ textAlign: 'right', fontVariantNumeric: 'tabular-nums', color: 'var(--text-muted)' }}>{row.votes}</td>
              </tr>
            ))}
          </tbody>
        </Table>
      </Container>
    </Box>
  );
}
