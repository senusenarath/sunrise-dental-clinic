package lk.sunrise.dental.api;

import jakarta.servlet.http.*;
import lk.sunrise.dental.model.Patient;
import lk.sunrise.dental.model.User;
import lk.sunrise.dental.service.PatientService;
import lk.sunrise.dental.service.ServiceResult;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * ================================================================
 * PatientApiServlet.java
 * REST/JSON API for patient records
 *
 * GET  /api/patients             - list active patients
 * GET  /api/patients?search=X    - search patients (name, contact, email, code)
 * GET  /api/patients/{id}        - get a single patient
 * POST /api/patients             - register a new patient (JSON body)
 *
 * Authenticated via the same session cookie as the web UI (see
 * AuthFilter). Business rules (validation, duplicate contact checks,
 * role restrictions) are enforced by the shared PatientService, so
 * this API and the JSP pages can never disagree on what's allowed.
 * Package : lk.sunrise.dental.api
 * ================================================================
 */
public class PatientApiServlet extends HttpServlet {

    private final PatientService patientService = new PatientService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            String search = req.getParameter("search");
            List<Patient> patients = (search != null && !search.isBlank())
                    ? patientService.searchPatients(search.trim())
                    : patientService.getAllPatients();
            ApiUtil.writeSuccess(res, ApiMapper.patientsToJsonArray(patients));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            Patient patient = patientService.getPatientById(id);
            if (patient == null) {
                ApiUtil.writeError(res, 404, "Patient not found.");
                return;
            }
            ApiUtil.writeSuccess(res, ApiMapper.toJson(patient));
        } catch (NumberFormatException e) {
            ApiUtil.writeError(res, 400, "Invalid patient id.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session   = req.getSession(false);
        User loggedInUser     = (User) session.getAttribute("loggedInUser");

        if (loggedInUser.isDentist()) {
            ApiUtil.writeError(res, 403, "Dentists are not permitted to register patients.");
            return;
        }

        JSONObject body = ApiUtil.readJsonBody(req);

        ServiceResult result = patientService.registerPatient(
                body.optString("fullName", null),
                body.optString("dateOfBirth", null),
                body.optString("gender", null),
                body.optString("address", null),
                body.optString("contact", null),
                body.optString("email", null),
                body.optString("bloodType", null),
                body.optString("allergies", null),
                body.optString("medicalNotes", null),
                loggedInUser.getId()
        );

        if (result.isSuccess()) {
            Patient created = patientService.getPatientById(result.getGeneratedId());
            ApiUtil.writeCreated(res, ApiMapper.toJson(created));
        } else {
            ApiUtil.writeError(res, 400, result.getMessage());
        }
    }
}
