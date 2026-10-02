/**
 * Issue Controller
 * Tracks site incidents, machinery breakdowns, safety reports, and material shortages.
 */

const { query } = require('../config/db');

async function createIssue(req, res, next) {
  try {
    const {
      project_id,
      site_id,
      machinery_id,
      issue_type,
      description,
      priority = 'MEDIUM'
    } = req.body;

    if (!project_id) {
      return res.status(400).json({ success: false, message: 'Project ID is required.' });
    }
    if (!issue_type || !issue_type.trim()) {
      return res.status(400).json({ success: false, message: 'Issue type / category is required.' });
    }
    if (!description || !description.trim()) {
      return res.status(400).json({ success: false, message: 'Issue description is required.' });
    }

    const validPriorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
    const issuePriority = validPriorities.includes(priority) ? priority : 'MEDIUM';

    const insertSql = `
      INSERT INTO issues (
        project_id, site_id, machinery_id, issue_type, description,
        priority, status, reported_by, created_at
      ) VALUES (?, ?, ?, ?, ?, ?, 'OPEN', ?, NOW())
    `;

    const [result] = await query(insertSql, [
      project_id,
      site_id || null,
      machinery_id || null,
      issue_type.trim(),
      description.trim(),
      issuePriority,
      req.user.id
    ]);

    return res.status(201).json({
      success: true,
      message: 'Site issue reported successfully.',
      issueId: result.insertId
    });
  } catch (error) {
    next(error);
  }
}

async function getAllIssues(req, res, next) {
  try {
    const { project_id, site_id, machinery_id, status, priority } = req.query;

    let sql = `
      SELECT 
        i.*,
        p.project_name,
        s.site_name,
        m.machine_name,
        m.registration_number,
        u.name AS reported_by_name
      FROM issues i
      JOIN projects p ON i.project_id = p.id
      LEFT JOIN sites s ON i.site_id = s.id
      LEFT JOIN machinery m ON i.machinery_id = m.id
      JOIN users u ON i.reported_by = u.id
      WHERE 1=1
    `;
    const params = [];

    if (project_id) {
      sql += ' AND i.project_id = ?';
      params.push(project_id);
    }
    if (site_id) {
      sql += ' AND i.site_id = ?';
      params.push(site_id);
    }
    if (machinery_id) {
      sql += ' AND i.machinery_id = ?';
      params.push(machinery_id);
    }
    if (status) {
      sql += ' AND i.status = ?';
      params.push(status);
    }
    if (priority) {
      sql += ' AND i.priority = ?';
      params.push(priority);
    }

    sql += ' ORDER BY FIELD(i.priority, "CRITICAL", "HIGH", "MEDIUM", "LOW"), i.created_at DESC';

    const [issues] = await query(sql, params);
    return res.status(200).json({ success: true, count: issues.length, issues });
  } catch (error) {
    next(error);
  }
}

async function getIssueById(req, res, next) {
  try {
    const { id } = req.params;

    const [rows] = await query(`
      SELECT 
        i.*,
        p.project_name,
        s.site_name,
        m.machine_name,
        m.registration_number,
        u.name AS reported_by_name,
        u.email AS reported_by_email
      FROM issues i
      JOIN projects p ON i.project_id = p.id
      LEFT JOIN sites s ON i.site_id = s.id
      LEFT JOIN machinery m ON i.machinery_id = m.id
      JOIN users u ON i.reported_by = u.id
      WHERE i.id = ?
    `, [id]);

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Issue not found.' });
    }

    return res.status(200).json({ success: true, issue: rows[0] });
  } catch (error) {
    next(error);
  }
}

async function updateIssue(req, res, next) {
  try {
    const { id } = req.params;
    const { status, priority, description } = req.body;

    const [existing] = await query('SELECT id FROM issues WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Issue not found.' });
    }

    const resolvedAt = (status === 'RESOLVED' || status === 'CLOSED') ? 'NOW()' : null;

    let updateSql = `
      UPDATE issues SET
        status = COALESCE(?, status),
        priority = COALESCE(?, priority),
        description = COALESCE(?, description)
    `;
    const params = [status || null, priority || null, description ? description.trim() : null];

    if (resolvedAt) {
      updateSql += ', resolved_at = NOW()';
    }

    updateSql += ' WHERE id = ?';
    params.push(id);

    await query(updateSql, params);
    return res.status(200).json({ success: true, message: 'Issue updated successfully.' });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  createIssue,
  getAllIssues,
  getIssueById,
  updateIssue
};
