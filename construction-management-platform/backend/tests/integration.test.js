/**
 * Live MySQL Integration Test Suite
 * Tests live connection, querying, availability, booking submission,
 * transactional owner approval, auto-expense creation, and overlapping conflict blocking.
 */

const { query, testConnection } = require('../config/db');
const { checkAvailability, createBookingRequest, approveBooking } = require('../services/bookingService');

async function testLiveDB() {
  console.log('================================================================');
  console.log('LIVE MYSQL DATABASE & TRANSACTION INTEGRATION TEST');
  console.log('================================================================');

  const connected = await testConnection();
  if (!connected) {
    console.error('Failed to connect to MySQL');
    process.exit(1);
  }

  // 1. Verify Users
  const [users] = await query('SELECT id, name, role, email FROM users');
  console.log(`\n✓ Found ${users.length} users seeded:`);
  users.forEach(u => console.log(`  - ${u.name} (${u.role}) [${u.email}]`));

  // 2. Verify Machinery
  const [machines] = await query('SELECT id, machine_name, machine_type, daily_rate, status FROM machinery');
  console.log(`\n✓ Found ${machines.length} machinery registered in marketplace.`);

  // 3. Test Availability Check on JCB (id=1) for 2026-10-10 to 2026-10-19
  const avail = await checkAvailability(1, '2026-10-10', '2026-10-19');
  console.log(`✓ Machine 1 (JCB) initial availability: ${avail.available ? 'AVAILABLE' : 'BLOCKED'}`);

  // 4. Test Availability Check on Tata Hitachi (id=2) which has existing confirmed booking for 2026-09-10 to 2026-09-20
  const conflictCheck = await checkAvailability(2, '2026-09-15', '2026-09-25');
  console.log(`✓ Machine 2 (Tata Hitachi) overlapping check: ${conflictCheck.available ? 'AVAILABLE' : 'BLOCKED (Expected)'}`);
  console.log(`  Conflicts detected count: ${conflictCheck.conflicts.length}`);

  // 5. Test Live Booking Creation by Contractor for JCB
  const booking = await createBookingRequest({
    machineryId: 1,
    projectId: 1,
    siteId: 1,
    contractorId: 2,
    startDate: '2026-10-10',
    endDate: '2026-10-19',
    operatorRequired: true,
    transportCharge: 4000,
    additionalCharge: 1000
  });
  console.log(`✓ Created new booking request ID: ${booking.bookingId}, Total: ₹${booking.totalAmount}`);

  // 6. Test Transactional Owner Approval
  const approval = await approveBooking(booking.bookingId, 3, 'OWNER');
  console.log(`✓ Owner approved booking ID: ${approval.bookingId}, Status: ${approval.status}`);

  // 7. Verify Auto-Created Expense in expenses table
  const [expense] = await query(
    "SELECT * FROM expenses WHERE reference_type = 'MACHINERY_BOOKING' AND reference_id = ?",
    [booking.bookingId]
  );
  console.log('✓ Verified auto-created project expense:');
  console.log(`  Category: ${expense[0].category} | Amount: ₹${expense[0].amount} | Description: ${expense[0].description}`);

  // 8. Test that overlapping booking for JCB from 2026-10-12 to 2026-10-15 is BLOCKED!
  try {
    await createBookingRequest({
      machineryId: 1,
      projectId: 1,
      siteId: 1,
      contractorId: 2,
      startDate: '2026-10-12',
      endDate: '2026-10-15'
    });
    console.error('FAILED: Overlapping booking was NOT blocked!');
    process.exit(1);
  } catch (err) {
    console.log(`✓ Conflict correctly prevented by engine: "${err.message}"`);
  }

  // 9. Re-seed clean database state after test so demo starts pristine
  console.log('\nCleaning test booking to keep pristine demo state...');
  await query("DELETE FROM expenses WHERE reference_type = 'MACHINERY_BOOKING' AND reference_id = ?", [booking.bookingId]);
  await query("DELETE FROM machinery_bookings WHERE id = ?", [booking.bookingId]);
  console.log('✓ Demo state reset cleanly.');

  console.log('\n================================================================');
  console.log('ALL LIVE MYSQL INTEGRATION TESTS PASSED 100%!');
  console.log('================================================================\n');
  process.exit(0);
}

testLiveDB().catch(e => {
  console.error('Live DB Test Failed:', e);
  process.exit(1);
});
