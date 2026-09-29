import { TextInput, PasswordInput, Button, Title, Container as MantineContainer, Stack, Box, Text } from '@mantine/core';
import { useForm } from '@mantine/form';
import { useNavigate, Link } from 'react-router-dom';
import { useState } from 'react';
import { fetchWithAuth } from '../api';
import { Alert } from '../components/shared/Alert';

export function Register() {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);

  const form = useForm({
    initialValues: { email: '', displayName: '', password: '' },
    validate: {
      email: (val) => (/^\S+@\S+$/.test(val) ? null : 'Invalid email'),
      password: (val) => (val.length < 6 ? 'Password must be at least 6 chars' : null),
      displayName: (val) => (val.trim().length > 0 ? null : 'Name is required')
    },
  });

  const handleSubmit = async (values: typeof form.values) => {
    setError(null);
    try {
      await fetchWithAuth('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(values),
      });
      navigate('/login');
    } catch (err: any) {
      setError(err.message || 'Registration failed');
    }
  };

  return (
    <Box style={{ display: 'flex', justifyContent: 'center', paddingTop: 64 }}>
      <MantineContainer size={400} w="100%">
        <Box className="design-container" p={32} style={{ textAlign: 'center' }}>
          <Title order={2} mb={24}>Create an account</Title>
          
          <form onSubmit={form.onSubmit(handleSubmit)} style={{ textAlign: 'left' }}>
            <Stack gap={20}>
              {error && <Alert type="danger" message={error} />}
              <TextInput
                label="Full name"
                placeholder="Jane Doe"
                required
                {...form.getInputProps('displayName')}
              />
              <TextInput
                label="Email address"
                placeholder="you@example.com"
                required
                {...form.getInputProps('email')}
              />
              <PasswordInput
                label="Password"
                placeholder="Create a password"
                required
                {...form.getInputProps('password')}
              />
              <Button type="submit" fullWidth mt={8} style={{ height: 32 }}>
                Register
              </Button>
            </Stack>
          </form>

          <Text size="sm" mt={24} style={{ color: 'var(--text-muted)' }}>
            Already have an account? <Link to="/login" style={{ color: 'var(--link)', textDecoration: 'none' }}>Sign in</Link>
          </Text>
        </Box>
      </MantineContainer>
    </Box>
  );
}
