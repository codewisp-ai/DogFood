import sys

with open('/Users/yash/Desktop/DogFood-export/frontend/src/App.tsx', 'r') as f:
    content = f.read()

# Add import
content = content.replace("import { Gallery } from './pages/gallery/Gallery';", "import { Gallery } from './pages/gallery/Gallery';\nimport { Leaderboard } from './pages/Leaderboard';")

# Add to publicRoutes
content = content.replace("const publicRoutes = ['/', '/login', '/register', '/gallery'];", "const publicRoutes = ['/', '/login', '/register', '/gallery', '/leaderboard'];")

# Add route
content = content.replace('<Route path="/gallery" element={<Gallery />} />', '<Route path="/gallery" element={<Gallery />} />\n            <Route path="/leaderboard" element={<Leaderboard />} />')

with open('/Users/yash/Desktop/DogFood-export/frontend/src/App.tsx', 'w') as f:
    f.write(content)
