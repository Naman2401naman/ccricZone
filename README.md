# CricZone

CricZone is a Spring Boot 2.7 + MongoDB cricket platform for player discovery,
match scoring, tournament management, turf booking, billing, and a Kafka-backed
booking workflow projection.

The production UI is served by Spring Boot from `src/main/resources/static`.
There is no separate committed frontend build step.

## Requirements

- Java 11+
- MongoDB running locally or a `MONGO_URI`
- `JWT_SECRET` with at least 32 bytes of entropy
- Optional: Docker for the Kafka booking workflow demo

## Run Locally

```powershell
$env:JWT_SECRET="replace-with-at-least-32-random-bytes"
.\mvnw spring-boot:run
```

Backend and UI:

- App: `http://localhost:8080`
- API base: `http://localhost:8080/api`
- Health: `http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

The default MongoDB connection is:

```text
mongodb://localhost:27017/criczone
```

Override it with:

```powershell
$env:MONGO_URI="mongodb://localhost:27017/criczone"
$env:JWT_SECRET="replace-with-at-least-32-random-bytes"
.\mvnw spring-boot:run
```

## Run With Event Sourcing Enabled

Start Kafka:

```powershell
docker compose -f docker-compose.kafka.yml up -d
```

Run Spring with the Kafka workflow publisher and consumer enabled:

```powershell
$env:KAFKA_ENABLED="true"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
$env:JWT_SECRET="replace-with-at-least-32-random-bytes"
.\mvnw spring-boot:run
```

Then create or update a booking and inspect its workflow timeline:

```text
GET /api/bookings/{bookingId}/workflow
GET /api/bookings/{bookingId}/history
GET /api/bookings/workflow/summary
```

## API Surface

The API is grouped by domain:

- `/api/users`
- `/api/teams`
- `/api/matches`
- `/api/tournaments`
- `/api/turfs`
- `/api/bookings`
- `/api/posts`
- `/api/leaderboard`
- `/api/health`
- `/api/version`

OpenAPI documentation is available at `/swagger-ui.html` and `/v3/api-docs`.

## Tests

```powershell
.\mvnw test
```
