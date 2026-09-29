import { Box, Group, Title, Text, Breadcrumbs, Anchor } from '@mantine/core';
import { ReactNode } from 'react';
import { Link } from 'react-router-dom';

interface BreadcrumbItem {
  label: string;
  href?: string;
}

interface PageHeaderProps {
  title: string;
  description?: string;
  breadcrumbs?: BreadcrumbItem[];
  actions?: ReactNode;
}

export function PageHeader({ title, description, breadcrumbs, actions }: PageHeaderProps) {
  return (
    <Box mb={24}>
      {breadcrumbs && breadcrumbs.length > 0 && (
        <Breadcrumbs separator=">" mb={12} style={{ fontSize: 14 }}>
          {breadcrumbs.map((item, index) => (
            item.href ? (
              <Anchor component={Link} to={item.href} key={index} style={{ color: 'var(--link)' }}>
                {item.label}
              </Anchor>
            ) : (
              <Text key={index} style={{ color: 'var(--text)' }}>
                {item.label}
              </Text>
            )
          ))}
        </Breadcrumbs>
      )}
      <Group justify="space-between" align="flex-start">
        <Box>
          <Title order={1} style={{ marginBottom: 4 }}>{title}</Title>
          {description && <Text size="sm" style={{ color: 'var(--text-muted)' }}>{description}</Text>}
        </Box>
        {actions && <Group gap={12}>{actions}</Group>}
      </Group>
    </Box>
  );
}
