/**
 * Automated Test Suite
 * Tests core business logic, date calculations, financial calculations,
 * and machinery booking overlap conflict detection.
 */

const assert = require('assert');
const {
  calculateTotalDays,
  calculateRentalAmount,
  calculateTotalAmount,
  calculateRemainingBudget,
  calculateBudgetUtilization,
  isLowStock
} = require('../utils/calculations');
const { checkDateOverlap, isValidDateRange } = require('../utils/dateUtils');

let totalTests = 0;
let passedTests = 0;

function runTest(testName, fn) {
  totalTests++;
  try {
    fn();
    console.log(`  ✓ PASS: ${testName}`);
    passedTests++;
  } catch (err) {
    console.error(`  ✗ FAIL: ${testName}`);
    console.error(`    Error: ${err.message}`);
  }
}

console.log('================================================================');
console.log('RUNNING BUSINESS LOGIC & CONFLICT PREVENTION UNIT TESTS');
console.log('================================================================');

// 1. Date Range Validation Tests
console.log('\n[1] Date Range & Overlap Logic:');

runTest('Valid date range validation', () => {
  assert.strictEqual(isValidDateRange('2026-10-10', '2026-10-19'), true);
  assert.strictEqual(isValidDateRange('2026-10-19', '2026-10-10'), false);
  assert.strictEqual(isValidDateRange('2026-10-10', '2026-10-10'), true);
});

runTest('Formula total_days = end_date - start_date + 1 (10 Oct to 19 Oct = 10 days)', () => {
  const days = calculateTotalDays('2026-10-10', '2026-10-19');
  assert.strictEqual(days, 10, 'Expected 10 inclusive days');
});

runTest('Single day booking gives total_days = 1', () => {
  const days = calculateTotalDays('2026-10-15', '2026-10-15');
  assert.strictEqual(days, 1);
});

// 2. Financial Calculations
console.log('\n[2] Financial Calculations & Formulas:');

runTest('Rental calculation: 5000 daily rate * 10 days = 50,000', () => {
  const rental = calculateRentalAmount(5000, 10);
  assert.strictEqual(rental, 50000);
});

runTest('Grand total calculation with operator, transport, and additional charges', () => {
  const total = calculateTotalAmount({
    dailyRate: 5000,
    totalDays: 10,
    operatorCharge: 8000,
    transportCharge: 4000,
    additionalCharge: 1500
  });
  // 50,000 + 8,000 + 4,000 + 1,500 = 63,500
  assert.strictEqual(total, 63500);
});

runTest('Budget remaining: 1,25,00,000 budget - 82,00,000 expenses = 43,00,000', () => {
  const remaining = calculateRemainingBudget(12500000, 8200000);
  assert.strictEqual(remaining, 4300000);
});

runTest('Budget utilization: (82,00,000 / 1,25,00,000) * 100 = 65.6%', () => {
  const utilization = calculateBudgetUtilization(12500000, 8200000);
  assert.strictEqual(utilization, 65.6);
});

runTest('Low stock check: 15 drums < 30 drums threshold triggers alert', () => {
  assert.strictEqual(isLowStock(15, 30), true);
  assert.strictEqual(isLowStock(45, 30), false);
});

// 3. Machinery Booking Conflict Detection
console.log('\n[3] Machinery Booking Conflict Prevention (CRITICAL SPECIFICATION):');

// Existing Confirmed Booking for JCB-001: 10 Oct 2026 -> 15 Oct 2026
const existingBooking = {
  machineryId: 1,
  startDate: '2026-10-10',
  endDate: '2026-10-15',
  status: 'APPROVED'
};

runTest('Conflict Detection: New booking 13 Oct -> 18 Oct MUST BE BLOCKED (Interior overlap)', () => {
  const overlaps = checkDateOverlap(
    '2026-10-13', '2026-10-18',
    existingBooking.startDate, existingBooking.endDate
  );
  assert.strictEqual(overlaps, true, 'Booking from 13-18 Oct overlaps with confirmed 10-15 Oct');
});

runTest('Conflict Detection: New booking 08 Oct -> 12 Oct MUST BE BLOCKED (Start overlap)', () => {
  const overlaps = checkDateOverlap(
    '2026-10-08', '2026-10-12',
    existingBooking.startDate, existingBooking.endDate
  );
  assert.strictEqual(overlaps, true, 'Booking from 08-12 Oct overlaps with confirmed 10-15 Oct');
});

runTest('Conflict Detection: New booking 10 Oct -> 15 Oct MUST BE BLOCKED (Exact match overlap)', () => {
  const overlaps = checkDateOverlap(
    '2026-10-10', '2026-10-15',
    existingBooking.startDate, existingBooking.endDate
  );
  assert.strictEqual(overlaps, true);
});

runTest('Conflict Detection: New booking 15 Oct -> 20 Oct MUST BE BLOCKED (Boundary overlap on 15 Oct)', () => {
  const overlaps = checkDateOverlap(
    '2026-10-15', '2026-10-20',
    existingBooking.startDate, existingBooking.endDate
  );
  assert.strictEqual(overlaps, true, 'Same-day boundary overlap must be prevented');
});

runTest('Availability: New booking 01 Oct -> 09 Oct MUST BE ALLOWED (Before existing)', () => {
  const overlaps = checkDateOverlap(
    '2026-10-01', '2026-10-09',
    existingBooking.startDate, existingBooking.endDate
  );
  assert.strictEqual(overlaps, false, 'Non-overlapping period before confirmed booking should be available');
});

runTest('Availability: New booking 16 Oct -> 25 Oct MUST BE ALLOWED (After existing)', () => {
  const overlaps = checkDateOverlap(
    '2026-10-16', '2026-10-25',
    existingBooking.startDate, existingBooking.endDate
  );
  assert.strictEqual(overlaps, false, 'Non-overlapping period after confirmed booking should be available');
});

// Summary
console.log('\n================================================================');
console.log(`TEST SUMMARY: ${passedTests} / ${totalTests} Passed (${Math.round((passedTests / totalTests) * 100)}%)`);
console.log('================================================================\n');

if (passedTests === totalTests) {
  process.exit(0);
} else {
  process.exit(1);
}
