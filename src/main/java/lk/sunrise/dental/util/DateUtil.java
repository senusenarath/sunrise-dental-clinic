package lk.sunrise.dental.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * ================================================================
 * DateUtil.java
 * Date, Time & Currency Formatting Utilities
 *
 * Centralized formatting for consistent display across all views.
 * Package : lk.sunrise.dental.util
 * ================================================================
 */
public class DateUtil {

    // ── Format Patterns ────────────────────────────────────────────
    private static final String FMT_DATE_DISPLAY  = "dd MMMM yyyy";
    private static final String FMT_DATE_SHORT    = "dd/MM/yyyy";
    private static final String FMT_DATE_INPUT    = "yyyy-MM-dd";
    private static final String FMT_TIME_DISPLAY  = "hh:mm a";
    private static final String FMT_TIME_INPUT    = "HH:mm";
    private static final String FMT_DATETIME_FULL = "dd MMM yyyy, hh:mm a";
    private static final String FMT_APT_CODE      = "yyyyMMdd";
    private static final String FMT_YEAR          = "yyyy";
    private static final String FMT_YEAR_MONTH    = "yyyyMM";

    // Prevent instantiation
    private DateUtil() {}

    // ──────────────────────────────────────────────────────────────
    // DATE FORMATTING
    // ──────────────────────────────────────────────────────────────

    /**
     * Format LocalDate → "15 January 2025"
     */
    public static String formatDisplay(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DateTimeFormatter.ofPattern(FMT_DATE_DISPLAY));
    }

    /**
     * Format LocalDate → "15/01/2025"
     */
    public static String formatShort(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DateTimeFormatter.ofPattern(FMT_DATE_SHORT));
    }

    /**
     * Format LocalDate → "2025-01-15" (for HTML date input)
     */
    public static String formatInput(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern(FMT_DATE_INPUT));
    }

    // ──────────────────────────────────────────────────────────────
    // TIME FORMATTING
    // ──────────────────────────────────────────────────────────────

    /**
     * Format LocalTime → "09:00 AM"
     */
    public static String formatTimeDisplay(LocalTime time) {
        if (time == null) return "N/A";
        return time.format(DateTimeFormatter.ofPattern(FMT_TIME_DISPLAY));
    }

    /**
     * Format LocalTime → "09:00" (for HTML time input)
     */
    public static String formatTimeInput(LocalTime time) {
        if (time == null) return "";
        return time.format(DateTimeFormatter.ofPattern(FMT_TIME_INPUT));
    }

    // ──────────────────────────────────────────────────────────────
    // DATETIME FORMATTING
    // ──────────────────────────────────────────────────────────────

    /**
     * Format LocalDateTime → "15 Jan 2025, 09:00 AM"
     */
    public static String formatDateTimeFull(LocalDateTime dt) {
        if (dt == null) return "N/A";
        return dt.format(DateTimeFormatter.ofPattern(FMT_DATETIME_FULL));
    }

    // ──────────────────────────────────────────────────────────────
    // CODE GENERATION HELPERS
    // ──────────────────────────────────────────────────────────────

    /**
     * Get today's date for appointment code.
     * Returns: "20250115"
     */
    public static String getTodayCode() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(FMT_APT_CODE));
    }

    /**
     * Get current year-month for bill code.
     * Returns: "202501"
     */
    public static String getYearMonthCode() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(FMT_YEAR_MONTH));
    }

    /**
     * Get current year as string.
     * Returns: "2025"
     */
    public static String getCurrentYear() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(FMT_YEAR));
    }

    // ──────────────────────────────────────────────────────────────
    // UTILITY METHODS
    // ──────────────────────────────────────────────────────────────

    /**
     * Calculate age from date of birth.
     */
    public static int calculateAge(LocalDate dob) {
        if (dob == null) return 0;
        return (int) ChronoUnit.YEARS.between(dob, LocalDate.now());
    }

    /**
     * Check if date is today.
     */
    public static boolean isToday(LocalDate date) {
        return date != null && LocalDate.now().equals(date);
    }

    /**
     * Check if date is in the past.
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Check if date is in the future.
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Get today's date.
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Get current time.
     */
    public static LocalTime nowTime() {
        return LocalTime.now();
    }

    // ──────────────────────────────────────────────────────────────
    // CURRENCY FORMATTING
    // ──────────────────────────────────────────────────────────────

    /**
     * Format amount as Sri Lankan Rupees.
     * Example: 15000.00 → "LKR 15,000.00"
     *
     * @param amount decimal amount
     * @return formatted currency string
     */
    public static String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return "LKR " + formatter.format(amount);
    }

    /**
     * Format amount without currency symbol.
     * Example: 15000.00 → "15,000.00"
     */
    public static String formatAmount(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(amount);
    }
}