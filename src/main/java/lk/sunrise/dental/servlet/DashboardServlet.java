package lk.sunrise.dental.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lk.sunrise.dental.model.Appointment;
import lk.sunrise.dental.model.User;
import lk.sunrise.dental.service.AppointmentService;
import lk.sunrise.dental.service.BillService;
import lk.sunrise.dental.service.PatientService;
import lk.sunrise.dental.service.UserService;

import java.io.IOException;
import java.util.List;

/**
 * ================================================================
 * DashboardServlet.java
 * Aggregates role-specific KPIs and clinical metrics
 *
 * GET /dashboard → Load and display dashboard with stats
 *
 * Package : lk.sunrise.dental.servlet
 * ================================================================
 */
public class DashboardServlet extends HttpServlet {

    private final AppointmentService appointmentService = new AppointmentService();
    private final PatientService     patientService     = new PatientService();
    private final BillService        billService        = new BillService();
    private final UserService        userService        = new UserService();

    private static final String VIEW = "/WEB-INF/views/dashboard/dashboard.jsp";

    // ──────────────────────────────────────────────────────────────
    // GET - Load Dashboard
    // ──────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("loggedInUser");

        // ── Access Denied Message ──────────────────────────────────
        String error = req.getParameter("error");
        if ("access_denied".equals(error)) {
            req.setAttribute("errorMsg",
                    "⛔ Access Denied. You do not have permission to view that page.");
        }

        // ── Success Message (e.g. from Send Reminders) ─────────────
        String success = req.getParameter("success");
        if (success != null && !success.trim().isEmpty()) {
            req.setAttribute("successMsg", success);
        }

        try {
            // ── Appointment Stats ──────────────────────────────────
            req.setAttribute("totalAppointments",  appointmentService.getTotalAppointments());
            req.setAttribute("scheduledCount",     appointmentService.getScheduledCount());
            req.setAttribute("completedCount",     appointmentService.getCompletedCount());
            req.setAttribute("thisMonthApts",      appointmentService.getThisMonthCount());

            // ── Patient Stats ──────────────────────────────────────
            req.setAttribute("totalPatients",      patientService.getTotalPatients());
            req.setAttribute("newPatientsMonth",   patientService.getNewPatientsThisMonth());

            // ── Financial Stats (Admin + Receptionist only) ────────
            if (!user.isDentist()) {
                req.setAttribute("totalRevenue",   billService.getTotalRevenue());
                req.setAttribute("monthlyRevenue", billService.getMonthlyRevenue());
                req.setAttribute("pendingAmount",  billService.getPendingAmount());
                req.setAttribute("pendingBills",   billService.getPendingCount());
            }

            // ── Staff Stats (Admin only) ───────────────────────────
            if (user.isAdmin()) {
                req.setAttribute("totalStaff",    userService.getTotalActiveStaff());
                req.setAttribute("totalDentists", userService.getTotalDentists());
            }

            // ── Today's Appointments List ──────────────────────────
            // Dentists see only their own patients for today; other
            // roles see the full clinic-wide schedule.
            List<Appointment> todayAptList = user.isDentist()
                    ? appointmentService.getTodayAppointmentsByDentist(user.getId())
                    : appointmentService.getTodayAppointments();

            req.setAttribute("todayAptList",      todayAptList);
            req.setAttribute("todayAppointments", todayAptList.size());

        } catch (Exception e) {
            System.err.println("[DashboardServlet] Error loading stats: " + e.getMessage());
            req.setAttribute("errorMsg", "Error loading dashboard data. Please refresh.");
        }

        req.getRequestDispatcher(VIEW).forward(req, res);
    }
}