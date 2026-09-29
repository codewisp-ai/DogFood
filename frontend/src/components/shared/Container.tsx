import { Box, Title, Group, Divider } from '@mantine/core';
import { ReactNode } from 'react';

interface ContainerProps {
  title?: ReactNode;
  count?: number;
  actions?: ReactNode;
  children: ReactNode;
  denseBody?: boolean;
}

export function Container({ title, count, actions, children, denseBody = false }: ContainerProps) {
  return (
    <Box className="design-container" mb={20}>
      {(title || actions) && (
        <>
          <Group justify="space-between" p={20}>
            <Title order={2}>
              {title} {count !== undefined && <span style={{ color: 'var(--text-muted)', fontWeight: 400 }}>({count})</span>}
            </Title>
            {actions && <Group gap={8}>{actions}</Group>}
          </Group>
          {denseBody && <Divider color="var(--border-subtle)" />}
        </>
      )}
      <Box p={denseBody ? 0 : 20}>{children}</Box>
    </Box>
  );
}
