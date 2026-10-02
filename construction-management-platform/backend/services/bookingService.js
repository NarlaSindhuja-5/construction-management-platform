/**
 * Machinery Booking Service
 * Implements business logic for availability check, conflict prevention,
 * booking creation, and transactional owner approval.
 */

const { query, getConnection } = require('../config/db');
const { calculateTotalDays, calculateTotalAmount } = require('../utils/calculations');
const { isValidDateRange, formatDate } = require('../utils/dateUtils');

/**
 * Checks if a machine has conflicting confirmed bookings or maintenance windows.
 * @param {number} machineryId
 * @param {string} startDate (YYYY-MM-DD)
 * @param {string} endDate (YYYY-MM-DD)
 * @param {number|null} excludeBookingId
 */
async function checkAvailability(machineryId, startDate, endDate, excludeBookingId = null) {
  const start = formatDate(startDate);
  const end = formatDate(endDate);

  if (!isValidDateRange(start, end)) {
    throw new Error('Invalid date range: End date must be on or after start date.');
  }

  // 1. Check existing APPROVED bookings that overlap:
  // Overlap condition: (start_date <= end AND end_date >= start)
  let bookingConflictSql = `
    SELECT 
      b.id,
      b.start_date,
      b.end_date,
      b.status,
      p.project_name,
      s.site_name
    FROM machinery_bookings b
    LEFT JOIN projects p ON b.project_id = p.id
    LEFT JOIN sites s ON b.site_id = s.id
    WHERE b.machinery_id = ?
      AND b.status = 'APPROVED'
      AND (b.start_date <= ? AND b.end_date >= ?)
  `;
  const bookingParams = [machineryId, end, start];

  if (excludeBookingId) {
    bookingConflictSql += ' AND b.id != ?';
    bookingParams.push(excludeBookingId);
  }

  const [bookingConflicts] = await query(bookingConflictSql, bookingParams);

  // 2. Check scheduled or in-progress maintenance windows
  const maintenanceSql = `
    SELECT 
      id,
      start_date,
      end_date,
      reason,
      status
    FROM machinery_maintenance
    WHERE machinery_id = ?
      AND status != 'COMPLETED'
      AND (start_date <= ? AND end_date >= ?)
  `;
  const [maintenanceConflicts] = await query(maintenanceSql, [machineryId, end, start]);

  const conflicts = [];

  bookingConflicts.forEach(b => {
    conflicts.push({
      type: 'BOOKING',
      id: b.id,
      startDate: formatDate(b.start_date),
      endDate: formatDate(b.end_date),
      status: b.status,
      projectName: b.project_name,
      siteName: b.site_name,
      description: `Confirmed booking for project: ${b.project_name || 'N/A'}`
    });
  });

  maintenanceConflicts.forEach(m => {
    conflicts.push({
      type: 'MAINTENANCE',
      id: m.id,
      startDate: formatDate(m.start_date),
      endDate: formatDate(m.end_date),
      status: m.status,
      description: `Maintenance scheduled: ${m.reason}`
    });
  });

  return {
    available: conflicts.length === 0,
    conflicts
  };
}

/**
 * Creates a new booking request in PENDING state.
 */
async function createBookingRequest({
  machineryId,
  projectId,
  siteId,
  contractorId,
  startDate,
  endDate,
  operatorRequired = true,
  transportCharge = 0,
  additionalCharge = 0
}) {
  const start = formatDate(startDate);
  const end = formatDate(endDate);

  if (!isValidDateRange(start, end)) {
    throw new Error('Please select a valid date range. End date cannot be before start date.');
  }

  // 1. Fetch machinery details
  const [machines] = await query('SELECT * FROM machinery WHERE id = ?', [machineryId]);
  if (!machines || machines.length === 0) {
    throw new Error('Selected machinery not found.');
  }
  const machine = machines[0];
  if (machine.status === 'INACTIVE') {
    throw new Error('This machinery is currently inactive and cannot be booked.');
  }

  // 2. Verify project & site exist
  const [projects] = await query('SELECT id, project_name FROM projects WHERE id = ?', [projectId]);
  if (!projects || projects.length === 0) {
    throw new Error('Specified project does not exist.');
  }

  const [sites] = await query('SELECT id, site_name FROM sites WHERE id = ? AND project_id = ?', [siteId, projectId]);
  if (!sites || sites.length === 0) {
    throw new Error('Specified site does not belong to the selected project.');
  }

  // 3. Check for conflict
  const availability = await checkAvailability(machineryId, start, end);
  if (!availability.available) {
    const conflictMsg = availability.conflicts
      .map(c => `${c.type} from ${c.startDate} to ${c.endDate}`)
      .join(', ');
    throw new Error(`This machinery is already booked or under maintenance during the selected dates (${conflictMsg}).`);
  }

  // 4. Calculations (Never trust calculations sent by client)
  const totalDays = calculateTotalDays(start, end);
  const dailyRate = parseFloat(machine.daily_rate);
  const operatorDailyCharge = operatorRequired ? parseFloat(machine.operator_charge || 0) : 0;
  const operatorTotalCharge = operatorDailyCharge * totalDays;

  const totalAmount = calculateTotalAmount({
    dailyRate,
    totalDays,
    operatorCharge: operatorTotalCharge,
    transportCharge,
    additionalCharge
  });

  // 5. Insert booking
  const insertSql = `
    INSERT INTO machinery_bookings (
      machinery_id,
      project_id,
      site_id,
      contractor_id,
      start_date,
      end_date,
      total_days,
      daily_rate,
      operator_charge,
      transport_charge,
      additional_charge,
      total_amount,
      status,
      requested_at
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', NOW())
  `;

  const [result] = await query(insertSql, [
    machineryId,
    projectId,
    siteId,
    contractorId,
    start,
    end,
    totalDays,
    dailyRate,
    operatorTotalCharge,
    parseFloat(transportCharge || 0),
    parseFloat(additionalCharge || 0),
    totalAmount
  ]);

  return {
    bookingId: result.insertId,
    machineryId,
    projectId,
    siteId,
    startDate: start,
    endDate: end,
    totalDays,
    dailyRate,
    operatorCharge: operatorTotalCharge,
    transportCharge: parseFloat(transportCharge || 0),
    additionalCharge: parseFloat(additionalCharge || 0),
    totalAmount,
    status: 'PENDING'
  };
}

/**
 * Approves a machinery booking using a strict DB Transaction.
 * Checks for concurrent conflicts, sets status to APPROVED, and automatically creates
 * a project expense record.
 */
async function approveBooking(bookingId, userId, userRole) {
  const connection = await getConnection();

  try {
    await connection.beginTransaction();

    // 1. Lock and retrieve booking record
    const [bookings] = await connection.query(
      `SELECT b.*, m.owner_id, m.machine_name, m.registration_number, p.project_name, s.site_name
       FROM machinery_bookings b
       JOIN machinery m ON b.machinery_id = m.id
       JOIN projects p ON b.project_id = p.id
       JOIN sites s ON b.site_id = s.id
       WHERE b.id = ? FOR UPDATE`,
      [bookingId]
    );

    if (!bookings || bookings.length === 0) {
      throw new Error('Booking request not found.');
    }

    const booking = bookings[0];

    // Check authorization: Must be owner of the machine OR ADMIN
    if (userRole !== 'ADMIN' && booking.owner_id !== userId) {
      throw new Error('Unauthorized: Only the machinery owner or administrator can approve this booking.');
    }

    if (booking.status === 'APPROVED') {
      throw new Error('This booking is already approved.');
    }

    if (booking.status !== 'PENDING') {
      throw new Error(`Cannot approve booking with status: ${booking.status}`);
    }

    const start = formatDate(booking.start_date);
    const end = formatDate(booking.end_date);

    // 2. Strict conflict check inside transaction
    const [existingApproved] = await connection.query(
      `SELECT id, start_date, end_date FROM machinery_bookings
       WHERE machinery_id = ?
         AND status = 'APPROVED'
         AND id != ?
         AND (start_date <= ? AND end_date >= ?)
       FOR UPDATE`,
      [booking.machinery_id, booking.id, end, start]
    );

    if (existingApproved.length > 0) {
      throw new Error('Cannot approve booking: A conflicting confirmed booking already exists for these dates.');
    }

    // 3. Maintenance check inside transaction
    const [maintenance] = await connection.query(
      `SELECT id FROM machinery_maintenance
       WHERE machinery_id = ?
         AND status != 'COMPLETED'
         AND (start_date <= ? AND end_date >= ?)
       FOR UPDATE`,
      [booking.machinery_id, end, start]
    );

    if (maintenance.length > 0) {
      throw new Error('Cannot approve booking: Machinery is scheduled for maintenance during these dates.');
    }

    // 4. Update booking to APPROVED
    await connection.query(
      `UPDATE machinery_bookings 
       SET status = 'APPROVED', approved_at = NOW(), rejection_reason = NULL
       WHERE id = ?`,
      [booking.id]
    );

    // 5. Create automatic expense record under category 'MACHINERY'
    const expenseDescription = `Machinery Rental: ${booking.machine_name} (${booking.registration_number}) - ${booking.total_days} Days`;
    await connection.query(
      `INSERT INTO expenses (
        project_id,
        site_id,
        category,
        amount,
        expense_date,
        description,
        reference_type,
        reference_id,
        created_by,
        created_at
       ) VALUES (?, ?, 'MACHINERY', ?, ?, ?, 'MACHINERY_BOOKING', ?, ?, NOW())`,
      [
        booking.project_id,
        booking.site_id,
        booking.total_amount,
        start, // Expense dated at start of rental
        expenseDescription,
        booking.id,
        booking.contractor_id
      ]
    );

    // 6. Commit transaction
    await connection.commit();

    return {
      success: true,
      bookingId: booking.id,
      status: 'APPROVED',
      approvedAt: new Date().toISOString(),
      machineName: booking.machine_name,
      totalAmount: booking.total_amount
    };
  } catch (err) {
    await connection.rollback();
    throw err;
  } finally {
    connection.release();
  }
}

/**
 * Rejects a machinery booking request with mandatory reason.
 */
async function rejectBooking(bookingId, userId, userRole, rejectionReason) {
  if (!rejectionReason || !rejectionReason.trim()) {
    throw new Error('Please provide a reason for rejecting the booking request.');
  }

  const [bookings] = await query(
    `SELECT b.*, m.owner_id FROM machinery_bookings b
     JOIN machinery m ON b.machinery_id = m.id
     WHERE b.id = ?`,
    [bookingId]
  );

  if (!bookings || bookings.length === 0) {
    throw new Error('Booking request not found.');
  }

  const booking = bookings[0];
  if (userRole !== 'ADMIN' && booking.owner_id !== userId) {
    throw new Error('Unauthorized: Only the machinery owner or administrator can reject this booking.');
  }

  if (booking.status !== 'PENDING') {
    throw new Error(`Cannot reject booking with current status: ${booking.status}`);
  }

  await query(
    `UPDATE machinery_bookings 
     SET status = 'REJECTED', rejected_at = NOW(), rejection_reason = ?
     WHERE id = ?`,
    [rejectionReason.trim(), bookingId]
  );

  return {
    bookingId,
    status: 'REJECTED',
    rejectionReason: rejectionReason.trim()
  };
}

/**
 * Cancels a booking (by contractor who created it, or admin)
 */
async function cancelBooking(bookingId, userId, userRole) {
  const connection = await getConnection();
  try {
    await connection.beginTransaction();

    const [bookings] = await connection.query(
      'SELECT * FROM machinery_bookings WHERE id = ? FOR UPDATE',
      [bookingId]
    );

    if (!bookings || bookings.length === 0) {
      throw new Error('Booking not found.');
    }

    const booking = bookings[0];
    if (userRole !== 'ADMIN' && booking.contractor_id !== userId) {
      throw new Error('Unauthorized: Only the contractor who requested the booking or an administrator can cancel it.');
    }

    if (['COMPLETED', 'CANCELLED'].includes(booking.status)) {
      throw new Error(`Cannot cancel a booking that is already ${booking.status}.`);
    }

    // If was APPROVED, delete or cancel corresponding expense
    if (booking.status === 'APPROVED') {
      await connection.query(
        "DELETE FROM expenses WHERE reference_type = 'MACHINERY_BOOKING' AND reference_id = ?",
        [booking.id]
      );
    }

    await connection.query(
      "UPDATE machinery_bookings SET status = 'CANCELLED' WHERE id = ?",
      [booking.id]
    );

    await connection.commit();
    return { bookingId, status: 'CANCELLED' };
  } catch (err) {
    await connection.rollback();
    throw err;
  } finally {
    connection.release();
  }
}

module.exports = {
  checkAvailability,
  createBookingRequest,
  approveBooking,
  rejectBooking,
  cancelBooking
};
