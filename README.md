# ?? Dogfood: Production-Grade Hackathon Platform

Dogfood is an open-source, self-hostable hackathon submission and judging portal built for scale and integrity.

## Tiers Achieved
- **Tier 1 (Core):** JWT Auth, Event/Team Management, Submission Engine with Redis distributed locks.
- **Tier 2 (Judging Integrity):** Z-Score Normalization, Bayesian Shrinkage, Row-Level Security isolation, RabbitMQ Audit Trail.
- **Tier 3 (Public Voting):** Quadratic Voting Engine, Redis Token-Bucket Rate Limiting.
- **Tier 4 (Extensibility):** Webhook Dispatcher (HMAC-SHA256), Ed25519 Cryptographically Signed PDFs (Apache PDFBox).

## Documentation
- [Architecture](ARCHITECTURE.md)
- [Data Model](DATA-MODEL.md)
- [Judging Mathematics](JUDGING.md)
- [Threat Model](THREAT-MODEL.md)

## Tech Stack
- **Backend:** Java 21, Spring Boot 3.3, Spring Cloud Gateway
- **Frontend:** React 18, Vite, Mantine UI, React Router
- **Infrastructure:** PostgreSQL 15 (Multi-Schema + RLS), Redis, RabbitMQ, Docker Compose
