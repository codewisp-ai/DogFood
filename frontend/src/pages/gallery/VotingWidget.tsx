import { Group, ActionIcon, Text, Tooltip } from '@mantine/core';
import { useState } from 'react';
import { fetchWithAuth } from '../../api';

interface VotingWidgetProps {
  submissionId: string;
}

export function VotingWidget({ submissionId }: VotingWidgetProps) {
  const [votes, setVotes] = useState(0);

  // Quadratic voting formula cost: (votes ^ 2)
  const cost = votes * votes;

  const handleVote = async (delta: number) => {
    const newVal = Math.max(0, votes + delta);
    setVotes(newVal);
    try {
      await fetchWithAuth('/api/voting/votes', {
        method: 'POST',
        body: JSON.stringify({ submissionId, votes: newVal, delta }),
      });
    } catch (err) {
      console.error('Failed to submit vote', err);
      // Revert on failure
      setVotes(votes);
    }
  };

  return (
    <Group justify="space-between" mt="auto">
      <Text size="xs" fw={500} c="blue">
        Cost: {cost} credits
      </Text>
      <Group gap="xs">
        <ActionIcon variant="light" color="red" onClick={() => handleVote(-1)} disabled={votes === 0}>
          -
        </ActionIcon>
        <Text fw={700} w={20} ta="center">{votes}</Text>
        <ActionIcon variant="light" color="green" onClick={() => handleVote(1)}>
          +
        </ActionIcon>
      </Group>
    </Group>
  );
}
