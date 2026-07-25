# CricZone

CricZone is now split into two independently deployable apps:

- `frontend/`: Static PWA UI for players, teams, matches, tournaments, turf booking, and billing.
- `backend/`: Spring Boot 2.7 API with MongoDB, JWT auth, OpenAPI docs, and optional Kafka booking workflow projections.

## Repository Structure

```text
frontend/   Static HTML/CSS/JS app and deployment config
backend/    Spring Boot API, Maven wrapper, backend docs, and Kafka compose file
.github/    CI for backend tests and frontend static build
```

## Run Backend

```powershell
cd backend
$env:JWT_SECRET="replace-with-at-least-32-random-bytes"
.\mvnw spring-boot:run
```

Backend URLs:

- API: `http://localhost:8080/api`
- Health: `http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

The default MongoDB connection is `mongodb://localhost:27017/criczone`. Override it with `MONGO_URI`.
If the URI does not include a database path, set `MONGO_DATABASE=criczone`.

## Run Frontend

```powershell
cd frontend
npm run dev
```

Frontend URL:

- App: `http://localhost:3000`

The frontend reads its API URL from `frontend/runtime-config.js`:

```js
window.__API_BASE__ = "http://localhost:8080/api";
```

For production frontend deployment from GitHub/Vercel, set `API_BASE_URL` in
the frontend Vercel project. The build writes that value into `dist/runtime-config.js`.

## Separate Deployment

Backend deployment:

1. Create a backend project with root directory `backend`.
2. Use the Dockerfile/container runtime when available.
3. Set `JWT_SECRET`, `MONGO_URI`, `MONGO_DATABASE`, `CLIENT_URL`, `ALLOW_ALL_ORIGINS=false`, and `KAFKA_ENABLED=false`.
4. Add Kafka env vars only if booking workflow projections are enabled.

Frontend deployment:

1. Create a frontend project with root directory `frontend`.
2. Set `API_BASE_URL` to the deployed backend API URL, ending in `/api`.
3. Run `npm run build` from `frontend/`.
4. Deploy `frontend/dist/` to a static host.

## Tests

```powershell
cd backend
.\mvnw test

cd ..\frontend
npm run build
```

CI runs both checks on every push and pull request.
