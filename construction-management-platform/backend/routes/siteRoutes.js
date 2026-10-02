const express = require('express');
const router = express.Router();
const siteController = require('../controllers/siteController');
const { authenticateJWT } = require('../middleware/authMiddleware');
const { authorizeRoles } = require('../middleware/roleMiddleware');

router.use(authenticateJWT);

router.post('/', authorizeRoles('ADMIN', 'CONTRACTOR'), siteController.createSite);
router.get('/', siteController.getAllSites);
router.get('/:id', siteController.getSiteById);
router.put('/:id', authorizeRoles('ADMIN', 'CONTRACTOR', 'SITE_MANAGER'), siteController.updateSite);
router.delete('/:id', authorizeRoles('ADMIN', 'CONTRACTOR'), siteController.deleteSite);

module.exports = router;
