package lk.sunrise.dental.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * ValidationUtil.java
 * Form Input Validation Utilities
 *
 * Centralized validation for all form inputs across the system.
 * Returns error messages list - empty list means valid.
 * Package : lk.sunrise.dental.util
 * ================================================================
 */
public class ValidationUtil {

    // Prevent instantiation
    private ValidationUtil() {}

    // ──────────────────────────────────────────────────────────────
    // PATIENT VALIDATION
    // ──────────────────────────────────────────────────────────────

    /**
     * Validate patient registration form inputs.
     *
     * @param fullName    patient full name
     * @param contact     phone number
     * @param email       email address (optional)
     * @param gender      gender selection
     * @return List of error messages. Empty = valid.
     */
    public static List<String> validatePatient(String fullName, String contact,
                                               String email, String gender) {
        List<String> errors = new ArrayList<>();

        // Full Name
        if (SecurityUtil.isEmpty(fullName)) {
            errors.add("Patient full name is required.");
        } else if (fullName.trim().length() < 2) {
            errors.add("Patient name must be at least 2 characters.");
        } else if (fullName.trim().length() > 100) {
            errors.add("Patient name cannot exceed 100 characters.");
        } else if (!fullName.trim().matches("^[a-zA-Z\\s.'-]+$")) {
            errors.add("Patient name can only contain letters, spaces, dots, hyphens and apostrophes.");
        }

        // Contact
        if (SecurityUtil.isEmpty(contact)) {
            errors.add("Contact number is required.");
        } else if (!SecurityUtil.isValidContact(contact)) {
            errors.add("Contact number must be a valid Sri Lankan number (e.g. 0771234567).");
        }

        // Email (optional but validate format if provided)
        if (!SecurityUtil.isEmpty(email) && !SecurityUtil.isValidEmail(email)) {
            errors.add("Please enter a valid email address.");
        }

        // Gender
        if (SecurityUtil.isEmpty(gender)) {
            errors.add("Please select a gender.");
        }

        return errors;
    }

    // ──────────────────────────────────────────────────────────────
    // APPOINTMENT VALIDATION
    // ──────────────────────────────────────────────────────────────

    /**
     * Validate appointment booking form inputs.
     *
     * @param patientId   selected patient ID
     * @param dentistId   selected dentist ID
     * @param treatmentId selected treatment ID
     * @param aptDate     appointment date string (yyyy-MM-dd)
     * @param aptTime     appointment time string (HH:mm)
     * @return List of error messages. Empty = valid.
     */
    public static List<String> validateAppointment(String patientId, String dentistId,
                                                    String treatmentId, String aptDate,
                                                    String aptTime) {
        List<String> errors = new ArrayList<>();

        // Patient
        if (SecurityUtil.isEmpty(patientId) || patientId.equals("0")) {
            errors.add("Please select a patient.");
        }

        // Dentist
        if (SecurityUtil.isEmpty(dentistId) || dentistId.equals("0")) {
            errors.add("Please select a dentist.");
        }

        // Treatment
        if (SecurityUtil.isEmpty(treatmentId) || treatmentId.equals("0")) {
            errors.add("Please select a treatment type.");
        }

        // Appointment Date
        if (SecurityUtil.isEmpty(aptDate)) {
            errors.add("Appointment date is required.");
        } else {
            try {
                LocalDate date = LocalDate.parse(aptDate);
                if (date.isBefore(LocalDate.now())) {
                    errors.add("Appointment date cannot be in the past.");
                }
            } catch (Exception e) {
                errors.add("Please enter a valid appointment date.");
            }
        }

        // Appointment Time
        if (SecurityUtil.isEmpty(aptTime)) {
            errors.add("Appointment time is required.");
        } else {
            try {
                LocalTime time = LocalTime.parse(aptTime);
                // Clinic hours: 8:00 AM to 6:00 PM
                LocalTime openTime  = LocalTime.of(8, 0);
                LocalTime closeTime = LocalTime.of(18, 0);
                if (time.isBefore(openTime) || time.isAfter(closeTime)) {
                    errors.add("Appointment time must be between 08:00 AM and 06:00 PM.");
                }
            } catch (Exception e) {
                errors.add("Please enter a valid appointment time.");
            }
        }

        return errors;
    }

    // ──────────────────────────────────────────────────────────────
    // BILLING VALIDATION
    // ──────────────────────────────────────────────────────────────

    /**
     * Validate bill creation form inputs.
     *
     * @param appointmentId selected appointment ID
     * @param paymentMethod selected payment method
     * @param discount      discount amount string
     * @return List of error messages. Empty = valid.
     */
    public static List<String> validateBill(String appointmentId,
                                             String paymentMethod,
                                             String discount) {
        List<String> errors = new ArrayList<>();

        // Appointment
        if (SecurityUtil.isEmpty(appointmentId) || appointmentId.equals("0")) {
            errors.add("Please select an appointment.");
        }

        // Payment Method
        if (SecurityUtil.isEmpty(paymentMethod)) {
            errors.add("Please select a payment method.");
        } else {
            List<String> validMethods = List.of("Cash", "Card", "Online Transfer", "QR Payment");
            if (!validMethods.contains(paymentMethod)) {
                errors.add("Invalid payment method selected.");
            }
        }

        // Discount
        if (!SecurityUtil.isEmpty(discount)) {
            try {
                double disc = Double.parseDouble(discount);
                if (disc < 0) {
                    errors.add("Discount cannot be negative.");
                }
                if (disc > 100000) {
                    errors.add("Discount amount seems too high. Please verify.");
                }
            } catch (NumberFormatException e) {
                errors.add("Discount must be a valid number.");
            }
        }

        return errors;
    }

    // ──────────────────────────────────────────────────────────────
    // USER / STAFF VALIDATION
    // ──────────────────────────────────────────────────────────────

    /**
     * Validate staff registration form inputs.
     *
     * @param username  login username
     * @param fullName  staff full name
     * @param password  plain password
     * @param role      staff role
     * @param contact   phone number
     * @param email     email address
     * @return List of error messages. Empty = valid.
     */
    public static List<String> validateUser(String username, String fullName,
                                             String password, String role,
                                             String contact, String email) {
        List<String> errors = new ArrayList<>();

        // Username
        if (SecurityUtil.isEmpty(username)) {
            errors.add("Username is required.");
        } else if (username.trim().length() < 3) {
            errors.add("Username must be at least 3 characters.");
        } else if (username.trim().length() > 50) {
            errors.add("Username cannot exceed 50 characters.");
        } else if (!username.trim().matches("^[a-zA-Z0-9._-]+$")) {
            errors.add("Username can only contain letters, numbers, dots, hyphens and underscores.");
        }

        // Full Name
        if (SecurityUtil.isEmpty(fullName)) {
            errors.add("Full name is required.");
        } else if (fullName.trim().length() < 2) {
            errors.add("Full name must be at least 2 characters.");
        }

        // Password
        if (SecurityUtil.isEmpty(password)) {
            errors.add("Password is required.");
        } else if (password.length() < 6) {
            errors.add("Password must be at least 6 characters.");
        } else if (password.length() > 64) {
            errors.add("Password cannot exceed 64 characters.");
        }

        // Role
        if (SecurityUtil.isEmpty(role)) {
            errors.add("Please select a role.");
        } else {
            List<String> validRoles = List.of("ADMIN", "RECEPTIONIST", "DENTIST");
            if (!validRoles.contains(role.toUpperCase())) {
                errors.add("Invalid role selected.");
            }
        }

        // Contact
        if (!SecurityUtil.isEmpty(contact) && !SecurityUtil.isValidContact(contact)) {
            errors.add("Contact number must be a valid Sri Lankan number.");
        }

        // Email
        if (!SecurityUtil.isEmpty(email) && !SecurityUtil.isValidEmail(email)) {
            errors.add("Please enter a valid email address.");
        }

        return errors;
    }

    // ──────────────────────────────────────────────────────────────
    // LOGIN VALIDATION
    // ──────────────────────────────────────────────────────────────

    /**
     * Validate login form inputs.
     *
     * @param username login username
     * @param password login password
     * @return List of error messages. Empty = valid.
     */
    public static List<String> validateLogin(String username, String password) {
        List<String> errors = new ArrayList<>();

        if (SecurityUtil.isEmpty(username)) {
            errors.add("Username is required.");
        }

        if (SecurityUtil.isEmpty(password)) {
            errors.add("Password is required.");
        }

        return errors;
    }

    // ──────────────────────────────────────────────────────────────
    // GENERAL HELPERS
    // ──────────────────────────────────────────────────────────────

    /**
     * Check if a string is a valid positive integer.
     */
    public static boolean isValidId(String value) {
        if (SecurityUtil.isEmpty(value)) return false;
        try {
            return Integer.parseInt(value.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parse integer safely with default value.
     */
    public static int parseIntSafe(String value, int defaultVal) {
        if (SecurityUtil.isEmpty(value)) return defaultVal;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * Parse double safely with default value.
     */
    public static double parseDoubleSafe(String value, double defaultVal) {
        if (SecurityUtil.isEmpty(value)) return defaultVal;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

}