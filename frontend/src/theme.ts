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
  defaultRadius: 'sm', // 4px
  
  fontFamily: '"IBM Plex Sans", Roboto, "Helvetica Neue", Arial, sans-serif',
  fontFamilyMonospace: '"IBM Plex Mono", ui-monospace, monospace',
  
  headings: {
    fontFamily: '"IBM Plex Sans", Roboto, "Helvetica Neue", Arial, sans-serif',
    fontWeight: '700',
    sizes: {
      h1: { fontSize: rem(28), lineHeight: '36px' },
      h2: { fontSize: rem(20), lineHeight: '24px' },
      h3: { fontSize: rem(18), lineHeight: '22px' },
      h4: { fontSize: rem(16), lineHeight: '20px' },
    }
  },
  
  components: {
    Button: Button.extend({
      defaultProps: {
        radius: 'sm',
      },
      classNames: {
        root: 'design-btn'
      }
    }),
    Card: Card.extend({
      defaultProps: {
        radius: 'md',
        withBorder: true,
      },
      classNames: {
        root: 'design-card'
      }
    }),
    Paper: Paper.extend({
      defaultProps: {
        radius: 'md',
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
          animationDuration: '1.5s'
        }
      }
    }),
    AppShell: {
      styles: {
        main: { backgroundColor: 'var(--bg)', minHeight: '100vh' },
        header: { backgroundColor: 'var(--nav-bg)', borderBottom: '1px solid var(--nav-border)' },
        navbar: { backgroundColor: 'var(--surface)', borderRight: '1px solid var(--border)' }
      }
    }
  },
});
