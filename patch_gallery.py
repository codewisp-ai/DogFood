import sys

with open('frontend/src/pages/gallery/Gallery.tsx', 'r') as f:
    content = f.read()

# Add sort state
content = content.replace("const [track, setTrack] = useState<string | null>(null);", "const [track, setTrack] = useState<string | null>(null);\n  const [sort, setSort] = useState<string | null>('Random (Unbiased)');")

# Modify fetch logic to use ballot randomization
old_fetch = """        const data = await fetchWithAuth(`/api/submissions/gallery?${queryParams.toString()}`);
        setSubmissions(data);"""

new_fetch = """        const data = await fetchWithAuth(`/api/submissions/gallery?${queryParams.toString()}`);
        // If sorting randomly, use the deterministic backend ballot randomization
        if (sort === 'Random (Unbiased)' && data.length > 0) {
           try {
             const ids = data.map((d: any) => d.id).join(',');
             const EVENT_ID = '00000000-0000-0000-0000-000000000000'; // Fallback if no constant
             // Fetch from real voting service to get seeded random order
             const randomizedIds = await fetchWithAuth(`/api/voting/${EVENT_ID}/ballot?submissionIds=${ids}`);
             if (Array.isArray(randomizedIds)) {
               data.sort((a: any, b: any) => randomizedIds.indexOf(a.id) - randomizedIds.indexOf(b.id));
             }
           } catch (e) {
             console.error('Failed to randomize ballot', e);
           }
        }
        setSubmissions(data);"""

content = content.replace(old_fetch, new_fetch)
content = content.replace("data={['Most Voted', 'Newest', 'Alphabetical']}", "data={['Random (Unbiased)', 'Most Voted', 'Newest', 'Alphabetical']}\n              value={sort}\n              onChange={setSort}")
content = content.replace("defaultValue=\"Most Voted\"", "")

with open('frontend/src/pages/gallery/Gallery.tsx', 'w') as f:
    f.write(content)
