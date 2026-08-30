package lk.sunrise.dental.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * ================================================================
 * Patient.java
 * Model class representing a patient medical profile
 * Package : lk.sunrise.dental.model
 * ================================================================
 */
public class Patient {

    // ── Fields ─────────────────────────────────────────────────────
    private int           id;
    private String        patientCode;
    private String        fullName;
    private LocalDate     dateOfBirth;
    private String        gender;
    private String        address;
    private String        contact;
    private String        email;
    private String        bloodType;
    private String        allergies;
    private String        medicalNotes;
    private boolean       isActive;
    private int           registeredBy;  // FK to users.id
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Extra fields populated via JOIN queries ────────────────────
    private String        registeredByName; // staff name who registered patient
    private int           totalAppointments;
    private int           completedTreatments;
    private int           activeBookings;

    // ── Constructors ───────────────────────────────────────────────

    /** Default constructor */
    public Patient() {}

    /** Constructor for quick registration */
    public Patient(String patientCode, String fullName, String contact) {
        this.patientCode = patientCode;
        this.fullName    = fullName;
        this.contact     = contact;
    }

    /** Full constructor */
    public Patient(int id, String patientCode, String fullName,
                   LocalDate dateOfBirth, String gender, String address,
                   String contact, String email, String bloodType,
                   String allergies, String medicalNotes, boolean isActive,
                   int registeredBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id           = id;
        this.patientCode  = patientCode;
        this.fullName     = fullName;
        this.dateOfBirth  = dateOfBirth;
        this.gender       = gender;
        this.address      = address;
        this.contact      = contact;
        this.email        = email;
        this.bloodType    = bloodType;
        this.allergies    = allergies;
        this.medicalNotes = medicalNotes;
        this.isActive     = isActive;
        this.registeredBy = registeredBy;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
    }

    // ── Computed Properties ────────────────────────────────────────

    /**
     * Calculate patient age from date of birth.
     * Returns 0 if DOB is not set.
     */
    public int getAge() {
        if (dateOfBirth == null) return 0;
        return (int) ChronoUnit.YEARS.between(dateOfBirth, LocalDate.now());
    }

    /**
     * Get gender icon for display.
     */
    public String getGenderIcon() {
        if ("Male".equalsIgnoreCase(gender))   return "♂";
        if ("Female".equalsIgnoreCase(gender)) return "♀";
        return "⚥";
    }

    /**
     * Get blood type badge CSS class.
     */
    public String getBloodTypeBadgeClass() {
        if (bloodType == null) return "badge-unknown";
        return switch (bloodType) {
            case "A+", "A-"   -> "badge-blood-a";
            case "B+", "B-"   -> "badge-blood-b";
            case "AB+", "AB-" -> "badge-blood-ab";
            case "O+", "O-"   -> "badge-blood-o";
            default            -> "badge-unknown";
        };
    }

    /**
     * Check if patient has any allergies recorded.
     */
    public boolean hasAllergies() {
        return allergies != null && !allergies.trim().isEmpty();
    }

    /**
     * Check if patient has medical notes.
     */
    public boolean hasMedicalNotes() {
        return medicalNotes != null && !medicalNotes.trim().isEmpty();
    }

    // ── Getters ────────────────────────────────────────────────────

    public int           getId()                  { return id; }
    public String        getPatientCode()         { return patientCode; }
    public String        getFullName()            { return fullName; }
    public LocalDate     getDateOfBirth()         { return dateOfBirth; }
    public String        getGender()              { return gender; }
    public String        getAddress()             { return address; }
    public String        getContact()             { return contact; }
    public String        getEmail()               { return email; }
    public String        getBloodType()           { return bloodType; }
    public String        getAllergies()            { return allergies; }
    public String        getMedicalNotes()         { return medicalNotes; }
    public boolean       isActive()               { return isActive; }
    public int           getRegisteredBy()        { return registeredBy; }
    public LocalDateTime getCreatedAt()           { return createdAt; }
    public LocalDateTime getUpdatedAt()           { return updatedAt; }
    public String        getRegisteredByName()    { return registeredByName; }
    public int           getTotalAppointments()   { return totalAppointments; }
    public int           getCompletedTreatments() { return completedTreatments; }
    public int           getActiveBookings()      { return activeBookings; }

    // ── Setters ────────────────────────────────────────────────────

    public void setId(int id)                                  { this.id                 = id; }
    public void setPatientCode(String patientCode)             { this.patientCode         = patientCode; }
    public void setFullName(String fullName)                   { this.fullName            = fullName; }
    public void setDateOfBirth(LocalDate dateOfBirth)          { this.dateOfBirth         = dateOfBirth; }
    public void setGender(String gender)                       { this.gender              = gender; }
    public void setAddress(String address)                     { this.address             = address; }
    public void setContact(String contact)                     { this.contact             = contact; }
    public void setEmail(String email)                         { this.email               = email; }
    public void setBloodType(String bloodType)                 { this.bloodType           = bloodType; }
    public void setAllergies(String allergies)                 { this.allergies           = allergies; }
    public void setMedicalNotes(String medicalNotes)           { this.medicalNotes        = medicalNotes; }
    public void setActive(boolean isActive)                    { this.isActive            = isActive; }
    public void setRegisteredBy(int registeredBy)              { this.registeredBy        = registeredBy; }
    public void setCreatedAt(LocalDateTime createdAt)          { this.createdAt           = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)          { this.updatedAt           = updatedAt; }
    public void setRegisteredByName(String registeredByName)   { this.registeredByName    = registeredByName; }
    public void setTotalAppointments(int totalAppointments)    { this.totalAppointments   = totalAppointments; }
    public void setCompletedTreatments(int completedTreatments){ this.completedTreatments = completedTreatments; }
    public void setActiveBookings(int activeBookings)          { this.activeBookings       = activeBookings; }

    // ── toString ───────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", patientCode='" + patientCode + '\'' +
                ", fullName='" + fullName + '\'' +
                ", contact='" + contact + '\'' +
                ", gender='" + gender + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}