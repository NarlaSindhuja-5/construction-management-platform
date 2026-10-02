const express = require('express');
const router = express.Router();
const workerController = require('../controllers/workerController');
const { authenticateJWT } = require('../middleware/authMiddleware');
const { authorizeRoles } = require('../middleware/roleMiddleware');

router.use(authenticateJWT);

router.post('/', authorizeRoles('ADMIN', 'CONTRACTOR', 'SITE_MANAGER'), workerController.createWorker);
router.get('/', workerController.getAllWorkers);
router.get('/:id', workerController.getWorkerById);
router.put('/:id', authorizeRoles('ADMIN', 'CONTRACTOR', 'SITE_MANAGER'), workerController.updateWorker);
router.delete('/:id', authorizeRoles('ADMIN', 'CONTRACTOR'), workerController.deleteWorker);

module.exports = router;
