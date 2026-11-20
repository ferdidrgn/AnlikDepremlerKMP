import { existsSync, readFileSync, readdirSync, statSync } from "node:fs";
import { dirname, isAbsolute, join, relative, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const requestedRoot = process.argv[2] ?? "composeApp/src/wasmJsMain/resources";
const root = isAbsolute(requestedRoot) ? requestedRoot : resolve(repoRoot, requestedRoot);
const publisherId = "pub-5779807348211992";
const accountId = `ca-${publisherId}`;
const playSigningFingerprints = [
  "2B:54:06:EF:F8:B4:8E:67:E0:3A:F5:05:69:AB:68:A7:35:B5:5F:D5:2C:A4:1F:48:8E:8C:23:55:58:81:AC:9B",
  "5C:93:78:73:4F:B4:C8:74:FD:2C:37:E0:4B:40:94:50:11:FF:36:8E:45:3B:45:89:F1:79:DD:7C:35:17:7F:DF",
];
const requiredFiles = [
  "index.html",
  "404.html",
  "site.css",
  "site.js",
  "service-worker.js",
  "manifest.json",
  "robots.txt",
  "sitemap.xml",
  "ads.txt",
  "app-ads.txt",
  "google5afbd46fec1b3ba7.html",
  ".well-known/assetlinks.json",
  "deprem-aninda-ne-yapmali/index.html",
  "hazirlik-listesi/index.html",
  "veri-kaynaklari/index.html",
  "hakkimizda/index.html",
  "gizlilik/index.html",
];

const fail = (message) => {
  console.error(`Web doğrulama hatası: ${message}`);
  process.exitCode = 1;
};

const read = (path) => readFileSync(join(root, path), "utf8");

for (const path of requiredFiles) {
  if (!existsSync(join(root, path))) fail(`eksik dosya: ${path}`);
}

if (process.exitCode) process.exit(process.exitCode);

for (const path of ["manifest.json", ".well-known/assetlinks.json"]) {
  try {
    JSON.parse(read(path));
  } catch (error) {
    fail(`${path} geçerli JSON değil: ${error.message}`);
  }
}

for (const path of ["ads.txt", "app-ads.txt"]) {
  const value = read(path).trim();
  if (value !== `google.com, ${publisherId}, DIRECT, f08c47fec0942fa0`) {
    fail(`${path} beklenen yayıncı kaydını içermiyor`);
  }
}

const pages = [
  ["index.html", "https://anlikdeprem.web.app/"],
  ["deprem-aninda-ne-yapmali/index.html", "https://anlikdeprem.web.app/deprem-aninda-ne-yapmali/"],
  ["hazirlik-listesi/index.html", "https://anlikdeprem.web.app/hazirlik-listesi/"],
  ["veri-kaynaklari/index.html", "https://anlikdeprem.web.app/veri-kaynaklari/"],
  ["hakkimizda/index.html", "https://anlikdeprem.web.app/hakkimizda/"],
  ["gizlilik/index.html", "https://anlikdeprem.web.app/gizlilik/"],
];

for (const [path, canonical] of pages) {
  const html = read(path);
  if (!html.includes(`<meta name="google-adsense-account" content="${accountId}">`)) {
    fail(`${path} AdSense hesap meta etiketini içermiyor`);
  }
  if (!html.includes(`<link rel="canonical" href="${canonical}">`)) {
    fail(`${path} kanonik URL eşleşmiyor`);
  }
  if (!/<main(?:\s|>)/.test(html)) fail(`${path} semantik main içermiyor`);
  if (!/<h1(?:\s|>)/.test(html)) fail(`${path} görünür h1 içermiyor`);
}

for (const path of ["index.html", "deprem-aninda-ne-yapmali/index.html", "hazirlik-listesi/index.html", "veri-kaynaklari/index.html", "hakkimizda/index.html"]) {
  if (!read(path).includes(`client=${accountId}`)) fail(`${path} AdSense yükleyicisini içermiyor`);
}

const preparednessPage = read("hazirlik-listesi/index.html");
if (!preparednessPage.includes('data-prep-planner') ||
    !preparednessPage.includes('name="meetingPlace"') ||
    !preparednessPage.includes('name="contactName"') ||
    !preparednessPage.includes("yalnızca bu tarayıcıda saklanır")) {
  fail("hazırlık listesi cihazda saklanan kontrol listesi/aile kartı içermiyor");
}
if (!read("service-worker.js").includes("/hazirlik-listesi/")) {
  fail("hazırlık listesi PWA çevrimdışı kabuğuna eklenmemiş");
}
const hostingConfig = JSON.parse(readFileSync(join(repoRoot, "firebase.json"), "utf8"));
const rewrites = hostingConfig?.hosting?.rewrites ?? [];
if (!rewrites.some((rewrite) => rewrite.source === "/earthquake/**" && rewrite.destination === "/index.html")) {
  fail("kalıcı deprem detay bağlantıları için Firebase Hosting rewrite eksik");
}
if (!rewrites.some((rewrite) => rewrite.source === "/map/**" && rewrite.destination === "/index.html")) {
  fail("harita derin bağlantıları için Firebase Hosting rewrite eksik");
}
const hostingHeaders = hostingConfig?.hosting?.headers ?? [];
const hasHeader = (source, key, value) => hostingHeaders.some((rule) =>
  rule.source === source && rule.headers?.some((header) => header.key === key && header.value === value));
if (!hasHeader("/.well-known/assetlinks.json", "Content-Type", "application/json; charset=utf-8")) {
  fail("assetlinks.json için Firebase Hosting JSON Content-Type başlığı eksik");
}
for (const route of ["/earthquake/**", "/map/**"]) {
  if (!hasHeader(route, "X-Robots-Tag", "noindex, follow")) {
    fail(`${route} için Firebase Hosting noindex başlığı eksik`);
  }
}

const assetLinks = JSON.parse(read(".well-known/assetlinks.json"));
const target = assetLinks?.[0]?.target;
if (target?.package_name !== "com.ferdidrgn.anlikdepremler") fail("assetlinks paket adı yanlış");
if (!Array.isArray(target?.sha256_cert_fingerprints) ||
    target.sha256_cert_fingerprints.length === 0 ||
    !target.sha256_cert_fingerprints.every((value) => /^(?:[0-9A-F]{2}:){31}[0-9A-F]{2}$/.test(value))) {
  fail("assetlinks SHA-256 parmak izi biçimi geçersiz");
}
for (const fingerprint of playSigningFingerprints) {
  if (!target.sha256_cert_fingerprints.includes(fingerprint)) {
    fail(`assetlinks Play signing SHA-256 eksik: ${fingerprint}`);
  }
}

const sitemap = read("sitemap.xml");
for (const [, canonical] of pages) {
  if (!sitemap.includes(`<loc>${canonical}</loc>`)) fail(`sitemap URL eksik: ${canonical}`);
}
if (/<(?:changefreq|priority)>/.test(sitemap)) fail("sitemap gereksiz changefreq/priority içeriyor");
if (!read("robots.txt").includes("Sitemap: https://anlikdeprem.web.app/sitemap.xml")) {
  fail("robots.txt sitemap bildirimi eksik");
}

const walk = (directory) => readdirSync(directory).flatMap((name) => {
  const path = join(directory, name);
  return statSync(path).isDirectory() ? walk(path) : [path];
});

if (root.endsWith(join("build", "dist", "wasmJs", "productionExecutable"))) {
  if (!existsSync(join(root, "composeApp.js"))) fail("production web paketi eksik dosya: composeApp.js");
  if (!walk(root).some((file) => file.endsWith(".wasm"))) {
    fail("production web paketinde Wasm çıktısı bulunamadı");
  }
}

for (const file of walk(root)) {
  if (!/\.(?:html|json|xml|txt|js|css)$/.test(file)) continue;
  const content = readFileSync(file, "utf8");
  if (/REPLACE_WITH|YOUR_SHA256|anlikdepremler\.com\/earthquake\//.test(content)) {
    fail(`yer tutucu veya eski alan adı bulundu: ${relative(root, file)}`);
  }
}

if (!process.exitCode) console.log(`Web varlıkları doğrulandı: ${root}`);
