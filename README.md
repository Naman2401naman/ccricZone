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

For production frontend deployment, change that value to your deployed backend API URL.

## Separate Deployment

Backend deployment:

1. Deploy the `backend/` folder as the Spring Boot service.
2. Set `JWT_SECRET`, `MONGO_URI`, and `CLIENT_URL`.
3. Add Kafka env vars only if booking workflow projections are enabled.

Frontend deployment:

1. Edit `frontend/runtime-config.js` to point at the deployed backend.
2. Run `npm run build` from `frontend/`.
3. Deploy `frontend/dist/` to a static host.

## Tests

```powershell
cd backend
.\mvnw test

cd ..\frontend
npm run build
```

CI runs both checks on every push and pull request.
