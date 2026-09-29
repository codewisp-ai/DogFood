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
import { Leaderboard } from './pages/Leaderboard';
import { Home } from './pages/Home';

function AppShell() {
  const [opened, { toggle }] = useDisclosure();
  const location = useLocation();

  const publicRoutes = ['/', '/login', '/register', '/gallery', '/leaderboard'];
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
            <Route path="/leaderboard" element={<Leaderboard />} />
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

