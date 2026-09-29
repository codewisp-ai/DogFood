import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import { Box } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';

import { useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { GlobalNav } from './components/shared/GlobalNav';
import { SideNav } from './components/shared/SideNav';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { ParticipantDashboard } from './pages/dashboard/ParticipantDashboard';
import { AdminDashboard } from './pages/dashboard/AdminDashboard';
import { JudgeDashboard } from './pages/judge/JudgeDashboard';
import { ScoringForm } from './pages/judge/ScoringForm';
import { CoiDeclaration } from './pages/judge/CoiDeclaration';
import { SubmissionForm } from './pages/SubmissionForm';
import { Gallery } from './pages/gallery/Gallery';
import { GalleryWidget } from './pages/gallery/GalleryWidget';
import { Leaderboard } from './pages/Leaderboard';
import { Home } from './pages/Home';

function AppShell() {
  const [opened, { toggle }] = useDisclosure();
  const location = useLocation();
  const { user } = useAuth();

  // Pages that never show a sidebar (pure public pages)
  const noSidebarRoutes = ['/', '/login', '/register', '/widget/gallery'];
  const isNoSidebarPage = noSidebarRoutes.includes(location.pathname);

  // Show sidebar when logged in AND not on a pure public page
  const showSidebar = !!user && !isNoSidebarPage;

  return (
    <Box style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {location.pathname !== '/widget/gallery' && <GlobalNav isPublicPage={isNoSidebarPage} opened={opened} toggle={toggle} />}
      
      {showSidebar && <SideNav opened={opened} />}

      <Box 
        component="main"
        className="app-main" 
        style={{ 
          flex: 1, 
          marginTop: location.pathname === '/widget/gallery' ? 0 : 56,
          marginLeft: showSidebar ? 280 : 0,
          transition: 'margin-left 150ms'
        }}
      >
        <Box style={{ maxWidth: location.pathname === '/widget/gallery' ? '100%' : 1280, margin: '0 auto', padding: location.pathname === '/widget/gallery' ? '0' : '24px' }}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/gallery" element={<Gallery />} />
            <Route path="/widget/gallery" element={<GalleryWidget />} />
            <Route path="/leaderboard" element={<Leaderboard />} />
            <Route path="/submit" element={<ProtectedRoute><SubmissionForm /></ProtectedRoute>} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/dashboard" element={<ProtectedRoute><ParticipantDashboard /></ProtectedRoute>} />
            <Route path="/admin" element={<ProtectedRoute><AdminDashboard /></ProtectedRoute>} />
            <Route path="/judge" element={<ProtectedRoute><JudgeDashboard /></ProtectedRoute>} />
            <Route path="/judge/coi" element={<ProtectedRoute><CoiDeclaration /></ProtectedRoute>} />
            <Route path="/judge/score/:submissionId" element={<ProtectedRoute><ScoringForm /></ProtectedRoute>} />
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

