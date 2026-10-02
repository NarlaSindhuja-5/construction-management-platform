/**
 * Expense Controller
 * Tracks construction financial expenditures across machinery, labour, materials, fuel, and transport.
 */

const { query } = require('../config/db');
const { formatDate } = require('../utils/dateUtils');

async function createExpense(req, res, next) {
  try {
    const {
      project_id,
      site_id,
      category,
      amount,
      expense_date,
      description,
      reference_type = 'MANUAL',
      reference_id = null
    } = req.body;

    if (!project_id) {
      return res.status(400).json({ success: false, message: 'Project ID is required.' });
    }
    if (!category) {
      return res.status(400).json({ success: false, message: 'Expense category is required.' });
    }
    const validCategories = ['MACHINERY', 'LABOUR', 'MATERIAL', 'TRANSPORT', 'FUEL', 'OTHER'];
    if (!validCategories.includes(category.toUpperCase())) {
      return res.status(400).json({
        success: false,
        message: `Invalid category. Must be one of: ${validCategories.join(', ')}.`
      });
    }
    if (parseFloat(amount) <= 0 || isNaN(parseFloat(amount))) {
      return res.status(400).json({ success: false, message: 'Expense amount must be greater than zero.' });
    }
    if (!expense_date) {
      return res.status(400).json({ success: false, message: 'Expense date is required.' });
    }
    if (!description || !description.trim()) {
      return res.status(400).json({ success: false, message: 'Description is required.' });
    }

    const insertSql = `
      INSERT INTO expenses (
        project_id, site_id, category, amount, expense_date, description,
        reference_type, reference_id, created_by, created_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
    `;

    const [result] = await query(insertSql, [
      project_id,
      site_id || null,
      category.toUpperCase(),
      parseFloat(amount),
      formatDate(expense_date),
      description.trim(),
      reference_type || 'MANUAL',
      reference_id || null,
      req.user.id
    ]);

    return res.status(201).json({
      success: true,
      message: 'Expense recorded successfully.',
      expenseId: result.insertId
    });
  } catch (error) {
    next(error);
  }
}

async function getAllExpenses(req, res, next) {
  try {
    const { project_id, site_id, category, start_date, end_date } = req.query;

    let sql = `
      SELECT 
        e.*,
        p.project_name,
        s.site_name,
        u.name AS created_by_name
      FROM expenses e
      JOIN projects p ON e.project_id = p.id
      LEFT JOIN sites s ON e.site_id = s.id
      LEFT JOIN users u ON e.created_by = u.id
      WHERE 1=1
    `;
    const params = [];

    if (project_id) {
      sql += ' AND e.project_id = ?';
      params.push(project_id);
    }
    if (site_id) {
      sql += ' AND e.site_id = ?';
      params.push(site_id);
    }
    if (category) {
      sql += ' AND e.category = ?';
      params.push(category);
    }
    if (start_date) {
      sql += ' AND e.expense_date >= ?';
      params.push(formatDate(start_date));
    }
    if (end_date) {
      sql += ' AND e.expense_date <= ?';
      params.push(formatDate(end_date));
    }

    sql += ' ORDER BY e.expense_date DESC, e.id DESC';

    const [expenses] = await query(sql, params);

    const formatted = expenses.map(e => ({
      ...e,
      amount: parseFloat(e.amount)
    }));

    return res.status(200).json({ success: true, count: formatted.length, expenses: formatted });
  } catch (error) {
    next(error);
  }
}

async function getExpensesByProject(req, res, next) {
  try {
    const { projectId } = req.params;

    const [expenses] = await query(`
      SELECT 
        e.*,
        s.site_name,
        u.name AS created_by_name
      FROM expenses e
      LEFT JOIN sites s ON e.site_id = s.id
      LEFT JOIN users u ON e.created_by = u.id
      WHERE e.project_id = ?
      ORDER BY e.expense_date DESC, e.id DESC
    `, [projectId]);

    const [summary] = await query(`
      SELECT category, COALESCE(SUM(amount), 0) AS total_amount, COUNT(*) AS count
      FROM expenses
      WHERE project_id = ?
      GROUP BY category
    `, [projectId]);

    const totalSpent = expenses.reduce((acc, e) => acc + parseFloat(e.amount), 0);

    return res.status(200).json({
      success: true,
      totalSpent: parseFloat(totalSpent.toFixed(2)),
      categoryBreakdown: summary,
      expenses: expenses.map(e => ({ ...e, amount: parseFloat(e.amount) }))
    });
  } catch (error) {
    next(error);
  }
}

async function deleteExpense(req, res, next) {
  try {
    const { id } = req.params;
    const [existing] = await query('SELECT id FROM expenses WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Expense record not found.' });
    }

    await query('DELETE FROM expenses WHERE id = ?', [id]);
    return res.status(200).json({ success: true, message: 'Expense record deleted successfully.' });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  createExpense,
  getAllExpenses,
  getExpensesByProject,
  deleteExpense
};
