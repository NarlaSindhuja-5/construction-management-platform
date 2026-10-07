/**
 * Machinery Booking & Owner Approval View Logic
 * Handles interactive booking forms, live calculation summaries,
 * owner approvals/rejections, and conflict prevention alerts.
 */

let allBookings = [];
let allProjects = [];
let allSites = [];
let allMachinery = [];
let currentRejectBookingId = null;
let currentUsageBookingId = null;

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  loadData();
  setupEventListeners();
  checkUrlParamsForQuickBooking();
});

function setupEventListeners() {
  document.getElementById('booking-form')?.addEventListener('submit', handleSubmitBooking);
  document.getElementById('reject-form')?.addEventListener('submit', handleConfirmRejection);
  document.getElementById('usage-form')?.addEventListener('submit', handleSaveUsage);

  // Dynamic live calculation inputs
  const calcInputs = [
    'booking-machine-id',
    'booking-start-date',
    'booking-end-date',
    'booking-operator-required',
    'booking-transport-charge',
    'booking-additional-charge'
  ];

  calcInputs.forEach(id => {
    document.getElementById(id)?.addEventListener('input', calculateLiveBookingCost);
    document.getElementById(id)?.addEventListener('change', calculateLiveBookingCost);
  });

  document.getElementById('booking-project-id')?.addEventListener('change', handleProjectChange);
}

async function loadData() {
  await Promise.all([
    loadProjects(),
    loadMachinery(),
    loadBookings()
  ]);
}

async function loadProjects() {
  try {
    const res = await apiRequest('/projects');
    if (res && res.projects) {
      allProjects = res.projects;
      const projectSelect = document.getElementById('booking-project-id');
      if (projectSelect) {
        projectSelect.innerHTML = '<option value="">Select Project</option>' +
          allProjects.map(p => `<option value="${p.id}">${escapeHtml(p.project_name)}</option>`).join('');
      }
    }
  } catch (err) {
    console.error('Error loading projects:', err);
  }
}

async function handleProjectChange() {
  const projectId = document.getElementById('booking-project-id')?.value;
  const siteSelect = document.getElementById('booking-site-id');
  if (!siteSelect) return;

  if (!projectId) {
    siteSelect.innerHTML = '<option value="">Select Site (Choose Project first)</option>';
    return;
  }

  try {
    const res = await apiRequest(`/sites?project_id=${projectId}`);
    if (res && res.sites) {
      allSites = res.sites;
      siteSelect.innerHTML = '<option value="">Select Construction Site</option>' +
        allSites.map(s => `<option value="${s.id}">${escapeHtml(s.site_name)} (${escapeHtml(s.location)})</option>`).join('');
    }
  } catch (err) {
    console.error('Error loading sites for project:', err);
  }
}

async function loadMachinery() {
  try {
    const res = await apiRequest('/machinery?all=true');
    if (res && res.machinery) {
      allMachinery = res.machinery;
      const machineSelect = document.getElementById('booking-machine-id');
      if (machineSelect) {
        machineSelect.innerHTML = '<option value="">Select Machinery to Book</option>' +
          allMachinery.map(m => `
            <option value="${m.id}" data-rate="${m.daily_rate}" data-op-charge="${m.operator_charge || 0}" data-op-avail="${m.operator_available}">
              ${escapeHtml(m.machine_name)} (${m.machine_type}) - ${formatINR(m.daily_rate)}/day
            </option>
          `).join('');
      }
    }
  } catch (err) {
    console.error('Error loading machinery:', err);
  }
}

async function loadBookings() {
  try {
    const res = await apiRequest('/machinery-bookings');
    if (res && res.bookings) {
      allBookings = res.bookings;
      renderBookings();
    }
  } catch (err) {
    showToast('Failed to load bookings: ' + err.message, 'error');
  }
}

function calculateLiveBookingCost() {
  const machineSelect = document.getElementById('booking-machine-id');
  const selectedOption = machineSelect?.selectedOptions[0];
  const startDate = document.getElementById('booking-start-date')?.value;
  const endDate = document.getElementById('booking-end-date')?.value;
  const operatorRequired = document.getElementById('booking-operator-required')?.checked;
  const transportCharge = parseFloat(document.getElementById('booking-transport-charge')?.value || 0);
  const additionalCharge = parseFloat(document.getElementById('booking-additional-charge')?.value || 0);

  const elDays = document.getElementById('calc-total-days');
  const elDailyRate = document.getElementById('calc-daily-rate');
  const elBaseRental = document.getElementById('calc-base-rental');
  const elOpCharge = document.getElementById('calc-op-charge');
  const elTransCharge = document.getElementById('calc-trans-charge');
  const elAddCharge = document.getElementById('calc-add-charge');
  const elGrandTotal = document.getElementById('calc-grand-total');

  if (!selectedOption || !selectedOption.value || !startDate || !endDate) {
    if (elDays) elDays.textContent = '0 Days';
    if (elGrandTotal) elGrandTotal.textContent = '₹0';
    return;
  }

  const dailyRate = parseFloat(selectedOption.getAttribute('data-rate') || 0);
  const opDailyCharge = operatorRequired ? parseFloat(selectedOption.getAttribute('data-op-charge') || 0) : 0;

  const totalDays = calculateDays(startDate, endDate);
  const baseRental = dailyRate * totalDays;
  const totalOpCharge = opDailyCharge * totalDays;
  const grandTotal = baseRental + totalOpCharge + transportCharge + additionalCharge;

  if (elDays) elDays.textContent = `${totalDays} Day${totalDays === 1 ? '' : 's'}`;
  if (elDailyRate) elDailyRate.textContent = `${formatINR(dailyRate)} / day`;
  if (elBaseRental) elBaseRental.textContent = formatINR(baseRental);
  if (elOpCharge) elOpCharge.textContent = formatINR(totalOpCharge);
  if (elTransCharge) elTransCharge.textContent = formatINR(transportCharge);
  if (elAddCharge) elAddCharge.textContent = formatINR(additionalCharge);
  if (elGrandTotal) elGrandTotal.textContent = formatINR(grandTotal);
}

function renderBookings() {
  const user = getAuthUser();
  const isOwner = user && (user.role === 'OWNER' || user.role === 'ADMIN');

  // 1. Pending Approvals Section (Crucial for Owner Dashboard)
  const pendingContainer = document.getElementById('pending-requests-tbody');
  const pendingCard = document.getElementById('pending-requests-card');
  const pendingList = allBookings.filter(b => b.status === 'PENDING');

  if (pendingCard) {
    if (isOwner || pendingList.length > 0) {
      pendingCard.style.display = 'block';
    } else {
      pendingCard.style.display = 'none';
    }
  }

  if (pendingContainer) {
    if (pendingList.length === 0) {
      pendingContainer.innerHTML = `
        <tr>
          <td colspan="7" class="empty-state" style="padding:1.5rem;">
            <p style="margin:0;">No pending booking requests awaiting approval.</p>
          </td>
        </tr>
      `;
    } else {
      pendingContainer.innerHTML = pendingList.map(b => `
        <tr class="table-row-warning">
          <td>
            <strong>${escapeHtml(b.machine_name)}</strong>
            <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(b.machine_type)} • Reg: ${escapeHtml(b.registration_number)}</div>
          </td>
          <td>
            <strong>${escapeHtml(b.project_name)}</strong>
            <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(b.site_name)}</div>
          </td>
          <td>
            <strong>${escapeHtml(b.contractor_name)}</strong>
            <div style="font-size:0.72rem; color:var(--text-muted);">${b.contractor_phone || ''}</div>
          </td>
          <td>
            <div>${formatDate(b.start_date)} to ${formatDate(b.end_date)}</div>
            <div style="font-size:0.75rem; font-weight:700; color:var(--accent-blue);">${b.total_days} Days</div>
          </td>
          <td>
            <div style="font-size:1.05rem; font-weight:800; color:var(--text-main);">${formatINR(b.total_amount)}</div>
            <div style="font-size:0.72rem; color:var(--text-muted);">Rate: ${formatINR(b.daily_rate)}/day</div>
          </td>
          <td>${getStatusBadge(b.status)}</td>
          <td style="white-space:nowrap;">
            ${isOwner ? `
              <button class="btn btn-success btn-sm" onclick="handleApproveBooking(${b.id})">✓ Approve</button>
              <button class="btn btn-danger btn-sm" onclick="openRejectModal(${b.id})">✕ Reject</button>
            ` : `
              <button class="btn btn-outline btn-sm" onclick="handleCancelBooking(${b.id})">Cancel Request</button>
            `}
          </td>
        </tr>
      `).join('');
    }
  }

  // 2. All Confirmed / Past Bookings Table
  const allBookingsContainer = document.getElementById('all-bookings-tbody');
  if (allBookingsContainer) {
    if (allBookings.length === 0) {
      allBookingsContainer.innerHTML = `
        <tr>
          <td colspan="8" class="empty-state">
            <div class="empty-state-icon">📋</div>
            <h3>No Bookings Recorded</h3>
            <p>Select a machine to submit a date-based booking request.</p>
          </td>
        </tr>
      `;
      return;
    }

    allBookingsContainer.innerHTML = allBookings.map(b => {
      let actions = '';
      if (b.status === 'APPROVED') {
        actions = `<button class="btn btn-secondary btn-sm" onclick="openUsageModal(${b.id})">📝 Log Usage</button>`;
      } else if (b.status === 'PENDING') {
        actions = isOwner
          ? `<button class="btn btn-success btn-sm" onclick="handleApproveBooking(${b.id})">Approve</button>`
          : `<button class="btn btn-outline btn-sm" onclick="handleCancelBooking(${b.id})">Cancel</button>`;
      } else if (b.status === 'REJECTED' && b.rejection_reason) {
        actions = `<span style="font-size:0.75rem; color:var(--color-danger);" title="${escapeHtml(b.rejection_reason)}">Reason: ${escapeHtml(b.rejection_reason)}</span>`;
      }

      return `
        <tr>
          <td>
            <strong>${escapeHtml(b.machine_name)}</strong>
            <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(b.registration_number)}</div>
          </td>
          <td>
            <div><strong>${escapeHtml(b.project_name)}</strong></div>
            <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(b.site_name)}</div>
          </td>
          <td>${escapeHtml(b.contractor_name)}</td>
          <td>
            <div>${formatDate(b.start_date)} to ${formatDate(b.end_date)}</div>
            <div style="font-size:0.72rem; color:var(--text-muted);">${b.total_days} Days</div>
          </td>
          <td><strong>${formatINR(b.total_amount)}</strong></td>
          <td>${getStatusBadge(b.status)}</td>
          <td style="font-size:0.75rem; color:var(--text-muted);">${formatDate(b.requested_at)}</td>
          <td style="white-space:nowrap;">${actions}</td>
        </tr>
      `;
    }).join('');
  }
}

function openCreateBookingModal() {
  const form = document.getElementById('booking-form');
  if (form) form.reset();
  calculateLiveBookingCost();
  openModal('booking-modal');
}

function checkUrlParamsForQuickBooking() {
  const params = new URLSearchParams(window.location.search);
  const machineId = params.get('machineId');
  const startDate = params.get('startDate');
  const endDate = params.get('endDate');

  if (machineId) {
    setTimeout(() => {
      const machineSelect = document.getElementById('booking-machine-id');
      if (machineSelect) {
        machineSelect.value = machineId;
      }
      if (startDate) document.getElementById('booking-start-date').value = startDate;
      if (endDate) document.getElementById('booking-end-date').value = endDate;
      calculateLiveBookingCost();
      openModal('booking-modal');
    }, 500);
  }
}

async function handleSubmitBooking(e) {
  e.preventDefault();

  const machineId = document.getElementById('booking-machine-id').value;
  const projectId = document.getElementById('booking-project-id').value;
  const siteId = document.getElementById('booking-site-id').value;
  const startDate = document.getElementById('booking-start-date').value;
  const endDate = document.getElementById('booking-end-date').value;
  const operatorRequired = document.getElementById('booking-operator-required').checked;
  const transportCharge = document.getElementById('booking-transport-charge').value || 0;
  const additionalCharge = document.getElementById('booking-additional-charge').value || 0;

  if (!machineId || !projectId || !siteId || !startDate || !endDate) {
    showToast('Please fill all required booking fields', 'warning');
    return;
  }

  const payload = {
    machinery_id: parseInt(machineId, 10),
    project_id: parseInt(projectId, 10),
    site_id: parseInt(siteId, 10),
    start_date: startDate,
    end_date: endDate,
    operator_required: operatorRequired,
    transport_charge: parseFloat(transportCharge),
    additional_charge: parseFloat(additionalCharge)
  };

  try {
    const res = await apiRequest('/machinery-bookings', {
      method: 'POST',
      body: JSON.stringify(payload)
    });

    showToast(res.message || 'Booking request submitted successfully!', 'success');
    closeModal('booking-modal');
    loadBookings();
  } catch (err) {
    // Conflict error message display
    showToast(err.message || 'Unable to submit booking request.', 'error');
  }
}

async function handleApproveBooking(bookingId) {
  if (!confirm('Approve this machinery booking? This will reserve the machine dates and record the rental expense to the project.')) {
    return;
  }

  try {
    const res = await apiRequest(`/machinery-bookings/${bookingId}/approve`, {
      method: 'PUT'
    });
    showToast(res.message || 'Booking approved successfully! Machine assigned.', 'success');
    loadBookings();
  } catch (err) {
    showToast(err.message || 'Approval failed: Conflicting booking exists', 'error');
  }
}

function openRejectModal(bookingId) {
  currentRejectBookingId = bookingId;
  const form = document.getElementById('reject-form');
  if (form) form.reset();
  openModal('reject-modal');
}

async function handleConfirmRejection(e) {
  e.preventDefault();
  if (!currentRejectBookingId) return;

  const reason = document.getElementById('rejection-reason').value;
  if (!reason || !reason.trim()) {
    showToast('Please provide a reason for rejection', 'warning');
    return;
  }

  try {
    await apiRequest(`/machinery-bookings/${currentRejectBookingId}/reject`, {
      method: 'PUT',
      body: JSON.stringify({ rejection_reason: reason.trim() })
    });
    showToast('Booking request rejected.', 'info');
    closeModal('reject-modal');
    loadBookings();
  } catch (err) {
    showToast(err.message || 'Error rejecting booking', 'error');
  }
}

async function handleCancelBooking(bookingId) {
  if (!confirm('Are you sure you want to cancel this booking request?')) return;
  try {
    await apiRequest(`/machinery-bookings/${bookingId}/cancel`, { method: 'PUT' });
    showToast('Booking cancelled.', 'info');
    loadBookings();
  } catch (err) {
    showToast(err.message || 'Failed to cancel booking', 'error');
  }
}

function openUsageModal(bookingId) {
  currentUsageBookingId = bookingId;
  const form = document.getElementById('usage-form');
  if (form) form.reset();
  document.getElementById('usage-date').value = new Date().toISOString().substring(0, 10);
  openModal('usage-modal');
}

async function handleSaveUsage(e) {
  e.preventDefault();
  if (!currentUsageBookingId) return;

  const payload = {
    usage_date: document.getElementById('usage-date').value,
    hours_used: parseFloat(document.getElementById('usage-hours').value),
    fuel_cost: parseFloat(document.getElementById('usage-fuel').value || 0),
    operator_present: document.getElementById('usage-operator-present').checked,
    work_description: document.getElementById('usage-work-desc').value,
    remarks: document.getElementById('usage-remarks').value
  };

  try {
    await apiRequest(`/machinery-bookings/${currentUsageBookingId}/usage`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    showToast('Machine daily usage recorded successfully.', 'success');
    closeModal('usage-modal');
  } catch (err) {
    showToast(err.message || 'Error saving usage log', 'error');
  }
}
