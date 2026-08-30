package lk.sunrise.dental.service;

import lk.sunrise.dental.dao.PatientDAO;
import lk.sunrise.dental.model.Patient;
import lk.sunrise.dental.service.event.EmailNotificationListener;
import lk.sunrise.dental.service.event.NotificationListener;
import lk.sunrise.dental.util.ValidationUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * PatientService.java
 * Business Logic Layer for Patient operations
 *
 * Handles patient registration, updates and search business rules.
 *
 * Design Pattern : Observer - notifies registered NotificationListeners
 * when a new patient is registered (see AppointmentService for the
 * same pattern applied to appointment events).
 * Package : lk.sunrise.dental.service
 * ================================================================
 */
public class PatientService {

    private final PatientDAO patientDAO = new PatientDAO();
    private final List<NotificationListener> listeners = new ArrayList<>();

    public PatientService() {
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
     * Get all active patients.
     */
    public List<Patient> getAllPatients() {
        return patientDAO.getAllPatients();
    }

    /**
     * Get patient by ID.
     */
    public Patient getPatientById(int id) {
        if (id <= 0) return null;
        return patientDAO.getPatientById(id);
    }

    /**
     * Get patient with appointment statistics.
     * Used for patient detail view.
     */
    public Patient getPatientWithStats(int id) {
        if (id <= 0) return null;
        return patientDAO.getPatientWithStats(id);
    }

    /**
     * Search patients by keyword.
     * Searches name, contact, email and patient code.
     *
     * @param keyword search term
     * @return list of matching patients or all patients if keyword is empty
     */
    public List<Patient> searchPatients(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return patientDAO.getAllPatients();
        }
        return patientDAO.searchPatients(keyword.trim());
    }

    /**
     * Get total active patient count.
     */
    public int getTotalPatients() {
        return patientDAO.getTotalPatients();
    }

    /**
     * Get new patients registered this month.
     */
    public int getNewPatientsThisMonth() {
        return patientDAO.getNewPatientsThisMonth();
    }

    // ──────────────────────────────────────────────────────────────
    // CREATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Register a new patient.
     * Validates inputs and checks for duplicate contact numbers.
     *
     * @param fullName     patient full name
     * @param dobStr       date of birth string (yyyy-MM-dd) - optional
     * @param gender       gender selection
     * @param address      home address
     * @param contact      phone number
     * @param email        email address - optional
     * @param bloodType    blood type - optional
     * @param allergies    known allergies - optional
     * @param medicalNotes general medical notes - optional
     * @param registeredBy ID of staff registering the patient
     * @return ServiceResult with success flag, message and patient ID
     */
    public ServiceResult registerPatient(String fullName, String dobStr,
                                          String gender, String address,
                                          String contact, String email,
                                          String bloodType, String allergies,
                                          String medicalNotes, int registeredBy) {
        // Validate inputs
        List<String> errors = ValidationUtil.validatePatient(
                fullName, contact, email, gender
        );
        if (!errors.isEmpty()) {
            return ServiceResult.failure(String.join(" | ", errors));
        }

        // Check duplicate contact number
        if (patientDAO.contactExists(contact.trim())) {
            return ServiceResult.failure(
                    "A patient with contact number '" + contact +
                    "' is already registered in the system."
            );
        }

        // Parse date of birth
        LocalDate dob = null;
        if (dobStr != null && !dobStr.trim().isEmpty()) {
            try {
                dob = LocalDate.parse(dobStr.trim());
                // Validate DOB is not in future
                if (dob.isAfter(LocalDate.now())) {
                    return ServiceResult.failure("Date of birth cannot be in the future.");
                }
                // Validate age is reasonable (max 130 years)
                if (dob.isBefore(LocalDate.now().minusYears(130))) {
                    return ServiceResult.failure("Please enter a valid date of birth.");
                }
            } catch (Exception e) {
                return ServiceResult.failure("Invalid date of birth format. Use YYYY-MM-DD.");
            }
        }

        // Build Patient object
        Patient patient = new Patient();
        patient.setPatientCode(patientDAO.generatePatientCode());
        patient.setFullName(fullName.trim());
        patient.setDateOfBirth(dob);
        patient.setGender(gender);
        patient.setAddress(address != null ? address.trim() : null);
        patient.setContact(contact.trim());
        patient.setEmail(email != null ? email.trim() : null);
        patient.setBloodType(bloodType != null && !bloodType.isEmpty() ? bloodType : "Unknown");
        patient.setAllergies(allergies != null ? allergies.trim() : null);
        patient.setMedicalNotes(medicalNotes != null ? medicalNotes.trim() : null);
        patient.setRegisteredBy(registeredBy);
        patient.setActive(true);

        // Save to database
        int newId = patientDAO.createPatient(patient);
        if (newId > 0) {
            notifyRegistered(newId);
            return ServiceResult.success(
                    "Patient '" + fullName + "' registered successfully " +
                    "with code: " + patient.getPatientCode(),
                    newId
            );
        }

        return ServiceResult.failure("Failed to register patient. Please try again.");
    }

    /** Notify observers that a new patient was registered. */
    private void notifyRegistered(int patientId) {
        Patient created = patientDAO.getPatientById(patientId);
        for (NotificationListener listener : listeners) {
            try {
                listener.onPatientRegistered(created);
            } catch (Exception e) {
                System.err.println("[PatientService] Listener error (registered): " + e.getMessage());
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Update existing patient details.
     *
     * @param patientId    ID of patient to update
     * @param fullName     updated full name
     * @param dobStr       updated date of birth
     * @param gender       updated gender
     * @param address      updated address
     * @param contact      updated contact
     * @param email        updated email
     * @param bloodType    updated blood type
     * @param allergies    updated allergies
     * @param medicalNotes updated medical notes
     * @return ServiceResult with success flag and message
     */
    public ServiceResult updatePatient(int patientId, String fullName,
                                        String dobStr, String gender,
                                        String address, String contact,
                                        String email, String bloodType,
                                        String allergies, String medicalNotes) {
        // Check patient exists
        Patient existing = patientDAO.getPatientById(patientId);
        if (existing == null) {
            return ServiceResult.failure("Patient not found in system.");
        }

        // Validate inputs
        List<String> errors = ValidationUtil.validatePatient(
                fullName, contact, email, gender
        );
        if (!errors.isEmpty()) {
            return ServiceResult.failure(String.join(" | ", errors));
        }

        // Check duplicate contact for other patients
        if (patientDAO.contactExistsForOther(contact.trim(), patientId)) {
            return ServiceResult.failure(
                    "Contact number '" + contact + "' is already used by another patient."
            );
        }

        // Parse date of birth
        LocalDate dob = null;
        if (dobStr != null && !dobStr.trim().isEmpty()) {
            try {
                dob = LocalDate.parse(dobStr.trim());
            } catch (Exception e) {
                return ServiceResult.failure("Invalid date of birth format.");
            }
        }

        // Update fields
        existing.setFullName(fullName.trim());
        existing.setDateOfBirth(dob);
        existing.setGender(gender);
        existing.setAddress(address != null ? address.trim() : null);
        existing.setContact(contact.trim());
        existing.setEmail(email != null ? email.trim() : null);
        existing.setBloodType(bloodType != null && !bloodType.isEmpty() ? bloodType : "Unknown");
        existing.setAllergies(allergies != null ? allergies.trim() : null);
        existing.setMedicalNotes(medicalNotes != null ? medicalNotes.trim() : null);

        boolean updated = patientDAO.updatePatient(existing);
        if (updated) {
            return ServiceResult.success(
                    "Patient '" + fullName + "' updated successfully."
            );
        }

        return ServiceResult.failure("Failed to update patient details. Please try again.");
    }

    /**
     * Deactivate (soft delete) a patient.
     *
     * @param patientId ID of patient to deactivate
     * @return ServiceResult with success flag and message
     */
    public ServiceResult deactivatePatient(int patientId) {
        Patient patient = patientDAO.getPatientById(patientId);
        if (patient == null) {
            return ServiceResult.failure("Patient not found.");
        }

        boolean deactivated = patientDAO.deactivatePatient(patientId);
        if (deactivated) {
            return ServiceResult.success(
                    "Patient '" + patient.getFullName() + "' has been removed from active records."
            );
        }

        return ServiceResult.failure("Failed to deactivate patient record.");
    }
}