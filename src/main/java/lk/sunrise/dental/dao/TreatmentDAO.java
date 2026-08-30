package lk.sunrise.dental.dao;

import lk.sunrise.dental.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ================================================================
 * TreatmentDAO.java
 * Data Access Object for Treatment/Procedure operations
 *
 * Handles reading treatment types and costs.
 * Package : lk.sunrise.dental.dao
 * ================================================================
 */
public class TreatmentDAO {

    /**
     * Get all active treatments as a list of maps.
     * Used for dropdown menus in appointment booking.
     *
     * @return List of treatment maps with id, name, cost, duration
     */
    public List<Map<String, Object>> getAllTreatments() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
                SELECT id, treatment_code, name, description,
                       base_cost, duration_mins
                FROM treatments
                WHERE is_active = TRUE
                ORDER BY name
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> treatment = new LinkedHashMap<>();
                treatment.put("id",           rs.getInt("id"));
                treatment.put("code",         rs.getString("treatment_code"));
                treatment.put("name",         rs.getString("name"));
                treatment.put("description",  rs.getString("description"));
                treatment.put("baseCost",     rs.getDouble("base_cost"));
                treatment.put("durationMins", rs.getInt("duration_mins"));
                list.add(treatment);
            }

        } catch (Exception e) {
            System.err.println("[TreatmentDAO] getAllTreatments error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get treatment by ID.
     *
     * @param id treatment ID
     * @return Map with treatment details or null
     */
    public Map<String, Object> getTreatmentById(int id) {
        String sql = """
                SELECT id, treatment_code, name, description,
                       base_cost, duration_mins
                FROM treatments
                WHERE id = ? AND is_active = TRUE
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> treatment = new LinkedHashMap<>();
                treatment.put("id",           rs.getInt("id"));
                treatment.put("code",         rs.getString("treatment_code"));
                treatment.put("name",         rs.getString("name"));
                treatment.put("description",  rs.getString("description"));
                treatment.put("baseCost",     rs.getDouble("base_cost"));
                treatment.put("durationMins", rs.getInt("duration_mins"));
                return treatment;
            }

        } catch (Exception e) {
            System.err.println("[TreatmentDAO] getTreatmentById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get base cost for a specific treatment.
     *
     * @param treatmentId treatment ID
     * @return base cost as double, 0.0 if not found
     */
    public double getTreatmentCost(int treatmentId) {
        String sql = "SELECT base_cost FROM treatments WHERE id = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, treatmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("base_cost");

        } catch (Exception e) {
            System.err.println("[TreatmentDAO] getTreatmentCost error: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get treatment statistics for reports.
     * Returns treatment name and appointment count.
     */
    public List<Map<String, Object>> getTreatmentStats() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
                SELECT
                    t.name,
                    t.base_cost,
                    COUNT(a.id)           AS appointment_count,
                    COALESCE(SUM(b.total_amount), 0) AS total_revenue
                FROM treatments t
                LEFT JOIN appointments a ON t.id = a.treatment_id
                LEFT JOIN bills        b ON a.id = b.appointment_id AND b.status = 'Paid'
                WHERE t.is_active = TRUE
                GROUP BY t.id, t.name, t.base_cost
                ORDER BY appointment_count DESC
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> stat = new LinkedHashMap<>();
                stat.put("name",             rs.getString("name"));
                stat.put("baseCost",         rs.getDouble("base_cost"));
                stat.put("appointmentCount", rs.getInt("appointment_count"));
                stat.put("totalRevenue",     rs.getDouble("total_revenue"));
                list.add(stat);
            }

        } catch (Exception e) {
            System.err.println("[TreatmentDAO] getTreatmentStats error: " + e.getMessage());
        }
        return list;
    }
}