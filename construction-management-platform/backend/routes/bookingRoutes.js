const express = require('express');
const router = express.Router();
const bookingController = require('../controllers/bookingController');
const { authenticateJWT } = require('../middleware/authMiddleware');
const { authorizeRoles } = require('../middleware/roleMiddleware');

router.use(authenticateJWT);

router.post('/', authorizeRoles('ADMIN', 'CONTRACTOR'), bookingController.createBooking);
router.get('/', bookingController.getAllBookings);
router.get('/:id', bookingController.getBookingById);
router.put('/:id/approve', authorizeRoles('ADMIN', 'OWNER'), bookingController.approveBooking);
router.put('/:id/reject', authorizeRoles('ADMIN', 'OWNER'), bookingController.rejectBooking);
router.put('/:id/cancel', authorizeRoles('ADMIN', 'CONTRACTOR'), bookingController.cancelBooking);
router.post('/:id/usage', authorizeRoles('ADMIN', 'SITE_MANAGER', 'CONTRACTOR'), bookingController.recordMachineUsage);
router.get('/:id/usage', bookingController.getMachineUsage);

module.exports = router;
