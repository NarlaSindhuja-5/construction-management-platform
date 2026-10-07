/**
 * Financial & Project Expense Tracking View Logic
 */

let allExpenses = [];
let allProjects = [];
let allSites = [];

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  loadData();
  setupEventListeners();
});

function setupEventListeners() {
  document.getElementById('expense-project-filter')?.addEventListener('change', filterAndRenderExpenses);
  document.getElementById('expense-category-filter')?.addEventListener('change', filterAndRenderExpenses);
  document.getElementById('expense-form')?.addEventListener('submit', handleSaveExpense);

  document.getElementById('expense-project-id')?.addEventListener('change', handleProjectChange);
}

async function loadData() {
  await Promise.all([
    loadProjectsDropdown(),
    loadExpenses()
  ]);
}

async function loadProjectsDropdown() {
  try {
    const res = await apiRequest('/projects');
    if (res && res.projects) {
      allProjects = res.projects;
      const filterEl = document.getElementById('expense-project-filter');
      const formEl = document.getElementById('expense-project-id');

      const options = allProjects.map(p => `<option value="${p.id}">${escapeHtml(p.project_name)}</option>`).join('');
      if (filterEl) filterEl.innerHTML = '<option value="">All Projects</option>' + options;
      if (formEl) formEl.innerHTML = '<option value="">Select Project</option>' + options;
    }
  } catch (err) {
    console.error('Error loading projects:', err);
  }
}

async function handleProjectChange() {
  const projectId = document.getElementById('expense-project-id')?.value;
  const siteSelect = document.getElementById('expense-site-id');
  if (!siteSelect) return;

  if (!projectId) {
    siteSelect.innerHTML = '<option value="">Select Site (Optional)</option>';
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

async function loadExpenses() {
  try {
    const res = await apiRequest('/expenses');
    if (res && res.expenses) {
      allExpenses = res.expenses;
      filterAndRenderExpenses();
    }
  } catch (err) {
    showToast('Failed to load expenses: ' + err.message, 'error');
  }
}

function filterAndRenderExpenses() {
  const proj = document.getElementById('expense-project-filter')?.value || '';
  const cat = document.getElementById('expense-category-filter')?.value || '';

  const filtered = allExpenses.filter(e => {
    const matchesProj = !proj || String(e.project_id) === String(proj);
    const matchesCat = !cat || e.category === cat;
    return matchesProj && matchesCat;
  });

  renderExpensesList(filtered);
  updateCategoryTotals(filtered);
}

function updateCategoryTotals(expenses) {
  const totals = {
    MACHINERY: 0,
    MATERIAL: 0,
    LABOUR: 0,
    FUEL: 0,
    TRANSPORT: 0,
    OTHER: 0
  };

  let grandTotal = 0;
  expenses.forEach(e => {
    const amt = parseFloat(e.amount || 0);
    grandTotal += amt;
    if (totals[e.category] !== undefined) {
      totals[e.category] += amt;
    } else {
      totals.OTHER += amt;
    }
  });

  const elGrand = document.getElementById('total-expenses-stat');
  const elMach = document.getElementById('category-machinery-total');
  const elMat = document.getElementById('category-material-total');
  const elLab = document.getElementById('category-labour-total');
  const elFuel = document.getElementById('category-fuel-total');

  if (elGrand) elGrand.textContent = formatINR(grandTotal);
  if (elMach) elMach.textContent = formatINR(totals.MACHINERY);
  if (elMat) elMat.textContent = formatINR(totals.MATERIAL);
  if (elLab) elLab.textContent = formatINR(totals.LABOUR);
  if (elFuel) elFuel.textContent = formatINR(totals.FUEL);
}

function renderExpensesList(expenses) {
  const tbody = document.getElementById('expenses-tbody');
  if (!tbody) return;

  if (expenses.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" class="empty-state">
          <div class="empty-state-icon">💰</div>
          <h3>No Expense Transactions Found</h3>
          <p>Approved machinery bookings and manual expenditures will appear here.</p>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = expenses.map(e => {
    const catBadges = {
      MACHINERY: '<span class="badge badge-purple">MACHINERY</span>',
      MATERIAL: '<span class="badge badge-warning">MATERIAL</span>',
      LABOUR: '<span class="badge badge-info">LABOUR</span>',
      FUEL: '<span class="badge badge-danger">FUEL</span>',
      TRANSPORT: '<span class="badge badge-neutral">TRANSPORT</span>',
      OTHER: '<span class="badge badge-neutral">OTHER</span>'
    };

    return `
      <tr>
        <td><strong>${formatDate(e.expense_date)}</strong></td>
        <td>${catBadges[e.category] || e.category}</td>
        <td>
          <strong>${escapeHtml(e.description)}</strong>
          ${e.reference_type ? `<div style="font-size:0.72rem; color:var(--text-muted);">Ref: ${escapeHtml(e.reference_type)} #${e.reference_id || ''}</div>` : ''}
        </td>
        <td>
          <div><strong>${escapeHtml(e.project_name)}</strong></div>
          <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(e.site_name || 'General Project Level')}</div>
        </td>
        <td><strong style="font-size:1.05rem; color:var(--accent-blue);">${formatINR(e.amount)}</strong></td>
        <td><span style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(e.created_by_name || 'Project Manager')}</span></td>
        <td>
          <button class="btn btn-danger btn-sm" onclick="handleDeleteExpense(${e.id})">Delete</button>
        </td>
      </tr>
    `;
  }).join('');
}

function openCreateExpenseModal() {
  const form = document.getElementById('expense-form');
  if (form) form.reset();
  document.getElementById('expense-date').value = new Date().toISOString().substring(0, 10);
  openModal('expense-modal');
}

async function handleSaveExpense(e) {
  e.preventDefault();

  const payload = {
    project_id: parseInt(document.getElementById('expense-project-id').value, 10),
    site_id: document.getElementById('expense-site-id').value ? parseInt(document.getElementById('expense-site-id').value, 10) : null,
    category: document.getElementById('expense-category').value,
    amount: parseFloat(document.getElementById('expense-amount').value),
    expense_date: document.getElementById('expense-date').value,
    description: document.getElementById('expense-desc').value,
    reference_type: 'MANUAL'
  };

  try {
    await apiRequest('/expenses', { method: 'POST', body: JSON.stringify(payload) });
    showToast('Expense recorded successfully.', 'success');
    closeModal('expense-modal');
    loadExpenses();
  } catch (err) {
    showToast(err.message || 'Error recording expense', 'error');
  }
}

async function handleDeleteExpense(id) {
  if (!confirm('Are you sure you want to remove this expense entry?')) return;
  try {
    await apiRequest(`/expenses/${id}`, { method: 'DELETE' });
    showToast('Expense removed.', 'success');
    loadExpenses();
  } catch (err) {
    showToast(err.message || 'Error deleting expense', 'error');
  }
}
