/**
 * User Controller
 * Handles user management and team lookups.
 */

const { query } = require('../config/db');

async function getAllUsers(req, res, next) {
  try {
    const { role, status, search } = req.query;
    let sql = 'SELECT id, name, email, phone, role, status, created_at FROM users WHERE 1=1';
    const params = [];

    if (role) {
      sql += ' AND role = ?';
      params.push(role);
    }
    if (status) {
      sql += ' AND status = ?';
      params.push(status);
    }
    if (search) {
      sql += ' AND (name LIKE ? OR email LIKE ?)';
      params.push(`%${search}%`, `%${search}%`);
    }

    sql += ' ORDER BY id ASC';
    const [users] = await query(sql, params);

    return res.status(200).json({ success: true, count: users.length, users });
  } catch (error) {
    next(error);
  }
}

async function getUserById(req, res, next) {
  try {
    const { id } = req.params;
    const [users] = await query('SELECT id, name, email, phone, role, status, created_at FROM users WHERE id = ?', [id]);
    if (users.length === 0) {
      return res.status(404).json({ success: false, message: 'User not found.' });
    }
    return res.status(200).json({ success: true, user: users[0] });
  } catch (error) {
    next(error);
  }
}

async function updateUserStatus(req, res, next) {
  try {
    const { id } = req.params;
    const { status } = req.body;
    if (!['ACTIVE', 'INACTIVE'].includes(status)) {
      return res.status(400).json({ success: false, message: 'Status must be ACTIVE or INACTIVE.' });
    }

    await query('UPDATE users SET status = ? WHERE id = ?', [status, id]);
    return res.status(200).json({ success: true, message: `User status updated to ${status}.` });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  getAllUsers,
  getUserById,
  updateUserStatus
};
