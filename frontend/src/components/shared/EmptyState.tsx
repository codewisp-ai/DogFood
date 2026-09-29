import { Flex, Title, Text, Button } from '@mantine/core';
import { ReactNode } from 'react';

interface EmptyStateProps {
  icon: ReactNode;
  title: string;
  description: string;
  actionLabel?: string;
  onAction?: () => void;
}

export function EmptyState({ icon, title, description, actionLabel, onAction }: EmptyStateProps) {
  return (
    <Flex direction="column" align="center" justify="center" p={40} style={{ textAlign: 'center' }}>
      <Flex 
        align="center" 
        justify="center" 
        style={{ 
          width: 64, 
          height: 64, 
          borderRadius: '50%', 
          backgroundColor: 'var(--surface-subtle)',
          color: 'var(--text-muted)',
          marginBottom: 24
        }}
      >
        {icon}
      </Flex>
      <Title order={3} style={{ marginBottom: 8, color: 'var(--text)' }}>
        {title}
      </Title>
      <Text size="sm" style={{ color: 'var(--text-muted)', marginBottom: 24, maxWidth: 300 }}>
        {description}
      </Text>
      {actionLabel && onAction && (
        <Button onClick={onAction} variant="filled">
          {actionLabel}
        </Button>
      )}
    </Flex>
  );
}
