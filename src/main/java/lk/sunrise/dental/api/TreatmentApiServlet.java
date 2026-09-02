package lk.sunrise.dental.api;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sunrise.dental.dao.TreatmentDAO;

import java.io.IOException;

/**
 * ================================================================
 * TreatmentApiServlet.java
 * Read-only REST/JSON API for treatment types and consultation costs
 *
 * GET /api/treatments - list all active treatments
 *
 * Package : lk.sunrise.dental.api
 * ================================================================
 */
public class TreatmentApiServlet extends HttpServlet {

    private final TreatmentDAO treatmentDAO = new TreatmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        ApiUtil.writeSuccess(res, ApiMapper.toJsonArrayFromMaps(treatmentDAO.getAllTreatments()));
    }
}
