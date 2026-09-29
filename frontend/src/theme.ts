import { createTheme, rem } from '@mantine/core';

export const theme = createTheme({
  primaryColor: 'accent',
  colors: {
    accent: [
      'var(--accent-soft)',
      'var(--accent-soft)',
      'var(--accent-soft)',
      'var(--accent-soft)',
      'var(--accent-soft)',
      'var(--accent)',       // 5
      'var(--accent-hover)', // 6
      'var(--accent-hover)',
      'var(--accent-hover)',
      'var(--accent-hover)'
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
    Button: {
      defaultProps: {
        radius: 'md',
      },
      styles: {
        root: {
          transition: 'background-color 120ms cubic-bezier(0.2, 0, 0, 1), border-color 120ms cubic-bezier(0.2, 0, 0, 1), color 120ms cubic-bezier(0.2, 0, 0, 1)',
          fontSize: rem(14),
          fontWeight: 500,
        }
      }
    },
    Card: {
      defaultProps: {
        radius: 'lg',
        withBorder: true,
      },
      styles: {
        root: {
          backgroundColor: 'var(--surface)',
          borderColor: 'var(--border)',
          transition: 'border-color 120ms cubic-bezier(0.2, 0, 0, 1), box-shadow 120ms cubic-bezier(0.2, 0, 0, 1), transform 120ms cubic-bezier(0.2, 0, 0, 1)',
        },
      },
    },
    AppShell: {
      styles: {
        main: {
          backgroundColor: 'var(--bg)',
          minHeight: '100vh',
        },
        header: {
          backgroundColor: 'var(--bg)',
          borderBottom: '1px solid var(--border)',
        },
        navbar: {
          backgroundColor: 'var(--surface)',
          borderRight: '1px solid var(--border)',
        }
      }
    },
    Paper: {
      defaultProps: {
        radius: 'lg',
      },
      styles: {
        root: {
          backgroundColor: 'var(--surface)',
          borderColor: 'var(--border)',
        }
      }
    },
    Input: {
      styles: {
        input: {
          backgroundColor: 'var(--surface)',
          borderColor: 'var(--border-strong)',
          borderRadius: 'var(--radius-md)',
          height: rem(36),
          '&:focus': {
            borderColor: 'var(--accent)',
            boxShadow: '0 0 0 3px var(--focus-ring)',
          }
        }
      }
    }
  },
});
