# End-to-End Construction Management & Resource Booking Platform

> **A production-ready full-stack enterprise platform connecting construction project management with date-based machinery resource booking, owner approval workflows, strict conflict prevention, automated project expense tracking, and progress monitoring.**

[![Node.js](https://img.shields.io/badge/Node.js-v24.x-green.svg)](https://nodejs.org/)
[![Express.js](https://img.shields.io/badge/Express.js-v4.19.x-blue.svg)](https://expressjs.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)](https://www.mysql.com/)
[![Authentication](https://img.shields.io/badge/Auth-JWT%20%2B%20Bcrypt-red.svg)](https://jwt.io/)
[![Architecture](https://img.shields.io/badge/Architecture-3--Tier%20REST-purple.svg)](#architecture)

---

## 1. Project Overview & Problem Statement

### The Problem in Modern Construction
Construction enterprises operate across multiple geographically distributed sites requiring expensive heavy machinery (JCBs, excavators, transit concrete mixers, road rollers, mobile cranes), specialized labor, and high-turnover building materials. Traditionally, machinery rental and resource assignments are coordinated manually via phone calls, WhatsApp messages, and disconnected spreadsheets:
- **No visibility into equipment availability:** Contractors don't know which machines are free for specific calendar dates.
- **Double bookings & costly collisions:** Machines get assigned to overlapping projects, causing severe timeline delays and financial penalties.
- **Disconnected expenses:** Rental costs negotiated informally are delayed in accounting, preventing real-time budget tracking.
- **Siloed site management:** Site engineers maintain notebooks for daily labor muster and progress without linking to baseline task milestones.

### The Solution: BuildPro Cloud
This platform delivers a single centralized cloud platform:
1. **Contractor** creates a project and operational construction sites.
2. Contractor browses the **Machinery Marketplace**, selects target dates, and system performs **pre-flight availability checks**.
3. Contractor submits a formal booking request with automatic inclusive day and rental cost calculations.
4. **Machinery Owner** logs into their portal to review contractor details and **approves/rejects via an atomic database transaction**.
5. Once approved:
   - The machine is allocated directly to the project and site.
   - The full rental cost is automatically converted into an **Expense entry under category `MACHINERY`**.
   - Overlapping confirmed bookings during those dates are **strictly blocked by the conflict prevention engine**.
6. Site Managers log daily task completion, labor counts, machine usage hours, and site incidents.
7. Executive Dashboard and Reports update live with real database figures.

---

## 2. Technology Stack

- **Frontend:** HTML5, Modern Vanilla CSS3 Design System, Vanilla JavaScript (ES6+), Fetch API *(No React, Angular, or Vue overhead)*.
- **Backend:** Node.js, Express.js REST API framework.
- **Database:** MySQL 8.0 with `mysql2` connection pooling and parameterized SQL queries *(No MongoDB, preventing data corruption via ACID transactions)*.
- **Security & Authentication:** JSON Web Tokens (JWT), Bcrypt password hashing, Role-Based Access Control (RBAC) middleware.
- **Architecture:** 3-Tier Layered Architecture (Presentation → Controller/Service → MySQL Database).

---

## 3. Four Distinct User Roles

| Role | Core Responsibilities & Permissions |
|---|---|
| **ADMIN** | Full system visibility; user management, master data controls, global audits and reports. |
| **CONTRACTOR / PROJECT MANAGER** | Creates projects & sites, searches machinery, requests bookings, manages workers & materials, monitors budgets. |
| **MACHINERY OWNER** | Registers machinery fleet, sets daily rates & operator fees, approves or rejects booking requests with reasons. |
| **SITE MANAGER** | Manages assigned sites, logs daily progress, records machine operating hours & fuel, reports site issues. |

---

## 4. Database Structure & Relational Schema

Database Name: `construction_management_db`

The schema contains **13 relational tables** with foreign keys, index optimizations, and cascade rules:
1. `users`: Credentials, RBAC roles, contact info, and active status.
2. `projects`: Baseline budgets, start/end dates, client details, and status.
3. `sites`: Operational construction sites under projects, linked to site managers.
4. `machinery`: Heavy equipment fleet, vehicle registration, location, daily rates, operator availability.
5. `machinery_bookings`: Date ranges, rate calculation, statuses (`PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`).
6. `machinery_maintenance`: Scheduled service windows that block machine availability.
7. `workers`: Labor workforce, trade skills (Operator, Mason, Electrician, Labour), daily wages, site assignments.
8. `materials`: Inventory quantities, units, suppliers, minimum threshold for stock warnings.
9. `tasks`: Work breakdown activities, deadlines, priority, progress percentage (0-100%).
10. `daily_progress`: Site diaries, workers present, work descriptions, and milestone updates.
11. `machine_usage`: On-site machine logs (hours operated, fuel expenses, remarks).
12. `expenses`: Itemized project costs (`MACHINERY`, `LABOUR`, `MATERIAL`, `TRANSPORT`, `FUEL`, `OTHER`).
13. `issues`: Mechanical breakdowns, safety hazards, weather delays, and resolution logs.

---

## 5. Machinery Booking & Conflict Prevention Logic

### Formula Specifications
$$\text{total\_days} = (\text{end\_date} - \text{start\_date}) + 1$$
$$\text{rental\_amount} = \text{daily\_rate} \times \text{total\_days}$$
$$\text{total\_amount} = \text{rental\_amount} + \text{operator\_charge} + \text{transport\_charge} + \text{additional\_charge}$$

### Conflict Detection Engine
A conflict exists if another confirmed booking or maintenance window overlaps with the requested range $[S_{\text{new}}, E_{\text{new}}]$:
$$(S_{\text{existing}} \le E_{\text{new}}) \land (E_{\text{existing}} \ge S_{\text{new}})$$

**Example:**
- Machine: `Tata Hitachi EX200 LC`
- Existing Confirmed Booking: **10 Oct → 15 Oct**
- New Booking Attempt: **13 Oct → 18 Oct**
- **Result:** System blocks the booking with error message:
  > *"This machinery is already booked during the selected dates."*

---

## 6. Installation & Setup Guide

### Prerequisites
- Node.js (v18.0 or newer installed)
- MySQL Server 8.0 running locally on port 3306

### Step 1: Clone Repository
```bash
git clone https://github.com/yourusername/construction-management-platform.git
cd construction-management-platform
```

### Step 2: Configure Environment Variables
Inside `backend/`, copy `.env.example` to `.env`:
```bash
cd backend
cp .env.example .env
```
Edit `backend/.env` with your MySQL root password:
```env
PORT=5000
NODE_ENV=development
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=your_mysql_password
DB_NAME=construction_management_db
JWT_SECRET=construction_mgmt_super_secret_jwt_key_2026_x89a
JWT_EXPIRES_IN=7d
```

### Step 3: Initialize Database & Seed Sample Data
Run the automated initialization script from `backend/`:
```bash
npm run db:init
```
*(This executes `database/schema.sql` to build all 13 tables, followed by `database/seed.sql` to populate sample projects, machines, workers, materials, and users).*

### Step 4: Install Dependencies & Run Server
```bash
npm install
npm run dev
```

The application will start:
- **Backend API:** `http://localhost:5000/api`
- **Web Frontend:** `http://localhost:5000`

---

## 7. Demo Accounts (1-Click Login Enabled)

For academic evaluations, viva presentations, or judge demonstrations, the login screen provides instant 1-click login buttons:

| Role | Email | Password |
|---|---|---|
| **Contractor / PM** | `contractor@buildpro.com` | `Contractor@123` |
| **Machinery Owner** | `owner@buildpro.com` | `Owner@123` |
| **Site Manager** | `sitemanager@buildpro.com` | `SiteManager@123` |
| **System Admin** | `admin@buildpro.com` | `Admin@123` |

---

## 8. Primary Faculty / Judge Demonstration Workflow

To demonstrate the full business workflow in under 4 minutes:

1. **Login as Contractor:**
   - Go to `http://localhost:5000/login.html` and click **🚜 Contractor**.
2. **Review Projects & Sites:**
   - Navigate to **Projects** → Open "Hyderabad Road Development Project".
   - Notice the allocated budget (₹1,25,00,000) and sites.
3. **Browse Marketplace & Check Dates:**
   - Navigate to **Machinery Marketplace** → Find **JCB 3DX Super EcoXcellence**.
   - Click **📅 Check Dates** → Enter `2026-10-10` to `2026-10-19` → System displays **"AVAILABLE FOR BOOKING"**.
4. **Submit Booking Request:**
   - Click **Proceed to Book Machine**.
   - Select "Hyderabad Road Development Project" and "Gachibowli Flyover" site.
   - Live cost breakdown calculates: 10 Days × ₹5,000 + Operator Charge + Transport = **₹63,000**.
   - Click **Submit Booking Request**.
5. **Logout & Sign In as Machinery Owner:**
   - Sign out and click **🔑 Machine Owner** on the login page.
   - Under **Pending Booking Requests**, the contractor's request appears with full details.
   - Click **✓ Approve**.
   - *(A database transaction verifies no concurrent conflict, locks the record, approves the booking, and automatically creates an expense record in the project).*
6. **Conflict Prevention Demonstration:**
   - Attempt to book the same JCB machine for overlapping dates (`2026-10-13` to `2026-10-18`).
   - The system immediately flags: **"⚠️ DATES NOT AVAILABLE (BOOKING CONFLICT DETECTED)"** and refuses the booking!
7. **Sign In as Contractor to View Expense & Progress:**
   - Log back in as Contractor → Open **Expenses & Budget**.
   - Notice the new approved entry: `Machinery Rental: JCB 3DX Super EcoXcellence (10 Days)` recorded automatically!
   - Open **Executive Reports** → View the updated financial utilization and print the executive summary.

---

## 9. Automated Testing

Run the automated business logic and conflict prevention test suite:
```bash
cd backend
npm test
```
**Test Results:**
```
================================================================
RUNNING BUSINESS LOGIC & CONFLICT PREVENTION UNIT TESTS
================================================================

[1] Date Range & Overlap Logic:
  ✓ PASS: Valid date range validation
  ✓ PASS: Formula total_days = end_date - start_date + 1 (10 Oct to 19 Oct = 10 days)
  ✓ PASS: Single day booking gives total_days = 1

[2] Financial Calculations & Formulas:
  ✓ PASS: Rental calculation: 5000 daily rate * 10 days = 50,000
  ✓ PASS: Grand total calculation with operator, transport, and additional charges
  ✓ PASS: Budget remaining: 1,25,00,000 budget - 82,00,000 expenses = 43,00,000
  ✓ PASS: Budget utilization: (82,00,000 / 1,25,00,000) * 100 = 65.6%
  ✓ PASS: Low stock check: 15 drums < 30 drums threshold triggers alert

[3] Machinery Booking Conflict Prevention (CRITICAL SPECIFICATION):
  ✓ PASS: Conflict Detection: New booking 13 Oct -> 18 Oct MUST BE BLOCKED (Interior overlap)
  ✓ PASS: Conflict Detection: New booking 08 Oct -> 12 Oct MUST BE BLOCKED (Start overlap)
  ✓ PASS: Conflict Detection: New booking 10 Oct -> 15 Oct MUST BE BLOCKED (Exact match overlap)
  ✓ PASS: Conflict Detection: New booking 15 Oct -> 20 Oct MUST BE BLOCKED (Boundary overlap on 15 Oct)
  ✓ PASS: Availability: New booking 01 Oct -> 09 Oct MUST BE ALLOWED (Before existing)
  ✓ PASS: Availability: New booking 16 Oct -> 25 Oct MUST BE ALLOWED (After existing)

================================================================
TEST SUMMARY: 14 / 14 Passed (100%)
================================================================
```

---

## 10. Git Commands & Repository Initialization

To initialize Git and push this repository to GitHub:

```bash
# 1. Initialize Git in the project root
git init

# 2. Add all files
git add .

# 3. Create initial commit
git commit -m "feat: complete end-to-end construction management and resource booking platform"

# 4. Link your remote GitHub repository
git remote add origin https://github.com/yourusername/construction-management-platform.git

# 5. Push to GitHub
git branch -M main
git push -u origin main
```

---

## 11. Future Scope

1. **Mobile Telemetry & IoT:** Integration with GPS tracking dongles on machinery for live geofencing and engine hours telematics.
2. **Photo-Verified Progress Evidence:** Allowing site engineers to attach geo-tagged photographs to daily progress logs.
3. **Escrow Payment Gateway:** Online payment integration (Razorpay / Stripe) to hold contractor rental deposits until project completion.
4. **AI Milestone Predictive Forecasting:** Machine learning models estimating project delay risks based on historical weather, equipment downtime, and material delivery logs.
