/**
 * Daily Site Progress View Logic
 */

let allProgress = [];
let allProjects = [];
let allSites = [];
let allTasks = [];

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  loadData();
  setupEventListeners();
});

function setupEventListeners() {
  document.getElementById('progress-project-filter')?.addEventListener('change', filterAndRenderProgress);
  document.getElementById('progress-date-filter')?.addEventListener('change', filterAndRenderProgress);
  document.getElementById('progress-form')?.addEventListener('submit', handleSaveProgress);

  document.getElementById('progress-project-id')?.addEventListener('change', handleProjectChange);
  document.getElementById('progress-site-id')?.addEventListener('change', handleSiteChange);

  const slider = document.getElementById('progress-percent-slider');
  const valDisplay = document.getElementById('progress-percent-display');
  if (slider && valDisplay) {
    slider.addEventListener('input', (e) => {
      valDisplay.textContent = `${e.target.value}%`;
    });
  }
}

async function loadData() {
  await Promise.all([
    loadProjectsDropdown(),
    loadProgressList()
  ]);
}

async function loadProjectsDropdown() {
  try {
    const res = await apiRequest('/projects');
    if (res && res.projects) {
      allProjects = res.projects;
      const filterEl = document.getElementById('progress-project-filter');
      const formEl = document.getElementById('progress-project-id');

      const options = allProjects.map(p => `<option value="${p.id}">${escapeHtml(p.project_name)}</option>`).join('');
      if (filterEl) filterEl.innerHTML = '<option value="">All Projects</option>' + options;
      if (formEl) formEl.innerHTML = '<option value="">Select Project</option>' + options;
    }
  } catch (err) {
    console.error('Error loading projects:', err);
  }
}

async function handleProjectChange() {
  const projectId = document.getElementById('progress-project-id')?.value;
  const siteSelect = document.getElementById('progress-site-id');
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

async function handleSiteChange() {
  const siteId = document.getElementById('progress-site-id')?.value;
  const projectId = document.getElementById('progress-project-id')?.value;
  const taskSelect = document.getElementById('progress-task-id');
  if (!taskSelect) return;

  if (!siteId) {
    taskSelect.innerHTML = '<option value="">Select Task (Optional)</option>';
    return;
  }

  try {
    const res = await apiRequest(`/tasks?site_id=${siteId}&project_id=${projectId}`);
    if (res && res.tasks) {
      allTasks = res.tasks;
      taskSelect.innerHTML = '<option value="">Select Task (Optional)</option>' +
        allTasks.map(t => `<option value="${t.id}">${escapeHtml(t.task_name)} (Current: ${t.progress_percent}%)</option>`).join('');
    }
  } catch (err) {
    console.error('Error loading tasks:', err);
  }
}

async function loadProgressList() {
  try {
    const res = await apiRequest('/progress');
    if (res && res.progress) {
      allProgress = res.progress;
      filterAndRenderProgress();
    }
  } catch (err) {
    showToast('Failed to load progress records: ' + err.message, 'error');
  }
}

function filterAndRenderProgress() {
  const proj = document.getElementById('progress-project-filter')?.value || '';
  const date = document.getElementById('progress-date-filter')?.value || '';

  const filtered = allProgress.filter(p => {
    const matchesProj = !proj || String(p.project_id) === String(proj);
    const matchesDate = !date || p.progress_date.substring(0, 10) === date;
    return matchesProj && matchesDate;
  });

  renderProgressList(filtered);
}

function renderProgressList(items) {
  const tbody = document.getElementById('progress-tbody');
  if (!tbody) return;

  if (items.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" class="empty-state">
          <div class="empty-state-icon">📈</div>
          <h3>No Daily Progress Logs Recorded</h3>
          <p>Record daily site achievements, labor muster counts, and activity updates.</p>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = items.map(p => `
    <tr>
      <td><strong>${formatDate(p.progress_date)}</strong></td>
      <td>
        <div><strong>${escapeHtml(p.project_name)}</strong></div>
        <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(p.site_name)}</div>
      </td>
      <td>${p.task_name ? escapeHtml(p.task_name) : '<span class="text-muted">General Site Work</span>'}</td>
      <td><span class="badge badge-info">${p.workers_present} Present</span></td>
      <td>
        <div>${escapeHtml(p.work_completed)}</div>
        ${p.issues ? `<div style="font-size:0.75rem; color:var(--color-danger); margin-top:0.25rem;"><strong>Issues:</strong> ${escapeHtml(p.issues)}</div>` : ''}
        ${p.remarks ? `<div style="font-size:0.75rem; color:var(--text-muted); margin-top:0.25rem;"><strong>Remarks:</strong> ${escapeHtml(p.remarks)}</div>` : ''}
      </td>
      <td>
        <div style="display:flex; align-items:center; gap:0.5rem;">
          <div class="progress-bar-wrap" style="width:60px; margin-top:0;">
            <div class="progress-bar-fill" style="width:${p.progress_percent}%;"></div>
          </div>
          <span style="font-size:0.8rem; font-weight:700;">${p.progress_percent}%</span>
        </div>
      </td>
      <td><span style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(p.recorded_by_name || 'Site Manager')}</span></td>
    </tr>
  `).join('');
}

function openCreateProgressModal() {
  const form = document.getElementById('progress-form');
  if (form) form.reset();
  document.getElementById('progress-date').value = new Date().toISOString().substring(0, 10);
  document.getElementById('progress-percent-slider').value = 50;
  document.getElementById('progress-percent-display').textContent = '50%';
  openModal('progress-modal');
}

async function handleSaveProgress(e) {
  e.preventDefault();

  const payload = {
    project_id: parseInt(document.getElementById('progress-project-id').value, 10),
    site_id: parseInt(document.getElementById('progress-site-id').value, 10),
    task_id: document.getElementById('progress-task-id').value ? parseInt(document.getElementById('progress-task-id').value, 10) : null,
    progress_date: document.getElementById('progress-date').value,
    workers_present: parseInt(document.getElementById('progress-workers-present').value || 0, 10),
    work_completed: document.getElementById('progress-work-completed').value,
    progress_percent: parseInt(document.getElementById('progress-percent-slider').value || 0, 10),
    issues: document.getElementById('progress-issues').value,
    remarks: document.getElementById('progress-remarks').value
  };

  try {
    await apiRequest('/progress', { method: 'POST', body: JSON.stringify(payload) });
    showToast('Daily progress recorded successfully! Task status synced.', 'success');
    closeModal('progress-modal');
    loadProgressList();
  } catch (err) {
    showToast(err.message || 'Error recording daily progress', 'error');
  }
}
