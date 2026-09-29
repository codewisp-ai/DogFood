import { Flex, Text } from '@mantine/core';
import { IconCircleDashed, IconInfoCircle, IconClock, IconAlertCircle, IconCheck, IconX, IconCircleFilled } from '@tabler/icons-react';

interface StatusIndicatorProps {
  status: 'Draft' | 'Submitted' | 'Under review' | 'Changes requested' | 'Approved' | 'Rejected' | 'Live';
}

export function StatusIndicator({ status }: StatusIndicatorProps) {
  let Icon = IconCircleDashed;
  let color = 'var(--text-muted)';
  
  switch (status) {
    case 'Draft': Icon = IconCircleDashed; color = 'var(--text-muted)'; break;
    case 'Submitted': Icon = IconInfoCircle; color = 'var(--info)'; break;
    case 'Under review': Icon = IconClock; color = 'var(--warning)'; break;
    case 'Changes requested': Icon = IconAlertCircle; color = 'var(--warning)'; break;
    case 'Approved': Icon = IconCheck; color = 'var(--success)'; break;
    case 'Rejected': Icon = IconX; color = 'var(--danger)'; break;
    case 'Live': Icon = IconCircleFilled; color = 'var(--success)'; break;
  }

  return (
    <Flex align="center" gap={8} style={{ color }}>
      <Icon size={16} stroke={1.5} />
      <Text size="sm" fw={400} style={{ color }}>{status}</Text>
    </Flex>
  );
}
