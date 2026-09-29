# Data Model & Storage Strategy

## The Single-DB / Multi-Schema Pattern
To balance microservice isolation with self-hosted operational simplicity, Dogfood uses a **single PostgreSQL instance** where each microservice owns a dedicated logical schema (e.g., identity, event, judging, udit).
- Services cannot cross-query schemas. 
- All cross-domain data exchange happens via REST or RabbitMQ.

## Schema-on-Write Flexibility
Hackathons require highly dynamic data (custom registration questions, dynamic team eligibility rules, flexible rubrics).
Instead of brittle EAV (Entity-Attribute-Value) tables, Dogfood heavily utilizes PostgreSQL's JSONB columns combined with Hibernate's @JdbcTypeCode(SqlTypes.JSON).
- Example: eligibilityRules in the Event service is a JSONB array processed dynamically by the Strategy Pattern in Java.

## Row-Level Security (RLS)
The judging schema enforces strict data isolation directly at the database engine level using Postgres RLS.
Even if a Java controller has a bug, the database forcibly applies:
`judge_id = current_setting('app.current_judge_id', true)::uuid`
This guarantees judges can only view and modify their own scores.

## Import and Export Paths
Organizers are never locked in. Dogfood supports comprehensive data portability:
- **CSV Export:** Real-time CSV exports are available for both Submissions (`GET /api/events/{eventId}/submissions/export`) and Judging Results (`GET /api/events/{eventId}/results/export`).
- **Bulk Export (T4):** An aggregated JSON payload representing the entire event state, including tracks, team configurations, and score distributions, can be extracted via the Webhook Dispatcher or direct API request.
- **Bulk Import (T4):** You can bootstrap an event by providing a `fixtures.json` to the seed script or sending an initial `POST` payload to the bulk-import endpoint. The backend will parse the payload and populate the `event`, `submission`, and `identity` schemas automatically.
