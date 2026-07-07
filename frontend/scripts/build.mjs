import { cpSync, existsSync, mkdirSync, rmSync } from "node:fs";
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

console.log("Static frontend built into dist/");
