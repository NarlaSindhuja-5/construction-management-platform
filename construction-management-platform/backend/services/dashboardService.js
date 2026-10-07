/**
 * Dashboard Service
 * Aggregates real-time business metrics, KPIs, project progress, and actionable alerts from MySQL.
 */

const { query } = require('../config/db');
const { calculateRemainingBudget, calculateBudgetUtilization } = require('../utils/calculations');

async function getDashboardSummary() {
  // 1. KPI Counts
  const [activeProjectsRow] = await query("SELECT COUNT(*) AS count FROM projects WHERE status = 'ACTIVE'");
  const [activeSitesRow] = await query("SELECT COUNT(*) AS count FROM sites WHERE status = 'ACTIVE'");
  const [totalWorkersRow] = await query("SELECT COUNT(*) AS count FROM workers WHERE status = 'ACTIVE'");
  const [availableMachineryRow] = await query("SELECT COUNT(*) AS count FROM machinery WHERE status = 'AVAILABLE'");
  const [pendingBookingsRow] = await query("SELECT COUNT(*) AS count FROM machinery_bookings WHERE status = 'PENDING'");
  
  // 2. Budget and Expenses Totals
  const [budgetRow] = await query("SELECT COALESCE(SUM(budget), 0) AS total_budget FROM projects WHERE status != 'CANCELLED'");
  const [expensesRow] = await query("SELECT COALESCE(SUM(amount), 0) AS total_expenses FROM expenses");

  const totalBudget = parseFloat(budgetRow[0].total_budget || 0);
  const totalExpenses = parseFloat(expensesRow[0].total_expenses || 0);
  const remainingBudget = calculateRemainingBudget(totalBudget, totalExpenses);
  const utilization = calculateBudgetUtilization(totalBudget, totalExpenses);

  return {
    activeProjects: activeProjectsRow[0].count,
    activeSites: activeSitesRow[0].count,
    totalWorkers: totalWorkersRow[0].count,
    availableMachinery: availableMachineryRow[0].count,
    pendingBookings: pendingBookingsRow[0].count,
    totalBudget,
    totalExpenses,
    remainingBudget,
    budgetUtilization: utilization
  };
}

async function getProjectsProgress() {
  const sql = `
    SELECT 
      p.id,
      p.project_name,
      p.client_name,
      p.status,
      p.budget,
      p.start_date,
      p.end_date,
      COUNT(t.id) AS total_tasks,
      COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) AS completed_tasks,
      COALESCE(ROUND(AVG(t.progress_percent)), 0) AS avg_task_progress,
      COALESCE((SELECT SUM(e.amount) FROM expenses e WHERE e.project_id = p.id), 0) AS total_spent
    FROM projects p
    LEFT JOIN tasks t ON p.id = t.project_id
    GROUP BY p.id
    ORDER BY p.created_at DESC
  `;

  const [rows] = await query(sql);

  return rows.map(r => {
    const budget = parseFloat(r.budget || 0);
    const spent = parseFloat(r.total_spent || 0);
    const remaining = calculateRemainingBudget(budget, spent);
    const utilization = calculateBudgetUtilization(budget, spent);

    return {
      id: r.id,
      projectName: r.project_name,
      clientName: r.client_name,
      status: r.status,
      budget,
      spent,
      remaining,
      utilization,
      startDate: r.start_date,
      endDate: r.end_date,
      totalTasks: r.total_tasks,
      completedTasks: r.completed_tasks,
      progressPercent: parseInt(r.avg_task_progress, 10)
    };
  });
}

async function getSystemAlerts() {
  const alerts = [];

  // 1. Low Material Stock: quantity < minimum_quantity
  const [lowStockMaterials] = await query(`
    SELECT m.id, m.material_name, m.quantity, m.minimum_quantity, m.unit, s.site_name, p.project_name
    FROM materials m
    JOIN sites s ON m.site_id = s.id
    JOIN projects p ON s.project_id = p.id
    WHERE m.quantity < m.minimum_quantity
  `);

  lowStockMaterials.forEach(m => {
    alerts.push({
      id: `mat-${m.id}`,
      type: 'LOW_STOCK',
      severity: 'WARNING',
      title: 'Low Material Stock Alert',
      message: `${m.material_name} at ${m.site_name} is running low (${m.quantity} ${m.unit} remaining, minimum required is ${m.minimum_quantity} ${m.unit}).`,
      details: { materialId: m.id, currentStock: m.quantity, minThreshold: m.minimum_quantity, unit: m.unit }
    });
  });

  // 2. Delayed Tasks: due_date < CURRENT_DATE AND status != 'COMPLETED'
  const [delayedTasks] = await query(`
    SELECT t.id, t.task_name, t.due_date, t.progress_percent, p.project_name, s.site_name
    FROM tasks t
    JOIN projects p ON t.project_id = p.id
    JOIN sites s ON t.site_id = s.id
    WHERE t.due_date < CURDATE() AND t.status != 'COMPLETED'
  `);

  delayedTasks.forEach(t => {
    alerts.push({
      id: `task-${t.id}`,
      type: 'DELAYED_TASK',
      severity: 'HIGH',
      title: 'Task Schedule Delay',
      message: `Task "${t.task_name}" (${t.projectName}) was due on ${t.due_date} but is currently at ${t.progress_percent}%.`,
      details: { taskId: t.id, dueDate: t.due_date, progressPercent: t.progress_percent }
    });
  });

  // 3. Pending Machinery Bookings requiring Owner Approval
  const [pendingBookings] = await query(`
    SELECT b.id, m.machine_name, p.project_name, b.start_date, b.end_date, u.name AS contractor_name
    FROM machinery_bookings b
    JOIN machinery m ON b.machinery_id = m.id
    JOIN projects p ON b.project_id = p.id
    JOIN users u ON b.contractor_id = u.id
    WHERE b.status = 'PENDING'
  `);

  pendingBookings.forEach(b => {
    alerts.push({
      id: `book-${b.id}`,
      type: 'PENDING_BOOKING',
      severity: 'INFO',
      title: 'Pending Machinery Booking Approval',
      message: `${b.contractor_name} requested ${b.machine_name} for ${b.project_name} (${b.start_date} to ${b.end_date}).`,
      details: { bookingId: b.id }
    });
  });

  // 4. Machinery Under Maintenance
  const [maintenanceRecords] = await query(`
    SELECT mm.id, m.machine_name, mm.start_date, mm.end_date, mm.reason
    FROM machinery_maintenance mm
    JOIN machinery m ON mm.machinery_id = m.id
    WHERE mm.status != 'COMPLETED' AND mm.end_date >= CURDATE()
  `);

  maintenanceRecords.forEach(mm => {
    alerts.push({
      id: `maint-${mm.id}`,
      type: 'MAINTENANCE',
      severity: 'WARNING',
      title: 'Scheduled Machinery Maintenance',
      message: `${mm.machine_name} is under maintenance from ${mm.start_date} to ${mm.end_date} (${mm.reason}).`,
      details: { maintenanceId: mm.id }
    });
  });

  // 5. Open Critical Issues
  const [criticalIssues] = await query(`
    SELECT i.id, i.issue_type, i.description, p.project_name, s.site_name
    FROM issues i
    JOIN projects p ON i.project_id = p.id
    LEFT JOIN sites s ON i.site_id = s.id
    WHERE i.priority IN ('HIGH', 'CRITICAL') AND i.status IN ('OPEN', 'IN_PROGRESS')
  `);

  criticalIssues.forEach(ci => {
    alerts.push({
      id: `issue-${ci.id}`,
      type: 'CRITICAL_ISSUE',
      severity: 'CRITICAL',
      title: `Site Issue: ${ci.issue_type}`,
      message: `${ci.project_name} (${ci.site_name || 'General'}): ${ci.description}`,
      details: { issueId: ci.id }
    });
  });

  return alerts;
}

module.exports = {
  getDashboardSummary,
  getProjectsProgress,
  getSystemAlerts
};
