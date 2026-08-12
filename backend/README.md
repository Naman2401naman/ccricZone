# CricZone Backend

Spring Boot API for CricZone.

## Requirements

- Java 11+
- MongoDB running locally or `MONGO_URI`
- `JWT_SECRET` with at least 32 bytes of entropy
- Optional: Docker for the Kafka booking workflow demo

Copy `.env.example` into your deploy platform settings and replace the sample values.

## Run Locally

```powershell
$env:JWT_SECRET="replace-with-at-least-32-random-bytes"
.\mvnw spring-boot:run
```

URLs:

- API base: `http://localhost:8080/api`
- Health: `http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Frontend CORS

The backend allows common local frontend origins by default, including:

- `http://localhost:3000`
- `http://localhost:5173`
- `http://localhost:5500`

For production, set `CLIENT_URL` to the deployed frontend origin:

```powershell
$env:CLIENT_URL="https://your-frontend.example.com"
```

## Kafka Booking Workflow

Start Kafka from this `backend/` folder:

```powershell
docker compose -f docker-compose.kafka.yml up -d
```

Run with Kafka enabled:

```powershell
$env:KAFKA_ENABLED="true"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
$env:JWT_SECRET="replace-with-at-least-32-random-bytes"
.\mvnw spring-boot:run
```

Workflow endpoints:

- `GET /api/bookings/{bookingId}/workflow`
- `GET /api/bookings/{bookingId}/history`
- `GET /api/bookings/workflow/summary`

## Team Segregation Analysis Model

The backend includes a lightweight Java model for team performance analysis. It trains from completed match history, learns feature weights for win rate, run difference, wicket difference, and experience, then uses those weights to rank and segregate teams into balanced groups.

Endpoints:

- `POST /api/team-segregation/train`: train/analyze from completed matches.
- `GET /api/team-segregation/performance?venue=Arena Turf`: list ranked team performance for a turf.
- `POST /api/team-segregation`: create balanced team groups using the trained scoring model.

Request body:

```json
{
  "teamIds": ["teamId1", "teamId2", "teamId3", "teamId4"],
  "venue": "Arena Turf",
  "bucketCount": 2,
  "minimumMatches": 0
}
```

## Technical Stack

- Java 11 and Spring Boot 2.7 for the REST API.
- Spring Web for controllers and JSON APIs.
- Spring Data MongoDB for users, teams, matches, turfs, bookings, posts, and tournaments.
- Spring Security with JWT authentication for protected APIs.
- Spring Validation for request DTO validation.
- Caffeine/Spring Cache for read-heavy endpoints.
- Springdoc OpenAPI/Swagger UI for API documentation.
- Optional Spring Kafka workflow for booking lifecycle projections.
- Maven for dependency management, builds, and tests.

## Tests

```powershell
.\mvnw test
```

## Deployment

Deploy this folder as a Docker web service. See [../DEPLOYMENT.md](../DEPLOYMENT.md) for the Render setup and required environment variables.
