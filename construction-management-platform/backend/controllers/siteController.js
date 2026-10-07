/**
 * Site Controller
 * Handles site creation, updates, and association with projects and site managers.
 */

const { query } = require('../config/db');

async function createSite(req, res, next) {
  try {
    const { project_id, site_name, location, address, site_manager_id, status = 'ACTIVE' } = req.body;

    if (!project_id) {
      return res.status(400).json({ success: false, message: 'Project ID is required.' });
    }
    if (!site_name || !site_name.trim()) {
      return res.status(400).json({ success: false, message: 'Site name is required.' });
    }
    if (!location || !location.trim()) {
      return res.status(400).json({ success: false, message: 'Location is required.' });
    }

    // Verify project exists
    const [projects] = await query('SELECT id FROM projects WHERE id = ?', [project_id]);
    if (projects.length === 0) {
      return res.status(404).json({ success: false, message: 'Parent project not found.' });
    }

    const [result] = await query(
      `INSERT INTO sites (project_id, site_name, location, address, site_manager_id, status, created_at)
       VALUES (?, ?, ?, ?, ?, ?, NOW())`,
      [
        project_id,
        site_name.trim(),
        location.trim(),
        address ? address.trim() : null,
        site_manager_id || null,
        status || 'ACTIVE'
      ]
    );

    return res.status(201).json({
      success: true,
      message: 'Site created successfully.',
      siteId: result.insertId
    });
  } catch (error) {
    next(error);
  }
}

async function getAllSites(req, res, next) {
  try {
    const { project_id, status, search } = req.query;

    let sql = `
      SELECT 
        s.*,
        p.project_name,
        u.name AS site_manager_name,
        u.phone AS site_manager_phone,
        COUNT(DISTINCT w.id) AS workers_count,
        COUNT(DISTINCT t.id) AS tasks_count
      FROM sites s
      JOIN projects p ON s.project_id = p.id
      LEFT JOIN users u ON s.site_manager_id = u.id
      LEFT JOIN workers w ON s.id = w.site_id
      LEFT JOIN tasks t ON s.id = t.site_id
      WHERE 1=1
    `;
    const params = [];

    if (project_id) {
      sql += ' AND s.project_id = ?';
      params.push(project_id);
    }
    if (status) {
      sql += ' AND s.status = ?';
      params.push(status);
    }
    if (search) {
      sql += ' AND (s.site_name LIKE ? OR s.location LIKE ? OR p.project_name LIKE ?)';
      params.push(`%${search}%`, `%${search}%`, `%${search}%`);
    }

    sql += ' GROUP BY s.id ORDER BY s.created_at DESC';

    const [sites] = await query(sql, params);
    return res.status(200).json({ success: true, count: sites.length, sites });
  } catch (error) {
    next(error);
  }
}

async function getSiteById(req, res, next) {
  try {
    const { id } = req.params;

    const [sites] = await query(`
      SELECT 
        s.*,
        p.project_name,
        p.client_name,
        u.name AS site_manager_name,
        u.email AS site_manager_email,
        u.phone AS site_manager_phone
      FROM sites s
      JOIN projects p ON s.project_id = p.id
      LEFT JOIN users u ON s.site_manager_id = u.id
      WHERE s.id = ?
    `, [id]);

    if (sites.length === 0) {
      return res.status(404).json({ success: false, message: 'Site not found.' });
    }

    return res.status(200).json({ success: true, site: sites[0] });
  } catch (error) {
    next(error);
  }
}

async function updateSite(req, res, next) {
  try {
    const { id } = req.params;
    const { site_name, location, address, site_manager_id, status } = req.body;

    const [existing] = await query('SELECT id FROM sites WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Site not found.' });
    }

    const updateSql = `
      UPDATE sites SET
        site_name = COALESCE(?, site_name),
        location = COALESCE(?, location),
        address = COALESCE(?, address),
        site_manager_id = COALESCE(?, site_manager_id),
        status = COALESCE(?, status)
      WHERE id = ?
    `;

    await query(updateSql, [
      site_name ? site_name.trim() : null,
      location ? location.trim() : null,
      address !== undefined ? address : null,
      site_manager_id !== undefined ? (site_manager_id || null) : null,
      status || null,
      id
    ]);

    return res.status(200).json({ success: true, message: 'Site updated successfully.' });
  } catch (error) {
    next(error);
  }
}

async function deleteSite(req, res, next) {
  try {
    const { id } = req.params;
    const [existing] = await query('SELECT id FROM sites WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Site not found.' });
    }

    await query('DELETE FROM sites WHERE id = ?', [id]);
    return res.status(200).json({ success: true, message: 'Site deleted successfully.' });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  createSite,
  getAllSites,
  getSiteById,
  updateSite,
  deleteSite
};
