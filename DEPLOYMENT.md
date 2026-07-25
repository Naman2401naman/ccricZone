# CricZone Deployment Notes

## Frontend

The frontend is a static app in `frontend/`.

Deploy from `frontend/`:

```powershell
vercel deploy --prod
```

Vercel runs `npm run build` and publishes `dist/`, configured in `frontend/vercel.json`.

## Backend

Do not deploy the Spring Boot backend as a normal Vercel static/serverless
project. The current Vercel backend URL is serving HTML for `/api/health`,
which means the frontend is not talking to the Spring API. Deploy the backend
as a long-running Docker web service, for example on Render or Railway.

The backend no longer crashes during startup when `JWT_SECRET` is missing, so
`/api/health` can still report the configuration problem. Authentication and
token creation still require `JWT_SECRET` to be a real secret of at least 32
bytes.

### Recommended: Render

This repo includes `render.yaml` for the backend. In Render:

1. Create a new Blueprint from this repo, or create a new Web Service manually.
2. If creating manually, use:
   - Root Directory: `backend`
   - Runtime: `Docker`
   - Dockerfile Path: `./Dockerfile`
   - Health Check Path: `/api/health`
3. Add production environment variables:
   - `MONGO_URI`: MongoDB Atlas connection string
   - `MONGO_DATABASE`: database name, for example `criczone`
   - `JWT_SECRET`: at least 32 random bytes/characters
   - `CLIENT_URL`: your frontend URL, for example `https://your-frontend.vercel.app`
   - `ALLOW_ALL_ORIGINS`: `false`
   - `KAFKA_ENABLED`: `false`
4. Deploy and open:

```text
https://YOUR-BACKEND.onrender.com/api/health
```

MongoDB is working only when the response includes:

```json
{
  "success": true,
  "status": "ok",
  "mongodb": "connected"
}
```

If it says `mongodb: "disconnected"`, fix the Atlas connection string and
Atlas Network Access rules, then redeploy.

If it says `jwtSecret: "missing-or-too-short"`, add or fix the `JWT_SECRET`
environment variable and redeploy. A valid value can be any random string of at
least 32 bytes.

### Vercel Note

The backend is a Spring Boot app in `backend/`. If you deploy it on Vercel from
GitHub, create a separate Vercel project for the backend and set:

- Root Directory: `backend`
- Framework Preset: Other
- Runtime/Framework: Docker or Container, if Vercel shows that option
- Build Command: leave empty
- Output Directory: leave empty
- Install Command: leave empty

The backend includes `backend/Dockerfile`. Prefer Render/Railway for this
long-running API if Vercel keeps returning serverless function errors.

Required Vercel production environment variables:

- `MONGO_URI`
- `MONGO_DATABASE`
- `JWT_SECRET`
- `CLIENT_URL`
- `ALLOW_ALL_ORIGINS`
- `KAFKA_ENABLED`

Important: the Atlas URI must either include a database path, such as
`mongodb+srv://USER:PASSWORD@cluster.example.mongodb.net/criczone?retryWrites=true&w=majority`,
or you must set `MONGO_DATABASE=criczone`. Without a database name Spring Boot
fails with `Database name must not be empty`.

## Connect Frontend To Backend

After the backend is live, deploy the frontend as a separate Vercel project:

- Root Directory: `frontend`
- Framework Preset: Other
- Build Command: `npm run build`
- Output Directory: `dist`
- Install Command: `npm install`

Set this frontend production environment variable:

- `API_BASE_URL`: your backend API URL, for example `https://YOUR-BACKEND.vercel.app/api`

If `API_BASE_URL` is not set, the build uses `frontend/runtime-config.js`.
For local testing, edit that file directly:

```js
window.__API_BASE__ = "http://localhost:8080/api";
```

## Previous Blocker

Backend deployments were blocked by Vercel team protection because the latest Git commit was authored by an email that did not have access to the Vercel team. The fix is to deploy from a commit authored by the Vercel account owner or add the commit author to the Vercel team.
