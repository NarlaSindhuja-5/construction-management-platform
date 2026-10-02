/**
 * End-to-End Construction Management & Resource Booking Platform
 * Main Server Entry Point
 */

const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });

const { testConnection } = require('./config/db');
const { errorHandler, notFoundHandler } = require('./middleware/errorMiddleware');

// Route Handlers
const authRoutes = require('./routes/authRoutes');
const userRoutes = require('./routes/userRoutes');
const projectRoutes = require('./routes/projectRoutes');
const siteRoutes = require('./routes/siteRoutes');
const machineryRoutes = require('./routes/machineryRoutes');
const bookingRoutes = require('./routes/bookingRoutes');
const workerRoutes = require('./routes/workerRoutes');
const materialRoutes = require('./routes/materialRoutes');
const taskRoutes = require('./routes/taskRoutes');
const progressRoutes = require('./routes/progressRoutes');
const expenseRoutes = require('./routes/expenseRoutes');
const issueRoutes = require('./routes/issueRoutes');
const dashboardRoutes = require('./routes/dashboardRoutes');
const reportRoutes = require('./routes/reportRoutes');

const app = express();
const PORT = process.env.PORT || 5000;

// HTTP Middleware
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

if (process.env.NODE_ENV !== 'test') {
  app.use(morgan('dev'));
}

// Health Check API
app.get('/api/health', (req, res) => {
  res.status(200).json({
    status: 'UP',
    platform: 'End-to-End Construction Management & Resource Booking Platform',
    timestamp: new Date().toISOString()
  });
});

// Mount REST API Routes
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/projects', projectRoutes);
app.use('/api/sites', siteRoutes);
app.use('/api/machinery', machineryRoutes);
app.use('/api/machinery-bookings', bookingRoutes);
app.use('/api/workers', workerRoutes);
app.use('/api/materials', materialRoutes);
app.use('/api/tasks', taskRoutes);
app.use('/api/progress', progressRoutes);
app.use('/api/expenses', expenseRoutes);
app.use('/api/issues', issueRoutes);
app.use('/api/dashboard', dashboardRoutes);
app.use('/api/reports', reportRoutes);

// Serve Frontend Static Files
const frontendPath = path.join(__dirname, '../frontend');
app.use(express.static(frontendPath));

// Fallback to index.html for root path or SPA navigation
app.get('/', (req, res) => {
  res.sendFile(path.join(frontendPath, 'index.html'));
});

// Undefined API handler
app.use('/api/*', notFoundHandler);

// Centralized error handler
app.use(errorHandler);

// Start Server
if (process.env.NODE_ENV !== 'test') {
  app.listen(PORT, async () => {
    console.log('================================================================');
    console.log('  CONSTRUCTION MANAGEMENT & RESOURCE BOOKING PLATFORM');
    console.log(`  Backend API Server listening on: http://localhost:${PORT}`);
    console.log(`  Frontend Web Application:        http://localhost:${PORT}`);
    console.log('================================================================');
    await testConnection();
  });
}

module.exports = app;
