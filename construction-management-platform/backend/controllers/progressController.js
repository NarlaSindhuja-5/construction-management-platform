/**
 * Progress Controller
 * Handles daily construction site progress logs and synchronizes task completion percentages.
 */

const { query, getConnection } = require('../config/db');
const { formatDate } = require('../utils/dateUtils');

async function recordProgress(req, res, next) {
  const connection = await getConnection();
  try {
    const {
      project_id,
      site_id,
      task_id,
      progress_date,
      workers_present = 0,
      work_completed,
      progress_percent = 0,
      issues,
      remarks
    } = req.body;

    if (!project_id || !site_id) {
      return res.status(400).json({ success: false, message: 'Project ID and Site ID are required.' });
    }
    if (!progress_date) {
      return res.status(400).json({ success: false, message: 'Progress date is required.' });
    }
    if (!work_completed || !work_completed.trim()) {
      return res.status(400).json({ success: false, message: 'Work completed description is required.' });
    }

    await connection.beginTransaction();

    const cleanProgress = Math.min(100, Math.max(0, parseInt(progress_percent || 0, 10)));

    const insertSql = `
      INSERT INTO daily_progress (
        project_id, site_id, task_id, progress_date, workers_present,
        work_completed, progress_percent, issues, remarks, created_by, created_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
    `;

    const [result] = await connection.query(insertSql, [
      project_id,
      site_id,
      task_id || null,
      formatDate(progress_date),
      parseInt(workers_present || 0, 10),
      work_completed.trim(),
      cleanProgress,
      issues ? issues.trim() : null,
      remarks ? remarks.trim() : null,
      req.user.id
    ]);

    // If linked to a task, update task progress & status
    if (task_id) {
      let taskStatus = 'IN_PROGRESS';
      if (cleanProgress === 100) taskStatus = 'COMPLETED';
      else if (cleanProgress === 0) taskStatus = 'NOT_STARTED';

      await connection.query(
        'UPDATE tasks SET progress_percent = ?, status = ? WHERE id = ?',
        [cleanProgress, taskStatus, task_id]
      );
    }

    await connection.commit();

    return res.status(201).json({
      success: true,
      message: 'Daily progress recorded successfully.',
      progressId: result.insertId
    });
  } catch (error) {
    await connection.rollback();
    next(error);
  } finally {
    connection.release();
  }
}

async function getProgressByProject(req, res, next) {
  try {
    const { projectId } = req.params;

    const sql = `
      SELECT 
        dp.*,
        p.project_name,
        s.site_name,
        t.task_name,
        u.name AS recorded_by_name
      FROM daily_progress dp
      JOIN projects p ON dp.project_id = p.id
      JOIN sites s ON dp.site_id = s.id
      LEFT JOIN tasks t ON dp.task_id = t.id
      LEFT JOIN users u ON dp.created_by = u.id
      WHERE dp.project_id = ?
      ORDER BY dp.progress_date DESC, dp.id DESC
    `;

    const [rows] = await query(sql, [projectId]);
    return res.status(200).json({ success: true, count: rows.length, progress: rows });
  } catch (error) {
    next(error);
  }
}

async function getAllProgress(req, res, next) {
  try {
    const { project_id, site_id, task_id, progress_date } = req.query;

    let sql = `
      SELECT 
        dp.*,
        p.project_name,
        s.site_name,
        t.task_name,
        u.name AS recorded_by_name
      FROM daily_progress dp
      JOIN projects p ON dp.project_id = p.id
      JOIN sites s ON dp.site_id = s.id
      LEFT JOIN tasks t ON dp.task_id = t.id
      LEFT JOIN users u ON dp.created_by = u.id
      WHERE 1=1
    `;
    const params = [];

    if (project_id) {
      sql += ' AND dp.project_id = ?';
      params.push(project_id);
    }
    if (site_id) {
      sql += ' AND dp.site_id = ?';
      params.push(site_id);
    }
    if (task_id) {
      sql += ' AND dp.task_id = ?';
      params.push(task_id);
    }
    if (progress_date) {
      sql += ' AND dp.progress_date = ?';
      params.push(formatDate(progress_date));
    }

    sql += ' ORDER BY dp.progress_date DESC, dp.id DESC';

    const [rows] = await query(sql, params);
    return res.status(200).json({ success: true, count: rows.length, progress: rows });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  recordProgress,
  getProgressByProject,
  getAllProgress
};
