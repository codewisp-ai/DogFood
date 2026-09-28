import { TextInput, Textarea, Button, Paper, Title, Stack, Container, Text, Group, ThemeIcon, Alert, Box } from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconUpload, IconCheck, IconAlertCircle, IconBrandGithub, IconVideo } from '@tabler/icons-react';
import { useState } from 'react';
import { fetchWithAuth } from '../api';
import { motion } from 'framer-motion';

export function SubmissionForm() {
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const form = useForm({
    initialValues: {
      name: '',
      description: '',
      repoUrl: '',
      videoUrl: '',
      customAnswers: {}
    },
  });

  const handleSubmit = async (values: typeof form.values) => {
    setError(null);
    setSuccess(false);
    setIsSubmitting(true);
    try {
      await fetchWithAuth('/api/submissions', {
        method: 'POST',
        body: JSON.stringify(values),
      });
      setSuccess(true);
      form.reset();
    } catch (err: any) {
      setError(err.message || 'Submission failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Container size="md" my="xl" pb={100}>
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5 }}>
        <Paper 
          withBorder 
          shadow="md" 
          p={40} 
          radius="xl" 
          style={{ background: 'var(--mantine-color-body)', position: 'relative', overflow: 'hidden' }}
        >
          {/* Decorative glow */}
          <Box style={{ position: 'absolute', top: -50, right: -50, width: 200, height: 200, borderRadius: '50%', background: 'rgba(20,184,166,0.1)', filter: 'blur(40px)', pointerEvents: 'none' }} />
          
          <Group mb="xl">
            <ThemeIcon size={50} radius="md" variant="light" color="teal">
              <IconUpload size="1.8rem" stroke={1.5} />
            </ThemeIcon>
            <Box>
              <Title order={2} style={{ fontFamily: 'Plus Jakarta Sans', letterSpacing: '-0.5px' }}>Ship Your Project</Title>
              <Text c="dimmed">Provide the details for your hackathon submission below.</Text>
            </Box>
          </Group>

          <form onSubmit={form.onSubmit(handleSubmit)}>
            <Stack gap="lg">
              {error && (
                <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }}>
                  <Alert icon={<IconAlertCircle size="1.2rem" />} color="red" variant="light" radius="md">
                    {error}
                  </Alert>
                </motion.div>
              )}
              {success && (
                <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }}>
                  <Alert icon={<IconCheck size="1.2rem" />} color="teal" variant="light" radius="md" title="Success!">
                    Your project has been shipped to the gallery. Time to celebrate!
                  </Alert>
                </motion.div>
              )}
              
              <TextInput 
                label="Project Name" 
                placeholder="e.g. NextGen API"
                required 
                size="md"
                radius="md"
                {...form.getInputProps('name')} 
              />
              
              <Textarea 
                label="Pitch / Description" 
                placeholder="What problem does it solve? How did you build it?"
                required 
                minRows={5} 
                size="md"
                radius="md"
                {...form.getInputProps('description')} 
              />
              
              <TextInput 
                label="GitHub Repository" 
                placeholder="https://github.com/your-username/repo"
                required 
                size="md"
                radius="md"
                leftSection={<IconBrandGithub size="1.2rem" />}
                {...form.getInputProps('repoUrl')} 
              />
              
              <TextInput 
                label="Demo Video URL" 
                placeholder="YouTube or Loom link (Optional)"
                size="md"
                radius="md"
                leftSection={<IconVideo size="1.2rem" />}
                {...form.getInputProps('videoUrl')} 
              />
              
              <TextInput 
                label="What AWS services did you use? (Custom Question)" 
                placeholder="e.g. S3, Lambda, DynamoDB"
                required 
                size="md"
                radius="md"
                {...form.getInputProps('customAnswers.aws_services')} 
              />

              <Button 
                type="submit" 
                size="xl" 
                mt="xl" 
                radius="xl"
                loading={isSubmitting}
                variant="gradient" 
                gradient={{ from: 'teal', to: 'indigo' }}
                fullWidth
              >
                Launch Submission
              </Button>
            </Stack>
          </form>
        </Paper>
      </motion.div>
    </Container>
  );
}
