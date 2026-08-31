package lk.sunrise.dental.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lk.sunrise.dental.model.User;
import lk.sunrise.dental.service.ServiceResult;
import lk.sunrise.dental.service.UserService;
import lk.sunrise.dental.util.ValidationUtil;

import java.io.IOException;
import java.util.List;

/**
 * ================================================================
 * StaffServlet.java
 * Staff provisioning and role management (Admin only)
 *
 * GET  /staff            → List all staff
 * GET  /staff/add        → Show add staff form
 * POST /staff/add        → Create new staff account
 * GET  /staff/edit?id=X  → Show edit form
 * POST /staff/edit       → Update staff account
 * POST /staff/toggle     → Enable/Disable account
 * POST /staff/password   → Change staff password
 *
 * Package : lk.sunrise.dental.servlet
 * ================================================================
 */
public class StaffServlet extends HttpServlet {

    private final UserService userService = new UserService();

    // ── View Path ──────────────────────────────────────────────────
    private static final String STAFF_VIEW = "/WEB-INF/views/staff/manage.jsp";

    // ──────────────────────────────────────────────────────────────
    // GET
    // ──────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Admin only
        if (!isAdmin(req)) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            handleList(req, res);
        } else {
            switch (pathInfo) {
                case "/add"  -> handleAddForm(req, res);
                case "/edit" -> handleEditForm(req, res);
                default -> handleList(req, res);
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

        if (!isAdmin(req)) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            res.sendRedirect(req.getContextPath() + "/staff");
            return;
        }

        switch (pathInfo) {
            case "/add"      -> handleAddSubmit(req, res);
            case "/edit"     -> handleEditSubmit(req, res);
            case "/toggle"   -> handleToggle(req, res);
            case "/password" -> handlePasswordChange(req, res);
            default -> res.sendRedirect(req.getContextPath() + "/staff");
        }
    }

    // ──────────────────────────────────────────────────────────────
    // HANDLERS
    // ──────────────────────────────────────────────────────────────

    /**
     * GET /staff - List all staff members
     */
    private void handleList(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String success = req.getParameter("success");
        String error   = req.getParameter("error");

        if (success != null) req.setAttribute("successMsg", success);
        if (error   != null) req.setAttribute("errorMsg",   error);

        List<User> allStaff = userService.getAllUsers();

        req.setAttribute("staffList",     allStaff);
        req.setAttribute("totalStaff",    userService.getTotalActiveStaff());
        req.setAttribute("totalDentists", userService.getTotalDentists());
        req.setAttribute("pageTitle",     "Staff Management");
        req.setAttribute("formMode",      "list");

        req.getRequestDispatcher(STAFF_VIEW).forward(req, res);
    }

    /**
     * GET /staff/add - Show add staff form
     */
    private void handleAddForm(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setAttribute("pageTitle", "Add New Staff");
        req.setAttribute("formMode",  "add");
        req.getRequestDispatcher(STAFF_VIEW).forward(req, res);
    }

    /**
     * POST /staff/add - Create new staff account
     */
    private void handleAddSubmit(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String username       = req.getParameter("username");
        String fullName       = req.getParameter("fullName");
        String password       = req.getParameter("password");
        String role           = req.getParameter("role");
        String email          = req.getParameter("email");
        String contact        = req.getParameter("contact");
        String specialization = req.getParameter("specialization");
        String consultFeeStr  = req.getParameter("consultFee");

        double consultFee = ValidationUtil.parseDoubleSafe(consultFeeStr, 1500.00);

        ServiceResult result = userService.createUser(
                username, fullName, password, role,
                email, contact, specialization, consultFee
        );

        if (result.isSuccess()) {
            res.sendRedirect(req.getContextPath() +
                    "/staff?success=" +
                    java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
        } else {
            req.setAttribute("errorMsg",       result.getMessage());
            req.setAttribute("pageTitle",      "Add New Staff");
            req.setAttribute("formMode",       "add");

            // Preserve form values
            req.setAttribute("fUsername",      username);
            req.setAttribute("fFullName",      fullName);
            req.setAttribute("fRole",          role);
            req.setAttribute("fEmail",         email);
            req.setAttribute("fContact",       contact);
            req.setAttribute("fSpecialization",specialization);
            req.setAttribute("fConsultFee",    consultFeeStr);

            req.getRequestDispatcher(STAFF_VIEW).forward(req, res);
        }
    }

    /**
     * GET /staff/edit?id=X - Show edit form
     */
    private void handleEditForm(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String idStr = req.getParameter("id");
        try {
            int  id   = Integer.parseInt(idStr);
            User user = userService.getUserById(id);

            if (user == null) {
                res.sendRedirect(req.getContextPath() + "/staff");
                return;
            }

            req.setAttribute("editUser",  user);
            req.setAttribute("pageTitle", "Edit Staff - " + user.getFullName());
            req.setAttribute("formMode",  "edit");
            req.getRequestDispatcher(STAFF_VIEW).forward(req, res);

        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/staff");
        }
    }

    /**
     * POST /staff/edit - Update staff account
     */
    private void handleEditSubmit(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String idStr          = req.getParameter("userId");
        String fullName       = req.getParameter("fullName");
        String role           = req.getParameter("role");
        String email          = req.getParameter("email");
        String contact        = req.getParameter("contact");
        String specialization = req.getParameter("specialization");
        String consultFeeStr  = req.getParameter("consultFee");
        String isActiveStr    = req.getParameter("isActive");

        double  consultFee = ValidationUtil.parseDoubleSafe(consultFeeStr, 1500.00);
        boolean isActive   = "true".equals(isActiveStr);

        try {
            int userId = Integer.parseInt(idStr);

            ServiceResult result = userService.updateUser(
                    userId, fullName, email, contact,
                    role, specialization, consultFee, isActive
            );

            if (result.isSuccess()) {
                res.sendRedirect(req.getContextPath() +
                        "/staff?success=" +
                        java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            } else {
                User editUser = userService.getUserById(userId);
                req.setAttribute("editUser",  editUser);
                req.setAttribute("errorMsg",  result.getMessage());
                req.setAttribute("pageTitle", "Edit Staff");
                req.setAttribute("formMode",  "edit");
                req.getRequestDispatcher(STAFF_VIEW).forward(req, res);
            }

        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/staff");
        }
    }

    /**
     * POST /staff/toggle - Enable/Disable staff account
     */
    private void handleToggle(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        // Prevent admin from disabling their own account
        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");
        String idStr          = req.getParameter("userId");

        try {
            int userId = Integer.parseInt(idStr);

            if (userId == loggedInUser.getId()) {
                res.sendRedirect(req.getContextPath() +
                        "/staff?error=You+cannot+disable+your+own+account.");
                return;
            }

            ServiceResult result = userService.toggleStatus(userId);

            if (result.isSuccess()) {
                res.sendRedirect(req.getContextPath() +
                        "/staff?success=" +
                        java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            } else {
                res.sendRedirect(req.getContextPath() +
                        "/staff?error=" +
                        java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            }

        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/staff");
        }
    }

    /**
     * POST /staff/password - Change staff password
     */
    private void handlePasswordChange(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        String idStr      = req.getParameter("userId");
        String newPassword = req.getParameter("newPassword");
        String confirm    = req.getParameter("confirmPassword");

        // Passwords must match
        if (newPassword == null || !newPassword.equals(confirm)) {
            res.sendRedirect(req.getContextPath() +
                    "/staff?error=Passwords+do+not+match.");
            return;
        }

        try {
            int userId = Integer.parseInt(idStr);

            ServiceResult result = userService.changePassword(userId, newPassword);

            if (result.isSuccess()) {
                res.sendRedirect(req.getContextPath() +
                        "/staff?success=" +
                        java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            } else {
                res.sendRedirect(req.getContextPath() +
                        "/staff?error=" +
                        java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            }

        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/staff");
        }
    }

    // ── Helper ─────────────────────────────────────────────────────

    /**
     * Check if current session user is Admin.
     */
    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && user.isAdmin();
    }
}