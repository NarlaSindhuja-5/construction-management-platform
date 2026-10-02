/**
 * Worker Controller
 * Manages construction workforce, trade skills, daily wages, and site assignments.
 */

const { query } = require('../config/db');

async function createWorker(req, res, next) {
  try {
    const { name, phone, skill, experience = 0, daily_wage, availability = 'AVAILABLE', site_id } = req.body;

    if (!name || !name.trim()) {
      return res.status(400).json({ success: false, message: 'Worker name is required.' });
    }
    if (!phone || !phone.trim()) {
      return res.status(400).json({ success: false, message: 'Phone number is required.' });
    }
    if (!skill || !skill.trim()) {
      return res.status(400).json({ success: false, message: 'Skill / Trade designation is required.' });
    }
    if (parseFloat(daily_wage) <= 0 || isNaN(parseFloat(daily_wage))) {
      return res.status(400).json({ success: false, message: 'Daily wage must be greater than zero.' });
    }

    const [result] = await query(
      `INSERT INTO workers (name, phone, skill, experience, daily_wage, availability, status, site_id, created_at)
       VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', ?, NOW())`,
      [
        name.trim(),
        phone.trim(),
        skill.trim(),
        parseInt(experience || 0, 10),
        parseFloat(daily_wage),
        availability || 'AVAILABLE',
        site_id || null
      ]
    );

    return res.status(201).json({
      success: true,
      message: 'Worker registered successfully.',
      workerId: result.insertId
    });
  } catch (error) {
    next(error);
  }
}

async function getAllWorkers(req, res, next) {
  try {
    const { skill, availability, site_id, search } = req.query;

    let sql = `
      SELECT 
        w.*,
        s.site_name,
        p.project_name
      FROM workers w
      LEFT JOIN sites s ON w.site_id = s.id
      LEFT JOIN projects p ON s.project_id = p.id
      WHERE w.status = 'ACTIVE'
    `;
    const params = [];

    if (skill) {
      sql += ' AND w.skill = ?';
      params.push(skill);
    }
    if (availability) {
      sql += ' AND w.availability = ?';
      params.push(availability);
    }
    if (site_id) {
      sql += ' AND w.site_id = ?';
      params.push(site_id);
    }
    if (search) {
      sql += ' AND (w.name LIKE ? OR w.phone LIKE ? OR w.skill LIKE ?)';
      params.push(`%${search}%`, `%${search}%`, `%${search}%`);
    }

    sql += ' ORDER BY w.created_at DESC';

    const [workers] = await query(sql, params);

    const formatted = workers.map(w => ({
      ...w,
      daily_wage: parseFloat(w.daily_wage)
    }));

    return res.status(200).json({ success: true, count: formatted.length, workers: formatted });
  } catch (error) {
    next(error);
  }
}

async function getWorkerById(req, res, next) {
  try {
    const { id } = req.params;

    const [rows] = await query(`
      SELECT 
        w.*,
        s.site_name,
        p.project_name
      FROM workers w
      LEFT JOIN sites s ON w.site_id = s.id
      LEFT JOIN projects p ON s.project_id = p.id
      WHERE w.id = ?
    `, [id]);

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Worker not found.' });
    }

    const worker = {
      ...rows[0],
      daily_wage: parseFloat(rows[0].daily_wage)
    };

    // Get assigned tasks
    const [tasks] = await query('SELECT * FROM tasks WHERE assigned_worker_id = ?', [id]);

    return res.status(200).json({ success: true, worker, tasks });
  } catch (error) {
    next(error);
  }
}

async function updateWorker(req, res, next) {
  try {
    const { id } = req.params;
    const { name, phone, skill, experience, daily_wage, availability, status, site_id } = req.body;

    const [existing] = await query('SELECT id FROM workers WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Worker not found.' });
    }

    const updateSql = `
      UPDATE workers SET
        name = COALESCE(?, name),
        phone = COALESCE(?, phone),
        skill = COALESCE(?, skill),
        experience = COALESCE(?, experience),
        daily_wage = COALESCE(?, daily_wage),
        availability = COALESCE(?, availability),
        status = COALESCE(?, status),
        site_id = COALESCE(?, site_id)
      WHERE id = ?
    `;

    await query(updateSql, [
      name ? name.trim() : null,
      phone ? phone.trim() : null,
      skill ? skill.trim() : null,
      experience !== undefined ? parseInt(experience, 10) : null,
      daily_wage !== undefined ? parseFloat(daily_wage) : null,
      availability || null,
      status || null,
      site_id !== undefined ? (site_id || null) : null,
      id
    ]);

    return res.status(200).json({ success: true, message: 'Worker updated successfully.' });
  } catch (error) {
    next(error);
  }
}

async function deleteWorker(req, res, next) {
  try {
    const { id } = req.params;
    const [existing] = await query('SELECT id FROM workers WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Worker not found.' });
    }

    // Soft delete or set INACTIVE
    await query("UPDATE workers SET status = 'INACTIVE', availability = 'AVAILABLE', site_id = NULL WHERE id = ?", [id]);
    return res.status(200).json({ success: true, message: 'Worker removed successfully.' });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  createWorker,
  getAllWorkers,
  getWorkerById,
  updateWorker,
  deleteWorker
};
