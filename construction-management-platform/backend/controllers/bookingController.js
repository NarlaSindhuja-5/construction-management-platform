/**
 * Machinery Booking Controller
 * Manages booking requests, status workflows (Approval/Rejection/Cancellation),
 * and on-site machine usage tracking.
 */

const { query } = require('../config/db');
const {
  createBookingRequest,
  approveBooking,
  rejectBooking,
  cancelBooking
} = require('../services/bookingService');

async function createBooking(req, res, next) {
  try {
    const {
      machinery_id,
      project_id,
      site_id,
      start_date,
      end_date,
      operator_required = true,
      transport_charge = 0,
      additional_charge = 0
    } = req.body;

    if (!machinery_id || !project_id || !site_id) {
      return res.status(400).json({
        success: false,
        message: 'Machinery, Project, and Site are required for booking.'
      });
    }

    if (!start_date || !end_date) {
      return res.status(400).json({
        success: false,
        message: 'Start date and End date are required.'
      });
    }

    const bookingResult = await createBookingRequest({
      machineryId: machinery_id,
      projectId: project_id,
      siteId: site_id,
      contractorId: req.user.id,
      startDate: start_date,
      endDate: end_date,
      operatorRequired: operator_required,
      transportCharge: transport_charge,
      additionalCharge: additional_charge
    });

    return res.status(201).json({
      success: true,
      message: 'Booking request submitted successfully. Awaiting machinery owner approval.',
      booking: bookingResult
    });
  } catch (error) {
    return res.status(400).json({
      success: false,
      message: error.message || 'Unable to submit booking request.'
    });
  }
}

async function getAllBookings(req, res, next) {
  try {
    const { status, project_id, site_id, machinery_id } = req.query;

    let sql = `
      SELECT 
        b.*,
        m.machine_name,
        m.machine_type,
        m.registration_number,
        m.location AS machine_location,
        m.owner_id,
        u_owner.name AS owner_name,
        u_owner.email AS owner_email,
        u_owner.phone AS owner_phone,
        p.project_name,
        s.site_name,
        u_con.name AS contractor_name,
        u_con.email AS contractor_email,
        u_con.phone AS contractor_phone
      FROM machinery_bookings b
      JOIN machinery m ON b.machinery_id = m.id
      JOIN users u_owner ON m.owner_id = u_owner.id
      JOIN projects p ON b.project_id = p.id
      JOIN sites s ON b.site_id = s.id
      JOIN users u_con ON b.contractor_id = u_con.id
      WHERE 1=1
    `;
    const params = [];

    // Role-based visibility
    if (req.user.role === 'OWNER') {
      sql += ' AND m.owner_id = ?';
      params.push(req.user.id);
    } else if (req.user.role === 'CONTRACTOR') {
      sql += ' AND b.contractor_id = ?';
      params.push(req.user.id);
    } else if (req.user.role === 'SITE_MANAGER') {
      sql += ' AND s.site_manager_id = ?';
      params.push(req.user.id);
    }

    if (status) {
      sql += ' AND b.status = ?';
      params.push(status);
    }
    if (project_id) {
      sql += ' AND b.project_id = ?';
      params.push(project_id);
    }
    if (site_id) {
      sql += ' AND b.site_id = ?';
      params.push(site_id);
    }
    if (machinery_id) {
      sql += ' AND b.machinery_id = ?';
      params.push(machinery_id);
    }

    sql += ' ORDER BY b.requested_at DESC, b.id DESC';

    const [bookings] = await query(sql, params);

    const formatted = bookings.map(b => ({
      ...b,
      daily_rate: parseFloat(b.daily_rate),
      operator_charge: parseFloat(b.operator_charge || 0),
      transport_charge: parseFloat(b.transport_charge || 0),
      additional_charge: parseFloat(b.additional_charge || 0),
      total_amount: parseFloat(b.total_amount)
    }));

    return res.status(200).json({ success: true, count: formatted.length, bookings: formatted });
  } catch (error) {
    next(error);
  }
}

async function getBookingById(req, res, next) {
  try {
    const { id } = req.params;

    const sql = `
      SELECT 
        b.*,
        m.machine_name,
        m.machine_type,
        m.registration_number,
        m.location AS machine_location,
        m.owner_id,
        u_owner.name AS owner_name,
        u_owner.email AS owner_email,
        u_owner.phone AS owner_phone,
        p.project_name,
        s.site_name,
        u_con.name AS contractor_name,
        u_con.email AS contractor_email,
        u_con.phone AS contractor_phone
      FROM machinery_bookings b
      JOIN machinery m ON b.machinery_id = m.id
      JOIN users u_owner ON m.owner_id = u_owner.id
      JOIN projects p ON b.project_id = p.id
      JOIN sites s ON b.site_id = s.id
      JOIN users u_con ON b.contractor_id = u_con.id
      WHERE b.id = ?
    `;

    const [rows] = await query(sql, [id]);
    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Booking not found.' });
    }

    const booking = {
      ...rows[0],
      daily_rate: parseFloat(rows[0].daily_rate),
      operator_charge: parseFloat(rows[0].operator_charge || 0),
      transport_charge: parseFloat(rows[0].transport_charge || 0),
      additional_charge: parseFloat(rows[0].additional_charge || 0),
      total_amount: parseFloat(rows[0].total_amount)
    };

    // Retrieve machine usage logs
    const [usageLogs] = await query(
      'SELECT * FROM machine_usage WHERE booking_id = ? ORDER BY usage_date DESC',
      [id]
    );

    return res.status(200).json({ success: true, booking, usageLogs });
  } catch (error) {
    next(error);
  }
}

async function handleApproveBooking(req, res, next) {
  try {
    const { id } = req.params;
    const result = await approveBooking(id, req.user.id, req.user.role);
    return res.status(200).json({
      success: true,
      message: 'Booking approved successfully. Machine assigned to project and rental expense recorded.',
      result
    });
  } catch (error) {
    return res.status(400).json({
      success: false,
      message: error.message || 'Unable to approve booking.'
    });
  }
}

async function handleRejectBooking(req, res, next) {
  try {
    const { id } = req.params;
    const { rejection_reason } = req.body;

    if (!rejection_reason || !rejection_reason.trim()) {
      return res.status(400).json({
        success: false,
        message: 'Rejection reason is required.'
      });
    }

    const result = await rejectBooking(id, req.user.id, req.user.role, rejection_reason);
    return res.status(200).json({
      success: true,
      message: 'Booking request rejected.',
      result
    });
  } catch (error) {
    return res.status(400).json({
      success: false,
      message: error.message || 'Unable to reject booking.'
    });
  }
}

async function handleCancelBooking(req, res, next) {
  try {
    const { id } = req.params;
    const result = await cancelBooking(id, req.user.id, req.user.role);
    return res.status(200).json({
      success: true,
      message: 'Booking cancelled successfully.',
      result
    });
  } catch (error) {
    return res.status(400).json({
      success: false,
      message: error.message || 'Unable to cancel booking.'
    });
  }
}

async function recordMachineUsage(req, res, next) {
  try {
    const { id } = req.params; // booking_id
    const { usage_date, hours_used, fuel_cost = 0, operator_present = true, work_description, remarks } = req.body;

    if (!usage_date || !hours_used) {
      return res.status(400).json({
        success: false,
        message: 'Usage date and hours used are required.'
      });
    }

    // Verify booking is approved
    const [bookings] = await query('SELECT status FROM machinery_bookings WHERE id = ?', [id]);
    if (bookings.length === 0) {
      return res.status(404).json({ success: false, message: 'Booking not found.' });
    }
    if (bookings[0].status !== 'APPROVED') {
      return res.status(400).json({ success: false, message: 'Usage can only be recorded for approved bookings.' });
    }

    const [result] = await query(
      `INSERT INTO machine_usage (
        booking_id, usage_date, hours_used, fuel_cost, operator_present, work_description, remarks
       ) VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [
        id,
        usage_date,
        parseFloat(hours_used),
        parseFloat(fuel_cost || 0),
        operator_present ? 1 : 0,
        work_description ? work_description.trim() : null,
        remarks ? remarks.trim() : null
      ]
    );

    return res.status(201).json({
      success: true,
      message: 'Machine usage log recorded successfully.',
      usageId: result.insertId
    });
  } catch (error) {
    next(error);
  }
}

async function getMachineUsage(req, res, next) {
  try {
    const { id } = req.params;
    const [rows] = await query(
      'SELECT * FROM machine_usage WHERE booking_id = ? ORDER BY usage_date DESC',
      [id]
    );
    return res.status(200).json({ success: true, usageLogs: rows });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  createBooking,
  getAllBookings,
  getBookingById,
  approveBooking: handleApproveBooking,
  rejectBooking: handleRejectBooking,
  cancelBooking: handleCancelBooking,
  recordMachineUsage,
  getMachineUsage
};
