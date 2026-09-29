import sys

with open('/Users/yash/Desktop/DogFood-export/frontend/src/App.tsx', 'r') as f:
    content = f.read()

# Add import
content = content.replace("import { Gallery } from './pages/gallery/Gallery';", "import { Gallery } from './pages/gallery/Gallery';\nimport { Home } from './pages/Home';")

# Delete inline Home component
import re
content = re.sub(r'function Home\(\) \{.*', '', content, flags=re.DOTALL)

with open('/Users/yash/Desktop/DogFood-export/frontend/src/App.tsx', 'w') as f:
    f.write(content)
