package lk.sunrise.dental.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 * ================================================================
 * HelpServlet.java
 * Serves interactive SOP training manual
 *
 * GET /help → Show help and training manual page
 *
 * Package : lk.sunrise.dental.servlet
 * ================================================================
 */
public class HelpServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/help/manual.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setAttribute("pageTitle", "Help & Staff Training Manual");
        req.getRequestDispatcher(VIEW).forward(req, res);
    }
}