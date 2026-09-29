import sys

with open('/Users/yash/Desktop/DogFood-export/frontend/src/App.tsx', 'r') as f:
    content = f.read()

# Update imports
content = content.replace("import { Dashboard } from './pages/dashboard/Dashboard';", "import { ParticipantDashboard } from './pages/dashboard/ParticipantDashboard';\nimport { AdminDashboard } from './pages/dashboard/AdminDashboard';")

# Update routes
content = content.replace('<Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />', '<Route path="/dashboard" element={<ProtectedRoute><ParticipantDashboard /></ProtectedRoute>} />\n            <Route path="/admin" element={<ProtectedRoute><AdminDashboard /></ProtectedRoute>} />')

with open('/Users/yash/Desktop/DogFood-export/frontend/src/App.tsx', 'w') as f:
    f.write(content)
