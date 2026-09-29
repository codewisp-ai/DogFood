# Threat Model & Security Architecture

This document provides a comprehensive security and threat model analysis for the Dogfood platform, following the STRIDE methodology. It documents mitigated attack vectors, implementation mechanisms, and honest architectural boundaries (unmitigated limits).

---

## 1. STRIDE Threat Matrix

| STRIDE Category | Threat Vector | Platform Mitigation | Mitigation Level |
|---|---|---|---|
| **Spoofing** | Forged organizer or judge identity | RS256 asymmetric JWT verification via JWKS with isolated keys | **Hardened** |
| **Tampering** | Deadline bypass via network latency manipulation | Distributed Redis lock (`SETNX`) on server-side monotonic time | **Hardened** |
| **Repudiation** | Malicious score tampering or unauthorized recusal | Immutable audit events published to `dogfood.audit` queue on RabbitMQ | **Hardened** |
| **Information Disclosure** | Cross-judge score snooping via API parameter tampering | Engine-enforced PostgreSQL Row-Level Security (RLS) on `judging.scores` | **Zero-Trust Hardened** |
| **Denial of Service** | Public gallery and voting submission spam | Redis token-bucket rate limiter per IP/voter fingerprint | **Rate-Limited** |
| **Elevation of Privilege** | Participant accessing administrative or judging routes | Claim-based role filtering injected by API gateway; never trusted from client parameters | **Hardened** |

---

## 2. In-Depth Vector Analysis & Mitigations

### 2.1 Cross-Judge Score Leakage (Information Disclosure)
- **Threat**: A rogue judge manipulates query parameters (e.g. `GET /api/judging/scores?judgeId=<peer-uuid>`) to inspect peer evaluations and manipulate the normalized average.
- **Mitigation (Dual-Layer)**:
  1. *Application Layer*: Spring Data repositories strictly filter by `judge_id` extracted from verified JWT claims. Request query parameters for `judge_id` are ignored.
  2. *Database Storage Layer*: PostgreSQL Row-Level Security (`V2__enable_rls.sql`) evaluates:
     ```sql
     CREATE POLICY judge_scores_select ON judging.scores
       FOR SELECT USING (
         judge_id = current_setting('app.current_judge_id', true)::uuid
         OR current_setting('app.current_role', true) IN ('ORGANIZER', 'ADMIN')
       );
     ```
     Even if an application controller suffers an injection or logic flaw, the PostgreSQL storage engine silently returns zero rows for peer evaluations.

### 2.2 Submission Cutoff Races (Tampering)
- **Threat**: Participants exploit network latency, client-side clocks, or connection retries to submit projects after the official deadline.
- **Mitigation**: Server-side deadline enforcement using distributed Redis locks (`SETNX`) on `event.submissionDeadline`. If `Instant.now()` exceeds the server deadline, the mutation is rejected with `HTTP 400 Bad Request` regardless of client timestamp claims.

### 2.3 Sybil Attacks & Ballot Stuffing (Denial of Service / Tampering)
- **Threat**: Scripted bots or malicious users vote repeatedly to distort community voting awards.
- **Mitigation**:
  1. *Quadratic Cost Curve*: Quadratic voting enforces $n^2$ credit consumption for $n$ votes. Capping budgets at 100 credits restricts influence mathematically ($\sqrt{100} = 10$ votes maximum).
  2. *Rate Limiting*: Redis token-bucket filter limits requests to 10 requests per second per IP address.
  3. *Deterministic Ballot Shuffling*: Project presentation order is randomized deterministically using `Seed = SHA-256(voterId + eventId)`, eliminating top-of-page presentation bias.

### 2.4 Pre-Built Code & Commit Dumps (Integrity Fraud)
- **Threat**: Participants submit projects built months before the hackathon kickoff or dump 20,000 lines into a single commit right before deadline.
- **Mitigation**:
  - In-memory clone using pure Java Eclipse JGit (zero shell execution to prevent command injection exploits).
  - Graph traversal inspects commit timestamp distributions against event start and end windows.
  - Generates an automated `risk_score` (0.0 to 100.0) and `risk_flags` (e.g. `SINGLE_COMMIT_DUMP`, `PRE_HACKATHON_CODE`) to prioritize organizer audit attention.

---

## 3. Honest Limits & Unmitigated Boundaries

A credible engineering threat model explicitly defines where automated protections end and human oversight is required:

1. **Squashed & Rewritten Git Histories (Unmitigated)**:
   - *Limitation*: A participant who writes code months prior can rewrite their local git history using `git rebase` or custom `GIT_AUTHOR_DATE` values to simulate an organic commit timeline during the hackathon weekend.
   - *Residual Risk*: In-memory forensics cannot prove historical veracity if commit author dates have been deliberately spoofed before pushing. Organizer manual interviews remain mandatory.

2. **Coordinated Human Collusion (Unmitigated)**:
   - *Limitation*: Multiple judges communicating offline can coordinate their evaluations to artificially inflate or deflate specific teams.
   - *Residual Risk*: While Z-score normalization and Bayesian shrinkage pull extreme outliers toward the global mean, consensus collusion between multiple judges on a single panel cannot be mathematically eliminated without cross-panel replication.

3. **Multi-Device / Distributed Sybil Networks (Partially Mitigated)**:
   - *Limitation*: Sophisticated voter syndicates operating across distinct residential proxies or real mobile devices with unique IP addresses cannot be completely separated from organic public voters without invasive KYC or identity verification.

4. **Superuser Database Access (Out of Scope)**:
   - *Limitation*: PostgreSQL RLS is enforced on application roles (`dogfood_app`). A compromised database superuser (`postgres` / `dogfood_admin`) bypasses RLS policies entirely.
   - *Mitigation*: Application services connect exclusively via least-privilege schema-restricted credentials.
