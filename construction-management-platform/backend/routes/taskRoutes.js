const express = require('express');
const router = express.Router();
const taskController = require('../controllers/taskController');
const { authenticateJWT } = require('../middleware/authMiddleware');
const { authorizeRoles } = require('../middleware/roleMiddleware');

router.use(authenticateJWT);

router.post('/', authorizeRoles('ADMIN', 'CONTRACTOR', 'SITE_MANAGER'), taskController.createTask);
router.get('/', taskController.getAllTasks);
router.get('/:id', taskController.getTaskById);
router.put('/:id', authorizeRoles('ADMIN', 'CONTRACTOR', 'SITE_MANAGER'), taskController.updateTask);
router.delete('/:id', authorizeRoles('ADMIN', 'CONTRACTOR'), taskController.deleteTask);

module.exports = router;
