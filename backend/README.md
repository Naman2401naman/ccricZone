# CricZone Backend

Spring Boot API for CricZone.

## Requirements

- Java 11+
- MongoDB running locally or `MONGO_URI`
- `JWT_SECRET` with at least 32 bytes of entropy
- Optional: Docker for the Kafka booking workflow demo

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

## Tests

```powershell
.\mvnw test
```
