import { TextInput, PasswordInput, Button, Paper, Title, Container, Stack, Alert } from '@mantine/core';
import { useForm } from '@mantine/form';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { fetchWithAuth } from '../api';

export function Register() {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);

  const form = useForm({
    initialValues: {
      email: '',
      displayName: '',
      password: '',
    },
    validate: {
      email: (val) => (/^\S+@\S+$/.test(val) ? null : 'Invalid email'),
      password: (val) => (val.length < 6 ? 'Password must include at least 6 characters' : null),
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
    <Container size={420} my={40}>
      <Title ta="center" order={2}>
        Create an account
      </Title>

      <Paper withBorder shadow="md" p={30} mt={30} radius="md">
        <form onSubmit={form.onSubmit(handleSubmit)}>
          <Stack>
            {error && <Alert color="red">{error}</Alert>}
            <TextInput
              label="Display Name"
              placeholder="Jane Doe"
              required
              {...form.getInputProps('displayName')}
            />
            <TextInput
              label="Email"
              placeholder="you@dogfood.dev"
              required
              {...form.getInputProps('email')}
            />
            <PasswordInput
              label="Password"
              placeholder="Your password"
              required
              {...form.getInputProps('password')}
            />
            <Button type="submit" fullWidth mt="xl">
              Register
            </Button>
          </Stack>
        </form>
      </Paper>
    </Container>
  );
}
