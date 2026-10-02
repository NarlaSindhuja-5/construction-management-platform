/**
 * Sites Management View Logic
 */

let allSites = [];
let allProjects = [];
let allSiteManagers = [];

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  loadData();
  setupEventListeners();
});

function setupEventListeners() {
  document.getElementById('site-search')?.addEventListener('input', filterAndRenderSites);
  document.getElementById('site-project-filter')?.addEventListener('change', filterAndRenderSites);
  document.getElementById('site-form')?.addEventListener('submit', handleSaveSite);
}

async function loadData() {
  await Promise.all([
    loadProjectsDropdown(),
    loadSiteManagersDropdown(),
    loadSites()
  ]);
}

async function loadProjectsDropdown() {
  try {
    const res = await apiRequest('/projects');
    if (res && res.projects) {
      allProjects = res.projects;
      const filterEl = document.getElementById('site-project-filter');
      const formProjectEl = document.getElementById('site-project-id');

      const options = allProjects.map(p => `<option value="${p.id}">${escapeHtml(p.project_name)}</option>`).join('');
      if (filterEl) filterEl.innerHTML = '<option value="">All Projects</option>' + options;
      if (formProjectEl) formProjectEl.innerHTML = '<option value="">Select Project</option>' + options;
    }
  } catch (err) {
    console.error('Error loading projects for dropdown:', err);
  }
}

async function loadSiteManagersDropdown() {
  try {
    const res = await apiRequest('/users?role=SITE_MANAGER');
    if (res && res.users) {
      allSiteManagers = res.users;
      const formManagerEl = document.getElementById('site-manager-id');
      if (formManagerEl) {
        formManagerEl.innerHTML = '<option value="">Assign Site Manager (Optional)</option>' +
          allSiteManagers.map(u => `<option value="${u.id}">${escapeHtml(u.name)} (${u.email})</option>`).join('');
      }
    }
  } catch (err) {
    console.error('Error loading site managers:', err);
  }
}

async function loadSites() {
  try {
    const res = await apiRequest('/sites');
    if (res && res.sites) {
      allSites = res.sites;
      filterAndRenderSites();
    }
  } catch (err) {
    showToast('Failed to load sites: ' + err.message, 'error');
  }
}

function filterAndRenderSites() {
  const searchTerm = (document.getElementById('site-search')?.value || '').toLowerCase();
  const projectId = document.getElementById('site-project-filter')?.value || '';

  const filtered = allSites.filter(s => {
    const matchesSearch = s.site_name.toLowerCase().includes(searchTerm) ||
                          s.location.toLowerCase().includes(searchTerm) ||
                          s.project_name.toLowerCase().includes(searchTerm);
    const matchesProject = !projectId || String(s.project_id) === String(projectId);
    return matchesSearch && matchesProject;
  });

  renderSitesList(filtered);
}

function renderSitesList(sites) {
  const tbody = document.getElementById('sites-table-body');
  if (!tbody) return;

  if (sites.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" class="empty-state">
          <div class="empty-state-icon">🏗️</div>
          <h3>No Construction Sites Found</h3>
          <p>Create a site and assign it to an active project.</p>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = sites.map(s => {
    return `
      <tr>
        <td>
          <strong>${escapeHtml(s.site_name)}</strong>
          <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(s.address || s.location)}</div>
        </td>
        <td>
          <span style="font-weight:600; color:var(--accent-blue);">${escapeHtml(s.project_name)}</span>
        </td>
        <td>${escapeHtml(s.location)}</td>
        <td>
          ${s.site_manager_name 
            ? `<div><strong>${escapeHtml(s.site_manager_name)}</strong></div><div style="font-size:0.75rem; color:var(--text-muted);">${s.site_manager_phone || ''}</div>`
            : '<span class="text-muted" style="font-style:italic;">Unassigned</span>'
          }
        </td>
        <td>
          <span class="badge badge-info">${s.workers_count || 0} Workers</span>
          <span class="badge badge-neutral">${s.tasks_count || 0} Tasks</span>
        </td>
        <td>${getStatusBadge(s.status)}</td>
        <td style="white-space:nowrap;">
          <button class="btn btn-secondary btn-sm" onclick="openEditSiteModal(${s.id})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="handleDeleteSite(${s.id})">Delete</button>
        </td>
      </tr>
    `;
  }).join('');
}

function openCreateSiteModal() {
  const form = document.getElementById('site-form');
  if (form) form.reset();
  document.getElementById('site-modal-id').value = '';
  document.getElementById('site-modal-title').textContent = 'Create Construction Site';
  openModal('site-modal');
}

function openEditSiteModal(id) {
  const site = allSites.find(s => s.id === id);
  if (!site) return;

  document.getElementById('site-modal-id').value = site.id;
  document.getElementById('site-modal-title').textContent = 'Edit Construction Site';
  document.getElementById('site-project-id').value = site.project_id;
  document.getElementById('site-name').value = site.site_name;
  document.getElementById('site-location').value = site.location;
  document.getElementById('site-address').value = site.address || '';
  document.getElementById('site-manager-id').value = site.site_manager_id || '';
  document.getElementById('site-status').value = site.status;

  openModal('site-modal');
}

async function handleSaveSite(e) {
  e.preventDefault();
  const id = document.getElementById('site-modal-id').value;

  const payload = {
    project_id: parseInt(document.getElementById('site-project-id').value, 10),
    site_name: document.getElementById('site-name').value,
    location: document.getElementById('site-location').value,
    address: document.getElementById('site-address').value,
    site_manager_id: document.getElementById('site-manager-id').value ? parseInt(document.getElementById('site-manager-id').value, 10) : null,
    status: document.getElementById('site-status').value
  };

  try {
    if (id) {
      await apiRequest(`/sites/${id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      showToast('Site updated successfully.', 'success');
    } else {
      await apiRequest('/sites', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      showToast('New site added successfully.', 'success');
    }

    closeModal('site-modal');
    loadSites();
  } catch (err) {
    showToast(err.message || 'Error saving site', 'error');
  }
}

async function handleDeleteSite(id) {
  if (!confirm('Are you sure you want to delete this site?')) return;
  try {
    await apiRequest(`/sites/${id}`, { method: 'DELETE' });
    showToast('Site deleted successfully.', 'success');
    loadSites();
  } catch (err) {
    showToast(err.message || 'Error deleting site', 'error');
  }
}
