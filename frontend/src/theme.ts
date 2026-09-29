import { createTheme, rem, Button, Card, Badge, TextInput, Table, Paper, Skeleton } from '@mantine/core';

export const theme = createTheme({
  primaryColor: 'accent',
  colors: {
    accent: [
      'var(--accent-soft)', 'var(--accent-soft)', 'var(--accent-soft)', 'var(--accent-soft)',
      'var(--accent-soft)', 'var(--accent)', 'var(--accent-hover)', 'var(--accent-hover)',
      'var(--accent-hover)', 'var(--accent-hover)'
    ],
  },
  primaryShade: { light: 5, dark: 5 },
  defaultRadius: 'md',
  
  fontFamily: 'Inter, system-ui, -apple-system, Segoe UI, sans-serif',
  fontFamilyMonospace: 'JetBrains Mono, ui-monospace, monospace',
  
  headings: {
    fontFamily: 'Inter, system-ui, -apple-system, Segoe UI, sans-serif',
    fontWeight: '600',
    sizes: {
      h1: { fontSize: rem(24), lineHeight: '32px' },
      h2: { fontSize: rem(18), lineHeight: '28px' },
      h3: { fontSize: rem(15), lineHeight: '24px' },
    }
  },
  
  components: {
    Button: Button.extend({
      defaultProps: {
        radius: 'md',
      },
      classNames: {
        root: 'design-btn'
      }
    }),
    Card: Card.extend({
      defaultProps: {
        radius: 'lg',
        withBorder: true,
      },
      classNames: {
        root: 'design-card'
      }
    }),
    Paper: Paper.extend({
      defaultProps: {
        radius: 'lg',
      },
      classNames: {
        root: 'design-paper'
      }
    }),
    TextInput: TextInput.extend({
      classNames: {
        input: 'design-input',
        label: 'design-label',
      }
    }),
    Table: Table.extend({
      classNames: {
        table: 'design-table',
        tr: 'design-tr',
        th: 'design-th',
        td: 'design-td',
      }
    }),
    Badge: Badge.extend({
      classNames: {
        root: 'design-badge'
      }
    }),
    Skeleton: Skeleton.extend({
      defaultProps: {
        animate: true,
      },
      styles: {
        root: {
          '--skeleton-color': 'var(--surface-subtle)',
          animationDuration: '1.4s'
        }
      }
    }),
    AppShell: {
      styles: {
        main: { backgroundColor: 'var(--bg)', minHeight: '100vh' },
        header: { backgroundColor: 'var(--bg)', borderBottom: '1px solid var(--border)' },
        navbar: { backgroundColor: 'var(--surface)', borderRight: '1px solid var(--border)' }
      }
    }
  },
});
