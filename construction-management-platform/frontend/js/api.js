/**
 * API Client & Toast Notification System
 * Handles authenticated fetch requests, centralized error handling, and modal triggers.
 */

// Dynamically determine API Base URL
// Supports localhost, direct LAN IP (192.168.x.x), and any global cloud/tunnel domain
const API_BASE = window.location.protocol === 'file:' 
  ? 'http://localhost:5000/api'
  : '/api';

/**
 * Standard fetch wrapper with JWT authorization and unified error extraction
 */
async function apiRequest(endpoint, options = {}) {
  const token = localStorage.getItem('buildpro_token');
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;

  try {
    const response = await fetch(url, {
      ...options,
      headers
    });

    // Handle token expiry or unauthenticated
    if (response.status === 401) {
      localStorage.removeItem('buildpro_token');
      localStorage.removeItem('buildpro_user');
      if (!window.location.pathname.endsWith('login.html')) {
        window.location.href = '/login.html';
      }
      throw new Error('Session expired. Please login again.');
    }

    const data = await response.json().catch(() => null);

    if (!response.ok) {
      const errorMsg = (data && data.message) ? data.message : `Request failed with status ${response.status}`;
      const err = new Error(errorMsg);
      err.data = data;
      err.status = response.status;
      throw err;
    }

    return data;
  } catch (error) {
    console.error(`[API Error: ${endpoint}]`, error);
    throw error;
  }
}

/**
 * Toast Notification Helper
 * @param {string} message
 * @param {'success'|'error'|'warning'|'info'} type
 */
function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;

  const iconMap = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  };

  toast.innerHTML = `
    <div style="display:flex; align-items:center; gap:0.5rem;">
      <span style="font-weight:700;">${iconMap[type] || '•'}</span>
      <span>${message}</span>
    </div>
    <button style="background:none; border:none; color:#fff; cursor:pointer; font-size:1.1rem;" onclick="this.parentElement.remove()">×</button>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4500);
}

/**
 * Modal Management Helpers
 */
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('active');
    document.body.style.overflow = '';
  }
}

// Close modal when clicking on overlay background
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('active');
    document.body.style.overflow = '';
  }
});
