package lk.sunrise.dental.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ================================================================
 * ValidationUtilTest.java
 * Unit tests for form input validation rules.
 * ================================================================
 */
class ValidationUtilTest {

    // ── Patient Validation ──────────────────────────────────────────

    @Test
    void validatePatient_acceptsCompleteValidInput() {
        List<String> errors = ValidationUtil.validatePatient(
                "Nimal Perera", "0771234567", "nimal@email.com", "Male");
        assertTrue(errors.isEmpty());
    }

    @Test
    void validatePatient_rejectsMissingName() {
        List<String> errors = ValidationUtil.validatePatient(
                "", "0771234567", "nimal@email.com", "Male");
        assertFalse(errors.isEmpty());
    }

    @Test
    void validatePatient_rejectsInvalidContact() {
        List<String> errors = ValidationUtil.validatePatient(
                "Nimal Perera", "12345", "nimal@email.com", "Male");
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("contact")));
    }

    @Test
    void validatePatient_allowsMissingOptionalEmail() {
        List<String> errors = ValidationUtil.validatePatient(
                "Nimal Perera", "0771234567", "", "Male");
        assertTrue(errors.isEmpty());
    }

    @Test
    void validatePatient_rejectsMissingGender() {
        List<String> errors = ValidationUtil.validatePatient(
                "Nimal Perera", "0771234567", "nimal@email.com", "");
        assertFalse(errors.isEmpty());
    }

    // ── Appointment Validation ──────────────────────────────────────

    @Test
    void validateAppointment_rejectsPastDate() {
        String yesterday = LocalDate.now().minusDays(1).toString();
        List<String> errors = ValidationUtil.validateAppointment(
                "1", "1", "1", yesterday, "09:00");
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("past")));
    }

    @Test
    void validateAppointment_rejectsOutOfHoursTime() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        List<String> errors = ValidationUtil.validateAppointment(
                "1", "1", "1", tomorrow, "20:00");
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("clinic hours")
                || e.toLowerCase().contains("08:00")));
    }

    @Test
    void validateAppointment_acceptsValidFutureSlot() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        List<String> errors = ValidationUtil.validateAppointment(
                "1", "2", "3", tomorrow, "09:00");
        assertTrue(errors.isEmpty());
    }

    @Test
    void validateAppointment_rejectsUnselectedDropdowns() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        List<String> errors = ValidationUtil.validateAppointment(
                "0", "0", "0", tomorrow, "09:00");
        assertEquals(3, errors.size());
    }

    // ── Bill Validation ──────────────────────────────────────────────

    @Test
    void validateBill_rejectsNegativeDiscount() {
        List<String> errors = ValidationUtil.validateBill("1", "Cash", "-500");
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("negative")));
    }

    @Test
    void validateBill_rejectsInvalidPaymentMethod() {
        List<String> errors = ValidationUtil.validateBill("1", "Bitcoin", "0");
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("payment method")));
    }

    @Test
    void validateBill_acceptsValidInput() {
        List<String> errors = ValidationUtil.validateBill("1", "Card", "500");
        assertTrue(errors.isEmpty());
    }

    // ── General Helpers ──────────────────────────────────────────────

    @Test
    void parseIntSafe_returnsDefaultOnBadInput() {
        assertEquals(42, ValidationUtil.parseIntSafe("not-a-number", 42));
        assertEquals(7, ValidationUtil.parseIntSafe("7", 42));
    }

    @Test
    void parseDoubleSafe_returnsDefaultOnBadInput() {
        assertEquals(1.5, ValidationUtil.parseDoubleSafe("nope", 1.5));
        assertEquals(9.99, ValidationUtil.parseDoubleSafe("9.99", 1.5));
    }

    @Test
    void isValidId_rejectsZeroAndNegative() {
        assertFalse(ValidationUtil.isValidId("0"));
        assertFalse(ValidationUtil.isValidId("-5"));
        assertTrue(ValidationUtil.isValidId("5"));
    }
}
