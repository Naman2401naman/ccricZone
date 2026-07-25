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

### Vercel Note

The backend is a Spring Boot app in `backend/`, but Vercel is not the right
target for this long-running API in its current form.

Deploy from `backend/`:

```powershell
vercel deploy --prod
```

The backend includes Dockerfiles, but the current deployed Vercel URL is not
running the API. Prefer Render/Railway for this backend.

Required Vercel production environment variables:

- `MONGO_URI`
- `JWT_SECRET`
- `CLIENT_URL`
- `ALLOW_ALL_ORIGINS`

## Connect Frontend To Backend

After the backend is live, edit `frontend/runtime-config.js`:

```js
window.__API_BASE__ = "https://YOUR-BACKEND.onrender.com/api";
```

Then rebuild and redeploy the frontend:

```powershell
cd frontend
npm.cmd run build
vercel deploy --prod
```

## Previous Blocker

Backend deployments were blocked by Vercel team protection because the latest Git commit was authored by an email that did not have access to the Vercel team. The fix is to deploy from a commit authored by the Vercel account owner or add the commit author to the Vercel team.
