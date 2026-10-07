# Architecture Specification

## End-to-End Construction Management & Resource Booking Platform

### 1. High-Level Architectural Model
The platform implements an enterprise three-tier web application architecture designed for low latency, zero external heavy frontend framework overhead, and strict ACID transaction guarantees.

```
+-----------------------------------------------------------------------+
|                         PRESENTATION TIER                             |
|    HTML5 • Vanilla CSS3 Design System • Native JavaScript ES6+        |
|    Dynamic DOM Rendering • Fetch API • Role-Adaptive Dashboards       |
+-----------------------------------------------------------------------+
                                  │
                          JSON / REST (HTTP/1.1)
                          Authorization: Bearer <JWT>
                                  ▼
+-----------------------------------------------------------------------+
|                         APPLICATION TIER                              |
|                         Node.js & Express                             |
|                                                                       |
|  [Middleware Pipeline]                                                |
|    ├── CORS & JSON Body Parser                                        |
|    ├── JWT Authenticator (`authenticateJWT`)                          |
|    ├── Role Authorization Guard (`authorizeRoles`)                    |
|    └── Centralized Error & Exception Handler                          |
|                                                                       |
|  [Controllers & REST Routes]                                          |
|    ├── Auth & Users          ├── Tasks & Progress                     |
|    ├── Projects & Sites      ├── Expenses & Budgets                   |
|    ├── Machinery & Bookings  ├── Issues & Maintenance                 |
|    └── Workers & Materials   └── Executive Dashboard & Reports        |
|                                                                       |
|  [Domain Services Layer]                                              |
|    ├── `bookingService.js`   (Conflict engine & Transaction approval) |
|    ├── `dashboardService.js` (Real SQL aggregation & Live alerts)     |
|    └── `reportService.js`    (Cross-entity audit compilation)         |
+-----------------------------------------------------------------------+
                                  │
                      Parameterized SQL Queries
                      mysql2 Connection Pool & Transactions
                                  ▼
+-----------------------------------------------------------------------+
|                            DATA TIER                                  |
|                            MySQL 8.0                                  |
|                                                                       |
|   13 Relational Tables (InnoDB, Foreign Keys, Referential Cascades)    |
|   `construction_management_db`                                        |
+-----------------------------------------------------------------------+
```

---

### 2. Core Business Logic Engines

#### 2.1 Date-Based Machinery Booking & Conflict Prevention
The core feature of this platform is eliminating double-bookings and scheduling collisions across construction projects.

- **Date Overlap Condition:**
  Two date ranges $[S_1, E_1]$ and $[S_2, E_2]$ overlap if and only if:
  $$\text{Overlap} \iff (S_1 \le E_2) \land (E_1 \ge S_2)$$
- **Enforcement Rules:**
  1. No machine may have more than one `APPROVED` booking for overlapping calendar dates.
  2. Scheduled maintenance periods (`status != 'COMPLETED'`) reserve the machine and block overlapping booking requests.
  3. Pre-flight check via `GET /api/machinery/:id/availability?startDate=...&endDate=...` informs contractors prior to submission.
  4. Double-check occurs inside an atomic database transaction at the moment the machinery owner approves the request.

#### 2.2 Transaction-Safe Owner Approval & Expense Conversion
When an equipment owner approves a pending booking request:
1. `connection.beginTransaction()`
2. `SELECT ... FROM machinery_bookings WHERE id = ? FOR UPDATE` (Row-level exclusive lock).
3. Re-verify that no concurrent transaction approved another booking for overlapping dates.
4. Verify machine is not under active maintenance.
5. Update booking status to `APPROVED`.
6. Insert automatic project expenditure into the `expenses` table under category `MACHINERY`:
   - `amount = booking.total_amount`
   - `project_id = booking.project_id`
   - `reference_type = 'MACHINERY_BOOKING'`
7. `connection.commit()` (or `rollback()` on any failure).

---

### 3. Role-Based Access Control (RBAC) Matrix

| Resource / Action | ADMIN | CONTRACTOR | OWNER | SITE_MANAGER |
|---|:---:|:---:|:---:|:---:|
| User Management & Status | Full | Read-Only | Read-Only | Read-Only |
| Create / Delete Projects | Full | Full | Read-Only | Read-Only |
| Create / Edit Sites | Full | Full | Read-Only | Read-Only |
| Register Machinery | Full | Read-Only | Full (Own Fleet) | Read-Only |
| Request Machinery Booking | Full | Full | Read-Only | Read-Only |
| Approve / Reject Bookings | Full | Read-Only | Full (Own Fleet) | Read-Only |
| Manage Workers & Materials| Full | Full | Read-Only | Full |
| Log Daily Progress & Usage| Full | Full | Read-Only | Full |
| View Financial Reports | Full | Full | Own Bookings | Read-Only |

---

### 4. Mathematical & Business Formulas
1. **Total Booking Days:**
   $$\text{total\_days} = (\text{end\_date} - \text{start\_date}) + 1$$
2. **Total Rental Amount:**
   $$\text{rental\_amount} = \text{daily\_rate} \times \text{total\_days}$$
3. **Grand Total Amount:**
   $$\text{total\_amount} = \text{rental\_amount} + \text{operator\_charge} + \text{transport\_charge} + \text{additional\_charge}$$
4. **Remaining Budget Balance:**
   $$\text{remaining\_budget} = \text{project\_budget} - \text{total\_expenses}$$
5. **Budget Utilization Percentage:**
   $$\text{utilization\_pct} = \left(\frac{\text{total\_expenses}}{\text{project\_budget}}\right) \times 100$$
6. **Low Stock Detection:**
   $$\text{is\_low\_stock} = (\text{quantity} < \text{minimum\_quantity})$$
