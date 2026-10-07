/**
 * Projects Management View Logic
 */

let allProjects = [];

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  loadProjects();
  setupEventListeners();
});

function setupEventListeners() {
  const searchInput = document.getElementById('project-search');
  const statusFilter = document.getElementById('project-status-filter');
  const projectForm = document.getElementById('project-form');

  if (searchInput) {
    searchInput.addEventListener('input', () => filterAndRenderProjects());
  }

  if (statusFilter) {
    statusFilter.addEventListener('change', () => filterAndRenderProjects());
  }

  if (projectForm) {
    projectForm.addEventListener('submit', handleSaveProject);
  }
}

async function loadProjects() {
  try {
    const res = await apiRequest('/projects');
    if (res && res.projects) {
      allProjects = res.projects;
      filterAndRenderProjects();
    }
  } catch (err) {
    showToast('Failed to load projects: ' + err.message, 'error');
  }
}

function filterAndRenderProjects() {
  const searchTerm = (document.getElementById('project-search')?.value || '').toLowerCase();
  const statusValue = document.getElementById('project-status-filter')?.value || '';

  const filtered = allProjects.filter(p => {
    const matchesSearch = p.project_name.toLowerCase().includes(searchTerm) ||
                          p.client_name.toLowerCase().includes(searchTerm) ||
                          p.location.toLowerCase().includes(searchTerm);
    const matchesStatus = !statusValue || p.status === statusValue;
    return matchesSearch && matchesStatus;
  });

  renderProjectsList(filtered);
}

function renderProjectsList(projects) {
  const tbody = document.getElementById('projects-table-body');
  if (!tbody) return;

  if (projects.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="empty-state">
          <div class="empty-state-icon">📁</div>
          <h3>No Projects Found</h3>
          <p>Get started by creating your first construction project.</p>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = projects.map(p => {
    return `
      <tr>
        <td>
          <a href="/pages/project-details.html?id=${p.id}" style="font-weight:700; color:var(--accent-blue);">
            ${escapeHtml(p.project_name)}
          </a>
          <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(p.project_type)} • ${escapeHtml(p.location)}</div>
        </td>
        <td><strong>${escapeHtml(p.client_name)}</strong></td>
        <td>${formatDate(p.start_date)} to ${formatDate(p.end_date)}</td>
        <td><strong>${formatINR(p.budget)}</strong></td>
        <td>
          <div style="font-size:0.85rem; font-weight:600; color:${p.total_spent > p.budget ? 'var(--color-danger)' : 'var(--text-main)'}">
            ${formatINR(p.total_spent)}
          </div>
          <div style="font-size:0.72rem; color:var(--text-muted);">${p.budget_utilization}% used</div>
        </td>
        <td>
          <div style="display:flex; align-items:center; gap:0.5rem;">
            <div class="progress-bar-wrap" style="width:70px; margin-top:0;">
              <div class="progress-bar-fill" style="width:${p.progress_percent}%;"></div>
            </div>
            <span style="font-size:0.78rem; font-weight:700;">${p.progress_percent}%</span>
          </div>
          <div style="font-size:0.72rem; color:var(--text-muted); margin-top:2px;">${p.sites_count} Sites • ${p.tasks_count} Tasks</div>
        </td>
        <td>${getStatusBadge(p.status)}</td>
        <td style="white-space:nowrap;">
          <a href="/pages/project-details.html?id=${p.id}" class="btn btn-outline btn-sm" title="View Project Details">
            View
          </a>
          <button class="btn btn-secondary btn-sm" onclick="openEditProjectModal(${p.id})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="handleDeleteProject(${p.id})">Delete</button>
        </td>
      </tr>
    `;
  }).join('');
}

function openCreateProjectModal() {
  const form = document.getElementById('project-form');
  if (form) form.reset();
  document.getElementById('project-modal-id').value = '';
  document.getElementById('project-modal-title').textContent = 'Create New Construction Project';
  openModal('project-modal');
}

function openEditProjectModal(id) {
  const project = allProjects.find(p => p.id === id);
  if (!project) return;

  document.getElementById('project-modal-id').value = project.id;
  document.getElementById('project-modal-title').textContent = 'Edit Construction Project';
  document.getElementById('project-name').value = project.project_name;
  document.getElementById('client-name').value = project.client_name;
  document.getElementById('project-type').value = project.project_type;
  document.getElementById('project-location').value = project.location;
  document.getElementById('start-date').value = project.start_date ? project.start_date.substring(0, 10) : '';
  document.getElementById('end-date').value = project.end_date ? project.end_date.substring(0, 10) : '';
  document.getElementById('project-budget').value = project.budget;
  document.getElementById('project-status').value = project.status;
  document.getElementById('project-description').value = project.description || '';

  openModal('project-modal');
}

async function handleSaveProject(e) {
  e.preventDefault();
  const id = document.getElementById('project-modal-id').value;

  const payload = {
    project_name: document.getElementById('project-name').value,
    client_name: document.getElementById('client-name').value,
    project_type: document.getElementById('project-type').value,
    location: document.getElementById('project-location').value,
    start_date: document.getElementById('start-date').value,
    end_date: document.getElementById('end-date').value,
    budget: parseFloat(document.getElementById('project-budget').value || 0),
    status: document.getElementById('project-status').value,
    description: document.getElementById('project-description').value
  };

  try {
    if (id) {
      await apiRequest(`/projects/${id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      showToast('Project updated successfully.', 'success');
    } else {
      await apiRequest('/projects', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      showToast('New project created successfully.', 'success');
    }

    closeModal('project-modal');
    loadProjects();
  } catch (err) {
    showToast(err.message || 'Error saving project', 'error');
  }
}

async function handleDeleteProject(id) {
  if (!confirm('Are you sure you want to delete this project? All associated sites, tasks, and records will be removed.')) {
    return;
  }

  try {
    await apiRequest(`/projects/${id}`, { method: 'DELETE' });
    showToast('Project deleted successfully.', 'success');
    loadProjects();
  } catch (err) {
    showToast(err.message || 'Error deleting project', 'error');
  }
}
