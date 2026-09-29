import { Box, Flex, Title, Text, Button, Group, SimpleGrid, Card, Container as MantineContainer, Divider } from '@mantine/core';
import { Link } from 'react-router-dom';
import { IconArrowRight, IconCode, IconGavel, IconTrophy, IconLiveView } from '@tabler/icons-react';
import { Container } from '../components/shared/Container';
import { StatusIndicator } from '../components/shared/StatusIndicator';
import { KeyValue } from '../components/shared/KeyValue';
import { Table } from '@mantine/core'; // We will use mantine table with design-table class

export function Home() {
  return (
    <Box style={{ backgroundColor: 'var(--bg)', width: '100vw', marginLeft: 'calc(-50vw + 50%)' }}>
      
      {/* 1. Hero Band */}
      <Box style={{ backgroundColor: 'var(--nav-bg)', padding: '64px 0' }}>
        <MantineContainer size={1280} px={24}>
          <SimpleGrid cols={{ base: 1, md: 12 }} spacing={64}>
            {/* Left Column (7) */}
            <Box style={{ gridColumn: 'span 7' }}>
              <Text size="sm" style={{ color: 'var(--nav-text-muted)', marginBottom: 16 }}>THE PREMIER HACKATHON ENGINE</Text>
              <Title 
                order={1} 
                style={{ 
                  fontSize: 44, 
                  lineHeight: '52px',
                  fontWeight: 700,
                  color: 'var(--nav-text)',
                  marginBottom: 24
                }}
              >
                Built for those who ship.
              </Title>
              <Text size="lg" style={{ color: 'var(--nav-text-muted)', maxWidth: '60ch', marginBottom: 24 }}>
                A masterclass platform enforcing role isolation, rigorous Bayesian judging shrinkage, and real-time observability.
              </Text>
              <Group mt={24}>
                <Button size="lg" component={Link} to="/register" style={{ height: 40, backgroundColor: 'var(--accent)', color: 'white' }}>
                  Start building
                </Button>
                <Button size="lg" variant="outline" component={Link} to="/gallery" style={{ height: 40, borderColor: 'white', color: 'white', backgroundColor: 'transparent' }}>
                  Explore gallery
                </Button>
              </Group>
            </Box>

            {/* Right Column (5) */}
            <Box style={{ gridColumn: 'span 5' }}>
              <Box className="design-container" p={24} style={{ backgroundColor: 'var(--surface)' }}>
                <Title order={2} mb={20}>Get started</Title>
                <Box mb={24}>
                  <Flex align="flex-start" gap={12} mb={12}>
                    <Text fw={700} style={{ color: 'var(--text-muted)' }}>1.</Text>
                    <Text style={{ color: 'var(--text)' }}>Register for an account and configure your profile.</Text>
                  </Flex>
                  <Flex align="flex-start" gap={12} mb={12}>
                    <Text fw={700} style={{ color: 'var(--text-muted)' }}>2.</Text>
                    <Text style={{ color: 'var(--text)' }}>Submit your project with repository links and demo.</Text>
                  </Flex>
                  <Flex align="flex-start" gap={12}>
                    <Text fw={700} style={{ color: 'var(--text-muted)' }}>3.</Text>
                    <Text style={{ color: 'var(--text)' }}>Get judged securely without bias.</Text>
                  </Flex>
                </Box>
                <Button fullWidth component={Link} to="/register" mb={16}>
                  Register
                </Button>
                <Text size="sm" ta="center">
                  Already have an account? <Link to="/login" style={{ color: 'var(--link)', textDecoration: 'none' }}>Sign in</Link>
                </Text>
              </Box>
            </Box>
          </SimpleGrid>
        </MantineContainer>
      </Box>

      {/* 2. Capabilities */}
      <Box p="48px 0" style={{ backgroundColor: 'var(--bg)' }}>
        <MantineContainer size={1280} px={24}>
          <Title order={2} mb={32}>Capabilities</Title>
          <SimpleGrid cols={{ base: 1, md: 3 }} spacing={32}>
            <Box>
              <IconCode size={24} color="var(--text)" style={{ marginBottom: 16 }} />
              <Title order={3} mb={12}>Seamless Submissions</Title>
              <Text style={{ color: 'var(--text-muted)' }}>
                Drop-in Markdown support, automated tech stack tagging, and instant repository integrations for your team's code.
              </Text>
            </Box>
            <Box>
              <IconGavel size={24} color="var(--text)" style={{ marginBottom: 16 }} />
              <Title order={3} mb={12}>Bayesian Judging</Title>
              <Text style={{ color: 'var(--text-muted)' }}>
                Eliminate judge bias instantly. Our engine normalizes scores across tracks using sophisticated statistical shrinkage.
              </Text>
            </Box>
            <Box>
              <IconTrophy size={24} color="var(--text)" style={{ marginBottom: 16 }} />
              <Title order={3} mb={12}>Real-time Gallery</Title>
              <Text style={{ color: 'var(--text-muted)' }}>
                Watch the leaderboard evolve live. Participants and the public can vote and view projects the moment they ship.
              </Text>
            </Box>
          </SimpleGrid>
        </MantineContainer>
      </Box>

      {/* Main Content Area for rest of the items */}
      <MantineContainer size={1280} px={24} pb={64}>
        
        {/* 3. How it works */}
        <Container title="How it works">
          <SimpleGrid cols={{ base: 1, md: 4 }} spacing={24}>
            <Box>
              <Text style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', marginBottom: 8 }}>01</Text>
              <Title order={4} mb={8}>Create</Title>
              <Text size="sm" style={{ color: 'var(--text-muted)' }}>Form a team and start building your idea.</Text>
            </Box>
            <Box>
              <Text style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', marginBottom: 8 }}>02</Text>
              <Title order={4} mb={8}>Submit</Title>
              <Text size="sm" style={{ color: 'var(--text-muted)' }}>Upload code and demo before the deadline.</Text>
            </Box>
            <Box>
              <Text style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', marginBottom: 8 }}>03</Text>
              <Title order={4} mb={8}>Review</Title>
              <Text size="sm" style={{ color: 'var(--text-muted)' }}>Judges evaluate based on defined rubrics.</Text>
            </Box>
            <Box>
              <Text style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', marginBottom: 8 }}>04</Text>
              <Title order={4} mb={8}>Win</Title>
              <Text size="sm" style={{ color: 'var(--text-muted)' }}>Top normalized scores take the prizes.</Text>
            </Box>
          </SimpleGrid>
        </Container>

        {/* 4. Live leaderboard preview */}
        <Container 
          title={<Group gap={12}>Top projects <StatusIndicator status="Live" /></Group>}
          actions={<Button variant="subtle" component={Link} to="/leaderboard">View all</Button>}
          denseBody
        >
          <Table className="design-table">
            <thead>
              <tr className="design-th">
                <th>Rank</th>
                <th>Project</th>
                <th>Team</th>
                <th style={{ textAlign: 'right' }}>Score</th>
              </tr>
            </thead>
            <tbody>
              <tr className="design-tr">
                <td className="design-td" style={{ fontFamily: 'var(--font-mono)' }}>1</td>
                <td className="design-td"><Link to="/gallery">Quantum DB</Link></td>
                <td className="design-td">Data Ninjas</td>
                <td className="design-td" style={{ textAlign: 'right', fontVariantNumeric: 'tabular-nums' }}>98.4</td>
              </tr>
              <tr className="design-tr">
                <td className="design-td" style={{ fontFamily: 'var(--font-mono)' }}>2</td>
                <td className="design-td"><Link to="/gallery">AI Code Reviewer</Link></td>
                <td className="design-td">Byte Me</td>
                <td className="design-td" style={{ textAlign: 'right', fontVariantNumeric: 'tabular-nums' }}>96.1</td>
              </tr>
              <tr className="design-tr">
                <td className="design-td" style={{ fontFamily: 'var(--font-mono)' }}>3</td>
                <td className="design-td"><Link to="/gallery">Green Chain</Link></td>
                <td className="design-td">Eco Devs</td>
                <td className="design-td" style={{ textAlign: 'right', fontVariantNumeric: 'tabular-nums' }}>94.8</td>
              </tr>
            </tbody>
          </Table>
        </Container>

        {/* 5. Platform numbers */}
        <Container title="Platform Stats">
          <KeyValue 
            items={[
              { label: 'Projects', value: '1,024' },
              { label: 'Participants', value: '3,492' },
              { label: 'Judges', value: '45' },
              { label: 'Votes', value: '12,841' }
            ]} 
          />
        </Container>
      </MantineContainer>

      {/* 6. Footer */}
      <Box style={{ backgroundColor: 'var(--nav-bg)', padding: '48px 0 24px' }}>
        <MantineContainer size={1280} px={24}>
          <SimpleGrid cols={{ base: 2, md: 4 }} spacing={32} mb={48}>
            <Box>
              <Text fw={700} mb={16} style={{ color: 'var(--nav-text)' }}>Platform</Text>
              <Box mb={8}><Link to="#" style={{ color: 'var(--nav-text-muted)', fontSize: 14 }}>Features</Link></Box>
              <Box mb={8}><Link to="#" style={{ color: 'var(--nav-text-muted)', fontSize: 14 }}>Security</Link></Box>
              <Box mb={8}><Link to="#" style={{ color: 'var(--nav-text-muted)', fontSize: 14 }}>Pricing</Link></Box>
            </Box>
            <Box>
              <Text fw={700} mb={16} style={{ color: 'var(--nav-text)' }}>Resources</Text>
              <Box mb={8}><Link to="#" style={{ color: 'var(--nav-text-muted)', fontSize: 14 }}>Documentation</Link></Box>
              <Box mb={8}><Link to="#" style={{ color: 'var(--nav-text-muted)', fontSize: 14 }}>API Reference</Link></Box>
              <Box mb={8}><Link to="#" style={{ color: 'var(--nav-text-muted)', fontSize: 14 }}>Guides</Link></Box>
            </Box>
            <Box>
              <Text fw={700} mb={16} style={{ color: 'var(--nav-text)' }}>Company</Text>
              <Box mb={8}><Link to="#" style={{ color: 'var(--nav-text-muted)', fontSize: 14 }}>About</Link></Box>
              <Box mb={8}><Link to="#" style={{ color: 'var(--nav-text-muted)', fontSize: 14 }}>Blog</Link></Box>
              <Box mb={8}><Link to="#" style={{ color: 'var(--nav-text-muted)', fontSize: 14 }}>Careers</Link></Box>
            </Box>
            <Box>
              <Text fw={700} mb={16} style={{ color: 'var(--nav-text)' }}>Legal</Text>
              <Box mb={8}><Link to="#" style={{ color: 'var(--nav-text-muted)', fontSize: 14 }}>Privacy Policy</Link></Box>
              <Box mb={8}><Link to="#" style={{ color: 'var(--nav-text-muted)', fontSize: 14 }}>Terms of Service</Link></Box>
              <Box mb={8}><Link to="#" style={{ color: 'var(--nav-text-muted)', fontSize: 14 }}>Cookie Policy</Link></Box>
            </Box>
          </SimpleGrid>
          <Divider color="var(--nav-border)" mb={24} />
          <Text size="xs" style={{ color: 'var(--nav-text-muted)' }}>
            © 2026 DogFood Platform. All rights reserved.
          </Text>
        </MantineContainer>
      </Box>

    </Box>
  );
}
