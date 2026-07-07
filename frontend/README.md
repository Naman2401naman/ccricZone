# CricZone Frontend

Standalone static frontend for CricZone.

## Run Locally

```powershell
npm run dev
```

The frontend runs at `http://localhost:3000` and calls the backend at
`http://localhost:8080/api` by default.

## Configure API URL

Edit `runtime-config.js` for each environment:

```js
window.__API_BASE__ = "https://your-backend.example.com/api";
```

Because this is a runtime file, a static host can replace it without rebuilding
the rest of the frontend.

## Build For Static Hosting

```powershell
npm run build
```

Deploy the generated `dist/` folder to any static host. The app expects to be
served from the domain root so the service worker and manifest paths resolve
correctly.
