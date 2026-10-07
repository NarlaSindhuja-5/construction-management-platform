const express = require('express');
const router = express.Router();
const reportController = require('../controllers/reportController');
const { authenticateJWT } = require('../middleware/authMiddleware');

router.use(authenticateJWT);

router.get('/project/:projectId', reportController.getProjectReport);
router.get('/expenses/:projectId', reportController.getExpensesReport);
router.get('/progress/:projectId', reportController.getProgressReport);

module.exports = router;
