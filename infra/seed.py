#!/usr/bin/env python3
import json
import os
import time
import uuid
import urllib.request
import urllib.error

API_BASE = "http://localhost:8080/api"

def wait_for_api():
    print("Waiting for API gateway to be ready...")
    for _ in range(60):
        try:
            req = urllib.request.Request("http://localhost:8080/actuator/health")
            with urllib.request.urlopen(req, timeout=2) as r:
                if r.getcode() == 200:
                    print("API is ready.")
                    return
        except Exception:
            pass
        time.sleep(2)
    print("Timeout waiting for API.")
    exit(1)

def register_user(email, password, name, role):
    print(f"Registering {email} as {role}...")
    payload = json.dumps({"email": email, "password": password, "displayName": name, "role": role}).encode('utf-8')
    req = urllib.request.Request(f"{API_BASE}/auth/register", data=payload, headers={'Content-Type': 'application/json'})
    try:
        with urllib.request.urlopen(req) as r:
            pass
    except urllib.error.HTTPError as e:
        print(f"Registration returned {e.code}")

    login_payload = json.dumps({"email": email, "password": password}).encode('utf-8')
    req_login = urllib.request.Request(f"{API_BASE}/auth/login", data=login_payload, headers={'Content-Type': 'application/json'})
    try:
        with urllib.request.urlopen(req_login) as r:
            resp = json.loads(r.read().decode('utf-8'))
            return resp.get("accessToken")
    except urllib.error.HTTPError as e:
        print(f"Failed to login {email}: {e.code}")
        return None

def main():
    wait_for_api()
    
    with open("fixtures.json", "r") as f:
        fixtures = json.load(f)
        
    print("Seeding users...")
    org_token = register_user("organizer@dogfood.dev", "password123", "Alice Organizer", "ORGANIZER")
    
    judge_a_email = fixtures["judges"][0]["email"]
    judge_b_email = fixtures["judges"][1]["email"]
    part_email = fixtures["teams"][0]["members"][0]
    
    for j in fixtures["judges"]:
        register_user(j["email"], "password123", j["name"], "JUDGE")
        
    judge_a_token = register_user(judge_a_email, "password123", fixtures["judges"][0]["name"], "JUDGE")
    judge_b_token = register_user(judge_b_email, "password123", fixtures["judges"][1]["name"], "JUDGE")
    part_token = register_user(part_email, "password123", "Participant", "PARTICIPANT")
    
    print("Tokens generated.")
    
    print("Generating seed.sql for fixtures...")
    sql = []
    event = fixtures["event"]
    evt_id = "00000000-0000-0000-0000-000000000001"
    
    sql.append(f"INSERT INTO events.events (id, name, slug, organizer_id, submission_deadline, status, created_at, version) VALUES ('{evt_id}', '{event['name']}', 'sample-hack-2026', '00000000-0000-0000-0000-000000000001', '{event['submissions_close']}', 'CLOSED', NOW(), 0) ON CONFLICT DO NOTHING;")
    
    for t in fixtures["tracks"]:
        tid = f"00000000-0000-0000-0000-0000000000{t['id'].split('_')[1]}"
        track_name = t['name'].replace("'", "''")
        sql.append(f"INSERT INTO events.tracks (id, event_id, name) VALUES ('{tid}', '{evt_id}', '{track_name}') ON CONFLICT DO NOTHING;")
        
    for team in fixtures["teams"]:
        team_id = f"00000000-0000-0000-0000-0000000000{team['id'].split('_')[1]}"
        team_name = team['name'].replace("'", "''")
        sql.append(f"INSERT INTO events.teams (id, event_id, name, created_by) VALUES ('{team_id}', '{evt_id}', '{team_name}', '00000000-0000-0000-0000-000000000001') ON CONFLICT DO NOTHING;")
        
    for prj in fixtures["projects"]:
        prj_uuid = f"00000000-0000-0000-0000-0000000000{prj['id'].split('_')[1]}"
        team_uuid = f"00000000-0000-0000-0000-0000000000{prj['team'].split('_')[1]}"
        track_uuid = f"00000000-0000-0000-0000-0000000000{prj['track'].split('_')[1]}"
        title = prj["title"].replace("'", "''")
        summary = prj["summary"].replace("'", "''")
        
        sql.append(f"INSERT INTO submissions.submissions (id, event_id, team_id, track_id, name, tagline, repository_url, status, submitted_at, version) VALUES ('{prj_uuid}', '{evt_id}', '{team_uuid}', '{track_uuid}', '{title}', '{summary}', '{prj['repo_url']}', 'SUBMITTED', '{prj['submitted_at']}', 0) ON CONFLICT DO NOTHING;")

    try:
        req = urllib.request.Request(f"{API_BASE}/auth/me", headers={"Authorization": f"Bearer {judge_a_token}"})
        with urllib.request.urlopen(req) as r:
            judge_a_uuid = json.loads(r.read().decode('utf-8')).get("id")
    except:
        judge_a_uuid = "00000000-0000-0000-0000-000000000001"
        
    toml_content = f"""[portal]
base_url = "http://localhost:8080"

[tiers]
claimed = ["T1", "T2", "T3", "T4"]
pitch = "Production-grade hackathon judging platform with mathematically sound normalization."

[auth]
organizer   = "Authorization: Bearer {org_token}"
judge_a     = "Authorization: Bearer {judge_a_token}"
judge_b     = "Authorization: Bearer {judge_b_token}"
participant = "Authorization: Bearer {part_token}"

[routes]
gallery      = "/api/events/{evt_id}/gallery"
submit       = "/api/submissions"
judge_scores = "/api/judging/my-assignments"
peer_scores  = "/api/judging/scores?judgeId={judge_a_uuid}"
csv_export   = "/api/events/{evt_id}/results/export"
"""

    with open(".dogfood.toml", "w") as f:
        f.write(toml_content)
        
    print(".dogfood.toml generated!")
    
    with open("infra/seed-fixtures.sql", "w") as f:
        f.write("\n".join(sql))
        
    print("SQL seed generated. Applying to database via docker...")
    os.system("docker exec -i food_dogs-postgres-1 psql -U dogfood_admin -d dogfood < infra/seed-fixtures.sql")
    print("Seeding complete! You can now run `python3 run.py .dogfood.toml`")

if __name__ == "__main__":
    main()
