const express = require('express');
const router = express.Router();
const progressController = require('../controllers/progressController');
const { authenticateJWT } = require('../middleware/authMiddleware');
const { authorizeRoles } = require('../middleware/roleMiddleware');

router.use(authenticateJWT);

router.post('/', authorizeRoles('ADMIN', 'SITE_MANAGER', 'CONTRACTOR'), progressController.recordProgress);
router.get('/', progressController.getAllProgress);
router.get('/project/:projectId', progressController.getProgressByProject);

module.exports = router;
