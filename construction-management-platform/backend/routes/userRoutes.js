const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');
const { authenticateJWT } = require('../middleware/authMiddleware');
const { authorizeRoles } = require('../middleware/roleMiddleware');

router.use(authenticateJWT);

router.get('/', userController.getAllUsers);
router.get('/:id', userController.getUserById);
router.put('/:id/status', authorizeRoles('ADMIN'), userController.updateUserStatus);

module.exports = router;
