import { BrowserRouter, Routes, Route, Link, useLocation } from 'react-router-dom';
import { Box, Flex, Title, Text, Button, Group, SimpleGrid, Card } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { IconCode, IconGavel, IconTrophy, IconArrowRight } from '@tabler/icons-react';

import { useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { GlobalNav } from './components/shared/GlobalNav';
import { SideNav } from './components/shared/SideNav';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { Dashboard } from './pages/dashboard/Dashboard';
import { JudgeDashboard } from './pages/judge/JudgeDashboard';
import { SubmissionForm } from './pages/SubmissionForm';
import { Gallery } from './pages/gallery/Gallery';

function AppShell() {
  const [opened, { toggle }] = useDisclosure();
  const location = useLocation();

  const publicRoutes = ['/', '/login', '/register', '/gallery'];
  const isPublicPage = publicRoutes.includes(location.pathname);

  return (
    <Box style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <GlobalNav isPublicPage={isPublicPage} opened={opened} toggle={toggle} />
      
      {!isPublicPage && <SideNav opened={opened} />}

      <Box 
        component="main"
        className="app-main" 
        style={{ 
          flex: 1, 
          marginTop: 56,
          marginLeft: isPublicPage ? 0 : 280,
          transition: 'margin-left 150ms'
        }}
      >
        <Box style={{ maxWidth: 1280, margin: '0 auto', padding: '24px' }}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/gallery" element={<Gallery />} />
            <Route path="/submit" element={<ProtectedRoute><SubmissionForm /></ProtectedRoute>} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
            <Route path="/judge" element={<ProtectedRoute><JudgeDashboard /></ProtectedRoute>} />
          </Routes>
        </Box>
      </Box>
    </Box>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AppShell />
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
            color: 'var(--text)'
          }}
        >
          Built for those who ship.
        </Title>

        <Text size="xl" mt="xl" maw={700} style={{ color: 'var(--text-muted)', lineHeight: 1.6 }}>
          A masterclass platform enforcing role isolation, rigorous Bayesian judging shrinkage, and real-time observability.
        </Text>

        <Group mt={40}>
          <Button size="lg" component={Link} to="/register" rightSection={<IconArrowRight size="1.2rem" />} variant="filled">
            Start Building
          </Button>
          <Button size="lg" variant="default" component={Link} to="/gallery">
            Explore Gallery
          </Button>
        </Group>
      </Flex>

      <SimpleGrid cols={{ base: 1, md: 3 }} spacing="xl" mt={20}>
        <Card>
          <IconCode size={24} stroke={1.5} color="var(--text)" style={{ marginBottom: 16 }} />
          <Title order={3} mb="md">Seamless Submissions</Title>
          <Text style={{ color: 'var(--text-muted)', lineHeight: 1.6 }}>
            Drop-in Markdown support, automated tech stack tagging, and instant repository integrations for your team's code.
          </Text>
        </Card>

        <Card>
          <IconGavel size={24} stroke={1.5} color="var(--text)" style={{ marginBottom: 16 }} />
          <Title order={3} mb="md">Bayesian Judging</Title>
          <Text style={{ color: 'var(--text-muted)', lineHeight: 1.6 }}>
            Eliminate judge bias instantly. Our engine normalizes scores across tracks using sophisticated statistical shrinkage.
          </Text>
        </Card>

        <Card>
          <IconTrophy size={24} stroke={1.5} color="var(--text)" style={{ marginBottom: 16 }} />
          <Title order={3} mb="md">Real-time Gallery</Title>
          <Text style={{ color: 'var(--text-muted)', lineHeight: 1.6 }}>
            Watch the leaderboard evolve live. Participants and the public can vote and view projects the moment they ship.
          </Text>
        </Card>
      </SimpleGrid>
    </Box>
  );
}
