import { Title, Container, Grid, Card, Text, Badge, Group, TextInput, Select, Button, ThemeIcon, ActionIcon, Flex, Skeleton, Box } from '@mantine/core';
import { useState, useEffect } from 'react';
import { VotingWidget } from './VotingWidget';
import { fetchWithAuth } from '../../api';
import { motion, AnimatePresence } from 'framer-motion';
import { IconSearch, IconFilter, IconRocket } from '@tabler/icons-react';

export function Gallery() {
  const [search, setSearch] = useState('');
  const [track, setTrack] = useState<string | null>(null);
  const [submissions, setSubmissions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchSubmissions = async () => {
      setLoading(true);
      try {
        const queryParams = new URLSearchParams();
        if (search) queryParams.append('search', search);
        if (track) queryParams.append('track', track);
        
        const data = await fetchWithAuth(`/api/submissions/gallery?${queryParams.toString()}`);
        setSubmissions(data);
      } catch (err) {
        console.error('Failed to fetch submissions', err);
      } finally {
        setLoading(false);
      }
    };
    // Debounce search slightly
    const timer = setTimeout(() => {
      fetchSubmissions();
    }, 300);
    return () => clearTimeout(timer);
  }, [search, track]);

  return (
    <Container size="xl" my="xl" pb={100}>
      <motion.div initial={{ opacity: 0, y: -20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5 }}>
        <Group justify="space-between" align="center" mb="xl" pb="md" style={{ borderBottom: '1px solid rgba(132, 94, 247, 0.2)' }}>
          <Box>
            <Title order={1} style={{ fontFamily: 'Plus Jakarta Sans', letterSpacing: '-1px' }}>Project Gallery</Title>
            <Text c="dimmed" mt="xs">Discover and vote on the incredible projects shipped this weekend.</Text>
          </Box>
          <Badge size="xl" variant="dot" color="teal">Live Voting Open</Badge>
        </Group>

        <Card withBorder padding="md" radius="xl" mb={40} >
          <Flex gap="md" align="center" wrap="wrap">
            <TextInput 
              placeholder="Search projects by name, tags, or description..." 
              value={search}
              onChange={(e) => setSearch(e.currentTarget.value)}
              flex={1}
              size="md"
              radius="xl"
              leftSection={<IconSearch size="1.1rem" stroke={1.5} />}
              styles={{ input: { border: '1px solid rgba(132, 94, 247, 0.3)' } }}
            />
            <Select 
              placeholder="Filter by Track" 
              data={['EdTech', 'Sustainability', 'FinTech']}
              value={track}
              onChange={setTrack}
              clearable
              size="md"
              radius="xl"
              leftSection={<IconFilter size="1.1rem" stroke={1.5} />}
              w={{ base: '100%', sm: 200 }}
            />
          </Flex>
        </Card>
      </motion.div>

      <Grid gutter="xl">
        <AnimatePresence mode="popLayout">
          {loading ? (
            Array(6).fill(0).map((_, i) => (
              <Grid.Col key={`skeleton-${i}`} span={{ base: 12, sm: 6, md: 4 }}>
                <Card padding="lg" radius="xl" withBorder>
                  <Skeleton height={20} width="60%" mb="md" />
                  <Skeleton height={15} width="100%" mb="xs" />
                  <Skeleton height={15} width="80%" mb="xl" />
                  <Group gap="xs" mb="lg">
                    <Skeleton height={24} width={60} radius="xl" />
                    <Skeleton height={24} width={60} radius="xl" />
                  </Group>
                  <Skeleton height={36} width="100%" radius="md" />
                </Card>
              </Grid.Col>
            ))
          ) : submissions.length > 0 ? (
            submissions.map((sub, index) => (
              <Grid.Col key={sub.id} span={{ base: 12, sm: 6, md: 4 }}>
                <motion.div
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.9 }}
                  transition={{ duration: 0.3, delay: index * 0.05 }}
                  style={{ height: '100%' }}
                >
                  <Card h="100%" padding="xl" radius="xl" withBorder style={{ display: 'flex', flexDirection: 'column' }}>
                    <Group justify="space-between" mb="md" align="flex-start">
                      <Group gap="sm" style={{ flex: 1 }}>
                        <ThemeIcon size={40} radius="md" variant="light" color="indigo">
                          <IconRocket size="1.4rem" stroke={1.5} />
                        </ThemeIcon>
                        <Title order={4} style={{ fontFamily: 'Plus Jakarta Sans', letterSpacing: '-0.5px' }} lineClamp={1}>
                          {sub.name}
                        </Title>
                      </Group>
                      <Badge color="pink" variant="light" size="sm" radius="sm">{sub.track}</Badge>
                    </Group>
                    
                    <Text size="sm" c="dimmed" mb="xl" lineClamp={3} style={{ flex: 1, lineHeight: 1.6 }}>
                      {sub.description}
                    </Text>
                    
                    <Group gap="xs" mb="xl">
                      {(sub.tags || []).map((t: string) => (
                        <Badge key={t} size="xs" variant="outline" color="indigo" radius="sm" style={{ textTransform: 'none' }}>
                          {t}
                        </Badge>
                      ))}
                    </Group>

                    <Box mt="auto" pt="md" style={{ borderTop: '1px solid var(--mantine-color-default-border)' }}>
                      <VotingWidget submissionId={sub.id} />
                    </Box>
                  </Card>
                </motion.div>
              </Grid.Col>
            ))
          ) : (
            <Grid.Col span={12}>
              <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
                <Text ta="center" c="dimmed" size="lg" mt={40}>
                  No projects found matching your search criteria.
                </Text>
              </motion.div>
            </Grid.Col>
          )}
        </AnimatePresence>
      </Grid>
    </Container>
  );
}
