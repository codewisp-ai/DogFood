import { Grid, Card, Text, Group, TextInput, Select, Button, Flex, Skeleton, Box, Pagination } from '@mantine/core';
import { useState, useEffect } from 'react';
import { fetchWithAuth } from '../../api';
import { EVENT_ID } from '../../constants';
import { IconSearch, IconFilter, IconList, IconLayoutGrid } from '@tabler/icons-react';
import { PageHeader } from '../../components/shared/PageHeader';
import { Container } from '../../components/shared/Container';
import { Tag } from '../../components/shared/Tag';
import { VotingWidget } from './VotingWidget';
import { ProjectComments } from './ProjectComments';
import { EmptyState } from '../../components/shared/EmptyState';

export function Gallery() {
  const [search, setSearch] = useState('');
  const [track, setTrack] = useState<string | null>(null);
  const [sort, setSort] = useState<string | null>('Random (Unbiased)');
  const [submissions, setSubmissions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [expandedComments, setExpandedComments] = useState<Record<string, boolean>>({});

  const toggleComments = (id: string) => setExpandedComments(prev => ({ ...prev, [id]: !prev[id] }));

  useEffect(() => {
    const fetchSubmissions = async () => {
      setLoading(true);
      try {
        const queryParams = new URLSearchParams();
        if (search) queryParams.append('search', search);
        if (track) queryParams.append('track', track);
        queryParams.append('page', String(page - 1));
        queryParams.append('size', '12');

        const data = await fetchWithAuth(`/api/events/${EVENT_ID}/gallery?${queryParams.toString()}`);
        let items: any[] = [];
        if (data && Array.isArray(data.content)) {
          items = data.content;
          setTotalPages(data.totalPages ?? 1);
        } else if (Array.isArray(data)) {
          items = data;
        }

        if (sort === 'Random (Unbiased)' && items.length > 0) {
           try {
             const ids = items.map((d: any) => d.id).join(',');
             const randomizedIds = await fetchWithAuth(`/api/voting/${EVENT_ID}/ballot?submissionIds=${ids}`);
             if (Array.isArray(randomizedIds)) {
               items.sort((a: any, b: any) => randomizedIds.indexOf(a.id) - randomizedIds.indexOf(b.id));
             }
           } catch (e) {
             console.error('Failed to randomize ballot', e);
           }
        }
        setSubmissions(items);
      } catch (err) {
        console.error('Failed to fetch submissions', err);
        setSubmissions([]);
      } finally {
        setLoading(false);
      }
    };
    const timer = setTimeout(() => {
      fetchSubmissions();
    }, 300);
    return () => clearTimeout(timer);
  }, [search, track, page]);

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
                    <Box style={{ flex: 1 }}>
                      <Group justify="space-between" align="flex-start" mb={8}>
                        <Text fw={700} style={{ color: 'var(--text)', fontSize: 16 }}>
                          {sub.name}
                        </Text>
                        {sub.tagline && <Tag>{sub.tagline.slice(0, 20)}</Tag>}
                      </Group>

                      <Text size="sm" mb={16} lineClamp={3} style={{ color: 'var(--text-muted)' }}>
                        {sub.description || sub.tagline || 'No description provided.'}
                      </Text>

                      <Group gap={8} mb={20}>
                        {(sub.techTags || []).map((t: string) => (
                          <Tag key={t}>{t}</Tag>
                        ))}
                      </Group>
                    </Box>

                    <Box pt={16} style={{ borderTop: '1px solid var(--border-subtle)' }}>
                      <Group justify="space-between" align="center">
                        <VotingWidget submissionId={sub.id} />
                        <Button variant="subtle" size="xs" onClick={() => toggleComments(sub.id)}>
                          {expandedComments[sub.id] ? 'Hide Comments' : 'Comments'}
                        </Button>
                      </Group>
                      {expandedComments[sub.id] && (
                        <ProjectComments submissionId={sub.id} />
                      )}
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
            <Pagination total={totalPages} value={page} onChange={setPage} size="sm" />
          </Flex>
        </Box>
      </Container>
    </Box>
  );
}
