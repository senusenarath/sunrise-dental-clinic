package lk.sunrise.dental.api;

import jakarta.servlet.http.*;
import lk.sunrise.dental.model.Bill;
import lk.sunrise.dental.model.User;
import lk.sunrise.dental.service.BillService;
import lk.sunrise.dental.service.ServiceResult;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * ================================================================
 * BillApiServlet.java
 * REST/JSON API for billing and invoices
 *
 * GET  /api/bills             - list all bills
 * GET  /api/bills?status=X    - filter by status
 * GET  /api/bills/{id}        - get a single bill
 * POST /api/bills             - generate a new invoice (JSON body)
 * POST /api/bills/{id}/settle - mark a bill as paid (JSON body)
 *
 * Package : lk.sunrise.dental.api
 * ================================================================
 */
public class BillApiServlet extends HttpServlet {

    private final BillService billService = new BillService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            List<Bill> bills = billService.getBillsByStatus(req.getParameter("status"));
            ApiUtil.writeSuccess(res, ApiMapper.toJsonArray(bills));
            return;
        }

        String[] parts = splitPath(pathInfo);
        try {
            int id = Integer.parseInt(parts[0]);
            Bill bill = billService.getBillById(id);
            if (bill == null) {
                ApiUtil.writeError(res, 404, "Bill not found.");
                return;
            }
            ApiUtil.writeSuccess(res, ApiMapper.toJson(bill));
        } catch (NumberFormatException e) {
            ApiUtil.writeError(res, 400, "Invalid bill id.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            ApiUtil.writeError(res, 403, "Dentists are not permitted to manage billing.");
            return;
        }

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            JSONObject body = ApiUtil.readJsonBody(req);
            ServiceResult result = billService.createBill(
                    body.optString("appointmentId", null),
                    body.optString("paymentMethod", null),
                    body.optString("discount", "0"),
                    loggedInUser.getId()
            );

            if (result.isSuccess()) {
                Bill created = billService.getBillById(result.getGeneratedId());
                ApiUtil.writeCreated(res, ApiMapper.toJson(created));
            } else {
                ApiUtil.writeError(res, 400, result.getMessage());
            }
            return;
        }

        String[] parts = splitPath(pathInfo);
        if (parts.length == 2 && "settle".equals(parts[1])) {
            try {
                int id = Integer.parseInt(parts[0]);
                JSONObject body = ApiUtil.readJsonBody(req);
                ServiceResult result = billService.settleBill(
                        id, body.optString("paymentMethod", null), loggedInUser.getId()
                );
                if (result.isSuccess()) {
                    ApiUtil.writeSuccess(res, ApiMapper.toJson(billService.getBillById(id)));
                } else {
                    ApiUtil.writeError(res, 400, result.getMessage());
                }
            } catch (NumberFormatException e) {
                ApiUtil.writeError(res, 400, "Invalid bill id.");
            }
            return;
        }

        ApiUtil.writeError(res, 404, "Unknown endpoint.");
    }

    private String[] splitPath(String pathInfo) {
        String trimmed = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        return trimmed.split("/");
    }
}
