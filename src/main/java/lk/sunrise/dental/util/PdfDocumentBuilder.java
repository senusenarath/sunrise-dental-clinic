package lk.sunrise.dental.util;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.awt.Color;

/**
 * ================================================================
 * PdfDocumentBuilder.java
 * Branded, line-cursor PDF writer built on Apache PDFBox
 *
 * Used to generate the invoice and analytics report downloads with
 * an actual visual design (a coloured brand band, section accents,
 * bordered tables, a highlighted total box) instead of plain
 * unstyled text - callers just write sections/rows/tables in order
 * without doing their own layout math.
 *
 * All text is sanitized to the WinAnsi-safe range before being
 * drawn: the standard PDF fonts (Helvetica) cannot render emoji or
 * other characters outside Latin-1, and this app's UI text is
 * emoji-heavy, so writing it unsanitized would throw at runtime.
 *
 * Colour state is explicitly reset before every draw call rather
 * than assumed, since PDF fill colour is shared by both shapes and
 * text - leaving it set from the last coloured box would otherwise
 * silently tint the next line of text.
 * Package : lk.sunrise.dental.util
 * ================================================================
 */
public class PdfDocumentBuilder implements AutoCloseable {

    private static final float MARGIN        = 50;
    private static final float PAGE_WIDTH     = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT    = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH  = PAGE_WIDTH - (2 * MARGIN);
    private static final float LINE_HEIGHT    = 18;

    // Brand palette (matches the web app's primary blue accent)
    private static final Color BRAND_BLUE  = new Color(14, 165, 233);
    private static final Color DARK_TEXT   = new Color(26, 26, 46);
    private static final Color MUTED_TEXT  = new Color(100, 100, 110);
    private static final Color LIGHT_FILL  = new Color(235, 245, 251);
    private static final Color BORDER_GRAY = new Color(215, 219, 224);
    private static final Color WHITE       = Color.WHITE;

    private final PDDocument document = new PDDocument();
    private PDPageContentStream stream;
    private float cursorY;
    private boolean closed = false;

    public PdfDocumentBuilder() throws IOException {
        newPage();
    }

    /** Call once, before anything else - draws the coloured brand band at the top. */
    public void writeBrandHeader(String title, String subtitle) throws IOException {
        float bandHeight = 70;

        setFill(BRAND_BLUE);
        stream.addRect(0, PAGE_HEIGHT - bandHeight, PAGE_WIDTH, bandHeight);
        stream.fill();

        setFill(WHITE);
        drawText(title, PDType1Font.HELVETICA_BOLD, 18, MARGIN, PAGE_HEIGHT - 34);
        if (subtitle != null && !subtitle.isEmpty()) {
            drawText(subtitle, PDType1Font.HELVETICA, 10, MARGIN, PAGE_HEIGHT - 50);
        }

        cursorY = PAGE_HEIGHT - bandHeight - 28;
    }

    /** A bold document sub-title, e.g. "INVOICE BILL-2026-0001". */
    public void writeDocTitle(String text) throws IOException {
        ensureRoom();
        setFill(DARK_TEXT);
        drawText(text, PDType1Font.HELVETICA_BOLD, 15, MARGIN, cursorY);
        cursorY -= LINE_HEIGHT + 6;
    }

    /** A section heading with a small blue accent bar to its left. */
    public void writeSectionTitle(String text) throws IOException {
        ensureRoom();
        cursorY -= 4;
        setFill(BRAND_BLUE);
        stream.addRect(MARGIN, cursorY - 2, 3, 13);
        stream.fill();

        setFill(DARK_TEXT);
        drawText(text, PDType1Font.HELVETICA_BOLD, 12, MARGIN + 10, cursorY);
        cursorY -= LINE_HEIGHT;
    }

    public void writeLine(String text) throws IOException {
        ensureRoom();
        setFill(DARK_TEXT);
        drawText(text, PDType1Font.HELVETICA, 10, MARGIN, cursorY);
        cursorY -= LINE_HEIGHT;
    }

    /** A muted-label / bold-value row, e.g. an invoice detail line. */
    public void writeRow(String label, String value) throws IOException {
        ensureRoom();
        setFill(MUTED_TEXT);
        drawText(label, PDType1Font.HELVETICA, 10, MARGIN, cursorY);
        setFill(DARK_TEXT);
        drawText(value, PDType1Font.HELVETICA_BOLD, 10, MARGIN + 190, cursorY);
        cursorY -= LINE_HEIGHT;
    }

    public void blankLine() {
        cursorY -= LINE_HEIGHT / 2f;
    }

    public void horizontalRule() throws IOException {
        ensureRoom();
        setStroke(BORDER_GRAY);
        stream.setLineWidth(0.75f);
        stream.moveTo(MARGIN, cursorY + 6);
        stream.lineTo(PAGE_WIDTH - MARGIN, cursorY + 6);
        stream.stroke();
        cursorY -= 10;
    }

    /**
     * A bordered table with a shaded header row and light row separators.
     * colWidths must sum to (roughly) the usable content width.
     */
    public void writeTable(String[] headers, List<String[]> rows, float[] colWidths) throws IOException {
        float rowHeight = 20;
        float tableWidth = 0;
        for (float w : colWidths) tableWidth += w;

        ensureRoom();
        setFill(LIGHT_FILL);
        stream.addRect(MARGIN, cursorY - rowHeight + 5, tableWidth, rowHeight);
        stream.fill();

        float x = MARGIN;
        setFill(DARK_TEXT);
        for (int i = 0; i < headers.length; i++) {
            drawText(headers[i], PDType1Font.HELVETICA_BOLD, 9, x + 5, cursorY);
            x += colWidths[i];
        }
        cursorY -= rowHeight;

        for (String[] row : rows) {
            ensureRoom();
            x = MARGIN;
            setFill(DARK_TEXT);
            for (int i = 0; i < row.length && i < colWidths.length; i++) {
                drawText(row[i], PDType1Font.HELVETICA, 9, x + 5, cursorY);
                x += colWidths[i];
            }
            cursorY -= rowHeight;

            setStroke(BORDER_GRAY);
            stream.setLineWidth(0.4f);
            stream.moveTo(MARGIN, cursorY + rowHeight - 4);
            stream.lineTo(MARGIN + tableWidth, cursorY + rowHeight - 4);
            stream.stroke();
        }

        cursorY -= 6;
    }

    /** A boxed, high-visibility label/value pair - used for the invoice total. */
    public void writeHighlightBox(String label, String value) throws IOException {
        float boxHeight = 34;
        ensureRoom();
        cursorY -= 4;

        setFill(LIGHT_FILL);
        stream.addRect(MARGIN, cursorY - boxHeight + 16, CONTENT_WIDTH, boxHeight);
        stream.fill();

        setStroke(BRAND_BLUE);
        stream.setLineWidth(1.1f);
        stream.addRect(MARGIN, cursorY - boxHeight + 16, CONTENT_WIDTH, boxHeight);
        stream.stroke();

        setFill(DARK_TEXT);
        drawText(label, PDType1Font.HELVETICA_BOLD, 12, MARGIN + 14, cursorY);
        drawText(value, PDType1Font.HELVETICA_BOLD, 14, MARGIN + 230, cursorY);

        cursorY -= boxHeight;
    }

    // ── Low-level drawing helpers ────────────────────────────────

    private void drawText(String text, PDType1Font font, int size, float x, float y) throws IOException {
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(x, y);
        stream.showText(sanitize(text));
        stream.endText();
    }

    private void setFill(Color color) throws IOException {
        stream.setNonStrokingColor(color);
    }

    private void setStroke(Color color) throws IOException {
        stream.setStrokingColor(color);
    }

    private void ensureRoom() throws IOException {
        if (cursorY < MARGIN + LINE_HEIGHT) {
            newPage();
        }
    }

    private void newPage() throws IOException {
        if (stream != null) {
            stream.close();
        }
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        stream = new PDPageContentStream(document, page);
        cursorY = PAGE_HEIGHT - MARGIN;
    }

    /** Strip anything the standard PDF fonts can't render (emoji, exotic Unicode). */
    private static String sanitize(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            sb.append(c <= 0xFF ? c : '?');
        }
        return sb.toString();
    }

    /** Finalize the document and stream it to the response as a download. */
    public void writeToResponse(HttpServletResponse res, String filename) throws IOException {
        if (stream != null) {
            stream.close();
            stream = null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        document.save(bos);
        document.close();
        closed = true;

        res.setContentType("application/pdf");
        res.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        res.setContentLength(bos.size());
        res.getOutputStream().write(bos.toByteArray());
        res.getOutputStream().flush();
    }

    /**
     * Safe to call after writeToResponse() already ran (e.g. via
     * try-with-resources) - only closes the underlying document once.
     */
    @Override
    public void close() throws IOException {
        if (closed) return;
        closed = true;
        if (stream != null) {
            stream.close();
            stream = null;
        }
        document.close();
    }
}
