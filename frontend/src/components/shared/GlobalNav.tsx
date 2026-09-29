import { Box, Group, Title, Button, ActionIcon, useMantineColorScheme, Burger, Flex, Text, Menu } from '@mantine/core';
import { IconDog, IconSun, IconMoon, IconSearch, IconBell, IconUser, IconLogout, IconLayoutDashboard } from '@tabler/icons-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface GlobalNavProps {
  isPublicPage: boolean;
  opened?: boolean;
  toggle?: () => void;
}

export function GlobalNav({ isPublicPage, opened, toggle }: GlobalNavProps) {
  const { colorScheme, toggleColorScheme } = useMantineColorScheme();
  const { user, logout, getUserRole } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const role = getUserRole();

  const getDashboardPath = () => {
    if (role === 'ORGANIZER') return '/admin';
    if (role === 'JUDGE') return '/judge';
    return '/dashboard';
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <Box 
      style={{ 
        height: 56, 
        backgroundColor: 'var(--nav-bg)', 
        borderBottom: '1px solid var(--nav-border)',
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        zIndex: 100
      }}
    >
      <Box style={{ maxWidth: 1280, margin: '0 auto', padding: '0 24px', height: '100%' }}>
        <Flex align="center" justify="space-between" style={{ height: '100%' }}>
          <Group gap={32} style={{ height: '100%' }}>
            <Group gap={12}>
              {!isPublicPage && toggle && (
                <Burger opened={opened} onClick={toggle} hiddenFrom="lg" size="sm" color="var(--nav-text)" />
              )}
              <Flex align="center" gap={8} component={Link} to="/" style={{ textDecoration: 'none' }}>
                <IconDog size={24} color="var(--nav-text)" />
                <Title order={4} style={{ color: 'var(--nav-text)', fontSize: 16 }}>Dogfood</Title>
              </Flex>
            </Group>

            {/* Nav Links */}
            <Group gap={4} style={{ height: '100%' }} visibleFrom="sm">
              <NavLink to="/gallery" label="Gallery" current={location.pathname} />
              <NavLink to="/leaderboard" label="Leaderboard" current={location.pathname} />
            </Group>
          </Group>

          <Group gap={16}>
            <ActionIcon variant="subtle" onClick={() => toggleColorScheme()} aria-label="Toggle color scheme">
              {colorScheme === 'dark' ? <IconSun size={20} color="var(--nav-text)" /> : <IconMoon size={20} color="var(--nav-text)" />}
            </ActionIcon>
            
            {!user ? (
              <Group gap={12}>
                <Button component={Link} to="/login" variant="subtle" style={{ color: 'var(--nav-text)', height: 32, padding: '0 12px' }}>
                  Sign in
                </Button>
                <Button component={Link} to="/register" variant="filled" style={{ height: 32, padding: '0 16px' }}>
                  Register
                </Button>
              </Group>
            ) : (
              <Group gap={16}>
                <IconSearch size={20} color="var(--nav-text)" style={{ cursor: 'pointer' }} />
                <IconBell size={20} color="var(--nav-text)" style={{ cursor: 'pointer' }} />
                <Menu shadow="md" width={200} position="bottom-end">
                  <Menu.Target>
                    <Flex align="center" gap={8} style={{ cursor: 'pointer' }}>
                      <Box style={{ width: 32, height: 32, borderRadius: '50%', backgroundColor: 'var(--accent)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <IconUser size={16} color="var(--text-on-accent)" />
                      </Box>
                      <Box visibleFrom="sm">
                        <Text size="sm" fw={700} style={{ color: 'var(--nav-text)', lineHeight: 1 }}>{user.email.split('@')[0]}</Text>
                        <Text size="xs" style={{ color: 'var(--nav-text-muted)' }}>{role ? role.charAt(0) + role.slice(1).toLowerCase() : 'User'}</Text>
                      </Box>
                    </Flex>
                  </Menu.Target>
                  <Menu.Dropdown>
                    <Menu.Label>{user.email}</Menu.Label>
                    <Menu.Item
                      leftSection={<IconLayoutDashboard size={14} />}
                      onClick={() => navigate(getDashboardPath())}
                    >
                      My Dashboard
                    </Menu.Item>
                    <Menu.Divider />
                    <Menu.Item
                      color="red"
                      leftSection={<IconLogout size={14} />}
                      onClick={handleLogout}
                    >
                      Sign Out
                    </Menu.Item>
                  </Menu.Dropdown>
                </Menu>
              </Group>
            )}
          </Group>
        </Flex>
      </Box>
    </Box>
  );
}

function NavLink({ to, label, current }: { to: string, label: string, current: string }) {
  const active = current === to;
  return (
    <Box
      component={Link}
      to={to}
      style={{
        display: 'flex',
        alignItems: 'center',
        height: '100%',
        padding: '0 12px',
        fontSize: 14,
        fontWeight: 400,
        color: active ? 'var(--nav-text)' : 'var(--nav-text-muted)',
        textDecoration: 'none',
        borderBottom: active ? '2px solid var(--accent)' : '2px solid transparent',
        transition: 'background-color 100ms',
      }}
      onMouseEnter={(e) => {
        if (!active) e.currentTarget.style.backgroundColor = 'var(--nav-hover)';
        if (!active) e.currentTarget.style.color = 'var(--nav-text)';
      }}
      onMouseLeave={(e) => {
        if (!active) e.currentTarget.style.backgroundColor = 'transparent';
        if (!active) e.currentTarget.style.color = 'var(--nav-text-muted)';
      }}
    >
      {label}
    </Box>
  );
}
