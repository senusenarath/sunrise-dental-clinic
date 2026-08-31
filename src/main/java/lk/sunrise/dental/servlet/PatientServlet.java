package lk.sunrise.dental.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lk.sunrise.dental.model.Patient;
import lk.sunrise.dental.model.User;
import lk.sunrise.dental.service.AppointmentService;
import lk.sunrise.dental.service.BillService;
import lk.sunrise.dental.service.PatientService;
import lk.sunrise.dental.service.ServiceResult;
import lk.sunrise.dental.util.CsvUtil;
import lk.sunrise.dental.util.DateUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * PatientServlet.java
 * Handles patient registration, search and management
 *
 * GET  /patients              → List all patients
 * GET  /patients?search=term  → Search patients
 * GET  /patients/view?id=X    → View patient detail
 * GET  /patients/add          → Show registration form
 * POST /patients/add          → Save new patient
 * GET  /patients/edit?id=X    → Show edit form
 * POST /patients/edit         → Update patient
 * POST /patients/delete       → Deactivate patient
 *
 * Package : lk.sunrise.dental.servlet
 * ================================================================
 */
public class PatientServlet extends HttpServlet {

    private final PatientService     patientService     = new PatientService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final BillService        billService        = new BillService();

    // ── View Paths ─────────────────────────────────────────────────
    private static final String LIST_VIEW   = "/WEB-INF/views/patient/list.jsp";
    private static final String DETAIL_VIEW = "/WEB-INF/views/patient/detail.jsp";
    private static final String FORM_VIEW   = "/WEB-INF/views/patient/form.jsp";

    // ──────────────────────────────────────────────────────────────
    // GET
    // ──────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo(); // e.g. /view, /add, /edit

        if (pathInfo == null || pathInfo.equals("/")) {
            handleList(req, res);
        } else {
            switch (pathInfo) {
                case "/view"   -> handleView(req, res);
                case "/add"    -> handleAddForm(req, res);
                case "/edit"   -> handleEditForm(req, res);
                default        -> res.sendRedirect(req.getContextPath() + "/patients");
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
            res.sendRedirect(req.getContextPath() + "/patients");
            return;
        }

        switch (pathInfo) {
            case "/add"    -> handleAddSubmit(req, res);
            case "/edit"   -> handleEditSubmit(req, res);
            case "/delete" -> handleDelete(req, res);
            default        -> res.sendRedirect(req.getContextPath() + "/patients");
        }
    }

    // ──────────────────────────────────────────────────────────────
    // HANDLERS
    // ──────────────────────────────────────────────────────────────

    /**
     * GET /patients - List all or search patients
     */
    private void handleList(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String keyword  = req.getParameter("search");
        List<Patient> patients;

        if (keyword != null && !keyword.trim().isEmpty()) {
            patients = patientService.searchPatients(keyword.trim());
            req.setAttribute("searchKeyword", keyword.trim());
            req.setAttribute("searchCount",   patients.size());
        } else {
            patients = patientService.getAllPatients();
        }

        if ("csv".equalsIgnoreCase(req.getParameter("export"))) {
            exportPatientsCsv(res, patients);
            return;
        }

        req.setAttribute("patients",      patients);
        req.setAttribute("totalPatients", patientService.getTotalPatients());
        req.setAttribute("pageTitle",     "Patient Directory");

        req.getRequestDispatcher(LIST_VIEW).forward(req, res);
    }

    /**
     * GET /patients?export=csv - Download the current patient list as CSV
     */
    private void exportPatientsCsv(HttpServletResponse res, List<Patient> patients) throws IOException {
        List<String[]> rows = new ArrayList<>();
        for (Patient p : patients) {
            rows.add(new String[] {
                    p.getPatientCode(),
                    p.getFullName(),
                    p.getGender(),
                    String.valueOf(p.getAge()),
                    p.getContact(),
                    p.getEmail(),
                    p.getBloodType(),
                    p.getAddress()
            });
        }

        CsvUtil.writeCsv(res,
                "sunrise-patients-" + DateUtil.getTodayCode() + ".csv",
                new String[] {"Patient Code", "Full Name", "Gender", "Age", "Contact", "Email", "Blood Type", "Address"},
                rows);
    }

    /**
     * GET /patients/view?id=X - View patient details
     */
    private void handleView(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String idStr = req.getParameter("id");

        if (idStr == null || idStr.trim().isEmpty()) {
            res.sendRedirect(req.getContextPath() + "/patients");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);

            // Get patient with appointment statistics
            Patient patient = patientService.getPatientWithStats(id);

            if (patient == null) {
                req.setAttribute("errorMsg", "Patient not found.");
                req.getRequestDispatcher(LIST_VIEW).forward(req, res);
                return;
            }

            // Get patient's appointment history
            req.setAttribute("patient",        patient);
            req.setAttribute("appointments",   appointmentService.getAppointmentsByPatient(id));
            req.setAttribute("bills",          billService.getBillsByPatient(id));
            req.setAttribute("pageTitle",      "Patient Profile - " + patient.getFullName());

            req.getRequestDispatcher(DETAIL_VIEW).forward(req, res);

        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/patients");
        }
    }

    /**
     * GET /patients/add - Show patient registration form
     */
    private void handleAddForm(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User loggedInUser   = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        req.setAttribute("pageTitle", "Register New Patient");
        req.setAttribute("formMode",  "add");
        req.getRequestDispatcher(FORM_VIEW).forward(req, res);
    }

    /**
     * POST /patients/add - Save new patient
     */
    private void handleAddSubmit(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User loggedInUser   = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        // Get form fields
        String fullName     = req.getParameter("fullName");
        String dob          = req.getParameter("dateOfBirth");
        String gender       = req.getParameter("gender");
        String address      = req.getParameter("address");
        String contact      = req.getParameter("contact");
        String email        = req.getParameter("email");
        String bloodType    = req.getParameter("bloodType");
        String allergies    = req.getParameter("allergies");
        String medicalNotes = req.getParameter("medicalNotes");

        // Register patient via service
        ServiceResult result = patientService.registerPatient(
                fullName, dob, gender, address, contact,
                email, bloodType, allergies, medicalNotes,
                loggedInUser.getId()
        );

        if (result.isSuccess()) {
            // Welcome email is sent by PatientService via the Observer-pattern
            // NotificationListener, so both this JSP flow and the REST API
            // get consistent notification behaviour.
            res.sendRedirect(req.getContextPath() +
                    "/patients/view?id=" + result.getGeneratedId() +
                    "&success=" + java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
        } else {
            req.setAttribute("errorMsg",     result.getMessage());
            req.setAttribute("pageTitle",    "Register New Patient");
            req.setAttribute("formMode",     "add");
            req.setAttribute("fullName",     fullName);
            req.setAttribute("dateOfBirth",  dob);
            req.setAttribute("gender",       gender);
            req.setAttribute("address",      address);
            req.setAttribute("contact",      contact);
            req.setAttribute("email",        email);
            req.setAttribute("bloodType",    bloodType);
            req.setAttribute("allergies",    allergies);
            req.setAttribute("medicalNotes", medicalNotes);
            req.getRequestDispatcher(FORM_VIEW).forward(req, res);
        }
    }

    /**
     * GET /patients/edit?id=X - Show edit form
     */
    private void handleEditForm(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User loggedInUser   = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        String idStr = req.getParameter("id");

        if (idStr == null) {
            res.sendRedirect(req.getContextPath() + "/patients");
            return;
        }

        try {
            int id          = Integer.parseInt(idStr);
            Patient patient = patientService.getPatientById(id);

            if (patient == null) {
                res.sendRedirect(req.getContextPath() + "/patients");
                return;
            }

            req.setAttribute("patient",   patient);
            req.setAttribute("pageTitle", "Edit Patient - " + patient.getFullName());
            req.setAttribute("formMode",  "edit");
            req.getRequestDispatcher(FORM_VIEW).forward(req, res);

        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/patients");
        }
    }

    /**
     * POST /patients/edit - Update patient details
     */
    private void handleEditSubmit(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User loggedInUser   = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        String idStr        = req.getParameter("patientId");
        String fullName     = req.getParameter("fullName");
        String dob          = req.getParameter("dateOfBirth");
        String gender       = req.getParameter("gender");
        String address      = req.getParameter("address");
        String contact      = req.getParameter("contact");
        String email        = req.getParameter("email");
        String bloodType    = req.getParameter("bloodType");
        String allergies    = req.getParameter("allergies");
        String medicalNotes = req.getParameter("medicalNotes");

        int patientId;
        try {
            patientId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/patients");
            return;
        }

        ServiceResult result = patientService.updatePatient(
                patientId, fullName, dob, gender, address,
                contact, email, bloodType, allergies, medicalNotes
        );

        if (result.isSuccess()) {
            res.sendRedirect(req.getContextPath() +
                    "/patients/view?id=" + patientId +
                    "&success=" + java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
        } else {
            Patient patient = patientService.getPatientById(patientId);
            req.setAttribute("patient",      patient);
            req.setAttribute("errorMsg",     result.getMessage());
            req.setAttribute("pageTitle",    "Edit Patient");
            req.setAttribute("formMode",     "edit");
            req.getRequestDispatcher(FORM_VIEW).forward(req, res);
        }
    }

    /**
     * POST /patients/delete - Deactivate patient
     */
    private void handleDelete(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Only admins can delete patients
        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");

        if (!loggedInUser.isAdmin()) {
            res.sendRedirect(req.getContextPath() +
                    "/patients?error=Only+administrators+can+remove+patient+records.");
            return;
        }

        String idStr = req.getParameter("patientId");
        try {
            int           patientId = Integer.parseInt(idStr);
            ServiceResult result    = patientService.deactivatePatient(patientId);

            if (result.isSuccess()) {
                res.sendRedirect(req.getContextPath() +
                        "/patients?success=" +
                        java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            } else {
                res.sendRedirect(req.getContextPath() +
                        "/patients?error=" +
                        java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            }
        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/patients");
        }
    }
}