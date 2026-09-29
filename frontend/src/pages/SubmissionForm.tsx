import { Box, Flex, Text, Button, TextInput, Textarea, Group } from '@mantine/core';
import { PageHeader } from '../components/shared/PageHeader';
import { Container } from '../components/shared/Container';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export function SubmissionForm() {
  const [step, setStep] = useState(1);
  const navigate = useNavigate();

  const steps = [
    { id: 1, title: 'Details' },
    { id: 2, title: 'Team' },
    { id: 3, title: 'Repository and media' },
    { id: 4, title: 'Review and submit' }
  ];

  return (
    <Box>
      <PageHeader 
        title="Submit project"
        breadcrumbs={[{ label: 'Home', href: '/' }, { label: 'Participant', href: '/dashboard' }, { label: 'Submit project' }]}
      />

      <Flex gap={32} align="flex-start">
        {/* Left column (240px) */}
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
            )
          })}
        </Box>

        {/* Right column */}
        <Box style={{ flex: 1, maxWidth: 720 }}>
          {step === 1 && (
            <Container title="Project Details">
              <TextInput label="Project Name" placeholder="e.g. NextGen API" mb={20} />
              <TextInput label="Tagline" placeholder="One sentence pitch..." mb={20} />
              <Textarea label="Long Description" placeholder="Markdown supported..." minRows={6} mb={20} />
              <TextInput label="Tech Stack" placeholder="Comma separated tags..." mb={20} />
              <TextInput label="Track" placeholder="Which track are you competing in?" />
            </Container>
          )}

          {step === 2 && (
            <Container title="Team">
              <Text style={{ color: 'var(--text-muted)', marginBottom: 20 }}>Invite members or enter their emails.</Text>
              <TextInput label="Member Emails" placeholder="Comma separated emails" />
            </Container>
          )}

          {step === 3 && (
            <Container title="Repository and Media">
              <TextInput label="GitHub Repository URL" placeholder="https://github.com/your-username/repo" mb={20} />
              <TextInput label="Live Link" placeholder="https://your-project.com" mb={20} />
              <TextInput label="Demo Video URL" placeholder="YouTube or Loom link" mb={20} />
              <TextInput label="Thumbnail URL" placeholder="Link to project thumbnail image" mb={20} />
              <Textarea label="Image Gallery URLs" placeholder="One image URL per line" minRows={3} mb={20} />
              <Textarea label="Custom Questions" placeholder="Answers to organizer-defined custom questions (JSON)" minRows={2} />
            </Container>
          )}

          {step === 4 && (
            <Container title="Review and Submit">
              <Text style={{ color: 'var(--text-muted)' }}>Please review your details before final submission.</Text>
            </Container>
          )}

          {/* Sticky action bar */}
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
              <Button variant="default">Save draft</Button>
              {step < 4 ? (
                <Button variant="filled" onClick={() => setStep(step + 1)}>Next</Button>
              ) : (
                <Button variant="filled" onClick={() => navigate('/dashboard')}>Submit Project</Button>
              )}
            </Group>
          </Box>
        </Box>
      </Flex>
    </Box>
  );
}
