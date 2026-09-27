# Threat Model

This document outlines the primary attack vectors mitigated by the Dogfood platform architecture.

## 1. Authorization Bypasses
- **Threat:** A user attempts to access the Organizer dashboard.
- **Mitigation:** The API Gateway intercepts all requests and validates the RS256 JWT signature against the Identity Service's JWKS endpoint. Roles are baked into the JWT payload (eventRoles: ["event-123:ORGANIZER"]) so they cannot be spoofed.

## 2. Cross-Tenant Data Leakage
- **Threat:** A judge manipulates an API parameter to view another judge's raw scores.
- **Mitigation:** Database-enforced PostgreSQL Row-Level Security (RLS) ensures that the database engine itself drops any rows not matching the authenticated judge's UUID context.

## 3. Ballot Stuffing & Sybil Attacks
- **Threat:** Bots spam the public voting endpoints.
- **Mitigation:** The Voting service implements a Token-Bucket Rate Limiter backed by Redis. Public endpoints utilize strict IP + User fingerprinting to cap the quadratic vote budget.

## 4. Deadline Manipulation
- **Threat:** A team attempts to submit a project 5 seconds after the deadline by abusing race conditions.
- **Mitigation:** The Submission Service utilizes distributed Redis locks to enforce strict cutoffs.
