import { Container, Title, Tabs, Card, Text, ThemeIcon, Group, Box, Badge } from '@mantine/core';
import { IconGavel, IconAlertTriangle, IconClipboardCheck, IconTarget } from '@tabler/icons-react';
import { motion } from 'framer-motion';
import { CoiDeclaration } from './CoiDeclaration';
import { ScoringForm } from './ScoringForm';

export function JudgeDashboard() {
  return (
    <Container size="xl" my="xl" pb={100}>
      <motion.div initial={{ opacity: 0, y: -20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5 }}>
        <Group justify="space-between" align="center" mb="xl" pb="md" style={{ borderBottom: '1px solid rgba(132, 94, 247, 0.2)' }}>
          <Group>
            <ThemeIcon size={50} radius="md" variant="light">
              <IconGavel size="1.8rem" stroke={1.5} />
            </ThemeIcon>
            <Box>
              <Title order={1} style={{ fontFamily: 'Plus Jakarta Sans', letterSpacing: '-1px' }}>Judge Portal</Title>
              <Text c="dimmed" mt="xs">Your secure workspace for evaluating projects and declaring conflicts.</Text>
            </Box>
          </Group>
          <Badge size="xl" color="indigo" variant="light" radius="sm">Role: JUDGE</Badge>
        </Group>
      </motion.div>

      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.2 }}>
        <Card withBorder radius="xl" padding={0} shadow="sm" style={{ background: 'var(--mantine-color-body)', overflow: 'hidden' }}>
          <Tabs defaultValue="assignments" orientation="vertical" placement="left" variant="pills" radius="md">
            <Tabs.List 
              p="md" 
              style={{ 
                borderRight: '1px solid var(--mantine-color-default-border)', 
                background: 'rgba(132, 94, 247, 0.03)',
                minWidth: '250px'
              }}
            >
              <Text size="xs" fw={800} c="dimmed" tt="uppercase" lts={2} mb="sm" pl="sm" mt="xs">Workflow</Text>
              <Tabs.Tab 
                value="coi" 
                leftSection={<IconAlertTriangle size="1.2rem" />} 
                style={{ marginBottom: 5, padding: '12px 16px', fontWeight: 600 }}
              >
                Declare Conflicts (COI)
              </Tabs.Tab>
              <Tabs.Tab 
                value="assignments" 
                leftSection={<IconClipboardCheck size="1.2rem" />}
                style={{ marginBottom: 5, padding: '12px 16px', fontWeight: 600 }}
              >
                My Assignments
              </Tabs.Tab>
              <Tabs.Tab 
                value="calibration" 
                leftSection={<IconTarget size="1.2rem" />}
                style={{ marginBottom: 5, padding: '12px 16px', fontWeight: 600 }}
              >
                Calibration Round
              </Tabs.Tab>
            </Tabs.List>

            <Tabs.Panel value="coi" p="xl" style={{ flex: 1 }}>
              <Box maw={800}>
                <Title order={3} mb="md" style={{ fontFamily: 'Plus Jakarta Sans' }}>Conflict of Interest Declaration</Title>
                <Text c="dimmed" mb="xl">Please declare any teams or participants you are personally affiliated with before beginning your judging assignments.</Text>
                <CoiDeclaration />
              </Box>
            </Tabs.Panel>
            
            <Tabs.Panel value="assignments" p="xl" style={{ flex: 1 }}>
              <Box maw={800}>
                <Title order={3} mb="md" style={{ fontFamily: 'Plus Jakarta Sans' }}>Scoring Assignments</Title>
                <Text c="dimmed" mb="xl">Evaluate your assigned projects according to the rubric. Bayesian shrinkage will automatically be applied to normalize your scores.</Text>
                <ScoringForm />
              </Box>
            </Tabs.Panel>
            
            <Tabs.Panel value="calibration" p="xl" style={{ flex: 1 }}>
              <Box maw={800}>
                <Title order={3} mb="md" style={{ fontFamily: 'Plus Jakarta Sans' }}>Calibration Round</Title>
                <Text c="dimmed" mb="xl">Score this control project to establish your judging baseline.</Text>
                <ScoringForm isCalibration={true} />
              </Box>
            </Tabs.Panel>
          </Tabs>
        </Card>
      </motion.div>
    </Container>
  );
}
