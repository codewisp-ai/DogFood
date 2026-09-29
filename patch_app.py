import sys

with open('frontend/src/App.tsx', 'r') as f:
    content = f.read()

# Add import
content = content.replace("import { Gallery } from './pages/gallery/Gallery';", "import { Gallery } from './pages/gallery/Gallery';\nimport { GalleryWidget } from './pages/gallery/GalleryWidget';")

# Add to noSidebarRoutes
content = content.replace("const noSidebarRoutes = ['/', '/login', '/register'];", "const noSidebarRoutes = ['/', '/login', '/register', '/widget/gallery'];")

# Add route
content = content.replace('<Route path="/gallery" element={<Gallery />} />', '<Route path="/gallery" element={<Gallery />} />\n            <Route path="/widget/gallery" element={<GalleryWidget />} />')

# Hide GlobalNav for widget
content = content.replace("<GlobalNav isPublicPage={isNoSidebarPage} opened={opened} toggle={toggle} />", "{location.pathname !== '/widget/gallery' && <GlobalNav isPublicPage={isNoSidebarPage} opened={opened} toggle={toggle} />}")

# Remove padding for widget
content = content.replace("style={{ maxWidth: 1280, margin: '0 auto', padding: '24px' }}", "style={{ maxWidth: location.pathname === '/widget/gallery' ? '100%' : 1280, margin: '0 auto', padding: location.pathname === '/widget/gallery' ? '0' : '24px' }}")
content = content.replace("marginTop: 56,", "marginTop: location.pathname === '/widget/gallery' ? 0 : 56,")

with open('frontend/src/App.tsx', 'w') as f:
    f.write(content)

