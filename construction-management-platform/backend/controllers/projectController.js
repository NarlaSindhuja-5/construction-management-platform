/**
 * Project Controller
 * Handles project creation, listing, updates, metrics, and resource aggregation.
 */

const { query } = require('../config/db');
const { isValidDateRange, formatDate } = require('../utils/dateUtils');
const { calculateRemainingBudget, calculateBudgetUtilization } = require('../utils/calculations');

async function createProject(req, res, next) {
  try {
    const {
      project_name,
      client_name,
      project_type,
      location,
      description,
      start_date,
      end_date,
      budget = 0,
      status = 'PLANNED'
    } = req.body;

    if (!project_name || !project_name.trim()) {
      return res.status(400).json({ success: false, message: 'Project name is required.' });
    }
    if (!client_name || !client_name.trim()) {
      return res.status(400).json({ success: false, message: 'Client name is required.' });
    }
    if (!project_type || !project_type.trim()) {
      return res.status(400).json({ success: false, message: 'Project type is required.' });
    }
    if (!location || !location.trim()) {
      return res.status(400).json({ success: false, message: 'Location is required.' });
    }
    if (!start_date || !end_date) {
      return res.status(400).json({ success: false, message: 'Start date and end date are required.' });
    }
    if (!isValidDateRange(start_date, end_date)) {
      return res.status(400).json({ success: false, message: 'End date cannot be prior to start date.' });
    }
    if (parseFloat(budget) < 0) {
      return res.status(400).json({ success: false, message: 'Budget cannot be negative.' });
    }

    const validStatuses = ['PLANNED', 'ACTIVE', 'COMPLETED', 'ON_HOLD', 'CANCELLED'];
    const projectStatus = validStatuses.includes(status) ? status : 'PLANNED';

    const insertSql = `
      INSERT INTO projects (
        project_name, client_name, project_type, location, description,
        start_date, end_date, budget, status, created_by, created_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
    `;

    const [result] = await query(insertSql, [
      project_name.trim(),
      client_name.trim(),
      project_type.trim(),
      location.trim(),
      description ? description.trim() : null,
      formatDate(start_date),
      formatDate(end_date),
      parseFloat(budget),
      projectStatus,
      req.user.id
    ]);

    return res.status(201).json({
      success: true,
      message: 'Project created successfully.',
      projectId: result.insertId
    });
  } catch (error) {
    next(error);
  }
}

async function getAllProjects(req, res, next) {
  try {
    const { status, search } = req.query;

    let sql = `
      SELECT 
        p.*,
        u.name AS created_by_name,
        COUNT(DISTINCT s.id) AS sites_count,
        COUNT(DISTINCT t.id) AS tasks_count,
        COALESCE(ROUND(AVG(t.progress_percent)), 0) AS progress_percent,
        COALESCE((SELECT SUM(e.amount) FROM expenses e WHERE e.project_id = p.id), 0) AS total_spent
      FROM projects p
      LEFT JOIN users u ON p.created_by = u.id
      LEFT JOIN sites s ON p.id = s.project_id
      LEFT JOIN tasks t ON p.id = t.project_id
      WHERE 1=1
    `;
    const params = [];

    if (status) {
      sql += ' AND p.status = ?';
      params.push(status);
    }
    if (search) {
      sql += ' AND (p.project_name LIKE ? OR p.client_name LIKE ? OR p.location LIKE ?)';
      params.push(`%${search}%`, `%${search}%`, `%${search}%`);
    }

    sql += ' GROUP BY p.id ORDER BY p.created_at DESC';

    const [projects] = await query(sql, params);

    const formatted = projects.map(p => {
      const budget = parseFloat(p.budget || 0);
      const spent = parseFloat(p.total_spent || 0);
      return {
        ...p,
        budget,
        total_spent: spent,
        remaining_budget: calculateRemainingBudget(budget, spent),
        budget_utilization: calculateBudgetUtilization(budget, spent),
        progress_percent: parseInt(p.progress_percent || 0, 10)
      };
    });

    return res.status(200).json({ success: true, count: formatted.length, projects: formatted });
  } catch (error) {
    next(error);
  }
}

async function getProjectById(req, res, next) {
  try {
    const { id } = req.params;

    const [projects] = await query(`
      SELECT p.*, u.name AS created_by_name, u.email AS created_by_email
      FROM projects p
      LEFT JOIN users u ON p.created_by = u.id
      WHERE p.id = ?
    `, [id]);

    if (projects.length === 0) {
      return res.status(404).json({ success: false, message: 'Project not found.' });
    }

    const project = projects[0];

    // Get linked sites
    const [sites] = await query(`
      SELECT s.*, u.name AS site_manager_name, u.phone AS site_manager_phone
      FROM sites s
      LEFT JOIN users u ON s.site_manager_id = u.id
      WHERE s.project_id = ?
    `, [id]);

    // Financial totals
    const [spentRow] = await query(
      'SELECT COALESCE(SUM(amount), 0) AS total_spent FROM expenses WHERE project_id = ?',
      [id]
    );

    // Tasks summary
    const [taskStats] = await query(`
      SELECT 
        COUNT(*) AS total_tasks,
        COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) AS completed_tasks,
        COALESCE(ROUND(AVG(progress_percent)), 0) AS avg_progress
      FROM tasks WHERE project_id = ?
    `, [id]);

    const budget = parseFloat(project.budget || 0);
    const spent = parseFloat(spentRow[0].total_spent || 0);

    return res.status(200).json({
      success: true,
      project: {
        ...project,
        budget,
        total_spent: spent,
        remaining_budget: calculateRemainingBudget(budget, spent),
        budget_utilization: calculateBudgetUtilization(budget, spent),
        progress_percent: parseInt(taskStats[0].avg_progress || 0, 10),
        total_tasks: taskStats[0].total_tasks,
        completed_tasks: taskStats[0].completed_tasks,
        sites
      }
    });
  } catch (error) {
    next(error);
  }
}

async function updateProject(req, res, next) {
  try {
    const { id } = req.params;
    const {
      project_name,
      client_name,
      project_type,
      location,
      description,
      start_date,
      end_date,
      budget,
      status
    } = req.body;

    const [existing] = await query('SELECT * FROM projects WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Project not found.' });
    }

    if (start_date && end_date && !isValidDateRange(start_date, end_date)) {
      return res.status(400).json({ success: false, message: 'End date cannot be prior to start date.' });
    }

    const updateSql = `
      UPDATE projects SET
        project_name = COALESCE(?, project_name),
        client_name = COALESCE(?, client_name),
        project_type = COALESCE(?, project_type),
        location = COALESCE(?, location),
        description = COALESCE(?, description),
        start_date = COALESCE(?, start_date),
        end_date = COALESCE(?, end_date),
        budget = COALESCE(?, budget),
        status = COALESCE(?, status)
      WHERE id = ?
    `;

    await query(updateSql, [
      project_name ? project_name.trim() : null,
      client_name ? client_name.trim() : null,
      project_type ? project_type.trim() : null,
      location ? location.trim() : null,
      description !== undefined ? description : null,
      start_date ? formatDate(start_date) : null,
      end_date ? formatDate(end_date) : null,
      budget !== undefined ? parseFloat(budget) : null,
      status || null,
      id
    ]);

    return res.status(200).json({ success: true, message: 'Project updated successfully.' });
  } catch (error) {
    next(error);
  }
}

async function deleteProject(req, res, next) {
  try {
    const { id } = req.params;
    const [existing] = await query('SELECT id FROM projects WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Project not found.' });
    }

    await query('DELETE FROM projects WHERE id = ?', [id]);
    return res.status(200).json({ success: true, message: 'Project and all associated sites deleted successfully.' });
  } catch (error) {
    next(error);
  }
}

async function getProjectResources(req, res, next) {
  try {
    const { id } = req.params;

    // Sites
    const [sites] = await query('SELECT * FROM sites WHERE project_id = ?', [id]);

    // Booked Machinery
    const [machinery] = await query(`
      SELECT b.*, m.machine_name, m.machine_type, m.registration_number, s.site_name
      FROM machinery_bookings b
      JOIN machinery m ON b.machinery_id = m.id
      JOIN sites s ON b.site_id = s.id
      WHERE b.project_id = ? AND b.status = 'APPROVED'
    `, [id]);

    // Assigned workers (via sites)
    const [workers] = await query(`
      SELECT w.*, s.site_name
      FROM workers w
      JOIN sites s ON w.site_id = s.id
      WHERE s.project_id = ?
    `, [id]);

    // Materials on sites
    const [materials] = await query(`
      SELECT m.*, s.site_name
      FROM materials m
      JOIN sites s ON m.site_id = s.id
      WHERE s.project_id = ?
    `, [id]);

    return res.status(200).json({
      success: true,
      resources: {
        sites,
        machinery,
        workers,
        materials
      }
    });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  createProject,
  getAllProjects,
  getProjectById,
  updateProject,
  deleteProject,
  getProjectResources
};
