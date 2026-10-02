/**
 * Error Handling Middleware
 * Centralized error handler returning clean JSON error responses.
 */

function errorHandler(err, req, res, next) {
  console.error('[Error Caught]:', err.stack || err.message || err);

  const statusCode = err.statusCode || (res.statusCode >= 400 ? res.statusCode : 500);

  // Friendly messages for specific database errors
  let message = err.message || 'Internal Server Error';
  if (err.code === 'ER_DUP_ENTRY') {
    message = 'Duplicate entry conflict. A record with this unique identifier already exists.';
  } else if (err.code === 'ER_NO_REFERENCED_ROW_2') {
    message = 'Foreign key constraint violated: referenced entity does not exist.';
  } else if (err.code === 'ECONNREFUSED') {
    message = 'Database service unavailable. Please check MySQL server connection.';
  }

  res.status(statusCode).json({
    success: false,
    message,
    ...(process.env.NODE_ENV === 'development' && { details: err.stack })
  });
}

function notFoundHandler(req, res) {
  res.status(404).json({
    success: false,
    message: `API Route Not Found: [${req.method}] ${req.originalUrl}`
  });
}

module.exports = {
  errorHandler,
  notFoundHandler
};
