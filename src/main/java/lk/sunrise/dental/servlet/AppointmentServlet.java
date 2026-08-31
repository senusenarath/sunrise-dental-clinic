package lk.sunrise.dental.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lk.sunrise.dental.dao.TreatmentDAO;
import lk.sunrise.dental.model.Appointment;
import lk.sunrise.dental.model.User;
import lk.sunrise.dental.service.AppointmentService;
import lk.sunrise.dental.service.PatientService;
import lk.sunrise.dental.service.ServiceResult;
import lk.sunrise.dental.service.UserService;
import lk.sunrise.dental.util.CsvUtil;
import lk.sunrise.dental.util.DateUtil;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ================================================================
 * AppointmentServlet.java
 * Handles appointment booking, filtering and status updates
 *
 * GET  /appointments                   → List appointments
 * GET  /appointments?status=X          → Filter by status
 * GET  /appointments?search=X          → Search appointments
 * GET  /appointments/register          → Show booking form
 * POST /appointments/register          → Book new appointment
 * GET  /appointments/view?id=X         → View appointment detail
 * GET  /appointments/update-treatment?id=X → Show update form
 * POST /appointments/update-treatment  → Update clinical status
 * POST /appointments/cancel            → Cancel appointment
 *
 * Package : lk.sunrise.dental.servlet
 * ================================================================
 */
public class AppointmentServlet extends HttpServlet {

    private final AppointmentService appointmentService = new AppointmentService();
    private final PatientService     patientService     = new PatientService();
    private final UserService        userService        = new UserService();
    private final TreatmentDAO       treatmentDAO       = new TreatmentDAO();

    // ── View Paths ─────────────────────────────────────────────────
    private static final String LIST_VIEW        = "/WEB-INF/views/appointment/list.jsp";
    private static final String VIEW_VIEW        = "/WEB-INF/views/appointment/view.jsp";
    private static final String REGISTER_VIEW    = "/WEB-INF/views/appointment/register.jsp";
    private static final String UPDATE_VIEW      = "/WEB-INF/views/appointment/update.jsp";
    private static final String RESCHEDULE_VIEW  = "/WEB-INF/views/appointment/reschedule.jsp";

    // ──────────────────────────────────────────────────────────────
    // GET
    // ──────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            handleList(req, res);
        } else {
            switch (pathInfo) {
                case "/register"         -> handleRegisterForm(req, res);
                case "/update-treatment" -> handleUpdateForm(req, res);
                case "/view"             -> handleView(req, res);
                case "/reschedule"       -> handleRescheduleForm(req, res);
                default -> res.sendRedirect(req.getContextPath() + "/appointments");
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // POST
    // ──────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null) {
            res.sendRedirect(req.getContextPath() + "/appointments");
            return;
        }

        switch (pathInfo) {
            case "/register"         -> handleRegisterSubmit(req, res);
            case "/update-treatment" -> handleUpdateSubmit(req, res);
            case "/reschedule"       -> handleRescheduleSubmit(req, res);
            case "/cancel"           -> handleCancel(req, res);
            case "/send-reminders"   -> handleSendReminders(req, res);
            default -> res.sendRedirect(req.getContextPath() + "/appointments");
        }
    }

    // ──────────────────────────────────────────────────────────────
    // HANDLERS
    // ──────────────────────────────────────────────────────────────

    /**
     * GET /appointments - List with optional filters
     */
    private void handleList(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String status  = req.getParameter("status");
        String search  = req.getParameter("search");
        String success = req.getParameter("success");
        String error   = req.getParameter("error");

        List<Appointment> appointments;

        if (search != null && !search.trim().isEmpty()) {
            appointments = appointmentService.searchAppointments(search.trim());
            req.setAttribute("searchKeyword", search.trim());
        } else if (status != null && !status.trim().isEmpty()) {
            appointments = appointmentService.getAppointmentsByStatus(status.trim());
            req.setAttribute("statusFilter", status.trim());
        } else {
            appointments = appointmentService.getAllAppointments();
        }

        if ("csv".equalsIgnoreCase(req.getParameter("export"))) {
            exportAppointmentsCsv(res, appointments);
            return;
        }

        if (success != null) req.setAttribute("successMsg", success);
        if (error   != null) req.setAttribute("errorMsg",   error);

        req.setAttribute("appointments",      appointments);
        req.setAttribute("totalAppointments", appointmentService.getTotalAppointments());
        req.setAttribute("scheduledCount",    appointmentService.getScheduledCount());
        req.setAttribute("completedCount",    appointmentService.getCompletedCount());
        req.setAttribute("todayCount",        appointmentService.getTodayCount());
        req.setAttribute("pageTitle",         "Appointment Schedule");

        req.getRequestDispatcher(LIST_VIEW).forward(req, res);
    }

    /**
     * GET /appointments?export=csv - Download the current appointment list as CSV
     */
    private void exportAppointmentsCsv(HttpServletResponse res, List<Appointment> appointments) throws IOException {
        List<String[]> rows = new ArrayList<>();
        for (Appointment apt : appointments) {
            rows.add(new String[] {
                    apt.getAptCode(),
                    apt.getPatientName(),
                    apt.getDentistName(),
                    apt.getTreatmentName(),
                    String.valueOf(apt.getAptDate()),
                    String.valueOf(apt.getAptTime()),
                    apt.getStatus(),
                    apt.getNotes()
            });
        }

        CsvUtil.writeCsv(res,
                "sunrise-appointments-" + DateUtil.getTodayCode() + ".csv",
                new String[] {"Apt Code", "Patient", "Dentist", "Treatment", "Date", "Time", "Status", "Notes"},
                rows);
    }

    /**
     * GET /appointments/view?id=X
     */
    private void handleView(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String idStr = req.getParameter("id");
        try {
            int id = Integer.parseInt(idStr);
            Appointment apt = appointmentService.getAppointmentById(id);

            if (apt == null) {
                res.sendRedirect(req.getContextPath() + "/appointments");
                return;
            }

            String success = req.getParameter("success");
            if (success != null) req.setAttribute("successMsg", success);

            req.setAttribute("appointment", apt);
            req.setAttribute("pageTitle",   "Appointment - " + apt.getAptCode());
            req.getRequestDispatcher(VIEW_VIEW).forward(req, res);

        } catch (Exception e) {
            res.sendRedirect(req.getContextPath() + "/appointments");
        }
    }

    /**
     * GET /appointments/register - Show booking form
     */
    private void handleRegisterForm(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Receptionists and Admins can book appointments
        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        // Load dropdown data
        List<User> dentists = userService.getAllDentists();
        req.setAttribute("patients",    patientService.getAllPatients());
        req.setAttribute("dentists",    dentists);
        req.setAttribute("dentistAvailability", buildDentistAvailability(dentists));
        req.setAttribute("treatments",  treatmentDAO.getAllTreatments());
        req.setAttribute("pageTitle",   "Book New Appointment");

        // Pre-select patient if coming from patient detail page
        String patientId = req.getParameter("patientId");
        if (patientId != null) {
            req.setAttribute("preselectedPatientId", patientId);
        }

        req.getRequestDispatcher(REGISTER_VIEW).forward(req, res);
    }

    /**
    * POST /appointments/register - Book new appointment WITH email notification
     */
    private void handleRegisterSubmit(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        String patientId    = req.getParameter("patientId");
        String dentistId    = req.getParameter("dentistId");
        String treatmentId  = req.getParameter("treatmentId");
        String aptDate      = req.getParameter("aptDate");
        String aptTime      = req.getParameter("aptTime");
        String notes        = req.getParameter("notes");

        ServiceResult result = appointmentService.bookAppointment(
                patientId, dentistId, treatmentId,
                aptDate, aptTime, notes,
                loggedInUser.getId()
        );

        if (result.isSuccess()) {
            // Confirmation email is sent by AppointmentService via the
            // Observer-pattern NotificationListener, so both this JSP
            // flow and the REST API get consistent notification behaviour.
            res.sendRedirect(req.getContextPath() +
                    "/appointments?success=" +
                    java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
        } else {
            List<User> retryDentists = userService.getAllDentists();
            req.setAttribute("errorMsg",   result.getMessage());
            req.setAttribute("patients",   patientService.getAllPatients());
            req.setAttribute("dentists",   retryDentists);
            req.setAttribute("dentistAvailability", buildDentistAvailability(retryDentists));
            req.setAttribute("treatments", treatmentDAO.getAllTreatments());
            req.setAttribute("pageTitle",  "Book New Appointment");
            req.setAttribute("selPatientId",   patientId);
            req.setAttribute("selDentistId",   dentistId);
            req.setAttribute("selTreatmentId", treatmentId);
            req.setAttribute("selAptDate",     aptDate);
            req.setAttribute("selAptTime",     aptTime);
            req.setAttribute("selNotes",       notes);
            req.getRequestDispatcher(REGISTER_VIEW).forward(req, res);
        }
    }

    /**
     * GET /appointments/update-treatment?id=X - Show clinical update form
     */
    private void handleUpdateForm(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String idStr = req.getParameter("id");
        try {
            int id          = Integer.parseInt(idStr);
            Appointment apt = appointmentService.getAppointmentById(id);

            if (apt == null) {
                res.sendRedirect(req.getContextPath() + "/appointments");
                return;
            }

            req.setAttribute("appointment", apt);
            req.setAttribute("pageTitle",   "Update Treatment - " + apt.getAptCode());
            req.getRequestDispatcher(UPDATE_VIEW).forward(req, res);

        } catch (Exception e) {
            res.sendRedirect(req.getContextPath() + "/appointments");
        }
    }

    /**
     * POST /appointments/update-treatment - Update clinical status
     */
    private void handleUpdateSubmit(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User loggedInUser   = (User) session.getAttribute("loggedInUser");

        String aptIdStr = req.getParameter("appointmentId");
        String status   = req.getParameter("status");
        String notes    = req.getParameter("notes");

        int aptId;
        try {
            aptId = Integer.parseInt(aptIdStr);
        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/appointments");
            return;
        }

        ServiceResult result = appointmentService.updateTreatmentStatus(
                aptId, status, notes, loggedInUser.getRole()
        );

        if (result.isSuccess()) {
            res.sendRedirect(req.getContextPath() +
                    "/appointments?success=" +
                    java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
        } else {
            Appointment apt = appointmentService.getAppointmentById(aptId);
            req.setAttribute("appointment", apt);
            req.setAttribute("errorMsg",    result.getMessage());
            req.setAttribute("pageTitle",   "Update Treatment");
            req.getRequestDispatcher(UPDATE_VIEW).forward(req, res);
        }
    }

    /**
     * GET /appointments/reschedule?id=X - Show reschedule form
     */
    private void handleRescheduleForm(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        String idStr = req.getParameter("id");
        try {
            int id = Integer.parseInt(idStr);
            Appointment apt = appointmentService.getAppointmentById(id);

            if (apt == null || !apt.isEditable()) {
                res.sendRedirect(req.getContextPath() + "/appointments");
                return;
            }

            List<User> rescheduleDentists = userService.getAllDentists();
            req.setAttribute("appointment", apt);
            req.setAttribute("dentists",    rescheduleDentists);
            req.setAttribute("dentistAvailability", buildDentistAvailability(rescheduleDentists));
            req.setAttribute("pageTitle",   "Reschedule - " + apt.getAptCode());
            req.getRequestDispatcher(RESCHEDULE_VIEW).forward(req, res);

        } catch (Exception e) {
            res.sendRedirect(req.getContextPath() + "/appointments");
        }
    }

    /**
     * POST /appointments/reschedule - Update date, time and/or dentist
     */
    private void handleRescheduleSubmit(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        String aptIdStr      = req.getParameter("appointmentId");
        String newDate       = req.getParameter("aptDate");
        String newTime       = req.getParameter("aptTime");
        String dentistIdStr  = req.getParameter("dentistId");

        int aptId;
        int dentistId;
        try {
            aptId     = Integer.parseInt(aptIdStr);
            dentistId = Integer.parseInt(dentistIdStr);
        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/appointments");
            return;
        }

        ServiceResult result = appointmentService.rescheduleAppointment(
                aptId, newDate, newTime, dentistId
        );

        if (result.isSuccess()) {
            res.sendRedirect(req.getContextPath() +
                    "/appointments/view?id=" + aptId +
                    "&success=" + java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
        } else {
            Appointment apt = appointmentService.getAppointmentById(aptId);
            List<User> retryDentists = userService.getAllDentists();
            req.setAttribute("appointment", apt);
            req.setAttribute("dentists",    retryDentists);
            req.setAttribute("dentistAvailability", buildDentistAvailability(retryDentists));
            req.setAttribute("errorMsg",    result.getMessage());
            req.setAttribute("pageTitle",   "Reschedule Appointment");
            req.getRequestDispatcher(RESCHEDULE_VIEW).forward(req, res);
        }
    }

    /**
     * Build a map of dentistId -> "busy until HH:mm a" for dentists who are
     * currently with a patient, so the booking/reschedule dropdowns can show
     * a real-time availability badge next to each dentist's name.
     */
    private Map<Integer, String> buildDentistAvailability(List<User> dentists) {
        Map<Integer, String> availability = new HashMap<>();
        for (User dentist : dentists) {
            LocalTime busyUntil = appointmentService.getDentistBusyUntil(dentist.getId());
            if (busyUntil != null) {
                availability.put(dentist.getId(), DateUtil.formatTimeDisplay(busyUntil));
            }
        }
        return availability;
    }

    /**
     * POST /appointments/send-reminders - Email tomorrow's patients a reminder
     */
    private void handleSendReminders(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        int sent = appointmentService.sendRemindersForTomorrow();
        String message = sent == 0
                ? "No upcoming appointments tomorrow needed a reminder."
                : sent + " reminder email(s) queued for tomorrow's appointments.";

        res.sendRedirect(req.getContextPath() +
                "/dashboard?success=" + java.net.URLEncoder.encode(message, "UTF-8"));
    }

    /**
     * POST /appointments/cancel - Cancel appointment
     */
    private void handleCancel(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");
        String aptIdStr       = req.getParameter("appointmentId");

        try {
            int aptId = Integer.parseInt(aptIdStr);

            ServiceResult result = appointmentService.cancelAppointment(
                    aptId, loggedInUser.getRole()
            );

            // Cancellation email is sent by AppointmentService via the
            // Observer-pattern NotificationListener.
            if (result.isSuccess()) {
                res.sendRedirect(req.getContextPath() +
                        "/appointments?success=" +
                        java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            } else {
                res.sendRedirect(req.getContextPath() +
                        "/appointments?error=" +
                        java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            }

        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/appointments");
        }
    }
}