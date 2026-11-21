const CACHE_PREFIX = 'anlik-depremler-shell-';
const CACHE_NAME = `${CACHE_PREFIX}v2`;
const APP_SHELL = [
  '/', '/index.html', '/site.css', '/site.js', '/manifest.json', '/404.html',
  '/hazirlik-listesi/',
  '/deprem-aninda-ne-yapmali/', '/veri-kaynaklari/', '/hakkimizda/', '/gizlilik/',
  '/icons/icon-192.png', '/icons/icon-512.png'
];

self.addEventListener('install', event => {
  event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll(APP_SHELL)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', event => {
  event.waitUntil(caches.keys()
    .then(keys => Promise.all(keys.filter(key => key.startsWith(CACHE_PREFIX) && key !== CACHE_NAME).map(key => caches.delete(key))))
    .then(() => self.clients.claim()));
});

self.addEventListener('fetch', event => {
  const request = event.request;
  const url = new URL(request.url);
  if (request.method !== 'GET' || url.origin !== self.location.origin) return;

  if (request.mode === 'navigate') {
    event.respondWith((async () => {
      try {
        const response = await fetch(request);
        if (response.ok) await (await caches.open(CACHE_NAME)).put(request, response.clone());
        return response;
      } catch {
        return (await caches.match(request)) || (await caches.match('/index.html')) || Response.error();
      }
    })());
    return;
  }

  event.respondWith((async () => {
    const cached = await caches.match(request);
    if (cached) return cached;
    try {
      const response = await fetch(request);
      if (response.ok && response.type === 'basic') await (await caches.open(CACHE_NAME)).put(request, response.clone());
      return response;
    } catch {
      return Response.error();
    }
  })());
});
