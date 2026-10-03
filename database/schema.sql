-- ==============================================================================
-- E2E Construction Management System - Database Schema
-- DBMS: MySQL 8.0+
-- Encoding: UTF-8 (utf8mb4)
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS `e2e_construction_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `e2e_construction_db`;

-- Disable foreign key checks during schema creation / recreation
SET FOREIGN_KEY_CHECKS = 0;

-- Drop tables in reverse dependency order if recreating
DROP TABLE IF EXISTS `labor_assignments`;
DROP TABLE IF EXISTS `labor_requests`;
DROP TABLE IF EXISTS `tenders`;
DROP TABLE IF EXISTS `machinery_bookings`;
DROP TABLE IF EXISTS `machinery_maintenance_images`;
DROP TABLE IF EXISTS `machinery_maintenance`;
DROP TABLE IF EXISTS `machinery_images`;
DROP TABLE IF EXISTS `machine_conditions`;
DROP TABLE IF EXISTS `material_orders`;
DROP TABLE IF EXISTS `material_stock_history`;
DROP TABLE IF EXISTS `materials`;
DROP TABLE IF EXISTS `machinery`;
DROP TABLE IF EXISTS `projects`;
DROP TABLE IF EXISTS `clients`;
DROP TABLE IF EXISTS `material_suppliers`;
DROP TABLE IF EXISTS `machinery_owners`;
DROP TABLE IF EXISTS `laborers`;
DROP TABLE IF EXISTS `contractors`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `roles`;

SET FOREIGN_KEY_CHECKS = 1;

-- ==============================================================================
-- 1. ROLES TABLE
-- Lookup table for authorization and user categorization.
-- ==============================================================================
CREATE TABLE `roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `description` VARCHAR(255) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_roles` PRIMARY KEY (`id`),
    CONSTRAINT `uq_roles_name` UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 2. USERS TABLE
-- Core identity table storing authentication credentials, contact details, and role.
-- ==============================================================================
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL,
    `email` VARCHAR(150) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `first_name` VARCHAR(100) NOT NULL,
    `last_name` VARCHAR(100) NOT NULL,
    `phone_number` VARCHAR(20) NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_users` PRIMARY KEY (`id`),
    CONSTRAINT `uq_users_email` UNIQUE (`email`),
    CONSTRAINT `uq_users_phone` UNIQUE (`phone_number`),
    CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`)
        REFERENCES `roles` (`id`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 3. CONTRACTORS TABLE
-- Professional contractor profile linked 1:1 with users.
-- ==============================================================================
CREATE TABLE `contractors` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `company_name` VARCHAR(150) NOT NULL,
    `company_description` TEXT NULL,
    `license_number` VARCHAR(100) NULL,
    `years_of_experience` INT UNSIGNED NOT NULL DEFAULT 0,
    `specialization` VARCHAR(100) NULL,
    `rating` DECIMAL(3, 2) NOT NULL DEFAULT 0.00,
    `address_line` VARCHAR(255) NOT NULL,
    `city` VARCHAR(100) NOT NULL,
    `state` VARCHAR(100) NOT NULL,
    `postal_code` VARCHAR(20) NULL,
    `profile_image` VARCHAR(500) NULL,
    `verification_status` ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_contractors` PRIMARY KEY (`id`),
    CONSTRAINT `uq_contractors_user_id` UNIQUE (`user_id`),
    CONSTRAINT `fk_contractors_user` FOREIGN KEY (`user_id`)
        REFERENCES `users` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 4. LABORERS TABLE
-- Skilled and unskilled laborer profiles linked 1:1 with users.
-- ==============================================================================
CREATE TABLE `laborers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `profile_image` VARCHAR(500) NULL,
    `location` VARCHAR(255) NOT NULL,
    `city` VARCHAR(100) NULL,
    `state` VARCHAR(100) NULL,
    `skills` VARCHAR(255) NOT NULL,
    `years_of_experience` INT UNSIGNED NOT NULL DEFAULT 0,
    `daily_wage` DECIMAL(10, 2) NOT NULL,
    `description` TEXT NULL,
    `availability_status` ENUM('AVAILABLE', 'WORKING', 'UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
    `verification_status` ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_laborers` PRIMARY KEY (`id`),
    CONSTRAINT `uq_laborers_user_id` UNIQUE (`user_id`),
    CONSTRAINT `fk_laborers_user` FOREIGN KEY (`user_id`)
        REFERENCES `users` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 5. MACHINERY OWNERS TABLE
-- Equipment and heavy vehicle rental business profile linked 1:1 with users.
-- ==============================================================================
CREATE TABLE `machinery_owners` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `company_name` VARCHAR(150) NOT NULL,
    `tax_id` VARCHAR(100) NULL,
    `location` VARCHAR(255) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `city` VARCHAR(100) NULL,
    `state` VARCHAR(100) NULL,
    `description` TEXT NULL,
    `profile_image` VARCHAR(500) NULL,
    `verification_status` ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_machinery_owners` PRIMARY KEY (`id`),
    CONSTRAINT `uq_machinery_owners_user_id` UNIQUE (`user_id`),
    CONSTRAINT `uq_machinery_owners_tax_id` UNIQUE (`tax_id`),
    CONSTRAINT `fk_machinery_owners_user` FOREIGN KEY (`user_id`)
        REFERENCES `users` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 6. MATERIAL SUPPLIERS TABLE
-- Building material merchant/wholesaler profile linked 1:1 with users.
-- ==============================================================================
CREATE TABLE `material_suppliers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `store_name` VARCHAR(150) NOT NULL,
    `gst_or_tax_number` VARCHAR(100) NULL,
    `contact_phone` VARCHAR(50) NULL,
    `warehouse_address` VARCHAR(255) NOT NULL,
    `city` VARCHAR(100) NOT NULL,
    `state` VARCHAR(100) NOT NULL,
    `delivery_available` BOOLEAN NOT NULL DEFAULT TRUE,
    `description` TEXT NULL,
    `verification_status` ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'VERIFIED',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_material_suppliers` PRIMARY KEY (`id`),
    CONSTRAINT `uq_material_suppliers_user_id` UNIQUE (`user_id`),
    CONSTRAINT `uq_material_suppliers_tax_number` UNIQUE (`gst_or_tax_number`),
    CONSTRAINT `fk_material_suppliers_user` FOREIGN KEY (`user_id`)
        REFERENCES `users` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 7. CLIENTS TABLE
-- Property owners / developers / clients initiating construction projects.
-- ==============================================================================
CREATE TABLE `clients` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `client_type` ENUM('INDIVIDUAL', 'CORPORATE', 'GOVERNMENT') NOT NULL DEFAULT 'INDIVIDUAL',
    `organization_name` VARCHAR(150) NULL,
    `billing_address` VARCHAR(255) NOT NULL,
    `city` VARCHAR(100) NOT NULL,
    `state` VARCHAR(100) NOT NULL,
    `postal_code` VARCHAR(20) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_clients` PRIMARY KEY (`id`),
    CONSTRAINT `uq_clients_user_id` UNIQUE (`user_id`),
    CONSTRAINT `fk_clients_user` FOREIGN KEY (`user_id`)
        REFERENCES `users` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 8. PROJECTS TABLE
-- Construction projects posted and owned by clients, optionally assigned to a contractor.
-- ==============================================================================
CREATE TABLE `projects` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `contractor_id` BIGINT NOT NULL,
    `client_id` BIGINT NULL,
    `name` VARCHAR(200) NOT NULL,
    `project_type` ENUM('BUILDING', 'ROAD', 'BRIDGE', 'INDUSTRIAL', 'RESIDENTIAL', 'COMMERCIAL', 'GOVERNMENT', 'OTHER') NOT NULL,
    `client_name` VARCHAR(150) NULL,
    `location` VARCHAR(255) NOT NULL,
    `city` VARCHAR(100) NOT NULL,
    `state` VARCHAR(100) NOT NULL,
    `start_date` DATE NULL,
    `expected_completion_date` DATE NULL,
    `budget` DECIMAL(15, 2) NOT NULL,
    `description` TEXT NOT NULL,
    `status` ENUM('PLANNED', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PLANNED',
    `progress_percentage` INT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_projects` PRIMARY KEY (`id`),
    CONSTRAINT `fk_projects_contractor` FOREIGN KEY (`contractor_id`)
        REFERENCES `contractors` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_projects_client` FOREIGN KEY (`client_id`)
        REFERENCES `clients` (`id`)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 9. MACHINERY TABLE
-- Machinery catalog managed by machinery owners for project equipment rentals.
-- ==============================================================================
CREATE TABLE `machinery` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `owner_id` BIGINT NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `category` ENUM('BUILDING', 'ROAD', 'BRIDGE', 'EARTHWORK', 'CONCRETE', 'LIFTING', 'TRANSPORT', 'OTHER') NOT NULL,
    `manufacturer` VARCHAR(100) NOT NULL,
    `model` VARCHAR(100) NOT NULL,
    `manufacturing_year` INT NOT NULL,
    `capacity` VARCHAR(100) NOT NULL,
    `fuel_type` VARCHAR(50) NOT NULL,
    `transmission` VARCHAR(50) NOT NULL,
    `location` VARCHAR(255) NOT NULL,
    `rental_price_per_day` DECIMAL(10, 2) NOT NULL,
    `description` TEXT NOT NULL,
    `availability_status` ENUM('AVAILABLE', 'BOOKED', 'MAINTENANCE', 'UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
    `verification_status` ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_machinery` PRIMARY KEY (`id`),
    CONSTRAINT `fk_machinery_owner` FOREIGN KEY (`owner_id`)
        REFERENCES `machinery_owners` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 9B. MACHINERY IMAGES TABLE
-- Uploaded photographs and inspection angle images for machinery.
-- ==============================================================================
CREATE TABLE `machinery_images` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `machinery_id` BIGINT NOT NULL,
    `image_type` ENUM('FRONT', 'REAR', 'LEFT_SIDE', 'RIGHT_SIDE', 'CABIN', 'ENGINE', 'TYRE', 'HYDRAULIC', 'HOUR_METER', 'OTHER') NOT NULL DEFAULT 'OTHER',
    `file_name` VARCHAR(255) NOT NULL,
    `stored_file_name` VARCHAR(255) NOT NULL,
    `file_path` VARCHAR(500) NOT NULL,
    `file_url` VARCHAR(500) NOT NULL,
    `file_size` BIGINT NOT NULL,
    `content_type` VARCHAR(100) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `pk_machinery_images` PRIMARY KEY (`id`),
    CONSTRAINT `fk_machinery_images_machine` FOREIGN KEY (`machinery_id`)
        REFERENCES `machinery` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 9C. MACHINE CONDITIONS TABLE
-- Detailed mechanical, operational, and physical condition logs for machinery.
-- ==============================================================================
CREATE TABLE `machine_conditions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `machinery_id` BIGINT NOT NULL,
    `engine` VARCHAR(50) NOT NULL,
    `engine_oil` VARCHAR(50) NOT NULL,
    `coolant` VARCHAR(50) NOT NULL,
    `transmission` VARCHAR(50) NOT NULL,
    `hydraulic_system` VARCHAR(50) NOT NULL,
    `battery` VARCHAR(50) NOT NULL,
    `brakes` VARCHAR(50) NOT NULL,
    `tyres` VARCHAR(50) NOT NULL,
    `lights` VARCHAR(50) NOT NULL,
    `body` VARCHAR(50) NOT NULL,
    `leakage` VARCHAR(50) NOT NULL,
    `starting_condition` VARCHAR(50) NOT NULL,
    `inspection_date` DATE NOT NULL,
    `last_service_date` DATE NULL,
    `next_service_date` DATE NULL,
    `engine_hours` DOUBLE NULL,
    `odometer` DOUBLE NULL,
    `remarks` TEXT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_machine_conditions` PRIMARY KEY (`id`),
    CONSTRAINT `fk_machine_conditions_machinery` FOREIGN KEY (`machinery_id`)
        REFERENCES `machinery` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 9D. MACHINERY MAINTENANCE TABLE & IMAGES
-- Complete maintenance history, service provider details, costs, and inspection images.
-- ==============================================================================
CREATE TABLE `machinery_maintenance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `machinery_id` BIGINT NOT NULL,
    `maintenance_date` DATE NOT NULL,
    `maintenance_type` ENUM('ENGINE_SERVICE', 'OIL_CHANGE', 'BRAKE_SERVICE', 'TYRE_REPLACEMENT', 'HYDRAULIC_SERVICE', 'ELECTRICAL_SERVICE', 'GENERAL_SERVICE', 'OTHER') NOT NULL,
    `description` TEXT NOT NULL,
    `cost` DECIMAL(12, 2) NOT NULL,
    `service_provider` VARCHAR(150) NOT NULL,
    `engine_hours` DOUBLE NULL,
    `next_service_date` DATE NULL,
    `remarks` TEXT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_machinery_maintenance` PRIMARY KEY (`id`),
    CONSTRAINT `fk_machinery_maintenance_machine` FOREIGN KEY (`machinery_id`)
        REFERENCES `machinery` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `machinery_maintenance_images` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `maintenance_id` BIGINT NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `stored_file_name` VARCHAR(255) NOT NULL,
    `file_path` VARCHAR(500) NOT NULL,
    `file_url` VARCHAR(500) NOT NULL,
    `file_size` BIGINT NULL,
    `content_type` VARCHAR(100) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `pk_machinery_maintenance_images` PRIMARY KEY (`id`),
    CONSTRAINT `fk_machinery_maintenance_images_maint` FOREIGN KEY (`maintenance_id`)
        REFERENCES `machinery_maintenance` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 9E. MACHINERY BOOKINGS TABLE
-- Rental contracts, requested dates, pricing, and double-booking prevention state.
-- ==============================================================================
CREATE TABLE `machinery_bookings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `machinery_id` BIGINT NOT NULL,
    `contractor_id` BIGINT NOT NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `total_days` INT NOT NULL,
    `daily_price` DECIMAL(10, 2) NOT NULL,
    `total_price` DECIMAL(12, 2) NOT NULL,
    `status` ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    `project_name` VARCHAR(150) NULL,
    `delivery_location` VARCHAR(255) NULL,
    `remarks` TEXT NULL,
    `owner_notes` TEXT NULL,
    `rejection_reason` TEXT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_machinery_bookings` PRIMARY KEY (`id`),
    CONSTRAINT `fk_machinery_bookings_machinery` FOREIGN KEY (`machinery_id`)
        REFERENCES `machinery` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_machinery_bookings_contractor` FOREIGN KEY (`contractor_id`)
        REFERENCES `contractors` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 9F. LABOR REQUESTS TABLE
-- Work requests sent by contractors to verified laborers for project staffing.
-- ==============================================================================
CREATE TABLE `labor_requests` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `contractor_id` BIGINT NOT NULL,
    `laborer_id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `total_days` INT NOT NULL,
    `daily_wage` DECIMAL(10, 2) NOT NULL,
    `total_estimated_cost` DECIMAL(12, 2) NOT NULL,
    `job_description` TEXT NOT NULL,
    `status` ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    `contractor_notes` TEXT NULL,
    `laborer_notes` TEXT NULL,
    `rejection_reason` TEXT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_labor_requests` PRIMARY KEY (`id`),
    CONSTRAINT `fk_labor_requests_contractor` FOREIGN KEY (`contractor_id`)
        REFERENCES `contractors` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_labor_requests_laborer` FOREIGN KEY (`laborer_id`)
        REFERENCES `laborers` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_labor_requests_project` FOREIGN KEY (`project_id`)
        REFERENCES `projects` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 9G. LABOR ASSIGNMENTS TABLE
-- Active project assignments created when a laborer accepts a contractor's work request.
-- ==============================================================================
CREATE TABLE `labor_assignments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `labor_request_id` BIGINT NOT NULL,
    `laborer_id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `contractor_id` BIGINT NOT NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `daily_wage` DECIMAL(10, 2) NOT NULL,
    `status` ENUM('ACTIVE', 'COMPLETED', 'TERMINATED') NOT NULL DEFAULT 'ACTIVE',
    `notes` TEXT NULL,
    `assigned_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `completed_at` TIMESTAMP NULL,
    CONSTRAINT `pk_labor_assignments` PRIMARY KEY (`id`),
    CONSTRAINT `uq_labor_assignments_request` UNIQUE (`labor_request_id`),
    CONSTRAINT `fk_labor_assignments_request` FOREIGN KEY (`labor_request_id`)
        REFERENCES `labor_requests` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_labor_assignments_laborer` FOREIGN KEY (`laborer_id`)
        REFERENCES `laborers` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_labor_assignments_project` FOREIGN KEY (`project_id`)
        REFERENCES `projects` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_labor_assignments_contractor` FOREIGN KEY (`contractor_id`)
        REFERENCES `contractors` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 10. MATERIALS TABLE
-- Construction supplies catalog managed by material suppliers.
-- ==============================================================================
CREATE TABLE `materials` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `supplier_id` BIGINT NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `category` ENUM('CEMENT', 'STEEL', 'SAND', 'AGGREGATE', 'BITUMEN', 'DBM', 'BC', 'WMM', 'GSB', 'CONCRETE', 'BRICKS', 'PIPES', 'ELECTRICAL', 'PLUMBING', 'FUEL_OIL', 'OTHER') NOT NULL,
    `grade_specification` VARCHAR(100) NULL,
    `unit` VARCHAR(50) NOT NULL,
    `available_quantity` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `price` DECIMAL(10, 2) NOT NULL,
    `location` VARCHAR(255) NOT NULL,
    `description` TEXT NULL,
    `availability` ENUM('IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK', 'UNAVAILABLE') NOT NULL DEFAULT 'IN_STOCK',
    `verification_status` ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'VERIFIED',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_materials` PRIMARY KEY (`id`),
    CONSTRAINT `fk_materials_supplier` FOREIGN KEY (`supplier_id`)
        REFERENCES `material_suppliers` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 10B. MATERIAL STOCK HISTORY TABLE
-- Log of stock replenishment, order deductions, adjustments, and price updates.
-- ==============================================================================
CREATE TABLE `material_stock_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `material_id` BIGINT NOT NULL,
    `previous_quantity` DECIMAL(12, 2) NOT NULL,
    `change_quantity` DECIMAL(12, 2) NOT NULL,
    `new_quantity` DECIMAL(12, 2) NOT NULL,
    `change_type` ENUM('INITIAL_STOCK', 'RESTOCK', 'ORDER_DEDUCTION', 'ORDER_CANCELLED_RESTORE', 'MANUAL_ADJUSTMENT', 'PRICE_UPDATE') NOT NULL,
    `reference_id` BIGINT NULL,
    `notes` TEXT NULL,
    `recorded_by` VARCHAR(150) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `pk_material_stock_history` PRIMARY KEY (`id`),
    CONSTRAINT `fk_stock_history_material` FOREIGN KEY (`material_id`)
        REFERENCES `materials` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 10C. MATERIAL ORDERS TABLE
-- Purchase orders placed by contractors with stock validation and tracking.
-- ==============================================================================
CREATE TABLE `material_orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_number` VARCHAR(100) NOT NULL,
    `contractor_id` BIGINT NOT NULL,
    `supplier_id` BIGINT NOT NULL,
    `material_id` BIGINT NOT NULL,
    `project_id` BIGINT NULL,
    `quantity` DECIMAL(12, 2) NOT NULL,
    `unit` VARCHAR(50) NOT NULL,
    `unit_price` DECIMAL(10, 2) NOT NULL,
    `total_amount` DECIMAL(14, 2) NOT NULL,
    `delivery_address` VARCHAR(255) NULL,
    `requested_delivery_date` DATE NULL,
    `status` ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED', 'COMPLETED', 'DELIVERED') NOT NULL DEFAULT 'PENDING',
    `contractor_notes` TEXT NULL,
    `supplier_notes` TEXT NULL,
    `rejection_reason` TEXT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_material_orders` PRIMARY KEY (`id`),
    CONSTRAINT `uq_material_orders_number` UNIQUE (`order_number`),
    CONSTRAINT `fk_material_orders_contractor` FOREIGN KEY (`contractor_id`)
        REFERENCES `contractors` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_material_orders_supplier` FOREIGN KEY (`supplier_id`)
        REFERENCES `material_suppliers` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_material_orders_material` FOREIGN KEY (`material_id`)
        REFERENCES `materials` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_material_orders_project` FOREIGN KEY (`project_id`)
        REFERENCES `projects` (`id`)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 11. TENDERS TABLE
-- Competitive procurement invitations published for projects to award contractors.
-- ==============================================================================
CREATE TABLE `tenders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `reference_code` VARCHAR(100) NOT NULL,
    `estimated_budget` DECIMAL(15, 2) NOT NULL,
    `submission_deadline` DATETIME NOT NULL,
    `awarded_contractor_id` BIGINT NULL,
    `status` ENUM('DRAFT', 'PUBLISHED', 'UNDER_EVALUATION', 'AWARDED', 'CANCELLED', 'CLOSED') NOT NULL DEFAULT 'DRAFT',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `pk_tenders` PRIMARY KEY (`id`),
    CONSTRAINT `uq_tenders_reference_code` UNIQUE (`reference_code`),
    CONSTRAINT `fk_tenders_project` FOREIGN KEY (`project_id`)
        REFERENCES `projects` (`id`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_tenders_awarded_contractor` FOREIGN KEY (`awarded_contractor_id`)
        REFERENCES `contractors` (`id`)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- SYSTEM INITIALIZATION DATA (Core Roles Only - No Fake Data)
-- ==============================================================================
INSERT INTO `roles` (`name`, `description`) VALUES
('ADMIN', 'System Administrator with full management and governance privileges'),
('CONTRACTOR', 'Licensed contractor managing bids, site execution, laborers, and materials'),
('LABORER', 'Skilled/unskilled worker offering on-site construction trade services'),
('MACHINERY_OWNER', 'Owner/supplier of heavy construction machinery and equipment rentals'),
('MATERIAL_SUPPLIER', 'Vendor/distributor supplying raw construction materials and goods'),
('CLIENT', 'Project owner, property developer, or individual commissioning construction')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);
