# CricZone Interview Notes

## Why Event Sourcing for Bookings

Bookings are the highest-risk workflow in CricZone because they involve money, availability, and potential double-booking conflicts. The main `bookings` document is still the source of truth for current state, but every lifecycle transition also publishes a `BookingWorkflowEvent` so the platform has an audit trail.

The snapshot collection exists because most product screens do not need to replay the full event history. They need the current workflow state quickly: booking status, payment status, owner, user, amount, and event count. The event log keeps the truth of what happened; the snapshot keeps reads fast.

Idempotency is handled with `eventId`. If Kafka redelivers an event, or a producer retries after a network interruption, the consumer checks `existsByEventId` and skips duplicates. That protects the audit log from repeated entries and prevents snapshot counters/state from drifting.

## Kafka Optional vs Always On

Kafka is feature-flagged with `KAFKA_ENABLED` so local development stays simple. A reviewer can run the core booking app with only MongoDB, then turn Kafka on when they want to demo the event-sourced workflow.

The tradeoff is consistency. With Kafka disabled, bookings still work, but workflow projections are not produced. In a production environment, Kafka should be treated as required infrastructure for complete auditability. The optional mode is a developer-experience compromise, not the strongest consistency mode.

## JWT and Auth Choices

The app now fails fast if `JWT_SECRET` is missing, placeholder, or resolves to fewer than 32 bytes. That is intentional: a weak JWT signing key should be a startup error, not a quiet production risk.

The auth filter caches resolved users briefly so every authenticated request does not require a MongoDB lookup. JWT claims carry the minimal identity shape, while endpoints still receive a `UserDocument` for code compatibility. At higher scale, the next step is to move more hot-path authorization data into JWT claims and only fetch the user document when an endpoint needs fresh profile data.

## What Changes at 10x Scale

- Use an outbox pattern for booking events instead of publishing directly from the request thread. That would make booking writes and event publication atomic from the application perspective.
- Make Kafka mandatory for production booking state changes and alert if the consumer lag grows.
- Add a unique slot constraint or transactional booking guard around turf/date/time ranges. The current conflict check is clear but should become database-enforced at high concurrency.
- Move billing/payment integration behind a dedicated payment provider abstraction and verify webhook idempotency separately from booking workflow idempotency.
- Split read-heavy projections by owner/user access patterns and add indexes around `ownerId`, `userId`, `updatedAt`, and workflow status.
- Replace in-memory auth rate limiting with Redis or an edge/WAF limiter so limits are consistent across app instances.
- Store password reset tokens in MongoDB or Redis with hashed token values instead of in-memory cache when running multiple instances.
- Keep OpenAPI and CI required on every PR so API drift is visible before review.
