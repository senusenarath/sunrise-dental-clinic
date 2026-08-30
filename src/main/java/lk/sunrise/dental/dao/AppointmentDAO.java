package lk.sunrise.dental.dao;

import lk.sunrise.dental.model.Appointment;
import lk.sunrise.dental.util.DBConnection;
import lk.sunrise.dental.util.DateUtil;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * AppointmentDAO.java
 * Data Access Object for Appointment operations
 *
 * Handles all database operations related to appointments
 * including double-booking prevention.
 * Package : lk.sunrise.dental.dao
 * ================================================================
 */
public class AppointmentDAO {

    // ── Base SELECT with JOINs ─────────────────────────────────────
    private static final String BASE_SELECT = """
            SELECT
                a.*,
                p.full_name      AS patient_name,
                p.patient_code   AS patient_code,
                p.contact        AS patient_contact,
                u.full_name      AS dentist_name,
                t.name           AS treatment_name,
                t.base_cost      AS treatment_cost,
                t.duration_mins  AS treatment_duration,
                u.consult_fee    AS consult_fee,
                cb.full_name     AS created_by_name,
                b.bill_code      AS bill_code,
                b.status         AS bill_status,
                CASE WHEN b.id IS NOT NULL THEN TRUE ELSE FALSE END AS has_bill
            FROM appointments a
            LEFT JOIN patients    p  ON a.patient_id   = p.id
            LEFT JOIN users       u  ON a.dentist_id   = u.id
            LEFT JOIN treatments  t  ON a.treatment_id = t.id
            LEFT JOIN users       cb ON a.created_by   = cb.id
            LEFT JOIN bills       b  ON a.id           = b.appointment_id
            """;

    // ──────────────────────────────────────────────────────────────
    // READ OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Get all appointments ordered by date descending.
     */
    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY a.apt_date DESC, a.apt_time ASC";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapAppointment(rs));

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] getAllAppointments error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get tomorrow's still-scheduled appointments.
     * Used to power the appointment reminder-email feature.
     */
    public List<Appointment> getTomorrowAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT +
                " WHERE a.apt_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND a.status = 'Scheduled'" +
                " ORDER BY a.apt_time ASC";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapAppointment(rs));

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] getTomorrowAppointments error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get today's appointments.
     */
    public List<Appointment> getTodayAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT +
                " WHERE a.apt_date = CURDATE()" +
                " ORDER BY a.apt_time ASC";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapAppointment(rs));

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] getTodayAppointments error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get appointments by status.
     */
    public List<Appointment> getAppointmentsByStatus(String status) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT +
                " WHERE a.status = ?" +
                " ORDER BY a.apt_date DESC, a.apt_time ASC";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAppointment(rs));

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] getAppointmentsByStatus error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get appointments by patient ID.
     */
    public List<Appointment> getAppointmentsByPatient(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT +
                " WHERE a.patient_id = ?" +
                " ORDER BY a.apt_date DESC, a.apt_time ASC";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAppointment(rs));

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] getAppointmentsByPatient error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get appointments by dentist ID.
     */
    public List<Appointment> getAppointmentsByDentist(int dentistId) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT +
                " WHERE a.dentist_id = ?" +
                " ORDER BY a.apt_date DESC, a.apt_time ASC";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dentistId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAppointment(rs));

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] getAppointmentsByDentist error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Check whether a dentist is currently with a patient right now
     * (an 'In Progress' appointment today), and if so, when they are
     * expected to be free.
     *
     * @return the estimated time the dentist becomes free, or null if
     *         they are available right now
     */
    public LocalTime getDentistBusyUntil(int dentistId) {
        String sql = """
                SELECT a.apt_time, t.duration_mins
                FROM appointments a
                JOIN treatments t ON a.treatment_id = t.id
                WHERE a.dentist_id = ? AND a.apt_date = CURDATE() AND a.status = 'In Progress'
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dentistId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Time aptTime = rs.getTime("apt_time");
                int  durationMins = rs.getInt("duration_mins");
                if (aptTime != null) {
                    return aptTime.toLocalTime().plusMinutes(durationMins);
                }
            }

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] getDentistBusyUntil error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get today's appointments for a specific dentist.
     * Used to personalize the dentist's dashboard schedule.
     */
    public List<Appointment> getTodayAppointmentsByDentist(int dentistId) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT +
                " WHERE a.dentist_id = ? AND a.apt_date = CURDATE()" +
                " ORDER BY a.apt_time ASC";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dentistId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAppointment(rs));

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] getTodayAppointmentsByDentist error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get appointment by ID.
     */
    public Appointment getAppointmentById(int id) {
        String sql = BASE_SELECT + " WHERE a.id = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapAppointment(rs);

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] getAppointmentById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get appointment by appointment code.
     */
    public Appointment getAppointmentByCode(String aptCode) {
        String sql = BASE_SELECT + " WHERE a.apt_code = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, aptCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapAppointment(rs);

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] getAppointmentByCode error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Search appointments by patient name or appointment code.
     */
    public List<Appointment> searchAppointments(String keyword) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT +
                " WHERE p.full_name LIKE ? OR a.apt_code LIKE ?" +
                " ORDER BY a.apt_date DESC";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String term = "%" + keyword.trim() + "%";
            ps.setString(1, term);
            ps.setString(2, term);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAppointment(rs));

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] searchAppointments error: " + e.getMessage());
        }
        return list;
    }

    // ──────────────────────────────────────────────────────────────
    // DOUBLE BOOKING PREVENTION
    // ──────────────────────────────────────────────────────────────

    /**
     * Check if dentist already has an appointment at the given date and time.
     * Prevents double booking.
     *
     * @param dentistId   dentist user ID
     * @param aptDate     appointment date
     * @param aptTime     appointment time (HH:mm)
     * @param excludeId   appointment ID to exclude (for updates, use 0 for new)
     * @return true if slot is already taken
     */
    public boolean isDentistBooked(int dentistId, String aptDate,
                                    String aptTime, int excludeId) {
        String sql = """
                SELECT COUNT(*) FROM appointments
                WHERE dentist_id = ?
                AND apt_date     = ?
                AND apt_time     = ?
                AND status      != 'Cancelled'
                AND id          != ?
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,    dentistId);
            ps.setString(2, aptDate);
            ps.setString(3, aptTime);
            ps.setInt(4,    excludeId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] isDentistBooked error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Generate next appointment code.
     * Format: APT-20250115-0001
     */
    public String generateAptCode() {
        String today = DateUtil.getTodayCode();
        String sql   = "SELECT COUNT(*) FROM appointments WHERE apt_code LIKE ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "APT-" + today + "-%");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("APT-%s-%04d", today, count);
            }

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] generateAptCode error: " + e.getMessage());
        }
        return "APT-" + today + "-0001";
    }

    // ──────────────────────────────────────────────────────────────
    // CREATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Book a new appointment.
     *
     * @param apt Appointment object with all details
     * @return generated appointment ID if successful, -1 otherwise
     */
    public int createAppointment(Appointment apt) {
        String sql = """
                INSERT INTO appointments
                (apt_code, patient_id, dentist_id, treatment_id,
                 apt_date, apt_time, status, notes, created_by)
                VALUES (?, ?, ?, ?, ?, ?, 'Scheduled', ?, ?)
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, apt.getAptCode());
            ps.setInt(2,    apt.getPatientId());
            ps.setInt(3,    apt.getDentistId());
            ps.setInt(4,    apt.getTreatmentId());
            ps.setDate(5,   Date.valueOf(apt.getAptDate()));
            ps.setTime(6,   Time.valueOf(apt.getAptTime()));
            ps.setString(7, apt.getNotes());
            ps.setInt(8,    apt.getCreatedBy());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] createAppointment error: " + e.getMessage());
        }
        return -1;
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Update appointment status and clinical notes.
     */
    public boolean updateTreatmentStatus(int aptId, String status, String notes) {
        String sql = """
                UPDATE appointments SET
                    status = ?,
                    notes  = ?
                WHERE id = ?
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, notes);
            ps.setInt(3,    aptId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] updateTreatmentStatus error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update full appointment details (for rescheduling).
     */
    public boolean updateAppointment(Appointment apt) {
        String sql = """
                UPDATE appointments SET
                    patient_id   = ?,
                    dentist_id   = ?,
                    treatment_id = ?,
                    apt_date     = ?,
                    apt_time     = ?,
                    notes        = ?
                WHERE id = ? AND status = 'Scheduled'
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,    apt.getPatientId());
            ps.setInt(2,    apt.getDentistId());
            ps.setInt(3,    apt.getTreatmentId());
            ps.setDate(4,   Date.valueOf(apt.getAptDate()));
            ps.setTime(5,   Time.valueOf(apt.getAptTime()));
            ps.setString(6, apt.getNotes());
            ps.setInt(7,    apt.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] updateAppointment error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cancel an appointment.
     */
    public boolean cancelAppointment(int aptId) {
        String sql = "UPDATE appointments SET status = 'Cancelled' WHERE id = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, aptId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] cancelAppointment error: " + e.getMessage());
        }
        return false;
    }

    // ──────────────────────────────────────────────────────────────
    // STATISTICS
    // ──────────────────────────────────────────────────────────────

    /** Get total appointments count */
    public int getTotalAppointments() {
        return getCountBySql("SELECT COUNT(*) FROM appointments");
    }

    /** Get today's appointment count */
    public int getTodayCount() {
        return getCountBySql("SELECT COUNT(*) FROM appointments WHERE apt_date = CURDATE()");
    }

    /** Get scheduled appointments count */
    public int getScheduledCount() {
        return getCountBySql("SELECT COUNT(*) FROM appointments WHERE status = 'Scheduled'");
    }

    /** Get completed appointments count */
    public int getCompletedCount() {
        return getCountBySql("SELECT COUNT(*) FROM appointments WHERE status = 'Completed'");
    }

    /** Get this month's appointment count */
    public int getThisMonthCount() {
        return getCountBySql("""
                SELECT COUNT(*) FROM appointments
                WHERE MONTH(apt_date) = MONTH(NOW())
                AND YEAR(apt_date) = YEAR(NOW())
                """);
    }

    /** Helper to execute count queries */
    private int getCountBySql(String sql) {
        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            System.err.println("[AppointmentDAO] getCountBySql error: " + e.getMessage());
        }
        return 0;
    }

    // ──────────────────────────────────────────────────────────────
    // RESULT SET MAPPER
    // ──────────────────────────────────────────────────────────────

    /**
     * Map ResultSet row to Appointment object.
     */
    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setAptCode(rs.getString("apt_code"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setDentistId(rs.getInt("dentist_id"));
        a.setTreatmentId(rs.getInt("treatment_id"));

        Date aptDate = rs.getDate("apt_date");
        if (aptDate != null) a.setAptDate(aptDate.toLocalDate());

        Time aptTime = rs.getTime("apt_time");
        if (aptTime != null) a.setAptTime(aptTime.toLocalTime());

        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        a.setCreatedBy(rs.getInt("created_by"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) a.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) a.setUpdatedAt(updatedAt.toLocalDateTime());

        // Joined fields
        try { a.setPatientName(rs.getString("patient_name"));         } catch (SQLException ignored) {}
        try { a.setPatientCode(rs.getString("patient_code"));         } catch (SQLException ignored) {}
        try { a.setPatientContact(rs.getString("patient_contact"));   } catch (SQLException ignored) {}
        try { a.setDentistName(rs.getString("dentist_name"));         } catch (SQLException ignored) {}
        try { a.setTreatmentName(rs.getString("treatment_name"));     } catch (SQLException ignored) {}
        try { a.setTreatmentCost(rs.getDouble("treatment_cost"));     } catch (SQLException ignored) {}
        try { a.setConsultFee(rs.getDouble("consult_fee"));           } catch (SQLException ignored) {}
        try { a.setTreatmentDuration(rs.getInt("treatment_duration"));} catch (SQLException ignored) {}
        try { a.setHasBill(rs.getBoolean("has_bill"));                } catch (SQLException ignored) {}
        try { a.setBillCode(rs.getString("bill_code"));               } catch (SQLException ignored) {}
        try { a.setBillStatus(rs.getString("bill_status"));           } catch (SQLException ignored) {}
        try { a.setCreatedByName(rs.getString("created_by_name"));    } catch (SQLException ignored) {}

        return a;
    }
}