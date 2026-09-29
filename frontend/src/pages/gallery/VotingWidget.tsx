import { Group, Button, Text, Tooltip } from '@mantine/core';
import { useState } from 'react';
import { fetchWithAuth } from '../../api';
import { IconThumbUp } from '@tabler/icons-react';
import { useAuth } from '../../context/AuthContext';

interface VotingWidgetProps {
  submissionId: string;
}

export function VotingWidget({ submissionId }: VotingWidgetProps) {
  const [votes, setVotes] = useState(0);
  const { user } = useAuth();

  const handleVote = async () => {
    if (!user) return;
    const newVal = votes + 1;
    setVotes(newVal);
    try {
      await fetchWithAuth('/api/voting/votes', {
        method: 'POST',
        body: JSON.stringify({ submissionId, votes: newVal, delta: 1 }),
      });
    } catch (err) {
      console.error('Failed to submit vote', err);
      setVotes(votes);
    }
  };

  const button = (
    <Button 
      variant={votes > 0 ? 'filled' : 'secondary'} 
      size="sm" 
      leftSection={<IconThumbUp size={16} />}
      onClick={handleVote}
      disabled={!user}
      style={{
        backgroundColor: votes > 0 ? 'var(--accent-soft)' : 'transparent',
        color: votes > 0 ? 'var(--accent)' : 'var(--text)',
        borderColor: votes > 0 ? 'var(--accent-soft)' : 'var(--border-strong)',
      }}
    >
      Vote ({votes})
    </Button>
  );

  return (
    <Group justify="space-between" align="center">
      <Text size="xs" style={{ color: 'var(--text-muted)' }}>
        Cost: {votes * votes} cr
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
