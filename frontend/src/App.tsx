import { BrowserRouter, Routes, Route, Link, useLocation } from 'react-router-dom';
import { 
  AppShell, Burger, Group, Title, NavLink, Container, 
  Text, Button, Card, ActionIcon, useMantineColorScheme, 
  useMantineTheme, Box, SimpleGrid, Badge, ThemeIcon, Flex, Avatar
} from '@mantine/core';
import { useDisclosure, useWindowScroll } from '@mantine/hooks';
import { 
  IconSun, IconMoonStars, IconHome, IconPhoto, 
  IconUpload, IconDashboard, IconGavel, IconDog,
  IconArrowRight, IconCode, IconTrophy
} from '@tabler/icons-react';
import { motion } from 'framer-motion';
import { useAuth } from './context/AuthContext';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Dashboard } from './pages/dashboard/Dashboard';
import { Gallery } from './pages/gallery/Gallery';
import { SubmissionForm } from './pages/SubmissionForm';
import { JudgeDashboard } from './pages/judge/JudgeDashboard';

// Premium Background Orbs Effect
function BackgroundGlow() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';
  
  if (!isDark) return null;
  
  return (
    <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, overflow: 'hidden', zIndex: 0, pointerEvents: 'none' }}>
      <div style={{ position: 'absolute', top: '-10%', left: '-10%', width: '50vw', height: '50vw', borderRadius: '50%', background: 'radial-gradient(circle, rgba(132,94,247,0.15) 0%, rgba(0,0,0,0) 70%)', filter: 'blur(60px)' }} />
      <div style={{ position: 'absolute', bottom: '-20%', right: '-10%', width: '60vw', height: '60vw', borderRadius: '50%', background: 'radial-gradient(circle, rgba(20,184,166,0.12) 0%, rgba(0,0,0,0) 70%)', filter: 'blur(80px)' }} />
    </div>
  );
}

function ThemeToggle() {
  const { colorScheme, toggleColorScheme } = useMantineColorScheme();
  const dark = colorScheme === 'dark';

  return (
    <ActionIcon
      variant="default"
      onClick={() => toggleColorScheme()}
      title="Toggle color scheme"
      size="xl"
      radius="xl"
      style={{ border: 'none', background: 'transparent' }}
    >
      <motion.div whileHover={{ rotate: 180 }} transition={{ duration: 0.3 }}>
        {dark ? <IconSun size="1.4rem" color="#ffd43b" /> : <IconMoonStars size="1.4rem" color="#4c6ef5" />}
      </motion.div>
    </ActionIcon>
  );
}

function App() {
  const [opened, { toggle }] = useDisclosure();
  const { user, logout } = useAuth();
  const theme = useMantineTheme();
  const [scroll] = useWindowScroll();

  return (
    <BrowserRouter>
      <BackgroundGlow />
      <AppShell
        header={{ height: 80 }}
        navbar={{ width: 280, breakpoint: 'sm', collapsed: { mobile: !opened } }}
        padding="xl"
      >
        <AppShell.Header style={{ 
          transition: 'all 0.3s ease',
          boxShadow: scroll.y > 20 ? '0 10px 30px rgba(0,0,0,0.1)' : 'none',
          borderBottom: scroll.y > 20 ? '1px solid var(--mantine-color-default-border)' : '1px solid transparent'
        }}>
          <Container size="xl" h="100%">
            <Group h="100%" justify="space-between">
              <Group>
                <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />
                <motion.div whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.9 }}>
                  <ThemeIcon variant="gradient" gradient={{ from: 'indigo', to: 'teal' }} size={42} radius="xl">
                    <IconDog size="1.8rem" stroke={1.5} />
                  </ThemeIcon>
                </motion.div>
                <Title order={3} style={{ fontFamily: 'Plus Jakarta Sans, sans-serif', fontWeight: 800, letterSpacing: '-0.5px' }}>
                  Dogfood
                </Title>
              </Group>
              <Group gap="md">
                <ThemeToggle />
                {user ? (
                  <Group gap="sm" visibleFrom="sm">
                    <Avatar radius="xl" color="indigo">{user.email?.charAt(0)?.toUpperCase() || 'U'}</Avatar>
                    <Button variant="subtle" color="red" radius="xl" onClick={logout}>Sign Out</Button>
                  </Group>
                ) : (
                  <>
                    <Button variant="subtle" component={Link} to="/login" radius="xl">Sign In</Button>
                    <Button variant="gradient" gradient={{ from: 'indigo', to: 'teal' }} component={Link} to="/register" radius="xl" px="xl">
                      Register Now
                    </Button>
                  </>
                )}
              </Group>
            </Group>
          </Container>
        </AppShell.Header>

        <AppShell.Navbar p="md" style={{ zIndex: 100 }}>
          <Box pt="lg" pb="xl">
            <Text size="xs" fw={800} c="dimmed" tt="uppercase" lts={2} mb="md" pl="sm">Navigation</Text>
            <NavLink 
              label="Home" 
              component={Link} to="/" 
              leftSection={<IconHome size="1.3rem" stroke={1.5} />} 
              variant="light"
              active={window.location.pathname === "/"}
              style={{ borderRadius: theme.radius.xl, marginBottom: 8, padding: '12px 16px', fontWeight: 600 }}
            />
            <NavLink 
              label="Gallery" 
              component={Link} to="/gallery" 
              leftSection={<IconPhoto size="1.3rem" stroke={1.5} />}
              style={{ borderRadius: theme.radius.xl, marginBottom: 8, padding: '12px 16px', fontWeight: 600 }}
            />
            {user && (
              <>
                <Text size="xs" fw={800} c="dimmed" tt="uppercase" lts={2} mt="xl" mb="md" pl="sm">Workspace</Text>
                <NavLink 
                  label="Submit Project" 
                  component={Link} to="/submit" 
                  leftSection={<IconUpload size="1.3rem" stroke={1.5} />}
                  style={{ borderRadius: theme.radius.xl, marginBottom: 8, padding: '12px 16px', fontWeight: 600 }}
                />
                <NavLink 
                  label="Organizer Dashboard" 
                  component={Link} to="/dashboard" 
                  leftSection={<IconDashboard size="1.3rem" stroke={1.5} />}
                  style={{ borderRadius: theme.radius.xl, marginBottom: 8, padding: '12px 16px', fontWeight: 600 }}
                />
                <NavLink 
                  label="Judge Portal" 
                  component={Link} to="/judge" 
                  leftSection={<IconGavel size="1.3rem" stroke={1.5} />}
                  style={{ borderRadius: theme.radius.xl, marginBottom: 8, padding: '12px 16px', fontWeight: 600 }}
                />
              </>
            )}
          </Box>
        </AppShell.Navbar>

        <AppShell.Main style={{ zIndex: 1 }}>
          <Container size="xl" pt={{ base: 'md', md: 'xl' }}>
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
    </BrowserRouter>
  );
}

export default App;

// Premium Animated Home Page (Bento Box Style)
function Home() {
  const { colorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  return (
    <Box pb={100}>
      <Flex 
        direction="column" 
        align="center" 
        ta="center" 
        pt={{ base: 60, md: 100 }} 
        pb={{ base: 60, md: 80 }}
      >
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5 }}>
          <Badge 
            variant="outline" 
            color="indigo" 
            size="lg" 
            radius="xl" 
            mb="xl"
            px="xl"
            py="md"
            style={{ border: '1px solid rgba(132, 94, 247, 0.3)', backdropFilter: 'blur(10px)', letterSpacing: '1px', fontWeight: 700 }}
          >
            THE PREMIER HACKATHON ENGINE
          </Badge>
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.1 }}>
          <Title 
            order={1} 
            style={{ 
              fontSize: 'clamp(3rem, 6vw, 5rem)', 
              lineHeight: 1.1,
              letterSpacing: '-2px',
              fontFamily: 'Plus Jakarta Sans, sans-serif'
            }}
          >
            Built for those who{' '}
            <span style={{ 
              background: 'linear-gradient(45deg, #7950f2, #14b8a6)', 
              WebkitBackgroundClip: 'text', 
              WebkitTextFillColor: 'transparent' 
            }}>
              ship.
            </span>
          </Title>
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.2 }}>
          <Text size="xl" mt="xl" mx="auto" maw={700} c="dimmed" style={{ lineHeight: 1.6 }}>
            A masterclass platform enforcing role isolation, rigorous Bayesian judging shrinkage, and real-time observability.
          </Text>
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.3 }}>
          <Group justify="center" mt={40}>
            <Button size="xl" radius="xl" variant="gradient" gradient={{ from: 'indigo', to: 'teal' }} component={Link} to="/register" rightSection={<IconArrowRight size="1.2rem" />}>
              Start Building
            </Button>
            <Button size="xl" radius="xl" variant="default" component={Link} to="/gallery" style={{ background: isDark ? 'rgba(255,255,255,0.05)' : 'white' }}>
              Explore Gallery
            </Button>
          </Group>
        </motion.div>
      </Flex>

      {/* Bento Grid */}
      <SimpleGrid cols={{ base: 1, md: 3 }} spacing="xl" mt={20}>
        <motion.div initial={{ opacity: 0, scale: 0.95 }} whileInView={{ opacity: 1, scale: 1 }} viewport={{ once: true }} transition={{ duration: 0.5, delay: 0.1 }}>
          <Card h="100%" style={{ background: isDark ? 'linear-gradient(135deg, rgba(37,38,43,0.8), rgba(20,21,23,0.8))' : 'linear-gradient(135deg, #ffffff, #f8f9fa)' }}>
            <ThemeIcon size={60} radius="xl" variant="light" color="indigo" mb="xl">
              <IconCode size="2rem" stroke={1.5} />
            </ThemeIcon>
            <Title order={3} mb="md" style={{ letterSpacing: '-0.5px' }}>Seamless Submissions</Title>
            <Text c="dimmed" size="lg" style={{ lineHeight: 1.6 }}>
              Drop-in Markdown support, automated tech stack tagging, and instant repository integrations for your team's code.
            </Text>
          </Card>
        </motion.div>

        <motion.div initial={{ opacity: 0, scale: 0.95 }} whileInView={{ opacity: 1, scale: 1 }} viewport={{ once: true }} transition={{ duration: 0.5, delay: 0.2 }}>
          <Card h="100%" style={{ background: isDark ? 'linear-gradient(135deg, rgba(37,38,43,0.8), rgba(20,21,23,0.8))' : 'linear-gradient(135deg, #ffffff, #f8f9fa)' }}>
            <ThemeIcon size={60} radius="xl" variant="light" color="teal" mb="xl">
              <IconGavel size="2rem" stroke={1.5} />
            </ThemeIcon>
            <Title order={3} mb="md" style={{ letterSpacing: '-0.5px' }}>Bayesian Judging</Title>
            <Text c="dimmed" size="lg" style={{ lineHeight: 1.6 }}>
              Eliminate judge bias instantly. Our engine normalizes scores across tracks using sophisticated statistical shrinkage.
            </Text>
          </Card>
        </motion.div>

        <motion.div initial={{ opacity: 0, scale: 0.95 }} whileInView={{ opacity: 1, scale: 1 }} viewport={{ once: true }} transition={{ duration: 0.5, delay: 0.3 }}>
          <Card h="100%" style={{ background: isDark ? 'linear-gradient(135deg, rgba(37,38,43,0.8), rgba(20,21,23,0.8))' : 'linear-gradient(135deg, #ffffff, #f8f9fa)' }}>
            <ThemeIcon size={60} radius="xl" variant="light" color="pink" mb="xl">
              <IconTrophy size="2rem" stroke={1.5} />
            </ThemeIcon>
            <Title order={3} mb="md" style={{ letterSpacing: '-0.5px' }}>Real-time Gallery</Title>
            <Text c="dimmed" size="lg" style={{ lineHeight: 1.6 }}>
              Watch the leaderboard evolve live. Participants and the public can vote and view projects the moment they ship.
            </Text>
          </Card>
        </motion.div>
      </SimpleGrid>
    </Box>
  );
}
