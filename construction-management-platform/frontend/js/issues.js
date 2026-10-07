/**
 * Issues & Breakdown Incident Tracking View Logic
 */

let allIssues = [];
let allProjects = [];
let allSites = [];
let allMachinery = [];

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  loadData();
  setupEventListeners();
});

function setupEventListeners() {
  document.getElementById('issue-status-filter')?.addEventListener('change', filterAndRenderIssues);
  document.getElementById('issue-priority-filter')?.addEventListener('change', filterAndRenderIssues);
  document.getElementById('issue-form')?.addEventListener('submit', handleSaveIssue);

  document.getElementById('issue-project-id')?.addEventListener('change', handleProjectChange);
}

async function loadData() {
  await Promise.all([
    loadProjectsDropdown(),
    loadMachineryDropdown(),
    loadIssues()
  ]);
}

async function loadProjectsDropdown() {
  try {
    const res = await apiRequest('/projects');
    if (res && res.projects) {
      allProjects = res.projects;
      const select = document.getElementById('issue-project-id');
      if (select) {
        select.innerHTML = '<option value="">Select Project</option>' +
          allProjects.map(p => `<option value="${p.id}">${escapeHtml(p.project_name)}</option>`).join('');
      }
    }
  } catch (err) {
    console.error('Error loading projects:', err);
  }
}

async function handleProjectChange() {
  const projectId = document.getElementById('issue-project-id')?.value;
  const siteSelect = document.getElementById('issue-site-id');
  if (!siteSelect) return;

  if (!projectId) {
    siteSelect.innerHTML = '<option value="">General / Select Site</option>';
    return;
  }

  try {
    const res = await apiRequest(`/sites?project_id=${projectId}`);
    if (res && res.sites) {
      allSites = res.sites;
      siteSelect.innerHTML = '<option value="">General Project Level / Select Site</option>' +
        allSites.map(s => `<option value="${s.id}">${escapeHtml(s.site_name)}</option>`).join('');
    }
  } catch (err) {
    console.error('Error loading sites:', err);
  }
}

async function loadMachineryDropdown() {
  try {
    const res = await apiRequest('/machinery?all=true');
    if (res && res.machinery) {
      allMachinery = res.machinery;
      const select = document.getElementById('issue-machine-id');
      if (select) {
        select.innerHTML = '<option value="">Not Machinery Related (General Site Issue)</option>' +
          allMachinery.map(m => `<option value="${m.id}">${escapeHtml(m.machine_name)} (${m.machine_type})</option>`).join('');
      }
    }
  } catch (err) {
    console.error('Error loading machinery:', err);
  }
}

async function loadIssues() {
  try {
    const res = await apiRequest('/issues');
    if (res && res.issues) {
      allIssues = res.issues;
      filterAndRenderIssues();
    }
  } catch (err) {
    showToast('Failed to load issues: ' + err.message, 'error');
  }
}

function filterAndRenderIssues() {
  const status = document.getElementById('issue-status-filter')?.value || '';
  const priority = document.getElementById('issue-priority-filter')?.value || '';

  const filtered = allIssues.filter(i => {
    const matchesStatus = !status || i.status === status;
    const matchesPriority = !priority || i.priority === priority;
    return matchesStatus && matchesPriority;
  });

  renderIssuesList(filtered);
}

function renderIssuesList(issues) {
  const tbody = document.getElementById('issues-tbody');
  if (!tbody) return;

  if (issues.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" class="empty-state">
          <div class="empty-state-icon">🛡️</div>
          <h3>No Site Issues or Breakdowns Reported</h3>
          <p>Report safety issues, equipment breakdowns, or supply delays to track resolutions.</p>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = issues.map(i => {
    return `
      <tr>
        <td>
          <span class="badge badge-info">${escapeHtml(i.issue_type)}</span>
          ${i.machine_name ? `<div style="font-size:0.75rem; color:var(--text-muted); margin-top:2px;">🚜 ${escapeHtml(i.machine_name)}</div>` : ''}
        </td>
        <td>
          <strong>${escapeHtml(i.description)}</strong>
          ${i.resolved_at ? `<div style="font-size:0.72rem; color:var(--color-success);">Resolved: ${formatDate(i.resolved_at)}</div>` : ''}
        </td>
        <td>
          <div><strong>${escapeHtml(i.project_name)}</strong></div>
          <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(i.site_name || 'General')}</div>
        </td>
        <td>${getPriorityBadge(i.priority)}</td>
        <td>${getStatusBadge(i.status)}</td>
        <td><span style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(i.reported_by_name || 'Site Engineer')}</span></td>
        <td style="white-space:nowrap;">
          ${i.status === 'OPEN' || i.status === 'IN_PROGRESS' ? `
            <button class="btn btn-success btn-sm" onclick="handleResolveIssue(${i.id})">Mark Resolved</button>
          ` : `
            <span class="text-muted" style="font-size:0.8rem;">✓ Closed</span>
          `}
        </td>
      </tr>
    `;
  }).join('');
}

function openCreateIssueModal() {
  const form = document.getElementById('issue-form');
  if (form) form.reset();
  openModal('issue-modal');
}

async function handleSaveIssue(e) {
  e.preventDefault();

  const payload = {
    project_id: parseInt(document.getElementById('issue-project-id').value, 10),
    site_id: document.getElementById('issue-site-id').value ? parseInt(document.getElementById('issue-site-id').value, 10) : null,
    machinery_id: document.getElementById('issue-machine-id').value ? parseInt(document.getElementById('issue-machine-id').value, 10) : null,
    issue_type: document.getElementById('issue-type').value,
    priority: document.getElementById('issue-priority').value,
    description: document.getElementById('issue-desc').value
  };

  try {
    await apiRequest('/issues', { method: 'POST', body: JSON.stringify(payload) });
    showToast('Issue recorded successfully.', 'success');
    closeModal('issue-modal');
    loadIssues();
  } catch (err) {
    showToast(err.message || 'Error recording issue', 'error');
  }
}

async function handleResolveIssue(id) {
  if (!confirm('Mark this issue as RESOLVED?')) return;
  try {
    await apiRequest(`/issues/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ status: 'RESOLVED' })
    });
    showToast('Issue marked as resolved.', 'success');
    loadIssues();
  } catch (err) {
    showToast(err.message || 'Error resolving issue', 'error');
  }
}
