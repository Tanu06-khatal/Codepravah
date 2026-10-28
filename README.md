# APAAR Verify: AICTE Student Data Management Platform
**Problem Statement ID:** 26263
**Title:** Development of an APAAR ID-Based Student Data Management and Verification Platform for AICTE-Approved Institutions
**Organization:** All India Council for Technical Education (AICTE), Ministry of Education, Government of India
**Department:** Regulation Bureau
**Architecture:** The Classic Indian e-Governance Stack (Java 21 + Spring Boot 3 + Angular 21 + Angular Material + Excel Report Engine + Secure Event Stream)

---

## 🌐 Live Production Deployments

| Component | Cloud Platform | Live Production URL | Health Check / Status |
| :--- | :--- | :--- | :---: |
| **Frontend Portal** | Render Static Cloud | **[https://codepravah-frontend.onrender.com](https://codepravah-frontend.onrender.com)** | 🟢 `Active (200 OK)` |
| **Backend REST API** | Render Docker Cloud | **[https://codepravah.onrender.com](https://codepravah.onrender.com)** | 🟢 `Active (200 OK)` |
| **API Health Endpoint** | Spring Boot 3 | `https://codepravah.onrender.com/api/aicte/stats` | 🟢 `Live JSON Feed` |

---

## 🏛️ System Overview

**APAAR Verify** is the national-scale academic data verification and intake auditing platform mandated by the **National Education Policy (NEP 2020)**. It establishes the 12-digit **APAAR ID (Automated Permanent Academic Account Registry)** as the single verifiable truth for technical higher education.

### Key Functional Capabilities
1. **Intake Cap Anomaly Interception:** Reconciles institutional admissions against sanctioned AICTE intake caps (detecting over-enrolment violations in real time).
2. **Statutory Quota Auditing:** Audits Supernumerary seats (Tuition Fee Waiver - TFW), NRI, OCI, and Foreign National (FN) allocations.
3. **Cross-Institution Duplicate Detection:** Publishes duplicate seat conflicts across state boundaries through real-time message events (secure event stream).
4. **Verifiable Academic Credential Pass:** Generates digital credentials with cryptographic SHA-256 QR seals displaying dual APAAR ID and Student ID imprints, including the student photograph.
5. **Government-Standard Reporting:** Dynamic server-side spreadsheet generation (`.xlsx`) with report title, generation timestamp, totals row and audit formatting.

### Platform Features
- **Bilingual Interface:** The entire platform (all portals, tables, bots and reports) switches instantly between English and हिन्दी using the header language toggle.
- **Role-Isolated Portals:** When you enter a portal (Student / College / AICTE), only that portal is visible in the navigation — no links or names of other portals are exposed.
- **Google Sign-In for any account:** College admins can continue with any Google account.
- **Real Registration:** Newly registered institutions are provisioned in the database with a clean registry (zero enrolments) and import their real students via CSV.
- **Live Database Statistics:** National KPIs, state-wise charts and the anomaly stream are computed live from the database — no static values.
- **AICTE Sahayak Helpdesk:** Trained on 20+ topic areas (APAAR, ABC credits, duplicates, quotas, registration, uploads, reports, statuses, DPDP consent, helpline and more), in both English and Hindi.

---

## 🚀 Running the Project

### Prerequisites
- **Java:** JDK 21 or higher (`java -version`)
- **Node.js:** v18.x, v20.x, or v22+ (`node -v`)

---

### Step 1: Start the Spring Boot Backend (Port 8085)
Open your terminal in the project root:
```bash
cd backend
java -jar target/apaar-verify-backend-1.0.0.jar
```
*The Spring Boot server initializes the H2 JPA database with seeded institutions and students, starts Tomcat on port `8085`, and prepares the Excel report engine.*

---

### Step 2: Start the Angular Frontend (Port 4200)
Open a second terminal window:
```bash
cd frontend
npm start
```
*The Angular development server will compile and host the application at **`http://localhost:4200`**.*

---

## 🧪 Step-by-Step Testing Guide

### 1. Student APAAR Verification Portal (`/student`)
1. Open **`http://localhost:4200/student`**.
2. Tick the DPDP statutory consent checkbox and enter a 12-digit APAAR ID (e.g. `123456789012` for a verified record, `100000000013` for pending, `100000000005` for a duplicate conflict).
3. Observe the verification lifecycle timeline and the **Official Verifiable Academic Credential Pass** displaying the student photograph, credentials, institutional details, and the cryptographic QR seal.
4. Use **Print Pass** for a properly formatted A4 report (application chrome is hidden automatically).
5. Switch the language from the header toggle — the full portal renders in हिन्दी.

---

### 2. College Admin Portal (`/college`)
1. Open **`http://localhost:4200/college`**.
2. Sign in with **Continue with Google** (any Google account) or institutional credentials:
   - **Institutional Email:** `college@aicte.ac.in`
   - **Passkey:** `123456`
3. Verify the **AICTE Regulatory Anomaly Alert** for enrolments exceeding sanctioned intake, and the four metric cards fed from the live database.
4. In the Student Registry table, use the search box (type a student **name**) and filters; click any column header to sort — the default order is ascending APAAR ID.
5. Click **Verify** / **Reject** on pending and duplicate records — changes persist to the database and all dashboards refresh.
6. Click **Bulk Upload CSV**: download the sample template, choose a real `.csv` file, then run batch validation. Import without selecting a file is rejected with an explicit error — success is reported only for genuinely imported rows.

**Registering a new institution:** switch to *Request Access / Register*, fill in the details, and submit. The institution is provisioned with a **clean registry (zero enrolments)** — no dummy data — and its admin imports real students via bulk upload.

---

### 3. AICTE Super Admin Portal (`/aicte`)
1. Open **`http://localhost:4200/aicte`**.
2. Check national KPIs — institutions, enrolments, verified records and duplicates — all computed live from the database.
3. Review the state-wise APAAR progress chart built from real student records.
4. Review the **College-Wise Intake Compliance Master Table** and the anomaly stream generated from actual duplicate/over-intake records.
5. Click **Export National Compliance Report (.xlsx)** for the formatted server-generated spreadsheet (title block, timestamp, totals row).
6. Inspect the **System Audit Ledger** with real audit logs from the database.

---

### 4. AICTE Sahayak (सहायक) AI Virtual Helpdesk
1. Click the floating **AICTE Sahayak** widget in the bottom-right corner of any screen.
2. Ask anything about APAAR ID, ABC credits, duplicate detection, intake quotas, institute registration, CSV uploads, reports, record statuses, DPDP consent or the helpline — or tap a quick question chip.
3. The assistant answers in English or Hindi following the active language.

---

## 🛠️ Automated Testing & Build Commands

### Frontend Production Build:
```bash
cd frontend
npm run build
```

### Backend REST API cURL Health Check:
```bash
# Student verification
curl -s http://localhost:8085/api/verify/123456789012

# College statistics & intake variance
curl -s http://localhost:8085/api/college/stats

# Live national statistics (database-computed)
curl -s http://localhost:8085/api/aicte/stats

# Excel report download
curl -O http://localhost:8085/api/aicte/export-excel
```

---

## 📁 Project Architecture

```text
├── backend/                  # Java 21 + Spring Boot 3 Backend
│   ├── src/main/java/        # SecurityConfig, Controllers, Services, Entities
│   │   └── gov/aicte/apaar/
│   │       ├── config/       # Spring Security 6 & CORS Configuration
│   │       ├── controller/   # Verification, College, AICTE REST Controllers
│   │       ├── model/        # JPA Entities (Student, College, AuditLog)
│   │       ├── repository/   # Spring Data JPA Repositories
│   │       └── service/      # ExcelReportService, EventPublisherService
│   ├── src/main/resources/   # application.properties (Port 8085, H2 JPA DB)
│   ├── pom.xml               # Spring Boot 3, Spring Security, POI Excel, JPA
│   └── target/               # Fat JAR (apaar-verify-backend-1.0.0.jar)
├── frontend/                 # Angular 21 + Angular Material Frontend
│   ├── src/app/
│   │   ├── pages/            # Landing, Student, College, AICTE Components
│   │   ├── services/         # ApaarService (HTTP), LanguageService (EN/HI)
│   │   ├── app.routes.ts     # Client routing configuration
│   │   └── app.ts            # Root component with Header, Footer, and AICTE Sahayak
│   ├── src/styles.css        # Official AICTE Ministry Design System + print rules
│   └── package.json          # Angular 21, Angular Material, Zone.js
└── README.md                 # Complete project documentation & testing manual
```
