import sys

with open('frontend/src/pages/dashboard/AdminDashboard.tsx', 'r') as f:
    content = f.read()

# Import DataExports
content = content.replace("import { ForensicScans } from './ForensicScans';", "import { ForensicScans } from './ForensicScans';\nimport { DataExports } from './DataExports';")

# Add Tab
content = content.replace('<Tabs.Tab value="forensics">JGit Forensics</Tabs.Tab>', '<Tabs.Tab value="forensics">JGit Forensics</Tabs.Tab>\n          <Tabs.Tab value="exports">Data Exports</Tabs.Tab>')

# Add Tab Panel
panel = """        <Tabs.Panel value="forensics">
          <Container title="JGit Forensic Scanner">
            <ForensicScans />
          </Container>
        </Tabs.Panel>
        <Tabs.Panel value="exports">
          <Container title="Data Export Operations">
            <DataExports />
          </Container>
        </Tabs.Panel>"""

content = content.replace("""        <Tabs.Panel value="forensics">
          <Container title="JGit Forensic Scanner">
            <ForensicScans />
          </Container>
        </Tabs.Panel>""", panel)

with open('frontend/src/pages/dashboard/AdminDashboard.tsx', 'w') as f:
    f.write(content)

