package lk.sunrise.dental.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ================================================================
 * Bill.java
 * Model class representing a patient invoice/bill
 * Package : lk.sunrise.dental.model
 * ================================================================
 */
public class Bill {

    // ── Fields ─────────────────────────────────────────────────────
    private int           id;
    private String        billCode;
    private int           appointmentId;
    private double        treatmentFee;
    private double        consultFee;
    private double        discount;
    private double        totalAmount;
    private String        paymentMethod;  // Cash, Card, Online Transfer, QR Payment
    private String        status;         // Pending, Paid, Cancelled
    private int           settledBy;
    private LocalDateTime settledAt;
    private int           createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Extra fields populated via JOIN queries ────────────────────
    private String        patientName;
    private String        patientCode;
    private String        patientContact;
    private String        patientAddress;
    private String        dentistName;
    private String        treatmentName;
    private LocalDate     aptDate;
    private LocalTime     aptTime;
    private String        aptCode;
    private String        settledByName;
    private String        createdByName;

    // ── Constructors ───────────────────────────────────────────────

    /** Default constructor */
    public Bill() {}

    /** Full constructor */
    public Bill(int id, String billCode, int appointmentId,
                double treatmentFee, double consultFee, double discount,
                double totalAmount, String paymentMethod, String status,
                int settledBy, LocalDateTime settledAt,
                int createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id            = id;
        this.billCode      = billCode;
        this.appointmentId = appointmentId;
        this.treatmentFee  = treatmentFee;
        this.consultFee    = consultFee;
        this.discount      = discount;
        this.totalAmount   = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status        = status;
        this.settledBy     = settledBy;
        this.settledAt     = settledAt;
        this.createdBy     = createdBy;
        this.createdAt     = createdAt;
        this.updatedAt     = updatedAt;
    }

    // ── Status Helper Methods ──────────────────────────────────────

    public boolean isPending()   { return "Pending".equalsIgnoreCase(status); }
    public boolean isPaid()      { return "Paid".equalsIgnoreCase(status); }
    public boolean isCancelled() { return "Cancelled".equalsIgnoreCase(status); }

    /**
     * Get status badge CSS class.
     */
    public String getStatusBadgeClass() {
        if (status == null) return "badge-default";
        return switch (status) {
            case "Pending"   -> "badge-pending";
            case "Paid"      -> "badge-paid";
            case "Cancelled" -> "badge-cancelled";
            default          -> "badge-default";
        };
    }

    /**
     * Get status icon.
     */
    public String getStatusIcon() {
        if (status == null) return "❓";
        return switch (status) {
            case "Pending"   -> "⏳";
            case "Paid"      -> "✅";
            case "Cancelled" -> "❌";
            default          -> "❓";
        };
    }

    /**
     * Get payment method icon.
     */
    public String getPaymentIcon() {
        if (paymentMethod == null) return "💰";
        return switch (paymentMethod) {
            case "Cash"            -> "💵";
            case "Card"            -> "💳";
            case "Online Transfer" -> "🌐";
            case "QR Payment"      -> "📱";
            default                -> "💰";
        };
    }

    /**
     * Calculate subtotal before discount.
     */
    public double getSubtotal() {
        return treatmentFee + consultFee;
    }

    /**
     * Check if bill can be edited.
     * Only Pending bills can be modified.
     */
    public boolean isEditable() {
        return isPending();
    }

    // ── Getters ────────────────────────────────────────────────────

    public int           getId()             { return id; }
    public String        getBillCode()       { return billCode; }
    public int           getAppointmentId()  { return appointmentId; }
    public double        getTreatmentFee()   { return treatmentFee; }
    public double        getConsultFee()     { return consultFee; }
    public double        getDiscount()       { return discount; }
    public double        getTotalAmount()    { return totalAmount; }
    public String        getPaymentMethod()  { return paymentMethod; }
    public String        getStatus()         { return status; }
    public int           getSettledBy()      { return settledBy; }
    public LocalDateTime getSettledAt()      { return settledAt; }
    public int           getCreatedBy()      { return createdBy; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }
    public String        getPatientName()    { return patientName; }
    public String        getPatientCode()    { return patientCode; }
    public String        getPatientContact() { return patientContact; }
    public String        getPatientAddress() { return patientAddress; }
    public String        getDentistName()    { return dentistName; }
    public String        getTreatmentName()  { return treatmentName; }
    public LocalDate     getAptDate()        { return aptDate; }
    public LocalTime     getAptTime()        { return aptTime; }
    public String        getAptCode()        { return aptCode; }
    public String        getSettledByName()  { return settledByName; }
    public String        getCreatedByName()  { return createdByName; }

    // ── Setters ────────────────────────────────────────────────────

    public void setId(int id)                                { this.id             = id; }
    public void setBillCode(String billCode)                 { this.billCode       = billCode; }
    public void setAppointmentId(int appointmentId)          { this.appointmentId  = appointmentId; }
    public void setTreatmentFee(double treatmentFee)         { this.treatmentFee   = treatmentFee; }
    public void setConsultFee(double consultFee)             { this.consultFee     = consultFee; }
    public void setDiscount(double discount)                 { this.discount       = discount; }
    public void setTotalAmount(double totalAmount)           { this.totalAmount    = totalAmount; }
    public void setPaymentMethod(String paymentMethod)       { this.paymentMethod  = paymentMethod; }
    public void setStatus(String status)                     { this.status         = status; }
    public void setSettledBy(int settledBy)                  { this.settledBy      = settledBy; }
    public void setSettledAt(LocalDateTime settledAt)        { this.settledAt      = settledAt; }
    public void setCreatedBy(int createdBy)                  { this.createdBy      = createdBy; }
    public void setCreatedAt(LocalDateTime createdAt)        { this.createdAt      = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)        { this.updatedAt      = updatedAt; }
    public void setPatientName(String patientName)           { this.patientName    = patientName; }
    public void setPatientCode(String patientCode)           { this.patientCode    = patientCode; }
    public void setPatientContact(String patientContact)     { this.patientContact = patientContact; }
    public void setPatientAddress(String patientAddress)     { this.patientAddress = patientAddress; }
    public void setDentistName(String dentistName)           { this.dentistName    = dentistName; }
    public void setTreatmentName(String treatmentName)       { this.treatmentName  = treatmentName; }
    public void setAptDate(LocalDate aptDate)                { this.aptDate        = aptDate; }
    public void setAptTime(LocalTime aptTime)                { this.aptTime        = aptTime; }
    public void setAptCode(String aptCode)                   { this.aptCode        = aptCode; }
    public void setSettledByName(String settledByName)       { this.settledByName  = settledByName; }
    public void setCreatedByName(String createdByName)       { this.createdByName  = createdByName; }

    // ── toString ───────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", billCode='" + billCode + '\'' +
                ", patientName='" + patientName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}