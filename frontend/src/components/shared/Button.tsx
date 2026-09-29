import { Button as MantineButton, ButtonProps as MantineButtonProps } from '@mantine/core';

interface ButtonProps extends MantineButtonProps {
  variant?: 'primary' | 'secondary' | 'ghost';
  onClick?: () => void;
  children: React.ReactNode;
}

export function Button({ variant = 'primary', size = 'md', style, ...props }: ButtonProps) {
  const heights = { sm: '32px', md: '36px', lg: '44px' };
  
  const baseStyle: React.CSSProperties = {
    height: heights[size as keyof typeof heights] || '36px',
    padding: '0 16px',
    borderRadius: 'var(--radius-md)',
    fontSize: '14px',
    fontWeight: 500,
    transition: 'background-color 120ms, border-color 120ms, color 120ms',
    ...style,
  };

  if (variant === 'primary') {
    return (
      <MantineButton 
        {...props} 
        size={size}
        style={{
          ...baseStyle,
          backgroundColor: 'var(--accent)',
          color: 'var(--text-on-accent)',
          border: 'none',
        }}
      />
    );
  }

  if (variant === 'secondary') {
    return (
      <MantineButton 
        {...props}
        size={size}
        style={{
          ...baseStyle,
          backgroundColor: 'var(--surface)',
          color: 'var(--text)',
          border: '1px solid var(--border-strong)',
        }}
      />
    );
  }

  if (variant === 'ghost') {
    return (
      <MantineButton 
        {...props}
        size={size}
        style={{
          ...baseStyle,
          backgroundColor: 'transparent',
          color: 'var(--text-muted)',
          border: 'none',
        }}
      />
    );
  }

  return <MantineButton {...props} style={baseStyle} size={size} />;
}
