/**
 * Task Controller
 * Handles project task scheduling, progress completion percentage, priority, and assignments.
 */

const { query } = require('../config/db');
const { isValidDateRange, formatDate } = require('../utils/dateUtils');

async function createTask(req, res, next) {
  try {
    const {
      project_id,
      site_id,
      task_name,
      description,
      assigned_worker_id,
      start_date,
      due_date,
      priority = 'MEDIUM',
      status = 'NOT_STARTED',
      progress_percent = 0
    } = req.body;

    if (!project_id || !site_id) {
      return res.status(400).json({ success: false, message: 'Project ID and Site ID are required.' });
    }
    if (!task_name || !task_name.trim()) {
      return res.status(400).json({ success: false, message: 'Task name is required.' });
    }
    if (!start_date || !due_date) {
      return res.status(400).json({ success: false, message: 'Start date and due date are required.' });
    }
    if (!isValidDateRange(start_date, due_date)) {
      return res.status(400).json({ success: false, message: 'Due date cannot be prior to start date.' });
    }

    const validPriorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
    const validStatuses = ['NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'DELAYED'];

    const taskPriority = validPriorities.includes(priority) ? priority : 'MEDIUM';
    let taskStatus = validStatuses.includes(status) ? status : 'NOT_STARTED';

    const progress = Math.min(100, Math.max(0, parseInt(progress_percent || 0, 10)));
    if (progress === 100) {
      taskStatus = 'COMPLETED';
    } else if (progress > 0 && taskStatus === 'NOT_STARTED') {
      taskStatus = 'IN_PROGRESS';
    }

    const insertSql = `
      INSERT INTO tasks (
        project_id, site_id, task_name, description, assigned_worker_id,
        start_date, due_date, priority, status, progress_percent, created_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
    `;

    const [result] = await query(insertSql, [
      project_id,
      site_id,
      task_name.trim(),
      description ? description.trim() : null,
      assigned_worker_id || null,
      formatDate(start_date),
      formatDate(due_date),
      taskPriority,
      taskStatus,
      progress
    ]);

    return res.status(201).json({
      success: true,
      message: 'Task created successfully.',
      taskId: result.insertId
    });
  } catch (error) {
    next(error);
  }
}

async function getAllTasks(req, res, next) {
  try {
    const { project_id, site_id, status, priority, worker_id, search } = req.query;

    let sql = `
      SELECT 
        t.*,
        p.project_name,
        s.site_name,
        w.name AS assigned_worker_name,
        w.skill AS assigned_worker_skill
      FROM tasks t
      JOIN projects p ON t.project_id = p.id
      JOIN sites s ON t.site_id = s.id
      LEFT JOIN workers w ON t.assigned_worker_id = w.id
      WHERE 1=1
    `;
    const params = [];

    if (project_id) {
      sql += ' AND t.project_id = ?';
      params.push(project_id);
    }
    if (site_id) {
      sql += ' AND t.site_id = ?';
      params.push(site_id);
    }
    if (status) {
      sql += ' AND t.status = ?';
      params.push(status);
    }
    if (priority) {
      sql += ' AND t.priority = ?';
      params.push(priority);
    }
    if (worker_id) {
      sql += ' AND t.assigned_worker_id = ?';
      params.push(worker_id);
    }
    if (search) {
      sql += ' AND (t.task_name LIKE ? OR t.description LIKE ?)';
      params.push(`%${search}%`, `%${search}%`);
    }

    sql += ' ORDER BY t.due_date ASC, t.id DESC';

    const [tasks] = await query(sql, params);
    return res.status(200).json({ success: true, count: tasks.length, tasks });
  } catch (error) {
    next(error);
  }
}

async function getTaskById(req, res, next) {
  try {
    const { id } = req.params;

    const [rows] = await query(`
      SELECT 
        t.*,
        p.project_name,
        s.site_name,
        w.name AS assigned_worker_name,
        w.phone AS assigned_worker_phone
      FROM tasks t
      JOIN projects p ON t.project_id = p.id
      JOIN sites s ON t.site_id = s.id
      LEFT JOIN workers w ON t.assigned_worker_id = w.id
      WHERE t.id = ?
    `, [id]);

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Task not found.' });
    }

    const [progressLogs] = await query(
      'SELECT * FROM daily_progress WHERE task_id = ? ORDER BY progress_date DESC',
      [id]
    );

    return res.status(200).json({ success: true, task: rows[0], progressLogs });
  } catch (error) {
    next(error);
  }
}

async function updateTask(req, res, next) {
  try {
    const { id } = req.params;
    const {
      task_name,
      description,
      assigned_worker_id,
      start_date,
      due_date,
      priority,
      status,
      progress_percent
    } = req.body;

    const [existing] = await query('SELECT id, progress_percent, status FROM tasks WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Task not found.' });
    }

    if (start_date && due_date && !isValidDateRange(start_date, due_date)) {
      return res.status(400).json({ success: false, message: 'Due date cannot be prior to start date.' });
    }

    let progress = progress_percent !== undefined
      ? Math.min(100, Math.max(0, parseInt(progress_percent, 10)))
      : existing[0].progress_percent;

    let computedStatus = status || existing[0].status;
    if (progress === 100) {
      computedStatus = 'COMPLETED';
    } else if (progress > 0 && computedStatus === 'NOT_STARTED') {
      computedStatus = 'IN_PROGRESS';
    }

    const updateSql = `
      UPDATE tasks SET
        task_name = COALESCE(?, task_name),
        description = COALESCE(?, description),
        assigned_worker_id = COALESCE(?, assigned_worker_id),
        start_date = COALESCE(?, start_date),
        due_date = COALESCE(?, due_date),
        priority = COALESCE(?, priority),
        status = ?,
        progress_percent = ?
      WHERE id = ?
    `;

    await query(updateSql, [
      task_name ? task_name.trim() : null,
      description !== undefined ? description : null,
      assigned_worker_id !== undefined ? (assigned_worker_id || null) : null,
      start_date ? formatDate(start_date) : null,
      due_date ? formatDate(due_date) : null,
      priority || null,
      computedStatus,
      progress,
      id
    ]);

    return res.status(200).json({ success: true, message: 'Task updated successfully.' });
  } catch (error) {
    next(error);
  }
}

async function deleteTask(req, res, next) {
  try {
    const { id } = req.params;
    const [existing] = await query('SELECT id FROM tasks WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Task not found.' });
    }

    await query('DELETE FROM tasks WHERE id = ?', [id]);
    return res.status(200).json({ success: true, message: 'Task deleted successfully.' });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  createTask,
  getAllTasks,
  getTaskById,
  updateTask,
  deleteTask
};
