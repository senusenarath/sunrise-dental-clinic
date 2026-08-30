package lk.sunrise.dental.dao;

import lk.sunrise.dental.model.Patient;
import lk.sunrise.dental.util.DBConnection;
import lk.sunrise.dental.util.DateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * PatientDAO.java
 * Data Access Object for Patient operations
 *
 * Handles all database operations related to patient records.
 * Package : lk.sunrise.dental.dao
 * ================================================================
 */
public class PatientDAO {

    // ──────────────────────────────────────────────────────────────
    // READ OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Get all active patients ordered by name.
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String sql = """
                SELECT p.*, u.full_name AS registered_by_name
                FROM patients p
                LEFT JOIN users u ON p.registered_by = u.id
                WHERE p.is_active = TRUE
                ORDER BY p.full_name
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                patients.add(mapPatient(rs));
            }

        } catch (Exception e) {
            System.err.println("[PatientDAO] getAllPatients error: " + e.getMessage());
        }
        return patients;
    }

    /**
     * Get patient by ID with full details.
     */
    public Patient getPatientById(int id) {
        String sql = """
                SELECT p.*, u.full_name AS registered_by_name
                FROM patients p
                LEFT JOIN users u ON p.registered_by = u.id
                WHERE p.id = ?
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapPatient(rs);
            }

        } catch (Exception e) {
            System.err.println("[PatientDAO] getPatientById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get patient by patient code.
     */
    public Patient getPatientByCode(String patientCode) {
        String sql = """
                SELECT p.*, u.full_name AS registered_by_name
                FROM patients p
                LEFT JOIN users u ON p.registered_by = u.id
                WHERE p.patient_code = ?
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patientCode);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapPatient(rs);
            }

        } catch (Exception e) {
            System.err.println("[PatientDAO] getPatientByCode error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Search patients by name, contact, or email.
     *
     * @param keyword search term
     * @return List of matching patients
     */
    public List<Patient> searchPatients(String keyword) {
        List<Patient> patients = new ArrayList<>();
        String sql = """
                SELECT p.*, u.full_name AS registered_by_name
                FROM patients p
                LEFT JOIN users u ON p.registered_by = u.id
                WHERE p.is_active = TRUE
                AND (
                    p.full_name    LIKE ? OR
                    p.contact      LIKE ? OR
                    p.email        LIKE ? OR
                    p.patient_code LIKE ?
                )
                ORDER BY p.full_name
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String term = "%" + keyword.trim() + "%";
            ps.setString(1, term);
            ps.setString(2, term);
            ps.setString(3, term);
            ps.setString(4, term);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                patients.add(mapPatient(rs));
            }

        } catch (Exception e) {
            System.err.println("[PatientDAO] searchPatients error: " + e.getMessage());
        }
        return patients;
    }

    /**
     * Get patient with appointment statistics.
     * Used for patient detail view.
     */
    public Patient getPatientWithStats(int id) {
        Patient patient = getPatientById(id);

        if (patient != null) {
            // Get appointment counts
            String sql = """
                    SELECT
                        COUNT(*) AS total_appointments,
                        SUM(CASE WHEN status = 'Completed'  THEN 1 ELSE 0 END) AS completed,
                        SUM(CASE WHEN status = 'Scheduled'
                             OR status = 'In Progress'       THEN 1 ELSE 0 END) AS active
                    FROM appointments
                    WHERE patient_id = ?
                    """;

            try (Connection conn = DBConnection.getConn();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    patient.setTotalAppointments(rs.getInt("total_appointments"));
                    patient.setCompletedTreatments(rs.getInt("completed"));
                    patient.setActiveBookings(rs.getInt("active"));
                }

            } catch (Exception e) {
                System.err.println("[PatientDAO] getPatientWithStats error: " + e.getMessage());
            }
        }

        return patient;
    }

    /**
     * Check if contact number already exists.
     */
    public boolean contactExists(String contact) {
        String sql = "SELECT COUNT(*) FROM patients WHERE contact = ? AND is_active = TRUE";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, contact);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt(1) > 0;

        } catch (Exception e) {
            System.err.println("[PatientDAO] contactExists error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if contact exists for another patient (for update).
     */
    public boolean contactExistsForOther(String contact, int excludeId) {
        String sql = "SELECT COUNT(*) FROM patients WHERE contact = ? AND id != ? AND is_active = TRUE";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, contact);
            ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt(1) > 0;

        } catch (Exception e) {
            System.err.println("[PatientDAO] contactExistsForOther error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Generate next patient code.
     * Format: PAT-2025-0001
     */
    public String generatePatientCode() {
        String year = DateUtil.getCurrentYear();
        String sql  = "SELECT COUNT(*) FROM patients WHERE patient_code LIKE ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "PAT-" + year + "-%");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("PAT-%s-%04d", year, count);
            }

        } catch (Exception e) {
            System.err.println("[PatientDAO] generatePatientCode error: " + e.getMessage());
        }
        return "PAT-" + year + "-0001";
    }

    // ──────────────────────────────────────────────────────────────
    // CREATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Register a new patient.
     *
     * @param patient Patient object with all details
     * @return generated patient ID if successful, -1 otherwise
     */
    public int createPatient(Patient patient) {
        String sql = """
                INSERT INTO patients
                (patient_code, full_name, date_of_birth, gender, address,
                 contact, email, blood_type, allergies, medical_notes,
                 is_active, registered_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE, ?)
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, patient.getPatientCode());
            ps.setString(2, patient.getFullName());

            if (patient.getDateOfBirth() != null) {
                ps.setDate(3, Date.valueOf(patient.getDateOfBirth()));
            } else {
                ps.setNull(3, Types.DATE);
            }

            ps.setString(4,  patient.getGender());
            ps.setString(5,  patient.getAddress());
            ps.setString(6,  patient.getContact());
            ps.setString(7,  patient.getEmail());
            ps.setString(8,  patient.getBloodType());
            ps.setString(9,  patient.getAllergies());
            ps.setString(10, patient.getMedicalNotes());
            ps.setInt(11,    patient.getRegisteredBy());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }

        } catch (Exception e) {
            System.err.println("[PatientDAO] createPatient error: " + e.getMessage());
        }
        return -1;
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Update existing patient details.
     */
    public boolean updatePatient(Patient patient) {
        String sql = """
                UPDATE patients SET
                    full_name     = ?,
                    date_of_birth = ?,
                    gender        = ?,
                    address       = ?,
                    contact       = ?,
                    email         = ?,
                    blood_type    = ?,
                    allergies     = ?,
                    medical_notes = ?
                WHERE id = ?
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patient.getFullName());

            if (patient.getDateOfBirth() != null) {
                ps.setDate(2, Date.valueOf(patient.getDateOfBirth()));
            } else {
                ps.setNull(2, Types.DATE);
            }

            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getAddress());
            ps.setString(5, patient.getContact());
            ps.setString(6, patient.getEmail());
            ps.setString(7, patient.getBloodType());
            ps.setString(8, patient.getAllergies());
            ps.setString(9, patient.getMedicalNotes());
            ps.setInt(10, patient.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[PatientDAO] updatePatient error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Soft delete patient (set is_active = FALSE).
     */
    public boolean deactivatePatient(int patientId) {
        String sql = "UPDATE patients SET is_active = FALSE WHERE id = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[PatientDAO] deactivatePatient error: " + e.getMessage());
        }
        return false;
    }

    // ──────────────────────────────────────────────────────────────
    // STATISTICS
    // ──────────────────────────────────────────────────────────────

    /**
     * Get total count of active patients.
     */
    public int getTotalPatients() {
        String sql = "SELECT COUNT(*) FROM patients WHERE is_active = TRUE";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            System.err.println("[PatientDAO] getTotalPatients error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get count of new patients registered this month.
     */
    public int getNewPatientsThisMonth() {
        String sql = """
                SELECT COUNT(*) FROM patients
                WHERE is_active = TRUE
                AND MONTH(created_at) = MONTH(NOW())
                AND YEAR(created_at)  = YEAR(NOW())
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            System.err.println("[PatientDAO] getNewPatientsThisMonth error: " + e.getMessage());
        }
        return 0;
    }

    // ──────────────────────────────────────────────────────────────
    // RESULT SET MAPPER
    // ──────────────────────────────────────────────────────────────

    /**
     * Map ResultSet row to Patient object.
     */
    private Patient mapPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setPatientCode(rs.getString("patient_code"));
        p.setFullName(rs.getString("full_name"));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) p.setDateOfBirth(dob.toLocalDate());

        p.setGender(rs.getString("gender"));
        p.setAddress(rs.getString("address"));
        p.setContact(rs.getString("contact"));
        p.setEmail(rs.getString("email"));
        p.setBloodType(rs.getString("blood_type"));
        p.setAllergies(rs.getString("allergies"));
        p.setMedicalNotes(rs.getString("medical_notes"));
        p.setActive(rs.getBoolean("is_active"));
        p.setRegisteredBy(rs.getInt("registered_by"));

        // Try to get joined column (may not exist in all queries)
        try {
            p.setRegisteredByName(rs.getString("registered_by_name"));
        } catch (SQLException ignored) {}

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) p.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) p.setUpdatedAt(updatedAt.toLocalDateTime());

        return p;
    }
}