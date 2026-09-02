package lk.sunrise.dental.util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * ================================================================
 * CsvUtil.java
 * Shared CSV file-download helper
 *
 * Centralizes the response headers and field-escaping every CSV
 * export (patients, appointments, bills, reports) needs, so the
 * browser always downloads these as a named file instead of
 * rendering them inline.
 * Package : lk.sunrise.dental.util
 * ================================================================
 */
public final class CsvUtil {

    private CsvUtil() {}

    /**
     * Stream a CSV file to the response as a forced download.
     *
     * @param res      the servlet response
     * @param filename download filename, e.g. "sunrise-patients-20260723.csv"
     * @param headers  column header row
     * @param rows     data rows, one String[] per row
     */
    public static void writeCsv(HttpServletResponse res, String filename,
                                 String[] headers, List<String[]> rows) throws IOException {
        res.reset();
        res.setContentType("text/csv;charset=UTF-8");
        res.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");

        PrintWriter writer = res.getWriter();
        writer.println(joinEscaped(headers));
        for (String[] row : rows) {
            writer.println(joinEscaped(row));
        }
        writer.flush();
    }

    private static String joinEscaped(String[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(values[i]));
        }
        return sb.toString();
    }

    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
