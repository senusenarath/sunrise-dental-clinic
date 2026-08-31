package lk.sunrise.dental.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lk.sunrise.dental.dao.ReportDAO;
import lk.sunrise.dental.dao.TreatmentDAO;
import lk.sunrise.dental.model.User;
import lk.sunrise.dental.service.AppointmentService;
import lk.sunrise.dental.service.BillService;
import lk.sunrise.dental.service.PatientService;
import lk.sunrise.dental.service.UserService;
import lk.sunrise.dental.util.DateUtil;
import lk.sunrise.dental.util.PdfDocumentBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ================================================================
 * ReportServlet.java
 * Executive BI Analytics and reporting dashboard
 *
 * GET /reports              → Load analytics data and forward to report view
 * GET /reports?export=csv   → Download treatment analytics as a CSV file
 *
 * Package : lk.sunrise.dental.servlet
 * ================================================================
 */
public class ReportServlet extends HttpServlet {

    private final AppointmentService appointmentService = new AppointmentService();
    private final PatientService     patientService     = new PatientService();
    private final BillService        billService        = new BillService();
    private final UserService        userService        = new UserService();
    private final TreatmentDAO       treatmentDAO       = new TreatmentDAO();
    private final ReportDAO          reportDAO          = new ReportDAO();

    private static final String VIEW = "/WEB-INF/views/report/dashboard.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String export = req.getParameter("export");
        if ("csv".equalsIgnoreCase(export)) {
            exportCsv(res);
            return;
        }
        if ("pdf".equalsIgnoreCase(export)) {
            exportPdf(res);
            return;
        }

        try {
            // ── Appointment Analytics ──────────────────────────────
            req.setAttribute("totalAppointments", appointmentService.getTotalAppointments());
            req.setAttribute("scheduledCount",    appointmentService.getScheduledCount());
            req.setAttribute("completedCount",    appointmentService.getCompletedCount());
            req.setAttribute("todayCount",        appointmentService.getTodayCount());
            req.setAttribute("thisMonthCount",    appointmentService.getThisMonthCount());

            // ── Patient Analytics ──────────────────────────────────
            req.setAttribute("totalPatients",     patientService.getTotalPatients());
            req.setAttribute("newPatientsMonth",  patientService.getNewPatientsThisMonth());

            // ── Financial Analytics ────────────────────────────────
            req.setAttribute("totalRevenue",      billService.getTotalRevenue());
            req.setAttribute("monthlyRevenue",    billService.getMonthlyRevenue());
            req.setAttribute("pendingAmount",     billService.getPendingAmount());
            req.setAttribute("pendingBills",      billService.getPendingCount());

            // ── Staff Analytics ────────────────────────────────────
            req.setAttribute("totalStaff",        userService.getTotalActiveStaff());
            req.setAttribute("totalDentists",     userService.getTotalDentists());

            // ── Treatment Statistics ───────────────────────────────
            req.setAttribute("treatmentStats",    treatmentDAO.getTreatmentStats());

            // ── Revenue Trend (last 6 months) ──────────────────────
            List<Map<String, Object>> revenueTrend = billService.getMonthlyRevenueTrend(6);
            double maxMonthlyRevenue = 0;
            for (Map<String, Object> point : revenueTrend) {
                double revenue = ((Number) point.get("revenue")).doubleValue();
                if (revenue > maxMonthlyRevenue) maxMonthlyRevenue = revenue;
            }
            req.setAttribute("revenueTrend",       revenueTrend);
            req.setAttribute("maxMonthlyRevenue",  maxMonthlyRevenue);

            // ── All Dentists for caseload report ───────────────────
            List<User> dentists = userService.getAllDentists();
            req.setAttribute("dentists", dentists);

            // ── Per-Dentist Workload (this month) via sp_dentist_workload ──
            LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
            LocalDate today      = LocalDate.now();
            Map<Integer, Map<String, Object>> dentistWorkloads = new HashMap<>();
            for (User dentist : dentists) {
                Map<String, Object> workload = reportDAO.getDentistWorkload(dentist.getId(), monthStart, today);
                if (workload != null) {
                    dentistWorkloads.put(dentist.getId(), workload);
                }
            }
            req.setAttribute("dentistWorkloads", dentistWorkloads);

            // ── Real-time Dentist Availability ─────────────────────
            Map<Integer, String> dentistAvailability = new HashMap<>();
            for (User dentist : dentists) {
                LocalTime busyUntil = appointmentService.getDentistBusyUntil(dentist.getId());
                if (busyUntil != null) {
                    dentistAvailability.put(dentist.getId(), DateUtil.formatTimeDisplay(busyUntil));
                }
            }
            req.setAttribute("dentistAvailability", dentistAvailability);

            req.setAttribute("pageTitle", "Executive Analytics & Reports");

        } catch (Exception e) {
            System.err.println("[ReportServlet] Error: " + e.getMessage());
            req.setAttribute("errorMsg", "Error loading report data.");
        }

        req.getRequestDispatcher(VIEW).forward(req, res);
    }

    /**
     * Stream the treatment analytics table as a downloadable CSV file.
     */
    private void exportCsv(HttpServletResponse res) throws IOException {
        res.setContentType("text/csv;charset=UTF-8");
        res.setHeader("Content-Disposition",
                "attachment; filename=\"sunrise-dental-report-" + DateUtil.getTodayCode() + ".csv\"");

        PrintWriter writer = res.getWriter();

        writer.println("Sunrise Dental Clinic - Executive Report");
        writer.println("Generated," + DateUtil.formatDateTimeFull(java.time.LocalDateTime.now()));
        writer.println();

        writer.println("Metric,Value");
        writer.println("Total Appointments," + appointmentService.getTotalAppointments());
        writer.println("Scheduled Appointments," + appointmentService.getScheduledCount());
        writer.println("Completed Appointments," + appointmentService.getCompletedCount());
        writer.println("This Month's Appointments," + appointmentService.getThisMonthCount());
        writer.println("Total Patients," + patientService.getTotalPatients());
        writer.println("New Patients This Month," + patientService.getNewPatientsThisMonth());
        writer.println("Total Revenue (LKR)," + billService.getTotalRevenue());
        writer.println("Monthly Revenue (LKR)," + billService.getMonthlyRevenue());
        writer.println("Pending Amount (LKR)," + billService.getPendingAmount());
        writer.println("Pending Bills," + billService.getPendingCount());
        writer.println("Active Staff," + userService.getTotalActiveStaff());
        writer.println("Active Dentists," + userService.getTotalDentists());
        writer.println();

        writer.println("Treatment,Base Cost (LKR),Appointments,Revenue Generated (LKR)");
        List<Map<String, Object>> treatmentStats = treatmentDAO.getTreatmentStats();
        for (Map<String, Object> stat : treatmentStats) {
            writer.println(
                    csvEscape(String.valueOf(stat.get("name"))) + "," +
                    stat.get("baseCost") + "," +
                    stat.get("appointmentCount") + "," +
                    stat.get("totalRevenue")
            );
        }

        writer.flush();
    }

    /**
     * Stream the executive analytics report as a downloadable PDF file.
     */
    private void exportPdf(HttpServletResponse res) throws IOException {
        try (PdfDocumentBuilder pdf = new PdfDocumentBuilder()) {
            pdf.writeBrandHeader("Sunrise Dental Clinic", "Executive Analytics & Reports");

            pdf.writeDocTitle("Report generated: " + DateUtil.formatDateTimeFull(java.time.LocalDateTime.now()));
            pdf.blankLine();

            pdf.writeSectionTitle("Key Metrics");
            pdf.writeTable(
                    new String[] {"Metric", "Value"},
                    java.util.List.of(
                            new String[] {"Total Appointments", String.valueOf(appointmentService.getTotalAppointments())},
                            new String[] {"Scheduled Appointments", String.valueOf(appointmentService.getScheduledCount())},
                            new String[] {"Completed Appointments", String.valueOf(appointmentService.getCompletedCount())},
                            new String[] {"This Month's Appointments", String.valueOf(appointmentService.getThisMonthCount())},
                            new String[] {"Total Patients", String.valueOf(patientService.getTotalPatients())},
                            new String[] {"New Patients This Month", String.valueOf(patientService.getNewPatientsThisMonth())},
                            new String[] {"Total Revenue (LKR)", String.format("%.2f", billService.getTotalRevenue())},
                            new String[] {"Monthly Revenue (LKR)", String.format("%.2f", billService.getMonthlyRevenue())},
                            new String[] {"Pending Amount (LKR)", String.format("%.2f", billService.getPendingAmount())},
                            new String[] {"Pending Bills", String.valueOf(billService.getPendingCount())},
                            new String[] {"Active Staff", String.valueOf(userService.getTotalActiveStaff())},
                            new String[] {"Active Dentists", String.valueOf(userService.getTotalDentists())}
                    ),
                    new float[] {300, 195}
            );
            pdf.blankLine();

            pdf.writeSectionTitle("Treatment Analytics");
            List<Map<String, Object>> treatmentStats = treatmentDAO.getTreatmentStats();
            List<String[]> treatmentRows = new java.util.ArrayList<>();
            for (Map<String, Object> stat : treatmentStats) {
                treatmentRows.add(new String[] {
                        String.valueOf(stat.get("name")),
                        String.valueOf(stat.get("appointmentCount")),
                        "LKR " + stat.get("totalRevenue")
                });
            }
            pdf.writeTable(
                    new String[] {"Treatment", "Appointments", "Revenue"},
                    treatmentRows,
                    new float[] {245, 100, 150}
            );

            pdf.writeToResponse(res, "sunrise-dental-report-" + DateUtil.getTodayCode() + ".pdf");
        }
    }

    /**
     * Quote a CSV field if it contains a comma, quote or newline.
     */
    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}