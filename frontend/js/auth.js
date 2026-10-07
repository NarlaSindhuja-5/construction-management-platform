/**
 * E2E Construction Management System - Authentication & JWT Management Module
 * Vanilla JavaScript & Fetch API implementation for real Spring Boot backend integration.
 */

const E2E_AUTH_CONFIG = {
    // Configurable API base URL with dynamic environment resolution
    get API_BASE_URL() {
        if (window.APP_CONFIG && window.APP_CONFIG.API_BASE_URL) {
            return window.APP_CONFIG.API_BASE_URL;
        }
        if (typeof window.getApiBaseUrl === 'function') {
            return window.getApiBaseUrl();
        }
        if (window.API_BASE_URL) {
            return window.API_BASE_URL;
        }
        const isLocal = typeof window !== 'undefined' && window.location && 
            (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' || window.location.protocol === 'file:');
        return isLocal ? 'http://localhost:8080/api' : (window.location.origin + '/api');
    },
    TOKEN_KEY: 'e2e_auth_token',
    USER_KEY: 'e2e_user_data'
};

const AuthService = {

    /**
     * Get stored JWT token
     */
    getToken() {
        return localStorage.getItem(E2E_AUTH_CONFIG.TOKEN_KEY);
    },

    /**
     * Get stored user details
     */
    getUser() {
        try {
            const data = localStorage.getItem(E2E_AUTH_CONFIG.USER_KEY);
            return data ? JSON.parse(data) : null;
        } catch (e) {
            console.error('Error parsing stored user data:', e);
            return null;
        }
    },

    /**
     * Save session data to localStorage
     */
    setSession(token, user) {
        if (token) {
            localStorage.setItem(E2E_AUTH_CONFIG.TOKEN_KEY, token);
        }
        if (user) {
            localStorage.setItem(E2E_AUTH_CONFIG.USER_KEY, JSON.stringify(user));
        }
    },

    /**
     * Clear auth session on logout or token expiration
     */
    clearSession() {
        localStorage.removeItem(E2E_AUTH_CONFIG.TOKEN_KEY);
        localStorage.removeItem(E2E_AUTH_CONFIG.USER_KEY);
    },

    /**
     * Check if user is currently authenticated with a valid token
     */
    isAuthenticated() {
        const token = this.getToken();
        if (!token) return false;
        return !this.isTokenExpired(token);
    },

    /**
     * Inspect token expiration time from JWT payload
     */
    isTokenExpired(token) {
        if (!token) return true;
        try {
            const parts = token.split('.');
            if (parts.length !== 3) return true;
            const payload = JSON.parse(atob(parts[1]));
            if (!payload.exp) return false;
            // exp is in seconds, convert to milliseconds
            return Date.now() >= payload.exp * 1000;
        } catch (e) {
            console.warn('Could not parse token expiration:', e);
            return true;
        }
    },

    /**
     * Core Fetch API wrapper with automatic JWT Bearer header and error handling
     */
    async apiFetch(endpoint, options = {}) {
        const url = endpoint.startsWith('http')
            ? endpoint
            : `${E2E_AUTH_CONFIG.API_BASE_URL}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;

        const headers = {
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        if (!(options.body instanceof FormData)) {
            headers['Content-Type'] = headers['Content-Type'] || 'application/json';
        }

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const fetchOptions = {
            ...options,
            headers
        };

        let response;
        try {
            response = await fetch(url, fetchOptions);
        } catch (networkError) {
            console.error('Network request failed:', networkError);
            throw {
                status: 0,
                error: 'Network Error',
                message: `Unable to connect to the backend server at ${E2E_AUTH_CONFIG.API_BASE_URL}. Please verify the API service is reachable.`
            };
        }

        // Handle 401 Unauthorized (Expired token or invalid credentials)
        if (response.status === 401) {
            const data = await response.json().catch(() => ({}));
            // If we sent a token and got 401, token has expired or is invalid
            if (token && !endpoint.includes('/auth/login')) {
                this.clearSession();
                const currentPath = window.location.pathname;
                if (!currentPath.includes('login.html')) {
                    const relativePrefix = currentPath.includes('/pages/') ? '../' : '';
                    window.location.href = `${relativePrefix}login.html?reason=expired`;
                    return;
                }
            }
            throw {
                status: 401,
                error: 'Unauthorized',
                message: data.message || 'Invalid email or password.'
            };
        }

        // Handle 403 Forbidden
        if (response.status === 403) {
            const data = await response.json().catch(() => ({}));
            throw {
                status: 403,
                error: 'Forbidden',
                message: data.message || 'You do not have permission to perform this action.'
            };
        }

        // Handle other error responses (400 Validation, 404, 500, etc.)
        if (!response.ok) {
            let errorData;
            try {
                errorData = await response.json();
            } catch (e) {
                errorData = { message: `Server error (${response.status} ${response.statusText})` };
            }

            throw {
                status: response.status,
                error: errorData.error || 'Request Failed',
                message: errorData.message || 'An unexpected error occurred.',
                validationErrors: errorData.validationErrors || null
            };
        }

        // 204 No Content
        if (response.status === 204) {
            return null;
        }

        return await response.json();
    },

    /**
     * Authenticate user with email and password
     */
    async login(email, password) {
        const payload = { email: email.trim(), password };
        const data = await this.apiFetch('/auth/login', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        // Backend AuthResponse format:
        // { token, tokenType: "Bearer", userId, email, firstName, lastName, role, message }
        const user = {
            id: data.userId,
            email: data.email,
            firstName: data.firstName,
            lastName: data.lastName,
            role: data.role
        };

        this.setSession(data.token, user);
        return { token: data.token, user };
    },

    /**
     * Register a new user
     */
    async register(registrationData) {
        const data = await this.apiFetch('/auth/register', {
            method: 'POST',
            body: JSON.stringify(registrationData)
        });

        const user = {
            id: data.userId,
            email: data.email,
            firstName: data.firstName,
            lastName: data.lastName,
            role: data.role
        };

        this.setSession(data.token, user);
        return { token: data.token, user };
    },

    /**
     * Retrieve authenticated user profile from GET /api/auth/me
     */
    async getCurrentUser() {
        const profile = await this.apiFetch('/auth/me', {
            method: 'GET'
        });

        // Update stored profile cache
        const currentUser = {
            id: profile.id,
            email: profile.email,
            firstName: profile.firstName,
            lastName: profile.lastName,
            role: profile.role,
            phoneNumber: profile.phoneNumber,
            active: profile.active
        };

        this.setSession(this.getToken(), currentUser);
        return currentUser;
    },

    /**
     * Log out current user and redirect to login page
     */
    logout() {
        this.clearSession();
        const isInPagesDir = window.location.pathname.includes('/pages/');
        const targetUrl = isInPagesDir ? '../login.html?reason=logout' : 'login.html?reason=logout';
        window.location.href = targetUrl;
    },

    /**
     * Role-based redirection path mapping:
     * ADMIN            -> Admin Dashboard
     * CONTRACTOR       -> Contractor Dashboard
     * LABORER          -> Laborer Dashboard
     * MACHINERY_OWNER  -> Machinery Dashboard
     * MATERIAL_SUPPLIER-> Supplier Dashboard
     * CLIENT           -> Client Dashboard
     */
    getDashboardUrlByRole(role) {
        const cleanRole = (role || '').trim().toUpperCase();
        const isInPagesDir = window.location.pathname.includes('/pages/');
        const prefix = isInPagesDir ? '' : 'pages/';

        switch (cleanRole) {
            case 'ADMIN':
                return `${prefix}admin-dashboard.html`;
            case 'CONTRACTOR':
            case 'SITE_MANAGER':
                return `${prefix}contractor-dashboard.html`;
            case 'LABORER':
                return `${prefix}laborer-dashboard.html`;
            case 'MACHINERY_OWNER':
                return `${prefix}machinery-dashboard.html`;
            case 'MATERIAL_SUPPLIER':
                return `${prefix}supplier-dashboard.html`;
            case 'CLIENT':
                return `${prefix}client-dashboard.html`;
            default:
                return `${isInPagesDir ? '../' : ''}index.html`;
        }
    },

    /**
     * Redirect to appropriate dashboard after successful authentication
     */
    redirectAfterLogin(user) {
        if (!user || !user.role) {
            window.location.href = 'index.html';
            return;
        }
        const dashboardUrl = this.getDashboardUrlByRole(user.role);
        window.location.href = dashboardUrl;
    },

    /**
     * Auto-redirect away from login/register if already logged in with valid token
     */
    redirectIfLoggedIn() {
        if (this.isAuthenticated()) {
            const user = this.getUser();
            if (user && user.role) {
                console.log('User already authenticated, redirecting to dashboard:', user.role);
                this.redirectAfterLogin(user);
            }
        }
    },

    /**
     * Protected page guard: verify authentication and role permissions
     */
    requireAuth(allowedRoles = []) {
        if (!this.isAuthenticated()) {
            this.clearSession();
            const isInPagesDir = window.location.pathname.includes('/pages/');
            const loginUrl = `${isInPagesDir ? '../' : ''}login.html?reason=unauthenticated`;
            window.location.href = loginUrl;
            return false;
        }

        const user = this.getUser();
        if (!user) {
            this.logout();
            return false;
        }

        if (allowedRoles && allowedRoles.length > 0) {
            const userRole = (user.role || '').toUpperCase();
            const isAllowed = allowedRoles.map(r => r.toUpperCase()).includes(userRole);
            if (!isAllowed) {
                console.warn(`Access denied for role ${userRole}. Required:`, allowedRoles);
                alert(`Access Denied: Your account role (${userRole}) is not authorized for this section.`);
                this.redirectAfterLogin(user);
                return false;
            }
        }

        return true;
    }
};

// UI Feedback & Operational Component Utilities
const UI = {
    showAlert(containerId, message, type = 'danger') {
        const container = document.getElementById(containerId);
        if (!container) return;

        let iconSvg = '';
        if (type === 'danger') {
            iconSvg = '<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/></svg>';
        } else if (type === 'success') {
            iconSvg = '<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/></svg>';
        } else if (type === 'warning') {
            iconSvg = '<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>';
        } else {
            iconSvg = '<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/></svg>';
        }

        container.innerHTML = `
            <div class="alert alert-${type}">
                <div style="flex-shrink: 0; line-height: 1;">${iconSvg}</div>
                <div style="flex: 1;">${message}</div>
            </div>
        `;
        container.style.display = 'block';
    },

    clearAlert(containerId) {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = '';
            container.style.display = 'none';
        }
    },

    setLoading(buttonId, isLoading, defaultText = 'Submit') {
        const button = document.getElementById(buttonId);
        if (!button) return;

        if (isLoading) {
            button.disabled = true;
            button.innerHTML = `<span class="spinner"></span> <span>Processing...</span>`;
        } else {
            button.disabled = false;
            button.innerHTML = defaultText;
        }
    },

    clearFieldErrors(form) {
        if (!form) return;
        form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        form.querySelectorAll('.invalid-feedback').forEach(el => el.remove());
    },

    showFieldError(fieldElement, errorMessage) {
        if (!fieldElement) return;
        fieldElement.classList.add('is-invalid');
        const errorDiv = document.createElement('div');
        errorDiv.className = 'invalid-feedback';
        errorDiv.innerText = errorMessage;
        fieldElement.parentElement.appendChild(errorDiv);
    },

    /**
     * Floating toast notifications
     */
    showToast(message, type = 'info', duration = 3500) {
        let container = document.getElementById('toastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toastContainer';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;

        let iconSvg = '';
        if (type === 'success') {
            iconSvg = '<svg width="18" height="18" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/></svg>';
        } else if (type === 'error' || type === 'danger') {
            iconSvg = '<svg width="18" height="18" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/></svg>';
        } else if (type === 'warning') {
            iconSvg = '<svg width="18" height="18" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>';
        } else {
            iconSvg = '<svg width="18" height="18" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/></svg>';
        }

        toast.innerHTML = `
            <div style="flex-shrink:0; line-height:1;">${iconSvg}</div>
            <div style="flex:1; font-weight:600;">${message}</div>
        `;

        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(20px)';
            setTimeout(() => toast.remove(), 250);
        }, duration);
    },

    /**
     * Standardized empty state HTML generator
     */
    renderEmptyState(container, options = {}) {
        if (!container) return;
        const target = typeof container === 'string' ? document.getElementById(container) : container;
        if (!target) return;

        const title = options.title || 'No Records Found';
        const message = options.message || 'There are no active records in this view right now.';
        const actionHtml = options.actionText && options.actionHref ? `
            <div class="empty-state-action">
                <a href="${options.actionHref}" class="btn btn-primary btn-sm">${options.actionText}</a>
            </div>
        ` : (options.actionText && options.actionOnClick ? `
            <div class="empty-state-action">
                <button onclick="${options.actionOnClick}" class="btn btn-primary btn-sm">${options.actionText}</button>
            </div>
        ` : '');

        target.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">
                    <svg width="48" height="48" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"/></svg>
                </div>
                <h4 class="empty-state-title">${title}</h4>
                <p class="empty-state-desc">${message}</p>
                ${actionHtml}
            </div>
        `;
    },

    /**
     * Standardized loading indicator
     */
    renderLoading(container, message = 'Loading records...') {
        if (!container) return;
        const target = typeof container === 'string' ? document.getElementById(container) : container;
        if (!target) return;

        target.innerHTML = `
            <div class="loading-state">
                <span class="spinner spinner-dark" style="width:28px;height:28px;border-width:3px;"></span>
                <span>${message}</span>
            </div>
        `;
    },

    /**
     * Standardized error state
     */
    renderError(container, message = 'Unable to fetch data from the server.', retryFnName = '') {
        if (!container) return;
        const target = typeof container === 'string' ? document.getElementById(container) : container;
        if (!target) return;

        const retryHtml = retryFnName ? `
            <button class="btn btn-outline btn-sm" style="margin-top:0.75rem;" onclick="${retryFnName}">Try Again</button>
        ` : '';

        target.innerHTML = `
            <div class="empty-state" style="border-color: var(--danger-border); background: var(--danger-subtle);">
                <div class="empty-state-icon" style="color: var(--danger);">
                    <svg width="40" height="40" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
                </div>
                <h4 class="empty-state-title" style="color: #991b1b;">Connection Error</h4>
                <p class="empty-state-desc" style="color: #991b1b;">${message}</p>
                ${retryHtml}
            </div>
        `;
    },

    /**
     * Format dates into human-readable industrial standard
     */
    formatDate(dateStr) {
        if (!dateStr) return 'N/A';
        try {
            const d = new Date(dateStr);
            if (isNaN(d.getTime())) return dateStr;
            return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
        } catch {
            return dateStr;
        }
    },

    /**
     * Format currency values
     */
    formatCurrency(amount) {
        if (amount == null || isNaN(amount)) return '$0';
        return '$' + Number(amount).toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
    },

    /**
     * Auto-bind mobile navigation menu toggling
     */
    initMobileNav() {
        const toggleBtn = document.querySelector('.mobile-nav-toggle');
        const navLinks = document.querySelector('.nav-links');
        if (toggleBtn && navLinks) {
            toggleBtn.addEventListener('click', () => {
                navLinks.classList.toggle('is-open');
            });
        }
    }
};

// Auto-run mobile nav init on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    UI.initMobileNav();
});

// Export to window for global access across vanilla HTML scripts
window.AuthService = AuthService;
window.UI = UI;
