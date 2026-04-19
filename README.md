# EMR Full-Stack Project (React + Spring Boot + MySQL)

This repo contains a runnable Electronic Medical Records (EMR) system:

- Backend: `emr-backend/` (Spring Boot REST API, JWT auth, role-based access)
- Frontend: `emr-frontend/` (React + Vite, dashboards & working forms)
- Database scripts: `db/` (MySQL schema + seed)

## Folder Structure

- `emr-backend/` Spring Boot API
- `emr-frontend/` React UI
- `db/schema.sql` MySQL schema (deliverable)
- `db/seed.sql` MySQL demo seed (deliverable)
- `docker-compose.yml` optional MySQL runner

## Demo Accounts

Seeded users (password: `Password123!`):

- Admin: `admin1` (or `admin1@emr.local`)
- Doctor: `doctor1` (or `doctor1@emr.local`)
- Nurse: `nurse1` (or `nurse1@emr.local`)
- Lab Tech: `labtech1` (or `labtech1@emr.local`)
- Patient: `patient1` (or `patient1@emr.local`)

Note: the seed inserts demo passwords as plaintext for readability; the backend auto-hashes them on startup.

## Running Locally

### 1) Start MySQL

Option A (recommended): Docker

1. Install Docker Desktop
2. From repo root:
   - `docker compose up -d`

Option B: Local MySQL

1. Create a database named `emr_db`
2. Run:
   - `db/schema.sql`
   - `db/seed.sql`
3. If you previously ran an older schema, drop/recreate `emr_db` first (new columns include `User.username`, `two_factor_enabled`, SSN fields, and `Lab_Technician`).

### 2) Start the Backend (Spring Boot)

Prereqs: Java 17+.

1. Edit DB settings if needed:
   - `emr-backend/src/main/resources/application.properties`
2. Run:
   - `cd emr-backend`
   - `.\mvnw.cmd spring-boot:run` (Windows, no Maven install needed)
   - or `mvn spring-boot:run` (if you already installed Maven)

Backend runs on `http://localhost:8080`.

### 3) Start the Frontend (React + Vite)

Prereqs: Node 18+.

1. Configure API URL (optional):
   - Copy `emr-frontend/.env.example` to `emr-frontend/.env`
   - Edit `VITE_API_BASE_URL` if your backend host/port differs
2. Run:
   - `cd emr-frontend`
   - `npm install`
   - `npm run dev`

Frontend runs on `http://localhost:5173`.

## Troubleshooting

### `npm` blocked in PowerShell (scripts disabled)

Use the `.cmd` shim:

- `npm.cmd install`
- `npm.cmd run dev`

Or change PowerShell execution policy for your user:

- `Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned`

### Maven can’t download dependencies (`Permission denied` / socket error)

If `.\mvnw.cmd` fails to download from Maven Central, your environment is blocking Java from making outbound HTTPS connections.

Try these fixes:

- Windows Firewall: allow outbound for `%JAVA_HOME%\bin\java.exe` (Java(TM) Platform SE binary).
- Antivirus / school security tools: allow Java to access the network.
- Proxy networks: configure Maven proxy in `%USERPROFILE%\.m2\settings.xml` (ask your IT/professor for the proxy host/port).
- If you’re on campus Wi‑Fi, try a personal hotspot/VPN to confirm it’s a network policy issue.

### Backend fails with `Access denied for user 'root'@'localhost'`

Your MySQL username/password in `emr-backend/src/main/resources/application.properties` doesn’t match your local MySQL.

Fix options:

- Use Docker MySQL (matches the defaults in `docker-compose.yml`):
  - `docker compose up -d`
- Or set env vars before running the backend (PowerShell example):
  - `$env:DB_USER='root'`
  - `$env:DB_PASSWORD='your_real_mysql_password'`
  - `.\mvnw.cmd spring-boot:run`
- Or edit the file directly:
  - `emr-backend/src/main/resources/application.properties` (`spring.datasource.username` / `spring.datasource.password`)

## Where To Change Ports / Credentials

- Backend port: `emr-backend/src/main/resources/application.properties` (`server.port`)
- Backend DB username/password/url: `emr-backend/src/main/resources/application.properties` (`spring.datasource.*`)
- Frontend API base URL:
  - `emr-frontend/.env` (`VITE_API_BASE_URL`) or `emr-frontend/src/env.ts`
- Frontend dev port: `emr-frontend/vite.config.ts` (`server.port`)

## Frontend <-> Backend Connection Notes

- The frontend calls the backend REST API using `fetch` in `emr-frontend/src/lib/http.ts`.
- On successful login, the backend returns:
  - a JWT (`token`)
  - the current user (`me`) including the `role`
- Login accepts `identifier` = username or email.
- The frontend stores the JWT in `localStorage` and sends it as `Authorization: Bearer <token>` on API calls.
- CORS is enabled for `http://localhost:5173` in `emr-backend/src/main/java/com/emr/config/CorsConfig.java`.

## API Route List (Main)

Base URL: `http://localhost:8080/api`

Auth
- `POST /auth/login`
- `POST /auth/register` (creates a PATIENT account)
- `POST /auth/reset-password`

Users / Roles (Admin)
- `GET /users/me`
- `GET /users`
- `POST /users`
- `PUT /users/{userId}/role`
- `PUT /users/{userId}/2fa` (enable/disable 2FA; returns secret/otpauth URI when enabling)

Doctors
- `GET /doctors`

Patients
- `GET /patients/my-profile` (Patient)
- `PUT /patients/my-profile` (Patient)
- `GET /patients/{patientId}` (Doctor/Nurse/Admin)
- `GET /patients/search?q=...` (Doctor/Nurse/Admin)

Appointments
- `GET /appointments/my`
- `POST /appointments` (Patient)
- `PUT /appointments/{appointmentId}/status` (Doctor/Nurse/Admin)

Prescriptions
- `GET /prescriptions/my` (Patient)
- `GET /prescriptions/patient/{patientId}` (Doctor/Nurse/Admin)
- `POST /prescriptions/patient/{patientId}` (Doctor)
- `PUT /prescriptions/{prescriptionId}/status` (Doctor/Nurse/Admin)

Billing
- `GET /billing/my` (Patient)
- `GET /billing/patient/{patientId}` (Doctor/Nurse/Admin)
- `POST /billing` (Admin/Nurse)
- `PUT /billing/{billingId}/status` (Admin/Nurse)

Insurance
- `GET /insurance/my` (Patient)
- `GET /insurance/patient/{patientId}` (Doctor/Nurse/Admin)
- `POST /insurance/patient/{patientId}` (Admin/Nurse)

Medical History
- `GET /medical-history/my` (Patient)
- `GET /medical-history/patient/{patientId}` (Doctor/Nurse/Admin)
- `POST /medical-history/patient/{patientId}` (Doctor/Nurse/Admin)

Test Results
- `GET /test-results/my` (Patient)
- `GET /test-results/patient/{patientId}` (Doctor/Nurse/Admin)
- `POST /test-results/patient/{patientId}` (Doctor/Nurse/Admin)

Access Logs (Admin)
- `GET /access-logs`

Reports (Admin)
- `GET /reports/summary`
- `GET /reports/appointments-by-doctor`
- `GET /reports/billing-status`
- `GET /reports/access-log-actions`
