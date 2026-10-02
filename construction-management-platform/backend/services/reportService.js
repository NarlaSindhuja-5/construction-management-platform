/**
 * Report Service
 * Aggregates analytical reports for projects, expenses, machinery bookings, and progress.
 */

const { query } = require('../config/db');
const { calculateRemainingBudget, calculateBudgetUtilization } = require('../utils/calculations');

async function getProjectReport(projectId) {
  // 1. Project details
  const [projects] = await query('SELECT * FROM projects WHERE id = ?', [projectId]);
  if (!projects || projects.length === 0) {
    throw new Error('Project not found');
  }
  const project = projects[0];

  // 2. Sites under project
  const [sites] = await query(`
    SELECT s.*, u.name AS site_manager_name, u.phone AS site_manager_phone
    FROM sites s
    LEFT JOIN users u ON s.site_manager_id = u.id
    WHERE s.project_id = ?
  `, [projectId]);

  // 3. Tasks overview
  const [tasks] = await query(`
    SELECT t.*, w.name AS assigned_worker_name, s.site_name
    FROM tasks t
    LEFT JOIN workers w ON t.assigned_worker_id = w.id
    LEFT JOIN sites s ON t.site_id = s.id
    WHERE t.project_id = ?
    ORDER BY t.due_date ASC
  `, [projectId]);

  // 4. Machinery bookings under project
  const [bookings] = await query(`
    SELECT b.*, m.machine_name, m.machine_type, m.registration_number, s.site_name, u.name AS contractor_name
    FROM machinery_bookings b
    JOIN machinery m ON b.machinery_id = m.id
    JOIN sites s ON b.site_id = s.id
    JOIN users u ON b.contractor_id = u.id
    WHERE b.project_id = ?
    ORDER BY b.start_date DESC
  `, [projectId]);

  // 5. Expenses breakdown by category
  const [expenses] = await query(`
    SELECT category, COALESCE(SUM(amount), 0) AS total_amount, COUNT(*) AS count
    FROM expenses
    WHERE project_id = ?
    GROUP BY category
  `, [projectId]);

  const [totalSpentRow] = await query(
    'SELECT COALESCE(SUM(amount), 0) AS total_spent FROM expenses WHERE project_id = ?',
    [projectId]
  );
  const totalSpent = parseFloat(totalSpentRow[0].total_spent || 0);
  const budget = parseFloat(project.budget || 0);

  // 6. Calculate progress percentage
  const totalTasksCount = tasks.length;
  const completedTasksCount = tasks.filter(t => t.status === 'COMPLETED').length;
  const avgProgress = totalTasksCount > 0
    ? Math.round(tasks.reduce((acc, t) => acc + (t.progress_percent || 0), 0) / totalTasksCount)
    : 0;

  return {
    project: {
      ...project,
      budget,
      totalSpent,
      remainingBudget: calculateRemainingBudget(budget, totalSpent),
      budgetUtilization: calculateBudgetUtilization(budget, totalSpent),
      progressPercent: avgProgress,
      totalTasks: totalTasksCount,
      completedTasks: completedTasksCount
    },
    sites,
    tasks,
    machineryBookings: bookings,
    expensesByCategory: expenses
  };
}

async function getExpensesReport(projectId = null) {
  let sql = `
    SELECT e.*, p.project_name, s.site_name, u.name AS recorded_by_name
    FROM expenses e
    JOIN projects p ON e.project_id = p.id
    LEFT JOIN sites s ON e.site_id = s.id
    LEFT JOIN users u ON e.created_by = u.id
  `;
  const params = [];

  if (projectId) {
    sql += ' WHERE e.project_id = ?';
    params.push(projectId);
  }

  sql += ' ORDER BY e.expense_date DESC, e.id DESC';

  const [expenses] = await query(sql, params);

  // Category summary
  let summarySql = `
    SELECT category, COALESCE(SUM(amount), 0) AS total_amount, COUNT(*) as transaction_count
    FROM expenses
  `;
  if (projectId) {
    summarySql += ' WHERE project_id = ? GROUP BY category';
  } else {
    summarySql += ' GROUP BY category';
  }
  const [categories] = await query(summarySql, params);

  const totalAmount = expenses.reduce((acc, e) => acc + parseFloat(e.amount || 0), 0);

  return {
    totalExpenses: parseFloat(totalAmount.toFixed(2)),
    categoryBreakdown: categories,
    records: expenses
  };
}

async function getProgressReport(projectId) {
  const [progress] = await query(`
    SELECT dp.*, p.project_name, s.site_name, t.task_name, u.name AS recorded_by_name
    FROM daily_progress dp
    JOIN projects p ON dp.project_id = p.id
    JOIN sites s ON dp.site_id = s.id
    LEFT JOIN tasks t ON dp.task_id = t.id
    LEFT JOIN users u ON dp.created_by = u.id
    WHERE dp.project_id = ?
    ORDER BY dp.progress_date DESC, dp.id DESC
  `, [projectId]);

  return progress;
}

module.exports = {
  getProjectReport,
  getExpensesReport,
  getProgressReport
};
