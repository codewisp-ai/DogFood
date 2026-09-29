import { Box, Table, Group, Text, Button } from '@mantine/core';
import { PageHeader } from '../../components/shared/PageHeader';
import { Container } from '../../components/shared/Container';
import { StatusIndicator } from '../../components/shared/StatusIndicator';
import { Link } from 'react-router-dom';

export function JudgeDashboard() {
  const dummyData = [
    { id: '1', name: 'Quantum DB', track: 'FinTech', status: 'Approved' },
    { id: '2', name: 'AI Code Reviewer', track: 'EdTech', status: 'Under review' },
    { id: '3', name: 'Green Chain', track: 'Sustainability', status: 'Draft' },
  ];

  return (
    <Box>
      <PageHeader 
        title="Judge Portal"
        breadcrumbs={[{ label: 'Home', href: '/' }, { label: 'Assigned Projects' }]}
        description="Your secure workspace for evaluating projects."
        actions={<Button variant="default">Declare Conflicts</Button>}
      />

      <Container 
        title="Assigned projects"
        count={3}
        denseBody
      >
        <Box p={20} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
          <Text size="sm" style={{ color: 'var(--text-muted)' }}>1 of 3 reviewed</Text>
        </Box>
        <Table className="design-table">
          <thead>
            <tr className="design-th">
              <th>Project</th>
              <th>Track</th>
              <th>Status</th>
              <th style={{ textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {dummyData.map((row) => (
              <tr key={row.id} className="design-tr">
                <td className="design-td">
                  <Link to={`/judge/score/${row.id}`} style={{ color: 'var(--link)', textDecoration: 'none', fontWeight: 700 }}>
                    {row.name}
                  </Link>
                </td>
                <td className="design-td">{row.track}</td>
                <td className="design-td"><StatusIndicator status={row.status as any} /></td>
                <td className="design-td" style={{ textAlign: 'right' }}>
                  <Button variant="ghost" size="sm">Score</Button>
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      </Container>
    </Box>
  );
}
