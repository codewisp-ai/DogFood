import { Button, Paper, Title, Stack, Select, NumberInput, Group } from '@mantine/core';
import { useForm } from '@mantine/form';

export function EligibilityRules() {
  const form = useForm({
    initialValues: {
      rules: [{ type: 'MAX_TEAM_SIZE', value: 4 }]
    },
  });

  const addRule = () => form.insertListItem('rules', { type: 'MIN_TEAM_SIZE', value: 1 });

  return (
    <Paper withBorder p="md" radius="md">
      <Title order={3} mb="md">Eligibility Rules (JSONB Engine)</Title>
      <form onSubmit={form.onSubmit((values) => console.log('Rules', values))}>
        <Stack>
          {form.values.rules.map((rule, index) => (
            <Group key={index} align="flex-end">
              <Select
                label="Rule Type"
                data={['MAX_TEAM_SIZE', 'MIN_TEAM_SIZE', 'ONE_SUBMISSION_PER_TEAM']}
                {...form.getInputProps(`rules.${index}.type`)}
              />
              {rule.type.includes('TEAM_SIZE') && (
                <NumberInput
                  label="Value"
                  {...form.getInputProps(`rules.${index}.value`)}
                />
              )}
              <Button color="red" variant="light" onClick={() => form.removeListItem('rules', index)}>Remove</Button>
            </Group>
          ))}
          <Group mt="md">
            <Button variant="outline" onClick={addRule}>Add Rule</Button>
            <Button type="submit">Save Rules</Button>
          </Group>
        </Stack>
      </form>
    </Paper>
  );
}
