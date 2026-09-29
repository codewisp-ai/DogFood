import { Box, SimpleGrid, Text } from '@mantine/core';
import { ReactNode } from 'react';

interface KeyValuePair {
  label: string;
  value: ReactNode;
}

interface KeyValueProps {
  items: KeyValuePair[];
  cols?: number;
}

export function KeyValue({ items, cols = 4 }: KeyValueProps) {
  return (
    <SimpleGrid cols={{ base: 1, sm: 2, md: cols }} spacing={16}>
      {items.map((item, idx) => (
        <Box key={idx}>
          <Text size="sm" fw={700} style={{ color: 'var(--text-muted)', marginBottom: 4 }}>
            {item.label}
          </Text>
          <Box style={{ fontSize: 14, color: 'var(--text)' }}>
            {item.value}
          </Box>
        </Box>
      ))}
    </SimpleGrid>
  );
}
