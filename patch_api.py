import sys

with open('frontend/src/api.ts', 'r') as f:
    content = f.read()

old = """  // Handle 204 No Content
  if (response.status === 204) return null;
  return response.json();"""

new = """  // Handle 204 No Content
  if (response.status === 204) return null;
  
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('text/csv')) {
    return response.text();
  }
  return response.json();"""

content = content.replace(old, new)

with open('frontend/src/api.ts', 'w') as f:
    f.write(content)

