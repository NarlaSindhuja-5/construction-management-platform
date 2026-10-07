const express = require('express');
const router = express.Router();
const projectController = require('../controllers/projectController');
const { authenticateJWT } = require('../middleware/authMiddleware');
const { authorizeRoles } = require('../middleware/roleMiddleware');

router.use(authenticateJWT);

router.post('/', authorizeRoles('ADMIN', 'CONTRACTOR'), projectController.createProject);
router.get('/', projectController.getAllProjects);
router.get('/:id', projectController.getProjectById);
router.put('/:id', authorizeRoles('ADMIN', 'CONTRACTOR'), projectController.updateProject);
router.delete('/:id', authorizeRoles('ADMIN', 'CONTRACTOR'), projectController.deleteProject);
router.get('/:id/resources', projectController.getProjectResources);

module.exports = router;
