import { createTheme, rem, virtualColor } from '@mantine/core';

export const theme = createTheme({
  primaryColor: 'hackathon',
  colors: {
    hackathon: [
      '#f3f0ff', '#e5dbff', '#d0bfff', '#b197fc', '#9775fa',
      '#845ef7', '#7950f2', '#7048e8', '#6741d9', '#5f3dc4'
    ],
    dark: [
      '#C1C2C5', '#A6A7AB', '#909296', '#5C5F66', '#373A40',
      '#2C2E33', '#25262B', '#1A1B1E', '#141517', '#0A0A0A'
    ]
  },
  primaryShade: { light: 6, dark: 5 },
  defaultRadius: 'lg',
  fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
  headings: {
    fontFamily: 'Plus Jakarta Sans, Inter, system-ui, sans-serif',
    fontWeight: '800',
    sizes: {
      h1: { fontSize: rem(64), lineHeight: '1.1' },
      h2: { fontSize: rem(48), lineHeight: '1.2' },
      h3: { fontSize: rem(32), lineHeight: '1.3' },
    }
  },
  components: {
    Button: {
      defaultProps: {
        radius: 'xl',
        fw: 700,
      },
      styles: {
        root: {
          transition: 'all 0.2s ease',
          '&:hover': {
            transform: 'scale(1.05)',
            boxShadow: '0 0 20px rgba(132, 94, 247, 0.4)',
          }
        }
      }
    },
    Card: {
      defaultProps: {
        radius: 'xl',
        withBorder: true,
      },
      styles: (theme) => ({
        root: {
          backgroundColor: 'var(--mantine-color-body)',
          borderColor: 'var(--mantine-color-default-border)',
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          backdropFilter: 'blur(10px)',
          '&:hover': {
            transform: 'translateY(-5px) scale(1.01)',
            boxShadow: '0 30px 60px rgba(0,0,0,0.12)',
            borderColor: theme.colors.hackathon[5],
          },
        },
      }),
    },
    AppShell: {
      styles: {
        main: {
          backgroundColor: 'var(--mantine-color-body)',
          minHeight: '100vh',
          overflowX: 'hidden',
          position: 'relative',
        },
        header: {
          borderBottom: '1px solid rgba(255,255,255,0.05)',
          backgroundColor: 'rgba(var(--mantine-color-body), 0.7)',
          backdropFilter: 'blur(12px) saturate(180%)',
          WebkitBackdropFilter: 'blur(12px) saturate(180%)',
        },
        navbar: {
          backgroundColor: 'rgba(var(--mantine-color-body), 0.7)',
          backdropFilter: 'blur(12px) saturate(180%)',
          borderRight: '1px solid rgba(255,255,255,0.05)',
        }
      }
    }
  },
});
