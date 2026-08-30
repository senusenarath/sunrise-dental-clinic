package lk.sunrise.dental.service;

import lk.sunrise.dental.dao.AppointmentDAO;
import lk.sunrise.dental.dao.PatientDAO;
import lk.sunrise.dental.dao.UserDAO;
import lk.sunrise.dental.model.Appointment;
import lk.sunrise.dental.model.Patient;
import lk.sunrise.dental.model.User;
import lk.sunrise.dental.service.event.EmailNotificationListener;
import lk.sunrise.dental.service.event.NotificationListener;
import lk.sunrise.dental.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * AppointmentService.java
 * Business Logic Layer for Appointment operations
 *
 * Handles booking validation, double-booking prevention,
 * status updates and appointment management rules.
 *
 * Design Pattern : Observer - notifies registered NotificationListeners
 * (e.g. EmailNotificationListener) when an appointment is booked or
 * cancelled, so every caller (JSP servlets AND the REST API) gets
 * consistent notification behaviour without duplicating email code.
 * Package : lk.sunrise.dental.service
 * ================================================================
 */
public class AppointmentService {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO     patientDAO     = new PatientDAO();
    private final UserDAO        userDAO        = new UserDAO();
    private final List<NotificationListener> listeners = new ArrayList<>();

    public AppointmentService() {
        listeners.add(new EmailNotificationListener());
    }

    /** Register an additional observer (e.g. for tests, or a future SMS channel). */
    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    // ──────────────────────────────────────────────────────────────
    // READ OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Get all appointments.
     */
    public List<Appointment> getAllAppointments() {
        return appointmentDAO.getAllAppointments();
    }

    /**
     * Get today's appointments.
     */
    public List<Appointment> getTodayAppointments() {
        return appointmentDAO.getTodayAppointments();
    }

    /**
     * Send reminder emails for all of tomorrow's scheduled appointments.
     * Reuses the same EmailService the Observer-pattern listener uses,
     * but is triggered manually (staff click "Send Reminders") rather
     * than by a booking/cancellation event.
     *
     * @return count of reminder emails actually queued
     */
    public int sendRemindersForTomorrow() {
        List<Appointment> tomorrow = appointmentDAO.getTomorrowAppointments();
        int sent = 0;

        for (Appointment apt : tomorrow) {
            Patient patient = patientDAO.getPatientById(apt.getPatientId());
            if (patient == null || patient.getEmail() == null || patient.getEmail().trim().isEmpty()) {
                continue;
            }

            final String email     = patient.getEmail();
            final String name      = patient.getFullName();
            final String code      = apt.getAptCode();
            final String dentist   = apt.getDentistName();
            final String treatment = apt.getTreatmentName();
            final String date      = String.valueOf(apt.getAptDate());
            final String time      = String.valueOf(apt.getAptTime());

            new Thread(() -> lk.sunrise.dental.util.EmailService.getInstance()
                    .sendAppointmentReminder(email, name, code, dentist, treatment, date, time)
            ).start();
            sent++;
        }

        return sent;
    }

    /**
     * Get appointment by ID.
     */
    public Appointment getAppointmentById(int id) {
        if (id <= 0) return null;
        return appointmentDAO.getAppointmentById(id);
    }

    /**
     * Get appointments by status filter.
     *
     * @param status filter value or null/empty for all
     */
    public List<Appointment> getAppointmentsByStatus(String status) {
        if (status == null || status.trim().isEmpty() || status.equals("All")) {
            return appointmentDAO.getAllAppointments();
        }
        return appointmentDAO.getAppointmentsByStatus(status);
    }

    /**
     * Get appointments for a specific patient.
     */
    public List<Appointment> getAppointmentsByPatient(int patientId) {
        return appointmentDAO.getAppointmentsByPatient(patientId);
    }

    /**
     * Get appointments for a specific dentist.
     */
    public List<Appointment> getAppointmentsByDentist(int dentistId) {
        return appointmentDAO.getAppointmentsByDentist(dentistId);
    }

    /**
     * Get today's appointments for a specific dentist.
     */
    public List<Appointment> getTodayAppointmentsByDentist(int dentistId) {
        return appointmentDAO.getTodayAppointmentsByDentist(dentistId);
    }

    /**
     * Check whether a dentist is currently with a patient right now.
     *
     * @return the estimated time they become free, or null if available now
     */
    public LocalTime getDentistBusyUntil(int dentistId) {
        return appointmentDAO.getDentistBusyUntil(dentistId);
    }

    /**
     * Search appointments by keyword.
     *
     * @param keyword search term (patient name or apt code)
     */
    public List<Appointment> searchAppointments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return appointmentDAO.getAllAppointments();
        }
        return appointmentDAO.searchAppointments(keyword.trim());
    }

    // ── Statistics ─────────────────────────────────────────────────

    public int getTotalAppointments()  { return appointmentDAO.getTotalAppointments(); }
    public int getTodayCount()         { return appointmentDAO.getTodayCount(); }
    public int getScheduledCount()     { return appointmentDAO.getScheduledCount(); }
    public int getCompletedCount()     { return appointmentDAO.getCompletedCount(); }
    public int getThisMonthCount()     { return appointmentDAO.getThisMonthCount(); }

    // ──────────────────────────────────────────────────────────────
    // CREATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Book a new patient appointment.
     * Validates inputs and prevents double booking.
     *
     * @param patientIdStr   patient ID string from form
     * @param dentistIdStr   dentist ID string from form
     * @param treatmentIdStr treatment ID string from form
     * @param aptDateStr     appointment date string (yyyy-MM-dd)
     * @param aptTimeStr     appointment time string (HH:mm)
     * @param notes          optional clinical notes
     * @param createdBy      ID of staff creating appointment
     * @return ServiceResult with success flag, message and appointment ID
     */
    public ServiceResult bookAppointment(String patientIdStr, String dentistIdStr,
                                          String treatmentIdStr, String aptDateStr,
                                          String aptTimeStr, String notes,
                                          int createdBy) {
        // Validate form inputs
        List<String> errors = ValidationUtil.validateAppointment(
                patientIdStr, dentistIdStr, treatmentIdStr, aptDateStr, aptTimeStr
        );
        if (!errors.isEmpty()) {
            return ServiceResult.failure(String.join(" | ", errors));
        }

        // Parse IDs
        int patientId   = ValidationUtil.parseIntSafe(patientIdStr, 0);
        int dentistId   = ValidationUtil.parseIntSafe(dentistIdStr, 0);
        int treatmentId = ValidationUtil.parseIntSafe(treatmentIdStr, 0);

        // Parse date and time
        LocalDate aptDate;
        LocalTime aptTime;
        try {
            aptDate = LocalDate.parse(aptDateStr);
            aptTime = LocalTime.parse(aptTimeStr);
        } catch (Exception e) {
            return ServiceResult.failure("Invalid date or time format.");
        }

        // ── DOUBLE BOOKING PREVENTION ────────────────────────────
        // Check if dentist already has appointment at this time
        if (appointmentDAO.isDentistBooked(dentistId, aptDateStr, aptTimeStr + ":00", 0)) {
            User dentist = userDAO.getUserById(dentistId);
            String dentistName = dentist != null ? dentist.getFullName() : "The selected dentist";
            return ServiceResult.failure(
                    dentistName + " already has an appointment at " +
                    aptTimeStr + " on " + aptDateStr +
                    ". Please select a different time slot."
            );
        }

        // Build appointment code
        String aptCode = appointmentDAO.generateAptCode();

        // Build Appointment object
        Appointment apt = new Appointment();
        apt.setAptCode(aptCode);
        apt.setPatientId(patientId);
        apt.setDentistId(dentistId);
        apt.setTreatmentId(treatmentId);
        apt.setAptDate(aptDate);
        apt.setAptTime(aptTime);
        apt.setStatus("Scheduled");
        apt.setNotes(notes != null ? notes.trim() : null);
        apt.setCreatedBy(createdBy);

        // Save to database
        int newId = appointmentDAO.createAppointment(apt);
        if (newId > 0) {
            notifyBooked(newId, patientId);
            return ServiceResult.success(
                    "Appointment booked successfully! Code: " + aptCode,
                    newId
            );
        }

        return ServiceResult.failure("Failed to book appointment. Please try again.");
    }

    /** Notify observers that a new appointment was booked. */
    private void notifyBooked(int appointmentId, int patientId) {
        Appointment created = appointmentDAO.getAppointmentById(appointmentId);
        Patient     patient = patientDAO.getPatientById(patientId);
        for (NotificationListener listener : listeners) {
            try {
                listener.onAppointmentBooked(created, patient);
            } catch (Exception e) {
                System.err.println("[AppointmentService] Listener error (booked): " + e.getMessage());
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Update treatment status and clinical notes.
     * Only dentists and admins can update clinical status.
     *
     * @param aptId      appointment ID
     * @param status     new status
     * @param notes      updated clinical notes
     * @param userRole   role of user making the update
     * @return ServiceResult with success flag and message
     */
    public ServiceResult updateTreatmentStatus(int aptId, String status,
                                                String notes, String userRole) {
        // Only ADMIN and DENTIST can update clinical status
        if (!"ADMIN".equalsIgnoreCase(userRole) && !"DENTIST".equalsIgnoreCase(userRole)) {
            return ServiceResult.failure(
                    "Access denied. Only dentists and administrators can update treatment status."
            );
        }

        // Check appointment exists
        Appointment apt = appointmentDAO.getAppointmentById(aptId);
        if (apt == null) {
            return ServiceResult.failure("Appointment not found.");
        }

        // Cannot update cancelled appointments
        if (apt.isCancelled()) {
            return ServiceResult.failure(
                    "Cannot update a cancelled appointment."
            );
        }

        // Validate status value
        List<String> validStatuses = List.of(
                "Scheduled", "In Progress", "Completed", "Cancelled"
        );
        if (status == null || !validStatuses.contains(status)) {
            return ServiceResult.failure("Invalid status value selected.");
        }

        boolean updated = appointmentDAO.updateTreatmentStatus(aptId, status, notes);
        if (updated) {
            return ServiceResult.success(
                    "Appointment " + apt.getAptCode() +
                    " status updated to '" + status + "' successfully."
            );
        }

        return ServiceResult.failure("Failed to update appointment status.");
    }

    /**
     * Cancel an existing appointment.
     *
     * @param aptId    appointment ID to cancel
     * @param userRole role of user cancelling
     * @return ServiceResult with success flag and message
     */
    public ServiceResult cancelAppointment(int aptId, String userRole) {
        // Check appointment exists
        Appointment apt = appointmentDAO.getAppointmentById(aptId);
        if (apt == null) {
            return ServiceResult.failure("Appointment not found.");
        }

        // Check it can be cancelled
        if (!apt.isCancellable()) {
            return ServiceResult.failure(
                    "This appointment cannot be cancelled as it is already " +
                    apt.getStatus().toLowerCase() + "."
            );
        }

        // Receptionists cannot cancel completed appointments
        if ("RECEPTIONIST".equalsIgnoreCase(userRole) && apt.isCompleted()) {
            return ServiceResult.failure(
                    "Receptionists cannot cancel completed appointments."
            );
        }

        boolean cancelled = appointmentDAO.cancelAppointment(aptId);
        if (cancelled) {
            notifyCancelled(apt);
            return ServiceResult.success(
                    "Appointment " + apt.getAptCode() + " cancelled successfully."
            );
        }

        return ServiceResult.failure("Failed to cancel appointment.");
    }

    /** Notify observers that an appointment was cancelled. */
    private void notifyCancelled(Appointment appointment) {
        Patient patient = patientDAO.getPatientById(appointment.getPatientId());
        for (NotificationListener listener : listeners) {
            try {
                listener.onAppointmentCancelled(appointment, patient);
            } catch (Exception e) {
                System.err.println("[AppointmentService] Listener error (cancelled): " + e.getMessage());
            }
        }
    }

    /**
     * Reschedule an existing appointment.
     *
     * @param aptId      appointment ID
     * @param newDateStr new date string
     * @param newTimeStr new time string
     * @param dentistId  dentist ID (may change)
     * @return ServiceResult with success flag and message
     */
    public ServiceResult rescheduleAppointment(int aptId, String newDateStr,
                                                String newTimeStr, int dentistId) {
        // Check appointment exists and is schedulable
        Appointment apt = appointmentDAO.getAppointmentById(aptId);
        if (apt == null) {
            return ServiceResult.failure("Appointment not found.");
        }

        if (!apt.isScheduled()) {
            return ServiceResult.failure(
                    "Only 'Scheduled' appointments can be rescheduled."
            );
        }

        // Parse and validate new date/time (same rules as a new booking)
        LocalDate newDate;
        LocalTime newTime;
        try {
            newDate = LocalDate.parse(newDateStr);
            newTime = LocalTime.parse(newTimeStr);
        } catch (Exception e) {
            return ServiceResult.failure("Invalid date or time format.");
        }

        if (newDate.isBefore(LocalDate.now())) {
            return ServiceResult.failure("Appointment date cannot be in the past.");
        }

        LocalTime openTime  = LocalTime.of(8, 0);
        LocalTime closeTime = LocalTime.of(18, 0);
        if (newTime.isBefore(openTime) || newTime.isAfter(closeTime)) {
            return ServiceResult.failure("Appointment time must be between 08:00 AM and 06:00 PM.");
        }

        // Check double booking (exclude current appointment)
        if (appointmentDAO.isDentistBooked(dentistId, newDateStr, newTimeStr + ":00", aptId)) {
            return ServiceResult.failure(
                    "The selected dentist is already booked at that time. " +
                    "Please choose a different time slot."
            );
        }

        apt.setAptDate(newDate);
        apt.setAptTime(newTime);
        apt.setDentistId(dentistId);

        boolean updated = appointmentDAO.updateAppointment(apt);
        if (updated) {
            return ServiceResult.success(
                    "Appointment rescheduled to " + newDateStr + " at " + newTimeStr
            );
        }

        return ServiceResult.failure("Failed to reschedule appointment.");
    }
}