# CricZone Deployment Guide

Deploy the backend and frontend as two separate services.

## 1. Prepare MongoDB Atlas

1. Create a free Atlas cluster.
2. Create a database user and password.
3. In Network Access, allow your deploy platform. For a student/demo project you can use `0.0.0.0/0`.
4. Copy the connection string and include a database name:

```text
mongodb+srv://USER:PASSWORD@cluster.example.mongodb.net/criczone?retryWrites=true&w=majority
```

## 2. Deploy Backend On Render

Recommended: use Render as a Docker web service. The repository already includes `render.yaml`.

Manual Render settings:

- Root Directory: `backend`
- Runtime: `Docker`
- Dockerfile Path: `./Dockerfile`
- Health Check Path: `/api/health`

Backend environment variables:

```text
MONGO_URI=mongodb+srv://USER:PASSWORD@cluster.example.mongodb.net/criczone?retryWrites=true&w=majority
MONGO_DATABASE=criczone
JWT_SECRET=replace-with-at-least-32-random-characters
CLIENT_URL=https://your-frontend.vercel.app
ALLOW_ALL_ORIGINS=false
KAFKA_ENABLED=false
```

After deployment, test:

```text
https://YOUR-BACKEND.onrender.com/api/health
```

Expected important fields:

```json
{
  "success": true,
  "status": "ok",
  "mongodb": "connected",
  "jwtSecret": "configured"
}
```

If MongoDB is disconnected, check the Atlas password, database name, and Network Access rules.

## 3. Deploy Frontend On Vercel

Create a separate Vercel project for `frontend/`.

Vercel settings:

- Root Directory: `frontend`
- Framework Preset: Other
- Install Command: `npm install`
- Build Command: `npm run build`
- Output Directory: `dist`

Frontend environment variable:

```text
API_BASE_URL=https://YOUR-BACKEND.onrender.com/api
```

Deploy, then update the backend `CLIENT_URL` to the real Vercel frontend URL and redeploy the backend.

## 4. Local Verification Before Upload

Backend:

```powershell
cd backend
.\mvnw test
```

Frontend:

```powershell
cd frontend
npm run build
```

## Notes

- Do not deploy the Spring Boot backend as a normal Vercel static/serverless project. Use Render, Railway, or another long-running container host.
- Keep `KAFKA_ENABLED=false` unless you also deploy Kafka and configure `KAFKA_BOOTSTRAP_SERVERS`.
- `frontend/runtime-config.js` is only the local fallback. In production, Vercel writes `API_BASE_URL` into `dist/runtime-config.js` during build.
