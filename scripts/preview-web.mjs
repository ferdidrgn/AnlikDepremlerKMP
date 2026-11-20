import { createServer } from "node:http";
import { existsSync, readFileSync, statSync } from "node:fs";
import { dirname, extname, join, normalize, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const productionRoot = join(repoRoot, "composeApp/build/dist/wasmJs/productionExecutable");
const sourceRoot = join(repoRoot, "composeApp/src/wasmJsMain/resources");
const root = existsSync(join(productionRoot, "composeApp.js")) ? productionRoot : sourceRoot;
const port = Number(process.env.PORT || 4173);
const host = process.env.HOST || "0.0.0.0";
const mimeTypes = {
  ".css": "text/css; charset=utf-8",
  ".html": "text/html; charset=utf-8",
  ".ico": "image/x-icon",
  ".js": "text/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".mjs": "text/javascript; charset=utf-8",
  ".png": "image/png",
  ".svg": "image/svg+xml",
  ".txt": "text/plain; charset=utf-8",
  ".wasm": "application/wasm",
  ".webp": "image/webp",
  ".xml": "application/xml; charset=utf-8",
};

const sourceModeFallback = `
  const loader = document.querySelector('.compose-loading');
  if (loader) loader.innerHTML = '<strong>Statik web kabuğu önizlemesi</strong><span>Canlı deprem paneli production Wasm derlemesinden sonra burada açılır.</span>';
`;

function resolveStaticResponse(rawPath) {
  const safePath = normalize(rawPath).replace(/^(?:\.\.(?:[\\/]|$))+/, "").replace(/^[\\/]+/, "");
  let filePath = join(root, safePath);

  if (existsSync(filePath) && statSync(filePath).isDirectory()) filePath = join(filePath, "index.html");
  if (!existsSync(filePath) && !extname(filePath)) filePath = join(filePath, "index.html");

  // Mirror Firebase Hosting's real SPA rewrites so deep links can be smoke-tested locally.
  if (!existsSync(filePath) && /^\/(?:earthquake|map)(?:\/|$)/.test(rawPath)) {
    filePath = join(root, "index.html");
  }

  if (!existsSync(filePath) && rawPath === "/composeApp.js" && root === sourceRoot) {
    return { status: 200, contentType: mimeTypes[".js"], body: sourceModeFallback };
  }

  if (!existsSync(filePath) || !statSync(filePath).isFile()) {
    return { status: 404, contentType: mimeTypes[".html"], body: readFileSync(join(root, "404.html")) };
  }

  return {
    status: 200,
    contentType: mimeTypes[extname(filePath).toLowerCase()] || "application/octet-stream",
    body: readFileSync(filePath),
  };
}

const server = createServer((request, response) => {
  const rawPath = decodeURIComponent(new URL(request.url, `http://${request.headers.host}`).pathname);
  const result = resolveStaticResponse(rawPath);
  response.writeHead(result.status, {
    "Content-Type": result.contentType,
    "Cache-Control": "no-store",
  });
  response.end(result.body);
});

async function runSmokeChecks() {
  console.log(`Anlık Depremler önizleme: http://localhost:${port}`);
  console.log(root === sourceRoot ? "Mod: statik web kabuğu" : "Mod: production Wasm paketi");
  const checks = [
    ["/", 200],
    ["/deprem-aninda-ne-yapmali/", 200],
    ["/hazirlik-listesi/", 200],
    ["/earthquake/test-event-id", 200],
    ["/veri-kaynaklari/", 200],
    ["/hakkimizda/", 200],
    ["/gizlilik/", 200],
    ["/ads.txt", 200],
    ["/app-ads.txt", 200],
    ["/robots.txt", 200],
    ["/sitemap.xml", 200],
    ["/.well-known/assetlinks.json", 200],
    ["/olmayan-sayfa", 404],
  ];
  for (const [path, expectedStatus] of checks) {
    const result = resolveStaticResponse(path);
    if (result.status !== expectedStatus) {
      throw new Error(`${path}: ${result.status}, beklenen ${expectedStatus}`);
    }
    console.log(`${result.status} ${path} ${result.contentType}`);
  }
  console.log("Statik rota smoke testi başarılı (HTTP sunucusu açılmadı)");
}

if (process.argv.includes("--smoke")) {
  runSmokeChecks().catch((error) => {
    console.error(error);
    process.exitCode = 1;
  });
} else {
  server.listen(port, host, () => {
    console.log(`Anlık Depremler önizleme: http://localhost:${port}`);
    console.log(root === sourceRoot ? "Mod: statik web kabuğu" : "Mod: production Wasm paketi");
  });
}
