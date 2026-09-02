package lk.sunrise.dental.api;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sunrise.dental.dao.ReportDAO;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

/**
 * ================================================================
 * ReportApiServlet.java
 * REST/JSON API for stored-procedure-backed analytics
 * (Admin only - see AuthFilter.ADMIN_ONLY_URLS)
 *
 * GET /api/reports/dentist-workload/{dentistId}
 * GET /api/reports/dentist-workload/{dentistId}?start=YYYY-MM-DD&end=YYYY-MM-DD
 *     - start/end default to the 1st of the current month and today
 *
 * Package : lk.sunrise.dental.api
 * ================================================================
 */
public class ReportApiServlet extends HttpServlet {

    private static final String WORKLOAD_PREFIX = "/dentist-workload/";

    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || !pathInfo.startsWith(WORKLOAD_PREFIX)) {
            ApiUtil.writeError(res, 404, "Unknown endpoint.");
            return;
        }

        String idPart = pathInfo.substring(WORKLOAD_PREFIX.length());
        try {
            int dentistId = Integer.parseInt(idPart);

            LocalDate start = parseDateOrDefault(req.getParameter("start"), LocalDate.now().withDayOfMonth(1));
            LocalDate end   = parseDateOrDefault(req.getParameter("end"), LocalDate.now());

            Map<String, Object> workload = reportDAO.getDentistWorkload(dentistId, start, end);
            if (workload == null) {
                ApiUtil.writeError(res, 404, "Dentist not found.");
                return;
            }

            ApiUtil.writeSuccess(res, new JSONObject(workload));

        } catch (NumberFormatException e) {
            ApiUtil.writeError(res, 400, "Invalid dentist id.");
        }
    }

    private LocalDate parseDateOrDefault(String value, LocalDate fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return fallback;
        }
    }
}
