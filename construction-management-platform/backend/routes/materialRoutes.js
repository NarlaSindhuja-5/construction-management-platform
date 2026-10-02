const express = require('express');
const router = express.Router();
const materialController = require('../controllers/materialController');
const { authenticateJWT } = require('../middleware/authMiddleware');
const { authorizeRoles } = require('../middleware/roleMiddleware');

router.use(authenticateJWT);

router.post('/', authorizeRoles('ADMIN', 'CONTRACTOR', 'SITE_MANAGER'), materialController.createMaterial);
router.get('/', materialController.getAllMaterials);
router.get('/:id', materialController.getMaterialById);
router.put('/:id', authorizeRoles('ADMIN', 'CONTRACTOR', 'SITE_MANAGER'), materialController.updateMaterial);
router.delete('/:id', authorizeRoles('ADMIN', 'CONTRACTOR'), materialController.deleteMaterial);

module.exports = router;
