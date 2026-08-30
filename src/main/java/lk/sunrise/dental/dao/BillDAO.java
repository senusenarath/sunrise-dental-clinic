package lk.sunrise.dental.dao;

import lk.sunrise.dental.model.Bill;
import lk.sunrise.dental.util.DBConnection;
import lk.sunrise.dental.util.DateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ================================================================
 * BillDAO.java
 * Data Access Object for Billing operations
 *
 * Handles all database operations related to invoices and payments.
 * Package : lk.sunrise.dental.dao
 * ================================================================
 */
public class BillDAO {

    // ── Base SELECT with JOINs ─────────────────────────────────────
    private static final String BASE_SELECT = """
            SELECT
                b.*,
                p.full_name      AS patient_name,
                p.patient_code   AS patient_code,
                p.contact        AS patient_contact,
                p.address        AS patient_address,
                u.full_name      AS dentist_name,
                t.name           AS treatment_name,
                a.apt_date       AS apt_date,
                a.apt_time       AS apt_time,
                a.apt_code       AS apt_code,
                sb.full_name     AS settled_by_name,
                cb.full_name     AS created_by_name
            FROM bills b
            LEFT JOIN appointments a  ON b.appointment_id = a.id
            LEFT JOIN patients     p  ON a.patient_id     = p.id
            LEFT JOIN users        u  ON a.dentist_id     = u.id
            LEFT JOIN treatments   t  ON a.treatment_id   = t.id
            LEFT JOIN users        sb ON b.settled_by      = sb.id
            LEFT JOIN users        cb ON b.created_by      = cb.id
            """;

    // ──────────────────────────────────────────────────────────────
    // READ OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Get all bills ordered by created date descending.
     */
    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY b.created_at DESC";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapBill(rs));

        } catch (Exception e) {
            System.err.println("[BillDAO] getAllBills error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get bill by ID with full details.
     */
    public Bill getBillById(int id) {
        String sql = BASE_SELECT + " WHERE b.id = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapBill(rs);

        } catch (Exception e) {
            System.err.println("[BillDAO] getBillById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get bill by bill code.
     */
    public Bill getBillByCode(String billCode) {
        String sql = BASE_SELECT + " WHERE b.bill_code = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, billCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapBill(rs);

        } catch (Exception e) {
            System.err.println("[BillDAO] getBillByCode error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get bill by appointment ID.
     */
    public Bill getBillByAppointmentId(int appointmentId) {
        String sql = BASE_SELECT + " WHERE b.appointment_id = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapBill(rs);

        } catch (Exception e) {
            System.err.println("[BillDAO] getBillByAppointmentId error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get bills by status.
     */
    public List<Bill> getBillsByStatus(String status) {
        List<Bill> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE b.status = ? ORDER BY b.created_at DESC";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBill(rs));

        } catch (Exception e) {
            System.err.println("[BillDAO] getBillsByStatus error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get bills for a specific patient.
     */
    public List<Bill> getBillsByPatient(int patientId) {
        List<Bill> list = new ArrayList<>();
        String sql = BASE_SELECT +
                " WHERE a.patient_id = ? ORDER BY b.created_at DESC";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBill(rs));

        } catch (Exception e) {
            System.err.println("[BillDAO] getBillsByPatient error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Check if bill already exists for appointment.
     */
    public boolean billExistsForAppointment(int appointmentId) {
        String sql = "SELECT COUNT(*) FROM bills WHERE appointment_id = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (Exception e) {
            System.err.println("[BillDAO] billExistsForAppointment error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Generate next bill code.
     * Format: BILL-2025-0001
     */
    public String generateBillCode() {
        String year = DateUtil.getCurrentYear();
        String sql  = "SELECT COUNT(*) FROM bills WHERE bill_code LIKE ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "BILL-" + year + "-%");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("BILL-%s-%04d", year, count);
            }

        } catch (Exception e) {
            System.err.println("[BillDAO] generateBillCode error: " + e.getMessage());
        }
        return "BILL-" + year + "-0001";
    }

    // ──────────────────────────────────────────────────────────────
    // CREATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Create a new bill/invoice.
     *
     * @param bill Bill object with all details
     * @return generated bill ID if successful, -1 otherwise
     */
    public int createBill(Bill bill) {
        String sql = """
                INSERT INTO bills
                (bill_code, appointment_id, treatment_fee, consult_fee,
                 discount, total_amount, payment_method, status, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending', ?)
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, bill.getBillCode());
            ps.setInt(2,    bill.getAppointmentId());
            ps.setDouble(3, bill.getTreatmentFee());
            ps.setDouble(4, bill.getConsultFee());
            ps.setDouble(5, bill.getDiscount());
            ps.setDouble(6, bill.getTotalAmount());
            ps.setString(7, bill.getPaymentMethod());
            ps.setInt(8,    bill.getCreatedBy());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            }

        } catch (Exception e) {
            System.err.println("[BillDAO] createBill error: " + e.getMessage());
        }
        return -1;
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Mark bill as Paid and record settlement details.
     *
     * @param billId        bill ID to settle
     * @param paymentMethod selected payment method
     * @param settledByUserId  ID of staff who processed payment
     * @return true if settled successfully
     */
    public boolean settleBill(int billId, String paymentMethod, int settledByUserId) {
        String sql = """
                UPDATE bills SET
                    status         = 'Paid',
                    payment_method = ?,
                    settled_by     = ?,
                    settled_at     = NOW()
                WHERE id = ? AND status = 'Pending'
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paymentMethod);
            ps.setInt(2,    settledByUserId);
            ps.setInt(3,    billId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[BillDAO] settleBill error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update bill details (only Pending bills can be updated).
     */
    public boolean updateBill(Bill bill) {
        String sql = """
                UPDATE bills SET
                    treatment_fee  = ?,
                    consult_fee    = ?,
                    discount       = ?,
                    total_amount   = ?,
                    payment_method = ?
                WHERE id = ? AND status = 'Pending'
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, bill.getTreatmentFee());
            ps.setDouble(2, bill.getConsultFee());
            ps.setDouble(3, bill.getDiscount());
            ps.setDouble(4, bill.getTotalAmount());
            ps.setString(5, bill.getPaymentMethod());
            ps.setInt(6,    bill.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[BillDAO] updateBill error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cancel a pending bill.
     */
    public boolean cancelBill(int billId) {
        String sql = "UPDATE bills SET status = 'Cancelled' WHERE id = ? AND status = 'Pending'";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, billId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[BillDAO] cancelBill error: " + e.getMessage());
        }
        return false;
    }

    // ──────────────────────────────────────────────────────────────
    // STATISTICS & REPORTS
    // ──────────────────────────────────────────────────────────────

    /**
     * Get paid revenue grouped by month for the last N months, oldest first.
     * Powers the Revenue Trend chart on the Reports page. Months with no
     * paid bills simply don't appear in the result.
     */
    public List<Map<String, Object>> getMonthlyRevenueTrend(int months) {
        List<Map<String, Object>> trend = new ArrayList<>();
        String sql = """
                SELECT
                    DATE_FORMAT(settled_at, '%b %Y') AS month_label,
                    DATE_FORMAT(settled_at, '%Y-%m') AS month_key,
                    COALESCE(SUM(total_amount), 0)   AS revenue
                FROM bills
                WHERE status = 'Paid'
                  AND settled_at >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)
                GROUP BY month_key, month_label
                ORDER BY month_key ASC
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, months);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("monthLabel", rs.getString("month_label"));
                row.put("revenue",    rs.getDouble("revenue"));
                trend.add(row);
            }

        } catch (Exception e) {
            System.err.println("[BillDAO] getMonthlyRevenueTrend error: " + e.getMessage());
        }
        return trend;
    }

    /**
     * Get total revenue from paid bills.
     */
    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bills WHERE status = 'Paid'";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getDouble(1);

        } catch (Exception e) {
            System.err.println("[BillDAO] getTotalRevenue error: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get this month's revenue.
     */
    public double getMonthlyRevenue() {
        String sql = """
                SELECT COALESCE(SUM(total_amount), 0) FROM bills
                WHERE status = 'Paid'
                AND MONTH(settled_at) = MONTH(NOW())
                AND YEAR(settled_at)  = YEAR(NOW())
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getDouble(1);

        } catch (Exception e) {
            System.err.println("[BillDAO] getMonthlyRevenue error: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get total pending amount (unpaid bills).
     */
    public double getPendingAmount() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bills WHERE status = 'Pending'";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getDouble(1);

        } catch (Exception e) {
            System.err.println("[BillDAO] getPendingAmount error: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get count of pending bills.
     */
    public int getPendingBillsCount() {
        String sql = "SELECT COUNT(*) FROM bills WHERE status = 'Pending'";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            System.err.println("[BillDAO] getPendingBillsCount error: " + e.getMessage());
        }
        return 0;
    }

    // ──────────────────────────────────────────────────────────────
    // RESULT SET MAPPER
    // ──────────────────────────────────────────────────────────────

    /**
     * Map ResultSet row to Bill object.
     */
    private Bill mapBill(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setId(rs.getInt("id"));
        b.setBillCode(rs.getString("bill_code"));
        b.setAppointmentId(rs.getInt("appointment_id"));
        b.setTreatmentFee(rs.getDouble("treatment_fee"));
        b.setConsultFee(rs.getDouble("consult_fee"));
        b.setDiscount(rs.getDouble("discount"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setPaymentMethod(rs.getString("payment_method"));
        b.setStatus(rs.getString("status"));
        b.setSettledBy(rs.getInt("settled_by"));
        b.setCreatedBy(rs.getInt("created_by"));

        Timestamp settledAt = rs.getTimestamp("settled_at");
        if (settledAt != null) b.setSettledAt(settledAt.toLocalDateTime());

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) b.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) b.setUpdatedAt(updatedAt.toLocalDateTime());

        // Joined fields
        try { b.setPatientName(rs.getString("patient_name"));       } catch (SQLException ignored) {}
        try { b.setPatientCode(rs.getString("patient_code"));       } catch (SQLException ignored) {}
        try { b.setPatientContact(rs.getString("patient_contact")); } catch (SQLException ignored) {}
        try { b.setPatientAddress(rs.getString("patient_address")); } catch (SQLException ignored) {}
        try { b.setDentistName(rs.getString("dentist_name"));       } catch (SQLException ignored) {}
        try { b.setTreatmentName(rs.getString("treatment_name"));   } catch (SQLException ignored) {}
        try { b.setAptCode(rs.getString("apt_code"));               } catch (SQLException ignored) {}
        try { b.setSettledByName(rs.getString("settled_by_name"));  } catch (SQLException ignored) {}
        try { b.setCreatedByName(rs.getString("created_by_name"));  } catch (SQLException ignored) {}

        Date aptDate = rs.getDate("apt_date");
        if (aptDate != null) b.setAptDate(aptDate.toLocalDate());

        Time aptTime = rs.getTime("apt_time");
        if (aptTime != null) b.setAptTime(aptTime.toLocalTime());

        return b;
    }
}