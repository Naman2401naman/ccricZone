import { cpSync, existsSync, mkdirSync, rmSync, writeFileSync } from "node:fs";
import { join, resolve } from "node:path";

const root = resolve(".");
const dist = join(root, "dist");
const requiredFiles = ["index.html", "runtime-config.js", "styles.css", "sw.js", "manifest.webmanifest"];

for (const file of requiredFiles) {
  if (!existsSync(join(root, file))) {
    throw new Error(`Missing required frontend file: ${file}`);
  }
}

rmSync(dist, { recursive: true, force: true });
mkdirSync(dist, { recursive: true });

for (const entry of ["index.html", "runtime-config.js", "styles.css", "sw.js", "manifest.webmanifest", "favicon.ico.png", "icons", "js"]) {
  cpSync(join(root, entry), join(dist, entry), { recursive: true });
}

const configuredApiBase = String(process.env.API_BASE_URL || process.env.VITE_API_BASE_URL || "").trim();
if (configuredApiBase) {
  const normalizedApiBase = configuredApiBase.replace(/\/+$/, "");
  writeFileSync(
    join(dist, "runtime-config.js"),
    `// Generated during build. Set API_BASE_URL in the hosting platform to change this.\nwindow.__API_BASE__ = ${JSON.stringify(normalizedApiBase)};\n`
  );
  console.log(`Runtime API base set to ${normalizedApiBase}`);
}

console.log("Static frontend built into dist/");
