/**
 * Analytical Reports & Export Logic
 */

let allProjects = [];

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  initReports();
});

async function initReports() {
  await loadProjects();
  document.getElementById('report-project-select')?.addEventListener('change', (e) => {
    if (e.target.value) {
      loadProjectReport(e.target.value);
    }
  });

  document.getElementById('btn-print-report')?.addEventListener('click', () => {
    window.print();
  });
}

async function loadProjects() {
  try {
    const res = await apiRequest('/projects');
    if (res && res.projects) {
      allProjects = res.projects;
      const select = document.getElementById('report-project-select');
      if (select) {
        select.innerHTML = '<option value="">-- Select a Project to Generate Report --</option>' +
          allProjects.map(p => `<option value="${p.id}">${escapeHtml(p.project_name)}</option>`).join('');

        if (allProjects.length > 0) {
          select.value = allProjects[0].id;
          loadProjectReport(allProjects[0].id);
        }
      }
    }
  } catch (err) {
    showToast('Failed to load projects: ' + err.message, 'error');
  }
}

async function loadProjectReport(projectId) {
  const container = document.getElementById('report-output-container');
  if (!container) return;

  container.innerHTML = '<div style="padding:2rem; text-align:center;">Loading analytical project report...</div>';

  try {
    const res = await apiRequest(`/reports/project/${projectId}`);
    if (!res || !res.report) {
      container.innerHTML = '<div class="empty-state"><p>Report unavailable.</p></div>';
      return;
    }

    const { project, sites, tasks, machineryBookings, expensesByCategory } = res.report;

    const categoryRows = expensesByCategory.map(c => `
      <div style="display:flex; justify-content:space-between; padding:0.4rem 0; border-bottom:1px solid var(--border-light); font-size:0.85rem;">
        <span style="font-weight:600;">${c.category}:</span>
        <span>${formatINR(c.total_amount)} (${c.count} records)</span>
      </div>
    `).join('') || '<p class="text-muted" style="font-size:0.85rem;">No recorded expenses yet.</p>';

    const siteListHtml = sites.map(s => `
      <div style="padding:0.6rem; background:#f8fafc; border-radius:var(--radius-sm); border:1px solid var(--border-color); margin-bottom:0.5rem; font-size:0.85rem;">
        <strong>${escapeHtml(s.site_name)}</strong> (${escapeHtml(s.location)})
        <div style="font-size:0.75rem; color:var(--text-muted);">Manager: ${escapeHtml(s.site_manager_name || 'Unassigned')} | Status: ${s.status}</div>
      </div>
    `).join('') || '<p class="text-muted" style="font-size:0.85rem;">No sites configured.</p>';

    const bookingRows = machineryBookings.map(b => `
      <tr>
        <td><strong>${escapeHtml(b.machine_name)}</strong> (${escapeHtml(b.machine_type)})</td>
        <td>${escapeHtml(b.site_name)}</td>
        <td>${formatDate(b.start_date)} to ${formatDate(b.end_date)} (${b.total_days} Days)</td>
        <td><strong>${formatINR(b.total_amount)}</strong></td>
        <td>${getStatusBadge(b.status)}</td>
      </tr>
    `).join('') || '<tr><td colspan="5" class="text-muted" style="text-align:center; padding:1rem;">No machinery bookings for this project.</td></tr>';

    const taskRows = tasks.map(t => `
      <tr>
        <td>${escapeHtml(t.task_name)}</td>
        <td>${escapeHtml(t.site_name)}</td>
        <td>${formatDate(t.due_date)}</td>
        <td>${t.progress_percent}%</td>
        <td>${getStatusBadge(t.status)}</td>
      </tr>
    `).join('') || '<tr><td colspan="5" class="text-muted" style="text-align:center; padding:1rem;">No tasks defined for this project.</td></tr>';

    container.innerHTML = `
      <!-- Executive Summary Header -->
      <div class="card" style="margin-bottom:1.5rem;">
        <div class="card-header" style="background:#0f172a; color:#fff;">
          <div>
            <h2 style="font-size:1.35rem; font-weight:800; color:#fff;">${escapeHtml(project.project_name)}</h2>
            <div style="font-size:0.8rem; color:#94a3b8; margin-top:2px;">
              Client: ${escapeHtml(project.client_name)} • Location: ${escapeHtml(project.location)} • Schedule: ${formatDate(project.start_date)} to ${formatDate(project.end_date)}
            </div>
          </div>
          <div>${getStatusBadge(project.status)}</div>
        </div>
        <div class="card-body">
          <div class="finance-metrics-strip" style="margin-bottom:1.5rem;">
            <div class="finance-metric-item">
              <div class="f-label">Total Allocated Budget</div>
              <div class="f-val">${formatINR(project.budget)}</div>
            </div>
            <div class="finance-metric-item">
              <div class="f-label">Total Cumulative Spent</div>
              <div class="f-val spent">${formatINR(project.totalSpent)}</div>
            </div>
            <div class="finance-metric-item">
              <div class="f-label">Remaining Budget Balance</div>
              <div class="f-val positive">${formatINR(project.remainingBudget)}</div>
            </div>
          </div>

          <div style="margin-bottom:1.5rem;">
            <div style="display:flex; justify-content:space-between; font-weight:700; font-size:0.9rem; margin-bottom:0.35rem;">
              <span>Overall Project Progress</span>
              <span>${project.progressPercent}% (${project.completedTasks} / ${project.totalTasks} Tasks Completed)</span>
            </div>
            <div class="progress-bar-wrap" style="height:12px;">
              <div class="progress-bar-fill ${project.progressPercent >= 80 ? 'success' : ''}" style="width:${project.progressPercent}%"></div>
            </div>
          </div>

          <div style="display:grid; grid-template-columns:1fr 1fr; gap:1.5rem;">
            <div>
              <h4 style="font-size:0.95rem; font-weight:700; margin-bottom:0.75rem;">Active Construction Sites (${sites.length})</h4>
              ${siteListHtml}
            </div>
            <div>
              <h4 style="font-size:0.95rem; font-weight:700; margin-bottom:0.75rem;">Expenditure Breakdown</h4>
              <div style="background:#f8fafc; padding:0.85rem; border-radius:var(--radius-sm); border:1px solid var(--border-color);">
                ${categoryRows}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Machinery Bookings Section -->
      <div class="card" style="margin-bottom:1.5rem;">
        <div class="card-header">
          <h3 class="card-title">🚜 Machinery Resources Deployed</h3>
        </div>
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Machine</th>
                <th>Site</th>
                <th>Rental Dates</th>
                <th>Total Cost</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              ${bookingRows}
            </tbody>
          </table>
        </div>
      </div>

      <!-- Construction Tasks Section -->
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">📋 Scheduled Project Tasks</h3>
        </div>
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Task Activity</th>
                <th>Site</th>
                <th>Due Date</th>
                <th>Progress</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              ${taskRows}
            </tbody>
          </table>
        </div>
      </div>
    `;
  } catch (err) {
    showToast('Failed to generate report: ' + err.message, 'error');
    container.innerHTML = `<p class="text-danger" style="padding:1.5rem;">Error: ${err.message}</p>`;
  }
}
