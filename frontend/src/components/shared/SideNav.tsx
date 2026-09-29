import { Box, Text } from '@mantine/core';
import { Link, useLocation } from 'react-router-dom';
import { IconUpload, IconLayoutDashboard, IconGavel, IconSettings } from '@tabler/icons-react';
import { ReactNode } from 'react';
import { useAuth } from '../../context/AuthContext';

interface NavItemProps {
  label: string;
  to: string;
  icon?: ReactNode;
  active: boolean;
}

function NavItem({ label, to, icon, active }: NavItemProps) {
  return (
    <Box
      component={Link}
      to={to}
      style={{
        display: 'flex',
        alignItems: 'center',
        height: 36,
        padding: '0 16px',
        textDecoration: 'none',
        fontSize: 14,
        fontWeight: active ? 700 : 400,
        color: active ? 'var(--text)' : 'var(--text-muted)',
        backgroundColor: active ? 'var(--accent-soft)' : 'transparent',
        borderLeft: active ? '3px solid var(--accent)' : '3px solid transparent',
        transition: 'background-color 100ms',
      }}
      onMouseEnter={(e) => {
        if (!active) e.currentTarget.style.backgroundColor = 'var(--surface-subtle)';
      }}
      onMouseLeave={(e) => {
        if (!active) e.currentTarget.style.backgroundColor = 'transparent';
      }}
    >
      {icon && <Box mr={12} style={{ display: 'flex', alignItems: 'center' }}>{icon}</Box>}
      {label}
    </Box>
  );
}

export function SideNav({ opened }: { opened: boolean }) {
  const location = useLocation();
  const { getUserRole } = useAuth();
  const role = getUserRole();

  return (
    <Box
      style={{
        width: 280,
        backgroundColor: 'var(--surface)',
        borderRight: '1px solid var(--border)',
        position: 'fixed',
        top: 56,
        bottom: 0,
        left: 0,
        zIndex: 90,
        overflowY: 'auto'
      }}
      className="side-nav-responsive" data-opened={opened}
    >
      {/* ORGANIZER */}
      {role === 'ORGANIZER' && (
        <>
          <Box p={20}>
            <Text size="md" fw={700} style={{ color: 'var(--text)' }}>Organizer</Text>
          </Box>
          <Box>
            <NavItem label="Event Dashboard" to="/admin" icon={<IconSettings size={16} />} active={location.pathname === '/admin'} />
          </Box>
        </>
      )}

      {/* JUDGE */}
      {role === 'JUDGE' && (
        <>
          <Box p={20}>
            <Text size="md" fw={700} style={{ color: 'var(--text)' }}>Judge</Text>
          </Box>
          <Box>
            <NavItem label="Assigned Projects" to="/judge" icon={<IconGavel size={16} />} active={location.pathname === '/judge'} />
          </Box>
        </>
      )}

      {/* PARTICIPANT (default) */}
      {(role === 'PARTICIPANT' || role === null) && (
        <>
          <Box p={20}>
            <Text size="md" fw={700} style={{ color: 'var(--text)' }}>Participant</Text>
          </Box>
          <Box>
            <NavItem label="Dashboard" to="/dashboard" icon={<IconLayoutDashboard size={16} />} active={location.pathname === '/dashboard'} />
            <NavItem label="Submit Project" to="/submit" icon={<IconUpload size={16} />} active={location.pathname === '/submit'} />
          </Box>
        </>
      )}
    </Box>
  );
}

