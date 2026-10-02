/**
 * Authentication Controller
 * Handles user registration, login, token issuance, and profile fetching.
 */

const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { query } = require('../config/db');

const JWT_SECRET = process.env.JWT_SECRET || 'construction_mgmt_super_secret_jwt_key_2026_x89a';
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '7d';

/**
 * Register a new user
 * POST /api/auth/register
 */
async function register(req, res, next) {
  try {
    const { name, email, password, phone, role = 'CONTRACTOR' } = req.body;

    if (!name || !name.trim()) {
      return res.status(400).json({ success: false, message: 'Full name is required.' });
    }
    if (!email || !email.includes('@')) {
      return res.status(400).json({ success: false, message: 'Valid email address is required.' });
    }
    if (!password || password.length < 6) {
      return res.status(400).json({ success: false, message: 'Password must be at least 6 characters.' });
    }

    const validRoles = ['ADMIN', 'CONTRACTOR', 'OWNER', 'SITE_MANAGER'];
    const assignedRole = validRoles.includes(role.toUpperCase()) ? role.toUpperCase() : 'CONTRACTOR';

    // Check if email already registered
    const [existing] = await query('SELECT id FROM users WHERE email = ?', [email.trim().toLowerCase()]);
    if (existing.length > 0) {
      return res.status(409).json({ success: false, message: 'Email is already registered. Please login.' });
    }

    // Hash password
    const salt = await bcrypt.genSalt(10);
    const passwordHash = await bcrypt.hash(password, salt);

    const [result] = await query(
      `INSERT INTO users (name, email, password_hash, phone, role, status, created_at)
       VALUES (?, ?, ?, ?, ?, 'ACTIVE', NOW())`,
      [name.trim(), email.trim().toLowerCase(), passwordHash, phone || null, assignedRole]
    );

    const newUserId = result.insertId;

    // Issue JWT
    const token = jwt.sign(
      { id: newUserId, email: email.trim().toLowerCase(), role: assignedRole },
      JWT_SECRET,
      { expiresIn: JWT_EXPIRES_IN }
    );

    return res.status(201).json({
      success: true,
      message: 'User account registered successfully.',
      token,
      user: {
        id: newUserId,
        name: name.trim(),
        email: email.trim().toLowerCase(),
        phone: phone || null,
        role: assignedRole
      }
    });
  } catch (error) {
    next(error);
  }
}

/**
 * Login user
 * POST /api/auth/login
 */
async function login(req, res, next) {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ success: false, message: 'Email and password are required.' });
    }

    const [rows] = await query(
      'SELECT id, name, email, password_hash, phone, role, status FROM users WHERE email = ?',
      [email.trim().toLowerCase()]
    );

    if (rows.length === 0) {
      return res.status(401).json({ success: false, message: 'Invalid email or password.' });
    }

    const user = rows[0];

    if (user.status !== 'ACTIVE') {
      return res.status(403).json({ success: false, message: 'Account is deactivated. Please contact administrator.' });
    }

    const isMatch = await bcrypt.compare(password, user.password_hash);
    if (!isMatch) {
      return res.status(401).json({ success: false, message: 'Invalid email or password.' });
    }

    // Issue JWT
    const token = jwt.sign(
      { id: user.id, email: user.email, role: user.role },
      JWT_SECRET,
      { expiresIn: JWT_EXPIRES_IN }
    );

    return res.status(200).json({
      success: true,
      message: 'Login successful.',
      token,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        phone: user.phone,
        role: user.role
      }
    });
  } catch (error) {
    next(error);
  }
}

/**
 * Get current authenticated user profile
 * GET /api/auth/me
 */
async function getMe(req, res) {
  return res.status(200).json({
    success: true,
    user: req.user
  });
}

module.exports = {
  register,
  login,
  getMe
};
