/**
 * Date Utility Functions
 * Handles date formatting, parsing, validation, and overlap checks.
 */

function parseDate(dateStr) {
  if (!dateStr) return null;
  if (dateStr instanceof Date) return dateStr;
  const parts = dateStr.toString().substring(0, 10).split('-');
  if (parts.length === 3) {
    const year = parseInt(parts[0], 10);
    const month = parseInt(parts[1], 10) - 1;
    const day = parseInt(parts[2], 10);
    return new Date(Date.UTC(year, month, day));
  }
  return new Date(dateStr);
}

function formatDate(date) {
  if (!date) return '';
  if (typeof date === 'string') return date.substring(0, 10);
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function isValidDateRange(startDate, endDate) {
  if (!startDate || !endDate) return false;
  const start = parseDate(startDate);
  const end = parseDate(endDate);
  if (isNaN(start.getTime()) || isNaN(end.getTime())) return false;
  return end.getTime() >= start.getTime();
}

/**
 * Checks if two date ranges [startA, endA] and [startB, endB] overlap.
 * Inclusive interval check: (startA <= endB) && (endA >= startB)
 */
function checkDateOverlap(startA, endA, startB, endB) {
  const sA = formatDate(startA);
  const eA = formatDate(endA);
  const sB = formatDate(startB);
  const eB = formatDate(endB);
  return (sA <= eB) && (eA >= sB);
}

module.exports = {
  parseDate,
  formatDate,
  isValidDateRange,
  checkDateOverlap
};
