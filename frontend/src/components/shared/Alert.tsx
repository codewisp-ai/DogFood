import { Flex, Text, ActionIcon } from '@mantine/core';
import { IconAlertCircle, IconCheck, IconInfoCircle, IconX } from '@tabler/icons-react';

interface AlertProps {
  type?: 'info' | 'success' | 'warning' | 'danger';
  message: string;
  onDismiss?: () => void;
  action?: React.ReactNode;
}

export function Alert({ type = 'info', message, onDismiss, action }: AlertProps) {
  let bg = 'var(--info-soft)';
  let borderColor = 'var(--info)';
  let Icon = IconInfoCircle;

  switch (type) {
    case 'success': bg = 'var(--success-soft)'; borderColor = 'var(--success)'; Icon = IconCheck; break;
    case 'warning': bg = 'var(--warning-soft)'; borderColor = 'var(--warning)'; Icon = IconAlertCircle; break;
    case 'danger': bg = 'var(--danger-soft)'; borderColor = 'var(--danger)'; Icon = IconX; break;
  }

  return (
    <Flex 
      align="center" 
      justify="space-between" 
      p={16} 
      style={{ 
        backgroundColor: bg, 
        border: `1px solid ${borderColor}`,
        borderRadius: 'var(--radius-lg)',
        marginBottom: 20
      }}
    >
      <Flex align="center" gap={12}>
        <Icon size={20} color={borderColor} stroke={1.5} />
        <Text size="sm" style={{ color: 'var(--text)' }}>{message}</Text>
        {action}
      </Flex>
      {onDismiss && (
        <ActionIcon variant="subtle" onClick={onDismiss} color="gray">
          <IconX size={16} />
        </ActionIcon>
      )}
    </Flex>
  );
}
