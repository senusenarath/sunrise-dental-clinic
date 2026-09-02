package lk.sunrise.dental.api;

import jakarta.servlet.http.*;
import lk.sunrise.dental.model.Appointment;
import lk.sunrise.dental.model.User;
import lk.sunrise.dental.service.AppointmentService;
import lk.sunrise.dental.service.ServiceResult;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * ================================================================
 * AppointmentApiServlet.java
 * REST/JSON API for appointment scheduling
 *
 * GET  /api/appointments             - list all appointments
 * GET  /api/appointments?status=X    - filter by status
 * GET  /api/appointments?search=X    - search by patient name or appointment code
 * GET  /api/appointments/{id}        - get a single appointment
 * POST /api/appointments             - book a new appointment (JSON body)
 * POST /api/appointments/{id}/cancel - cancel an appointment
 * PUT  /api/appointments/{id}/status - update clinical status (JSON body)
 *
 * Package : lk.sunrise.dental.api
 * ================================================================
 */
public class AppointmentApiServlet extends HttpServlet {

    private final AppointmentService appointmentService = new AppointmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            String search = req.getParameter("search");
            String status = req.getParameter("status");

            List<Appointment> appointments;
            if (search != null && !search.isBlank()) {
                appointments = appointmentService.searchAppointments(search.trim());
            } else if (status != null && !status.isBlank()) {
                appointments = appointmentService.getAppointmentsByStatus(status.trim());
            } else {
                appointments = appointmentService.getAllAppointments();
            }
            ApiUtil.writeSuccess(res, ApiMapper.appointmentsToJsonArray(appointments));
            return;
        }

        String[] parts = splitPath(pathInfo);
        try {
            int id = Integer.parseInt(parts[0]);
            Appointment apt = appointmentService.getAppointmentById(id);
            if (apt == null) {
                ApiUtil.writeError(res, 404, "Appointment not found.");
                return;
            }
            ApiUtil.writeSuccess(res, ApiMapper.toJson(apt));
        } catch (NumberFormatException e) {
            ApiUtil.writeError(res, 400, "Invalid appointment id.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");
        String pathInfo       = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            if (loggedInUser.isDentist()) {
                ApiUtil.writeError(res, 403, "Dentists are not permitted to book appointments.");
                return;
            }

            JSONObject body = ApiUtil.readJsonBody(req);
            ServiceResult result = appointmentService.bookAppointment(
                    body.optString("patientId", null),
                    body.optString("dentistId", null),
                    body.optString("treatmentId", null),
                    body.optString("aptDate", null),
                    body.optString("aptTime", null),
                    body.optString("notes", null),
                    loggedInUser.getId()
            );

            if (result.isSuccess()) {
                Appointment created = appointmentService.getAppointmentById(result.getGeneratedId());
                ApiUtil.writeCreated(res, ApiMapper.toJson(created));
            } else {
                ApiUtil.writeError(res, 400, result.getMessage());
            }
            return;
        }

        String[] parts = splitPath(pathInfo);
        if (parts.length == 2 && "cancel".equals(parts[1])) {
            try {
                int id = Integer.parseInt(parts[0]);
                ServiceResult result = appointmentService.cancelAppointment(id, loggedInUser.getRole());
                if (result.isSuccess()) {
                    ApiUtil.writeSuccess(res, ApiMapper.toJson(appointmentService.getAppointmentById(id)));
                } else {
                    ApiUtil.writeError(res, 400, result.getMessage());
                }
            } catch (NumberFormatException e) {
                ApiUtil.writeError(res, 400, "Invalid appointment id.");
            }
            return;
        }

        ApiUtil.writeError(res, 404, "Unknown endpoint.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");
        String pathInfo       = req.getPathInfo();

        String[] parts = pathInfo == null ? new String[0] : splitPath(pathInfo);
        if (parts.length == 2 && "status".equals(parts[1])) {
            try {
                int id = Integer.parseInt(parts[0]);
                JSONObject body = ApiUtil.readJsonBody(req);
                ServiceResult result = appointmentService.updateTreatmentStatus(
                        id,
                        body.optString("status", null),
                        body.optString("notes", null),
                        loggedInUser.getRole()
                );
                if (result.isSuccess()) {
                    ApiUtil.writeSuccess(res, ApiMapper.toJson(appointmentService.getAppointmentById(id)));
                } else {
                    ApiUtil.writeError(res, 400, result.getMessage());
                }
            } catch (NumberFormatException e) {
                ApiUtil.writeError(res, 400, "Invalid appointment id.");
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
