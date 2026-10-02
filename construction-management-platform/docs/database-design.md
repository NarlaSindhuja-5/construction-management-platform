# Database Design Document

## Database Name: `construction_management_db`
**Database Engine:** MySQL 8.0 (InnoDB)  
**Character Set:** `utf8mb4`  
**Collation:** `utf8mb4_unicode_ci`  

---

### 1. Entity-Relationship Schema Overview

```
 [users]
    │ 1
    ├──< [projects] (created_by)
    │       │ 1
    │       ├──< [sites] (project_id)
    │       │       │ 1
    │       │       ├──< [workers] (site_id)
    │       │       ├──< [materials] (site_id)
    │       │       ├──< [tasks] (site_id) ────< [daily_progress] (task_id)
    │       │       └──< [daily_progress] (site_id)
    │       │
    │       ├──< [machinery_bookings] (project_id)
    │       │       │ 1
    │       │       └──< [machine_usage] (booking_id)
    │       │
    │       ├──< [expenses] (project_id)
    │       └──< [issues] (project_id)
    │
    └──< [machinery] (owner_id)
            │ 1
            ├──< [machinery_bookings] (machinery_id)
            └──< [machinery_maintenance] (machinery_id)
```

---

### 2. Table Specifications

#### 2.1 `users`
Stores system accounts, credentials, and role authorizations.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique User Identifier |
| `name` | VARCHAR(100) | NOT NULL | User's Full Name |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE | Authentication Email |
| `password_hash` | VARCHAR(255) | NOT NULL | Bcrypt Hashed Password |
| `phone` | VARCHAR(20) | NULL | Contact Mobile Number |
| `role` | ENUM | NOT NULL | `ADMIN`, `CONTRACTOR`, `OWNER`, `SITE_MANAGER` |
| `status` | ENUM | DEFAULT 'ACTIVE' | `ACTIVE`, `INACTIVE` |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account Creation Time |
| `updated_at` | TIMESTAMP | AUTO UPDATE | Last Profile Update Time |

#### 2.2 `projects`
Construction contracts and baseline budgets.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Project ID |
| `project_name` | VARCHAR(200) | NOT NULL | Project Title |
| `client_name` | VARCHAR(150) | NOT NULL | Government / Private Client |
| `project_type` | VARCHAR(100) | NOT NULL | Commercial, Highway, Residential, etc. |
| `location` | VARCHAR(200) | NOT NULL | City / Region |
| `description` | TEXT | NULL | Detailed Scope |
| `start_date` | DATE | NOT NULL | Project Start Date |
| `end_date` | DATE | NOT NULL | Target Completion Date |
| `budget` | DECIMAL(15,2)| NOT NULL, DEFAULT 0.00 | Allocated Budget Amount |
| `status` | ENUM | DEFAULT 'PLANNED' | `PLANNED`, `ACTIVE`, `COMPLETED`, `ON_HOLD`, `CANCELLED` |
| `created_by` | INT | NOT NULL, FK -> users(id) | Creator Contractor/Admin |

#### 2.3 `sites`
Operational construction sites located within projects.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Site ID |
| `project_id` | INT | NOT NULL, FK -> projects(id) ON DELETE CASCADE | Parent Project |
| `site_name` | VARCHAR(200) | NOT NULL | Zone / Site Identifier |
| `location` | VARCHAR(200) | NOT NULL | Area / City |
| `address` | TEXT | NULL | Physical Address Coordinates |
| `site_manager_id`| INT | NULL, FK -> users(id) ON DELETE SET NULL | Assigned Site Engineer |
| `status` | ENUM | DEFAULT 'ACTIVE' | `ACTIVE`, `COMPLETED`, `INACTIVE` |

#### 2.4 `machinery`
Heavy equipment registered in the rental marketplace.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Machine ID |
| `owner_id` | INT | NOT NULL, FK -> users(id) | Fleet Owner ID |
| `machine_name` | VARCHAR(150) | NOT NULL | Brand / Model Name |
| `machine_type` | VARCHAR(100) | NOT NULL | JCB, Excavator, Roller, Crane, etc. |
| `registration_number`| VARCHAR(50)| NOT NULL, UNIQUE | Vehicle / Plate Registration |
| `location` | VARCHAR(150) | NOT NULL | Operating City |
| `daily_rate` | DECIMAL(12,2)| NOT NULL | Daily Rental Cost |
| `operator_available` | BOOLEAN | DEFAULT TRUE | Certified Operator Included |
| `operator_charge` | DECIMAL(12,2)| DEFAULT 0.00 | Daily Surcharge for Operator |
| `status` | ENUM | DEFAULT 'AVAILABLE' | `AVAILABLE`, `BOOKED`, `MAINTENANCE`, `INACTIVE` |
| `description` | TEXT | NULL | Capacity, Attachments, Specs |

#### 2.5 `machinery_bookings`
Date-based rental reservations and approval states.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Booking ID |
| `machinery_id` | INT | NOT NULL, FK -> machinery(id) | Booked Equipment |
| `project_id` | INT | NOT NULL, FK -> projects(id) | Target Project |
| `site_id` | INT | NOT NULL, FK -> sites(id) | Target Construction Site |
| `contractor_id` | INT | NOT NULL, FK -> users(id) | Requesting Contractor |
| `start_date` | DATE | NOT NULL | Rental Start Date |
| `end_date` | DATE | NOT NULL | Rental End Date |
| `total_days` | INT | NOT NULL | Inclusive Days Count |
| `daily_rate` | DECIMAL(12,2)| NOT NULL | Rate at Booking Time |
| `operator_charge`| DECIMAL(12,2)| DEFAULT 0.00 | Total Operator Charges |
| `transport_charge`| DECIMAL(12,2)| DEFAULT 0.00 | Mobilization / Hauling Cost |
| `additional_charge`| DECIMAL(12,2)| DEFAULT 0.00 | Ancillary Surcharges |
| `total_amount` | DECIMAL(15,2)| NOT NULL | Total Rental Amount |
| `status` | ENUM | DEFAULT 'PENDING' | `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`, `COMPLETED` |
| `requested_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Submission Timestamp |
| `approved_at` | TIMESTAMP | NULL | Owner Approval Timestamp |
| `rejected_at` | TIMESTAMP | NULL | Owner Rejection Timestamp |
| `rejection_reason` | TEXT | NULL | Required Reason if Rejected |

#### 2.6 `machinery_maintenance`
Maintenance windows that block machinery availability.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Maintenance ID |
| `machinery_id` | INT | NOT NULL, FK -> machinery(id) | Equipment |
| `start_date` | DATE | NOT NULL | Service Start Date |
| `end_date` | DATE | NOT NULL | Expected Completion Date |
| `reason` | TEXT | NOT NULL | Repair / Overhaul Details |
| `status` | ENUM | DEFAULT 'SCHEDULED' | `SCHEDULED`, `IN_PROGRESS`, `COMPLETED` |

#### 2.7 `workers`
Skilled tradespeople and laborer records.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Worker ID |
| `name` | VARCHAR(100) | NOT NULL | Worker Full Name |
| `phone` | VARCHAR(20) | NOT NULL | Contact Mobile |
| `skill` | VARCHAR(100) | NOT NULL | Operator, Mason, Electrician, etc. |
| `experience` | INT | DEFAULT 0 | Years in Trade |
| `daily_wage` | DECIMAL(10,2)| NOT NULL | Daily Compensation |
| `availability` | ENUM | DEFAULT 'AVAILABLE' | `AVAILABLE`, `ASSIGNED`, `ON_LEAVE` |
| `status` | ENUM | DEFAULT 'ACTIVE' | `ACTIVE`, `INACTIVE` |
| `site_id` | INT | NULL, FK -> sites(id) ON DELETE SET NULL | Current Site Assignment |

#### 2.8 `materials`
Raw construction stock inventory.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Material ID |
| `site_id` | INT | NOT NULL, FK -> sites(id) | Storage Site |
| `material_name` | VARCHAR(100) | NOT NULL | Brand / Grade Name |
| `category` | VARCHAR(100) | NOT NULL | Cement, Steel, Sand, Aggregate, etc. |
| `quantity` | DECIMAL(12,2)| NOT NULL, DEFAULT 0.00 | Available Quantity |
| `minimum_quantity`| DECIMAL(12,2)| NOT NULL, DEFAULT 0.00 | Safety Reorder Threshold |
| `unit` | VARCHAR(50) | NOT NULL | Bags, Tons, CuM, Liters |
| `unit_price` | DECIMAL(12,2)| NOT NULL, DEFAULT 0.00 | Purchase Price per Unit |
| `supplier` | VARCHAR(150) | NULL | Supplier Agency |

#### 2.9 `tasks`
Work breakdown activity items.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Task ID |
| `project_id` | INT | NOT NULL, FK -> projects(id) | Project |
| `site_id` | INT | NOT NULL, FK -> sites(id) | Site |
| `task_name` | VARCHAR(200) | NOT NULL | Activity Name |
| `description` | TEXT | NULL | Work Specs |
| `assigned_worker_id`| INT | NULL, FK -> workers(id) | Lead Worker In-Charge |
| `start_date` | DATE | NOT NULL | Start Date |
| `due_date` | DATE | NOT NULL | Target Deadline |
| `priority` | ENUM | DEFAULT 'MEDIUM' | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `status` | ENUM | DEFAULT 'NOT_STARTED'| `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `DELAYED` |
| `progress_percent` | INT | DEFAULT 0 | 0 to 100 Percentage |

#### 2.10 `daily_progress`
Site daily log entries.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Progress Log ID |
| `project_id` | INT | NOT NULL, FK -> projects(id) | Project |
| `site_id` | INT | NOT NULL, FK -> sites(id) | Site |
| `task_id` | INT | NULL, FK -> tasks(id) | Associated Task |
| `progress_date`| DATE | NOT NULL | Date of Work |
| `workers_present`| INT | DEFAULT 0 | Muster Roll Count |
| `work_completed`| TEXT | NOT NULL | Work Description |
| `progress_percent`| INT | DEFAULT 0 | Milestone Percentage |
| `issues` | TEXT | NULL | Site Hurdles |
| `remarks` | TEXT | NULL | General Observations |
| `created_by` | INT | NOT NULL, FK -> users(id) | Site Engineer |

#### 2.11 `machine_usage`
On-site machine logbook.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Usage ID |
| `booking_id` | INT | NOT NULL, FK -> machinery_bookings(id) | Active Booking |
| `usage_date` | DATE | NOT NULL | Date of Operation |
| `hours_used` | DECIMAL(5,2) | NOT NULL | Operating Hours |
| `fuel_cost` | DECIMAL(10,2)| DEFAULT 0.00 | Daily Diesel Expense |
| `operator_present`| BOOLEAN | DEFAULT TRUE | Operator Present |
| `work_description`| TEXT | NULL | Excavation / Lifting Done |

#### 2.12 `expenses`
Financial expenditures and automated rental bookings.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Expense ID |
| `project_id` | INT | NOT NULL, FK -> projects(id) | Project |
| `site_id` | INT | NULL, FK -> sites(id) | Site |
| `category` | ENUM | NOT NULL | `MACHINERY`, `LABOUR`, `MATERIAL`, `TRANSPORT`, `FUEL`, `OTHER` |
| `amount` | DECIMAL(15,2)| NOT NULL | Expense Value |
| `expense_date` | DATE | NOT NULL | Date Incurred |
| `description` | TEXT | NOT NULL | Voucher Purpose |
| `reference_type`| VARCHAR(50) | NULL | `MACHINERY_BOOKING`, `MATERIAL_PURCHASE`, `MANUAL` |
| `reference_id` | INT | NULL | Referenced Record ID |
| `created_by` | INT | NOT NULL, FK -> users(id) | Recorded By User |

#### 2.13 `issues`
Site breakdowns and incident logs.
| Field | Type | Attributes | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Issue ID |
| `project_id` | INT | NOT NULL, FK -> projects(id) | Project |
| `site_id` | INT | NULL, FK -> sites(id) | Site |
| `machinery_id` | INT | NULL, FK -> machinery(id) | Involved Equipment |
| `issue_type` | VARCHAR(100) | NOT NULL | Breakdown, Shortage, Safety, etc. |
| `description` | TEXT | NOT NULL | Incident Notes |
| `priority` | ENUM | DEFAULT 'MEDIUM' | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `status` | ENUM | DEFAULT 'OPEN' | `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED` |
| `reported_by` | INT | NOT NULL, FK -> users(id) | Reporter |
| `resolved_at` | TIMESTAMP | NULL | Resolution Timestamp |
