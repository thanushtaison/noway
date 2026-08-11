var CACHE_NAME = 'noway-v1';
var SHELL_URLS = [
    '/',
    '/products',
    '/login',
    '/register',
    '/css/style.css',
    '/js/main.js',
    '/images/logo.svg',
    '/manifest.json'
];

self.addEventListener('install', function (event) {
    event.waitUntil(
        caches.open(CACHE_NAME).then(function (cache) {
            return cache.addAll(SHELL_URLS);
        })
    );
    self.skipWaiting();
});

self.addEventListener('activate', function (event) {
    event.waitUntil(
        caches.keys().then(function (keys) {
            return Promise.all(
                keys.filter(function (key) {
                    return key !== CACHE_NAME;
                }).map(function (key) {
                    return caches.delete(key);
                })
            );
        })
    );
    self.clients.claim();
});

self.addEventListener('fetch', function (event) {
    var request = event.request;
    if (request.method !== 'GET') {
        return;
    }
    var url = new URL(request.url);
    if (url.origin !== location.origin) {
        return;
    }

    if (request.mode === 'navigate') {
        event.respondWith(
            fetch(request).then(function (response) {
                var copy = response.clone();
                caches.open(CACHE_NAME).then(function (cache) {
                    cache.put(request, copy);
                });
                return response;
            }).catch(function () {
                return caches.match('/');
            })
        );
        return;
    }

    event.respondWith(
        caches.match(request).then(function (cached) {
            if (cached) {
                return cached;
            }
            return fetch(request).then(function (response) {
                if (response && response.status === 200 && response.type === 'basic') {
                    var copy = response.clone();
                    caches.open(CACHE_NAME).then(function (cache) {
                        cache.put(request, copy);
                    });
                }
                return response;
            });
        })
    );
});
