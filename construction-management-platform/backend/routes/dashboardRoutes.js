const express = require('express');
const router = express.Router();
const dashboardController = require('../controllers/dashboardController');
const { authenticateJWT } = require('../middleware/authMiddleware');

router.use(authenticateJWT);

router.get('/summary', dashboardController.getSummary);
router.get('/projects', dashboardController.getProjects);
router.get('/alerts', dashboardController.getAlerts);

module.exports = router;
