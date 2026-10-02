/**
 * Role-Based Access Control Middleware
 * Restricts endpoint execution to specified user roles.
 */

function authorizeRoles(...allowedRoles) {
  return (req, res, next) => {
    if (!req.user) {
      return res.status(401).json({
        success: false,
        message: 'Authentication required.'
      });
    }

    if (!allowedRoles.includes(req.user.role)) {
      return res.status(403).json({
        success: false,
        message: `Insufficient permission. Action requires one of: [${allowedRoles.join(', ')}].`
      });
    }

    next();
  };
}

module.exports = {
  authorizeRoles
};
