import { Box, Flex, Text, Button, TextInput, Textarea, Group, Alert, Select } from '@mantine/core';
import { useForm } from '@mantine/form';
import { PageHeader } from '../components/shared/PageHeader';
import { Container } from '../components/shared/Container';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchWithAuth } from '../api';
import { EVENT_ID } from '../constants';

export function SubmissionForm() {
  const [step, setStep] = useState(1);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [tracks, setTracks] = useState<{ label: string; value: string }[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchWithAuth(`/api/events/${EVENT_ID}/tracks`)
      .then((data: any) => {
        if (Array.isArray(data)) {
          setTracks(data.map((t: any) => ({ label: t.name, value: t.id })));
        }
      })
      .catch((err) => console.error('Failed to load tracks', err));
  }, []);

  const steps = [
    { id: 1, title: 'Details' },
    { id: 2, title: 'Team' },
    { id: 3, title: 'Repository and media' },
    { id: 4, title: 'Review and submit' }
  ];

  const form = useForm({
    initialValues: {
      name: '',
      tagline: '',
      description: '',
      techTags: '',
      trackId: '',
      teamName: '',
      repositoryUrl: '',
      demoVideoUrl: '',
      liveLink: '',
      thumbnailUrl: '',
      imageGallery: '',
    },
    validate: {
      name: (val: string) => val.trim().length < 2 ? 'Project name is required' : null,
      teamName: (val: string) => val.trim().length < 2 ? 'Team name is required' : null,
    }
  });

  const handleSubmit = async (asDraft: boolean = false) => {
    if (form.validate().hasErrors) return;
    setSubmitting(true);
    setError(null);

    try {
      // Step 1: create a team for this event
      const teamRes = await fetchWithAuth(`/api/events/${EVENT_ID}/teams`, {
        method: 'POST',
        body: JSON.stringify({ name: form.values.teamName }),
      });
      const teamId: string = teamRes.id;

      // Step 2: create the submission
      const submissionRes = await fetchWithAuth('/api/submissions', {
        method: 'POST',
        body: JSON.stringify({
          eventId: EVENT_ID,
          teamId,
          trackId: form.values.trackId || null,
          name: form.values.name,
          tagline: form.values.tagline || null,
          description: form.values.description,
          thumbnailUrl: form.values.thumbnailUrl || null,
          imageGallery: form.values.imageGallery
            ? form.values.imageGallery.split('\n').map((u: string) => u.trim()).filter(Boolean)
            : [],
          repositoryUrl: form.values.repositoryUrl || null,
          demoVideoUrl: form.values.demoVideoUrl || null,
          liveLink: form.values.liveLink || null,
          techTags: form.values.techTags
            ? form.values.techTags.split(',').map((t: string) => t.trim()).filter(Boolean)
            : [],
        }),
      });

      // Step 3: finalize submission if not draft
      if (!asDraft) {
        await fetchWithAuth(`/api/submissions/${submissionRes.id}/submit`, {
          method: 'POST',
        });
      }

      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Failed to submit project. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box>
      <PageHeader
        title="Submit project"
        breadcrumbs={[{ label: 'Home', href: '/' }, { label: 'Participant', href: '/dashboard' }, { label: 'Submit project' }]}
      />

      <Flex gap={32} align="flex-start">
        {/* Left step nav */}
        <Box w={240} style={{ flexShrink: 0 }}>
          {steps.map((s) => {
            const isActive = s.id === step;
            const isCompleted = s.id < step;
            return (
              <Box
                key={s.id}
                p="8px 16px"
                mb={8}
                style={{
                  borderLeft: isActive ? '3px solid var(--accent)' : '3px solid transparent',
                  cursor: 'pointer'
                }}
                onClick={() => setStep(s.id)}
              >
                <Text size="xs" style={{ color: isActive || isCompleted ? 'var(--text)' : 'var(--text-muted)' }}>
                  Step {s.id}
                </Text>
                <Text size="sm" fw={700} style={{ color: isActive ? 'var(--accent)' : (isCompleted ? 'var(--text)' : 'var(--text-muted)') }}>
                  {s.title}
                </Text>
              </Box>
            );
          })}
        </Box>

        {/* Right content */}
        <Box style={{ flex: 1, maxWidth: 720 }}>
          {error && <Alert color="red" mb={20} title="Error">{error}</Alert>}

          {step === 1 && (
            <Container title="Project Details">
              <Box p={24}>
                <TextInput label="Project Name" placeholder="e.g. NextGen API" mb={20} required {...form.getInputProps('name')} />
                <TextInput label="One-line Pitch / Tagline" placeholder="What does it do in one sentence?" mb={20} {...form.getInputProps('tagline')} />
                <Textarea label="Description" placeholder="Describe your project in detail..." minRows={6} mb={20} {...form.getInputProps('description')} />
                <TextInput label="Tech Stack" placeholder="e.g. React, Spring Boot, PostgreSQL (comma separated)" mb={20} {...form.getInputProps('techTags')} />
                <Select
                  label="Track"
                  placeholder="Select track"
                  data={tracks}
                  clearable
                  {...form.getInputProps('trackId')}
                />
              </Box>
            </Container>
          )}

          {step === 2 && (
            <Container title="Team">
              <Box p={24}>
                <Text size="sm" mb={16} style={{ color: 'var(--text-muted)' }}>
                  Create a team name for your submission. You can invite teammates later.
                </Text>
                <TextInput label="Team Name" placeholder="e.g. Data Ninjas" required {...form.getInputProps('teamName')} />
              </Box>
            </Container>
          )}

          {step === 3 && (
            <Container title="Repository and Media">
              <Box p={24}>
                <TextInput label="GitHub Repository" placeholder="https://github.com/your-username/repo" mb={20} {...form.getInputProps('repositoryUrl')} />
                <TextInput label="Live Demo Link" placeholder="https://yourapp.vercel.app" mb={20} {...form.getInputProps('liveLink')} />
                <TextInput label="Demo Video URL" placeholder="YouTube or Loom link" mb={20} {...form.getInputProps('demoVideoUrl')} />
                <TextInput label="Thumbnail Image URL" placeholder="https://example.com/thumb.png" mb={20} {...form.getInputProps('thumbnailUrl')} />
                <Textarea label="Image Gallery URLs" placeholder="One image URL per line" minRows={3} {...form.getInputProps('imageGallery')} />
              </Box>
            </Container>
          )}

          {step === 4 && (
            <Container title="Review and Submit">
              <Box p={24}>
                <Text fw={700} mb={8}>Project Name</Text>
                <Text mb={16} style={{ color: 'var(--text-muted)' }}>{form.values.name || '—'}</Text>

                <Text fw={700} mb={8}>Track</Text>
                <Text mb={16} style={{ color: 'var(--text-muted)' }}>
                  {tracks.find((t) => t.value === form.values.trackId)?.label || 'None selected'}
                </Text>

                <Text fw={700} mb={8}>Tagline</Text>
                <Text mb={16} style={{ color: 'var(--text-muted)' }}>{form.values.tagline || '—'}</Text>

                <Text fw={700} mb={8}>Team</Text>
                <Text mb={16} style={{ color: 'var(--text-muted)' }}>{form.values.teamName || '—'}</Text>

                <Text fw={700} mb={8}>Description</Text>
                <Text mb={16} style={{ color: 'var(--text-muted)' }}>{form.values.description || '—'}</Text>

                <Text fw={700} mb={8}>Repository</Text>
                <Text mb={16} style={{ color: 'var(--text-muted)' }}>{form.values.repositoryUrl || '—'}</Text>

                <Text fw={700} mb={8}>Tech Tags</Text>
                <Text mb={16} style={{ color: 'var(--text-muted)' }}>{form.values.techTags || '—'}</Text>
              </Box>
            </Container>
          )}

          {/* Action bar */}
          <Box
            p={20}
            mt={24}
            style={{
              backgroundColor: 'var(--surface)',
              borderTop: '1px solid var(--border)',
              borderRadius: 'var(--radius-lg)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}
          >
            <Button variant="subtle" onClick={() => navigate('/dashboard')}>Cancel</Button>
            <Group gap={16}>
              <Button variant="default" disabled={step === 1} onClick={() => setStep(step - 1)}>Previous</Button>
              {step < 4 ? (
                <Button variant="filled" onClick={() => setStep(step + 1)}>Next</Button>
              ) : (
                <>
                  <Button variant="outline" loading={submitting} onClick={() => handleSubmit(true)}>Save as Draft</Button>
                  <Button variant="filled" loading={submitting} onClick={() => handleSubmit(false)}>Submit Project</Button>
                </>
              )}
            </Group>
          </Box>
        </Box>
      </Flex>
    </Box>
  );
}
