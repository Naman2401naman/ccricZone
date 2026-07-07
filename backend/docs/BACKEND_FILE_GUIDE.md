# CricZone Spring Backend File Guide

This document explains what each important file in the `backend/` Spring Boot module does.

## Entry Point

- `src/main/java/com/criczone/demo/DemoApplication.java`: Starts the Spring Boot application and enables Spring cache support.

## API Controllers

- `api/BookingController.java`: Handles turf booking creation, cancellation, billing updates, CSV exports, and booking workflow inspection endpoints.
- `api/LeaderboardController.java`: Exposes public leaderboard endpoints for batsmen, bowlers, and all-rounders.
- `api/MatchController.java`: Manages match creation, live score updates, toss handling, completion, reporting, and highlights.
- `api/PostController.java`: Supports social feed posts, likes, comments, and per-user post lookups.
- `api/SystemController.java`: Exposes health and version endpoints and checks MongoDB connectivity.
- `api/TeamController.java`: Creates and manages teams, members, invitations, suggestions, and team randomization.
- `api/TournamentController.java`: Handles tournament creation, registrations, fixture generation, standings, playoffs, and status changes.
- `api/TurfController.java`: Manages turf creation, lookup, nearby search, owner inventory, updates, and deletes.
- `api/UserController.java`: Handles registration, login, profile updates, player search, public player views, leaderboards, and follow/unfollow flows.

## Configuration

- `config/AppProperties.java`: Binds custom app settings for JWT, cache, Kafka workflow, and allowed client origins.
- `config/CacheConfig.java`: Creates the Caffeine cache manager and registers named caches used by read-heavy endpoints.
- `config/CacheNames.java`: Central list of cache region names to keep cache annotations consistent.
- `config/KafkaWorkflowConfig.java`: Creates Kafka producer and consumer factories plus the listener container used by booking workflow events.
- `config/SecurityConfig.java`: Configures stateless Spring Security, JWT auth, public routes, CORS, and password encoding.

## Domain Models

- `domain/BookingDocument.java`: MongoDB booking document storing slot timing, status, billing, and audit timestamps.
- `domain/BookingWorkflowEventDocument.java`: MongoDB audit record for each booking event consumed from Kafka.
- `domain/BookingWorkflowSnapshotDocument.java`: MongoDB projection that stores the latest workflow state for each booking.
- `domain/MatchDocument.java`: MongoDB match document containing innings, scorecard, ball-by-ball, analytics, and metadata fields.
- `domain/PostDocument.java`: MongoDB post document containing content, likes, comments, and timestamps.
- `domain/TeamDocument.java`: MongoDB team document storing owner, members, tournament link, and basic stats.
- `domain/TournamentDocument.java`: MongoDB tournament document storing teams, schedule, standings, knockout data, media, and status.
- `domain/TurfDocument.java`: MongoDB turf document containing owner, location, amenities, pricing, and active state.
- `domain/UserDocument.java`: MongoDB user document containing identity, profile, rankings, stats, teams, tournaments, and social data.

## Repositories

- `repo/BookingRepository.java`: Mongo repository for bookings with helpers for user history and slot conflict checks.
- `repo/BookingWorkflowEventRepository.java`: Reads booking workflow event timelines and prevents duplicate event persistence by `eventId`.
- `repo/BookingWorkflowSnapshotRepository.java`: Reads the latest booking workflow view for owners and users.
- `repo/MatchRepository.java`: Mongo repository for match persistence and creator-scoped lookup.
- `repo/PostRepository.java`: Mongo repository for posts and per-user feed queries.
- `repo/TeamRepository.java`: Mongo repository for teams and owner-scoped lookup.
- `repo/TournamentRepository.java`: Mongo repository for tournaments and status-based lookup.
- `repo/TurfRepository.java`: Mongo repository for turf lookup by owner and active state.
- `repo/UserRepository.java`: Mongo repository for users with email and phone uniqueness lookups.

## Security

- `security/AuthUser.java`: Lightweight authenticated principal used by Spring Security after JWT validation.
- `security/JwtAuthFilter.java`: Reads bearer tokens, validates them, and attaches the current user to the request.
- `security/JwtService.java`: Generates and parses JWT tokens using the configured signing secret.

## Support

- `support/ApiException.java`: Custom runtime exception that carries an HTTP status code.
- `support/ApiSupport.java`: Shared helpers for validation, parsing, deep merge, role checks, map building, and public user shaping.
- `support/GlobalExceptionHandler.java`: Converts exceptions into consistent JSON error responses and includes request ids.
- `support/RequestTracingFilter.java`: Adds `X-Request-Id`, logs request latency/status, and exposes request ids to handlers.

## Booking Kafka Workflow

- `workflow/booking/BookingWorkflowConsumer.java`: Kafka consumer that receives booking lifecycle events and sends them into Mongo projections.
- `workflow/booking/BookingWorkflowEvent.java`: Event payload sent through Kafka for booking workflow processing.
- `workflow/booking/BookingWorkflowEventType.java`: Enum of supported booking lifecycle event types.
- `workflow/booking/BookingWorkflowProjectionService.java`: Persists workflow events and updates the latest booking snapshot.
- `workflow/booking/BookingWorkflowPublisher.java`: Interface used by controllers so booking APIs do not depend directly on Kafka.
- `workflow/booking/KafkaBookingWorkflowPublisher.java`: Real Kafka-backed publisher used when Kafka is enabled.
- `workflow/booking/NoOpBookingWorkflowPublisher.java`: Safe fallback publisher used when Kafka is disabled.

## Resources

- `src/main/resources/application.properties`: Runtime configuration for server port, Mongo, JWT, cache, and Kafka settings.

## Frontend Boundary

The frontend is intentionally outside this backend module at the repo-level `frontend/` folder. The backend exposes JSON APIs, OpenAPI docs, health checks, and CORS for approved frontend origins. It no longer serves the single-page app from `src/main/resources/static`.

## Local Utility Files

- `pom.xml`: Maven build file for the Spring backend module.
- `mvnw` and `mvnw.cmd`: Maven wrapper launchers.
- `docker-compose.kafka.yml`: Optional local Kafka broker for the booking workflow demo.
