/**
 * E2E Construction Management System - Runtime Configuration Module
 * 
 * Production-ready API URL resolution:
 * 1. Checks window.APP_CONFIG.API_BASE_URL (can be injected by server or HTML).
 * 2. Checks <meta name="api-base-url" content="..."> in the document head.
 * 3. Checks localStorage override 'e2e_api_base_url' (useful for staging / QA testing).
 * 4. Fallback:
 *    - In local development (localhost / 127.0.0.1 / file:): defaults to 'http://localhost:8080/api'
 *    - In production (custom domain or HTTPS): defaults to '/api' (relative to current origin)
 */
(function () {
    window.APP_CONFIG = window.APP_CONFIG || {};

    function resolveApiBaseUrl() {
        // Explicitly configured in JS
        if (window.APP_CONFIG.API_BASE_URL) {
            return window.APP_CONFIG.API_BASE_URL;
        }

        // Configured via meta tag: <meta name="api-base-url" content="https://api.yourdomain.com/api">
        const metaTag = document.querySelector('meta[name="api-base-url"]');
        if (metaTag && metaTag.getAttribute('content')) {
            return metaTag.getAttribute('content').trim();
        }

        // QA / Staging override from localStorage
        try {
            const stored = localStorage.getItem('e2e_api_base_url');
            if (stored && stored.trim()) {
                return stored.trim();
            }
        } catch (e) {
            // Storage access might fail in restricted iframe environments
        }

        // Window variable override
        if (window.API_BASE_URL) {
            return window.API_BASE_URL;
        }

        // Automatic environment-based resolution
        const hostname = window.location.hostname;
        const isLocalDev = !hostname || hostname === 'localhost' || hostname === '127.0.0.1' || window.location.protocol === 'file:';

        if (isLocalDev) {
            return 'http://localhost:8080/api';
        }

        // In production on a public domain, default to the relative /api path on the current origin
        return `${window.location.origin}/api`;
    }

    window.APP_CONFIG.API_BASE_URL = resolveApiBaseUrl();

    // Helper getter available across all frontend scripts
    window.getApiBaseUrl = function () {
        return window.APP_CONFIG.API_BASE_URL;
    };
})();
