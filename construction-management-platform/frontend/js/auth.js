/**
 * Authentication & Session Management
 */

function getAuthToken() {
  return localStorage.getItem('buildpro_token');
}

function getAuthUser() {
  const userJson = localStorage.getItem('buildpro_user');
  try {
    return userJson ? JSON.parse(userJson) : null;
  } catch (e) {
    return null;
  }
}

function setAuth(token, user) {
  localStorage.setItem('buildpro_token', token);
  localStorage.setItem('buildpro_user', JSON.stringify(user));
}

function clearAuth() {
  localStorage.removeItem('buildpro_token');
  localStorage.removeItem('buildpro_user');
}

function isLoggedIn() {
  return !!getAuthToken() && !!getAuthUser();
}

/**
 * Checks authentication on protected pages
 */
function requireAuth() {
  if (!isLoggedIn()) {
    window.location.href = '/login.html';
    return false;
  }
  return true;
}

/**
 * Checks if current user matches permitted roles
 */
function hasRole(...allowedRoles) {
  const user = getAuthUser();
  if (!user || !user.role) return false;
  return allowedRoles.includes(user.role);
}

/**
 * Logs out user and clears local session
 */
function logout() {
  clearAuth();
  showToast('Logged out successfully', 'info');
  setTimeout(() => {
    window.location.href = '/login.html';
  }, 300);
}

/**
 * Convenience helper to populate demo credentials on login page
 */
function fillDemoCredentials(email, password) {
  const emailInput = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  if (emailInput && passwordInput) {
    emailInput.value = email;
    passwordInput.value = password;
  }
}
