import { Group, Button, Text, Tooltip } from '@mantine/core';
import { useState, useEffect } from 'react';
import { fetchWithAuth } from '../../api';
import { IconThumbUp } from '@tabler/icons-react';
import { useAuth } from '../../context/AuthContext';
import { EVENT_ID } from '../../constants';

interface VotingWidgetProps {
  submissionId: string;
}

export function VotingWidget({ submissionId }: VotingWidgetProps) {
  const [votes, setVotes] = useState(0);
  const [hasVoted, setHasVoted] = useState(false);
  const [loading, setLoading] = useState(false);
  const { user } = useAuth();

  // Load real vote count and whether this user already voted on mount
  useEffect(() => {
    const loadVotes = async () => {
      try {
        const results: any[] = await fetchWithAuth(`/api/voting/${EVENT_ID}/results`);
        const entry = results.find((r: any) => r.submissionId === submissionId);
        if (entry) setVotes(entry.totalVotes ?? 0);
      } catch {
        // silently fail — gallery still renders without vote counts
      }
    };

    const loadMyVotes = async () => {
      if (!user) return;
      try {
        const myVotes: any[] = await fetchWithAuth(`/api/voting/${EVENT_ID}/my-votes`);
        const alreadyVoted = myVotes.some((v: any) => v.submissionId === submissionId);
        setHasVoted(alreadyVoted);
      } catch {
        // not critical
      }
    };

    loadVotes();
    loadMyVotes();
  }, [submissionId, user]);

  const handleVote = async () => {
    if (!user || hasVoted || loading) return;
    setLoading(true);
    try {
      await fetchWithAuth(`/api/voting/${EVENT_ID}/vote`, {
        method: 'POST',
        body: JSON.stringify({
          submissionId,
          votesToCast: 1,
          deviceFingerprint: null,
        }),
      });
      setVotes(v => v + 1);
      setHasVoted(true);
    } catch (err) {
      console.error('Failed to cast vote', err);
    } finally {
      setLoading(false);
    }
  };

  const button = (
    <Button
      variant={hasVoted ? 'filled' : 'default'}
      size="sm"
      leftSection={<IconThumbUp size={16} />}
      onClick={handleVote}
      disabled={!user || hasVoted || loading}
      loading={loading}
      style={{
        backgroundColor: hasVoted ? 'var(--accent-soft)' : 'transparent',
        color: hasVoted ? 'var(--accent)' : 'var(--text)',
        borderColor: hasVoted ? 'var(--accent-soft)' : 'var(--border-strong)',
      }}
    >
      {hasVoted ? `Voted (${votes})` : `Vote (${votes})`}
    </Button>
  );

  return (
    <Group justify="space-between" align="center">
      <Text size="xs" style={{ color: 'var(--text-muted)' }}>
        {votes} vote{votes !== 1 ? 's' : ''}
      </Text>
      {!user ? (
        <Tooltip label="Sign in to vote" withArrow position="top">
          {button}
        </Tooltip>
      ) : (
        button
      )}
    </Group>
  );
}
