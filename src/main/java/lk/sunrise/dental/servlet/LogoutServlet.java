package lk.sunrise.dental.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lk.sunrise.dental.model.User;

import java.io.IOException;

/**
 * ================================================================
 * LogoutServlet.java
 * Handles secure session invalidation and sign-out
 *
 * GET  /logout → Invalidate session and redirect to login
 * POST /logout → Same as GET (supports form-based logout)
 *
 * Package : lk.sunrise.dental.servlet
 * ================================================================
 */
public class LogoutServlet extends HttpServlet {

    // ──────────────────────────────────────────────────────────────
    // GET - Process Logout
    // ──────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processLogout(req, res);
    }

    // ──────────────────────────────────────────────────────────────
    // POST - Process Logout (form button)
    // ──────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processLogout(req, res);
    }

    // ──────────────────────────────────────────────────────────────
    // LOGOUT LOGIC
    // ──────────────────────────────────────────────────────────────

    /**
     * Invalidate session and redirect to login page.
     */
    private void processLogout(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        HttpSession session = req.getSession(false);

        if (session != null) {
            // Log the logout event
            User user = (User) session.getAttribute("loggedInUser");
            if (user != null) {
                System.out.println("[LogoutServlet] 👋 Logout: " +
                                   user.getUsername() + " | Role: " + user.getRole());
            }

            // Remove all session attributes
            session.removeAttribute("loggedInUser");
            session.removeAttribute("userId");
            session.removeAttribute("userRole");
            session.removeAttribute("userName");

            // Invalidate session completely
            session.invalidate();
        }

        // Prevent browser caching of authenticated pages
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma",        "no-cache");
        res.setDateHeader("Expires",   0);

        // Redirect to login with logout confirmation
        res.sendRedirect(req.getContextPath() + "/login?msg=logout");
    }
}