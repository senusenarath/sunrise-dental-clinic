package lk.sunrise.dental.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lk.sunrise.dental.model.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * ================================================================
 * AuthFilter.java
 * Session Security Filter & Role Authorization Guard
 *
 * Intercepts ALL requests and:
 * 1. Allows public URLs (login, assets) without session check
 * 2. Redirects unauthenticated users to login page
 * 3. Enforces role-based access control for restricted URLs
 *
 * Package : lk.sunrise.dental.filter
 * ================================================================
 */
public class AuthFilter implements Filter {

    // ── Public URLs (no login required) ───────────────────────────
    private static final List<String> PUBLIC_URLS = Arrays.asList(
            "/login",
            "/index.jsp"
    );

    // ── Static asset prefixes (always allowed) ────────────────────
    private static final List<String> PUBLIC_PREFIXES = Arrays.asList(
            "/assets/",
            "/favicon"
    );

    // ── Admin-only URLs ───────────────────────────────────────────
    private static final List<String> ADMIN_ONLY_URLS = Arrays.asList(
            "/staff",
            "/reports",
            "/api/reports"
    );

    // ── Dentist-restricted URLs (Admin + Dentist only) ────────────
    private static final List<String> DENTIST_URLS = Arrays.asList(
            "/appointments/update-treatment"
    );

    // ──────────────────────────────────────────────────────────────

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[AuthFilter] ✅ Security filter initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse res  = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String requestURI  = req.getRequestURI();

        // Strip context path to get relative URI
        String path = requestURI.substring(contextPath.length());

        // ── Step 1: Allow static assets always ────────────────────
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // ── Step 2: Allow public URLs without session ──────────────
        if (isPublicUrl(path)) {
            chain.doFilter(request, response);
            return;
        }

        // ── Step 3: Check session exists ──────────────────────────
        HttpSession session = req.getSession(false);
        User loggedInUser   = (session != null)
                              ? (User) session.getAttribute("loggedInUser")
                              : null;

        boolean isApiRequest = path.startsWith("/api/");

        // Not logged in → redirect to login (or 401 JSON for API clients)
        if (loggedInUser == null) {
            if (isApiRequest) {
                sendJsonError(res, HttpServletResponse.SC_UNAUTHORIZED,
                        "Authentication required. Please log in first.");
                return;
            }
            res.sendRedirect(contextPath + "/login");
            return;
        }

        // ── Step 4: Role-based access control ─────────────────────
        String userRole = loggedInUser.getRole();

        // Admin-only pages
        if (isAdminOnlyUrl(path) && !"ADMIN".equalsIgnoreCase(userRole)) {
            if (isApiRequest) {
                sendJsonError(res, HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                return;
            }
            res.sendRedirect(contextPath + "/dashboard?error=access_denied");
            return;
        }

        // Dentist/Admin-only pages (clinical updates)
        if (isDentistUrl(path) &&
            !"ADMIN".equalsIgnoreCase(userRole) &&
            !"DENTIST".equalsIgnoreCase(userRole)) {
            if (isApiRequest) {
                sendJsonError(res, HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                return;
            }
            res.sendRedirect(contextPath + "/dashboard?error=access_denied");
            return;
        }

        // ── Step 5: All checks passed → allow request ─────────────
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("[AuthFilter] Filter destroyed.");
    }

    // ── Helper Methods ─────────────────────────────────────────────

    /**
     * Check if URL is publicly accessible (no login needed).
     */
    private boolean isPublicUrl(String path) {
        for (String pub : PUBLIC_URLS) {
            if (path.equals(pub) || path.startsWith(pub + "?")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if URL is admin-only.
     */
    private boolean isAdminOnlyUrl(String path) {
        for (String admin : ADMIN_ONLY_URLS) {
            if (path.startsWith(admin)) return true;
        }
        return false;
    }

    /**
     * Check if URL requires dentist or admin role.
     */
    private boolean isDentistUrl(String path) {
        for (String dentist : DENTIST_URLS) {
            if (path.startsWith(dentist)) return true;
        }
        return false;
    }

    /**
     * Write a JSON error envelope for API requests that the filter rejects,
     * instead of the HTML redirect used for the browser-facing pages.
     */
    private void sendJsonError(HttpServletResponse res, int status, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}