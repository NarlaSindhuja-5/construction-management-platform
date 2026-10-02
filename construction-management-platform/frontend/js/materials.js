/**
 * Materials & Inventory View Logic
 */

let allMaterials = [];
let allSites = [];

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  loadData();
  setupEventListeners();
});

function setupEventListeners() {
  document.getElementById('material-search')?.addEventListener('input', filterAndRenderMaterials);
  document.getElementById('material-cat-filter')?.addEventListener('change', filterAndRenderMaterials);
  document.getElementById('material-stock-filter')?.addEventListener('change', filterAndRenderMaterials);
  document.getElementById('material-form')?.addEventListener('submit', handleSaveMaterial);
}

async function loadData() {
  await Promise.all([
    loadSitesDropdown(),
    loadMaterials()
  ]);
}

async function loadSitesDropdown() {
  try {
    const res = await apiRequest('/sites');
    if (res && res.sites) {
      allSites = res.sites;
      const select = document.getElementById('material-site-id');
      if (select) {
        select.innerHTML = '<option value="">Select Site</option>' +
          allSites.map(s => `<option value="${s.id}">${escapeHtml(s.site_name)} (${escapeHtml(s.project_name)})</option>`).join('');
      }
    }
  } catch (err) {
    console.error('Error loading sites:', err);
  }
}

async function loadMaterials() {
  try {
    const res = await apiRequest('/materials');
    if (res && res.materials) {
      allMaterials = res.materials;
      filterAndRenderMaterials();
    }
  } catch (err) {
    showToast('Failed to load materials: ' + err.message, 'error');
  }
}

function filterAndRenderMaterials() {
  const search = (document.getElementById('material-search')?.value || '').toLowerCase();
  const cat = document.getElementById('material-cat-filter')?.value || '';
  const lowOnly = document.getElementById('material-stock-filter')?.checked;

  const filtered = allMaterials.filter(m => {
    const matchesSearch = m.material_name.toLowerCase().includes(search) ||
                          m.category.toLowerCase().includes(search) ||
                          (m.supplier && m.supplier.toLowerCase().includes(search));
    const matchesCat = !cat || m.category === cat;
    const matchesLow = !lowOnly || m.is_low_stock;

    return matchesSearch && matchesCat && matchesLow;
  });

  renderMaterialsList(filtered);
}

function renderMaterialsList(materials) {
  const tbody = document.getElementById('materials-tbody');
  if (!tbody) return;

  if (materials.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="empty-state">
          <div class="empty-state-icon">📦</div>
          <h3>No Materials Found</h3>
          <p>Register raw materials, cement, steel, or aggregate to site inventories.</p>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = materials.map(m => {
    const rowClass = m.is_low_stock ? 'table-row-warning' : '';
    const stockBadge = m.is_low_stock 
      ? '<span class="badge badge-danger">⚠️ LOW STOCK</span>' 
      : '<span class="badge badge-success">IN STOCK</span>';

    return `
      <tr class="${rowClass}">
        <td>
          <strong>${escapeHtml(m.material_name)}</strong>
          <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(m.category)}</div>
        </td>
        <td>
          <div><strong>${escapeHtml(m.site_name)}</strong></div>
          <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(m.project_name || '')}</div>
        </td>
        <td>
          <div style="font-size:1.05rem; font-weight:700; color:${m.is_low_stock ? 'var(--color-danger)' : 'var(--text-main)'};">
            ${m.quantity} ${escapeHtml(m.unit)}
          </div>
          <div style="font-size:0.72rem; color:var(--text-muted);">Min: ${m.minimum_quantity} ${escapeHtml(m.unit)}</div>
        </td>
        <td>${stockBadge}</td>
        <td>${formatINR(m.unit_price)}</td>
        <td><strong>${formatINR(m.total_value)}</strong></td>
        <td>${escapeHtml(m.supplier || 'N/A')}</td>
        <td style="white-space:nowrap;">
          <button class="btn btn-secondary btn-sm" onclick="openEditMaterialModal(${m.id})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="handleDeleteMaterial(${m.id})">Delete</button>
        </td>
      </tr>
    `;
  }).join('');
}

function openCreateMaterialModal() {
  const form = document.getElementById('material-form');
  if (form) form.reset();
  document.getElementById('material-modal-id').value = '';
  document.getElementById('material-modal-title').textContent = 'Add Material Stock Item';
  openModal('material-modal');
}

function openEditMaterialModal(id) {
  const material = allMaterials.find(m => m.id === id);
  if (!material) return;

  document.getElementById('material-modal-id').value = material.id;
  document.getElementById('material-modal-title').textContent = 'Update Material Stock';
  document.getElementById('material-site-id').value = material.site_id;
  document.getElementById('material-name').value = material.material_name;
  document.getElementById('material-category').value = material.category;
  document.getElementById('material-quantity').value = material.quantity;
  document.getElementById('material-min-qty').value = material.minimum_quantity;
  document.getElementById('material-unit').value = material.unit;
  document.getElementById('material-unit-price').value = material.unit_price;
  document.getElementById('material-supplier').value = material.supplier || '';

  openModal('material-modal');
}

async function handleSaveMaterial(e) {
  e.preventDefault();
  const id = document.getElementById('material-modal-id').value;

  const payload = {
    site_id: parseInt(document.getElementById('material-site-id').value, 10),
    material_name: document.getElementById('material-name').value,
    category: document.getElementById('material-category').value,
    quantity: parseFloat(document.getElementById('material-quantity').value),
    minimum_quantity: parseFloat(document.getElementById('material-min-qty').value),
    unit: document.getElementById('material-unit').value,
    unit_price: parseFloat(document.getElementById('material-unit-price').value || 0),
    supplier: document.getElementById('material-supplier').value
  };

  try {
    if (id) {
      await apiRequest(`/materials/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      showToast('Material stock updated successfully.', 'success');
    } else {
      await apiRequest('/materials', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Material registered successfully.', 'success');
    }
    closeModal('material-modal');
    loadMaterials();
  } catch (err) {
    showToast(err.message || 'Error saving material', 'error');
  }
}

async function handleDeleteMaterial(id) {
  if (!confirm('Are you sure you want to delete this material item?')) return;
  try {
    await apiRequest(`/materials/${id}`, { method: 'DELETE' });
    showToast('Material deleted.', 'success');
    loadMaterials();
  } catch (err) {
    showToast(err.message || 'Error deleting material', 'error');
  }
}
