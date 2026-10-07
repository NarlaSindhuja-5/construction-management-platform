/**
 * Financial and Operational Calculation Functions
 * All business calculations are performed and validated server-side.
 */

const { parseDate } = require('./dateUtils');

/**
 * Calculates number of inclusive calendar days between two dates.
 * total_days = end_date - start_date + 1
 */
function calculateTotalDays(startDate, endDate) {
  const start = parseDate(startDate);
  const end = parseDate(endDate);
  if (!start || !end || isNaN(start.getTime()) || isNaN(end.getTime())) {
    throw new Error('Invalid date provided for day calculation');
  }
  if (end < start) {
    throw new Error('End date cannot be prior to start date');
  }
  const diffTime = Math.abs(end.getTime() - start.getTime());
  const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));
  return diffDays + 1;
}

/**
 * Calculates rental amount
 * Formula: daily_rate * total_days
 */
function calculateRentalAmount(dailyRate, totalDays) {
  const rate = parseFloat(dailyRate) || 0;
  const days = parseInt(totalDays, 10) || 0;
  return parseFloat((rate * days).toFixed(2));
}

/**
 * Calculates grand total for a machinery booking:
 * total_amount = rental_amount + operator_charge + transport_charge + additional_charge
 */
function calculateTotalAmount({
  dailyRate = 0,
  totalDays = 0,
  operatorCharge = 0,
  transportCharge = 0,
  additionalCharge = 0
}) {
  const rentalAmount = calculateRentalAmount(dailyRate, totalDays);
  const opCharge = parseFloat(operatorCharge) || 0;
  const transCharge = parseFloat(transportCharge) || 0;
  const addCharge = parseFloat(additionalCharge) || 0;
  const total = rentalAmount + opCharge + transCharge + addCharge;
  return parseFloat(total.toFixed(2));
}

/**
 * Remaining Budget: project_budget - total_expenses
 */
function calculateRemainingBudget(budget, totalExpenses) {
  const b = parseFloat(budget) || 0;
  const e = parseFloat(totalExpenses) || 0;
  return parseFloat((b - e).toFixed(2));
}

/**
 * Budget Utilization Percentage: (total_expenses / project_budget) * 100
 */
function calculateBudgetUtilization(budget, totalExpenses) {
  const b = parseFloat(budget) || 0;
  const e = parseFloat(totalExpenses) || 0;
  if (b <= 0) return 0;
  const pct = (e / b) * 100;
  return parseFloat(pct.toFixed(2));
}

/**
 * Low stock indicator: quantity < minimum_quantity
 */
function isLowStock(quantity, minimumQuantity) {
  const q = parseFloat(quantity) || 0;
  const min = parseFloat(minimumQuantity) || 0;
  return q < min;
}

module.exports = {
  calculateTotalDays,
  calculateRentalAmount,
  calculateTotalAmount,
  calculateRemainingBudget,
  calculateBudgetUtilization,
  isLowStock
};
