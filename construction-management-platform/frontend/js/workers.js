/**
 * Workers View Logic
 */

let allWorkers = [];
let allSites = [];

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  loadData();
  setupEventListeners();
});

function setupEventListeners() {
  document.getElementById('worker-search')?.addEventListener('input', filterAndRenderWorkers);
  document.getElementById('worker-skill-filter')?.addEventListener('change', filterAndRenderWorkers);
  document.getElementById('worker-avail-filter')?.addEventListener('change', filterAndRenderWorkers);
  document.getElementById('worker-form')?.addEventListener('submit', handleSaveWorker);
}

async function loadData() {
  await Promise.all([
    loadSitesDropdown(),
    loadWorkers()
  ]);
}

async function loadSitesDropdown() {
  try {
    const res = await apiRequest('/sites');
    if (res && res.sites) {
      allSites = res.sites;
      const siteSelect = document.getElementById('worker-site-id');
      if (siteSelect) {
        siteSelect.innerHTML = '<option value="">Unassigned (Available pool)</option>' +
          allSites.map(s => `<option value="${s.id}">${escapeHtml(s.site_name)} (${escapeHtml(s.project_name)})</option>`).join('');
      }
    }
  } catch (err) {
    console.error('Error loading sites:', err);
  }
}

async function loadWorkers() {
  try {
    const res = await apiRequest('/workers');
    if (res && res.workers) {
      allWorkers = res.workers;
      filterAndRenderWorkers();
    }
  } catch (err) {
    showToast('Failed to load workers: ' + err.message, 'error');
  }
}

function filterAndRenderWorkers() {
  const search = (document.getElementById('worker-search')?.value || '').toLowerCase();
  const skill = document.getElementById('worker-skill-filter')?.value || '';
  const avail = document.getElementById('worker-avail-filter')?.value || '';

  const filtered = allWorkers.filter(w => {
    const matchesSearch = w.name.toLowerCase().includes(search) ||
                          w.phone.toLowerCase().includes(search) ||
                          w.skill.toLowerCase().includes(search);
    const matchesSkill = !skill || w.skill === skill;
    const matchesAvail = !avail || w.availability === avail;

    return matchesSearch && matchesSkill && matchesAvail;
  });

  renderWorkersList(filtered);
}

function renderWorkersList(workers) {
  const tbody = document.getElementById('workers-tbody');
  if (!tbody) return;

  if (workers.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" class="empty-state">
          <div class="empty-state-icon">👷</div>
          <h3>No Workers Found</h3>
          <p>Add skilled personnel and laborers to your workforce registry.</p>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = workers.map(w => `
    <tr>
      <td>
        <strong>${escapeHtml(w.name)}</strong>
        <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(w.phone)}</div>
      </td>
      <td><span class="badge badge-info">${escapeHtml(w.skill)}</span></td>
      <td>${w.experience} Years</td>
      <td><strong>${formatINR(w.daily_wage)} / day</strong></td>
      <td>${getStatusBadge(w.availability)}</td>
      <td>
        ${w.site_name 
          ? `<div><strong>${escapeHtml(w.site_name)}</strong></div><div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(w.project_name || '')}</div>`
          : '<span class="text-muted">Unassigned</span>'
        }
      </td>
      <td style="white-space:nowrap;">
        <button class="btn btn-secondary btn-sm" onclick="openEditWorkerModal(${w.id})">Edit</button>
        <button class="btn btn-danger btn-sm" onclick="handleDeleteWorker(${w.id})">Delete</button>
      </td>
    </tr>
  `).join('');
}

function openCreateWorkerModal() {
  const form = document.getElementById('worker-form');
  if (form) form.reset();
  document.getElementById('worker-modal-id').value = '';
  document.getElementById('worker-modal-title').textContent = 'Add Worker / Operator';
  openModal('worker-modal');
}

function openEditWorkerModal(id) {
  const worker = allWorkers.find(w => w.id === id);
  if (!worker) return;

  document.getElementById('worker-modal-id').value = worker.id;
  document.getElementById('worker-modal-title').textContent = 'Edit Worker Profile';
  document.getElementById('worker-name').value = worker.name;
  document.getElementById('worker-phone').value = worker.phone;
  document.getElementById('worker-skill').value = worker.skill;
  document.getElementById('worker-experience').value = worker.experience;
  document.getElementById('worker-wage').value = worker.daily_wage;
  document.getElementById('worker-availability').value = worker.availability;
  document.getElementById('worker-site-id').value = worker.site_id || '';

  openModal('worker-modal');
}

async function handleSaveWorker(e) {
  e.preventDefault();
  const id = document.getElementById('worker-modal-id').value;

  const payload = {
    name: document.getElementById('worker-name').value,
    phone: document.getElementById('worker-phone').value,
    skill: document.getElementById('worker-skill').value,
    experience: parseInt(document.getElementById('worker-experience').value || 0, 10),
    daily_wage: parseFloat(document.getElementById('worker-wage').value),
    availability: document.getElementById('worker-availability').value,
    site_id: document.getElementById('worker-site-id').value ? parseInt(document.getElementById('worker-site-id').value, 10) : null
  };

  try {
    if (id) {
      await apiRequest(`/workers/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      showToast('Worker updated successfully.', 'success');
    } else {
      await apiRequest('/workers', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Worker added successfully.', 'success');
    }
    closeModal('worker-modal');
    loadWorkers();
  } catch (err) {
    showToast(err.message || 'Error saving worker', 'error');
  }
}

async function handleDeleteWorker(id) {
  if (!confirm('Are you sure you want to remove this worker?')) return;
  try {
    await apiRequest(`/workers/${id}`, { method: 'DELETE' });
    showToast('Worker removed.', 'success');
    loadWorkers();
  } catch (err) {
    showToast(err.message || 'Error deleting worker', 'error');
  }
}
