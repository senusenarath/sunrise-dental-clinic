package lk.sunrise.dental.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lk.sunrise.dental.model.User;
import lk.sunrise.dental.service.UserService;

import java.io.IOException;

/**
 * ================================================================
 * LoginServlet.java
 * Handles staff authentication with Cookie support
 *
 * GET  /login → Show login page
 * POST /login → Process login + set remember me cookie
 * ================================================================
 */
public class LoginServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private static final String LOGIN_VIEW   = "/WEB-INF/views/auth/login.jsp";
    private static final String COOKIE_NAME  = "sunrise_remembered_user";
    private static final int    COOKIE_AGE   = 7 * 24 * 60 * 60; // 7 days

    // ──────────────────────────────────────────────────────────────
    // GET - Show Login Page
    // ──────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Already logged in → dashboard
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("loggedInUser") != null) {
            res.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        // Check remember me cookie
        String rememberedUsername = getCookieValue(req, COOKIE_NAME);
        if (rememberedUsername != null) {
            req.setAttribute("rememberedUsername", rememberedUsername);
            req.setAttribute("rememberChecked", "checked");
        }

        // Messages
        String msg = req.getParameter("msg");
        String err = req.getParameter("error");
        if ("logout".equals(msg)) {
            req.setAttribute("successMsg", "You have been logged out successfully.");
        }
        if ("session_expired".equals(err)) {
            req.setAttribute("errorMsg", "Your session has expired. Please log in again.");
        }

        req.getRequestDispatcher(LOGIN_VIEW).forward(req, res);
    }

    // ──────────────────────────────────────────────────────────────
    // POST - Process Login
    // ──────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String username   = req.getParameter("username");
        String password   = req.getParameter("password");
        String rememberMe = req.getParameter("rememberMe"); // checkbox

        // Validation
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            req.setAttribute("errorMsg", "Username and password are required.");
            req.setAttribute("username", username);
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, res);
            return;
        }

        // Authenticate
        User user = userService.login(username.trim(), password);

        if (user != null) {
            // ── Remember Me Cookie ─────────────────────────────
            if ("on".equals(rememberMe)) {
                // Set cookie for 7 days
                Cookie cookie = new Cookie(COOKIE_NAME, username.trim());
                cookie.setMaxAge(COOKIE_AGE);
                cookie.setPath(req.getContextPath());
                cookie.setHttpOnly(true); // Security: no JS access
                res.addCookie(cookie);
                System.out.println("[LoginServlet] 🍪 Remember me cookie set for: " + username);
            } else {
                // Clear existing cookie if unchecked
                Cookie cookie = new Cookie(COOKIE_NAME, "");
                cookie.setMaxAge(0);
                cookie.setPath(req.getContextPath());
                res.addCookie(cookie);
            }

            // ── Create Session ─────────────────────────────────
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) oldSession.invalidate();

            HttpSession session = req.getSession(true);
            session.setAttribute("loggedInUser", user);
            session.setAttribute("userId",       user.getId());
            session.setAttribute("userRole",     user.getRole());
            session.setAttribute("userName",     user.getFullName());
            session.setMaxInactiveInterval(30 * 60);

            System.out.println("[LoginServlet] ✅ Login: " +
                    user.getUsername() + " | Role: " + user.getRole());

            res.sendRedirect(req.getContextPath() + "/dashboard");

        } else {
            System.out.println("[LoginServlet] ❌ Failed login: " + username);
            req.setAttribute("errorMsg",
                    "Invalid username or password. Please try again.");
            req.setAttribute("username", username);
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, res);
        }
    }

    // ── Helper - Get Cookie Value ───────────────────────────────
    private String getCookieValue(HttpServletRequest req, String cookieName) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}