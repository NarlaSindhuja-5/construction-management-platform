/**
 * Navigation & Shell Manager
 * Injects user details, manages active link state, and handles mobile responsiveness.
 */

document.addEventListener('DOMContentLoaded', () => {
  initNavigation();
});

function initNavigation() {
  const user = getAuthUser();

  // Populate user badge in sidebar footer
  const userNameEl = document.getElementById('sidebar-user-name');
  const userRoleEl = document.getElementById('sidebar-user-role');
  const userAvatarEl = document.getElementById('sidebar-user-avatar');

  if (user) {
    if (userNameEl) userNameEl.textContent = user.name || 'User';
    if (userRoleEl) userRoleEl.textContent = user.role || 'GUEST';
    if (userAvatarEl) {
      const initial = user.name ? user.name.charAt(0).toUpperCase() : 'U';
      userAvatarEl.textContent = initial;
    }
  }

  // Active Link Highlight
  const currentPath = window.location.pathname.toLowerCase();
  const navItems = document.querySelectorAll('.nav-item');

  navItems.forEach(item => {
    const href = item.getAttribute('href');
    if (!href) return;

    const target = href.toLowerCase();
    if (
      currentPath.endsWith(target) ||
      (currentPath.includes(target) && target !== 'dashboard.html' && target !== '/' && target !== 'index.html')
    ) {
      item.classList.add('active');
    } else {
      item.classList.remove('active');
    }
  });

  // Mobile menu toggle
  const toggleBtn = document.getElementById('menu-toggle');
  const sidebar = document.querySelector('.app-sidebar');
  let backdrop = document.querySelector('.sidebar-backdrop');

  if (toggleBtn && sidebar) {
    if (!backdrop) {
      backdrop = document.createElement('div');
      backdrop.className = 'sidebar-backdrop';
      document.body.appendChild(backdrop);
    }

    toggleBtn.addEventListener('click', () => {
      sidebar.classList.toggle('open');
      backdrop.classList.toggle('active');
    });

    backdrop.addEventListener('click', () => {
      sidebar.classList.remove('open');
      backdrop.classList.remove('active');
    });
  }

  // Bind logout buttons
  const logoutBtns = document.querySelectorAll('.btn-logout');
  logoutBtns.forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      logout();
    });
  });
}
