# System Architecture

Dogfood is a production-grade, self-hostable hackathon platform built on a distributed microservices architecture. 
Instead of the operational overhead of Kubernetes, the platform is designed to be easily deployable via a single docker compose up command.

## Core Components
The system is composed of an API Gateway and 10 distinct microservices:

1. **API Gateway (Spring Cloud Gateway):** Central ingress point handling JWT validation, routing, and rate-limiting.
2. **Identity Service:** Issues RS256 JWTs and manages user profiles and authentication.
3. **Event Service:** Manages hackathon metadata, tracks, and dynamic eligibility rules (JSONB).
4. **Submission Service:** Handles project uploads with distributed Redis locks for strict deadline enforcement.
5. **Judging Service:** Manages rubrics, COI declarations, and mathematically normalizes scores using Bayesian shrinkage.
6. **Voting Service:** Implements Quadratic Voting with Redis token-bucket rate limiting to prevent Sybil attacks.
7. **Certificate Service:** Generates cryptographically signed PDFs using Ed25519 and Apache PDFBox.
8. **Webhook Dispatcher:** Delivers HMAC-SHA256 signed event payloads to external systems.
9. **Observability Service:** Subscribes to the RabbitMQ firehose to maintain a tamper-proof audit trail.
10. **Notification Service:** Dispatches transactional emails, Discord, and Slack alerts for critical events.
11. **Asset Service:** Manages secure file uploads (resumes, project assets) to S3-compatible storage with pre-signed URLs.

## Communication & Messaging
- **Synchronous:** REST over HTTP/1.1 for direct client-to-service interactions (via Gateway).
- **Asynchronous:** RabbitMQ Topic Exchanges for decoupled event broadcasting.

### RabbitMQ Exchange Layout
We use a primary topic exchange named `dogfood.events`. Services bind queues to this exchange using routing keys.
* **Exchange:** `dogfood.events` (Type: `topic`)
* **Queues and Bindings:**
  * `audit.queue` bound to `*.*.created` and `*.*.updated`
  * `certificate.queue` bound to `hackathon.ended`
  * `webhook.queue` bound to `#` (receives all events for user-configured webhooks)
  * `notification.queue` bound to `submission.received` and `judging.completed`

## Security & Data Isolation
### Postgres Row-Level Security (RLS)
For defense-in-depth, we implement Postgres Row-Level Security (RLS) to ensure strict role isolation at the database level.
Even if an application vulnerability occurs, queries cannot access unauthorized data. We inject the user's role and ID from the JWT into the Postgres session variables:
```sql
CREATE POLICY user_isolation_policy ON submissions
    USING (user_id = current_setting('jwt.claims.user_id')::uuid 
           OR current_setting('jwt.claims.role') = 'ADMIN');
```
This guarantees that participants can only view their own submissions, while judges and admins have appropriate broader access.

## Resilience4j Circuit Breaker
To prevent cascading failures across our distributed system, we implement the Resilience4j Circuit Breaker pattern.
* **Rationale:** If a synchronous downstream service (like Identity or Event) experiences high latency or downtime, the Circuit Breaker opens. This fails fast, freeing up threads in the Gateway and calling services rather than hanging and causing resource exhaustion.
* **Implementation:** Configured with a sliding window. If the failure rate exceeds 50% or latency exceeds 2 seconds, the circuit opens. Fallback methods return cached data or default error responses (e.g., returning a 503 Service Unavailable gracefully) until the circuit transitions to half-open and eventually closed when health is restored.
