/**
 * JWT Authentication Middleware
 * Validates bearer token and injects authenticated user payload into req.user
 */

const jwt = require('jsonwebtoken');
const { query } = require('../config/db');

async function authenticateJWT(req, res, next) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({
        success: false,
        message: 'Authentication required. No authorization token provided.'
      });
    }

    const token = authHeader.split(' ')[1];
    const secret = process.env.JWT_SECRET || 'construction_mgmt_super_secret_jwt_key_2026_x89a';

    let decoded;
    try {
      decoded = jwt.verify(token, secret);
    } catch (err) {
      return res.status(401).json({
        success: false,
        message: 'Session expired or invalid token. Please login again.'
      });
    }

    // Verify user is active in DB
    const [rows] = await query(
      'SELECT id, name, email, phone, role, status FROM users WHERE id = ?',
      [decoded.id]
    );

    if (!rows || rows.length === 0) {
      return res.status(401).json({
        success: false,
        message: 'User account not found.'
      });
    }

    const user = rows[0];
    if (user.status !== 'ACTIVE') {
      return res.status(403).json({
        success: false,
        message: 'Your account is currently inactive. Please contact system admin.'
      });
    }

    req.user = user;
    next();
  } catch (error) {
    console.error('[AuthMiddleware Error]', error);
    return res.status(500).json({
      success: false,
      message: 'Authentication error processing request.'
    });
  }
}

module.exports = {
  authenticateJWT
};
