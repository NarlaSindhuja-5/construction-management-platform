-- =============================================================================
-- End-to-End Construction Management & Resource Booking Platform
-- Seed Data: Sample Projects, Sites, Machinery, Users, Tasks, Materials
-- Passwords:
--   admin@buildpro.com         -> Admin@123
--   contractor@buildpro.com    -> Contractor@123
--   owner@buildpro.com         -> Owner@123
--   sitemanager@buildpro.com   -> SiteManager@123
-- =============================================================================

USE `construction_management_db`;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `machine_usage`;
TRUNCATE TABLE `expenses`;
TRUNCATE TABLE `issues`;
TRUNCATE TABLE `daily_progress`;
TRUNCATE TABLE `tasks`;
TRUNCATE TABLE `materials`;
TRUNCATE TABLE `workers`;
TRUNCATE TABLE `machinery_maintenance`;
TRUNCATE TABLE `machinery_bookings`;
TRUNCATE TABLE `machinery`;
TRUNCATE TABLE `sites`;
TRUNCATE TABLE `projects`;
TRUNCATE TABLE `users`;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Insert Users
INSERT INTO `users` (`id`, `name`, `email`, `password_hash`, `phone`, `role`, `status`) VALUES
(1, 'Admin User', 'admin@buildpro.com', '$2a$10$s2fO87GKextjZ.F6ZAPkju4VcrcwHmQ3Ygomrjr622/MWpwI/Aj8i', '+91 9876543201', 'ADMIN', 'ACTIVE'),
(2, 'Vikram Mehta (Contractor)', 'contractor@buildpro.com', '$2a$10$BOc9.Cpd6qWHpGEVc84s5.GmHYIb2ByO4QkZwKUMT2caA2zRYDMEK', '+91 9876543202', 'CONTRACTOR', 'ACTIVE'),
(3, 'Rajesh Patel (Heavy Equipments)', 'owner@buildpro.com', '$2a$10$GF129bR9i25hEIDTtuRhge1nf3xsX9sCRbdlf5SPG2F..e.fatii6', '+91 9876543203', 'OWNER', 'ACTIVE'),
(4, 'Kiran Reddy (Site Engineer)', 'sitemanager@buildpro.com', '$2a$10$V7lwUWBDFVrofWVuXLLBceSDHZwOluwjFwweUlXRpa6wE/Yryc0kG', '+91 9876543204', 'SITE_MANAGER', 'ACTIVE');

-- 2. Insert Projects
INSERT INTO `projects` (`id`, `project_name`, `client_name`, `project_type`, `location`, `description`, `start_date`, `end_date`, `budget`, `status`, `created_by`) VALUES
(1, 'Hyderabad Road Development Project', 'GHMC Infrastructure Corp', 'Highway / Roadway', 'Gachibowli, Hyderabad', 'Construction of 6-lane bypass corridor including drainage culverts and slip roads.', '2026-09-01', '2027-04-30', 12500000.00, 'ACTIVE', 2),
(2, 'Hitec Commercial Towers', 'Nexus Tech Spaces Ltd', 'Commercial Building', 'Madhapur, Hyderabad', 'Modern 15-storey IT commercial park with double-level basement parking.', '2026-08-15', '2027-11-30', 45000000.00, 'ACTIVE', 2),
(3, 'Solar Microgrid Power Substation', 'Southern Power Discom', 'Industrial Energy', 'Shamshabad, Hyderabad', 'Civil foundational works and high-voltage transmission substation equipment housing.', '2026-10-01', '2027-03-15', 8500000.00, 'PLANNED', 2);

-- 3. Insert Sites
INSERT INTO `sites` (`id`, `project_id`, `site_name`, `location`, `address`, `site_manager_id`, `status`) VALUES
(1, 1, 'Gachibowli Flyover & Approach Road', 'Hyderabad', 'Outer Ring Road Junction, Gachibowli, Hyderabad, 500032', 4, 'ACTIVE'),
(2, 1, 'Madhapur Corridor Sector-3', 'Hyderabad', 'Plot 45, Hitec City Main Road, Hyderabad, 500081', 4, 'ACTIVE'),
(3, 2, 'Tower-A Deep Foundation & Basement', 'Hyderabad', 'Financial District, Nanakramguda, Hyderabad, 500032', 4, 'ACTIVE');

-- 4. Insert Machinery
INSERT INTO `machinery` (`id`, `owner_id`, `machine_name`, `machine_type`, `registration_number`, `location`, `daily_rate`, `operator_available`, `operator_charge`, `status`, `description`) VALUES
(1, 3, 'JCB 3DX Super EcoXcellence', 'JCB', 'TS-09-EA-4412', 'Hyderabad', 5000.00, 1, 800.00, 'AVAILABLE', 'Heavy duty backhoe loader with 4x4 drive, fuel efficient eco-mode, and ditch cleaning bucket.'),
(2, 3, 'Tata Hitachi EX200 LC Excavator', 'Excavator', 'TS-08-AB-8921', 'Hyderabad', 12000.00, 1, 1200.00, 'AVAILABLE', '20-ton hydraulic excavator for heavy earthwork, trenching, and foundation excavation.'),
(3, 3, 'L&T 1190 Soil Compactor Road Roller', 'Roller', 'TS-07-ZZ-3301', 'Hyderabad', 4500.00, 1, 700.00, 'AVAILABLE', '11-ton vibratory soil compactor suitable for granular sub-base and asphalt compaction.'),
(4, 3, 'Ashok Leyland 2518 Heavy Tipper (16 CuM)', 'Tipper', 'TS-10-TP-5544', 'Hyderabad', 4000.00, 1, 600.00, 'AVAILABLE', 'Twin-axle high capacity tipper truck for aggregate and debris hauling.'),
(5, 3, 'Schwing Stetter Transit Concrete Mixer (6 CuM)', 'Concrete Mixer', 'TS-11-CM-1090', 'Hyderabad', 7500.00, 1, 900.00, 'AVAILABLE', 'Self-loading high efficiency concrete mixer with GPS telemetry.'),
(6, 3, 'ACE 14X Hydraulic Mobile Crane', 'Crane', 'TS-09-CR-8822', 'Hyderabad', 15000.00, 1, 1500.00, 'AVAILABLE', '14-ton mobile pick and carry crane for precast beam lifting and girder placement.'),
(7, 3, 'CAT 950GC Heavy Wheel Loader', 'Loader', 'TS-08-LD-7711', 'Hyderabad', 9000.00, 1, 1000.00, 'AVAILABLE', '3.1 cubic meter bucket capacity wheel loader for aggregate stockpile loading.'),
(8, 3, 'Komatsu D85EX Crawler Bulldozer', 'Bulldozer', 'TS-07-BD-6633', 'Hyderabad', 18000.00, 1, 1800.00, 'AVAILABLE', 'Heavy crawler dozer for site clearing, land levelling, and rough grading.');

-- 5. Insert Sample Confirmed Booking (Tata Hitachi Excavator on Site 1)
-- Demonstrating confirmed reservation from 2026-09-10 to 2026-09-20
INSERT INTO `machinery_bookings` (`id`, `machinery_id`, `project_id`, `site_id`, `contractor_id`, `start_date`, `end_date`, `total_days`, `daily_rate`, `operator_charge`, `transport_charge`, `additional_charge`, `total_amount`, `status`, `requested_at`, `approved_at`) VALUES
(1, 2, 1, 1, 2, '2026-09-10', '2026-09-20', 11, 12000.00, 1200.00, 4000.00, 0.00, 137200.00, 'APPROVED', '2026-09-05 10:00:00', '2026-09-06 11:30:00');

-- 6. Insert Machinery Maintenance (Road Roller scheduled maintenance)
INSERT INTO `machinery_maintenance` (`id`, `machinery_id`, `start_date`, `end_date`, `reason`, `status`) VALUES
(1, 3, '2026-11-01', '2026-11-05', 'Scheduled 500-hour hydraulic oil change and drum vibration damper replacement', 'SCHEDULED');

-- 7. Insert Workers
INSERT INTO `workers` (`id`, `name`, `phone`, `skill`, `experience`, `daily_wage`, `availability`, `status`, `site_id`) VALUES
(1, 'Suresh Kumar', '+91 9876543210', 'Operator', 7, 900.00, 'ASSIGNED', 'ACTIVE', 1),
(2, 'Ramesh Rao', '+91 9876543211', 'Mason', 10, 850.00, 'ASSIGNED', 'ACTIVE', 1),
(3, 'Venkatesh V', '+91 9876543212', 'Electrician', 5, 800.00, 'ASSIGNED', 'ACTIVE', 1),
(4, 'Mohammad Ali', '+91 9876543213', 'Supervisor', 12, 1200.00, 'ASSIGNED', 'ACTIVE', 1),
(5, 'Raju Nayak', '+91 9876543214', 'Labour', 3, 600.00, 'ASSIGNED', 'ACTIVE', 1),
(6, 'Shiva Krishna', '+91 9876543215', 'Carpenter', 6, 800.00, 'AVAILABLE', 'ACTIVE', NULL),
(7, 'Anil Verma', '+91 9876543216', 'Plumber', 4, 750.00, 'AVAILABLE', 'ACTIVE', NULL);

-- 8. Insert Materials
INSERT INTO `materials` (`id`, `site_id`, `material_name`, `category`, `quantity`, `minimum_quantity`, `unit`, `unit_price`, `supplier`) VALUES
(1, 1, 'UltraTech 53 Grade OPC Cement', 'Cement', 850.00, 200.00, 'Bags', 380.00, 'UltraTech Cement Distributors'),
(2, 1, 'TMT Fe 550D High Tensile Steel', 'Steel', 45.00, 10.00, 'Tons', 62000.00, 'Tata Tiscon Steel Depot'),
(3, 1, 'River Sand (M-Sand Fine Aggregate)', 'Sand', 180.00, 50.00, 'Tons', 1800.00, 'Telangana River Sands Corp'),
(4, 1, 'Crushed Blue Metal Aggregate 20mm', 'Aggregate', 240.00, 60.00, 'Tons', 1200.00, 'Deccan Stone Quarries'),
(5, 1, 'VG-30 Highway Grade Bitumen', 'Bitumen', 15.00, 30.00, 'Drums', 14500.00, 'HPCL Infra Supply'); -- Note: Below minimum quantity (15 < 30) for alert demonstration!

-- 9. Insert Tasks
INSERT INTO `tasks` (`id`, `project_id`, `site_id`, `task_name`, `description`, `assigned_worker_id`, `start_date`, `due_date`, `priority`, `status`, `progress_percent`) VALUES
(1, 1, 1, 'Site Clearing & Topsoil Stripping', 'Clearing vegetation, debris and removing 150mm topsoil along chainage 0+000 to 2+500.', 4, '2026-09-02', '2026-09-15', 'HIGH', 'COMPLETED', 100),
(2, 1, 1, 'Sub-grade Earthwork & Trench Excavation', 'Excavation of trenching for drainage lines and embankment cutting.', 1, '2026-09-12', '2026-10-05', 'HIGH', 'IN_PROGRESS', 75),
(3, 1, 1, 'Reinforced Concrete Storm Drainage Laying', 'Fabrication of RCC box culverts and casting drainage covers.', 2, '2026-09-20', '2026-10-25', 'MEDIUM', 'IN_PROGRESS', 40),
(4, 1, 1, 'Granular Sub-Base (GSB) Layer Compaction', 'Spreading crushed stone aggregate and rolling with vibratory soil compactor.', 1, '2026-10-10', '2026-11-15', 'CRITICAL', 'NOT_STARTED', 0),
(5, 1, 1, 'Dense Bituminous Macadam (DBM) Road Laying', 'Hot asphalt batch plant supply and mechanical sensor paver finishing.', 4, '2026-11-16', '2026-12-30', 'CRITICAL', 'NOT_STARTED', 0);

-- 10. Insert Daily Progress Records
INSERT INTO `daily_progress` (`id`, `project_id`, `site_id`, `task_id`, `progress_date`, `workers_present`, `work_completed`, `progress_percent`, `issues`, `remarks`, `created_by`) VALUES
(1, 1, 1, 1, '2026-09-10', 18, 'Completed chainage 1+200 to 2+000 clearing and leveling.', 85, 'Minor boulder obstruction cleared using rock breaker.', 'Work progressing ahead of baseline schedule.', 4),
(2, 1, 1, 1, '2026-09-14', 22, 'Final inspection of site clearing completed and approved by GHMC surveyor.', 100, 'None.', 'Ready for subsequent sub-grade excavation.', 4),
(3, 1, 1, 2, '2026-09-25', 16, 'Excavated 450 meters drainage trench using Tata Hitachi excavator.', 60, 'Heavy localized rain delayed morning shift by 2 hours.', 'Overtime worked to recover lost hours.', 4);

-- 11. Insert Machine Usage
INSERT INTO `machine_usage` (`id`, `booking_id`, `usage_date`, `hours_used`, `fuel_cost`, `operator_present`, `work_description`, `remarks`) VALUES
(1, 1, '2026-09-12', 8.50, 4200.00, 1, 'Trench excavation chainage 0+400 to 0+650.', 'Machine operated at optimum RPM with zero downtime.'),
(2, 1, '2026-09-13', 7.00, 3500.00, 1, 'Hard rock chipping and bucket loading into haul tipper.', 'Routine evening grease lubrication performed.');

-- 12. Insert Expenses
INSERT INTO `expenses` (`id`, `project_id`, `site_id`, `category`, `amount`, `expense_date`, `description`, `reference_type`, `reference_id`, `created_by`) VALUES
(1, 1, 1, 'MACHINERY', 137200.00, '2026-09-06', 'Machinery Rental: Tata Hitachi EX200 LC Excavator (11 days)', 'MACHINERY_BOOKING', 1, 2),
(2, 1, 1, 'MATERIAL', 323000.00, '2026-09-05', 'Advance payment for 850 bags UltraTech 53 Grade Cement', 'MATERIAL_PURCHASE', 1, 2),
(3, 1, 1, 'MATERIAL', 2790000.00, '2026-09-07', 'Supply of 45 tons TMT Fe 550D rebar steel bundle', 'MATERIAL_PURCHASE', 2, 2),
(4, 1, 1, 'LABOUR', 185000.00, '2026-09-15', 'Bi-weekly skilled worker and labour muster roll settlement', 'MANUAL', NULL, 2),
(5, 1, 1, 'FUEL', 48500.00, '2026-09-20', 'Diesel bulk supply 550 liters for on-site machinery and DG sets', 'MANUAL', NULL, 2);

-- 13. Insert Issues
INSERT INTO `issues` (`id`, `project_id`, `site_id`, `machinery_id`, `issue_type`, `description`, `priority`, `status`, `reported_by`, `resolved_at`) VALUES
(1, 1, 1, 2, 'Breakdown', 'Minor hydraulic hose seal leak detected on secondary arm cylinder of excavator.', 'MEDIUM', 'RESOLVED', 4, '2026-09-14 16:30:00'),
(2, 1, 1, NULL, 'Material Shortage', 'VG-30 Bitumen stock is down to 15 drums against minimum required 30 drums. Reorder needed before road laying begins.', 'HIGH', 'OPEN', 4, NULL);
