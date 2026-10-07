const express = require('express');
const router = express.Router();
const machineryController = require('../controllers/machineryController');
const { authenticateJWT } = require('../middleware/authMiddleware');
const { authorizeRoles } = require('../middleware/roleMiddleware');

router.use(authenticateJWT);

router.post('/', authorizeRoles('ADMIN', 'OWNER'), machineryController.createMachinery);
router.get('/', machineryController.getAllMachinery);
router.get('/:id', machineryController.getMachineryById);
router.put('/:id', authorizeRoles('ADMIN', 'OWNER'), machineryController.updateMachinery);
router.delete('/:id', authorizeRoles('ADMIN', 'OWNER'), machineryController.deleteMachinery);
router.get('/:id/availability', machineryController.checkMachineryAvailability);
router.get('/:id/maintenance', machineryController.getMaintenanceRecords);
router.post('/:id/maintenance', authorizeRoles('ADMIN', 'OWNER'), machineryController.addMaintenanceRecord);

module.exports = router;
