package lk.sunrise.dental.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ================================================================
 * Appointment.java
 * Model class representing a patient appointment
 * Package : lk.sunrise.dental.model
 * ================================================================
 */
public class Appointment {

    // ── Fields ─────────────────────────────────────────────────────
    private int           id;
    private String        aptCode;
    private int           patientId;
    private int           dentistId;
    private int           treatmentId;
    private LocalDate     aptDate;
    private LocalTime     aptTime;
    private String        status;      // Scheduled, In Progress, Completed, Cancelled
    private String        notes;
    private int           createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Extra fields populated via JOIN queries ────────────────────
    private String        patientName;
    private String        patientCode;
    private String        patientContact;
    private String        dentistName;
    private String        treatmentName;
    private double        treatmentCost;
    private double        consultFee;
    private int           treatmentDuration;
    private boolean       hasBill;
    private String        billCode;
    private String        billStatus;
    private String        createdByName;

    // ── Constructors ───────────────────────────────────────────────

    /** Default constructor */
    public Appointment() {}

    /** Full constructor */
    public Appointment(int id, String aptCode, int patientId, int dentistId,
                       int treatmentId, LocalDate aptDate, LocalTime aptTime,
                       String status, String notes, int createdBy,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id          = id;
        this.aptCode     = aptCode;
        this.patientId   = patientId;
        this.dentistId   = dentistId;
        this.treatmentId = treatmentId;
        this.aptDate     = aptDate;
        this.aptTime     = aptTime;
        this.status      = status;
        this.notes       = notes;
        this.createdBy   = createdBy;
        this.createdAt   = createdAt;
        this.updatedAt   = updatedAt;
    }

    // ── Status Helper Methods ──────────────────────────────────────

    public boolean isScheduled()  { return "Scheduled".equalsIgnoreCase(status); }
    public boolean isInProgress() { return "In Progress".equalsIgnoreCase(status); }
    public boolean isCompleted()  { return "Completed".equalsIgnoreCase(status); }
    public boolean isCancelled()  { return "Cancelled".equalsIgnoreCase(status); }

    /**
     * Get status badge CSS class for styling.
     */
    public String getStatusBadgeClass() {
        if (status == null) return "badge-default";
        return switch (status) {
            case "Scheduled"   -> "badge-scheduled";
            case "In Progress" -> "badge-inprogress";
            case "Completed"   -> "badge-completed";
            case "Cancelled"   -> "badge-cancelled";
            default            -> "badge-default";
        };
    }

    /**
     * Get status icon for display.
     */
    public String getStatusIcon() {
        if (status == null) return "❓";
        return switch (status) {
            case "Scheduled"   -> "📅";
            case "In Progress" -> "⚙️";
            case "Completed"   -> "✅";
            case "Cancelled"   -> "❌";
            default            -> "❓";
        };
    }

    /**
     * Check if appointment can be edited.
     * Only Scheduled appointments can be modified.
     */
    public boolean isEditable() {
        return isScheduled();
    }

    /**
     * Check if appointment can be cancelled.
     */
    public boolean isCancellable() {
        return isScheduled() || isInProgress();
    }

    /**
     * Calculate total cost (treatment + consultation fee).
     */
    public double getTotalCost() {
        return treatmentCost + consultFee;
    }

    /**
     * Get formatted appointment date and time display.
     * Example: "15 Jan 2025 at 09:00 AM"
     */
    public String getDateTimeDisplay() {
        if (aptDate == null || aptTime == null) return "N/A";
        return aptDate + " at " + aptTime;
    }

    // ── Getters ────────────────────────────────────────────────────

    public int           getId()                { return id; }
    public String        getAptCode()           { return aptCode; }
    public int           getPatientId()         { return patientId; }
    public int           getDentistId()         { return dentistId; }
    public int           getTreatmentId()       { return treatmentId; }
    public LocalDate     getAptDate()           { return aptDate; }
    public LocalTime     getAptTime()           { return aptTime; }
    public String        getStatus()            { return status; }
    public String        getNotes()             { return notes; }
    public int           getCreatedBy()         { return createdBy; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public String        getPatientName()       { return patientName; }
    public String        getPatientCode()       { return patientCode; }
    public String        getPatientContact()    { return patientContact; }
    public String        getDentistName()       { return dentistName; }
    public String        getTreatmentName()     { return treatmentName; }
    public double        getTreatmentCost()     { return treatmentCost; }
    public double        getConsultFee()        { return consultFee; }
    public int           getTreatmentDuration() { return treatmentDuration; }
    public boolean       isHasBill()            { return hasBill; }
    public String        getBillCode()          { return billCode; }
    public String        getBillStatus()        { return billStatus; }
    public String        getCreatedByName()     { return createdByName; }

    // ── Setters ────────────────────────────────────────────────────

    public void setId(int id)                                  { this.id                = id; }
    public void setAptCode(String aptCode)                     { this.aptCode           = aptCode; }
    public void setPatientId(int patientId)                    { this.patientId         = patientId; }
    public void setDentistId(int dentistId)                    { this.dentistId         = dentistId; }
    public void setTreatmentId(int treatmentId)               { this.treatmentId        = treatmentId; }
    public void setAptDate(LocalDate aptDate)                  { this.aptDate           = aptDate; }
    public void setAptTime(LocalTime aptTime)                  { this.aptTime           = aptTime; }
    public void setStatus(String status)                       { this.status            = status; }
    public void setNotes(String notes)                         { this.notes             = notes; }
    public void setCreatedBy(int createdBy)                    { this.createdBy         = createdBy; }
    public void setCreatedAt(LocalDateTime createdAt)          { this.createdAt         = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)          { this.updatedAt         = updatedAt; }
    public void setPatientName(String patientName)             { this.patientName       = patientName; }
    public void setPatientCode(String patientCode)             { this.patientCode       = patientCode; }
    public void setPatientContact(String patientContact)       { this.patientContact    = patientContact; }
    public void setDentistName(String dentistName)             { this.dentistName       = dentistName; }
    public void setTreatmentName(String treatmentName)         { this.treatmentName     = treatmentName; }
    public void setTreatmentCost(double treatmentCost)         { this.treatmentCost     = treatmentCost; }
    public void setConsultFee(double consultFee)               { this.consultFee        = consultFee; }
    public void setTreatmentDuration(int treatmentDuration)    { this.treatmentDuration = treatmentDuration; }
    public void setHasBill(boolean hasBill)                    { this.hasBill           = hasBill; }
    public void setBillCode(String billCode)                   { this.billCode          = billCode; }
    public void setBillStatus(String billStatus)               { this.billStatus        = billStatus; }
    public void setCreatedByName(String createdByName)         { this.createdByName     = createdByName; }

    // ── toString ───────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", aptCode='" + aptCode + '\'' +
                ", patientName='" + patientName + '\'' +
                ", dentistName='" + dentistName + '\'' +
                ", aptDate=" + aptDate +
                ", aptTime=" + aptTime +
                ", status='" + status + '\'' +
                '}';
    }
}