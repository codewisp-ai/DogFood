import sys

with open('/Users/yash/Desktop/DogFood-export/frontend/src/components/shared/SideNav.tsx', 'r') as f:
    content = f.read()

# Add admin links
admin_links = """
      <Box p={20} pt={32}>
        <Text size="md" fw={700} style={{ color: 'var(--text)' }}>Admin</Text>
      </Box>
      <Box>
        <NavItem label="Event Settings" to="/dashboard" active={location.pathname === '/dashboard'} />
        <NavItem label="Eligibility Rules" to="/dashboard?tab=rules" active={location.pathname === '/dashboard?tab=rules'} />
        <NavItem label="Judging Rubric" to="/dashboard?tab=rubric" active={location.pathname === '/dashboard?tab=rubric'} />
        <NavItem label="Live Progress" to="/dashboard?tab=progress" active={location.pathname === '/dashboard?tab=progress'} />
      </Box>
"""

content = content.replace("</Box>\n    </Box>\n  );\n}", "</Box>\n" + admin_links + "    </Box>\n  );\n}")

with open('/Users/yash/Desktop/DogFood-export/frontend/src/components/shared/SideNav.tsx', 'w') as f:
    f.write(content)
