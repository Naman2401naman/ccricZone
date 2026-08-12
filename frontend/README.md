# CricZone Frontend

Standalone static frontend for CricZone.

## Run Locally

```powershell
npm run dev
```

The frontend runs at `http://localhost:3000` and calls the backend at
`http://localhost:8080/api` by default.

## Configure API URL

For local development, edit `runtime-config.js` if your backend is not running on `http://localhost:8080/api`:

```js
window.__API_BASE__ = "https://your-backend.example.com/api";
```

Because this is a runtime file, a static host can replace it without rebuilding
the rest of the frontend.

For production builds, set `API_BASE_URL` in the hosting platform instead of editing source files.

## Build For Static Hosting

```powershell
npm run build
```

Deploy the generated `dist/` folder to any static host. The app expects to be
served from the domain root so the service worker and manifest paths resolve
correctly.

For Vercel, use `npm run build` as the build command and `dist` as the output directory.
