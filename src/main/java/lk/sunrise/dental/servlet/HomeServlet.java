package lk.sunrise.dental.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 * ================================================================
 * HomeServlet.java
 * Root landing page router
 *
 * GET  /home → Redirect to dashboard if logged in, else login
 *
 * Package : lk.sunrise.dental.servlet
 * ================================================================
 */
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session != null && session.getAttribute("loggedInUser") != null) {
            // Logged in → go to dashboard
            res.sendRedirect(req.getContextPath() + "/dashboard");
        } else {
            // Not logged in → go to login
            res.sendRedirect(req.getContextPath() + "/login");
        }
    }
}