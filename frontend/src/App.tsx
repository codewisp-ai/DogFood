import { BrowserRouter, Routes, Route, Link, useLocation } from 'react-router-dom';
import { AppShell, Burger, Group, Title, Button, Text, ActionIcon, useMantineColorScheme, Container, Flex, Badge, SimpleGrid, Card, ThemeIcon, Box } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { IconSun, IconMoon, IconArrowRight, IconCode, IconGavel, IconTrophy, IconDog, IconHome, IconPhoto, IconUpload, IconDashboard } from '@tabler/icons-react';
import { useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { Login } from './pages/auth/Login';
import { Register } from './pages/auth/Register';
import { Dashboard } from './pages/dashboard/Dashboard';
import { JudgeDashboard } from './pages/dashboard/JudgeDashboard';
import { SubmissionForm } from './pages/dashboard/SubmissionForm';
import { Gallery } from './pages/Gallery';

function Shell() {
  const [opened, { toggle }] = useDisclosure();
  const { colorScheme, toggleColorScheme } = useMantineColorScheme();
  const { user, logout } = useAuth();
  const location = useLocation();

  const isPublicPage = ['/', '/login', '/register', '/gallery'].includes(location.pathname);

  return (
    <AppShell
      header={{ height: 60 }}
      navbar={isPublicPage ? undefined : {
        width: 250,
        breakpoint: 'sm',
        collapsed: { mobile: !opened },
      }}
      padding="md"
    >
      <AppShell.Header>
        <Group h="100%" px="md" justify="space-between">
          <Group>
            {!isPublicPage && <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />}
            <IconDog size={32} color="var(--accent)" />
            <Title order={3} component={Link} to="/" style={{ color: 'var(--text)', textDecoration: 'none', letterSpacing: '-0.5px' }}>
              DogFood
            </Title>
          </Group>

          <Group>
            <ActionIcon variant="subtle" onClick={() => toggleColorScheme()} size="lg" radius="xl" aria-label="Toggle color scheme" style={{ color: 'var(--text)' }}>
              {colorScheme === 'dark' ? <IconSun size="1.2rem" /> : <IconMoon size="1.2rem" />}
            </ActionIcon>
            {user ? (
              <Button variant="subtle" onClick={logout} style={{ color: 'var(--text-muted)' }}>Logout</Button>
            ) : (
              <>
                <Button variant="subtle" component={Link} to="/login" style={{ color: 'var(--text-muted)' }}>Sign In</Button>
                <Button variant="filled" component={Link} to="/register" style={{ backgroundColor: 'var(--accent)', color: 'var(--text-on-accent)' }}>Get Started</Button>
              </>
            )}
          </Group>
        </Group>
      </AppShell.Header>

      {!isPublicPage && (
        <AppShell.Navbar p="md">
          <Box component="nav">
            <Button component={Link} to="/" variant={location.pathname === '/' ? 'filled' : 'subtle'} leftSection={<IconHome size={18} />} fullWidth justify="flex-start" mb="sm" style={{ backgroundColor: location.pathname === '/' ? 'var(--accent-soft)' : 'transparent', color: location.pathname === '/' ? 'var(--accent)' : 'var(--text)' }}>
              Home
            </Button>
            <Button component={Link} to="/gallery" variant={location.pathname === '/gallery' ? 'filled' : 'subtle'} leftSection={<IconPhoto size={18} />} fullWidth justify="flex-start" mb="sm" style={{ backgroundColor: location.pathname === '/gallery' ? 'var(--accent-soft)' : 'transparent', color: location.pathname === '/gallery' ? 'var(--accent)' : 'var(--text)' }}>
              Gallery
            </Button>
            
            {user && (
              <>
                <Text size="xs" fw={700} c="dimmed" mt="xl" mb="sm" pl="sm">Workspace</Text>
                <Button component={Link} to="/submit" variant={location.pathname === '/submit' ? 'filled' : 'subtle'} leftSection={<IconUpload size={18} />} fullWidth justify="flex-start" mb="sm" style={{ backgroundColor: location.pathname === '/submit' ? 'var(--accent-soft)' : 'transparent', color: location.pathname === '/submit' ? 'var(--accent)' : 'var(--text)' }}>
                  Submit Project
                </Button>
                <Button component={Link} to="/dashboard" variant={location.pathname === '/dashboard' ? 'filled' : 'subtle'} leftSection={<IconDashboard size={18} />} fullWidth justify="flex-start" mb="sm" style={{ backgroundColor: location.pathname === '/dashboard' ? 'var(--accent-soft)' : 'transparent', color: location.pathname === '/dashboard' ? 'var(--accent)' : 'var(--text)' }}>
                  Dashboard
                </Button>
                <Button component={Link} to="/judge" variant={location.pathname === '/judge' ? 'filled' : 'subtle'} leftSection={<IconGavel size={18} />} fullWidth justify="flex-start" mb="sm" style={{ backgroundColor: location.pathname === '/judge' ? 'var(--accent-soft)' : 'transparent', color: location.pathname === '/judge' ? 'var(--accent)' : 'var(--text)' }}>
                  Judge Portal
                </Button>
              </>
            )}
          </Box>
        </AppShell.Navbar>
      )}

      <AppShell.Main>
        <Container size="xl" pt={isPublicPage ? 0 : 'md'}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/gallery" element={<Gallery />} />
            <Route path="/submit" element={<ProtectedRoute><SubmissionForm /></ProtectedRoute>} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
            <Route path="/judge" element={<ProtectedRoute><JudgeDashboard /></ProtectedRoute>} />
          </Routes>
        </Container>
      </AppShell.Main>
    </AppShell>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Shell />
    </BrowserRouter>
  );
}

export default App;

function Home() {
  return (
    <Box pb={100}>
      <Flex 
        direction="column" align="flex-start" ta="left" 
        pt={{ base: 60, md: 100 }} 
        pb={{ base: 60, md: 80 }}
      >
        

        <Title 
          order={1} 
          style={{ 
            fontSize: 'clamp(3rem, 6vw, 5rem)', 
            lineHeight: 1.1,
            letterSpacing: '-2px',
            color: 'var(--text)'
          }}
        >
          Built for those who{' '}
          ship.
        </Title>

        <Text size="xl" mt="xl" mx="auto" maw={700} style={{ color: 'var(--text-muted)', lineHeight: 1.6 }}>
          A masterclass platform enforcing role isolation, rigorous Bayesian judging shrinkage, and real-time observability.
        </Text>

        <Group justify="center" mt={40}>
          <Button size="xl" component={Link} to="/register" rightSection={<IconArrowRight size="1.2rem" />} style={{ backgroundColor: 'var(--accent)', color: 'var(--text-on-accent)' }}>
            Start Building
          </Button>
          <Button size="xl" variant="default" component={Link} to="/gallery" style={{ backgroundColor: 'var(--surface)', color: 'var(--text)', borderColor: 'var(--border-strong)' }}>
            Explore Gallery
          </Button>
        </Group>
      </Flex>

      <SimpleGrid cols={{ base: 1, md: 3 }} spacing="xl" mt={20}>
        <Card style={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)' }}>
          <IconCode size={24} stroke={1.5} color="var(--text)" style={{ marginBottom: 16 }} />
          <Title order={3} mb="md" style={{ color: 'var(--text)' }}>Seamless Submissions</Title>
          <Text style={{ color: 'var(--text-muted)', lineHeight: 1.6 }}>
            Drop-in Markdown support, automated tech stack tagging, and instant repository integrations for your team's code.
          </Text>
        </Card>

        <Card style={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)' }}>
          <IconGavel size={24} stroke={1.5} color="var(--text)" style={{ marginBottom: 16 }} />
          <Title order={3} mb="md" style={{ color: 'var(--text)' }}>Bayesian Judging</Title>
          <Text style={{ color: 'var(--text-muted)', lineHeight: 1.6 }}>
            Eliminate judge bias instantly. Our engine normalizes scores across tracks using sophisticated statistical shrinkage.
          </Text>
        </Card>

        <Card style={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)' }}>
          <IconTrophy size={24} stroke={1.5} color="var(--text)" style={{ marginBottom: 16 }} />
          <Title order={3} mb="md" style={{ color: 'var(--text)' }}>Real-time Gallery</Title>
          <Text style={{ color: 'var(--text-muted)', lineHeight: 1.6 }}>
            Watch the leaderboard evolve live. Participants and the public can vote and view projects the moment they ship.
          </Text>
        </Card>
      </SimpleGrid>
    </Box>
  );
}
