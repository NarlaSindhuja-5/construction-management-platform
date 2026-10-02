/**
 * Machinery Marketplace & Fleet Management View Logic
 */

let allMachinery = [];
let selectedMachineForBooking = null;

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  loadMachinery();
  setupEventListeners();
  checkRoleViewCustomizations();
});

function checkRoleViewCustomizations() {
  const user = getAuthUser();
  const registerBtn = document.getElementById('btn-register-machinery');
  if (registerBtn) {
    if (user && (user.role === 'OWNER' || user.role === 'ADMIN')) {
      registerBtn.style.display = 'inline-flex';
    } else {
      registerBtn.style.display = 'none';
    }
  }
}

function setupEventListeners() {
  document.getElementById('machine-search')?.addEventListener('input', applyFilters);
  document.getElementById('filter-type')?.addEventListener('change', applyFilters);
  document.getElementById('filter-location')?.addEventListener('input', applyFilters);
  document.getElementById('filter-operator')?.addEventListener('change', applyFilters);
  document.getElementById('filter-start-date')?.addEventListener('change', handleDateFilterChange);
  document.getElementById('filter-end-date')?.addEventListener('change', handleDateFilterChange);
  
  document.getElementById('machinery-form')?.addEventListener('submit', handleSaveMachinery);
  document.getElementById('availability-form')?.addEventListener('submit', handleCheckAvailabilityModal);
}

async function loadMachinery() {
  try {
    const sDate = document.getElementById('filter-start-date')?.value;
    const eDate = document.getElementById('filter-end-date')?.value;

    let url = '/machinery?all=true';
    if (sDate && eDate) {
      url += `&startDate=${sDate}&endDate=${eDate}`;
    }

    const res = await apiRequest(url);
    if (res && res.machinery) {
      allMachinery = res.machinery;
      applyFilters();
    }
  } catch (err) {
    showToast('Failed to load machinery: ' + err.message, 'error');
  }
}

function handleDateFilterChange() {
  const sDate = document.getElementById('filter-start-date')?.value;
  const eDate = document.getElementById('filter-end-date')?.value;
  if (sDate && eDate) {
    loadMachinery();
  } else {
    applyFilters();
  }
}

function applyFilters() {
  const search = (document.getElementById('machine-search')?.value || '').toLowerCase();
  const type = document.getElementById('filter-type')?.value || '';
  const loc = (document.getElementById('filter-location')?.value || '').toLowerCase();
  const opOnly = document.getElementById('filter-operator')?.checked;

  const filtered = allMachinery.filter(m => {
    const matchesSearch = m.machine_name.toLowerCase().includes(search) ||
                          m.registration_number.toLowerCase().includes(search) ||
                          m.machine_type.toLowerCase().includes(search);
    const matchesType = !type || m.machine_type === type;
    const matchesLoc = !loc || m.location.toLowerCase().includes(loc);
    const matchesOp = !opOnly || m.operator_available === 1;

    return matchesSearch && matchesType && matchesLoc && matchesOp;
  });

  renderMachineryCards(filtered);
}

function renderMachineryCards(machines) {
  const grid = document.getElementById('machinery-grid');
  if (!grid) return;

  if (machines.length === 0) {
    grid.innerHTML = `
      <div style="grid-column: 1/-1;" class="empty-state">
        <div class="empty-state-icon">🚜</div>
        <h3>No Machinery Found</h3>
        <p>No machines match the selected filter criteria or availability dates.</p>
      </div>
    `;
    return;
  }

  const currentUser = getAuthUser();

  grid.innerHTML = machines.map(m => {
    let dateStatusBadge = '';
    if (m.is_available_for_dates !== undefined) {
      dateStatusBadge = m.is_available_for_dates
        ? '<span class="badge badge-success" style="margin-left:0.5rem;">Available on Selected Dates</span>'
        : '<span class="badge badge-danger" style="margin-left:0.5rem;">Booked on Selected Dates</span>';
    }

    const isOwner = currentUser && (currentUser.id === m.owner_id || currentUser.role === 'ADMIN');

    return `
      <div class="machine-card">
        <div class="machine-card-header">
          <div>
            <span class="badge badge-info">${escapeHtml(m.machine_type)}</span>
            ${dateStatusBadge}
            <div class="machine-name-title" style="margin-top:0.35rem;">${escapeHtml(m.machine_name)}</div>
          </div>
          ${getStatusBadge(m.status)}
        </div>
        <div class="machine-card-body">
          <div class="machine-spec-row">
            <span>Registration No:</span>
            <span class="machine-spec-val">${escapeHtml(m.registration_number)}</span>
          </div>
          <div class="machine-spec-row">
            <span>Location:</span>
            <span class="machine-spec-val">${escapeHtml(m.location)}</span>
          </div>
          <div class="machine-spec-row">
            <span>Operator:</span>
            <span class="machine-spec-val">${m.operator_available ? 'Available (+₹' + m.operator_charge + '/day)' : 'Not Provided'}</span>
          </div>
          <div class="machine-spec-row">
            <span>Fleet Owner:</span>
            <span class="machine-spec-val">${escapeHtml(m.owner_name || 'Independent Owner')}</span>
          </div>
          <p style="font-size:0.8rem; color:var(--text-muted); margin-top:0.5rem; line-height:1.4;">
            ${escapeHtml(m.description || 'Standard high-performance construction equipment.')}
          </p>
          <div class="machine-rate-box">
            <span style="font-size:0.75rem; font-weight:700; color:var(--text-muted); text-transform:uppercase;">Daily Rental</span>
            <div><span class="rate-amt">${formatINR(m.daily_rate)}</span><span style="font-size:0.75rem; color:var(--text-muted);"> / day</span></div>
          </div>
        </div>
        <div class="machine-card-actions">
          <button class="btn btn-outline btn-sm" onclick="openAvailabilityModal(${m.id})">
            📅 Check Dates
          </button>
          <a href="/pages/bookings.html?machineId=${m.id}" class="btn btn-primary btn-sm" style="flex:1;">
            Book Machine
          </a>
          ${isOwner ? `
            <button class="btn btn-secondary btn-sm" onclick="openEditMachineryModal(${m.id})" title="Edit Details">⚙️</button>
          ` : ''}
        </div>
      </div>
    `;
  }).join('');
}

function openCreateMachineryModal() {
  const form = document.getElementById('machinery-form');
  if (form) form.reset();
  document.getElementById('machinery-modal-id').value = '';
  document.getElementById('machinery-modal-title').textContent = 'Register New Machinery';
  openModal('machinery-modal');
}

function openEditMachineryModal(id) {
  const machine = allMachinery.find(m => m.id === id);
  if (!machine) return;

  document.getElementById('machinery-modal-id').value = machine.id;
  document.getElementById('machinery-modal-title').textContent = 'Update Machinery Equipment';
  document.getElementById('machine-name-input').value = machine.machine_name;
  document.getElementById('machine-type-input').value = machine.machine_type;
  document.getElementById('machine-reg-input').value = machine.registration_number;
  document.getElementById('machine-loc-input').value = machine.location;
  document.getElementById('machine-rate-input').value = machine.daily_rate;
  document.getElementById('machine-operator-avail').checked = !!machine.operator_available;
  document.getElementById('machine-operator-charge').value = machine.operator_charge || 0;
  document.getElementById('machine-status-input').value = machine.status;
  document.getElementById('machine-desc-input').value = machine.description || '';

  openModal('machinery-modal');
}

async function handleSaveMachinery(e) {
  e.preventDefault();
  const id = document.getElementById('machinery-modal-id').value;

  const payload = {
    machine_name: document.getElementById('machine-name-input').value,
    machine_type: document.getElementById('machine-type-input').value,
    registration_number: document.getElementById('machine-reg-input').value,
    location: document.getElementById('machine-loc-input').value,
    daily_rate: parseFloat(document.getElementById('machine-rate-input').value),
    operator_available: document.getElementById('machine-operator-avail').checked,
    operator_charge: parseFloat(document.getElementById('machine-operator-charge').value || 0),
    status: document.getElementById('machine-status-input').value,
    description: document.getElementById('machine-desc-input').value
  };

  try {
    if (id) {
      await apiRequest(`/machinery/${id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      showToast('Machinery updated successfully.', 'success');
    } else {
      await apiRequest('/machinery', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      showToast('Machinery registered successfully.', 'success');
    }

    closeModal('machinery-modal');
    loadMachinery();
  } catch (err) {
    showToast(err.message || 'Error saving machinery', 'error');
  }
}

function openAvailabilityModal(machineId) {
  const machine = allMachinery.find(m => m.id === machineId);
  if (!machine) return;

  document.getElementById('avail-machine-id').value = machine.id;
  document.getElementById('avail-modal-title').textContent = `Check Availability: ${machine.machine_name}`;
  document.getElementById('avail-result-box').innerHTML = '';
  document.getElementById('avail-result-box').style.display = 'none';

  // Default dates: next 5 days
  const today = new Date();
  const nextWeek = new Date();
  nextWeek.setDate(today.getDate() + 7);

  document.getElementById('avail-start-date').value = today.toISOString().substring(0, 10);
  document.getElementById('avail-end-date').value = nextWeek.toISOString().substring(0, 10);

  openModal('availability-modal');
}

async function handleCheckAvailabilityModal(e) {
  e.preventDefault();
  const machineId = document.getElementById('avail-machine-id').value;
  const startDate = document.getElementById('avail-start-date').value;
  const endDate = document.getElementById('avail-end-date').value;
  const resultBox = document.getElementById('avail-result-box');

  if (!startDate || !endDate) {
    showToast('Please select both start and end dates', 'warning');
    return;
  }

  try {
    const res = await apiRequest(`/machinery/${machineId}/availability?startDate=${startDate}&endDate=${endDate}`);
    resultBox.style.display = 'block';

    if (res.available) {
      resultBox.innerHTML = `
        <div style="padding:1rem; background-color:var(--color-success-bg); border-left:4px solid var(--color-success); border-radius:var(--radius-sm); color:#065f46;">
          <strong>✓ AVAILABLE FOR BOOKING!</strong>
          <p style="font-size:0.85rem; margin-top:0.25rem;">
            This machine is free from <strong>${formatDate(startDate)}</strong> to <strong>${formatDate(endDate)}</strong> (${calculateDays(startDate, endDate)} Days).
          </p>
          <div style="margin-top:0.75rem;">
            <a href="/pages/bookings.html?machineId=${machineId}&startDate=${startDate}&endDate=${endDate}" class="btn btn-success btn-sm">
              Proceed to Book Machine
            </a>
          </div>
        </div>
      `;
    } else {
      const conflictItems = res.conflicts.map(c => `
        <li style="margin-bottom:0.25rem;">
          <strong>${c.type}:</strong> ${formatDate(c.startDate)} to ${formatDate(c.endDate)} - ${escapeHtml(c.description || '')}
        </li>
      `).join('');

      resultBox.innerHTML = `
        <div style="padding:1rem; background-color:var(--color-danger-bg); border-left:4px solid var(--color-danger); border-radius:var(--radius-sm); color:#991b1b;">
          <strong>⚠️ DATES NOT AVAILABLE (BOOKING CONFLICT DETECTED)</strong>
          <p style="font-size:0.85rem; margin-top:0.25rem;">
            The system detected conflicting reservations or scheduled maintenance:
          </p>
          <ul style="font-size:0.8rem; margin-left:1.25rem; margin-top:0.35rem;">
            ${conflictItems}
          </ul>
          <p style="font-size:0.78rem; margin-top:0.5rem; color:#7f1d1d;">
            Please select an alternative date window where this machine is free.
          </p>
        </div>
      `;
    }
  } catch (err) {
    showToast('Failed to check availability: ' + err.message, 'error');
  }
}
