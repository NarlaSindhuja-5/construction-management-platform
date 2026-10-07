/**
 * E2E Construction Management System - Unified Production Gateway
 * 
 * Functions:
 * 1. Serves all static frontend assets (HTML, CSS, JS, images)
 * 2. Seamlessly reverse-proxies all /api/* requests to Spring Boot on port 8080
 * 3. Eliminates cross-origin CORS conflicts, mixed content errors, and multiple ports
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = process.env.PORT || 3000;
const FRONTEND_DIR = path.join(__dirname, 'frontend');
const BACKEND_HOST = process.env.BACKEND_HOST || '127.0.0.1';
const BACKEND_PORT = process.env.BACKEND_PORT || 8080;

const MIME_TYPES = {
    '.html': 'text/html; charset=UTF-8',
    '.css': 'text/css; charset=UTF-8',
    '.js': 'application/javascript; charset=UTF-8',
    '.json': 'application/json; charset=UTF-8',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.webp': 'image/webp',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon',
    '.pdf': 'application/pdf',
    '.woff': 'font/woff',
    '.woff2': 'font/woff2',
    '.ttf': 'font/ttf'
};

function proxyApiRequest(req, res) {
    const proxyHeaders = { ...req.headers };
    proxyHeaders['host'] = `${BACKEND_HOST}:${BACKEND_PORT}`;
    proxyHeaders['x-forwarded-host'] = req.headers['host'] || 'localhost';
    proxyHeaders['x-forwarded-proto'] = req.headers['x-forwarded-proto'] || 'https';
    if (req.socket.remoteAddress) {
        proxyHeaders['x-forwarded-for'] = req.headers['x-forwarded-for'] 
            ? `${req.headers['x-forwarded-for']}, ${req.socket.remoteAddress}`
            : req.socket.remoteAddress;
    }

    const options = {
        hostname: BACKEND_HOST,
        port: BACKEND_PORT,
        path: req.url,
        method: req.method,
        headers: proxyHeaders
    };

    const proxyReq = http.request(options, (proxyRes) => {
        res.writeHead(proxyRes.statusCode, proxyRes.headers);
        proxyRes.pipe(res);
    });

    proxyReq.on('error', (err) => {
        console.error('[Gateway Error] Proxy error connecting to backend:', err.message);
        if (!res.headersSent) {
            res.writeHead(502, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({
                status: 'DOWN',
                error: 'Bad Gateway',
                message: 'Unable to communicate with the Spring Boot backend server on port ' + BACKEND_PORT
            }));
        }
    });

    req.pipe(proxyReq);
}

function serveStaticFile(req, res, parsedUrl) {
    let pathname = decodeURIComponent(parsedUrl.pathname);
    if (pathname === '/' || pathname === '') {
        pathname = '/index.html';
    }

    let filePath = path.join(FRONTEND_DIR, pathname);

    // Security check: prevent directory traversal
    if (!filePath.startsWith(FRONTEND_DIR)) {
        res.writeHead(403, { 'Content-Type': 'text/plain' });
        return res.end('403 Forbidden');
    }

    fs.stat(filePath, (err, stats) => {
        if (err || !stats.isFile()) {
            // Check if file exists inside pages/ directory
            const pagesPath = path.join(FRONTEND_DIR, 'pages', pathname);
            if (fs.existsSync(pagesPath) && fs.statSync(pagesPath).isFile()) {
                return sendFile(res, pagesPath);
            }

            // Check if appending .html matches
            const htmlPath = filePath + '.html';
            if (fs.existsSync(htmlPath) && fs.statSync(htmlPath).isFile()) {
                return sendFile(res, htmlPath);
            }

            // Fallback 404
            res.writeHead(404, { 'Content-Type': 'text/html; charset=UTF-8' });
            return res.end(`
                <!DOCTYPE html>
                <html>
                <head><title>404 Not Found</title><meta name="viewport" content="width=device-width, initial-scale=1"></head>
                <body style="font-family:sans-serif; text-align:center; padding: 3rem;">
                    <h1>404 - Page Not Found</h1>
                    <p>The requested file does not exist.</p>
                    <a href="/" style="color:#0284c7; font-weight:600;">Return Home</a>
                </body>
                </html>
            `);
        }

        sendFile(res, filePath);
    });
}

function sendFile(res, filePath) {
    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    res.writeHead(200, {
        'Content-Type': contentType,
        'Cache-Control': ext === '.html' ? 'no-cache' : 'public, max-age=86400'
    });

    fs.createReadStream(filePath).pipe(res);
}

const server = http.createServer((req, res) => {
    const parsedUrl = url.parse(req.url);

    // API calls routed to Spring Boot backend
    if (parsedUrl.pathname.startsWith('/api/') || parsedUrl.pathname === '/api') {
        return proxyApiRequest(req, res);
    }

    // Static asset serving
    serveStaticFile(req, res, parsedUrl);
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`=======================================================`);
    console.log(`  E2E Construction Unified Gateway Running`);
    console.log(`  Local URL:   http://localhost:${PORT}`);
    console.log(`  Proxying API: http://${BACKEND_HOST}:${BACKEND_PORT}/api`);
    console.log(`=======================================================`);
});
