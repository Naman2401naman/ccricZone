import { createReadStream, existsSync, statSync } from "node:fs";
import { createServer } from "node:http";
import { extname, join, normalize, resolve, sep } from "node:path";

const root = resolve(process.argv[2] || ".");
const port = Number(process.argv[3] || process.env.PORT || 3000);

const contentTypes = new Map([
  [".css", "text/css; charset=utf-8"],
  [".html", "text/html; charset=utf-8"],
  [".ico", "image/x-icon"],
  [".js", "text/javascript; charset=utf-8"],
  [".json", "application/json; charset=utf-8"],
  [".png", "image/png"],
  [".svg", "image/svg+xml"],
  [".webmanifest", "application/manifest+json; charset=utf-8"]
]);

const resolveFile = (urlPath) => {
  const pathname = decodeURIComponent(new URL(urlPath, "http://localhost").pathname);
  const normalizedPath = normalize(pathname).replace(/^([/\\])+/, "");
  const requestedPath = resolve(root, normalizedPath);
  if (requestedPath !== root && !requestedPath.startsWith(`${root}${sep}`)) {
    return null;
  }

  if (existsSync(requestedPath) && statSync(requestedPath).isFile()) {
    return requestedPath;
  }

  return join(root, "index.html");
};

createServer((request, response) => {
  const filePath = resolveFile(request.url || "/");
  if (!filePath || !existsSync(filePath)) {
    response.writeHead(404);
    response.end("Not found");
    return;
  }

  response.writeHead(200, {
    "Content-Type": contentTypes.get(extname(filePath)) || "application/octet-stream"
  });
  createReadStream(filePath).pipe(response);
}).listen(port, () => {
  console.log(`CricZone frontend running at http://localhost:${port}`);
});
