import re

with open('/Users/yash/Desktop/DogFood-export/frontend/src/App.tsx', 'r') as f:
    content = f.read()

# Make Flex left-aligned instead of centered
content = content.replace('direction="column" \n        align="center" \n        ta="center"', 'direction="column" align="flex-start" ta="left"')

with open('/Users/yash/Desktop/DogFood-export/frontend/src/App.tsx', 'w') as f:
    f.write(content)
