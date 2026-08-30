-- ================================================================
-- SUNRISE DENTAL CLINIC - DATABASE SETUP SCRIPT
-- Database : sunrise_dental_db
-- Version  : 1.0.0
-- Author   : Sunrise Dental Clinic System
-- ================================================================

DROP DATABASE IF EXISTS sunrise_dental_db;
CREATE DATABASE sunrise_dental_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sunrise_dental_db;

-- ----------------------------------------------------------------
-- TABLE 1: treatments
-- ----------------------------------------------------------------
CREATE TABLE treatments (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    treatment_code  VARCHAR(20)     NOT NULL UNIQUE,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    base_cost       DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    duration_mins   INT             NOT NULL DEFAULT 30,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- TABLE 2: users
-- ----------------------------------------------------------------
CREATE TABLE users (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    user_code       VARCHAR(20)     NOT NULL UNIQUE,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password_hash   VARCHAR(64)     NOT NULL,
    full_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(100)    UNIQUE,
    contact         VARCHAR(15),
    role            ENUM('ADMIN','RECEPTIONIST','DENTIST') NOT NULL DEFAULT 'RECEPTIONIST',
    specialization  VARCHAR(100),
    consult_fee     DECIMAL(10,2)   DEFAULT 1500.00,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    last_login      TIMESTAMP       NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- TABLE 3: patients
-- ----------------------------------------------------------------
CREATE TABLE patients (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    patient_code    VARCHAR(20)     NOT NULL UNIQUE,
    full_name       VARCHAR(100)    NOT NULL,
    date_of_birth   DATE,
    gender          ENUM('Male','Female','Other'),
    address         TEXT,
    contact         VARCHAR(15)     NOT NULL,
    email           VARCHAR(100),
    blood_type      ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-','Unknown') DEFAULT 'Unknown',
    allergies       TEXT,
    medical_notes   TEXT,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    registered_by   INT,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (registered_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ----------------------------------------------------------------
-- TABLE 4: appointments
-- ----------------------------------------------------------------
CREATE TABLE appointments (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    apt_code        VARCHAR(25)     NOT NULL UNIQUE,
    patient_id      INT             NOT NULL,
    dentist_id      INT             NOT NULL,
    treatment_id    INT             NOT NULL,
    apt_date        DATE            NOT NULL,
    apt_time        TIME            NOT NULL,
    status          ENUM('Scheduled','In Progress','Completed','Cancelled') NOT NULL DEFAULT 'Scheduled',
    notes           TEXT,
    created_by      INT,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)   REFERENCES patients(id)   ON DELETE RESTRICT,
    FOREIGN KEY (dentist_id)   REFERENCES users(id)      ON DELETE RESTRICT,
    FOREIGN KEY (treatment_id) REFERENCES treatments(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by)   REFERENCES users(id)      ON DELETE SET NULL
);

-- ----------------------------------------------------------------
-- TABLE 5: bills
-- ----------------------------------------------------------------
CREATE TABLE bills (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    bill_code       VARCHAR(20)     NOT NULL UNIQUE,
    appointment_id  INT             NOT NULL UNIQUE,
    treatment_fee   DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    consult_fee     DECIMAL(10,2)   NOT NULL DEFAULT 1500.00,
    discount        DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    payment_method  ENUM('Cash','Card','Online Transfer','QR Payment') DEFAULT 'Cash',
    status          ENUM('Pending','Paid','Cancelled') NOT NULL DEFAULT 'Pending',
    settled_by      INT,
    settled_at      TIMESTAMP       NULL,
    created_by      INT,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE RESTRICT,
    FOREIGN KEY (settled_by)     REFERENCES users(id)        ON DELETE SET NULL,
    FOREIGN KEY (created_by)     REFERENCES users(id)        ON DELETE SET NULL
);

-- ================================================================
-- INDEXES
-- ================================================================
CREATE INDEX idx_apt_date       ON appointments(apt_date);
CREATE INDEX idx_apt_status     ON appointments(status);
CREATE INDEX idx_apt_patient    ON appointments(patient_id);
CREATE INDEX idx_apt_dentist    ON appointments(dentist_id);
CREATE INDEX idx_pat_name       ON patients(full_name);
CREATE INDEX idx_pat_contact    ON patients(contact);
CREATE INDEX idx_bill_status    ON bills(status);

-- ================================================================
-- ADVANCED DATABASE FEATURES: Functions, Procedures & Triggers
--
-- These enforce/derive the same business rules the Java service
-- layer already applies (double-booking prevention, no past-dated
-- new bookings, bill total calculation), as a database-level safety
-- net that holds even if a row is ever inserted/updated outside the
-- application. They are additive: none of them change the result of
-- any currently-working application code path.
-- ================================================================

-- ── Function: bill total calculation ───────────────────────────
-- Mirrors Bill.calculateTotal() / BillService.calculateTotal() in Java.
DELIMITER $$
CREATE FUNCTION fn_calculate_bill_total(
    p_treatment_fee DECIMAL(10,2),
    p_consult_fee   DECIMAL(10,2),
    p_discount      DECIMAL(10,2)
) RETURNS DECIMAL(10,2)
DETERMINISTIC
NO SQL
BEGIN
    RETURN GREATEST(0, (p_treatment_fee + p_consult_fee) - p_discount);
END$$
DELIMITER ;

-- ── Trigger: keep bills.total_amount consistent ────────────────
-- Recomputes total_amount from treatment_fee/consult_fee/discount on
-- every insert/update, using the function above, so the stored total
-- can never drift out of sync with its components.
DELIMITER $$
CREATE TRIGGER trg_bills_total_ins
BEFORE INSERT ON bills
FOR EACH ROW
BEGIN
    SET NEW.total_amount = fn_calculate_bill_total(NEW.treatment_fee, NEW.consult_fee, NEW.discount);
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_bills_total_upd
BEFORE UPDATE ON bills
FOR EACH ROW
BEGIN
    SET NEW.total_amount = fn_calculate_bill_total(NEW.treatment_fee, NEW.consult_fee, NEW.discount);
END$$
DELIMITER ;

-- ── Trigger: block newly-scheduled appointments in the past ────
-- Only applies to NEW 'Scheduled' rows (as created by the booking
-- flow) - historical 'Completed'/'Cancelled' records legitimately
-- have past dates, so they are left untouched.
DELIMITER $$
CREATE TRIGGER trg_appointments_no_past_date
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    IF NEW.status = 'Scheduled' AND NEW.apt_date < CURDATE() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'A newly scheduled appointment cannot be booked in the past.';
    END IF;
END$$
DELIMITER ;

-- ── Triggers: double-booking prevention at the database level ──
-- Backstops AppointmentDAO.isDentistBooked(), which is checked in
-- Java before the INSERT/UPDATE is issued - this closes the small
-- race-condition window between that check and the write.
DELIMITER $$
CREATE TRIGGER trg_appointments_no_double_booking_ins
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    DECLARE conflict_count INT;

    IF NEW.status != 'Cancelled' THEN
        SELECT COUNT(*) INTO conflict_count
        FROM appointments
        WHERE dentist_id = NEW.dentist_id
          AND apt_date   = NEW.apt_date
          AND apt_time   = NEW.apt_time
          AND status    != 'Cancelled';

        IF conflict_count > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'This dentist already has an appointment at that date and time.';
        END IF;
    END IF;
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_appointments_no_double_booking_upd
BEFORE UPDATE ON appointments
FOR EACH ROW
BEGIN
    DECLARE conflict_count INT;

    IF NEW.status != 'Cancelled' THEN
        SELECT COUNT(*) INTO conflict_count
        FROM appointments
        WHERE dentist_id = NEW.dentist_id
          AND apt_date   = NEW.apt_date
          AND apt_time   = NEW.apt_time
          AND status    != 'Cancelled'
          AND id        != NEW.id;

        IF conflict_count > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'This dentist already has an appointment at that date and time.';
        END IF;
    END IF;
END$$
DELIMITER ;

-- ── Stored Procedure: dentist workload report ───────────────────
-- Summarizes one dentist's appointment counts and paid revenue over
-- a date range. Called from Java via ReportDAO.getDentistWorkload()
-- (JDBC CallableStatement) and exposed at
-- GET /api/reports/dentist-workload/{id} (admin only).
DELIMITER $$
CREATE PROCEDURE sp_dentist_workload(
    IN p_dentist_id INT,
    IN p_start_date DATE,
    IN p_end_date   DATE
)
READS SQL DATA
BEGIN
    SELECT
        u.full_name AS dentist_name,
        COUNT(a.id) AS total_appointments,
        SUM(CASE WHEN a.status = 'Completed'   THEN 1 ELSE 0 END) AS completed,
        SUM(CASE WHEN a.status = 'Scheduled'    THEN 1 ELSE 0 END) AS scheduled,
        SUM(CASE WHEN a.status = 'In Progress'  THEN 1 ELSE 0 END) AS in_progress,
        SUM(CASE WHEN a.status = 'Cancelled'    THEN 1 ELSE 0 END) AS cancelled,
        COALESCE(SUM(CASE WHEN a.status = 'Completed' THEN b.total_amount ELSE 0 END), 0) AS revenue_generated
    FROM users u
    LEFT JOIN appointments a
           ON a.dentist_id = u.id
          AND a.apt_date BETWEEN p_start_date AND p_end_date
    LEFT JOIN bills b
           ON b.appointment_id = a.id
          AND b.status = 'Paid'
    WHERE u.id = p_dentist_id
    GROUP BY u.id, u.full_name;
END$$
DELIMITER ;

-- ================================================================
-- SEED DATA: Treatments
-- ================================================================
INSERT INTO treatments (treatment_code, name, description, base_cost, duration_mins) VALUES
('TRT-001', 'General Consultation',     'Initial patient consultation and oral examination',        1500.00,  30),
('TRT-002', 'Tooth Extraction',         'Simple or surgical removal of a damaged tooth',            3500.00,  45),
('TRT-003', 'Root Canal Treatment',     'Complete root canal therapy for infected tooth pulp',      15000.00, 90),
('TRT-004', 'Dental Filling',           'Composite or amalgam restoration for decayed tooth',       4000.00,  45),
('TRT-005', 'Teeth Cleaning',           'Professional dental scaling and polishing procedure',      2500.00,  60),
('TRT-006', 'Teeth Whitening',          'In-clinic professional teeth whitening treatment',         12000.00, 60),
('TRT-007', 'Dental Crown',             'Porcelain or metal dental crown fitting and placement',    18000.00, 120),
('TRT-008', 'Gum Treatment',            'Periodontal treatment for gum disease management',         6000.00,  60),
('TRT-009', 'Orthodontic Consultation', 'Braces and teeth alignment assessment consultation',       5000.00,  45),
('TRT-010', 'Dental Implant',           'Titanium implant surgery for missing tooth replacement',   85000.00, 180),
('TRT-011', 'Dentures',                 'Full or partial removable denture fabrication and fitting', 25000.00, 90),
('TRT-012', 'Dental X-Ray',             'Periapical or panoramic dental X-Ray imaging',             2000.00,  15);

-- ================================================================
-- SEED DATA: Users
-- Password hash values (SHA-256, verified against SecurityUtil.hashPassword):
--   admin123     = 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
--   reception123 = 5145dba3b6bda2d610d2c5c435a1c2481eefd3146b6a7e004ad73f794386e031
--   dentist123   = 22990c57fbef2aeac16a2bf5e0caeafc43717c99e2040b0e3ac8d468d42794f0
-- NOTE: the previous hashes in this file were malformed (wrong length / wrong
-- value), so none of the documented demo accounts could ever log in.
-- ================================================================

INSERT INTO users (user_code, username, password_hash, full_name, email, contact, role, consult_fee) VALUES
(
    'USR-001',
    'admin',
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    'System Administrator',
    'admin@sunrisedental.lk',
    '0112345678',
    'ADMIN',
    0.00
);

INSERT INTO users (user_code, username, password_hash, full_name, email, contact, role, consult_fee) VALUES
(
    'USR-002',
    'receptionist',
    '5145dba3b6bda2d610d2c5c435a1c2481eefd3146b6a7e004ad73f794386e031',
    'Sarah Fernando',
    'sarah@sunrisedental.lk',
    '0112345679',
    'RECEPTIONIST',
    0.00
);

INSERT INTO users (user_code, username, password_hash, full_name, email, contact, role, specialization, consult_fee) VALUES
(
    'USR-003',
    'dentist',
    '22990c57fbef2aeac16a2bf5e0caeafc43717c99e2040b0e3ac8d468d42794f0',
    'Dr. Priya Sharma',
    'priya@sunrisedental.lk',
    '0112345680',
    'DENTIST',
    'General Dentistry',
    1500.00
);

INSERT INTO users (user_code, username, password_hash, full_name, email, contact, role, specialization, consult_fee) VALUES
(
    'USR-004',
    'dr.perera',
    '22990c57fbef2aeac16a2bf5e0caeafc43717c99e2040b0e3ac8d468d42794f0',
    'Dr. Kamal Perera',
    'kamal@sunrisedental.lk',
    '0112345681',
    'DENTIST',
    'Orthodontics',
    2000.00
);

INSERT INTO users (user_code, username, password_hash, full_name, email, contact, role, specialization, consult_fee) VALUES
(
    'USR-005',
    'dr.silva',
    '22990c57fbef2aeac16a2bf5e0caeafc43717c99e2040b0e3ac8d468d42794f0',
    'Dr. Amali Silva',
    'amali@sunrisedental.lk',
    '0112345682',
    'DENTIST',
    'Periodontics',
    1800.00
);

-- ================================================================
-- SEED DATA: Patients
-- ================================================================
INSERT INTO patients (patient_code, full_name, date_of_birth, gender, address, contact, email, blood_type, registered_by) VALUES
('PAT-2025-0001', 'Nimal Perera',     '1985-03-15', 'Male',   '12 Galle Road, Colombo 03',     '0771234567', 'nimal@email.com',   'B+',      1),
('PAT-2025-0002', 'Kamala Fernando',  '1990-07-22', 'Female', '45 Kandy Road, Colombo 10',     '0777654321', 'kamala@email.com',  'A+',      2),
('PAT-2025-0003', 'Ruwan Jayasinghe', '1978-11-08', 'Male',   '78 Marine Drive, Colombo 06',   '0712345678', 'ruwan@email.com',   'O+',      2),
('PAT-2025-0004', 'Dilani Wijeratne', '1995-04-30', 'Female', '23 Baseline Road, Colombo 09',  '0751234567', 'dilani@email.com',  'AB+',     1),
('PAT-2025-0005', 'Saman Kumara',     '1982-09-12', 'Male',   '56 Nugegoda Road, Nugegoda',    '0761234567', 'saman@email.com',   'A-',      2);

-- ================================================================
-- SEED DATA: Appointments
-- ================================================================
INSERT INTO appointments (apt_code, patient_id, dentist_id, treatment_id, apt_date, apt_time, status, notes, created_by) VALUES
('APT-20250115-0001', 1, 3, 5,  '2025-01-15', '09:00:00', 'Completed',  'Routine cleaning completed successfully.',   2),
('APT-20250115-0002', 2, 4, 9,  '2025-01-15', '10:00:00', 'Completed',  'Orthodontic consultation completed.',        2),
('APT-20250116-0001', 3, 3, 2,  '2025-01-16', '11:00:00', 'Completed',  'Lower left molar extracted successfully.',   2),
('APT-20250120-0001', 4, 5, 8,  '2025-01-20', '14:00:00', 'Completed',  'Gum treatment session 1 completed.',        1),
('APT-20250125-0001', 5, 3, 4,  '2025-01-25', '09:30:00', 'Completed',  'Composite filling on tooth #14 done.',      2);

-- ================================================================
-- SEED DATA: Bills
-- ================================================================
INSERT INTO bills (bill_code, appointment_id, treatment_fee, consult_fee, discount, total_amount, payment_method, status, settled_by, settled_at, created_by) VALUES
('BILL-2025-0001', 1, 2500.00, 1500.00, 0.00,   4000.00, 'Cash',            'Paid', 2, '2025-01-15 09:45:00', 2),
('BILL-2025-0002', 2, 5000.00, 2000.00, 0.00,   7000.00, 'Card',            'Paid', 2, '2025-01-15 10:50:00', 2),
('BILL-2025-0003', 3, 3500.00, 1500.00, 0.00,   5000.00, 'Cash',            'Paid', 2, '2025-01-16 12:00:00', 2),
('BILL-2025-0004', 4, 6000.00, 1800.00, 500.00, 7300.00, 'Online Transfer', 'Paid', 1, '2025-01-20 15:00:00', 1),
('BILL-2025-0005', 5, 4000.00, 1500.00, 0.00,   5500.00, 'QR Payment',      'Paid', 2, '2025-01-25 10:15:00', 2);

-- ================================================================
-- SEED DATA: Additional Patients (for demo variety)
-- ================================================================
INSERT INTO patients (patient_code, full_name, date_of_birth, gender, address, contact, email, blood_type, registered_by) VALUES
('PAT-2025-0006', 'Anushka Rathnayake',  '1988-02-10', 'Female', '14 Havelock Road, Colombo 05',    '0712223344', 'anushka@email.com',   'B-',  1),
('PAT-2025-0007', 'Chamara Bandara',     '1975-06-25', 'Male',   '9 Duplication Road, Colombo 04',  '0723334455', 'chamara@email.com',   'O-',  2),
('PAT-2025-0008', 'Ishara Gunasekara',   '2000-01-18', 'Female', '31 Union Place, Colombo 02',      '0734445566', 'ishara@email.com',    'AB-', 2),
('PAT-2025-0009', 'Nuwan Wickramasinghe','1992-09-05', 'Male',   '5 Ward Place, Colombo 07',        '0745556677', 'nuwan@email.com',     'A+',  1),
('PAT-2025-0010', 'Tharindu Senanayake', '1983-12-30', 'Male',   '22 Reid Avenue, Colombo 07',      '0756667788', 'tharindu@email.com',  'O+',  2);

-- ================================================================
-- SEED DATA: Live/relative activity
--
-- Uses CURDATE()/CURTIME() instead of fixed literal dates so the
-- dashboard, the real-time dentist availability badges and the
-- appointment-reminder feature always have relevant data to show,
-- no matter what actual calendar date this script is run on.
-- ================================================================

-- Today: one completed earlier, one dentist currently "In Progress"
-- (so the availability badge has something real to show immediately
-- after setup), and one still scheduled for later today.
INSERT INTO appointments (apt_code, patient_id, dentist_id, treatment_id, apt_date, apt_time, status, notes, created_by) VALUES
(CONCAT('APT-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-9001'), 6, 3, 5, CURDATE(), '08:30:00',                        'Completed',   'Morning cleaning completed.',        2),
(CONCAT('APT-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-9002'), 7, 4, 9, CURDATE(), SUBTIME(CURTIME(), '00:15:00'),    'In Progress', 'Orthodontic consultation ongoing.',  2),
(CONCAT('APT-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-9003'), 8, 5, 8, CURDATE(), ADDTIME(CURTIME(), '02:00:00'),    'Scheduled',   NULL,                                 1);

-- Tomorrow: scheduled appointments for the reminder-email feature to
-- find and notify.
INSERT INTO appointments (apt_code, patient_id, dentist_id, treatment_id, apt_date, apt_time, status, notes, created_by) VALUES
(CONCAT('APT-', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '%Y%m%d'), '-9001'), 9,  3, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', 'Scheduled', NULL, 2),
(CONCAT('APT-', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '%Y%m%d'), '-9002'), 10, 4, 9, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30:00', 'Scheduled', NULL, 2),
(CONCAT('APT-', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '%Y%m%d'), '-9003'), 6,  5, 8, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', 'Scheduled', NULL, 1);

-- Last 4 months: completed appointments spread across time so the
-- revenue-trend chart on the Reports page has real month-to-month
-- variation instead of a single flat data point.
INSERT INTO appointments (apt_code, patient_id, dentist_id, treatment_id, apt_date, apt_time, status, notes, created_by) VALUES
(CONCAT('APT-', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y%m%d'), '-8001'), 6,  3, 5,  DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '09:00:00', 'Completed', 'Routine check.',        2),
(CONCAT('APT-', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y%m%d'), '-8002'), 7,  4, 9,  DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '11:00:00', 'Completed', 'Braces consultation.',  2),
(CONCAT('APT-', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '%Y%m%d'), '-8001'), 8,  5, 8,  DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '10:00:00', 'Completed', 'Gum treatment session.',1),
(CONCAT('APT-', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '%Y%m%d'), '-8002'), 9,  3, 2,  DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '13:00:00', 'Completed', 'Tooth extraction.',     2),
(CONCAT('APT-', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '%Y%m%d'), '-8001'), 10, 4, 7,  DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '09:30:00', 'Completed', 'Crown fitting.',        2),
(CONCAT('APT-', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '%Y%m%d'), '-8002'), 6,  5, 4,  DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '15:00:00', 'Completed', 'Filling done.',         1),
(CONCAT('APT-', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '%Y%m%d'), '-8001'), 7,  3, 6,  DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '10:30:00', 'Completed', 'Whitening session.',    2),
(CONCAT('APT-', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '%Y%m%d'), '-8002'), 8,  4, 12, DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '16:00:00', 'Completed', 'X-Ray imaging.',        2);

-- Bills for the newly-completed appointments above (appointment ids
-- 6, and 12-19, following on from the original 5 appointments).
-- total_amount is recalculated by trg_bills_total_ins regardless of
-- the value supplied here, so these are guaranteed self-consistent.
INSERT INTO bills (bill_code, appointment_id, treatment_fee, consult_fee, discount, total_amount, payment_method, status, settled_by, settled_at, created_by) VALUES
('BILL-DEMO-9001', 6,  2500.00, 1500.00, 0.00, 4000.00,  'Cash',            'Paid', 2, NOW(),                                             2),
('BILL-DEMO-9002', 12, 2500.00, 1500.00, 0.00, 4000.00,  'Card',            'Paid', 2, DATE_SUB(NOW(), INTERVAL 1 MONTH),                 2),
('BILL-DEMO-9003', 13, 5000.00, 2000.00, 0.00, 7000.00,  'Online Transfer', 'Paid', 2, DATE_SUB(NOW(), INTERVAL 1 MONTH),                 2),
('BILL-DEMO-9004', 14, 6000.00, 1800.00, 0.00, 7800.00,  'Cash',            'Paid', 1, DATE_SUB(NOW(), INTERVAL 2 MONTH),                 1),
('BILL-DEMO-9005', 15, 3500.00, 1500.00, 0.00, 5000.00,  'QR Payment',      'Paid', 2, DATE_SUB(NOW(), INTERVAL 2 MONTH),                 2),
('BILL-DEMO-9006', 16, 18000.00,2000.00, 0.00, 20000.00, 'Card',            'Paid', 2, DATE_SUB(NOW(), INTERVAL 3 MONTH),                 2),
('BILL-DEMO-9007', 17, 4000.00, 1800.00, 0.00, 5800.00,  'Cash',            'Paid', 1, DATE_SUB(NOW(), INTERVAL 3 MONTH),                 1),
('BILL-DEMO-9008', 18, 12000.00,1500.00, 0.00, 13500.00, 'Online Transfer', 'Paid', 2, DATE_SUB(NOW(), INTERVAL 4 MONTH),                 2),
('BILL-DEMO-9009', 19, 2000.00, 2000.00, 0.00, 4000.00,  'Card',            'Paid', 2, DATE_SUB(NOW(), INTERVAL 4 MONTH),                 2);

-- ================================================================
-- VERIFICATION
-- ================================================================
SELECT '✅ DATABASE SETUP COMPLETE!' AS Result;
SELECT COUNT(*) AS Total_Treatments  FROM treatments;
SELECT COUNT(*) AS Total_Users       FROM users;
SELECT COUNT(*) AS Total_Patients    FROM patients;
SELECT COUNT(*) AS Total_Appointments FROM appointments;
SELECT COUNT(*) AS Total_Bills       FROM bills;