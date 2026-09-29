import { Box, Table, Button, Group } from '@mantine/core';
import { PageHeader } from '../../components/shared/PageHeader';
import { Container } from '../../components/shared/Container';
import { KeyValue } from '../../components/shared/KeyValue';
import { StatusIndicator } from '../../components/shared/StatusIndicator';
import { Link, useNavigate } from 'react-router-dom';

export function ParticipantDashboard() {
  const navigate = useNavigate();

  return (
    <Box>
      <PageHeader 
        title="Participant Dashboard"
        breadcrumbs={[{ label: 'Home', href: '/' }, { label: 'Dashboard' }]}
        actions={<Button variant="filled" onClick={() => navigate('/submit')}>Create Project</Button>}
      />

      <Container title="Event Status">
        <KeyValue 
          items={[
            { label: 'Registration Deadline', value: '12 Oct 2026, 14:00' },
            { label: 'Submission Deadline', value: '14 Oct 2026, 17:00' },
            { label: 'Judging Phase', value: 'Starts 15 Oct 2026' }
          ]} 
        />
      </Container>

      <Container title="My Projects" count={1} denseBody>
        <Table className="design-table">
          <thead>
            <tr className="design-th">
              <th>Project Name</th>
              <th>Track</th>
              <th>Status</th>
              <th style={{ textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr className="design-tr">
              <td className="design-td">
                <Link to="/submit" style={{ color: 'var(--link)', textDecoration: 'none', fontWeight: 700 }}>
                  Quantum DB
                </Link>
              </td>
              <td className="design-td">FinTech</td>
              <td className="design-td"><StatusIndicator status="Draft" /></td>
              <td className="design-td" style={{ textAlign: 'right' }}>
                <Group gap={8} justify="flex-end">
                  <Button variant="default" size="sm">Edit</Button>
                  <Button variant="default" size="sm">Withdraw</Button>
                </Group>
              </td>
            </tr>
          </tbody>
        </Table>
      </Container>
    </Box>
  );
}
