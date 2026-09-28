import { Tabs, Title, Container, Card, Text, ThemeIcon, Group, Box, Badge } from '@mantine/core';
import { IconDashboard, IconCalendarEvent, IconShieldLock, IconListCheck, IconChartBar } from '@tabler/icons-react';
import { motion } from 'framer-motion';
import { CreateEvent } from './CreateEvent';
import { ManageRubric } from './ManageRubric';
import { EligibilityRules } from './EligibilityRules';
import { JudgeProgress } from './JudgeProgress';
import { ForensicScans } from './ForensicScans';
import { useAuth } from '../../context/AuthContext';

export function Dashboard() {
  const { user } = useAuth();
  
  return (
    <Container size="xl" my="xl" pb={100}>
      <motion.div initial={{ opacity: 0, y: -20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5 }}>
        <Group justify="space-between" align="center" mb="xl" pb="md" style={{ borderBottom: '1px solid rgba(132, 94, 247, 0.2)' }}>
          <Group>
            <ThemeIcon size={50} radius="md" variant="gradient" gradient={{ from: 'grape', to: 'indigo' }}>
              <IconDashboard size="1.8rem" stroke={1.5} />
            </ThemeIcon>
            <Box>
              <Title order={1} style={{ fontFamily: 'Plus Jakarta Sans', letterSpacing: '-1px' }}>Organizer Dashboard</Title>
              <Text c="dimmed" mt="xs">Complete control center for managing your hackathon event.</Text>
            </Box>
          </Group>
          <Badge size="xl" color="grape" variant="light" radius="sm">Role: ADMIN</Badge>
        </Group>
      </motion.div>

      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.2 }}>
        <Card withBorder radius="xl" padding={0} shadow="sm" style={{ background: 'var(--mantine-color-body)', overflow: 'hidden' }}>
          <Tabs defaultValue="create" orientation="vertical" placement="left" variant="pills" radius="md">
            <Tabs.List 
              p="md" 
              style={{ 
                borderRight: '1px solid var(--mantine-color-default-border)', 
                background: 'rgba(132, 94, 247, 0.03)',
                minWidth: '250px'
              }}
            >
              <Text size="xs" fw={800} c="dimmed" tt="uppercase" lts={2} mb="sm" pl="sm" mt="xs">Management</Text>
              <Tabs.Tab 
                value="create" 
                leftSection={<IconCalendarEvent size="1.2rem" />}
                style={{ marginBottom: 5, padding: '12px 16px', fontWeight: 600 }}
              >
                Event Setup
              </Tabs.Tab>
              <Tabs.Tab 
                value="rules" 
                leftSection={<IconShieldLock size="1.2rem" />}
                style={{ marginBottom: 5, padding: '12px 16px', fontWeight: 600 }}
              >
                Eligibility Rules
              </Tabs.Tab>
              <Tabs.Tab 
                value="rubric" 
                leftSection={<IconListCheck size="1.2rem" />}
                style={{ marginBottom: 5, padding: '12px 16px', fontWeight: 600 }}
              >
                Judging Rubric
              </Tabs.Tab>
              <Tabs.Tab 
                value="progress" 
                leftSection={<IconChartBar size="1.2rem" />}
                style={{ marginBottom: 5, padding: '12px 16px', fontWeight: 600 }}
              >
                Live Progress
              </Tabs.Tab>
              <Tabs.Tab 
                value="forensics" 
                leftSection={<IconShieldLock size="1.2rem" />}
                style={{ marginBottom: 5, padding: '12px 16px', fontWeight: 600 }}
              >
                JGit Forensics
              </Tabs.Tab>
            </Tabs.List>

            <Tabs.Panel value="create" p="xl" style={{ flex: 1 }}>
              <Box maw={800}>
                <Title order={3} mb="md" style={{ fontFamily: 'Plus Jakarta Sans' }}>Event Configuration</Title>
                <Text c="dimmed" mb="xl">Create and configure the main event details, registration deadlines, and tracks.</Text>
                <CreateEvent />
              </Box>
            </Tabs.Panel>
            <Tabs.Panel value="rules" p="xl" style={{ flex: 1 }}>
              <Box maw={800}>
                <Title order={3} mb="md" style={{ fontFamily: 'Plus Jakarta Sans' }}>Eligibility & Security</Title>
                <Text c="dimmed" mb="xl">Define domain restrictions, team size limits, and participant requirements.</Text>
                <EligibilityRules />
              </Box>
            </Tabs.Panel>
            <Tabs.Panel value="rubric" p="xl" style={{ flex: 1 }}>
              <Box maw={800}>
                <Title order={3} mb="md" style={{ fontFamily: 'Plus Jakarta Sans' }}>Scoring Engine</Title>
                <Text c="dimmed" mb="xl">Design the judging rubric dimensions (e.g., Technical Complexity, Design) and weights.</Text>
                <ManageRubric />
              </Box>
            </Tabs.Panel>
            <Tabs.Panel value="progress" p="xl" style={{ flex: 1 }}>
              <Box maw={800}>
                <Title order={3} mb="md" style={{ fontFamily: 'Plus Jakarta Sans' }}>Real-time Telemetry</Title>
                <Text c="dimmed" mb="xl">Monitor judging completion rates and automated Bayesian score normalizations live.</Text>
                <JudgeProgress />
              </Box>
            </Tabs.Panel>
            <Tabs.Panel value="forensics" p="xl" style={{ flex: 1 }}>
              <Box>
                <Title order={3} mb="md" style={{ fontFamily: 'Plus Jakarta Sans' }}>JGit Forensic Scanner</Title>
                <ForensicScans />
              </Box>
            </Tabs.Panel>
          </Tabs>
        </Card>
      </motion.div>
    </Container>
  );
}
