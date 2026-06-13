# CricZone

This repo now has a split setup:

- Spring Boot backend in the repo root
- React frontend in `frontend/`

The frontend talks to the Spring API over `/api`.

## Run backend

```powershell
./mvnw spring-boot:run
```

Backend default:
- `http://localhost:8080`
- API base: `http://localhost:8080/api`

## Run frontend

```powershell
cd frontend
npm install
npm run dev
```

Frontend default:
- `http://localhost:5173`
- Set `VITE_API_BASE_URL` in `frontend/.env` if your backend is elsewhere

## Notes

- The Spring controllers are already split by domain: users, teams, matches, tournaments, turfs, bookings, posts, leaderboard, and system health.
- The React app is a separate IDE-friendly project, so you can run it independently from the backend.
- The old static frontend still exists under `src/main/resources/static` as a legacy fallback, but the new React app is the intended UI.
