/**
 * Material Controller
 * Handles inventory, stock levels, suppliers, and low-stock threshold alerting.
 */

const { query } = require('../config/db');
const { isLowStock } = require('../utils/calculations');

async function createMaterial(req, res, next) {
  try {
    const {
      site_id,
      material_name,
      category,
      quantity = 0,
      minimum_quantity = 0,
      unit,
      unit_price = 0,
      supplier
    } = req.body;

    if (!site_id) {
      return res.status(400).json({ success: false, message: 'Site ID is required.' });
    }
    if (!material_name || !material_name.trim()) {
      return res.status(400).json({ success: false, message: 'Material name is required.' });
    }
    if (!category || !category.trim()) {
      return res.status(400).json({ success: false, message: 'Category is required.' });
    }
    if (!unit || !unit.trim()) {
      return res.status(400).json({ success: false, message: 'Unit of measurement is required.' });
    }

    const [result] = await query(
      `INSERT INTO materials (
        site_id, material_name, category, quantity, minimum_quantity, unit, unit_price, supplier, created_at
       ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())`,
      [
        site_id,
        material_name.trim(),
        category.trim(),
        parseFloat(quantity || 0),
        parseFloat(minimum_quantity || 0),
        unit.trim(),
        parseFloat(unit_price || 0),
        supplier ? supplier.trim() : null
      ]
    );

    return res.status(201).json({
      success: true,
      message: 'Material inventory registered successfully.',
      materialId: result.insertId
    });
  } catch (error) {
    next(error);
  }
}

async function getAllMaterials(req, res, next) {
  try {
    const { site_id, project_id, category, low_stock, search } = req.query;

    let sql = `
      SELECT 
        m.*,
        s.site_name,
        p.project_name
      FROM materials m
      JOIN sites s ON m.site_id = s.id
      JOIN projects p ON s.project_id = p.id
      WHERE 1=1
    `;
    const params = [];

    if (site_id) {
      sql += ' AND m.site_id = ?';
      params.push(site_id);
    }
    if (project_id) {
      sql += ' AND s.project_id = ?';
      params.push(project_id);
    }
    if (category) {
      sql += ' AND m.category = ?';
      params.push(category);
    }
    if (low_stock === 'true' || low_stock === '1') {
      sql += ' AND m.quantity < m.minimum_quantity';
    }
    if (search) {
      sql += ' AND (m.material_name LIKE ? OR m.category LIKE ? OR m.supplier LIKE ?)';
      params.push(`%${search}%`, `%${search}%`, `%${search}%`);
    }

    sql += ' ORDER BY m.created_at DESC';

    const [materials] = await query(sql, params);

    const formatted = materials.map(m => {
      const q = parseFloat(m.quantity);
      const minQ = parseFloat(m.minimum_quantity);
      return {
        ...m,
        quantity: q,
        minimum_quantity: minQ,
        unit_price: parseFloat(m.unit_price),
        total_value: parseFloat((q * parseFloat(m.unit_price)).toFixed(2)),
        is_low_stock: isLowStock(q, minQ)
      };
    });

    return res.status(200).json({ success: true, count: formatted.length, materials: formatted });
  } catch (error) {
    next(error);
  }
}

async function getMaterialById(req, res, next) {
  try {
    const { id } = req.params;

    const [rows] = await query(`
      SELECT 
        m.*,
        s.site_name,
        p.project_name
      FROM materials m
      JOIN sites s ON m.site_id = s.id
      JOIN projects p ON s.project_id = p.id
      WHERE m.id = ?
    `, [id]);

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Material not found.' });
    }

    const m = rows[0];
    const q = parseFloat(m.quantity);
    const minQ = parseFloat(m.minimum_quantity);

    return res.status(200).json({
      success: true,
      material: {
        ...m,
        quantity: q,
        minimum_quantity: minQ,
        unit_price: parseFloat(m.unit_price),
        total_value: parseFloat((q * parseFloat(m.unit_price)).toFixed(2)),
        is_low_stock: isLowStock(q, minQ)
      }
    });
  } catch (error) {
    next(error);
  }
}

async function updateMaterial(req, res, next) {
  try {
    const { id } = req.params;
    const {
      material_name,
      category,
      quantity,
      minimum_quantity,
      unit,
      unit_price,
      supplier
    } = req.body;

    const [existing] = await query('SELECT id FROM materials WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Material not found.' });
    }

    const updateSql = `
      UPDATE materials SET
        material_name = COALESCE(?, material_name),
        category = COALESCE(?, category),
        quantity = COALESCE(?, quantity),
        minimum_quantity = COALESCE(?, minimum_quantity),
        unit = COALESCE(?, unit),
        unit_price = COALESCE(?, unit_price),
        supplier = COALESCE(?, supplier)
      WHERE id = ?
    `;

    await query(updateSql, [
      material_name ? material_name.trim() : null,
      category ? category.trim() : null,
      quantity !== undefined ? parseFloat(quantity) : null,
      minimum_quantity !== undefined ? parseFloat(minimum_quantity) : null,
      unit ? unit.trim() : null,
      unit_price !== undefined ? parseFloat(unit_price) : null,
      supplier !== undefined ? supplier.trim() : null,
      id
    ]);

    return res.status(200).json({ success: true, message: 'Material updated successfully.' });
  } catch (error) {
    next(error);
  }
}

async function deleteMaterial(req, res, next) {
  try {
    const { id } = req.params;
    const [existing] = await query('SELECT id FROM materials WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Material not found.' });
    }

    await query('DELETE FROM materials WHERE id = ?', [id]);
    return res.status(200).json({ success: true, message: 'Material deleted successfully.' });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  createMaterial,
  getAllMaterials,
  getMaterialById,
  updateMaterial,
  deleteMaterial
};
