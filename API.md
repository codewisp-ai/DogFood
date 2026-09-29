# 📖 Dogfood Platform API Specification & Reference

This document provides a comprehensive, production-grade reference for all REST endpoints across the **Dogfood Hackathon Platform**. All external requests enter through the **Spring Cloud Gateway (`:8080`)**, which enforces rate limiting, token verification, and reverse proxy routing.

---

## 🔐 Global Authentication & Headers

| Header | Required | Description | Example |
|---|---|---|---|
| `Authorization` | Conditional | Asymmetric Bearer JWT (`RS256`). Required on all non-public routes. | `Bearer eyJhbGciOiJSUzI1...` |
| `Content-Type` | Conditional | `application/json` for mutations; `text/csv` for exports. | `application/json` |
| `Idempotency-Key` | Conditional | UUID required for score and vote mutations to prevent duplicates. | `c4b1e5a2-9d3f-4e8b-8a7c-1f2e3d4c5b6a` |
| `X-Correlation-ID`| Optional | Distributed tracing correlation ID propagated to audit logs. | `trace-8921-abc` |

---

## 1. Identity & Access Service (`:8081` via Gateway `/api/auth/**`)

### `POST /api/auth/register`
Register a new participant, judge, or organizer.
- **Access**: Public
- **Request Body**:
  ```json
  {
    "email": "developer@example.org",
    "password": "password123",
    "displayName": "Alex Dev",
    "role": "PARTICIPANT"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
    "refreshToken": "7c9b8e21-4d3f...",
    "expiresIn": 2592000,
    "user": {
      "id": "b18e08b5-7dbb-44de-82a8-0786e6a5e39f",
      "email": "developer@example.org",
      "displayName": "Alex Dev"
    }
  }
  ```

### `POST /api/auth/login`
Authenticate existing credentials and issue 30-day evaluation JWT.
- **Access**: Public
- **Request Body**:
  ```json
  {
    "email": "organizer@dogfood.dev",
    "password": "password123"
  }
  ```
- **Response `200 OK`**: Returns `accessToken`, `refreshToken`, `expiresIn`, and `user` profile.

### `GET /api/auth/.well-known/jwks.json`
Public JSON Web Key Set (JWKS) containing the RSA public key exponent and modulus for stateless token verification.
- **Access**: Public

### `GET /api/auth/events/{eventId}/users/export.csv`
Export all registered users and assigned event roles in standardized CSV format.
- **Access**: `ORGANIZER` only
- **Response `200 OK`**: `text/csv`

---

## 2. Event & Team Service (`:8082` via Gateway `/api/events/**`)

### `GET /api/events`
List all hackathon events.
- **Access**: Public

### `GET /api/events/{slugOrId}`
Retrieve event details, tracks, custom questions, and deadline boundaries by UUID or slug (`sample-hack-2026`).
- **Access**: Public
- **Response `200 OK`**:
  ```json
  {
    "id": "00000000-0000-0000-0000-000000000001",
    "name": "Sample Hack 2026",
    "slug": "sample-hack-2026",
    "submissionDeadline": "2026-03-01T18:00:00Z",
    "status": "OPEN",
    "eligibilityRules": []
  }
  ```

### `PUT /api/events/{eventId}`
Update event configurations, submission deadline, status, and rubric linkages.
- **Access**: `ORGANIZER` only

### `POST /api/events/{eventId}/teams`
Create a new team for an event.
- **Access**: Authenticated `PARTICIPANT`
- **Request Body**: `{"name": "CyberHounds"}`

### `GET /api/events/teams/{teamId}`
Retrieve team details and roster members.
- **Access**: Authenticated

### `POST /api/events/teams/join`
Join a team using a signed invitation token.
- **Access**: Authenticated `PARTICIPANT`
- **Request Body**: `{"token": "inv_9f8e7d..."}`

### `GET /api/events/{eventId}/prizes`
List all tracks and prizes configured for the event.
- **Access**: Public

---

## 3. Submission & Forensics Service (`:8083` via Gateway `/api/submissions/**`)

### `POST /api/submissions`
Create or update a draft project submission.
- **Access**: Authenticated `PARTICIPANT` (Team Member)
- **Request Body**:
  ```json
  {
    "eventId": "00000000-0000-0000-0000-000000000001",
    "teamId": "tm_01",
    "name": "Hollow Signal",
    "tagline": "Real-time mesh communications",
    "description": "Comprehensive markdown pitch...",
    "repositoryUrl": "https://github.com/example/repo",
    "demoVideoUrl": "https://youtube.com/watch?v=...",
    "liveLink": "https://hollow-signal.vercel.app",
    "techTags": ["React", "Spring Boot", "PostgreSQL"],
    "customAnswers": {}
  }
  ```

### `POST /api/submissions/{id}/submit`
Finalize project submission. Enforces cutoff using server-time Redis distributed lock (`SETNX`).
- **Access**: Authenticated `PARTICIPANT`

### `GET /api/events/{eventId}/gallery`
Retrieve paginated, public project submissions.
- **Access**: Public
- **Query Params**: `page` (default: 0), `size` (default: 12), `track` (optional), `search` (optional).
- **Response `200 OK`**:
  ```json
  {
    "content": [
      {
        "id": "prj_01",
        "name": "Glass Signal",
        "tagline": "One line summary",
        "status": "SUBMITTED",
        "techTags": ["Rust", "WASM"]
      }
    ],
    "totalPages": 2,
    "totalElements": 20
  }
  ```

### `GET /api/events/{eventId}/submissions/export.csv`
Export all project submissions with links and forensic status.
- **Access**: `ORGANIZER` only

---

## 4. Judging & Normalization Service (`:8084` via Gateway `/api/judging/**`)

### `GET /api/judging/my-assignments`
Retrieve projects assigned to the calling judge. Disjoint batching guarantees peer isolation.
- **Access**: `JUDGE` only (Extracted from JWT claims)

### `POST /api/judging/scores`
Submit evaluation score for a specific rubric criterion.
- **Access**: `JUDGE` only
- **Headers**: `Idempotency-Key: <uuid>`
- **Request Body**:
  ```json
  {
    "eventId": "00000000-0000-0000-0000-000000000001",
    "submissionId": "prj_01",
    "criterionId": "crit_innovation",
    "rawScore": 8.5,
    "feedback": "Strong architecture, clean separation of concerns."
  }
  ```

### `POST /api/judging/coi`
Declare a Conflict of Interest (COI). Automatically recuses judge from assignment and triggers reassignment.
- **Access**: `JUDGE` only
- **Request Body**:
  ```json
  {
    "eventId": "00000000-0000-0000-0000-000000000001",
    "submissionId": "prj_01",
    "reason": "Previous mentor to team lead"
  }
  ```

### `GET /api/events/{eventId}/rubric`
Fetch active weighted criteria rubric for the event.
- **Access**: Public / Authenticated

### `PUT /api/events/{eventId}/rubric`
Configure multi-criterion weights summing to 1.0.
- **Access**: `ORGANIZER` only
- **Request Body**:
  ```json
  {
    "criteria": [
      {"name": "Technical Execution", "weight": 0.40, "maxScore": 10},
      {"name": "Innovation & Originality", "weight": 0.35, "maxScore": 10},
      {"name": "Design & UI/UX", "weight": 0.25, "maxScore": 10}
    ]
  }
  ```

### `GET /api/events/{eventId}/results`
Compute and return normalized scores using per-judge Z-Scores and Bayesian shrinkage ($k_0=5$).
- **Access**: `ORGANIZER` only (during review) / Public (when event closed)

### `GET /api/events/{eventId}/results/export`
Export final normalized rankings and raw distributions in CSV format.
- **Access**: `ORGANIZER` only

---

## 5. Community Consensus & Voting Service (`:8085` via Gateway `/api/voting/**`)

### `GET /api/voting/{eventId}/ballot`
Get deterministically shuffled project ballot. Seeded by `SHA-256(voterId + eventId)` to eliminate presentation bias.
- **Access**: Public / Authenticated

### `POST /api/voting/{eventId}/vote`
Cast quadratic vote. Cost is quadratic: $n$ votes costs $n^2$ credits from a 100-credit budget.
- **Access**: Public / Authenticated
- **Request Body**:
  ```json
  {
    "submissionId": "prj_01",
    "votesToCast": 3,
    "deviceFingerprint": "fp_8a2b3c..."
  }
  ```

### `GET /api/voting/{eventId}/results`
Get community vote tallies. Hidden while voting is open unless called by an organizer.
- **Access**: Public (if closed) / `ORGANIZER`

---

## 6. Observability, Telemetry & Audit (`:8089` via Gateway)

### `GET /actuator/health`
System liveness and readiness probe for Docker / Kubernetes healthchecks.
- **Access**: Public

### `GET /status`
Public platform status page showing service health, uptime, and database connectivity.
- **Access**: Public

### `GET /api/events/{eventId}/judge-progress`
Server-Sent Events (SSE) stream pushing real-time judge scoring velocity and progress directly from RabbitMQ.
- **Access**: `ORGANIZER` only
- **Response**: `text/event-stream`
