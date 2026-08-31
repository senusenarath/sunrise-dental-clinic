package lk.sunrise.dental.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lk.sunrise.dental.model.Bill;
import lk.sunrise.dental.model.User;
import lk.sunrise.dental.service.AppointmentService;
import lk.sunrise.dental.service.BillService;
import lk.sunrise.dental.service.ServiceResult;
import lk.sunrise.dental.util.CsvUtil;
import lk.sunrise.dental.util.DateUtil;
import lk.sunrise.dental.util.PdfDocumentBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * BillServlet.java
 * Manages invoice generation, settlement and receipt printing
 *
 * GET  /bills               → List all bills
 * GET  /bills?status=X      → Filter bills by status
 * GET  /bills/create?aptId=X → Show bill creation form
 * POST /bills/create        → Generate new invoice
 * GET  /bills/view?id=X     → View/print invoice
 * POST /bills/settle        → Mark bill as paid
 * POST /bills/cancel        → Cancel pending bill
 *
 * Package : lk.sunrise.dental.servlet
 * ================================================================
 */
public class BillServlet extends HttpServlet {

    private final BillService        billService        = new BillService();
    private final AppointmentService appointmentService = new AppointmentService();

    // ── View Paths ─────────────────────────────────────────────────
    private static final String LIST_VIEW    = "/WEB-INF/views/billing/list.jsp";
    private static final String INVOICE_VIEW = "/WEB-INF/views/billing/invoice.jsp";

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
                case "/create" -> handleCreateForm(req, res);
                case "/view"   -> handleView(req, res);
                default -> res.sendRedirect(req.getContextPath() + "/bills");
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
            res.sendRedirect(req.getContextPath() + "/bills");
            return;
        }

        switch (pathInfo) {
            case "/create" -> handleCreateSubmit(req, res);
            case "/settle" -> handleSettle(req, res);
            case "/cancel" -> handleCancel(req, res);
            default -> res.sendRedirect(req.getContextPath() + "/bills");
        }
    }

    // ──────────────────────────────────────────────────────────────
    // HANDLERS
    // ──────────────────────────────────────────────────────────────

    /**
     * GET /bills - List all bills with optional filter
     */
    private void handleList(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String status  = req.getParameter("status");
        String success = req.getParameter("success");
        String error   = req.getParameter("error");

        List<Bill> bills = billService.getBillsByStatus(status);

        if ("csv".equalsIgnoreCase(req.getParameter("export"))) {
            exportBillsCsv(res, bills);
            return;
        }

        if (success != null) req.setAttribute("successMsg", success);
        if (error   != null) req.setAttribute("errorMsg",   error);

        req.setAttribute("bills",          bills);
        req.setAttribute("statusFilter",   status);
        req.setAttribute("totalRevenue",   billService.getTotalRevenue());
        req.setAttribute("monthlyRevenue", billService.getMonthlyRevenue());
        req.setAttribute("pendingAmount",  billService.getPendingAmount());
        req.setAttribute("pendingCount",   billService.getPendingCount());
        req.setAttribute("pageTitle",      "Billing & Invoices");

        req.getRequestDispatcher(LIST_VIEW).forward(req, res);
    }

    /**
     * GET /bills?export=csv - Download the current bill list as CSV
     */
    private void exportBillsCsv(HttpServletResponse res, List<Bill> bills) throws IOException {
        List<String[]> rows = new ArrayList<>();
        for (Bill bill : bills) {
            rows.add(new String[] {
                    bill.getBillCode(),
                    bill.getPatientName(),
                    bill.getTreatmentName(),
                    String.valueOf(bill.getTreatmentFee()),
                    String.valueOf(bill.getConsultFee()),
                    String.valueOf(bill.getDiscount()),
                    String.valueOf(bill.getTotalAmount()),
                    bill.getPaymentMethod(),
                    bill.getStatus()
            });
        }

        CsvUtil.writeCsv(res,
                "sunrise-bills-" + DateUtil.getTodayCode() + ".csv",
                new String[] {"Bill Code", "Patient", "Treatment", "Treatment Fee", "Consult Fee",
                        "Discount", "Total Amount", "Payment Method", "Status"},
                rows);
    }

    /**
     * GET /bills/create?aptId=X - Show bill creation form
     */
    private void handleCreateForm(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Dentists cannot create bills
        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        String aptIdStr = req.getParameter("aptId");

        // If appointment ID provided → pre-load appointment details
        if (aptIdStr != null && !aptIdStr.isEmpty()) {
            try {
                int aptId = Integer.parseInt(aptIdStr);

                // Check if bill already exists
                Bill existingBill = billService.getBillByAppointmentId(aptId);
                if (existingBill != null) {
                    res.sendRedirect(req.getContextPath() +
                            "/bills/view?id=" + existingBill.getId());
                    return;
                }

                req.setAttribute("preselectedApt",
                        appointmentService.getAppointmentById(aptId));
            } catch (NumberFormatException ignored) {}
        }

        // Load completed appointments without bills for dropdown
        req.setAttribute("unbilledAppointments",
                appointmentService.getAppointmentsByStatus("Completed"));
        req.setAttribute("pageTitle", "Generate Invoice");

        req.getRequestDispatcher(INVOICE_VIEW).forward(req, res);
    }

    /**
     * POST /bills/create - Generate new invoice
     */
    private void handleCreateSubmit(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User loggedInUser   = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        String appointmentId = req.getParameter("appointmentId");
        String paymentMethod = req.getParameter("paymentMethod");
        String discount      = req.getParameter("discount");

        ServiceResult result = billService.createBill(
                appointmentId, paymentMethod, discount, loggedInUser.getId()
        );

        if (result.isSuccess()) {
            // Invoice email is sent by BillService via the Observer-pattern
            // NotificationListener, so both this JSP flow and the REST API
            // get consistent notification behaviour.
            res.sendRedirect(req.getContextPath() +
                    "/bills/view?id=" + result.getGeneratedId() +
                    "&success=" + java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
        } else {
            req.setAttribute("errorMsg", result.getMessage());
            req.setAttribute("unbilledAppointments",
                    appointmentService.getAppointmentsByStatus("Completed"));
            req.setAttribute("pageTitle", "Generate Invoice");
            req.getRequestDispatcher(INVOICE_VIEW).forward(req, res);
        }
    }

    /**
     * GET /bills/view?id=X            - View invoice
     * GET /bills/view?id=X&format=pdf - Download invoice as PDF
     */
    private void handleView(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String idStr = req.getParameter("id");
        try {
            int  id   = Integer.parseInt(idStr);
            Bill bill = billService.getBillById(id);

            if (bill == null) {
                res.sendRedirect(req.getContextPath() + "/bills");
                return;
            }

            if ("pdf".equalsIgnoreCase(req.getParameter("format"))) {
                exportInvoicePdf(res, bill);
                return;
            }

            String success = req.getParameter("success");
            if (success != null) req.setAttribute("successMsg", success);

            req.setAttribute("bill",      bill);
            req.setAttribute("pageTitle", "Invoice - " + bill.getBillCode());
            req.getRequestDispatcher(INVOICE_VIEW).forward(req, res);

        } catch (Exception e) {
            res.sendRedirect(req.getContextPath() + "/bills");
        }
    }

    /**
     * Generate a downloadable PDF version of an invoice.
     */
    private void exportInvoicePdf(HttpServletResponse res, Bill bill) throws IOException {
        try (PdfDocumentBuilder pdf = new PdfDocumentBuilder()) {
            pdf.writeBrandHeader("Sunrise Dental Clinic", "Colombo, Sri Lanka  |  Tel: 011-234-5678");

            pdf.writeDocTitle("INVOICE " + bill.getBillCode());
            pdf.writeRow("Status:", bill.getStatus());
            pdf.blankLine();

            pdf.writeSectionTitle("Bill To");
            pdf.writeRow("Patient:", bill.getPatientName());
            pdf.writeRow("Patient Code:", bill.getPatientCode());
            pdf.writeRow("Contact:", bill.getPatientContact());
            if (bill.getPatientAddress() != null && !bill.getPatientAddress().isEmpty()) {
                pdf.writeRow("Address:", bill.getPatientAddress());
            }
            pdf.blankLine();

            pdf.writeSectionTitle("Appointment");
            pdf.writeRow("Reference:", bill.getAptCode());
            pdf.writeRow("Date:", String.valueOf(bill.getAptDate()));
            pdf.writeRow("Time:", String.valueOf(bill.getAptTime()));
            pdf.writeRow("Dentist:", bill.getDentistName());
            pdf.writeRow("Treatment:", bill.getTreatmentName());
            pdf.blankLine();

            pdf.writeSectionTitle("Charges");
            pdf.writeTable(
                    new String[] {"Description", "Amount (LKR)"},
                    java.util.List.of(
                            new String[] {"Treatment Fee", String.format("%.2f", bill.getTreatmentFee())},
                            new String[] {"Consultation Fee", String.format("%.2f", bill.getConsultFee())},
                            new String[] {"Discount", bill.getDiscount() > 0
                                    ? "-" + String.format("%.2f", bill.getDiscount())
                                    : "0.00"}
                    ),
                    new float[] {300, 195}
            );
            pdf.blankLine();
            pdf.writeHighlightBox("TOTAL AMOUNT DUE (LKR)", String.format("%.2f", bill.getTotalAmount()));
            pdf.blankLine();

            pdf.writeSectionTitle("Payment");
            pdf.writeRow("Payment Method:", bill.getPaymentMethod());
            if (bill.isPaid()) {
                pdf.writeRow("Settled By:", bill.getSettledByName());
                pdf.writeRow("Settled At:", String.valueOf(bill.getSettledAt()));
            }
            pdf.blankLine();
            pdf.writeLine("Thank you for choosing Sunrise Dental Clinic.");

            pdf.writeToResponse(res, "invoice-" + bill.getBillCode() + ".pdf");
        }
    }

    /**
     * POST /bills/settle - Mark bill as paid
     */
    private void handleSettle(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User loggedInUser   = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            res.sendRedirect(req.getContextPath() + "/dashboard?error=access_denied");
            return;
        }

        String billIdStr     = req.getParameter("billId");
        String paymentMethod = req.getParameter("paymentMethod");

        try {
            int billId = Integer.parseInt(billIdStr);

            ServiceResult result = billService.settleBill(
                    billId, paymentMethod, loggedInUser.getId()
            );

            // Payment confirmation email is sent by BillService via the
            // Observer-pattern NotificationListener.
            if (result.isSuccess()) {
                res.sendRedirect(req.getContextPath() +
                        "/bills/view?id=" + billId +
                        "&success=" + java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            } else {
                res.sendRedirect(req.getContextPath() +
                        "/bills/view?id=" + billId +
                        "&error=" + java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            }

        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/bills");
        }
    }

    /**
     * POST /bills/cancel - Cancel pending bill
     */
    private void handleCancel(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        HttpSession session = req.getSession(false);
        User loggedInUser   = (User) session.getAttribute("loggedInUser");

        String billIdStr = req.getParameter("billId");
        try {
            int billId = Integer.parseInt(billIdStr);

            ServiceResult result = billService.cancelBill(
                    billId, loggedInUser.getRole()
            );

            if (result.isSuccess()) {
                res.sendRedirect(req.getContextPath() +
                        "/bills?success=" +
                        java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            } else {
                res.sendRedirect(req.getContextPath() +
                        "/bills?error=" +
                        java.net.URLEncoder.encode(result.getMessage(), "UTF-8"));
            }

        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/bills");
        }
    }
}