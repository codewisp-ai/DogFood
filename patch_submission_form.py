import sys

with open('frontend/src/pages/SubmissionForm.tsx', 'r') as f:
    content = f.read()

# Step 1: Add Tagline and Track
step1_old = """          {step === 1 && (
            <Container title="Project Details">
              <TextInput label="Project Name" placeholder="e.g. NextGen API" mb={20} />
              <Textarea label="Pitch / Description" placeholder="Markdown supported..." minRows={6} mb={20} />
              <TextInput label="Tech Stack" placeholder="Select tags..." />
            </Container>
          )}"""

step1_new = """          {step === 1 && (
            <Container title="Project Details">
              <TextInput label="Project Name" placeholder="e.g. NextGen API" mb={20} />
              <TextInput label="Tagline" placeholder="One sentence pitch..." mb={20} />
              <Textarea label="Long Description" placeholder="Markdown supported..." minRows={6} mb={20} />
              <TextInput label="Tech Stack" placeholder="Comma separated tags..." mb={20} />
              <TextInput label="Track" placeholder="Which track are you competing in?" />
            </Container>
          )}"""

# Step 3: Add Live Link, Thumbnail, Image Gallery
step3_old = """          {step === 3 && (
            <Container title="Repository and Media">
              <TextInput label="GitHub Repository" placeholder="https://github.com/your-username/repo" mb={20} />
              <TextInput label="Demo Video URL" placeholder="YouTube or Loom link" mb={20} />
            </Container>
          )}"""

step3_new = """          {step === 3 && (
            <Container title="Repository and Media">
              <TextInput label="GitHub Repository URL" placeholder="https://github.com/your-username/repo" mb={20} />
              <TextInput label="Live Link" placeholder="https://your-project.com" mb={20} />
              <TextInput label="Demo Video URL" placeholder="YouTube or Loom link" mb={20} />
              <TextInput label="Thumbnail URL" placeholder="Link to project thumbnail image" mb={20} />
              <Textarea label="Image Gallery URLs" placeholder="One image URL per line" minRows={3} mb={20} />
              <Textarea label="Custom Questions" placeholder="Answers to organizer-defined custom questions (JSON)" minRows={2} />
            </Container>
          )}"""

content = content.replace(step1_old, step1_new)
content = content.replace(step3_old, step3_new)

with open('frontend/src/pages/SubmissionForm.tsx', 'w') as f:
    f.write(content)

