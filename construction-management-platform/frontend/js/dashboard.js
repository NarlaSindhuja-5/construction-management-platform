/**
 * Dashboard View Logic
 * Fetches real database KPIs, progress metrics, and actionable alerts.
 */

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  loadDashboardData();
});

async function loadDashboardData() {
  await Promise.all([
    fetchSummaryKPIs(),
    fetchProjectsProgress(),
    fetchSystemAlerts()
  ]);
}

async function fetchSummaryKPIs() {
  try {
    const res = await apiRequest('/dashboard/summary');
    if (!res || !res.summary) return;
    const s = res.summary;

    const elActiveProjects = document.getElementById('stat-active-projects');
    const elActiveSites = document.getElementById('stat-active-sites');
    const elTotalWorkers = document.getElementById('stat-total-workers');
    const elAvailableMachinery = document.getElementById('stat-available-machinery');
    const elPendingBookings = document.getElementById('stat-pending-bookings');
    
    const elTotalBudget = document.getElementById('finance-total-budget');
    const elTotalSpent = document.getElementById('finance-total-spent');
    const elRemainingBudget = document.getElementById('finance-remaining-budget');
    const elUtilizationRate = document.getElementById('finance-utilization-rate');
    const elBudgetBar = document.getElementById('finance-progress-bar');

    if (elActiveProjects) elActiveProjects.textContent = s.activeProjects;
    if (elActiveSites) elActiveSites.textContent = s.activeSites;
    if (elTotalWorkers) elTotalWorkers.textContent = s.totalWorkers;
    if (elAvailableMachinery) elAvailableMachinery.textContent = s.availableMachinery;
    if (elPendingBookings) elPendingBookings.textContent = s.pendingBookings;

    if (elTotalBudget) elTotalBudget.textContent = formatINR(s.totalBudget);
    if (elTotalSpent) elTotalSpent.textContent = formatINR(s.totalExpenses);
    if (elRemainingBudget) elRemainingBudget.textContent = formatINR(s.remainingBudget);
    if (elUtilizationRate) elUtilizationRate.textContent = `${s.budgetUtilization}%`;

    if (elBudgetBar) {
      elBudgetBar.style.width = `${Math.min(100, s.budgetUtilization)}%`;
      if (s.budgetUtilization > 90) {
        elBudgetBar.className = 'progress-bar-fill danger';
      } else if (s.budgetUtilization > 75) {
        elBudgetBar.className = 'progress-bar-fill warning';
      } else {
        elBudgetBar.className = 'progress-bar-fill';
      }
    }
  } catch (err) {
    console.error('Error fetching dashboard summary:', err);
    showToast('Failed to load dashboard summary metrics', 'error');
  }
}

async function fetchProjectsProgress() {
  const container = document.getElementById('projects-progress-list');
  if (!container) return;

  try {
    const res = await apiRequest('/dashboard/projects');
    if (!res || !res.projects || res.projects.length === 0) {
      container.innerHTML = '<div class="empty-state"><p>No active projects found.</p></div>';
      return;
    }

    container.innerHTML = res.projects.map(p => {
      let barClass = 'progress-bar-fill';
      if (p.progressPercent >= 80) barClass = 'progress-bar-fill success';
      else if (p.progressPercent >= 40) barClass = 'progress-bar-fill';
      else barClass = 'progress-bar-fill warning';

      return `
        <div class="project-progress-item">
          <div class="project-progress-header">
            <div>
              <span class="project-progress-title">${escapeHtml(p.projectName)}</span>
              <span style="margin-left:0.5rem;">${getStatusBadge(p.status)}</span>
            </div>
            <span class="project-progress-percent">${p.progressPercent}%</span>
          </div>
          <div class="progress-bar-wrap">
            <div class="${barClass}" style="width: ${p.progressPercent}%"></div>
          </div>
          <div class="project-progress-meta">
            <span>Client: ${escapeHtml(p.clientName)}</span>
            <span>Spent: ${formatINR(p.spent)} / Budget: ${formatINR(p.budget)}</span>
          </div>
        </div>
      `;
    }).join('');
  } catch (err) {
    console.error('Error loading project progress:', err);
    container.innerHTML = '<p class="text-muted" style="padding:1rem;">Failed to load project progress.</p>';
  }
}

async function fetchSystemAlerts() {
  const container = document.getElementById('alerts-list');
  if (!container) return;

  try {
    const res = await apiRequest('/dashboard/alerts');
    if (!res || !res.alerts || res.alerts.length === 0) {
      container.innerHTML = `
        <div style="padding:1.5rem; text-align:center; color:var(--text-muted); background:#f8fafc; border-radius:var(--radius-md);">
          <span style="font-size:1.5rem; display:block; margin-bottom:0.25rem;">✓</span>
          <strong>All Systems Normal</strong>
          <p style="font-size:0.8rem; margin-top:0.25rem;">No critical material shortages, delay conflicts, or pending alerts.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = res.alerts.map(a => {
      let icon = 'ℹ';
      if (a.severity === 'CRITICAL') icon = '🚨';
      else if (a.severity === 'WARNING' || a.severity === 'HIGH') icon = '⚠️';
      else if (a.type === 'PENDING_BOOKING') icon = '🚜';

      return `
        <div class="alert-item severity-${a.severity}">
          <span class="alert-icon">${icon}</span>
          <div class="alert-content">
            <div class="alert-title">${escapeHtml(a.title)}</div>
            <div class="alert-desc">${escapeHtml(a.message)}</div>
          </div>
        </div>
      `;
    }).join('');
  } catch (err) {
    console.error('Error fetching alerts:', err);
    container.innerHTML = '<p class="text-muted">Failed to load alerts.</p>';
  }
}
