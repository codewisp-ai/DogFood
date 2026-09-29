import sys, re

with open('/Users/yash/Desktop/DogFood-export/frontend/src/App.tsx', 'r') as f:
    content = f.read()

content = re.sub(r'<Badge[^>]*>\s*THE PREMIER HACKATHON ENGINE\s*</Badge>', '', content, flags=re.DOTALL)
content = content.replace("<span>\n            ship.\n          </span>", "ship.")
content = content.replace("<span>ship.</span>", "ship.")

with open('/Users/yash/Desktop/DogFood-export/frontend/src/App.tsx', 'w') as f:
    f.write(content)
