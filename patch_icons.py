import re

with open('/Users/yash/Desktop/DogFood-export/frontend/src/App.tsx', 'r') as f:
    content = f.read()

# Replace ThemeIcon block with just the Icon
content = re.sub(r'<ThemeIcon[^>]*>.*?<IconCode size="2rem" stroke={1\.5} />.*?</ThemeIcon>', '<IconCode size={24} stroke={1.5} color="var(--text)" style={{ marginBottom: 16 }} />', content, flags=re.DOTALL)
content = re.sub(r'<ThemeIcon[^>]*>.*?<IconGavel size="2rem" stroke={1\.5} />.*?</ThemeIcon>', '<IconGavel size={24} stroke={1.5} color="var(--text)" style={{ marginBottom: 16 }} />', content, flags=re.DOTALL)
content = re.sub(r'<ThemeIcon[^>]*>.*?<IconTrophy size="2rem" stroke={1\.5} />.*?</ThemeIcon>', '<IconTrophy size={24} stroke={1.5} color="var(--text)" style={{ marginBottom: 16 }} />', content, flags=re.DOTALL)

with open('/Users/yash/Desktop/DogFood-export/frontend/src/App.tsx', 'w') as f:
    f.write(content)
