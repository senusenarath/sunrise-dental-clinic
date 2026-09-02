package lk.sunrise.dental.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 * ================================================================
 * ApiUtil.java
 * Shared helpers for the JSON REST API layer
 *
 * Centralizes request-body parsing and response writing so every
 * API servlet returns a consistent {success, data|message} shape.
 * Package : lk.sunrise.dental.api
 * ================================================================
 */
public final class ApiUtil {

    private ApiUtil() {}

    /**
     * Read the request body and parse it as a JSON object.
     * Returns an empty object if the body is blank.
     */
    public static JSONObject readJsonBody(HttpServletRequest req) throws IOException {
        String body;
        try (BufferedReader reader = req.getReader()) {
            body = reader.lines().collect(Collectors.joining());
        }
        if (body == null || body.isBlank()) return new JSONObject();
        return new JSONObject(body);
    }

    /** Write a 200 OK {success:true, data:...} response. */
    public static void writeSuccess(HttpServletResponse res, Object data) throws IOException {
        writeEnvelope(res, 200, true, data, null);
    }

    /** Write a 201 Created {success:true, data:...} response. */
    public static void writeCreated(HttpServletResponse res, Object data) throws IOException {
        writeEnvelope(res, 201, true, data, null);
    }

    /** Write an error {success:false, message:...} response with the given HTTP status. */
    public static void writeError(HttpServletResponse res, int status, String message) throws IOException {
        writeEnvelope(res, status, false, null, message);
    }

    private static void writeEnvelope(HttpServletResponse res, int status,
                                       boolean success, Object data, String message) throws IOException {
        JSONObject envelope = new JSONObject();
        envelope.put("success", success);
        if (data != null)    envelope.put("data", data);
        if (message != null) envelope.put("message", message);

        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = res.getWriter();
        writer.write(envelope.toString());
        writer.flush();
    }
}
