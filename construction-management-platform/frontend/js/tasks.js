/**
 * Tasks View Logic
 */

let allTasks = [];
let allProjects = [];
let allSites = [];
let allWorkers = [];

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  loadData();
  setupEventListeners();
});

function setupEventListeners() {
  document.getElementById('task-search')?.addEventListener('input', filterAndRenderTasks);
  document.getElementById('task-project-filter')?.addEventListener('change', filterAndRenderTasks);
  document.getElementById('task-status-filter')?.addEventListener('change', filterAndRenderTasks);
  document.getElementById('task-priority-filter')?.addEventListener('change', filterAndRenderTasks);
  document.getElementById('task-form')?.addEventListener('submit', handleSaveTask);

  document.getElementById('task-project-id')?.addEventListener('change', handleProjectChange);

  const progressSlider = document.getElementById('task-progress-range');
  const progressVal = document.getElementById('task-progress-val');
  if (progressSlider && progressVal) {
    progressSlider.addEventListener('input', (e) => {
      progressVal.textContent = `${e.target.value}%`;
    });
  }
}

async function loadData() {
  await Promise.all([
    loadProjectsDropdown(),
    loadWorkersDropdown(),
    loadTasks()
  ]);
}

async function loadProjectsDropdown() {
  try {
    const res = await apiRequest('/projects');
    if (res && res.projects) {
      allProjects = res.projects;
      const filterEl = document.getElementById('task-project-filter');
      const formEl = document.getElementById('task-project-id');

      const options = allProjects.map(p => `<option value="${p.id}">${escapeHtml(p.project_name)}</option>`).join('');
      if (filterEl) filterEl.innerHTML = '<option value="">All Projects</option>' + options;
      if (formEl) formEl.innerHTML = '<option value="">Select Project</option>' + options;
    }
  } catch (err) {
    console.error('Error loading projects:', err);
  }
}

async function handleProjectChange() {
  const projectId = document.getElementById('task-project-id')?.value;
  const siteSelect = document.getElementById('task-site-id');
  if (!siteSelect) return;

  if (!projectId) {
    siteSelect.innerHTML = '<option value="">Select Site (Choose Project first)</option>';
    return;
  }

  try {
    const res = await apiRequest(`/sites?project_id=${projectId}`);
    if (res && res.sites) {
      allSites = res.sites;
      siteSelect.innerHTML = '<option value="">Select Site</option>' +
        allSites.map(s => `<option value="${s.id}">${escapeHtml(s.site_name)}</option>`).join('');
    }
  } catch (err) {
    console.error('Error loading sites:', err);
  }
}

async function loadWorkersDropdown() {
  try {
    const res = await apiRequest('/workers');
    if (res && res.workers) {
      allWorkers = res.workers;
      const workerSelect = document.getElementById('task-worker-id');
      if (workerSelect) {
        workerSelect.innerHTML = '<option value="">Unassigned</option>' +
          allWorkers.map(w => `<option value="${w.id}">${escapeHtml(w.name)} (${w.skill})</option>`).join('');
      }
    }
  } catch (err) {
    console.error('Error loading workers:', err);
  }
}

async function loadTasks() {
  try {
    const res = await apiRequest('/tasks');
    if (res && res.tasks) {
      allTasks = res.tasks;
      filterAndRenderTasks();
    }
  } catch (err) {
    showToast('Failed to load tasks: ' + err.message, 'error');
  }
}

function filterAndRenderTasks() {
  const search = (document.getElementById('task-search')?.value || '').toLowerCase();
  const proj = document.getElementById('task-project-filter')?.value || '';
  const status = document.getElementById('task-status-filter')?.value || '';
  const prio = document.getElementById('task-priority-filter')?.value || '';

  const filtered = allTasks.filter(t => {
    const matchesSearch = t.task_name.toLowerCase().includes(search) ||
                          (t.description && t.description.toLowerCase().includes(search));
    const matchesProj = !proj || String(t.project_id) === String(proj);
    const matchesStatus = !status || t.status === status;
    const matchesPrio = !prio || t.priority === prio;

    return matchesSearch && matchesProj && matchesStatus && matchesPrio;
  });

  renderTasksList(filtered);
}

function renderTasksList(tasks) {
  const tbody = document.getElementById('tasks-tbody');
  if (!tbody) return;

  if (tasks.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="empty-state">
          <div class="empty-state-icon">📝</div>
          <h3>No Tasks Found</h3>
          <p>Create work activity tasks and track completion milestones.</p>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = tasks.map(t => {
    return `
      <tr>
        <td>
          <strong>${escapeHtml(t.task_name)}</strong>
          <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(t.description || '')}</div>
        </td>
        <td>
          <div><strong>${escapeHtml(t.project_name)}</strong></div>
          <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(t.site_name)}</div>
        </td>
        <td>${escapeHtml(t.assigned_worker_name || 'Unassigned')}</td>
        <td>${formatDate(t.due_date)}</td>
        <td>${getPriorityBadge(t.priority)}</td>
        <td>
          <div style="display:flex; align-items:center; gap:0.5rem;">
            <div class="progress-bar-wrap" style="width:70px; margin-top:0;">
              <div class="progress-bar-fill" style="width:${t.progress_percent}%;"></div>
            </div>
            <span style="font-size:0.78rem; font-weight:700;">${t.progress_percent}%</span>
          </div>
        </td>
        <td>${getStatusBadge(t.status)}</td>
        <td style="white-space:nowrap;">
          <button class="btn btn-secondary btn-sm" onclick="openEditTaskModal(${t.id})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="handleDeleteTask(${t.id})">Delete</button>
        </td>
      </tr>
    `;
  }).join('');
}

function openCreateTaskModal() {
  const form = document.getElementById('task-form');
  if (form) form.reset();
  document.getElementById('task-modal-id').value = '';
  document.getElementById('task-modal-title').textContent = 'Create Construction Task';
  document.getElementById('task-progress-range').value = 0;
  document.getElementById('task-progress-val').textContent = '0%';
  openModal('task-modal');
}

async function openEditTaskModal(id) {
  const task = allTasks.find(t => t.id === id);
  if (!task) return;

  document.getElementById('task-modal-id').value = task.id;
  document.getElementById('task-modal-title').textContent = 'Edit Task';
  document.getElementById('task-project-id').value = task.project_id;
  await handleProjectChange();

  document.getElementById('task-site-id').value = task.site_id;
  document.getElementById('task-name').value = task.task_name;
  document.getElementById('task-description').value = task.description || '';
  document.getElementById('task-worker-id').value = task.assigned_worker_id || '';
  document.getElementById('task-start-date').value = task.start_date ? task.start_date.substring(0, 10) : '';
  document.getElementById('task-due-date').value = task.due_date ? task.due_date.substring(0, 10) : '';
  document.getElementById('task-priority').value = task.priority;
  document.getElementById('task-status').value = task.status;
  document.getElementById('task-progress-range').value = task.progress_percent;
  document.getElementById('task-progress-val').textContent = `${task.progress_percent}%`;

  openModal('task-modal');
}

async function handleSaveTask(e) {
  e.preventDefault();
  const id = document.getElementById('task-modal-id').value;

  const payload = {
    project_id: parseInt(document.getElementById('task-project-id').value, 10),
    site_id: parseInt(document.getElementById('task-site-id').value, 10),
    task_name: document.getElementById('task-name').value,
    description: document.getElementById('task-description').value,
    assigned_worker_id: document.getElementById('task-worker-id').value ? parseInt(document.getElementById('task-worker-id').value, 10) : null,
    start_date: document.getElementById('task-start-date').value,
    due_date: document.getElementById('task-due-date').value,
    priority: document.getElementById('task-priority').value,
    status: document.getElementById('task-status').value,
    progress_percent: parseInt(document.getElementById('task-progress-range').value || 0, 10)
  };

  try {
    if (id) {
      await apiRequest(`/tasks/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      showToast('Task updated successfully.', 'success');
    } else {
      await apiRequest('/tasks', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Task created successfully.', 'success');
    }
    closeModal('task-modal');
    loadTasks();
  } catch (err) {
    showToast(err.message || 'Error saving task', 'error');
  }
}

async function handleDeleteTask(id) {
  if (!confirm('Are you sure you want to delete this task?')) return;
  try {
    await apiRequest(`/tasks/${id}`, { method: 'DELETE' });
    showToast('Task deleted.', 'success');
    loadTasks();
  } catch (err) {
    showToast(err.message || 'Error deleting task', 'error');
  }
}
