# REST API Specification

All endpoints communicate via JSON over HTTP. Protected endpoints require the `Authorization: Bearer <JWT>` header.

Base URL: `http://localhost:5000/api`

---

### 1. Authentication APIs

#### 1.1 Register New Account
- **Endpoint:** `POST /api/auth/register`
- **Auth:** Public
- **Request Body:**
```json
{
  "name": "Vikram Mehta",
  "email": "contractor@buildpro.com",
  "password": "Contractor@123",
  "phone": "+91 9876543202",
  "role": "CONTRACTOR"
}
```
- **Success Response (201 Created):**
```json
{
  "success": true,
  "message": "User account registered successfully.",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 2,
    "name": "Vikram Mehta",
    "email": "contractor@buildpro.com",
    "role": "CONTRACTOR"
  }
}
```

#### 1.2 Sign In
- **Endpoint:** `POST /api/auth/login`
- **Auth:** Public
- **Request Body:**
```json
{
  "email": "contractor@buildpro.com",
  "password": "Contractor@123"
}
```
- **Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful.",
  "token": "eyJhbGciOiJIUzI1Ni...",
  "user": {
    "id": 2,
    "name": "Vikram Mehta (Contractor)",
    "email": "contractor@buildpro.com",
    "role": "CONTRACTOR"
  }
}
```

#### 1.3 Get Current User Profile
- **Endpoint:** `GET /api/auth/me`
- **Auth:** Bearer Token
- **Success Response (200 OK):**
```json
{
  "success": true,
  "user": {
    "id": 2,
    "name": "Vikram Mehta",
    "email": "contractor@buildpro.com",
    "role": "CONTRACTOR"
  }
}
```

---

### 2. Machinery & Date Availability APIs

#### 2.1 Check Date-Based Availability
- **Endpoint:** `GET /api/machinery/:id/availability?startDate=2026-10-10&endDate=2026-10-19`
- **Auth:** Bearer Token
- **Success (Available):**
```json
{
  "available": true,
  "conflicts": []
}
```
- **Success (Conflict Detected):**
```json
{
  "available": false,
  "conflicts": [
    {
      "type": "BOOKING",
      "id": 1,
      "startDate": "2026-10-10",
      "endDate": "2026-10-15",
      "status": "APPROVED",
      "projectName": "Hyderabad Road Development Project"
    }
  ]
}
```

#### 2.2 List Machinery in Marketplace
- **Endpoint:** `GET /api/machinery?type=JCB&location=Hyderabad&startDate=2026-10-10&endDate=2026-10-15`
- **Auth:** Bearer Token
- **Response (200 OK):** Returns matching machines enriched with `is_available_for_dates`.

---

### 3. Machinery Booking & Transactional Approval APIs

#### 3.1 Request Machinery Booking
- **Endpoint:** `POST /api/machinery-bookings`
- **Auth:** CONTRACTOR, ADMIN
- **Request Body:**
```json
{
  "machinery_id": 1,
  "project_id": 1,
  "site_id": 1,
  "start_date": "2026-10-10",
  "end_date": "2026-10-19",
  "operator_required": true,
  "transport_charge": 4000,
  "additional_charge": 1000
}
```
- **Response (201 Created):**
```json
{
  "success": true,
  "message": "Booking request submitted successfully. Awaiting machinery owner approval.",
  "booking": {
    "bookingId": 2,
    "totalDays": 10,
    "dailyRate": 5000,
    "operatorCharge": 8000,
    "transportCharge": 4000,
    "additionalCharge": 1000,
    "totalAmount": 63000,
    "status": "PENDING"
  }
}
```
- **Conflict Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "This machinery is already booked during the selected dates."
}
```

#### 3.2 Approve Booking (Transactional)
- **Endpoint:** `PUT /api/machinery-bookings/:id/approve`
- **Auth:** OWNER, ADMIN
- **Response (200 OK):**
```json
{
  "success": true,
  "message": "Booking approved successfully. Machine assigned to project and rental expense recorded.",
  "result": {
    "bookingId": 2,
    "status": "APPROVED",
    "totalAmount": 63000
  }
}
```

#### 3.3 Reject Booking
- **Endpoint:** `PUT /api/machinery-bookings/:id/reject`
- **Auth:** OWNER, ADMIN
- **Request Body:**
```json
{
  "rejection_reason": "Machinery undergoing scheduled hydraulic cylinder replacement."
}
```

---

### 4. Executive Dashboard & Analytical Reports

#### 4.1 Dashboard Summary
- **Endpoint:** `GET /api/dashboard/summary`
- **Auth:** Bearer Token
- **Response (200 OK):**
```json
{
  "success": true,
  "summary": {
    "activeProjects": 3,
    "activeSites": 3,
    "totalWorkers": 7,
    "availableMachinery": 8,
    "pendingBookings": 1,
    "totalBudget": 66000000,
    "totalExpenses": 3483700,
    "remainingBudget": 62516300,
    "budgetUtilization": 5.28
  }
}
```

#### 4.2 Comprehensive Project Report
- **Endpoint:** `GET /api/reports/project/:projectId`
- **Auth:** Bearer Token
- **Response (200 OK):** Delivers project financials, sites, tasks, machinery deployments, and category breakdown.
