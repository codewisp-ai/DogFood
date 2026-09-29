import { Box } from '@mantine/core';

export function Tag({ children }: { children: React.ReactNode }) {
  return (
    <Box className="design-tag" style={{ display: 'inline-flex', alignItems: 'center' }}>
      {children}
    </Box>
  );
}
