-- =============================================================================
-- End-to-End Construction Management & Resource Booking Platform
-- Database Schema: construction_management_db
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `construction_management_db`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE `construction_management_db`;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `role` ENUM('ADMIN', 'CONTRACTOR', 'OWNER', 'SITE_MANAGER') NOT NULL,
  `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_users_role` (`role`),
  INDEX `idx_users_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Projects Table
CREATE TABLE IF NOT EXISTS `projects` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `project_name` VARCHAR(200) NOT NULL,
  `client_name` VARCHAR(150) NOT NULL,
  `project_type` VARCHAR(100) NOT NULL,
  `location` VARCHAR(200) NOT NULL,
  `description` TEXT DEFAULT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `budget` DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
  `status` ENUM('PLANNED', 'ACTIVE', 'COMPLETED', 'ON_HOLD', 'CANCELLED') DEFAULT 'PLANNED',
  `created_by` INT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX `idx_projects_status` (`status`),
  INDEX `idx_projects_created_by` (`created_by`),
  INDEX `idx_projects_dates` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Sites Table
CREATE TABLE IF NOT EXISTS `sites` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `project_id` INT NOT NULL,
  `site_name` VARCHAR(200) NOT NULL,
  `location` VARCHAR(200) NOT NULL,
  `address` TEXT DEFAULT NULL,
  `site_manager_id` INT DEFAULT NULL,
  `status` ENUM('ACTIVE', 'COMPLETED', 'INACTIVE') DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`site_manager_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX `idx_sites_project` (`project_id`),
  INDEX `idx_sites_manager` (`site_manager_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Machinery Table
CREATE TABLE IF NOT EXISTS `machinery` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `owner_id` INT NOT NULL,
  `machine_name` VARCHAR(150) NOT NULL,
  `machine_type` VARCHAR(100) NOT NULL,
  `registration_number` VARCHAR(50) NOT NULL UNIQUE,
  `location` VARCHAR(150) NOT NULL,
  `daily_rate` DECIMAL(12, 2) NOT NULL,
  `operator_available` BOOLEAN DEFAULT TRUE,
  `operator_charge` DECIMAL(12, 2) DEFAULT 0.00,
  `status` ENUM('AVAILABLE', 'BOOKED', 'MAINTENANCE', 'INACTIVE') DEFAULT 'AVAILABLE',
  `description` TEXT DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX `idx_machinery_type` (`machine_type`),
  INDEX `idx_machinery_status` (`status`),
  INDEX `idx_machinery_location` (`location`),
  INDEX `idx_machinery_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Machinery Bookings Table
CREATE TABLE IF NOT EXISTS `machinery_bookings` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `machinery_id` INT NOT NULL,
  `project_id` INT NOT NULL,
  `site_id` INT NOT NULL,
  `contractor_id` INT NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `total_days` INT NOT NULL,
  `daily_rate` DECIMAL(12, 2) NOT NULL,
  `operator_charge` DECIMAL(12, 2) DEFAULT 0.00,
  `transport_charge` DECIMAL(12, 2) DEFAULT 0.00,
  `additional_charge` DECIMAL(12, 2) DEFAULT 0.00,
  `total_amount` DECIMAL(15, 2) NOT NULL,
  `status` ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
  `requested_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `approved_at` TIMESTAMP NULL DEFAULT NULL,
  `rejected_at` TIMESTAMP NULL DEFAULT NULL,
  `rejection_reason` TEXT DEFAULT NULL,
  FOREIGN KEY (`machinery_id`) REFERENCES `machinery` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`site_id`) REFERENCES `sites` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`contractor_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX `idx_bookings_machine_dates` (`machinery_id`, `status`, `start_date`, `end_date`),
  INDEX `idx_bookings_project` (`project_id`),
  INDEX `idx_bookings_contractor` (`contractor_id`),
  INDEX `idx_bookings_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Machinery Maintenance Table
CREATE TABLE IF NOT EXISTS `machinery_maintenance` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `machinery_id` INT NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `reason` TEXT NOT NULL,
  `status` ENUM('SCHEDULED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'SCHEDULED',
  FOREIGN KEY (`machinery_id`) REFERENCES `machinery` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX `idx_maintenance_machine_dates` (`machinery_id`, `status`, `start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Workers Table
CREATE TABLE IF NOT EXISTS `workers` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `skill` VARCHAR(100) NOT NULL,
  `experience` INT DEFAULT 0,
  `daily_wage` DECIMAL(10, 2) NOT NULL,
  `availability` ENUM('AVAILABLE', 'ASSIGNED', 'ON_LEAVE') DEFAULT 'AVAILABLE',
  `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
  `site_id` INT DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`site_id`) REFERENCES `sites` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX `idx_workers_site` (`site_id`),
  INDEX `idx_workers_skill` (`skill`),
  INDEX `idx_workers_availability` (`availability`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Materials Table
CREATE TABLE IF NOT EXISTS `materials` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `site_id` INT NOT NULL,
  `material_name` VARCHAR(100) NOT NULL,
  `category` VARCHAR(100) NOT NULL,
  `quantity` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  `minimum_quantity` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  `unit` VARCHAR(50) NOT NULL,
  `unit_price` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  `supplier` VARCHAR(150) DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`site_id`) REFERENCES `sites` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX `idx_materials_site` (`site_id`),
  INDEX `idx_materials_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Tasks Table
CREATE TABLE IF NOT EXISTS `tasks` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `project_id` INT NOT NULL,
  `site_id` INT NOT NULL,
  `task_name` VARCHAR(200) NOT NULL,
  `description` TEXT DEFAULT NULL,
  `assigned_worker_id` INT DEFAULT NULL,
  `start_date` DATE NOT NULL,
  `due_date` DATE NOT NULL,
  `priority` ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
  `status` ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'DELAYED') DEFAULT 'NOT_STARTED',
  `progress_percent` INT DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`site_id`) REFERENCES `sites` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`assigned_worker_id`) REFERENCES `workers` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX `idx_tasks_project` (`project_id`),
  INDEX `idx_tasks_site` (`site_id`),
  INDEX `idx_tasks_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Daily Progress Table
CREATE TABLE IF NOT EXISTS `daily_progress` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `project_id` INT NOT NULL,
  `site_id` INT NOT NULL,
  `task_id` INT DEFAULT NULL,
  `progress_date` DATE NOT NULL,
  `workers_present` INT DEFAULT 0,
  `work_completed` TEXT NOT NULL,
  `progress_percent` INT DEFAULT 0,
  `issues` TEXT DEFAULT NULL,
  `remarks` TEXT DEFAULT NULL,
  `created_by` INT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`site_id`) REFERENCES `sites` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX `idx_progress_project` (`project_id`),
  INDEX `idx_progress_date` (`progress_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Machine Usage Table
CREATE TABLE IF NOT EXISTS `machine_usage` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `booking_id` INT NOT NULL,
  `usage_date` DATE NOT NULL,
  `hours_used` DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
  `fuel_cost` DECIMAL(10, 2) DEFAULT 0.00,
  `operator_present` BOOLEAN DEFAULT TRUE,
  `work_description` TEXT DEFAULT NULL,
  `remarks` TEXT DEFAULT NULL,
  FOREIGN KEY (`booking_id`) REFERENCES `machinery_bookings` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX `idx_usage_booking` (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Expenses Table
CREATE TABLE IF NOT EXISTS `expenses` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `project_id` INT NOT NULL,
  `site_id` INT DEFAULT NULL,
  `category` ENUM('MACHINERY', 'LABOUR', 'MATERIAL', 'TRANSPORT', 'FUEL', 'OTHER') NOT NULL,
  `amount` DECIMAL(15, 2) NOT NULL,
  `expense_date` DATE NOT NULL,
  `description` TEXT NOT NULL,
  `reference_type` VARCHAR(50) DEFAULT NULL,
  `reference_id` INT DEFAULT NULL,
  `created_by` INT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`site_id`) REFERENCES `sites` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX `idx_expenses_project` (`project_id`),
  INDEX `idx_expenses_category` (`category`),
  INDEX `idx_expenses_date` (`expense_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Issues Table
CREATE TABLE IF NOT EXISTS `issues` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `project_id` INT NOT NULL,
  `site_id` INT DEFAULT NULL,
  `machinery_id` INT DEFAULT NULL,
  `issue_type` VARCHAR(100) NOT NULL,
  `description` TEXT NOT NULL,
  `priority` ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
  `status` ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') DEFAULT 'OPEN',
  `reported_by` INT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `resolved_at` TIMESTAMP NULL DEFAULT NULL,
  FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`site_id`) REFERENCES `sites` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`machinery_id`) REFERENCES `machinery` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`reported_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX `idx_issues_project` (`project_id`),
  INDEX `idx_issues_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
