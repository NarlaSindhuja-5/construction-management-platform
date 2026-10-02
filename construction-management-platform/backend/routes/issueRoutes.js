const express = require('express');
const router = express.Router();
const issueController = require('../controllers/issueController');
const { authenticateJWT } = require('../middleware/authMiddleware');
const { authorizeRoles } = require('../middleware/roleMiddleware');

router.use(authenticateJWT);

router.post('/', authorizeRoles('ADMIN', 'CONTRACTOR', 'SITE_MANAGER'), issueController.createIssue);
router.get('/', issueController.getAllIssues);
router.get('/:id', issueController.getIssueById);
router.put('/:id', authorizeRoles('ADMIN', 'CONTRACTOR', 'SITE_MANAGER'), issueController.updateIssue);

module.exports = router;
