/**
 * UI Utilities & Formatters
 */

/**
 * Format currency in Indian Rupees style (e.g. ₹50,000 or ₹1,25,00,000)
 */
function formatINR(amount) {
  const num = parseFloat(amount || 0);
  if (isNaN(num)) return '₹0';
  return '₹' + num.toLocaleString('en-IN', {
    maximumFractionDigits: 2,
    minimumFractionDigits: 0
  });
}

/**
 * Format date string into human readable display (e.g. "15 Oct 2026")
 */
function formatDate(dateStr) {
  if (!dateStr) return 'N/A';
  const cleanStr = dateStr.toString().substring(0, 10);
  const parts = cleanStr.split('-');
  if (parts.length === 3) {
    const year = parseInt(parts[0], 10);
    const monthIndex = parseInt(parts[1], 10) - 1;
    const day = parseInt(parts[2], 10);
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return `${day} ${months[monthIndex]} ${year}`;
  }
  return dateStr;
}

/**
 * Calculates number of inclusive days between two dates
 */
function calculateDays(startDate, endDate) {
  if (!startDate || !endDate) return 0;
  const s = new Date(startDate);
  const e = new Date(endDate);
  if (isNaN(s.getTime()) || isNaN(e.getTime()) || e < s) return 0;
  const diff = Math.round(Math.abs(e - s) / (1000 * 60 * 60 * 24));
  return diff + 1;
}

/**
 * Returns formatted HTML badge for entity status
 */
function getStatusBadge(status) {
  if (!status) return '<span class="badge badge-neutral">UNKNOWN</span>';
  const s = status.toUpperCase();

  switch (s) {
    case 'ACTIVE':
    case 'APPROVED':
    case 'COMPLETED':
    case 'AVAILABLE':
    case 'RESOLVED':
      return `<span class="badge badge-success">${s}</span>`;

    case 'PENDING':
    case 'IN_PROGRESS':
    case 'PLANNED':
    case 'ASSIGNED':
      return `<span class="badge badge-warning">${s}</span>`;

    case 'REJECTED':
    case 'CANCELLED':
    case 'DELAYED':
    case 'MAINTENANCE':
    case 'CRITICAL':
      return `<span class="badge badge-danger">${s}</span>`;

    case 'BOOKED':
    case 'ON_HOLD':
      return `<span class="badge badge-info">${s}</span>`;

    case 'NOT_STARTED':
    case 'INACTIVE':
    case 'CLOSED':
    default:
      return `<span class="badge badge-neutral">${s}</span>`;
  }
}

/**
 * Returns formatted HTML badge for priority
 */
function getPriorityBadge(priority) {
  if (!priority) return '<span class="badge badge-neutral">NORMAL</span>';
  const p = priority.toUpperCase();
  switch (p) {
    case 'CRITICAL':
      return '<span class="badge badge-danger">CRITICAL</span>';
    case 'HIGH':
      return '<span class="badge badge-warning">HIGH</span>';
    case 'MEDIUM':
      return '<span class="badge badge-info">MEDIUM</span>';
    case 'LOW':
    default:
      return '<span class="badge badge-neutral">LOW</span>';
  }
}

/**
 * Escapes HTML to prevent XSS
 */
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
