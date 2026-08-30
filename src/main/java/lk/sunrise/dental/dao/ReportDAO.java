package lk.sunrise.dental.dao;

import lk.sunrise.dental.util.DBConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ================================================================
 * ReportDAO.java
 * Data Access Object for stored-procedure-backed analytics
 *
 * Calls the sp_dentist_workload MySQL stored procedure via a JDBC
 * CallableStatement, rather than re-implementing the same grouping
 * query as inline Java SQL - the "how do we summarize a dentist's
 * caseload" rule lives once, in the database.
 * Package : lk.sunrise.dental.dao
 * ================================================================
 */
public class ReportDAO {

    /**
     * Get a dentist's appointment/revenue workload for a date range.
     *
     * @param dentistId user ID of the dentist
     * @param startDate range start (inclusive)
     * @param endDate   range end (inclusive)
     * @return workload summary map, or null if the dentist does not exist
     */
    public Map<String, Object> getDentistWorkload(int dentistId, LocalDate startDate, LocalDate endDate) {
        String sql = "{call sp_dentist_workload(?, ?, ?)}";

        try (Connection conn = DBConnection.getConn();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, dentistId);
            cs.setDate(2, Date.valueOf(startDate));
            cs.setDate(3, Date.valueOf(endDate));

            boolean hasResultSet = cs.execute();
            if (hasResultSet) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs.next()) {
                        Map<String, Object> workload = new LinkedHashMap<>();
                        workload.put("dentistName",       rs.getString("dentist_name"));
                        workload.put("totalAppointments", rs.getInt("total_appointments"));
                        workload.put("completed",         rs.getInt("completed"));
                        workload.put("scheduled",         rs.getInt("scheduled"));
                        workload.put("inProgress",         rs.getInt("in_progress"));
                        workload.put("cancelled",         rs.getInt("cancelled"));
                        workload.put("revenueGenerated",  rs.getDouble("revenue_generated"));
                        return workload;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("[ReportDAO] getDentistWorkload error: " + e.getMessage());
        }
        return null;
    }
}
