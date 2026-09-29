import { TextInput, PasswordInput, Button, Title, Container as MantineContainer, Stack, Box, Text } from '@mantine/core';
import { useForm } from '@mantine/form';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { useState } from 'react';
import { fetchWithAuth } from '../api';
import { Alert } from '../components/shared/Alert';

export function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);

  const form = useForm({
    initialValues: { email: '', password: '' },
    validate: {
      email: (val: string) => (/^\S+@\S+$/.test(val) ? null : 'Invalid email'),
      password: (val: string) => (val.length < 6 ? 'Password must be at least 6 chars' : null),
    },
  });

  const handleSubmit = async (values: typeof form.values) => {
    setError(null);
    try {
      const data = await fetchWithAuth('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(values),
      });
      login(data.accessToken);

      // Decode token directly (don't wait for React state) to get role for redirect
      const payload = JSON.parse(atob(data.accessToken.split('.')[1]));
      const roles: string[] = payload.eventRoles || [];
      if (roles.some((r: string) => r.endsWith(':ORGANIZER'))) {
        navigate('/admin');
      } else if (roles.some((r: string) => r.endsWith(':JUDGE'))) {
        navigate('/judge');
      } else {
        navigate('/dashboard');
      }
    } catch (err: any) {
      setError(err.message || 'Login failed');
    }
  };

  return (
    <Box style={{ display: 'flex', justifyContent: 'center', paddingTop: 64 }}>
      <MantineContainer size={400} w="100%">
        <Box className="design-container" p={32} style={{ textAlign: 'center' }}>
          <Title order={2} mb={24}>Sign in to Dogfood</Title>
          
          <form onSubmit={form.onSubmit(handleSubmit)} style={{ textAlign: 'left' }}>
            <Stack gap={20}>
              {error && <Alert type="danger" message={error} />}
              <TextInput
                label="Email address"
                placeholder="you@example.com"
                required
                {...form.getInputProps('email')}
              />
              <PasswordInput
                label="Password"
                placeholder="Enter your password"
                required
                {...form.getInputProps('password')}
              />
              <Button type="submit" fullWidth mt={8} style={{ height: 32 }}>
                Sign in
              </Button>
            </Stack>
          </form>

          <Text size="sm" mt={24} style={{ color: 'var(--text-muted)' }}>
            Don't have an account? <Link to="/register" style={{ color: 'var(--link)', textDecoration: 'none' }}>Register</Link>
          </Text>
        </Box>
      </MantineContainer>
    </Box>
  );
}
