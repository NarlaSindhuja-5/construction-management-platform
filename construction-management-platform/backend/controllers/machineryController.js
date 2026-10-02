/**
 * Machinery Controller
 * Handles machinery listings, registrations, search filters, availability calendar,
 * and scheduled maintenance.
 */

const { query } = require('../config/db');
const { checkAvailability } = require('../services/bookingService');
const { isValidDateRange, formatDate } = require('../utils/dateUtils');

async function createMachinery(req, res, next) {
  try {
    const {
      machine_name,
      machine_type,
      registration_number,
      location,
      daily_rate,
      operator_available = true,
      operator_charge = 0,
      description,
      owner_id
    } = req.body;

    if (!machine_name || !machine_name.trim()) {
      return res.status(400).json({ success: false, message: 'Machine name is required.' });
    }
    if (!machine_type || !machine_type.trim()) {
      return res.status(400).json({ success: false, message: 'Machine type is required.' });
    }
    if (!registration_number || !registration_number.trim()) {
      return res.status(400).json({ success: false, message: 'Unique registration number is required.' });
    }
    if (!location || !location.trim()) {
      return res.status(400).json({ success: false, message: 'Location is required.' });
    }
    if (parseFloat(daily_rate) <= 0 || isNaN(parseFloat(daily_rate))) {
      return res.status(400).json({ success: false, message: 'Daily rental rate must be greater than zero.' });
    }

    // Determine owner: If caller is OWNER, assign their ID. If ADMIN, can specify owner_id or default to caller
    const effectiveOwnerId = (req.user.role === 'OWNER' || !owner_id) ? req.user.id : owner_id;

    // Check duplicate registration
    const [existing] = await query('SELECT id FROM machinery WHERE registration_number = ?', [registration_number.trim()]);
    if (existing.length > 0) {
      return res.status(409).json({ success: false, message: 'A machine with this registration number already exists.' });
    }

    const insertSql = `
      INSERT INTO machinery (
        owner_id, machine_name, machine_type, registration_number, location,
        daily_rate, operator_available, operator_charge, status, description, created_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'AVAILABLE', ?, NOW())
    `;

    const [result] = await query(insertSql, [
      effectiveOwnerId,
      machine_name.trim(),
      machine_type.trim(),
      registration_number.trim().toUpperCase(),
      location.trim(),
      parseFloat(daily_rate),
      operator_available ? 1 : 0,
      parseFloat(operator_charge || 0),
      description ? description.trim() : null
    ]);

    return res.status(201).json({
      success: true,
      message: 'Machinery registered successfully.',
      machineryId: result.insertId
    });
  } catch (error) {
    next(error);
  }
}

async function getAllMachinery(req, res, next) {
  try {
    const {
      type,
      location,
      minRate,
      maxRate,
      startDate,
      endDate,
      operatorRequired,
      ownerId,
      status,
      search
    } = req.query;

    let sql = `
      SELECT 
        m.*,
        u.name AS owner_name,
        u.email AS owner_email,
        u.phone AS owner_phone
      FROM machinery m
      JOIN users u ON m.owner_id = u.id
      WHERE 1=1
    `;
    const params = [];

    // Role-specific auto-filtering: If caller is OWNER and did not specify otherwise, show their machines
    if (req.user && req.user.role === 'OWNER' && !req.query.all) {
      sql += ' AND m.owner_id = ?';
      params.push(req.user.id);
    } else if (ownerId) {
      sql += ' AND m.owner_id = ?';
      params.push(ownerId);
    }

    if (type) {
      sql += ' AND m.machine_type = ?';
      params.push(type);
    }
    if (location) {
      sql += ' AND m.location LIKE ?';
      params.push(`%${location}%`);
    }
    if (minRate) {
      sql += ' AND m.daily_rate >= ?';
      params.push(parseFloat(minRate));
    }
    if (maxRate) {
      sql += ' AND m.daily_rate <= ?';
      params.push(parseFloat(maxRate));
    }
    if (status) {
      sql += ' AND m.status = ?';
      params.push(status);
    }
    if (operatorRequired === 'true' || operatorRequired === '1') {
      sql += ' AND m.operator_available = 1';
    }
    if (search) {
      sql += ' AND (m.machine_name LIKE ? OR m.machine_type LIKE ? OR m.registration_number LIKE ? OR m.location LIKE ?)';
      params.push(`%${search}%`, `%${search}%`, `%${search}%`, `%${search}%`);
    }

    sql += ' ORDER BY m.created_at DESC';

    const [machineryList] = await query(sql, params);

    // If date range is specified, evaluate exact date-based availability for each machine
    let results = machineryList;
    if (startDate && endDate && isValidDateRange(startDate, endDate)) {
      const enriched = await Promise.all(
        machineryList.map(async m => {
          const avail = await checkAvailability(m.id, startDate, endDate);
          return {
            ...m,
            daily_rate: parseFloat(m.daily_rate),
            operator_charge: parseFloat(m.operator_charge || 0),
            is_available_for_dates: avail.available,
            conflicts: avail.conflicts
          };
        })
      );
      results = enriched;
    } else {
      results = machineryList.map(m => ({
        ...m,
        daily_rate: parseFloat(m.daily_rate),
        operator_charge: parseFloat(m.operator_charge || 0)
      }));
    }

    return res.status(200).json({ success: true, count: results.length, machinery: results });
  } catch (error) {
    next(error);
  }
}

async function getMachineryById(req, res, next) {
  try {
    const { id } = req.params;

    const [rows] = await query(`
      SELECT 
        m.*,
        u.name AS owner_name,
        u.email AS owner_email,
        u.phone AS owner_phone
      FROM machinery m
      JOIN users u ON m.owner_id = u.id
      WHERE m.id = ?
    `, [id]);

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Machinery not found.' });
    }

    const machine = {
      ...rows[0],
      daily_rate: parseFloat(rows[0].daily_rate),
      operator_charge: parseFloat(rows[0].operator_charge || 0)
    };

    // Get upcoming confirmed bookings
    const [bookings] = await query(`
      SELECT id, start_date, end_date, status, total_days, total_amount
      FROM machinery_bookings
      WHERE machinery_id = ? AND status IN ('APPROVED', 'PENDING')
      ORDER BY start_date ASC
    `, [id]);

    // Get maintenance records
    const [maintenance] = await query(`
      SELECT * FROM machinery_maintenance
      WHERE machinery_id = ?
      ORDER BY start_date ASC
    `, [id]);

    return res.status(200).json({
      success: true,
      machinery: machine,
      upcomingBookings: bookings,
      maintenanceRecords: maintenance
    });
  } catch (error) {
    next(error);
  }
}

async function updateMachinery(req, res, next) {
  try {
    const { id } = req.params;
    const {
      machine_name,
      machine_type,
      location,
      daily_rate,
      operator_available,
      operator_charge,
      status,
      description
    } = req.body;

    const [existing] = await query('SELECT * FROM machinery WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Machinery not found.' });
    }

    // Role check: Only owner or admin can update
    if (req.user.role !== 'ADMIN' && existing[0].owner_id !== req.user.id) {
      return res.status(403).json({ success: false, message: 'Unauthorized to update this machinery.' });
    }

    const updateSql = `
      UPDATE machinery SET
        machine_name = COALESCE(?, machine_name),
        machine_type = COALESCE(?, machine_type),
        location = COALESCE(?, location),
        daily_rate = COALESCE(?, daily_rate),
        operator_available = COALESCE(?, operator_available),
        operator_charge = COALESCE(?, operator_charge),
        status = COALESCE(?, status),
        description = COALESCE(?, description)
      WHERE id = ?
    `;

    await query(updateSql, [
      machine_name ? machine_name.trim() : null,
      machine_type ? machine_type.trim() : null,
      location ? location.trim() : null,
      daily_rate !== undefined ? parseFloat(daily_rate) : null,
      operator_available !== undefined ? (operator_available ? 1 : 0) : null,
      operator_charge !== undefined ? parseFloat(operator_charge) : null,
      status || null,
      description !== undefined ? description : null,
      id
    ]);

    return res.status(200).json({ success: true, message: 'Machinery details updated successfully.' });
  } catch (error) {
    next(error);
  }
}

async function deleteMachinery(req, res, next) {
  try {
    const { id } = req.params;
    const [existing] = await query('SELECT * FROM machinery WHERE id = ?', [id]);
    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: 'Machinery not found.' });
    }

    if (req.user.role !== 'ADMIN' && existing[0].owner_id !== req.user.id) {
      return res.status(403).json({ success: false, message: 'Unauthorized to delete this machinery.' });
    }

    await query('DELETE FROM machinery WHERE id = ?', [id]);
    return res.status(200).json({ success: true, message: 'Machinery deleted successfully.' });
  } catch (error) {
    next(error);
  }
}

/**
 * Check date-based availability for a specific machine
 * GET /api/machinery/:id/availability?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
 */
async function checkMachineryAvailability(req, res, next) {
  try {
    const { id } = req.params;
    const { startDate, endDate } = req.query;

    if (!startDate || !endDate) {
      return res.status(400).json({
        success: false,
        message: 'Both startDate and endDate query parameters are required (YYYY-MM-DD).'
      });
    }

    if (!isValidDateRange(startDate, endDate)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid date range: End date cannot be before start date.'
      });
    }

    const availability = await checkAvailability(id, startDate, endDate);
    return res.status(200).json(availability);
  } catch (error) {
    next(error);
  }
}

async function getMaintenanceRecords(req, res, next) {
  try {
    const { id } = req.params;
    const [records] = await query(
      'SELECT * FROM machinery_maintenance WHERE machinery_id = ? ORDER BY start_date DESC',
      [id]
    );
    return res.status(200).json({ success: true, records });
  } catch (error) {
    next(error);
  }
}

async function addMaintenanceRecord(req, res, next) {
  try {
    const { id } = req.params;
    const { start_date, end_date, reason, status = 'SCHEDULED' } = req.body;

    if (!start_date || !end_date || !reason) {
      return res.status(400).json({ success: false, message: 'Start date, end date, and maintenance reason are required.' });
    }

    if (!isValidDateRange(start_date, end_date)) {
      return res.status(400).json({ success: false, message: 'End date cannot be prior to start date.' });
    }

    const [result] = await query(
      `INSERT INTO machinery_maintenance (machinery_id, start_date, end_date, reason, status)
       VALUES (?, ?, ?, ?, ?)`,
      [id, formatDate(start_date), formatDate(end_date), reason.trim(), status]
    );

    return res.status(201).json({
      success: true,
      message: 'Maintenance window scheduled successfully.',
      maintenanceId: result.insertId
    });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  createMachinery,
  getAllMachinery,
  getMachineryById,
  updateMachinery,
  deleteMachinery,
  checkMachineryAvailability,
  getMaintenanceRecords,
  addMaintenanceRecord
};
