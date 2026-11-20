import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";

const resources = resolve("composeApp/src/wasmJsMain/resources");
const output = resolve(process.argv[2] || "./build-preview/anlik-depremler.html");
const css = readFileSync(resolve(resources, "site.css"), "utf8");
const siteJs = readFileSync(resolve(resources, "site.js"), "utf8");
const previewJs = `${siteJs}\nconst loader=document.querySelector('.compose-loading');if(loader){loader.innerHTML='<strong>Statik web kabuğu önizlemesi</strong><span>Canlı deprem paneli production Wasm derlemesinden sonra burada açılır.</span>';}`;

let html = readFileSync(resolve(resources, "index.html"), "utf8")
  .replace(/<link rel="preconnect"[^>]*>\s*/g, "")
  .replace(/<link href="https:\/\/fonts\.googleapis\.com[^>]*>\s*/g, "")
  .replace('<link rel="stylesheet" href="/site.css">', `<style>${css}</style>`)
  .replace(/<script async src="https:\/\/pagead2\.googlesyndication\.com[^>]*><\/script>/, "")
  .replace('<script src="/site.js" defer></script>', "")
  .replace('<script src="/composeApp.js"></script>', `<script>${previewJs}</script>`)
  .replace(/(?:href|src)="\/(?:favicon\.ico|icons\/[^"]+)"/g, 'href="data:,"');

mkdirSync(dirname(output), { recursive: true });
writeFileSync(output, html, "utf8");
console.log(output);
