# CricZone

CricZone is a clean two-part project:

- `backend/`: Spring Boot 2.7 REST API with MongoDB, JWT auth, OpenAPI docs, caching, and optional Kafka booking workflow events.
- `frontend/`: Static HTML/CSS/JS PWA for players, teams, matches, tournaments, turfs, and bookings.

## Local Run

Backend:

```powershell
cd backend
$env:MONGO_URI="mongodb://localhost:27017/criczone"
$env:MONGO_DATABASE="criczone"
$env:JWT_SECRET="replace-with-at-least-32-random-characters"
.\mvnw spring-boot:run
```

Frontend:

```powershell
cd frontend
npm run dev
```

URLs:

- Frontend: `http://localhost:3000`
- API health: `http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Production

Use [DEPLOYMENT.md](DEPLOYMENT.md) for the full deployment flow. Recommended setup:

- MongoDB Atlas for database.
- Render or Railway for `backend/` as a Docker web service.
- Vercel for `frontend/` as a static app.

## Verification

```powershell
cd backend
.\mvnw test

cd ..\frontend
npm run build
```
