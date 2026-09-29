import { Grid, Card, Text, Group, TextInput, Select, Button, Flex, Skeleton, Box, Pagination } from '@mantine/core';
import { useState, useEffect } from 'react';
import { fetchWithAuth } from '../../api';
import { IconSearch, IconFilter, IconList, IconLayoutGrid } from '@tabler/icons-react';
import { PageHeader } from '../../components/shared/PageHeader';
import { Container } from '../../components/shared/Container';
import { Tag } from '../../components/shared/Tag';
import { VotingWidget } from './VotingWidget';
import { EmptyState } from '../../components/shared/EmptyState';
import { Link } from 'react-router-dom';

export function Gallery() {
  const [search, setSearch] = useState('');
  const [track, setTrack] = useState<string | null>(null);
  const [sort, setSort] = useState<string | null>('Random (Unbiased)');
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
        // If sorting randomly, use the deterministic backend ballot randomization
        if (sort === 'Random (Unbiased)' && data.length > 0) {
           try {
             const ids = data.map((d: any) => d.id).join(',');
             
             // Fetch from real voting service to get seeded random order
             const randomizedIds = await fetchWithAuth(`/api/voting/${EVENT_ID}/ballot?submissionIds=${ids}`);
             if (Array.isArray(randomizedIds)) {
               data.sort((a: any, b: any) => randomizedIds.indexOf(a.id) - randomizedIds.indexOf(b.id));
             }
           } catch (e) {
             console.error('Failed to randomize ballot', e);
           }
        }
        setSubmissions(data);
      } catch (err) {
        console.error('Failed to fetch submissions', err);
      } finally {
        setLoading(false);
      }
    };
    const timer = setTimeout(() => {
      fetchSubmissions();
    }, 300);
    return () => clearTimeout(timer);
  }, [search, track]);

  return (
    <Box>
      <PageHeader 
        title="Project Gallery"
        description="Discover and vote on the incredible projects shipped this weekend."
        breadcrumbs={[{ label: 'Home', href: '/' }, { label: 'Gallery' }]}
      />

      <Container denseBody>
        <Box p={20} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
          <Flex gap={16} align="center" wrap="wrap">
            <TextInput 
              placeholder="Search projects by name, tags, or description..." 
              value={search}
              onChange={(e) => setSearch(e.currentTarget.value)}
              flex={1}
              leftSection={<IconSearch size={16} color="var(--text-muted)" />}
            />
            <Select 
              placeholder="Filter by Track" 
              data={['EdTech', 'Sustainability', 'FinTech']}
              value={track}
              onChange={setTrack}
              clearable
              leftSection={<IconFilter size={16} color="var(--text-muted)" />}
              w={{ base: '100%', sm: 200 }}
            />
            <Select 
              placeholder="Sort by"
              data={['Random (Unbiased)', 'Most Voted', 'Newest', 'Alphabetical']}
              value={sort}
              onChange={setSort}
              
              w={{ base: '100%', sm: 160 }}
            />
            <Group gap={8}>
              <Button variant="default" px={8}><IconLayoutGrid size={16} /></Button>
              <Button variant="ghost" px={8}><IconList size={16} color="var(--text-muted)" /></Button>
            </Group>
          </Flex>
        </Box>

        <Box p={20}>
          <Grid gutter={20}>
            {loading ? (
              Array(6).fill(0).map((_, i) => (
                <Grid.Col key={`skeleton-${i}`} span={{ base: 12, sm: 6, lg: 4 }}>
                  <Card style={{ height: '100%' }}>
                    <Skeleton height={20} width="60%" mb={16} />
                    <Skeleton height={14} width="100%" mb={8} />
                    <Skeleton height={14} width="80%" mb={24} />
                    <Group gap={8} mb={24}>
                      <Skeleton height={20} width={60} radius="sm" />
                      <Skeleton height={20} width={60} radius="sm" />
                    </Group>
                    <Skeleton height={32} width="100%" radius="sm" />
                  </Card>
                </Grid.Col>
              ))
            ) : submissions.length > 0 ? (
              submissions.map((sub) => (
                <Grid.Col key={sub.id} span={{ base: 12, sm: 6, lg: 4 }}>
                  <Card style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                    {/* Optional 16:9 thumbnail top could go here if we had images */}
                    <Box style={{ flex: 1 }}>
                      <Group justify="space-between" align="flex-start" mb={8}>
                        <Text component={Link} to={`/gallery/${sub.id}`} fw={700} style={{ color: 'var(--link)', textDecoration: 'none', fontSize: 16 }}>
                          {sub.name}
                        </Text>
                        <Tag>{sub.track}</Tag>
                      </Group>
                      
                      <Text size="sm" mb={16} lineClamp={3} style={{ color: 'var(--text-muted)' }}>
                        {sub.description}
                      </Text>
                      
                      <Group gap={8} mb={20}>
                        {(sub.tags || []).map((t: string) => (
                          <Tag key={t}>{t}</Tag>
                        ))}
                      </Group>
                    </Box>

                    <Box pt={16} style={{ borderTop: '1px solid var(--border-subtle)' }}>
                      <VotingWidget submissionId={sub.id} />
                    </Box>
                  </Card>
                </Grid.Col>
              ))
            ) : (
              <Grid.Col span={12}>
                <EmptyState 
                  icon={<IconSearch size={24} />}
                  title="No projects found"
                  description="We couldn't find any projects matching your search criteria."
                  actionLabel="Clear filters"
                  onAction={() => { setSearch(''); setTrack(null); }}
                />
              </Grid.Col>
            )}
          </Grid>
          
          <Flex justify="center" mt={32}>
            <Pagination total={10} value={1} size="sm" />
          </Flex>
        </Box>
      </Container>
    </Box>
  );
}
