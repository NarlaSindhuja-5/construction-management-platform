const express = require('express');
const router = express.Router();
const expenseController = require('../controllers/expenseController');
const { authenticateJWT } = require('../middleware/authMiddleware');
const { authorizeRoles } = require('../middleware/roleMiddleware');

router.use(authenticateJWT);

router.post('/', authorizeRoles('ADMIN', 'CONTRACTOR', 'SITE_MANAGER'), expenseController.createExpense);
router.get('/', expenseController.getAllExpenses);
router.get('/project/:projectId', expenseController.getExpensesByProject);
router.delete('/:id', authorizeRoles('ADMIN', 'CONTRACTOR'), expenseController.deleteExpense);

module.exports = router;
