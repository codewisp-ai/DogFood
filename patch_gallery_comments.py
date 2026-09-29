import sys

with open('frontend/src/pages/gallery/Gallery.tsx', 'r') as f:
    content = f.read()

# Fix import
if "EVENT_ID" not in content[:500]:
    content = content.replace("import { fetchWithAuth } from '../../api';", "import { fetchWithAuth } from '../../api';\nimport { EVENT_ID } from '../../constants';")

# Import ProjectComments
content = content.replace("import { VotingWidget } from './VotingWidget';", "import { VotingWidget } from './VotingWidget';\nimport { ProjectComments } from './ProjectComments';")

# State for expanded comments
content = content.replace("const [loading, setLoading] = useState(true);", "const [loading, setLoading] = useState(true);\n  const [expandedComments, setExpandedComments] = useState<Record<string, boolean>>({});\n\n  const toggleComments = (id: string) => setExpandedComments(prev => ({ ...prev, [id]: !prev[id] }));")

# Add button and comments section
voting_widget_block = """<Box pt={16} style={{ borderTop: '1px solid var(--border-subtle)' }}>
                      <VotingWidget submissionId={sub.id} />
                    </Box>"""

new_voting_widget_block = """<Box pt={16} style={{ borderTop: '1px solid var(--border-subtle)' }}>
                      <Group justify="space-between" align="center">
                        <VotingWidget submissionId={sub.id} />
                        <Button variant="subtle" size="xs" onClick={() => toggleComments(sub.id)}>
                          {expandedComments[sub.id] ? 'Hide Comments' : 'Comments'}
                        </Button>
                      </Group>
                      {expandedComments[sub.id] && (
                        <ProjectComments submissionId={sub.id} />
                      )}
                    </Box>"""

content = content.replace(voting_widget_block, new_voting_widget_block)

with open('frontend/src/pages/gallery/Gallery.tsx', 'w') as f:
    f.write(content)

