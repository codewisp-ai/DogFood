import { Box, Tabs } from '@mantine/core';
import { PageHeader } from '../../components/shared/PageHeader';
import { Container } from '../../components/shared/Container';
import { CreateEvent } from './CreateEvent';
import { ManageRubric } from './ManageRubric';
import { EligibilityRules } from './EligibilityRules';
import { JudgeProgress } from './JudgeProgress';
import { ForensicScans } from './ForensicScans';
import { DataExports } from './DataExports';

export function AdminDashboard() {
  return (
    <Box>
      <PageHeader 
        title="Admin Dashboard"
        breadcrumbs={[{ label: 'Home', href: '/' }, { label: 'Admin' }]}
        description="Complete control center for managing your hackathon event."
      />

      <Tabs defaultValue="create" classNames={{
        list: 'design-tabs-list',
        tab: 'design-tab',
      }}>
        <Tabs.List mb={24}>
          <Tabs.Tab value="create">Event Setup</Tabs.Tab>
          <Tabs.Tab value="rules">Eligibility Rules</Tabs.Tab>
          <Tabs.Tab value="rubric">Judging Rubric</Tabs.Tab>
          <Tabs.Tab value="progress">Live Progress</Tabs.Tab>
          <Tabs.Tab value="forensics">JGit Forensics</Tabs.Tab>
          <Tabs.Tab value="exports">Data Exports</Tabs.Tab>
        </Tabs.List>

        <Tabs.Panel value="create">
          <Container title="Event Configuration">
            <CreateEvent />
          </Container>
        </Tabs.Panel>
        <Tabs.Panel value="rules">
          <Container title="Eligibility & Security">
            <EligibilityRules />
          </Container>
        </Tabs.Panel>
        <Tabs.Panel value="rubric">
          <Container title="Scoring Engine">
            <ManageRubric />
          </Container>
        </Tabs.Panel>
        <Tabs.Panel value="progress">
          <Container title="Real-time Telemetry">
            <JudgeProgress />
          </Container>
        </Tabs.Panel>
        <Tabs.Panel value="forensics">
          <Container title="JGit Forensic Scanner">
            <ForensicScans />
          </Container>
        </Tabs.Panel>
        <Tabs.Panel value="exports">
          <Container title="Data Export Operations">
            <DataExports />
          </Container>
        </Tabs.Panel>
      </Tabs>
    </Box>
  );
}
