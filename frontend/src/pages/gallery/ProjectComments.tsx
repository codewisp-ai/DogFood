import { useState, useEffect } from 'react';
import { Box, Text, Group, TextInput, Button, Stack, Avatar, Paper } from '@mantine/core';
import { fetchWithAuth } from '../../api';

export function ProjectComments({ submissionId }: { submissionId: string }) {
  const [comments, setComments] = useState<any[]>([]);
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(false);

  const loadComments = async () => {
    try {
      const data = await fetchWithAuth(`/api/submissions/${submissionId}/comments`);
      setComments(data || []);
    } catch (e) {
      console.error('Failed to load comments', e);
    }
  };

  useEffect(() => {
    loadComments();
  }, [submissionId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    setLoading(true);
    try {
      // In a real app, author name comes from auth context.
      // Here we just use a default or let backend use "Anonymous" if not provided.
      await fetchWithAuth(`/api/submissions/${submissionId}/comments`, {
        method: 'POST',
        headers: { 'X-User-Name': 'Visitor' },
        body: JSON.stringify({ content: newComment.trim() })
      });
      setNewComment('');
      loadComments();
    } catch (e) {
      console.error('Failed to post comment', e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box mt={16}>
      <Text fw={600} size="sm" mb={12}>Comments ({comments.length})</Text>
      
      <Stack gap={12} mb={16}>
        {comments.map((c: any) => (
          <Paper key={c.id} p="sm" bg="var(--bg-subtle)" radius="sm">
            <Group gap={8} mb={4}>
              <Avatar size={20} radius="xl" color="blue">{c.authorName?.charAt(0) || 'V'}</Avatar>
              <Text size="xs" fw={600}>{c.authorName || 'Anonymous'}</Text>
              <Text size="xs" c="dimmed">
                {c.createdAt ? new Date(c.createdAt).toLocaleDateString() : 'Just now'}
              </Text>
            </Group>
            <Text size="sm">{c.content}</Text>
          </Paper>
        ))}
      </Stack>

      <form onSubmit={handleSubmit}>
        <Group gap={8} align="flex-start">
          <TextInput
            placeholder="Add a comment..."
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            style={{ flex: 1 }}
            size="sm"
          />
          <Button type="submit" size="sm" loading={loading} disabled={!newComment.trim()}>
            Post
          </Button>
        </Group>
      </form>
    </Box>
  );
}
